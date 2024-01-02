(ns solita.etp.schema.public-energiatodistus-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.public-energiatodistus
             :as public-energiatodistus-schema]))

(def example-2013
  {:allekirjoitusaika (java.time.Instant/now)
   :voimassaolo-paattymisaika (java.time.Instant/now)
   :korvaava-energiatodistus-id nil
   :tulokset {:e-luku 10
              :e-luokka "B"
              :kaytettavat-energiamuodot
              {:fossiilinen-polttoaine 25000
               :sahko 0
               :kaukojaahdytys nil
               :kaukolampo 25000
               :uusiutuva-polttoaine 5000
               :muu []}}
   :tila-id 2
   :perustiedot {:havainnointikaynti (java.time.LocalDate/now)
                 :rakennustunnus "1035150826"
                 :katuosoite-sv nil
                 :keskeiset-suositukset-fi "Suositukset FI"
                 :kieli 0
                 :nimi-fi "Rakennuksen nimi"
                 :nimi-sv nil
                 :postinumero "33100"
                 :yritys {:nimi "Joku yritys Oy"
                          :katuosoite "Hämeenkatu 1"
                          :postinumero "33100"
                          :postitoimipaikka "Tampere"}
                 :kayttotarkoitus "RT"
                 :katuosoite-fi "Hämeenkatu 10"
                 :keskeiset-suositukset-sv "Suositukset SV"
                 :valmistumisvuosi 2000}
   :lahtotiedot {:lammitetty-nettoala 1500
                 :ilmanvaihto {:tyyppi-id 4
                               :kuvaus-fi "Ilmanvaihdon kuvaus FI"
                               :kuvaus-sv "Ilmanvaihdon kuvaus SV"}
                 :lammitys {:lammitysmuoto-1 {:id 0
                                              :kuvaus-fi "Lämmitys 1 FI"
                                              :kuvaus-sv "Lämmitys 1 SV"}
                            :lammitysmuoto-2 {:id 6
                                              :kuvaus-fi "Lämmitys 2 FI"
                                              :kuvaus-sv "Lämmitys 2 SV"}
                            :lammonjako {:id 10
                                         :kuvaus-fi nil
                                         :kuvaus-sv nil}}}
   :id 200000
   :versio 2013})

(def example-2018
  (-> example-2013
      (assoc-in [:perustiedot :laatimisvaihe] 1)
      (assoc :versio 2018)))

(t/deftest Energiatodistus-test
  (t/is (= example-2013
           (schema/validate
            public-energiatodistus-schema/Energiatodistus2013
            example-2013)))
  (t/is (= example-2013
           (schema/validate
            public-energiatodistus-schema/Energiatodistus
            example-2013)))
  (t/is (= example-2018
           (schema/validate
            public-energiatodistus-schema/Energiatodistus2018
            example-2018)))
  (t/is (= example-2018
           (schema/validate
            public-energiatodistus-schema/Energiatodistus
            example-2018))))
