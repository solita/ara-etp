(ns solita.etp.service.csv-to-s3-test
  (:require [solita.etp.service.csv-to-s3 :as csv-to-s3]
            [solita.etp.service.file :as file]
            [clojure.test :as t]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]))

(t/use-fixtures :each ts/fixture)

(def public-columns #{[:id]
                      [:versio]
                      [:allekirjoitusaika]
                      [:voimassaolo-paattymisaika]
                      [:perustiedot :kieli]
                      [:perustiedot :laatimisvaihe]
                      [:perustiedot :havainnointikaynti]
                      [:perustiedot :nimi-fi]
                      [:perustiedot :nimi-sv]
                      [:perustiedot :valmistumisvuosi]
                      [:perustiedot :katuosoite-fi]
                      [:perustiedot :katuosoite-sv]
                      [:perustiedot :postinumero]
                      [:perustiedot :rakennustunnus]
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
                      [:lahtotiedot :ilmanvaihto :kuvaus-fi]
                      [:lahtotiedot :ilmanvaihto :kuvaus-sv]
                      [:lahtotiedot :lammitys :lammitysmuoto-1 :id]
                      [:lahtotiedot :lammitys :lammitysmuoto-2 :id]
                      [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi]
                      [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv]
                      [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi]
                      [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv]
                      [:lahtotiedot :lammitys :lammonjako :id]
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
                      [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]
                      [:tulokset :kaytettavat-energiamuodot :muu 0 :nimi]
                      [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia]
                      [:tulokset :kaytettavat-energiamuodot :muu 0 :muotokerroin]
                      [:tulokset :kaytettavat-energiamuodot :muu 1 :nimi]
                      [:tulokset :kaytettavat-energiamuodot :muu 1 :ostoenergia]
                      [:tulokset :kaytettavat-energiamuodot :muu 1 :muotokerroin]
                      [:tulokset :kaytettavat-energiamuodot :muu 2 :nimi]
                      [:tulokset :kaytettavat-energiamuodot :muu 2 :ostoenergia]
                      [:tulokset :kaytettavat-energiamuodot :muu 2 :muotokerroin]})

