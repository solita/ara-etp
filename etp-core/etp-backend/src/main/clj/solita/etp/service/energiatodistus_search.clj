(ns solita.etp.service.energiatodistus-search
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-search-fields :as search-fields]
            [schema.core :as schema]
            [solita.common.map :as m]
            [solita.common.schema :as xschema]
            [flathead.deep :as deep]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.public-energiatodistus :as public-energiatodistus-schema]
            [solita.etp.schema.geo :as geo-schema]
            [flathead.flatten :as flat]
            [schema.coerce :as coerce]
            [solita.etp.service.json :as json]
            [solita.etp.service.rooli :as rooli-service])
  (:import (schema.core Constrained Predicate EqSchema)
           (clojure.lang ArityException)))

(defn- illegal-argument! [msg]
  (throw (IllegalArgumentException. msg)))

(defn- throw-ex-info [map]
  (throw (ex-info (:message map) map)))

(defn- schema-coercers [schema]
  (cond
    (class? schema) (coerce/coercer! schema json/json-coercions)
    (= schema schema/Int) (coerce/coercer! schema/Int json/json-coercions)
    (xschema/maybe? schema) (schema-coercers (:schema schema))
    (instance? Constrained schema) (schema-coercers (:schema schema))
    (instance? EqSchema schema) (schema-coercers (class (:v schema)))
    (instance? Predicate schema) (illegal-argument! (str "Predicate schema element: " + schema + " is not supported"))
    (map? schema) (m/map-values schema-coercers schema)
    (vector? schema) (mapv schema-coercers schema)
    (coll? schema) (map schema-coercers schema)
    :else (illegal-argument! (str "Unsupported schema element: " schema))))


(defn schemas->search-schema [& schemas]
  (->> schemas
       (map schema-coercers)
       (apply deep/deep-merge)
       (flat/tree->flat ".")))

(def private-search-schema
  (schemas->search-schema
    {:energiatodistus energiatodistus-schema/Energiatodistus2013}
    {:energiatodistus energiatodistus-schema/Energiatodistus2018}
    {:energiatodistus
     {:perustiedot
      {:nimi        schema/Str
       :postinumero schema/Int}}}
    {:laatija
     {:patevyystaso common-schema/Key
      :toteamispaivamaara common-schema/Date
      :voimassaolo-paattymisaika common-schema/Instant}}
    {:postinumero {:label common-schema/String50}}
    (deep/map-values second search-fields/computed-fields)
    geo-schema/Search))

(def bilingual-fields #{"energiatodistus.perustiedot.nimi"
                        "postinumero.label"})

