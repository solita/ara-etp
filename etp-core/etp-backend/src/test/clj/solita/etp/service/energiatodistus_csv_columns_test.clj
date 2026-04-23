(ns solita.etp.service.energiatodistus-csv-columns-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus-csv :as csv]))

(t/deftest bank-columns-count
  (t/is (= 55 (count csv/bank-columns))
        (str "bank-columns count changed unexpectedly: " (count csv/bank-columns))))

(t/deftest anonymized-columns-count
  (t/is (= 415 (count csv/anonymized-columns))
        (str "anonymized-columns count changed unexpectedly: " (count csv/anonymized-columns))))

(t/deftest tilastokeskus-columns-count
  (t/is (= 429 (count csv/tilastokeskus-columns))
        (str "tilastokeskus-columns count changed unexpectedly: " (count csv/tilastokeskus-columns))))

(t/deftest bank-columns-snapshot
  (t/is (= [[:id]
            [:versio]
            [:allekirjoitusaika]
            [:voimassaolo-paattymisaika]
            [:perustiedot :kieli]
            [:perustiedot :kieli-fi]
            [:perustiedot :laatimisvaihe]
            [:perustiedot :laatimisvaihe-fi]
            [:perustiedot :havainnointikaynti]
            [:perustiedot :nimi-fi]
            [:perustiedot :nimi-sv]
            [:perustiedot :valmistumisvuosi]
            [:perustiedot :katuosoite-fi]
            [:perustiedot :katuosoite-sv]
            [:perustiedot :postinumero]
            [:perustiedot :postitoimipaikka-fi]
            [:perustiedot :postitoimipaikka-sv]
            [:perustiedot :rakennustunnus]
            [:perustiedot :kiinteistotunnus]
            [:perustiedot :paakayttotarkoitus-id]
            [:perustiedot :paakayttotarkoitus-fi]
            [:perustiedot :kayttotarkoitus]
            [:perustiedot :alakayttotarkoitus-fi]
            [:tulokset :e-luku]
            [:tulokset :e-luokka]
            [:tulokset :e-luokka-rajat :raja-uusi-2018]
            [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]
            [:perustiedot :keskeiset-suositukset-fi]
            [:perustiedot :keskeiset-suositukset-sv]
            [:lahtotiedot :lammitetty-nettoala]
            [:lahtotiedot :ilmanvaihto :tyyppi-id]
            [:lahtotiedot :ilmanvaihto :label-fi]
            [:lahtotiedot :ilmanvaihto :kuvaus-fi]
            [:lahtotiedot :ilmanvaihto :kuvaus-sv]
            [:lahtotiedot :lammitys :lammitysmuoto-1 :id]
            [:lahtotiedot :lammitys :lammitysmuoto-2 :id]
            [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
            [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi]
            [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv]
            [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi]
            [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv]
            [:lahtotiedot :lammitys :lammonjako :id]
            [:lahtotiedot :lammitys :lammonjako-label-fi]
            [:lahtotiedot :lammitys :lammonjako :kuvaus-fi]
            [:lahtotiedot :lammitys :lammonjako :kuvaus-sv]
            [:tulokset :kaytettavat-energiamuodot :kaukolampo]
            [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin]
            [:tulokset :kaytettavat-energiamuodot :sahko]
            [:tulokset :kaytettavat-energiamuodot :sahko-kerroin]
            [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine]
            [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin]
            [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine]
            [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin]
            [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys]
            [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]]
           (vec csv/bank-columns))))

(t/deftest anonymized-columns-headers
  (let [headers (mapv csv/column-ks->str csv/anonymized-columns)]
    (t/is (= "Versio" (first headers)))
    (t/is (= "Lisamerkintoja-sv" (last headers)))
    (t/is (every? string? headers))))

(t/deftest tilastokeskus-columns-headers
  (let [headers (mapv csv/column-ks->str csv/tilastokeskus-columns)]
    (t/is (= "Id" (first headers)))
    (t/is (= "Lisamerkintoja-sv" (last headers)))
    (t/is (every? string? headers))))

(t/deftest bank-columns-no-duplicates
  (t/is (= (count csv/bank-columns)
           (count (distinct csv/bank-columns)))
        "bank-columns contains duplicates"))

(t/deftest anonymized-columns-no-duplicates
  (t/is (= (count csv/anonymized-columns)
           (count (distinct csv/anonymized-columns)))
        "anonymized-columns contains duplicates"))

(t/deftest tilastokeskus-columns-no-duplicates
  (t/is (= (count csv/tilastokeskus-columns)
           (count (distinct csv/tilastokeskus-columns)))
        "tilastokeskus-columns contains duplicates"))
