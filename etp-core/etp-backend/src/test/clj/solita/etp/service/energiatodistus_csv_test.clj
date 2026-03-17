(ns solita.etp.service.energiatodistus-csv-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
            [solita.etp.service.complete-energiatodistus
             :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus-csv :as service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.csv :as csv-service])
  (:import (java.time Instant)))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat-adds (laatija-test-data/generate-adds 1)
        laatijat-ids (-> laatijat-adds
                         first
                         (assoc-in [:patevyystaso] 4)
                         vector
                         (laatija-test-data/insert!))
        laatijat (zipmap laatijat-ids laatijat-adds)
        laatija-id (first laatijat-ids)
        laatija-whoami {:id laatija-id :patevyystaso 4 :rooli 0}
        energiatodistukset-2018 (energiatodistus-test-data/generate-and-insert!
                                  25
                                  2018
                                  true
                                  laatija-id)
        energiatodistukset-2026 (energiatodistus-test-data/generate-and-insert!
                                  25
                                  2026
                                  true
                                  laatija-id)
        energiatodistukset (concat energiatodistukset-2018 energiatodistukset-2026)
        et-ids-to-add-ppps-for (->> energiatodistukset-2026 keys (take 10))
        id-of-et-where-ppp-removed (first (take 1 et-ids-to-add-ppps-for))
        ids-of-ets-with-pps (into [] (remove (set [id-of-et-where-ppp-removed]) (set et-ids-to-add-ppps-for)))
        ids-of-ets-without-pps (into [] (remove (-> ids-of-ets-with-pps set) (-> energiatodistukset keys set)))

        ppp-id-for-removed (first (ppp-test-data/generate-and-insert! [id-of-et-where-ppp-removed] laatija-whoami))

        ppp-ids (ppp-test-data/generate-and-insert! ids-of-ets-with-pps laatija-whoami)
        et-id->ppp-id (zipmap ids-of-ets-with-pps ppp-ids)]
    (-> (ppp-test-data/generate-add id-of-et-where-ppp-removed)
        (assoc-in [:valid] false)
        (ppp-test-data/update! ppp-id-for-removed laatija-whoami))
    {:laatijat               laatijat
     :ppp-laatija-whoami     laatija-whoami
     :ids-of-ets-with-pps    ids-of-ets-with-pps
     :ids-of-ets-without-pps ids-of-ets-without-pps
     :ppp-removed-case       {:ppp-id ppp-id-for-removed
                              :et-id  id-of-et-where-ppp-removed}
     :et-id->ppp-id          et-id->ppp-id
     :energiatodistukset     energiatodistukset}))

