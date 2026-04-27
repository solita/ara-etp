(ns solita.etp.energiatodistus-search-api-test
  (:require
    [clojure.test :as t]
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

;; ============================================================
;; AE-2590: PPP search API tests
;; ============================================================

(t/deftest search-energiatodistus-by-ppp-valid-api-test
  ;; Given: signed ET2026 with valid PPP
  (let [[laatija-id] (->> (laatija-test-data/generate-adds 1)
                          (map #(assoc-in % [:patevyystaso] 4))
                          (laatija-test-data/insert!))]
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [et-id (first (energiatodistus-test-data/insert!
                         (energiatodistus-test-data/generate-adds 1 2026 true) laatija-id))
          whoami {:id laatija-id :patevyystaso 4 :rooli 0}
          _ (ppp-test-data/generate-and-insert! [et-id] whoami)]
      (energiatodistus-search-test/sign-energiatodistukset! [[laatija-id et-id]])
      ;; When: GET /api/private/energiatodistukset with where=[[[ "=","perusparannuspassi.valid",true]]]
      ;; URL-encoded: %5B%5B%5B%22%3D%22%2C%22perusparannuspassi.valid%22%2Ctrue%5D%5D%5D
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22perusparannuspassi.valid%22%2Ctrue%5D%5D%5D")
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        ;; Then: HTTP 200 with the ET found
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 1))
        (t/is (= (:id (first response-body)) et-id))))))

(t/deftest search-energiatodistus-by-ppp-id-api-test
  ;; Given: signed ET2026 with PPP
  (let [[laatija-id] (->> (laatija-test-data/generate-adds 1)
                          (map #(assoc-in % [:patevyystaso] 4))
                          (laatija-test-data/insert!))]
    (test-kayttajat/insert-virtu-paakayttaja!)
    (let [et-id (first (energiatodistus-test-data/insert!
                         (energiatodistus-test-data/generate-adds 1 2026 true) laatija-id))
          whoami {:id laatija-id :patevyystaso 4 :rooli 0}
          ppp-id (first (ppp-test-data/generate-and-insert! [et-id] whoami))]
      (energiatodistus-search-test/sign-energiatodistukset! [[laatija-id et-id]])
      ;; When: GET /api/private/energiatodistukset with where=[[["=","perusparannuspassi.id",<ppp-id>]]]
      (let [where-param (str "%5B%5B%5B%22%3D%22%2C%22perusparannuspassi.id%22%2C" ppp-id "%5D%5D%5D")
            response (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset?where=" where-param))
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        ;; Then: HTTP 200 with the correct ET found
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 1))
        (t/is (= (:id (first response-body)) et-id))))))

(t/deftest search-energiatodistus-by-ppp-public-api-returns-400-test
  ;; Given: signed ET2026 with PPP (public API should not allow PPP fields)
  (let [[laatija-id] (->> (laatija-test-data/generate-adds 1)
                          (map #(assoc-in % [:patevyystaso] 4))
                          (laatija-test-data/insert!))]
    (let [et-id (first (energiatodistus-test-data/insert!
                         (energiatodistus-test-data/generate-adds 1 2026 true) laatija-id))
          whoami {:id laatija-id :patevyystaso 4 :rooli 0}
          _ (ppp-test-data/generate-and-insert! [et-id] whoami)]
      (energiatodistus-search-test/sign-energiatodistukset! [[laatija-id et-id]])
      ;; When: GET /api/public/energiatodistukset with PPP field
      ;; URL-encoded: [[["=","perusparannuspassi.valid",true]]]
      (let [response (ts/handler (-> (mock/request :get "/api/public/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22perusparannuspassi.valid%22%2Ctrue%5D%5D%5D")
                                     (mock/header "Accept" "application/json")))]
        ;; Then: HTTP 400 (PPP fields not in public schema)
        (t/is (= (:status response) 400))))))
