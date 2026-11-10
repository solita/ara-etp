(ns solita.etp.service.energiatodistus-kaukolampo-kerroin-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.db :as db]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts]))
(t/use-fixtures :each ts/fixture)

(db/require-queries 'energiatodistus)
(def ^:private kerroin-path [:tulokset :kaytettavat-energiamuodot-kertoimet :kaukolampo])
(t/deftest schema-validation-test
  (t/testing "null and the limits are valid"
    (doseq [v [nil 0 1 0.2 0.38 0.3M 0.25]]
      (t/is (nil? (schema/check energiatodistus-schema/KaukolampoKerroin2027 v)) (str v))))
  (t/testing "values outside the limits are invalid"
    (doseq [v [-0.1 1.1 -0.3]]
      (t/is (some? (schema/check energiatodistus-schema/KaukolampoKerroin2027 v)) (str v))))
  (t/testing "field is not allowed in ET2013 and ET2018"
    (doseq [s [energiatodistus-schema/EnergiatodistusSave2013
               energiatodistus-schema/EnergiatodistusSave2018]]
      (t/is (nil? (get-in s [:tulokset :kaytettavat-energiamuodot-kertoimet :kaukolampo]))))))
(defn- update-kerroin! [laatija-id id add kerroin]
  (service/update-energiatodistus!
    (ts/db-user laatija-id)
    {:id laatija-id :rooli 0}
    id
    (assoc-in add kerroin-path kerroin)))
(t/deftest store-and-reset-test
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        [id add] (first (energiatodistus-test-data/generate-and-insert! 1 2027 false laatija-id))
        find-kerroin #(-> (service/find-energiatodistus ts/*db* id) (get-in kerroin-path))]
    (t/testing "null and explicit 0.38 stay separate"
      (update-kerroin! laatija-id id add nil)
      (t/is (nil? (find-kerroin)))
      (update-kerroin! laatija-id id add 0.38M)
      (t/is (= 0.38M (find-kerroin)))
      (update-kerroin! laatija-id id add 0.25M)
      (t/is (= 0.25M (find-kerroin))))
    (t/testing "reset keeps the value in laatimisvaihe 2"
      (update-kerroin! laatija-id id (assoc-in add [:perustiedot :laatimisvaihe] 2) 0.25M)
      (energiatodistus-db/reset-kaukolampo-kerroin-when-not-allowed! ts/*db* {:id id})
      (t/is (= 0.25M (find-kerroin))))
    (t/testing "reset clears the value in other laatimisvaihe"
      (update-kerroin! laatija-id id (assoc-in add [:perustiedot :laatimisvaihe] 1) 0.25M)
      (energiatodistus-db/reset-kaukolampo-kerroin-when-not-allowed! ts/*db* {:id id})
      (t/is (nil? (find-kerroin))))))
