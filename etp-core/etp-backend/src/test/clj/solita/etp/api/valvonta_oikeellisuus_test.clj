(ns solita.etp.api.valvonta-oikeellisuus-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.service.valvonta-oikeellisuus :as valvonta-service]
    [solita.etp.whoami :as test-whoami]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn- setup-valvonta-cases!
  "Creates laatija, energiatodistukset and starts valvonta cases for testing.
   Returns a map with :paakayttaja-id, :laatija-id, and :energiatodistus-ids"
  []
  (let [paakayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
        [laatija-id _] (laatija-test-data/generate-and-insert!)
        ;; Create energiatodistukset with all supported versions
        [et-2013-id _] (energiatodistus-test-data/generate-and-insert! 2013 true laatija-id)
        [et-2018-id _] (energiatodistus-test-data/generate-and-insert! 2018 true laatija-id)
        [et-2026-id _] (energiatodistus-test-data/generate-and-insert! 2026 true laatija-id)
        whoami (test-whoami/paakayttaja paakayttaja-id)]
    ;; Start valvonta cases by setting pending to true
    (doseq [et-id [et-2013-id et-2018-id et-2026-id]]
      (valvonta-service/save-valvonta! (ts/db-user paakayttaja-id) whoami et-id
                                       {:pending true :valvoja-id paakayttaja-id}))
    ;; Add a toimenpide to one of the cases to have more realistic data
    (valvonta-service/add-toimenpide! (ts/db-user paakayttaja-id)
                                      ts/*aws-s3-client*
                                      whoami
                                      et-2018-id
                                      {:type-id       2 ; tietopyyntö
                                       :deadline-date nil
                                       :template-id   nil
                                       :description   "Test toimenpide"
                                       :virheet       []
                                       :severity-id   nil
                                       :tiedoksi      []})
    {:paakayttaja-id      paakayttaja-id
     :laatija-id          laatija-id
     :energiatodistus-ids [et-2013-id et-2018-id et-2026-id]}))

(t/deftest valvonta-oikeellisuus-list-endpoint
  (t/testing "Pääkäyttäjä can call the valvonta oikeellisuus list endpoint without getting a 500 error"
    (setup-valvonta-cases!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus")
                                   (mock/query-string {:offset 0 :limit 10})
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))
          body (j/read-value (:body response))]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (vector? body)
            "Response should be a JSON array")
      (t/is (>= (count body) 3)
            "Response should contain at least 3 valvonta cases"))))

(t/deftest valvonta-oikeellisuus-count-endpoint
  (t/testing "Pääkäyttäjä can call the valvonta oikeellisuus count endpoint"
    (setup-valvonta-cases!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus/count")
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))
          body (j/read-value (:body response) j/keyword-keys-object-mapper)]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (contains? body :count)
            "Response should contain a count field")
      (t/is (>= (:count body) 3)
            "Count should be at least 3"))))

(t/deftest valvonta-oikeellisuus-toimenpidetyypit-endpoint
  (t/testing "Pääkäyttäjä can call the toimenpidetyypit endpoint"
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus/toimenpidetyypit")
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (vector? (j/read-value (:body response)))
            "Response should be a JSON array"))))

(t/deftest valvonta-oikeellisuus-virhetypes-endpoint
  (t/testing "Pääkäyttäjä can call the virhetypes endpoint"
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus/virhetypes")
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (vector? (j/read-value (:body response)))
            "Response should be a JSON array"))))

(t/deftest valvonta-oikeellisuus-severities-endpoint
  (t/testing "Pääkäyttäjä can call the severities endpoint"
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus/severities")
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (vector? (j/read-value (:body response)))
            "Response should be a JSON array"))))

(t/deftest valvonta-oikeellisuus-templates-endpoint
  (t/testing "Pääkäyttäjä can call the templates endpoint"
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/oikeellisuus/templates")
                                   (mock/header "Accept" "application/json")
                                   (test-kayttajat/with-virtu-user)))]
      (t/is (= 200 (:status response))
            (str "Expected status 200, got " (:status response)
                 " with body: " (:body response)))
      (t/is (vector? (j/read-value (:body response)))
            "Response should be a JSON array"))))
