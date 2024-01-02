(ns solita.etp.service.valvonta-oikeellisuus-test
  (:require [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.whoami :as test-whoami]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.valvonta-oikeellisuus :as service]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test :as etp-test]
            [solita.common.map :as map])
  (:import (java.time LocalDate)))

(t/use-fixtures :each ts/fixture)

(t/deftest find-valvonta
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        [laatija-id _] (laatija-test-data/generate-and-insert!)
        [id _] (energiatodistus-test-data/generate-and-insert!
                 2018 true laatija-id)
        valvonta (service/find-valvonta (ts/db-user paakayttaja-id)
                                        (test-whoami/paakayttaja paakayttaja-id)
                                        id)]
    (t/is (some? valvonta))
    (t/is (= valvonta {:id id, :pending false, :valvoja-id nil, :ongoing false}))))

(t/deftest find-valvonta-laatija
  (let [[laatija-id _] (laatija-test-data/generate-and-insert!)
        [laatija2-id _] (laatija-test-data/generate-and-insert!)
        [id _] (energiatodistus-test-data/generate-and-insert!
                 2018 true laatija-id)
        valvonta (service/find-valvonta (ts/db-user laatija-id)
                                        (test-whoami/laatija laatija-id)
                                        id)]
    (t/is (some? valvonta))
    (t/is (= valvonta {:id id, :pending false, :valvoja-id nil, :ongoing false}))

    (t/is (= (etp-test/catch-ex-data
               #((service/find-valvonta (ts/db-user laatija2-id)
                                        (test-whoami/laatija laatija2-id)
                                        id)))
             {:type   :forbidden,
              :reason (str "User " laatija2-id
                           " is not allowed to access laatija: " laatija-id
                           " valvonta information.")}))))

(t/deftest update-toimenpide-laatija
  (let [[laatija-id _] (laatija-test-data/generate-and-insert!)
        [id _] (energiatodistus-test-data/generate-and-insert!
                 2018 true laatija-id)
        rfi-reply {:type-id       4
                   :deadline-date nil
                   :template-id   nil
                   :description   "Test"
                   :virheet       []
                   :severity-id   nil
                   :tiedoksi      []}
        {toimenpide-id :id}
        (service/add-toimenpide! (ts/db-user laatija-id)
                                 ts/*aws-s3-client*
                                 (test-whoami/laatija laatija-id)
                                 id rfi-reply)]

    (service/update-toimenpide! (ts/db-user laatija-id)
                                (test-whoami/laatija laatija-id)
                                id toimenpide-id (assoc rfi-reply :description "Test2"))

    (t/is (map/submap? (assoc rfi-reply :description "Test2")
                       (service/find-toimenpide (ts/db-user laatija-id)
                                      (test-whoami/laatija laatija-id)
                                      id toimenpide-id)))))

(t/deftest update-toimenpide-paakayttaja
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        [laatija-id _] (laatija-test-data/generate-and-insert!)
        [id _] (energiatodistus-test-data/generate-and-insert!
                 2018 true laatija-id)
        {virhetype-id :id}
        (service/add-virhetype! (ts/db-user paakayttaja-id)
                                {:label-fi "test"
                                :label-sv "test"
                                :ordinal 0 :valid true
                                :description-fi "test"
                                :description-sv "test"})
        audit-report {:type-id       7
                   :deadline-date nil
                   :template-id   nil
                   :description   "Test"
                   :virheet       [{:description "Test"
                                    :type-id     virhetype-id}]
                   :severity-id   nil
                   :tiedoksi      []}
        now (LocalDate/now)
        {toimenpide-id :id}
        (service/add-toimenpide! (ts/db-user paakayttaja-id)
                                 ts/*aws-s3-client*
                                 (test-whoami/paakayttaja paakayttaja-id)
                                 id audit-report)]

    (service/update-toimenpide! (ts/db-user paakayttaja-id)
                                (test-whoami/paakayttaja paakayttaja-id)
                                id toimenpide-id {:template-id 1})

    (t/is (map/submap? (assoc audit-report :template-id 1)
                       (service/find-toimenpide (ts/db-user paakayttaja-id)
                                                (test-whoami/paakayttaja paakayttaja-id)
                                                id toimenpide-id)))

    (service/publish-toimenpide! (ts/db-user paakayttaja-id)
                                 ts/*aws-s3-client*
                                 (test-whoami/paakayttaja paakayttaja-id)
                                 id toimenpide-id)

    (service/update-toimenpide! (ts/db-user paakayttaja-id)
                                (test-whoami/paakayttaja paakayttaja-id)
                                id toimenpide-id { :deadline-date now })

    (t/is (map/submap? (assoc audit-report :template-id 1 :deadline-date now)
                       (service/find-toimenpide (ts/db-user paakayttaja-id)
                                                (test-whoami/paakayttaja paakayttaja-id)
                                                id toimenpide-id)))

    ;; wait for emails to finish
    (Thread/sleep 100)))