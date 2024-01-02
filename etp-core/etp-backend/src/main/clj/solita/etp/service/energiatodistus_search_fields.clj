(ns solita.etp.service.energiatodistus-search-fields
  (:require [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.e-luokka :as e-luokka]
            [solita.etp.service.polttoaine :as polttoaine]
            [solita.etp.db :as db]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.laatija :as laatija-schema]
            [clojure.string :as str]
            [flathead.deep :as deep]
            [solita.etp.schema.common :as common-schema])
  (:import (clojure.lang IPersistentVector)))

(defn- abbreviation [identifier]
  (or (some-> identifier keyword energiatodistus-service/db-abbreviations name)
      identifier))

(defn field->db-column
  "If search field represents persistent column in database
   this returns a fullname of corresponding database column. "
  [[table & field-parts]]
  (str table "."
       (as-> field-parts $
             (vec $)
             (update $ 0 abbreviation)
             (map db/snake-case $)
             (str/join "$" $))))

(defn- per-nettoala-sql [value-expression]
  (str value-expression " / nullif(energiatodistus.lt$lammitetty_nettoala, 0)"))

(defn- per-nettoala-entry [^IPersistentVector path rename]
  (fn [[key schema]]
    (let [computed-field-key (-> key name rename keyword)
          field-parts (concat ["energiatodistus"] path [(name key)])]
      [computed-field-key
       [(per-nettoala-sql (field->db-column field-parts)) schema]])))

(defn- per-nettoala-for-schema
  ([path rename-key schema]
    (into {} (map (per-nettoala-entry (map name path) rename-key))
          (get-in schema path))))

(defn- painotettu-kulutus-sql [path key]
  (str (field->db-column (conj path (name key))) " * (case energiatodistus.versio"
       " when 2013 then "
       (get-in e-luokka/energiamuotokerroin [2013 key])
       " when 2018 then "
       (get-in e-luokka/energiamuotokerroin [2018 key])
       " end)"))

(defn- painotettu-kulutus-entry [[key schema]]
  [(-> key name (str "-painotettu") keyword)
   [(painotettu-kulutus-sql
      ["energiatodistus" "tulokset" "kaytettavat-energiamuodot"]
      key)
    schema]])

(defn- computed-field-neliovuosikulutus [[key [sql-expression schema]]]
  [(-> key name (str "-neliovuosikulutus") keyword)
   [(per-nettoala-sql sql-expression)
    schema]])

(def ^:private kaytettavat-energiamuodot-painotettu-kulutus
  (into {} (map painotettu-kulutus-entry)
        (:kaytettavat-energiamuodot energiatodistus-schema/Tulokset)))

(defn- ua-sql [key]
  (let [db-name (-> key name db/snake-case)]
    (str "energiatodistus.lt$rakennusvaippa$" db-name "$u * "
         "energiatodistus.lt$rakennusvaippa$" db-name "$ala")))

(def ^:private ua-fields
  (into {}
        (comp
          (filter (fn [[_ value]] (= value energiatodistus-schema/Rakennusvaippa)))
          (map (fn [[key _]] [key {:UA [(ua-sql key) common-schema/NonNegative]}])))
        energiatodistus-schema/LahtotiedotRakennusvaippa))

(def ^:private ua-total-sql
  (->> ua-fields
       (map (fn [[_ value]] (-> value :UA first)))
       (str/join " + ")
       (str "energiatodistus.lt$rakennusvaippa$kylmasillat_ua + ")))

(def ^:private osuus-lampohaviosta-fields
  (into {:kylmasillat-osuus-lampohaviosta
         [(str "energiatodistus.lt$rakennusvaippa$kylmasillat_ua / nullif((" ua-total-sql "), 0)")
          common-schema/NonNegative]}
        (map (fn [[key value]]
               [key {:osuus-lampohaviosta
                     [(str (-> value :UA first) " / nullif((" ua-total-sql "), 0)")
                      common-schema/NonNegative]}]))
        ua-fields))

(def computed-fields
  "Computed field consists of sql expression and value schema [sql, schema]"
  {:energiatodistus
   {:lahtotiedot
    {:rakennusvaippa (deep/deep-merge ua-fields osuus-lampohaviosta-fields)}
    :tulokset
    {:kaytettavat-energiamuodot
     (merge
       kaytettavat-energiamuodot-painotettu-kulutus
       (into {} (map computed-field-neliovuosikulutus)
             kaytettavat-energiamuodot-painotettu-kulutus))
     :uusiutuvat-omavaraisenergiat
     (per-nettoala-for-schema
       [:tulokset :uusiutuvat-omavaraisenergiat]
       #(str % "-neliovuosikulutus")
       energiatodistus-schema/Energiatodistus2018)
     :nettotarve
     (per-nettoala-for-schema
       [:tulokset :nettotarve]
       #(str/replace % "vuosikulutus" "neliovuosikulutus")
       energiatodistus-schema/Energiatodistus2018)
     :lampokuormat
     (per-nettoala-for-schema
       [:tulokset :lampokuormat]
       #(str % "-neliovuosikuorma")
       energiatodistus-schema/Energiatodistus2018)}
    :toteutunut-ostoenergiankulutus
    {:ostettu-energia
     (per-nettoala-for-schema
       [:toteutunut-ostoenergiankulutus :ostettu-energia]
       #(str/replace % "vuosikulutus" "neliovuosikulutus")
       energiatodistus-schema/Energiatodistus2018)
     :ostetut-polttoaineet
     (into {} (map (fn [[polttoaine factor]]
                     (let [path-prefix [:toteutunut-ostoenergiankulutus
                                        :ostetut-polttoaineet]
                           path (concat path-prefix [polttoaine])
                           field-name (keyword (str (name polttoaine) "-neliovuosikulutus"))
                           field-parts (concat ["energiatodistus"] (map name path))
                           field-value (per-nettoala-sql (str factor " * " (field->db-column field-parts)))
                           field-schema (get-in energiatodistus-schema/Energiatodistus2018 path)]
                       [field-name [field-value field-schema]]))
                polttoaine/muunnoskertoimet))}}
   :laatija
   {:voimassaolo-paattymisaika ["patevyys_paattymisaika(laatija)"
                                (get-in laatija-schema/Laatija [:voimassaolo-paattymisaika])]}})
