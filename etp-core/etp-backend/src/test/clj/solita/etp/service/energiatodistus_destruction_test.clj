(ns solita.etp.service.energiatodistus-destruction-test
  (:require [clojure.test :as t]
            [solita.common.time :as time]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-destruction :as service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts])
  (:import (java.time Instant LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(defn update-energiatodistus! [energiatodistus-id energiatodistus laatija-id]
  (energiatodistus-service/update-energiatodistus! (ts/db-user laatija-id)
                                                   {:id laatija-id :rooli 0}
                                                   energiatodistus-id
                                                   energiatodistus))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2 laatija-id-3] laatija-ids
        energiatodistus-adds (energiatodistus-test-data/generate-adds 3
                                                                      2018
                                                                      true)
        energiatodistus-ids (mapcat #(energiatodistus-test-data/insert!
                                       [%1]
                                       %2)
                                    energiatodistus-adds
                                    laatija-ids)
        [energiatodistus-id-1 energiatodistus-id-2 energiatodistus-id-3] energiatodistus-ids
        [energiatodistus-add-1 energiatodistus-add-2 energiatodistus-add-3] energiatodistus-adds]

    ;; Sign energiatodistus 1
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    (update-energiatodistus! energiatodistus-id-2
                             (assoc energiatodistus-add-2
                               ;; Korvaa energiatodistus 1 with energiatodistus 2
                               :korvattu-energiatodistus-id
                               energiatodistus-id-1
                               ;; Expire energiatodistus 2
                               :voimassaolo-paattymisaika
                               (LocalDate/ofInstant (Instant/ofEpochSecond 0) (ZoneId/of "Europe/Helsinki")))
                             laatija-id-2)

    (update-energiatodistus! energiatodistus-id-3
                             (assoc energiatodistus-add-3
                               ;; Set expiration of energiatodistus 3 to today
                               :voimassaolo-paattymisaika
                               (time/now))
                             laatija-id-3)

    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest linked-data-exist?-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2 id-3] ids]
    (t/testing "Todistus that is korvattu, but does not korvaa any other todistus, should not have linked data."
      (t/is (false? (#'service/linked-data-exist? ts/*db* id-1))))
    (t/testing "Todistus that korvaa other todistus, should have linked data."
      (t/is (true? (#'service/linked-data-exist? ts/*db* id-2))))
    (t/testing "Todistus that does not korvaa or be korvattu should not have linked data."
      (t/is (true? (#'service/linked-data-exist? ts/*db* id-3))))))

(t/deftest get-currently-expired-todistus-ids-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2 id-3] ids
        expired-ids (#'service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "Todistus with expiration time at year 1970 should be expired."
      (t/is (some #{id-2} expired-ids)))
    (t/testing "Todistus with expiration date set by signing it today should not be expired."
      (t/is (nil? (some #{id-1} expired-ids))))
    (t/testing "Todistus whose expiration is today should not be expired yet."
      (t/is (nil? (some #{id-3} expired-ids))))))
