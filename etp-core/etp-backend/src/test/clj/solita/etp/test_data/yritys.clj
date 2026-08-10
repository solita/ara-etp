(ns solita.etp.test-data.yritys
  (:require [solita.etp.test-system :as ts]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.schema.yritys :as yritys-schema]
            [solita.etp.service.yritys :as yritys-service]))

(defn- used-y-tunnukset []
  ;; The test database is created fresh per test (see ts/fixture), so
  ;; querying it directly gives us the precise set of ytunnus already in
  ;; use for *this* test, instead of relying on separate bookkeeping that
  ;; can drift out of sync (e.g. a JVM-global atom shared across every
  ;; test run).
  (->> (yritys-service/find-all-yritykset ts/*db*)
       (map :ytunnus)
       set))

(defn generate-adds [n]
  (let [used (used-y-tunnukset)]
    (->> generators/unique-ytunnukset
         (remove used)
         (take n)
         (map #(generators/complete {:ytunnus                %
                                     :verkkolaskuoperaattori (rand-int 32)
                                     :type-id                1}
                                    yritys-schema/YritysSave)))))

(def generate-updates generate-adds)

(defn insert! [yritys-adds laatija-id]
  (mapv #(yritys-service/add-yritys! (ts/db-user laatija-id) {:id laatija-id} %)
        yritys-adds))

(defn generate-and-insert! [n laatija-id]
  (let [yritys-adds (generate-adds n)]
    (zipmap (insert! yritys-adds laatija-id) yritys-adds)))
