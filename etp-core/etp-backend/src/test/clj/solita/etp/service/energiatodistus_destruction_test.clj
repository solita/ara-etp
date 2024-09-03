(ns solita.etp.service.energiatodistus-destruction-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-destruction :as service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn update-energiatodistus! [energiatodistus-id energiatodistus laatija-id]
  (energiatodistus-service/update-energiatodistus! (ts/db-user laatija-id)
                                                   {:id laatija-id :rooli 0}
                                                   energiatodistus-id
                                                   energiatodistus))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2] laatija-ids
        energiatodistus-adds (energiatodistus-test-data/generate-adds 3
                                                                      2018
                                                                      true)
        energiatodistus-ids (mapcat #(energiatodistus-test-data/insert!
                                       [%1]
                                       %2)
                                    energiatodistus-adds
                                    laatija-ids)
        [energiatodistus-id-1 energiatodistus-id-2] energiatodistus-ids
        [_ energiatodistus-add-2] energiatodistus-adds]

    ;; Sign energiatodistus 1
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    ;; Korvaa energiatodistus 1 with energiatodistus 2
    (update-energiatodistus! energiatodistus-id-2
                             (assoc energiatodistus-add-2
                               :korvattu-energiatodistus-id
                               energiatodistus-id-1)
                             laatija-id-2)

    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest linked-data-exist?-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2] ids]
    (t/is (false? (service/linked-data-exist? ts/*db* id-1)))
    (t/is (true? (service/linked-data-exist? ts/*db* id-2)))
    (t/is (true? (service/linked-data-exist? ts/*db* id-2)))))
