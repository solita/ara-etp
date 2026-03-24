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
