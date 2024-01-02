(ns solita.etp.service.kayttaja-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.common.map :as map]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.service.kayttaja :as service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.schema.kayttaja :as kayttaja-schema]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.schema.common :as common-schema]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  {:kayttajat (kayttaja-test-data/generate-and-insert! 200)})

(t/deftest add-and-find-test
  (let [{:keys [kayttajat]} (test-data-set)]
    (doseq [[id kayttaja] kayttajat
            :let [whoami (rand-nth [kayttaja-test-data/paakayttaja
                                    kayttaja-test-data/laskuttaja
                                    {:id id}])
                  found (service/find-kayttaja ts/*db* whoami id)]]
      (schema/validate kayttaja-schema/Kayttaja found)
      (t/is (map/submap? (dissoc kayttaja :api-key) found)))))

(t/deftest add-and-find-no-permissions-test
  (let [{:keys [kayttajat]} (test-data-set)]
    (doseq [[id _] kayttajat]
      (t/is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"Forbidden"
             (service/find-kayttaja ts/*db*
                                    (rand-nth [kayttaja-test-data/laatija
                                               kayttaja-test-data/patevyyden-toteaja])
                                    id))))))

(t/deftest update-and-find-test
  (let [{:keys [kayttajat]} (test-data-set)
        updates (kayttaja-test-data/generate-updates (count kayttajat))]
    (doseq [[[id kayttaja] update] (->> (interleave kayttajat updates)
                                        (partition 2))]
      (service/update-kayttaja! ts/*db*
                                kayttaja-test-data/paakayttaja
                                id
                                update)
      (let [found (service/find-kayttaja
                   ts/*db*
                   (rand-nth [kayttaja-test-data/paakayttaja
                              kayttaja-test-data/laskuttaja])
                   id)]
        (schema/validate kayttaja-schema/Kayttaja found)
        (t/is (map/submap? (dissoc update :api-key) found))))))

(t/deftest update-and-find-no-permissions-test
  (let [{:keys [kayttajat]} (test-data-set)
        updates (kayttaja-test-data/generate-updates (count kayttajat))]
    (doseq [[[id kayttaja] update] (->> (interleave kayttajat updates)
                                        (partition 2))]
      (t/is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"Forbidden"
             (service/update-kayttaja!
              ts/*db*
              (rand-nth [kayttaja-test-data/laatija
                         kayttaja-test-data/patevyyden-toteaja
                         kayttaja-test-data/laskuttaja])
              id
              update))))))
