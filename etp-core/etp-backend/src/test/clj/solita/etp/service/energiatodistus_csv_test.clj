(ns solita.etp.service.energiatodistus-csv-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.schema.energiatodistus :as schema]
            [solita.etp.service.complete-energiatodistus
             :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus-csv :as service]
            [solita.etp.service.csv :as csv-service])
  (:import (java.time Instant)))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            100
                            2018
                            true
                            (-> laatijat keys sort first))]
    {:laatijat laatijat
     :energiatodistukset energiatodistukset}))

(t/deftest columns-test
  (let [luokittelut (complete-energiatodistus-service/luokittelut ts/*db*)]
    (t/is (every? vector? service/private-columns))
    (t/is (every? #(or (keyword? %)
                       (integer? %))
                  (apply concat service/private-columns)))

    ;; Tests that all paths in generated energiatodistus are
    ;; found in columns listed in the service. Basically for finding typos
    ;; in configuration.
    (t/is (every? #(contains? (set service/private-columns) %)
                  (->> (energiatodistus-test-data/generate-adds 100 2018 true)
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
                       (filter #(every? keyword? %)))))))

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
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (let [result (service/energiatodistukset-private-csv
                   ts/*db* {:id laatija-id :rooli 0} {})
          buffer (StringBuffer.)]
      (result #(.append buffer %))
      (t/is (str/starts-with? (.toString buffer)
                              "\"Id\";\"Versio\";")))))
