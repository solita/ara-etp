(ns solita.etp.schema.public-energiatodistus
  (:require [schema.core :as schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.common :as common-schema]))

(def Perustiedot
  (select-keys energiatodistus-schema/Perustiedot
               [:katuosoite-fi :katuosoite-sv :valmistumisvuosi
                :havainnointikaynti :rakennustunnus :postinumero
                :keskeiset-suositukset-fi :keskeiset-suositukset-sv
                :laatimisvaihe :yritys :kieli :nimi-fi :nimi-sv :kayttotarkoitus]))

(def LahtotiedotIlmanvaihto
  (select-keys energiatodistus-schema/LahtotiedotIlmanvaihto
               [:tyyppi-id :kuvaus-fi :kuvaus-sv]))

(def LahtotiedotLammitys
  (select-keys energiatodistus-schema/LahtotiedotLammitys
               [:lammitysmuoto-1 :lammitysmuoto-2 :lammonjako]))

(def Lahtotiedot
  (-> energiatodistus-schema/Lahtotiedot
      (select-keys [:lammitetty-nettoala])
      (assoc :ilmanvaihto LahtotiedotIlmanvaihto)
      (assoc :lammitys LahtotiedotLammitys)))

(def Tulokset
  (-> energiatodistus-schema/Tulokset
      (select-keys [:kaytettavat-energiamuodot])
      (assoc-in [:kaytettavat-energiamuodot :muu]
                [(energiatodistus-schema/optional-properties
                  energiatodistus-schema/UserDefinedEnergiamuoto)])))

(defn energiatodistus-versio [versio]
  (-> (energiatodistus-schema/energiatodistus-versio
       versio
       (energiatodistus-schema/optional-properties
        {:perustiedot Perustiedot
         :lahtotiedot Lahtotiedot
         :tulokset    Tulokset}))
      (dissoc :laatija-fullname
              :laatija-id
              :laskutusaika)))

(def Energiatodistus2013
  (-> (energiatodistus-versio 2013)
      energiatodistus-schema/dissoc-not-in-2013))

(def Energiatodistus2018 (energiatodistus-versio 2018))

(def Energiatodistus
  (schema/conditional
   (partial energiatodistus-schema/versio? 2013) Energiatodistus2013
   (partial energiatodistus-schema/versio? 2018) Energiatodistus2018))