(defn column-ks->str [ks]
  (->> ks
       (map #(if (keyword? %) (name %) %))
       (map str/capitalize)
       (str/join " / ")))

(defn columns->header-strings [columns]
  (->> columns
       (map column-ks->str)
       set))

(t/deftest test-public-csv-to-s3
  (t/testing "Public csv doesn't exist before generating"
    (t/is (false? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key))))

  (t/testing "Public csv exists after generating"
    (csv-to-s3/update-public-csv-in-s3! ts/*db* nil ts/*aws-s3-client* {:where nil})
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key)))))

(defn get-first-three-lines-from-csv [key]
  (with-open [csv-file (file/find-file ts/*aws-s3-client* key)
              reader (clojure.java.io/reader csv-file)]
    (let [csv-data (csv/read-csv reader :separator \;)
          [header first-line second-line third-line] (take 4 csv-data)]
      [header first-line second-line third-line])))

(t/deftest test-update-public-csv-in-s3!-handles-empty-query
  (t/testing "CSV contains expected columns when energiatodistus exists"
    ;; Generate and insert test data as required
    (csv-to-s3/update-public-csv-in-s3! ts/*db* nil ts/*aws-s3-client* {:where nil})
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key))
          "CSV should exist after generation.")

    (let [[headers & _] (get-first-three-lines-from-csv csv-to-s3/public-csv-key)
          hidden-columns #{[:laatija-fullname]
                           [:tila-id]
                           [:korvaava-energiatodistus-id]
                           [:laatija-id]
                           [:perustiedot :yritys :nimi]}
          header-set (set headers)]

      ;; Verify that all expected headers from public-columns are present in the CSV
      (t/is (set/subset? (columns->header-strings public-columns) header-set)
            "CSV headers should include all public-columns definitions")

      ;; Transform hidden-columns to header strings
      (let [hidden-header-strings (columns->header-strings hidden-columns)]

        ;; Verify that no hidden columns are present in the CSV headers
        (t/is (empty? (set/intersection hidden-header-strings header-set))
              "CSV should not contain any hidden columns")))))

(t/deftest test-update-public-csv-in-s3!-with-multiple-data
  (let [;; Generate three different rakennustunnus
        rakennustunnus-1 (generators/generate-rakennustunnus)
        rakennustunnus-2 (generators/generate-rakennustunnus)
        rakennustunnus-3 (generators/generate-rakennustunnus)

        ;; Insert laatija and get laatija-id
        laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))

          ;; Define fixed sisainen-kuorma
        fixed-sisainen-kuorma-pk {:henkilot {:kayttoaste 0.6M, :lampokuorma 14M}
                                  :kuluttajalaitteet {:kayttoaste 0.6M, :lampokuorma 8M}
                                  :valaistus {:kayttoaste 0.6M}}

        fixed-sisainen-kuorma-yat {:henkilot {:kayttoaste 0.6M, :lampokuorma 2M}
                                   :kuluttajalaitteet {:kayttoaste 0.6M, :lampokuorma 3M}
                                   :valaistus {:kayttoaste 0.1M}}

        ;; Create three energiatodistus:
        ;; - todistus-1 with kayttotarkoitus "PK"
        ;; - todistus-2 with kayttotarkoitus "PK"
        ;; - todistus-3 with kayttotarkoitus "YAT"
        todistus-1 (-> (test-data.energiatodistus/generate-add 2018 true)
                       (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1)
                       (assoc-in [:perustiedot :kayttotarkoitus] "PK")
                       (assoc-in [:lahtotiedot :sis-kuorma] fixed-sisainen-kuorma-pk))
        todistus-2 (-> (test-data.energiatodistus/generate-add 2018 true)
                       (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-2)
                       (assoc-in [:perustiedot :kayttotarkoitus] "PK")
                       (assoc-in [:lahtotiedot :sis-kuorma] fixed-sisainen-kuorma-pk))
        todistus-3 (-> (test-data.energiatodistus/generate-add 2018 true)
                       (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-3)
                       (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                       (assoc-in [:lahtotiedot :sis-kuorma] fixed-sisainen-kuorma-yat))

        ;; Insert all three todistus
        [todistus-1-id] (test-data.energiatodistus/insert! [todistus-1] laatija-id)
        [todistus-2-id] (test-data.energiatodistus/insert! [todistus-2] laatija-id)
        [todistus-3-id] (test-data.energiatodistus/insert! [todistus-3] laatija-id)]

    ;; Sign all three todistus
    (test-data.energiatodistus/sign! todistus-1-id laatija-id true)
    (test-data.energiatodistus/sign! todistus-2-id laatija-id true)
    (test-data.energiatodistus/sign! todistus-3-id laatija-id true)

    ;; Ensure CSV doesn't exist before generating
    (t/is (false? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key))
          "CSV should not exist before generation.")

    ;; Generate CSV
    (csv-to-s3/update-public-csv-in-s3! ts/*db* nil ts/*aws-s3-client* {:where nil})

    ;; Verify CSV was created
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key))
          "CSV should exist after generation.")

    (t/testing "CSV contains only signed energiatodistus data with kayttotarkoitus 'PK' and excludes 'YAT'"
      ;; Rakennustunnus matches the first generated energiatodistus
      (let [[_ first _] (get-first-three-lines-from-csv csv-to-s3/public-csv-key)]
        (t/is (true? (str/includes? first rakennustunnus-1))))

      ;; Rakennustunnus matches the second generated energiatodistus
      (let [[_ _ second] (get-first-three-lines-from-csv csv-to-s3/public-csv-key)]
        (t/is (true? (str/includes? second rakennustunnus-2))))

      ;; Rakennustunnus does not match the third generated energiatodistus
      (let [[_ _ _ third] (get-first-three-lines-from-csv csv-to-s3/public-csv-key)]
        (t/is (true? (nil? third)))))))

