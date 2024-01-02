(ns solita.etp.service.energiatodistus
  (:require [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.geo :as geo-schema]
            [solita.etp.service.json :as json]
            [solita.etp.service.kielisyys :as kielisyys]
            [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
            [solita.etp.service.energiatodistus-validation :as validation]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.postgresql.composite :as pg-composite]
            [solita.common.schema :as xschema]
            [schema.coerce :as coerce]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [flathead.flatten :as flat]
            [clojure.string :as str]
            [clojure.core.match :as match]
            [schema.core :as schema]
            [solita.common.map :as map]
            [solita.common.logic :as logic]
            [schema-tools.coerce :as stc]
            [solita.etp.service.file :as file-service]
            [solita.common.maybe :as maybe])
  (:import (org.apache.pdfbox.pdmodel PDDocument)))

; *** Require sql functions ***
(db/require-queries 'energiatodistus)

(defn find-protected-postinumerot [db min-count]
  (-> (energiatodistus-db/select-protected-postinumero-versio-kayttotarkoitus
       db
       {:min-count min-count})
      (->> (map #(select-keys % [:postinumero :versio :kayttotarkoitus])))
      set))

; *** Conversions from database data types ***
(defn coerce-energiatodistus [energiatodistus-schema]
  (coerce/coercer! energiatodistus-schema
                   (stc/or-matcher
                    stc/map-filter-matcher
                    (assoc json/json-coercions
                           geo-schema/PostinumeroFI
                           (logic/unless* nil? #(format "%05d" %))
                           schema/Num
                           xschema/parse-big-decimal))))

(def db-abbreviations
  {:perustiedot :pt
   :lahtotiedot :lt
   :tulokset :t
   :toteutunut-ostoenergiankulutus :to
   :huomiot                        :h})

(def db-toimenpide-type
  [:nimi-fi
   :nimi-sv
   :lampo
   :sahko
   :jaahdytys
   :eluvun-muutos])

(def db-userdefined_energiamuoto-type
  [:nimi
   :muotokerroin
   :ostoenergia])

(def db-userdefined_energia-type
  [:nimi-fi
   :nimi-sv
   :vuosikulutus])

(def db-kuukausierittely-type
  [:tuotto$aurinkosahko
   :tuotto$tuulisahko
   :tuotto$aurinkolampo
   :tuotto$muulampo
   :tuotto$muusahko
   :tuotto$lampopumppu
   :kulutus$sahko
   :kulutus$lampo])

(def db-ostettu-polttoaine-type
  [:nimi
   :yksikko
   :muunnoskerroin
   :maara-vuodessa])

(def db-composite-types
  {:t$kaytettavat-energiamuodot$muu    db-userdefined_energiamuoto-type
   :t$uusiutuvat-omavaraisenergiat$muu db-userdefined_energia-type
   :t$kuukausierittely                 db-kuukausierittely-type
   :to$ostettu-energia$muu             db-userdefined_energia-type
   :to$ostetut-polttoaineet$muu        db-ostettu-polttoaine-type
   :h$iv-ilmastointi$toimenpide        db-toimenpide-type
   :h$valaistus-muut$toimenpide        db-toimenpide-type
   :h$lammitys$toimenpide              db-toimenpide-type
   :h$ymparys$toimenpide               db-toimenpide-type
   :h$alapohja-ylapohja$toimenpide     db-toimenpide-type})

(defn file-key [id kieli]
  (when id (format "energiatodistukset/energiatodistus-%s-%s" id kieli)))

(defn convert-db-case [name]
  (-> name
      (str/replace #"\$u$" "\\$U")
      (str/replace #"\-ua$" "-UA")))

(defn convert-db-key-case [key]
  (-> key
      name
      convert-db-case
      keyword))

(defn- find-numeric-column-validations [db versio]
  (->>
    (energiatodistus-db/select-numeric-validations db {:versio versio})
    (map db/kebab-case-keys)
    (map #(flat/flat->tree #"\$" %))))

(defn replace-abbreviation->fullname [path]
  (reduce (fn [result [fullname abbreviation]]
            (if (str/starts-with? result (name abbreviation))
              (reduced (str/replace-first
                         result (name abbreviation) (name fullname)))
              result))
          path db-abbreviations))

(defn to-property-name [column-name]
  (-> column-name
      db/kebab-case
      convert-db-case
      replace-abbreviation->fullname
      (str/replace "$" ".")))

(defn find-numeric-validations [db versio]
  (map (comp
         #(set/rename-keys % {:column-name :property})
         #(update % :column-name to-property-name))
       (find-numeric-column-validations db versio)))

(defn- assoc-in-e-luokka [energiatodistus ks db versio]
  (assoc-in
    energiatodistus ks
    (logic/if-let*
      [e-luku (-> energiatodistus :tulokset :e-luku)
       alakayttotarkoitus-id (-> energiatodistus :perustiedot :kayttotarkoitus)
       nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)]
      (:e-luokka (e-luokka-service/find-e-luokka
          db versio alakayttotarkoitus-id nettoala e-luku)))))

(defn assoc-e-tehokkuus [energiatodistus db versio]
  (-> energiatodistus
      (assoc-in
        [:tulokset :e-luku]
        (e-luokka-service/e-luku versio energiatodistus))
      (assoc-in-e-luokka
        [:tulokset :e-luokka] db versio)))

(defn- check-value [column-name value {:keys [min max]}]
  (when (and value (or (< value min) (> value max)))
    {:property (to-property-name column-name)
     :value value
     :min min
     :max max}))

(defn check-error! [column-name value interval]
  (if-let [error (check-value column-name value interval)]
    (exception/throw-ex-info!
      (assoc error
        :type :invalid-value
        :message
        (str "Property: " (to-property-name column-name)
             " has an invalid value: " value)))))

(defn validate-db-row! [db energiatodistus versio]
  (->> (find-numeric-column-validations db versio)
       (keep (fn [{:keys [column-name] :as validation}]
               (let [value ((-> column-name str/lower-case db/kebab-case keyword)
                            energiatodistus)]
                 (check-error! column-name value (:error validation))
                 (check-value column-name value (:warning validation)))))
       doall))

(defn flat->tree [energiatodistus]
  (as-> energiatodistus %
       (map/map-values (logic/when* vector? (partial mapv flat->tree)) %)
       (dissoc % :valvonta)
       (flat/flat->tree #"\$" %)))

(defn find-sisaiset-kuormat [db versio]
  (map (comp flat->tree db/kebab-case-keys)
       (energiatodistus-db/select-sisaiset-kuormat db {:versio versio})))

(defn- assoc-laskutusosoite-id [row]
  (-> row
    (assoc :laskutusosoite-id
      (when (:laskutettava-yritys-defined row)
        (or (:laskutettava-yritys-id row) -1)))
    (dissoc :laskutettava-yritys-defined)))

(defn- assoc-laskutettava-yritys-defined [energiatodistus]
  (assoc energiatodistus :laskutettava-yritys-defined
         (or (some? (:laskutettava-yritys-id energiatodistus))
             (some? (:laskutusosoite-id energiatodistus)))))

(defn- update-laskutettava-yritys-id [energiatodistus]
  (if-let [laskutusosoite-id (:laskutusosoite-id energiatodistus)]
    (assoc energiatodistus
      :laskutettava-yritys-id
      (if (== laskutusosoite-id -1) nil laskutusosoite-id))
    energiatodistus))

(defn- validate-laskutettava-yritys-id! [db laatija-id energiatodistus]
  (if-let [laskutettava-yritys-id (:laskutettava-yritys-id energiatodistus)]
    (let [laskutusosoitteet (->> (laatija-service/find-laatija-laskutusosoitteet db laatija-id)
                                 (filter :valid) (map :id) set)]
      (when-not (contains? laskutusosoitteet laskutettava-yritys-id)
        (exception/throw-ex-info!
          :invalid-laskutusosoite
          (str "Laatija: " laatija-id " does not belong to yritys: "
               laskutettava-yritys-id " or yritys is deleted."))))))

(defn- save-laskutusosoite-id [energiatodistus]
  (-> energiatodistus
      assoc-laskutettava-yritys-defined
      update-laskutettava-yritys-id
      (dissoc :laskutusosoite-id)))

(defn schema->db-row->energiatodistus [schema]
  (comp (coerce-energiatodistus schema)
        assoc-laskutusosoite-id
        (logic/when*
         #(= (:versio %) 2013)
         #(update-in % [:tulokset :uusiutuvat-omavaraisenergiat] :muu))
        #(set/rename-keys % (set/map-invert db-abbreviations))
        flat->tree
        (partial pg-composite/parse-composite-type-literals db-composite-types)
        (partial map/map-keys convert-db-key-case)
        db/kebab-case-keys))

(def db-row->energiatodistus
  (schema->db-row->energiatodistus energiatodistus-schema/Energiatodistus))

(defn tree->flat [energiatodistus]
  (->> energiatodistus
       (flat/tree->flat "$")
       (map/map-values (logic/when* vector? (partial mapv tree->flat)))))

(defn- parseInt [str] (Integer/parseInt str))

(def energiatodistus->db-row
  (comp
   (partial pg-composite/write-composite-type-literals db-composite-types)
   #(map/map-keys (fn [k] (-> k name str/lower-case keyword)) %)
   tree->flat
   #(set/rename-keys % db-abbreviations)
   #(update-in % [:perustiedot :postinumero] (logic/unless* nil? parseInt))
   save-laskutusosoite-id
   (logic/when*
    #(= (:versio %) 2013)
    #(update-in % [:tulokset :uusiutuvat-omavaraisenergiat] (partial assoc {} :muu)))))

(defn- select-energiatodistus-for-find
  [{:keys [tila-id laatija-id draft-visible-to-paakayttaja] :as energiatodistus} whoami]
  (match/match
   [(energiatodistus-tila/tila-key tila-id)
    (-> whoami :rooli rooli-service/rooli-key)
    (= laatija-id (:id whoami))
    draft-visible-to-paakayttaja]
   [_ :laatija true _] (assoc energiatodistus :kommentti nil)
   [(:or :signed :discarded :replaced) (:or :paakayttaja :laskuttaja) _ _] energiatodistus
   [_ (:or :paakayttaja :laskuttaja) _ true] energiatodistus
   :else (exception/throw-forbidden!)))

(defn find-energiatodistus
  ([db id]
   (first (map db-row->energiatodistus
               (energiatodistus-db/select-energiatodistus db {:id id}))))
  ([db whoami id]
   (if-let [energiatodistus (find-energiatodistus db id)]
     (select-energiatodistus-for-find energiatodistus whoami))))

(def ^:private db-row->energiatodistus-for-any-laatija
  (schema->db-row->energiatodistus
    energiatodistus-schema/EnergiatodistusForAnyLaatija))

(defn find-energiatodistus-any-laatija [db id]
  (first (map db-row->energiatodistus-for-any-laatija
              (energiatodistus-db/select-energiatodistus db {:id id}))))

(defn find-korvattavat [db query]
  (map db-row->energiatodistus-for-any-laatija
       (energiatodistus-db/select-korvattavat
         db (merge {:rakennustunnus nil :postinumero nil
                    :katuosoite-fi  nil :katuosoite-sv nil}
                   (update query :postinumero
                           (maybe/lift1 #(Integer/parseInt %)))))))

(defn- throw-invalid-replace! [id msg]
  (exception/throw-ex-info! :invalid-replace (str "Replaceable energiatodistus " id msg)))

(defn assert-korvaavuus-draft! [db id energiatodistus]
  (when-let [korvattu-energiatodistus-id (:korvattu-energiatodistus-id energiatodistus)]
    (if-let [korvattava-energiatodistus (find-energiatodistus db korvattu-energiatodistus-id)]
      (cond
        (and (:korvaava-energiatodistus-id korvattava-energiatodistus)
             (not= (:korvaava-energiatodistus-id korvattava-energiatodistus) id))
        (throw-invalid-replace! korvattu-energiatodistus-id " is already replaced")
        (not (contains? #{:signed :discarded} (-> korvattava-energiatodistus :tila-id energiatodistus-tila/tila-key)))
        (throw-invalid-replace! korvattu-energiatodistus-id " is not in signed or discarded state"))
      (throw-invalid-replace! korvattu-energiatodistus-id " does not exist"))))

(defn validate-sisainen-kuorma!
  [db versio energiatodistus]
  (validation/validate-sisainen-kuorma!
    (find-sisaiset-kuormat db versio)
    (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
    energiatodistus))

(defn- validate-draft! [db id versio energiatodistus]
  (assert-korvaavuus-draft! db id energiatodistus)
  (validate-sisainen-kuorma! db versio energiatodistus))

(defn- assoc-in-default [m keys default-value]
  (if (nil? (get-in m keys))
    (assoc-in m keys default-value) m))

(defn update-perustiedot-nimi [energiatodistus]
  (let [nimi (-> energiatodistus :perustiedot :nimi)
        no-language? (-> energiatodistus :perustiedot :kieli nil?)]
    (cond-> (map/dissoc-in energiatodistus [:perustiedot :nimi])
            (or no-language? (kielisyys/fi? energiatodistus))
            (assoc-in-default [:perustiedot :nimi-fi] nimi)
            (or no-language? (kielisyys/sv? energiatodistus))
            (assoc-in-default [:perustiedot :nimi-sv] nimi))))

(defn add-energiatodistus! [db whoami versio energiatodistus]
  (validate-draft! db nil versio energiatodistus)
  (let [energiatodistus-db-row (-> energiatodistus
                                   (assoc :versio versio :laatija-id (:id whoami))
                                   (assoc-e-tehokkuus db versio)
                                   (dissoc :kommentti
                                           :bypass-validation-limits
                                           :bypass-validation-limits-reason)
                                   update-perustiedot-nimi
                                   energiatodistus->db-row)
        warnings (validate-db-row! db energiatodistus-db-row versio)]
    (validate-laskutettava-yritys-id! db (:id whoami) energiatodistus-db-row)
    {:id (-> (db/with-db-exception-translation jdbc/insert!
               db
               :energiatodistus
               energiatodistus-db-row
               db/default-opts)
             first
             :id)
     :warnings warnings}))

(defn assert-laatija! [whoami energiatodistus]
  (when (and (not= (:laatija-id energiatodistus) (:id whoami))
             (rooli-service/laatija? whoami))
    (exception/throw-forbidden!
      (str "User " (:id whoami)
           " is not the laatija of energiatodistus: "
           (:id energiatodistus)))))

(defn- select-energiatodistus-for-update
  [energiatodistus-update id tila-id rooli laskutettu?]
  (match/match
    [(energiatodistus-tila/tila-key tila-id) rooli laskutettu?]
    [:draft :laatija false] (dissoc energiatodistus-update
                                    :kommentti
                                    :bypass-validation-limits
                                    :bypass-validation-limits-reason)
    [:draft :paakayttaja false] (select-keys energiatodistus-update
                                             [:kommentti
                                              :bypass-validation-limits
                                              :bypass-validation-limits-reason])
    [:signed :laatija false] (select-keys energiatodistus-update
                                          [:laskutettava-yritys-id
                                           :laskuriviviite
                                           :pt$rakennustunnus])
    [:signed :laatija true] (select-keys energiatodistus-update
                                         [:pt$rakennustunnus])
    [:signed :paakayttaja false] (select-keys energiatodistus-update
                                              [:laskutettava-yritys-id
                                               :laskuriviviite
                                               :pt$rakennustunnus
                                               :pt$nimi-fi
                                               :pt$nimi-sv
                                               :korvattu-energiatodistus-id
                                               :kommentti])
    [:signed :paakayttaja true] (select-keys energiatodistus-update
                                             [:pt$rakennustunnus
                                              :pt$nimi-fi
                                              :pt$nimi-sv
                                              :korvattu-energiatodistus-id
                                              :kommentti])
    [:replaced :paakayttaja _] (select-keys energiatodistus-update
                                            [:kommentti])
    :else (exception/throw-forbidden!
            (str "Role: " rooli
                 " is not allowed to update energiatodistus " id
                 " in state: " (energiatodistus-tila/tila-key tila-id) " laskutettu: " laskutettu?))))

(defn- mark-energiatodistus-korvattu! [db id]
  (when id
    (when-not (== (energiatodistus-db/update-energiatodistus-korvattu! db {:id id}) 1)
      (throw-invalid-replace! id " is not in signed or discarded state"))))

(defn- revert-energiatodistus-korvattu! [db id]
  (when id (energiatodistus-db/revert-energiatodistus-korvattu! db {:id id})))

(defn- update-korvattu! [energiatodistus db tila-id current-korvattu-energiatodistus-id]
  (let [new-korvattava-energiatodistus-id (get energiatodistus :korvattu-energiatodistus-id :not-found)]
    (when (and (#{:signed :replaced :discarded} (energiatodistus-tila/tila-key tila-id))
               (not= new-korvattava-energiatodistus-id :not-found)
               (not= new-korvattava-energiatodistus-id current-korvattu-energiatodistus-id))
      (mark-energiatodistus-korvattu! db new-korvattava-energiatodistus-id)
      (revert-energiatodistus-korvattu! db current-korvattu-energiatodistus-id)))
  energiatodistus)

(defn- db-update-energiatodistus! [db id current-energiatodistus energiatodistus rooli]
  (jdbc/with-db-transaction [db db]
    (let [versio (:versio current-energiatodistus)
          tila-id (:tila-id current-energiatodistus)
          laskutettu? (-> current-energiatodistus :laskutusaika ((complement nil?)))
          current-korvattu-energiatodistus-id (:korvattu-energiatodistus-id current-energiatodistus)
          energiatodistus-db-row (-> energiatodistus
                                     (assoc-e-tehokkuus db versio)
                                     (assoc :versio versio)
                                     energiatodistus->db-row
                                     (dissoc :versio)
                                     (select-energiatodistus-for-update id
                                                                        tila-id
                                                                        rooli
                                                                        laskutettu?)
                                     (update-korvattu! db
                                                       tila-id
                                                       current-korvattu-energiatodistus-id))]
      (when-not (or (:bypass-validation-limits energiatodistus-db-row)
                    (:bypass-validation-limits current-energiatodistus))
        (validate-db-row! db energiatodistus-db-row versio))
      (validate-laskutettava-yritys-id! db (:laatija-id current-energiatodistus) energiatodistus-db-row)
      (first (db/with-db-exception-translation jdbc/update!
               db
               :energiatodistus
               energiatodistus-db-row
               ["id = ? and tila_id = ? and (laskutusaika is not null) = ?"
                id
                tila-id
                laskutettu?]
               db/default-opts)))))

(defn- assert-update! [id result]
  (if (== result 0)
    (exception/throw-ex-info!
      :update-conflict
      (str "Energiatodistus " id
           " update conflicts with other concurrent update."))
    result))

(defn find-required-properties [db versio bypass-validation]
  (cons
    "laskutusosoite-id"
    (map (comp to-property-name :column-name)
         (energiatodistus-db/select-required-columns
           db {:versio versio :bypass-validation bypass-validation}))))

(defn find-required-constraints [db energiatodistus]
  (-> (find-required-properties
        db (:versio energiatodistus)
        (:bypass-validation-limits energiatodistus))
      validation/required-constraints))

(defn validate-required!
  ([db current update]
   (validation/validate-required!
     (find-required-constraints db current)
     current update))
  ([db energiatodistus]
   (validation/validate-required!
     (find-required-constraints db energiatodistus)
     energiatodistus)))

(defn update-energiatodistus! [db whoami id energiatodistus]
  (if-let [current-energiatodistus (find-energiatodistus db id)]
    (do
      (assert-laatija! whoami current-energiatodistus)
      (if (energiatodistus-tila/draft? current-energiatodistus)
        (validate-draft! db id (:versio current-energiatodistus) energiatodistus)
        (validate-required! db
                            current-energiatodistus
                            energiatodistus))
      (assert-update!
       id
       (db-update-energiatodistus! db id
                                   current-energiatodistus
                                   energiatodistus
                                   (-> whoami :rooli rooli-service/rooli-key)))
      nil)
    (exception/throw-ex-info!
      :not-found
      (str "Energiatodistus " id " does not exists."))))

(defn delete-energiatodistus-luonnos! [db whoami id]
  (assert-laatija! whoami (find-energiatodistus db id))
  (energiatodistus-db/delete-energiatodistus-luonnos! db {:id id}))

(defn set-energiatodistus-discarded! [db id discard?]
  (if discard?
    (energiatodistus-db/discard-energiatodistus! db {:id id})
    (energiatodistus-db/undo-discard-energiatodistus! db {:id id})))

;;
;; Signing process
;;

(defn start-energiatodistus-signing! [db whoami id]
  (jdbc/with-db-transaction [db db]
    (let [result (energiatodistus-db/update-energiatodistus-allekirjoituksessa!
                   db {:id id :laatija-id (:id whoami)})
          energiatodistus (find-energiatodistus db id)]
      (if (= result 1)
        (do
          (validate-required! db energiatodistus)
          (laatija-service/validate-laatija-patevyys! db (:id whoami))
          :ok)
        (when-let [{:keys [tila-id]} energiatodistus]
          (assert-laatija! whoami energiatodistus)
          (case (energiatodistus-tila/tila-key tila-id)
            :in-signing :already-in-signing
            :deleted nil
            :already-signed))))))

(defn- failure-code [db whoami id]
  (when-let [{:keys [tila-id] :as et} (find-energiatodistus db id)]
    (assert-laatija! whoami et)
    (case (energiatodistus-tila/tila-key tila-id)
      :draft :not-in-signing
      :deleted nil
      :already-signed)))

(defn- pdf-signed? [content]
  (with-open
    [doc (PDDocument/load content)]
    (-> (.getLastSignatureDictionary doc) nil? not)))

(defn energiatodistus-pdf-signed? [aws-s3-client id language]
  (try
    (let [key (file-key id language)
          content (file-service/find-file aws-s3-client key)]
      (pdf-signed? content))
    (catch Exception _e
      false)))

(defn language-id->codes [language]
  (get {0 ["fi"]
        1 ["sv"]
        2 ["fi" "sv"]} language))

(defn- assert-energiatodistus-pdf-signed! [aws-s3-client energiatodistus]
  (let [id (:id energiatodistus)
        language (-> energiatodistus :perustiedot :kieli)]
    (doseq [language (language-id->codes language)]
      (when-not (energiatodistus-pdf-signed? aws-s3-client id language)
        (exception/throw-ex-info!
          :not-signed (str "Energiatodistus " id " pdf for language " language " is not signed"))))))

(defn end-energiatodistus-signing! [db aws-s3-client whoami id & [{:keys [skip-pdf-signed-assert?]}]]
  (jdbc/with-db-transaction [db db]
    (let [result (energiatodistus-db/update-energiatodistus-allekirjoitettu!
                   db {:id id :laatija-id (:id whoami)})]
      (if (= result 1)
        (let [energiatodistus (find-energiatodistus db id)]
          (when-not skip-pdf-signed-assert?
            (assert-energiatodistus-pdf-signed! aws-s3-client energiatodistus))
          (mark-energiatodistus-korvattu! db (:korvattu-energiatodistus-id energiatodistus))
          :ok)
        (failure-code db whoami id)))))

(defn cancel-energiatodistus-signing! [db whoami id]
  (let [result (energiatodistus-db/update-energiatodistus-luonnos!
                 db {:id id :laatija-id (:id whoami)})]
    (if (= result 1) :ok (failure-code db whoami id))))
