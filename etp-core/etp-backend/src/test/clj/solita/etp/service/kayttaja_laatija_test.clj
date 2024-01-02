(ns solita.etp.service.kayttaja-laatija-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [schema-tools.core :as st]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.service.kayttaja-laatija :as service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.schema.kayttaja :as kayttaja-schema]
            [solita.etp.schema.laatija :as laatija-schema]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [paakayttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        laskuttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                             (map #(assoc % :rooli 3)))
        laskuttaja-ids (kayttaja-test-data/insert! laskuttaja-adds)
        laatijat (laatija-test-data/generate-and-insert! 100)]
    {:paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)
     :laskuttajat (zipmap laskuttaja-ids laskuttaja-adds)
     :laatijat laatijat}))

(t/deftest upsert-new-test
  (let [{:keys [laatijat]} (test-data-set)]
    (doseq [id (keys laatijat)
            :let [add (get laatijat id)
                  found-kayttaja (kayttaja-service/find-kayttaja ts/*db* id)
                  found-laatija (laatija-service/find-laatija-by-id ts/*db*
                                                                    id)]]
      (schema/validate kayttaja-schema/Kayttaja found-kayttaja)
      (schema/validate laatija-schema/Laatija found-laatija)
      (t/is (-> add
                (st/select-schema laatija-schema/KayttajaAdd)
                (dissoc :api-key)
                (xmap/submap? found-kayttaja)))
      (t/is (-> add
                (st/select-schema laatija-schema/LaatijaAdd)
                (xmap/submap? found-laatija))))))

(t/deftest upsert-existing-test
  (let [{:keys [laatijat]} (test-data-set)
        not-updated-id (-> laatijat keys sort last)
        not-updated-add (get laatijat not-updated-id)]
    (doseq [id (-> laatijat keys sort butlast)
            :let [{:keys [henkilotunnus] :as add} (get laatijat id)

                  ;; Adds are always used to upsert
                  update (-> (laatija-test-data/generate-adds 1)
                             first
                             (assoc :henkilotunnus henkilotunnus))
                  found-original (laatija-service/find-laatija-by-henkilotunnus
                                  ts/*db*
                                  henkilotunnus)
                  _ (service/upsert-kayttaja-laatijat! ts/*db* [update])
                  found-updated (laatija-service/find-laatija-by-henkilotunnus
                                 ts/*db*
                                 henkilotunnus)
                  found-not-updated (laatija-service/find-laatija-by-henkilotunnus
                                     ts/*db*
                                     (:henkilotunnus not-updated-add))]]
      (t/is (not= found-original found-updated))
      (schema/validate
       (dissoc laatija-schema/Laatija :voimassa :voimassaolo-paattymisaika)
       (dissoc found-updated :henkilotunnus))
      (t/is (xmap/submap? (st/select-schema update
                                            laatija-schema/LaatijaAdd)
                          found-updated))
      (t/is (xmap/submap? (st/select-schema not-updated-add
                                            laatija-schema/LaatijaAdd)
                          found-not-updated)))))

(t/deftest update-kayttaja-laatija!-test
  (let [{:keys [paakayttajat laatijat]} (test-data-set)
        paakayttaja-id (-> paakayttajat keys sort first)
        not-updated-id (-> laatijat keys sort last)
        not-updated-add (get laatijat not-updated-id)]
    (doseq [id (-> laatijat keys sort butlast)
            :let [whoami (rand-nth [{:rooli 2 :id paakayttaja-id}
                                    {:rooli 0 :id id}])
                  paakayttaja? (= (:rooli whoami) 2)
                  update (-> (laatija-test-data/generate-updates 1 paakayttaja?)
                             first)
                  found-original (laatija-service/find-laatija-by-id ts/*db* id)
                  _ (service/update-kayttaja-laatija! ts/*db*
                                                      whoami
                                                      id
                                                      update)
                  found-updated (laatija-service/find-laatija-by-id ts/*db* id)
                  found-not-updated (laatija-service/find-laatija-by-id
                                     ts/*db*
                                     not-updated-id)]]
      (t/is (not= found-original found-updated))
      (schema/validate laatija-schema/Laatija found-updated)
      (t/is (xmap/submap? (-> update
                              (st/select-schema laatija-schema/LaatijaUpdate)
                              (dissoc :api-key))
                          found-updated))
      (t/is (xmap/submap? (st/select-schema not-updated-add
                                            laatija-schema/LaatijaAdd)
                          found-not-updated)))))

(t/deftest update-kayttaja-laatija!-no-permissions-test
  (let [{:keys [laskuttajat laatijat]} (test-data-set)
        laskuttaja-id (-> laskuttajat keys sort first)]
    (doseq [id (-> laatijat keys sort)
            :let [whoami (rand-nth [{:rooli 3 :id laskuttaja-id}
                                    {:rooli 0 :id (inc id)}])
                  update (-> (laatija-test-data/generate-updates 1 false)
                             first)]]
      (t/is (= :forbidden
               (:type
                (etp-test/catch-ex-data
                 #(service/update-kayttaja-laatija! ts/*db* whoami id update)))))
      (t/is (-> laatijat
                (get id)
                (st/select-schema laatija-schema/LaatijaAdd)
                (xmap/submap? (laatija-service/find-laatija-by-id
                               ts/*db*
                               id)))))))
