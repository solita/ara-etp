(ns solita.etp.energiatodistus-search-api-test
  (:require
    [clojure.test :as t]
    [clojure.java.jdbc :as jdbc]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
    [solita.etp.service.energiatodistus-search-test :as energiatodistus-search-test]))

(t/use-fixtures :each ts/fixture)

(t/deftest search-energiatodistus
  (laatija-test-data/insert! (laatija-test-data/generate-adds 1))
  (test-kayttajat/insert-virtu-paakayttaja!)
  (let [energiatodistus-adds (concat
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 1)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 2)))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 2)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 1)))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 3)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 4)))
                                    (energiatodistus-test-data/generate-adds 2 2018 true)))
        ids (energiatodistus-test-data/insert! energiatodistus-adds 1)]
    (energiatodistus-search-test/sign-energiatodistukset! (map vector (repeatedly (constantly 1)) ids))
    (t/testing "Haku ilman parametrejä palauttaa kaikki energiatodisukset"
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset")
                                              (test-kayttajat/with-virtu-user)
                                              (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 4))))
    (t/testing "Haku löytää energiatodistukset, joissa kumpi tahansa lämmitysmuoto on haettu"
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.lahtotiedot.lammitys.lammitysmuoto.id%22%2C1%5D%5D%5D")
                                  (test-kayttajat/with-virtu-user)
                                  (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 2))
        (t/is (every? (fn [et] (or (= (->> et :lahtotiedot :lammitys :lammitysmuoto-1 :id) 1)
                                   (= (->> et :lahtotiedot :lammitys :lammitysmuoto-2 :id) 1)))
                      response-body))))))

(t/deftest search-energiatodistus-by-havainnointikayntityyppi-api-test
  ;; Given: signed ET2026 certificates with known havainnointikayntityyppi-id
  (let [[laatija-id] (laatija-test-data/insert! (laatija-test-data/generate-adds 1))]
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [et-adds (->> (energiatodistus-test-data/generate-adds 2 2026 true)
                       (map #(assoc-in % [:perustiedot :havainnointikayntityyppi-id] 1)))
          ids (energiatodistus-test-data/insert! et-adds laatija-id)]
      (energiatodistus-search-test/sign-energiatodistukset! (map #(vector laatija-id %) ids))
      ;; When: GET /api/private/energiatodistukset with where clause for havainnointikayntityyppi-id = 1
      ;; URL-encoded: [[[\"=\",\"energiatodistus.perustiedot.havainnointikayntityyppi-id\",1]]]
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.perustiedot.havainnointikayntityyppi-id%22%2C1%5D%5D%5D")
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        ;; Then: HTTP 200 with correct results
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 2))))))

(t/deftest search-energiatodistus-unknown-field-api-test
  ;; Given: pääkäyttäjä
  (laatija-test-data/insert! (laatija-test-data/generate-adds 1))
  (test-kayttajat/insert-virtu-paakayttaja!)
  ;; When: searching with a field that doesn't exist in the schema
  ;; URL-encoded: [[["=","energiatodistus.olematon-kentta","test"]]]
  (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.olematon-kentta%22%2C%22test%22%5D%5D%5D")
                                 (test-kayttajat/with-virtu-user)
                                 (mock/header "Accept" "application/json")))]
    ;; Then: HTTP 400 error
    (t/is (= (:status response) 400))))

;; AE-2590: API-level test for searching by perusparannuspassi.valid
(t/deftest search-energiatodistus-by-ppp-valid-api-test
  ;; Given: ET2026 certificates, some with PPP and some without
  (let [[laatija-id] (laatija-test-data/insert!
                       (map #(assoc-in % [:patevyystaso] 4)
                            (laatija-test-data/generate-adds 1)))
        laatija-whoami {:id laatija-id :patevyystaso 4 :rooli 0}]
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [et-with-ppp-adds (energiatodistus-test-data/generate-adds 1 2026 true)
          et-without-ppp-adds (energiatodistus-test-data/generate-adds 1 2026 true)
          ids-with-ppp (energiatodistus-test-data/insert! et-with-ppp-adds laatija-id)
          ids-without-ppp (energiatodistus-test-data/insert! et-without-ppp-adds laatija-id)]
      (energiatodistus-search-test/sign-energiatodistukset!
        (map #(vector laatija-id %) (concat ids-with-ppp ids-without-ppp)))
      (ppp-test-data/generate-and-insert! ids-with-ppp laatija-whoami)
      ;; When: GET with where clause for perusparannuspassi.valid = true
      ;; URL-encoded: [[["=","perusparannuspassi.valid",true]]]
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22perusparannuspassi.valid%22%2Ctrue%5D%5D%5D")
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        ;; Then: HTTP 200 with only the certificate that has PPP
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 1))
        (t/is (= (:id (first response-body)) (first ids-with-ppp)))))))

;; AE-2590: API-level test for searching by yksinkertaistettu-paivitysmenettely
(t/deftest search-energiatodistus-by-yksinkertaistettu-api-test
  ;; Given: ET2026 certificates, one with yksinkertaistettu-paivitysmenettely = true
  (let [[laatija-id] (laatija-test-data/insert! (laatija-test-data/generate-adds 1))]
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [et-adds (energiatodistus-test-data/generate-adds 2 2026 true)
          ids (energiatodistus-test-data/insert! et-adds laatija-id)
          target-id (first ids)]
      (energiatodistus-search-test/sign-energiatodistukset! (map #(vector laatija-id %) ids))
      ;; Set one to true
      (jdbc/execute! ts/*db*
        ["UPDATE energiatodistus SET yksinkertaistettu_paivitysmenettely = true WHERE id = ?" target-id])
      ;; When: GET with where clause for yksinkertaistettu-paivitysmenettely = true
      ;; URL-encoded: [[["=","energiatodistus.yksinkertaistettu-paivitysmenettely",true]]]
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.yksinkertaistettu-paivitysmenettely%22%2Ctrue%5D%5D%5D")
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        ;; Then: HTTP 200 with only the certificate that has true
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 1))
        (t/is (= (:id (first response-body)) target-id))))))
