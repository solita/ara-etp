(ns solita.etp.service.energiatodistus-performance
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.db :as db]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-test :as energiatodistus-test]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn add-energiatodistukset! [db laatija-id versio energiatodistukset]
  (let [db-rows (map #(-> % (assoc :versio versio :laatija-id laatija-id)
                          energiatodistus-service/energiatodistus->db-row)
                     energiatodistukset)]
    (jdbc/insert-multi!
      db :energiatodistus
      (keys (first db-rows))
      (map vals db-rows)
      db/default-opts)))

;; This test can be used to generate test data for manual performance testing
;; 1. remove drop test database in test-system namespace
;; 2. run this test (run-test #'solita.etp.service.energiatodistus-test/add-energiatodistukset-for-performance-testing)
#_(t/deftest add-energiatodistukset-for-performance-testing
  (let [laatija-id (energiatodistus-test/add-laatija!)
        energiatodistukset (vec (apply pcalls (repeat 5000 energiatodistus-test/generate-energiatodistus-2018)))
        db (ts/db-user laatija-id)]
   (time
     (doseq [batch (partition 1000 energiatodistukset)]
       (time (add-energiatodistukset! db laatija-id 2018 batch))))))