(t/deftest columns-test
  (let [luokittelut (complete-energiatodistus-service/luokittelut ts/*db*)]
    (t/is (every? vector? service/private-columns))
    (t/is (every? #(or (keyword? %)
                       (integer? %))
                  (apply concat service/private-columns)))

    ;; Tests that all paths in generated energiatodistus are
    ;; found in columns listed in the service. Basically for finding typos
    ;; in configuration.
    (t/testing "ET2018 paths are covered by private-columns"
      (t/is (every? #(contains? (set service/private-columns) %)
                    (->> (energiatodistus-test-data/generate-adds 10 2018 true)
                         (map #(complete-energiatodistus-service/complete-energiatodistus
                                 %
                                 luokittelut))
                         (map #(dissoc % :kommentti :laskutusosoite-id))
                         (map #(xmap/dissoc-in % [:tulokset :kuukausierittely]))
                         (map #(xmap/dissoc-in % [:tulokset
                                                  :kuukausierittely-summat]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :katuosoite]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :postinumero]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :postitoimipaikka]))
                         (map #(xmap/dissoc-in % [:tulokset :e-luokka-rajat]))
                         (map #(xmap/dissoc-in % [:lahtotiedot :lammitys :lampohavio-lammittamaton-tila]))
                         (map xmap/paths)
                         (apply concat)
                         (filter #(every? keyword? %))))))

    ;; Tests that all paths in generated ET2026 energiatodistus are
    ;; found in columns listed in the service.
    (t/testing "ET2026 paths are covered by private-columns"
      (t/is (every? #(contains? (set service/private-columns) %)
                    (->> (energiatodistus-test-data/generate-adds 1 2026 true)
                         (map #(complete-energiatodistus-service/complete-energiatodistus
                                 %
                                 luokittelut))
                         (map #(dissoc % :kommentti :laskutusosoite-id))
                         (map #(xmap/dissoc-in % [:tulokset :kuukausierittely]))
                         (map #(xmap/dissoc-in % [:tulokset
                                                  :kuukausierittely-summat]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :katuosoite]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :postinumero]))
                         (map #(xmap/dissoc-in % [:perustiedot :yritys :postitoimipaikka]))
                         (map #(xmap/dissoc-in % [:tulokset :e-luokka-rajat]))
                         (map #(xmap/dissoc-in % [:lahtotiedot :lammitys :lampohavio-lammittamaton-tila]))
                         (map xmap/paths)
                         (apply concat)
                         (filter #(every? keyword? %))))))))

(t/deftest column-ks->str-test
  (t/is (= "" (service/column-ks->str [])))
  (t/is (= "Foo / B / 5 / X" (service/column-ks->str
                               [:foo "b" 5 :x]))))

(t/deftest csv-line-test
  (t/is (= "\n" (csv-service/csv-line [])))
  (t/is (= "\"test\";1,235;-15;2021-01-01T14:15\n"
           (csv-service/csv-line ["test"
                                  1.23456
                                  -15
                                  (Instant/parse "2021-01-01T12:15:00.000Z")]))))

(t/deftest write-energiatodistukset-csv-test
  (let [{:keys [ids-of-ets-with-pps ids-of-ets-without-pps ppp-laatija-whoami ppp-removed-case et-id->ppp-id]} (test-data-set)
        laatija-id (:id ppp-laatija-whoami)]
    (let [result (service/energiatodistukset-private-csv
                   ts/*db* {:id laatija-id :rooli 0} {})
          buffer (StringBuffer.)
          _ (result #(.append buffer %))

          et-with-ppp-id (rand-nth ids-of-ets-with-pps)
          et-without-ppp-id (rand-nth ids-of-ets-without-pps)

          csv-lines-strings (-> (.toString buffer) str/split-lines)
          header-line (first csv-lines-strings)]
      (t/testing "The csv starts with expected headers"
        (t/is (str/starts-with? header-line
                                "\"Id\";\"Perusparannuspassi-id\";\"Versio\";")))
      (t/testing "Perusparannuspassi-id shows up as expected for a todistus with perusparannuspassi"
        (let [et-with-ppp-line (->> csv-lines-strings
                                    (filter #(str/starts-with? % (str et-with-ppp-id)))
                                    first)
              et-with-ppp (energiatodistus-service/find-energiatodistus ts/*db* et-with-ppp-id)
              ppp-id (:perusparannuspassi-id et-with-ppp)]
          (t/testing "Perusparannuspassi-id is not for some other et"
            (t/is (not (nil? ppp-id)))
            (t/is (= ppp-id (et-id->ppp-id et-with-ppp-id))))
          (t/is (str/starts-with? et-with-ppp-line (str et-with-ppp-id ";" ppp-id ";")))))
      (t/testing "Perusparannuspassi-id is empty for energiatodistus without perusparannuspassi"
        (let [et-without-ppp-line (->> csv-lines-strings
                                       (filter #(str/starts-with? % (str et-without-ppp-id)))
                                       first)]
          (t/is (str/starts-with? et-without-ppp-line (str et-without-ppp-id ";;")))))
      (t/testing "Perusparannuspassi-id is empty for energiatodistus that had perusparannuspassi but it is removed"
        (let [et-without-ppp-line (->> csv-lines-strings
                                       (filter #(str/starts-with? % (str (:et-id ppp-removed-case))))
                                       first)]
          (t/is (str/starts-with? et-without-ppp-line (str (:et-id ppp-removed-case) ";;")))))

      ;; --- AE-2622: ET2026 new field columns ---

      (t/testing "Header contains ET2026 ilmastoselvitys columns"
        (t/is (str/includes? header-line "Ilmastoselvitys / Laatimisajankohta"))
        (t/is (str/includes? header-line "Ilmastoselvitys / Hiilijalanjalki / Rakennus / Rakennustuotteiden-valmistus"))
        (t/is (str/includes? header-line "Ilmastoselvitys / Hiilikadenjalki / Rakennus / Uudelleenkaytto")))

      (t/testing "Header contains ET2026 uusiutuvat omavaraisenergiat kokonaistuotanto columns"
        (t/is (str/includes? header-line "Tulokset / Uusiutuvat-omavaraisenergiat-kokonaistuotanto / Aurinkosahko")))

      (t/testing "Header contains ET2026 toteutunut ostoenergiankulutus new columns"
        (t/is (str/includes? header-line "Toteutunut-ostoenergiankulutus / Tietojen-alkuperavuosi")))

      (t/testing "Header contains kasvihuonepaastojen-muutos in huomiot toimenpide columns"
        (t/is (str/includes? header-line "Kasvihuonepaastojen-muutos")))

      (t/testing "Header contains havainnointikayntityyppi columns"
        (t/is (str/includes? header-line "Perustiedot / Havainnointikayntityyppi-id")))

      (t/testing "Header contains lahtotiedot new boolean columns"
        (t/is (str/includes? header-line "Lahtotiedot / Energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin"))
        (t/is (str/includes? header-line "Lahtotiedot / Lammitys / Lammonjako-lampotilajousto")))

      (t/testing "Header contains lammitys kayttoikaa-jaljella-arvio-vuosina column"
        (t/is (str/includes? header-line "Huomiot / Lammitys / Kayttoikaa-jaljella-arvio-vuosina")))

      (t/testing "Every data row has the same number of columns as the header"
        (let [;; Strip quoted CSV values (which may contain ;) before splitting
              strip-quotes #(str/replace % #"\"(?:[^\"]|\"\")*\"" "X")
              header-col-count (count (str/split (strip-quotes header-line) #";" -1))
              data-lines (rest csv-lines-strings)]
          (t/is (every? #(= header-col-count
                            (count (str/split (strip-quotes %) #";" -1)))
                        data-lines))))

      (t/testing "ET2026 row has non-empty ilmastoselvitys laadintaperuste value"
        (let [headers (mapv #(str/replace % #"\"" "") (str/split header-line #";"))
              laadintaperuste-idx (.indexOf headers "Ilmastoselvitys / Laadintaperuste")
              ;; Find a 2026 row - versio is the 3rd column (index 2)
              et2026-line (->> (rest csv-lines-strings)
                              (filter #(let [cols (str/split % #";" -1)]
                                         (= "2026" (nth cols 2 nil))))
                              first)
              et2026-cols (when et2026-line (str/split et2026-line #";" -1))]
          (t/is (pos? laadintaperuste-idx)
                "Ilmastoselvitys / Laadintaperuste column should exist in headers")
          (t/is (some? et2026-line)
                "There should be at least one ET2026 row in the CSV")
          (when (and et2026-cols (pos? laadintaperuste-idx))
            (t/is (not (str/blank? (nth et2026-cols laadintaperuste-idx "")))
                  "ET2026 row should have a non-empty ilmastoselvitys laadintaperuste value"))))

      (t/testing "ET2018 row has empty ilmastoselvitys laadintaperuste value"
        (let [headers (mapv #(str/replace % #"\"" "") (str/split header-line #";"))
              laadintaperuste-idx (.indexOf headers "Ilmastoselvitys / Laadintaperuste")
              ;; Find a 2018 row
              et2018-line (->> (rest csv-lines-strings)
                              (filter #(let [cols (str/split % #";" -1)]
                                         (= "2018" (nth cols 2 nil))))
                              first)
              et2018-cols (when et2018-line (str/split et2018-line #";" -1))]
          (when (and et2018-cols (pos? laadintaperuste-idx))
            (t/is (str/blank? (nth et2018-cols laadintaperuste-idx ""))
                  "ET2018 row should have empty ilmastoselvitys laadintaperuste value"))))

      (t/testing "ET2026 row has non-empty kayttoikaa-jaljella-arvio-vuosina value"
        (let [headers (mapv #(str/replace % #"\"" "") (str/split header-line #";"))
              kayttoikaa-idx (.indexOf headers "Huomiot / Lammitys / Kayttoikaa-jaljella-arvio-vuosina")
              et2026-line (->> (rest csv-lines-strings)
                              (filter #(let [cols (str/split % #";" -1)]
                                         (= "2026" (nth cols 2 nil))))
                              first)
              et2026-cols (when et2026-line (str/split et2026-line #";" -1))]
          (t/is (pos? kayttoikaa-idx)
                "Huomiot / Lammitys / Kayttoikaa-jaljella-arvio-vuosina column should exist")
          (when (and et2026-cols (pos? kayttoikaa-idx))
            (t/is (not (str/blank? (nth et2026-cols kayttoikaa-idx "")))
                  "ET2026 row should have a non-empty kayttoikaa-jaljella-arvio-vuosina value")))))))
