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

;; Endpoint uses false for tayttaa-aplus-vaatimukset and "Ei" for the A0
;; marking, so A+ is always downgraded to A. A0 is not an energy class.
(t/deftest e-luokka-2027-endpoint-test
  ;; 2027, YAT, nettoala 100, e-luku 1 → A+ before downgrade → A (false, false)
  (let [response (get-e-luokka 2027 "YAT" 100 1)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body)))
    ;; raja-asteikko is 7-element and contains A+ but not A0
    (t/is (= [[78 "A+"] [98 "A"] [106 "B"]
              [130 "C"] [181 "D"] [265 "E"] [310 "F"]]
             (:raja-asteikko body))))

  ;; 2027, YAT, nettoala 100, e-luku 79 → A (the former A0 range)
  (let [response (get-e-luokka 2027 "YAT" 100 79)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2027, RT, nettoala 100, e-luku 57 → A+ before downgrade → A (false, false)
  (let [response (get-e-luokka 2027 "RT" 100 57)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2027, S, nettoala 100, e-luku 230 → A (the former A0 range)
  (let [response (get-e-luokka 2027 "S" 100 230)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "A" (:e-luokka body))))

  ;; 2027, AK3, nettoala 100, e-luku 163 → G
  (let [response (get-e-luokka 2027 "AK3" 100 163)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= "G" (:e-luokka body))))

  ;; 2027 response raja-asteikko has 7 elements
  (let [response (get-e-luokka 2027 "YAT" 100 1)
        body (response-body response)]
    (t/is (= 7 (count (:raja-asteikko body)))))

  ;; 2027 response does NOT contain :raja-uusi-2018
  (let [response (get-e-luokka 2027 "YAT" 100 1)
        body (response-body response)]
    (t/is (nil? (:raja-uusi-2018 body))))

  ;; Regression: 2018 endpoint still returns 6-element asteikko
  (let [response (get-e-luokka 2018 "KAT" 100 1)
        body (response-body response)]
    (t/is (= 200 (:status response)))
    (t/is (= 6 (count (:raja-asteikko body))))))
