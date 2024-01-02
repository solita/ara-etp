(ns solita.etp.test-data.yritys
  (:require [solita.etp.test-system :as ts]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.schema.yritys :as yritys-schema]
            [solita.etp.service.yritys :as yritys-service]))

(def used-y-tunnukset (atom #{}))

(defn generate-adds [n]
  (let [generated-data
        ;; Generate more than needed so there
        ;; are still enough after removing duplicates
        ;; Each call starts the generation from 0000000-0 so this basically
        ;; also sets the maximum amount that can be generated
        (take (* n 10000)
              (map #(generators/complete {:ytunnus                %
                                          :verkkolaskuoperaattori (rand-int 32)
                                          :type-id                1}
                                         yritys-schema/YritysSave)
                   generators/unique-ytunnukset))
        return-value (take n
                           (remove #(contains? @used-y-tunnukset (:ytunnus %))
                                   generated-data))
        added-y-tunnukset (map :ytunnus return-value)]
    (swap! used-y-tunnukset into added-y-tunnukset)
    return-value))

(def generate-updates generate-adds)

(defn insert! [yritys-adds laatija-id]
  (mapv #(yritys-service/add-yritys! (ts/db-user laatija-id) {:id laatija-id} %)
        yritys-adds))

(defn generate-and-insert! [n laatija-id]
  (let [yritys-adds (generate-adds n)]
    (zipmap (insert! yritys-adds laatija-id) yritys-adds)))