(def multiplexed-fields #{"energiatodistus.lahtotiedot.lammitys.lammitysmuoto"})

(def public-search-schema
  (schemas->search-schema
    {:energiatodistus public-energiatodistus-schema/Energiatodistus2013}
    {:energiatodistus public-energiatodistus-schema/Energiatodistus2018}
    {:energiatodistus
     {:perustiedot
      {:nimi schema/Str
       :postinumero schema/Int}}}
    geo-schema/Search))

(defn- schema-contains? [key schema]
  (if (xschema/conditional? schema)
    (not (not-any? (partial schema-contains? key) (map second (:preds-and-schemas schema))))
    (contains? schema key)))

(def ^:private select-expressions
  {any? "energiatodistus.*"
   (partial schema-contains? :laatija-fullname)
   ["fullname(kayttaja.*) laatija_fullname"]
   (partial schema-contains? :korvaava-energiatodistus-id)
   ["korvaava_energiatodistus.id as korvaava_energiatodistus_id"]
   (partial schema-contains? :valvonta)
   ["coalesce(last_toimenpide.ongoing, false) valvonta$ongoing"
    "last_toimenpide.type_id valvonta$type_id"]})

(defn select [schema]
  (str "select "
       (->> select-expressions
            (map (fn [[has? expression]] (if (has? schema) expression [])))
            flatten
            (str/join ",\n"))))

(def relation
  "FROM energiatodistus
   INNER JOIN kayttaja ON kayttaja.id = energiatodistus.laatija_id
   INNER JOIN laatija ON laatija.id = energiatodistus.laatija_id
   LEFT JOIN vo_last_toimenpide_v1 last_toimenpide ON last_toimenpide.energiatodistus_id = energiatodistus.id
   LEFT JOIN energiatodistus korvaava_energiatodistus
     ON korvaava_energiatodistus.korvattu_energiatodistus_id = energiatodistus.id
   LEFT JOIN postinumero ON postinumero.id = energiatodistus.pt$postinumero
   LEFT JOIN kunta ON kunta.id = postinumero.kunta_id
   LEFT JOIN toimintaalue ON toimintaalue.id = kunta.toimintaalue_id")

(defn- coercer! [field search-schema]
  (if-let [coercer (some-> field keyword search-schema)]
    coercer
    (throw-ex-info {:type :unknown-field :field field
                    :message (str "Unknown field: " field)})))

(defn validate-field! [field search-schema]
  (coercer! field search-schema)
  field)

(defn coerce-value! [field value search-schema]
  ((coercer! field search-schema) value))

(defn field->sql [field search-schema]
  (validate-field! field search-schema)
  (let [field-parts (str/split field #"\.")
        computed-field (get-in search-fields/computed-fields (map keyword field-parts))]
    (if (nil? computed-field)
      (search-fields/field->db-column field-parts)
      (first computed-field))))

(defn icontains-expression [search-schema _ field value]
  [(str "0 != position(lower(?) in lower(" (field->sql field search-schema) "))")
   (coerce-value! field value search-schema)])

(defn infix-notation [search-schema operator field value]
  [(str (field->sql field search-schema) " " operator " ?")
   (coerce-value! field value search-schema)])

(defn between-expression [search-schema _ field value1 value2]
  [(str (field->sql field search-schema) " between ? and ?")
   (coerce-value! field value1 search-schema)
   (coerce-value! field value2 search-schema)])

(defn is-null-expression [search-schema _ field]
  [(str (field->sql field search-schema) " is null")])

(defn in-expression [search-schema _ field values]
  [(str (field->sql field search-schema) " = any (?)")
   (mapv #(coerce-value! field % search-schema) values)])

(defn expression-seq->sql [logic-operator expression->sql expressions]
  (let [sql-expressions (map expression->sql expressions)]
    (cons (str/join (format " %s " logic-operator) (map first sql-expressions))
          (mapcat rest sql-expressions))))

(defn- glob-pattern-matcher [pattern]
  (let [field (subs pattern 0 (- (count pattern) 2))
        matcher (fn [language] #(= % (str field "-" language)))]
    (some-fn (matcher "fi") (matcher "sv"))))

(defn- validate-match! [pattern fields]
  (when (empty? fields)
    (throw-ex-info {:type :unknown-field :field pattern
                    :message (str "Field glob pattern: " pattern " does not match any fields.")})))

(defn- globbing [predicate]
  (fn [search-schema operator field & values]
    (if (str/includes? field "*")
      (let [fields (filter (glob-pattern-matcher field) (->> search-schema keys (map name)))]
        (validate-match! field fields)
        (update
          (vec (expression-seq->sql "or" #(apply predicate search-schema operator % values) fields)) 0
          #(str "(" % ")")))
      (apply predicate search-schema operator field values))))

(def predicates
  {"="  infix-notation
   ">=" infix-notation
   "<=" infix-notation
   ">"  infix-notation
   "<"  infix-notation
   "icontains" icontains-expression
   "like"  (globbing infix-notation)
   "ilike"  (globbing infix-notation)
   "not ilike" infix-notation
   "between" between-expression
   "nil?" is-null-expression
   "in" in-expression})

(defn- sql-formatter! [predicate-name]
  (if-let [formatter (predicates predicate-name)]
    formatter
    (throw-ex-info {:type :unknown-predicate :predicate predicate-name
                    :message (str "Unknown predicate: " predicate-name)})))

(defn- expand-bilingual-expression [formatter search-schema predicate field & values]
  (let [logical-op (if (= predicate "not ilike") "and" "or")
        [fi-sql & fi-values] (apply formatter search-schema predicate (str field "-fi") values)
        [sv-sql & sv-values] (apply formatter search-schema predicate (str field "-sv") values)]
    (concat [(str "((" fi-sql ")" logical-op "(" sv-sql "))")]
            fi-values sv-values)))

(defn- expand-multiplexed-expression
  "Expands expressions for fields where the actual db field has suffixes -1 and -2 somewhere in the search path, such as
  energiatodistus.lahtotiedot.lammitys.lammitysmuoto(-1|-2).id. Could be modified to use with other suffixes as well,
  such as language options"
  [formatter search-schema predicate search-field multiplexed-field & values]
  (let [logical-op "or"
        suffix (-> search-field (str/split (re-pattern multiplexed-field) 2) last)
        [sql-1 & values-1] (apply formatter search-schema predicate (str multiplexed-field "-1" suffix) values)
        [sql-2 & values-2] (apply formatter search-schema predicate (str multiplexed-field "-2" suffix) values)]
    (concat [(str "((" sql-1 ")" logical-op "(" sql-2 "))")]
            values-1 values-2)))

(defn predicate-expression->sql [search-schema expression]
  (let [[predicate field & values] expression
        formatter (sql-formatter! predicate)
        multiplexed-field (if (some? field)
                            (first (filter #(str/starts-with? field %) multiplexed-fields))
                            nil)]
    (try
      (cond
        (contains? bilingual-fields field) (apply expand-bilingual-expression formatter search-schema predicate field values)
        (some? multiplexed-field) (apply expand-multiplexed-expression formatter search-schema predicate field multiplexed-field values)
        :else (apply formatter search-schema predicate field values))
      (catch ArityException _
        (throw-ex-info {:type :invalid-arguments :predicate predicate
                        :message (str "Wrong number of arguments: " (rest expression)
                                      " for predicate: " predicate)})))))

(defn where->sql [where search-schema]
  (expression-seq->sql
    "or"
    #(expression-seq->sql
       "and" (partial predicate-expression->sql search-schema) %)
    where))

(defn keyword->sql [keyword]
  (when (-> keyword str/blank? not)
    (concat
      ["postinumero.id::text = ltrim(?, '0') OR kunta.label_fi ILIKE ? OR
       kunta.label_sv ILIKE ? OR toimintaalue.label_fi ILIKE ? OR
       toimintaalue.label_sv ILIKE ? OR
       energiatodistus.pt$katuosoite_fi ILIKE ? OR
       energiatodistus.pt$katuosoite_sv ILIKE ?"]
      [keyword]
      (repeat 6 (str keyword "%")))))

(defn whoami->sql [{:keys [id] :as whoami}]
  (cond
    (or (rooli-service/paakayttaja? whoami)
        (rooli-service/laskuttaja? whoami))
    ["(energiatodistus.tila_id IN (2, 3, 4) OR
       (energiatodistus.draft_visible_to_paakayttaja AND
        energiatodistus.tila_id <> 5))"]

    (rooli-service/laatija? whoami)
    ["energiatodistus.laatija_id = ? AND energiatodistus.tila_id <> 5" id]

    (rooli-service/public? whoami)
    ["energiatodistus.tila_id = 2 AND
     ((energiatodistus.versio = 2013 AND
       energiatodistus.pt$kayttotarkoitus NOT IN ('YAT', 'KAT', 'MEP', 'MAEP'))
      OR
      (energiatodistus.versio = 2018 AND
       energiatodistus.pt$kayttotarkoitus NOT IN ('YAT', 'KAT', 'KREP')))"]
    (rooli-service/system? whoami)
    ["energiatodistus.tila_id IN (2, 4)"]))

(defn search-schema [whoami]
  (if (rooli-service/public? whoami)
    public-search-schema
    private-search-schema))

(defn sql-query [select whoami {:keys [sort order limit offset where keyword]}]
  (schema/validate [[[(schema/one schema/Str "predicate") schema/Any]]] where)
  (let [search-schema (search-schema whoami)
        [where-sql & where-params] (where->sql where search-schema)
        [keyword-sql & keyword-params] (keyword->sql keyword)
        [visibility-sql & visibility-params] (whoami->sql whoami)]
    (concat [(str select \newline
                  relation \newline
                  "where "
                  visibility-sql
                  (when-not (str/blank? where-sql)
                    (format " and (%s) " where-sql))
                  (when-not (str/blank? keyword-sql)
                    (format " and (%s) " keyword-sql))
                  (when-not (str/blank? sort)
                    (str \newline "order by " (field->sql sort search-schema) " " (or order "asc")))
                  (when limit (str \newline "limit " limit))
                  (when-not (nil? offset) (str \newline "offset " offset)))]
            visibility-params
            where-params
            keyword-params)))

(defn search
  "Energiatodistus search for APIs. Returns only public data and makes sure that
   there's a sensible limit for results. Coerces results with
   schema->db-row->energiatodistus."
  [db whoami query schema]
  (let [query (update query :limit #(min 1000 (or % 1000)))]
    (->> (jdbc/query db (sql-query (select schema) whoami query) nil)
         (map (energiatodistus-service/schema->db-row->energiatodistus schema)))))

(defn reducible-search
  "Energiatodistus search for other services. Does reducibly-query meaning that
   there are certain limitations of how results can be handled. Does not coerce
   with db-row->energiatodistus, so that must be done manually if needed."
  [db whoami query options]
  (jdbc/reducible-query db (sql-query (select energiatodistus-schema/Energiatodistus) whoami query) options))

(defn search-count [db whoami query]
  (first (jdbc/query db (sql-query "select count(*) count" whoami query) nil)))
