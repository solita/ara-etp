(ns solita.etp.api.energiatodistus-luokittelut-test
  (:require [clojure.test :as t]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(defn- get-e-luokka [versio alakayttotarkoitus nettoala e-luku]
  (ts/handler (-> (mock/request :get (str "/api/public/energiatodistukset/e-luokka/"
                                          versio "/" alakayttotarkoitus "/"
                                          nettoala "/" e-luku))
                  (mock/header "Accept" "application/json"))))

(defn- response-body [response]
  (-> response :body (j/read-value object-mapper)))

;; Endpoint uses false, false for tayttaa-aplus/a0-vaatimukset,
;; so A+ and A0 are always downgraded to A.
(t/deftest e-luokka-2026-endpoint-test
  ;; 2026, YAT, nettoala 100, e-luku 1 → A+ before downgrade → A (false, false)
  (let [response (get-e-luokka 2026 "YAT" 100 1)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body)))
    ;; raja-asteikko is still 8-element and contains A+, A0
    (t/is (= [[78 "A+"] [98 "A0"] [98 "A"] [106 "B"]
              [130 "C"] [181 "D"] [265 "E"] [310 "F"]]
             (:raja-asteikko body))))

  ;; 2026, YAT, nettoala 100, e-luku 79 → A0 before downgrade → A (false, false)
  (let [response (get-e-luokka 2026 "YAT" 100 79)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2026, RT, nettoala 100, e-luku 57 → A+ before downgrade → A (false, false)
  (let [response (get-e-luokka 2026 "RT" 100 57)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2026, S, nettoala 100, e-luku 230 → A0 before downgrade → A (false, false)
  (let [response (get-e-luokka 2026 "S" 100 230)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2026, AK3, nettoala 100, e-luku 163 → G
  (let [response (get-e-luokka 2026 "AK3" 100 163)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "G" (:e-luokka body))))

  ;; 2026 response raja-asteikko has 8 elements
  (let [response (get-e-luokka 2026 "YAT" 100 1)
        body (response-body response)]
    (t/is (= 8 (count (:raja-asteikko body)))))

  ;; 2026 response does NOT contain :raja-uusi-2018
  (let [response (get-e-luokka 2026 "YAT" 100 1)
        body (response-body response)]
    (t/is (nil? (:raja-uusi-2018 body))))

  ;; Regression: 2018 endpoint still returns 6-element asteikko
  (let [response (get-e-luokka 2018 "KAT" 100 1)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= 6 (count (:raja-asteikko body))))))

;; --- Laatimisvaiheet version-parameterized endpoint tests ---

(defn- get-laatimisvaiheet [versio]
  (ts/handler (-> (mock/request :get (str "/api/public/energiatodistukset/laatimisvaiheet/" versio))
                  (mock/header "Accept" "application/json"))))

(defn- get-laatimisvaiheet-unparameterized []
  (ts/handler (-> (mock/request :get "/api/public/energiatodistukset/laatimisvaiheet")
                  (mock/header "Accept" "application/json"))))

(t/deftest laatimisvaiheet-unparameterized-returns-404
  ;; given the unparameterized /laatimisvaiheet endpoint has been removed,
  ;; when requesting GET /api/public/energiatodistukset/laatimisvaiheet,
  ;; then a 404 status is returned
  (let [response (get-laatimisvaiheet-unparameterized)]
    (t/is (= 404 (:status response)))))

(t/deftest laatimisvaiheet-2026-returns-2026-entries
  ;; given the laatimisvaiheet endpoint with version 2026 (exact match),
  ;; when requesting laatimisvaiheet for version 2026,
  ;; then all 5 entries for versio=2026 with IDs 0-4 are returned
  (let [response (get-laatimisvaiheet 2026)
        body (response-body response)
        ids (set (map :id body))]
    (t/is (= 200 (:status response)))
    (t/is (= #{0 1 2 3 4} ids))
    (t/is (= 5 (count body)))))

(t/deftest laatimisvaiheet-2018-returns-2018-entries
  ;; given the laatimisvaiheet endpoint with version 2018 (exact match),
  ;; when requesting laatimisvaiheet for version 2018,
  ;; then exactly 3 entries for versio=2018 with IDs 0-2 are returned
  (let [response (get-laatimisvaiheet 2018)
        body (response-body response)
        ids (set (map :id body))]
    (t/is (= 200 (:status response)))
    (t/is (= #{0 1 2} ids))
    (t/is (= 3 (count body)))
    (t/is (not (contains? ids 3)))
    (t/is (not (contains? ids 4)))))

(t/deftest laatimisvaiheet-2018-endpoint-returns-short-labels
  ;; given a GET request to /laatimisvaiheet/2018,
  ;; when the response is received,
  ;; then id=0 has short-form label "Rakennuslupa" from the DB
  (let [response (get-laatimisvaiheet 2018)
        body (response-body response)
        by-id (into {} (map (juxt :id identity) body))]
    (t/is (= 200 (:status response)))
    (t/is (= "Rakennuslupa" (:label-fi (get by-id 0))))
    (t/is (= "Käyttöönotto" (:label-fi (get by-id 1))))
    (t/is (= "Olemassa oleva rakennus" (:label-fi (get by-id 2))))))

(t/deftest laatimisvaiheet-2026-endpoint-returns-long-labels
  ;; given a GET request to /laatimisvaiheet/2026,
  ;; when the response is received,
  ;; then id=0 has long-form label "Rakennuslupavaihe, uudisrakennus" from the DB
  (let [response (get-laatimisvaiheet 2026)
        body (response-body response)
        by-id (into {} (map (juxt :id identity) body))]
    (t/is (= 200 (:status response)))
    (t/is (= "Rakennuslupavaihe, uudisrakennus" (:label-fi (get by-id 0))))
    (t/is (= "Käyttöönottovaihe, uudisrakennus" (:label-fi (get by-id 1))))
    (t/is (= "Olemassa oleva rakennus" (:label-fi (get by-id 2))))
    (t/is (= "Rakennuslupavaihe, laajamittainen perusparannus" (:label-fi (get by-id 3))))
    (t/is (= "Käyttöönottovaihe, laajamittainen perusparannus" (:label-fi (get by-id 4))))))

(t/deftest laatimisvaiheet-entry-has-expected-fields
  ;; given the laatimisvaiheet endpoint,
  ;; when requesting entries for any version,
  ;; then each entry has id, label_fi, label_sv, valid fields
  (let [response (get-laatimisvaiheet 2026)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (doseq [entry body]
      (t/is (contains? entry :id))
      (t/is (contains? entry :label-fi))
      (t/is (contains? entry :label-sv))
      (t/is (contains? entry :valid)))))
