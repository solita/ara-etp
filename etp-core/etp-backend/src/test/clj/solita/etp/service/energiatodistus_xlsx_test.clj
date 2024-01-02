(ns solita.etp.service.energiatodistus-xlsx-test
  (:require [clojure.test :as t]
            [schema-generators.generators :as g]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.schema.energiatodistus :as schema]
            [solita.etp.service.energiatodistus-xlsx :as service]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            100
                            2018
                            false
                            (-> laatijat keys sort first))]
    {:laatijat laatijat
     :energiatodistukset energiatodistukset}))

(def energiatodistukset (energiatodistus-test-data/generate-adds 100 2018 true))

(t/deftest other-paths-test
  (t/is (empty? (service/other-paths nil)))
  (t/is (empty? (service/other-paths [])))
  (t/is (= (service/other-paths (map #(assoc % :new-key 1) energiatodistukset))
           #{[:new-key]})))

(t/deftest paths-for-k-test
  (let [perustiedot-paths (service/paths-for-k energiatodistukset :perustiedot)]
    (t/is (empty? (service/paths-for-k [] :perustiedot)))
    (t/is (empty? (service/paths-for-k energiatodistukset :non-existing-k)))
    (t/is (= (first perustiedot-paths) [:perustiedot :havainnointikaynti]))
    (t/is (contains? perustiedot-paths [:perustiedot :tilaaja]))
    (t/is (-> perustiedot-paths
              (contains? [:lahtotiedot :lammitetty-nettoala])
              not))))

(t/deftest path->str-test
  (t/is (= (service/path->str []) ""))
  (t/is (= (service/path->str [:a :b :c]) "A / B / C"))
  (t/is (= (service/path->str [1 2 3]) "1 / 2 / 3"))
  (t/is (= (service/path->str [:foo "bar" 3 :baz]) "Foo / Bar / 3 / Baz")))

(t/deftest search-completed-energiatodistukset-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (let [found-energiatodistukset (service/search-completed-energiatodistukset
                                    ts/*db*
                                    {:id laatija-id :rooli 0}
                                    {})]
      (t/is (= 100 (count found-energiatodistukset)))
      (t/is (-> (service/search-completed-energiatodistukset
                 ts/*db*
                 {:id (inc laatija-id) :rooli 0}
                 {})
                count
                zero?)))))

(t/deftest find-energiatodistukset-xlsx-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (t/is (instance? java.io.InputStream
                     (service/find-energiatodistukset-xlsx
                      ts/*db*
                      {:id laatija-id :rooli 0}
                      {})))))
