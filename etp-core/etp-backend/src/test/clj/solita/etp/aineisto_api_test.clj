(ns solita.etp.aineisto-api-test
  (:require
    [clojure.test :as t]
    [ring.mock.request :as mock]
    [solita.etp.service.aineisto :as aineisto-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Duration Instant)
           (java.util Base64)))

(def allowed-network "192.168.1.1/32")
(def user-ip "192.168.1.1")

(t/use-fixtures :each ts/fixture)

(t/deftest fetch-aineisto
  (let [kayttaja-id (test-kayttajat/insert! (->> (test-kayttajat/generate-adds 1)
                                                 (map #(merge % test-kayttajat/aineistoasiakas))
                                                 (map #(assoc %
                                                                :email "yhteyshenkilo@example.com"
                                                                :api-key "password"
                                                                ))))
        api-key (->> "yhteyshenkilo@example.com:password"
                     .getBytes
                     (.encode (Base64/getEncoder))
                     (String.))
        auth-header (str "Basic " api-key)
        aineisto-url "/api/external/aineistot/1/energiatodistukset.csv"]
    (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 1
                                                                     :valid-until (.plus (Instant/now) (Duration/ofSeconds 86400))
                                                                     :ip-address  allowed-network}})
    (t/testing "User with access receives redirect response"
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "Authorization" auth-header)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (some? (-> response :headers (get "Location"))))
        (t/is (= (:status response) 302))))
    (t/testing "Anonymous user can't fetch aineisto"
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 401))))
    (t/testing "User with expired access can't fetch aineisto"
      (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 1
                                                                       :valid-until (.minus (Instant/now) (Duration/ofSeconds 86400))
                                                                       :ip-address  allowed-network}})
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "Authorization" auth-header)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 403))))
    (t/testing "User with wrong IP can't fetch aineisto"
      (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 1
                                                                       :valid-until (.plus (Instant/now) (Duration/ofSeconds 86400))
                                                                       :ip-address  "192.168.1.2"}})
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "Authorization" auth-header)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 403))))
    (t/testing "User can't access aineisto with wrong id"
      (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 2
                                                                       :valid-until (.plus (Instant/now) (Duration/ofSeconds 86400))
                                                                       :ip-address  allowed-network}})
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "Authorization" auth-header)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 403))))
    (t/testing "Access can be just an IP"
      (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 1
                                                                       :valid-until (.plus (Instant/now) (Duration/ofSeconds 86400))
                                                                       :ip-address  "192.168.1.1"}})
      (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                     (mock/header "Authorization" auth-header)
                                     (mock/header "X-Forwarded-For" user-ip)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 302))))
    (t/testing "IP subnetting works"
      (aineisto-service/set-kayttaja-aineistot! ts/*db* kayttaja-id #{{:aineisto-id 1
                                                                       :valid-until (.plus (Instant/now) (Duration/ofSeconds 86400))
                                                                       :ip-address  "192.168.1.0/24"}})
      (t/testing "with allowed ip"
        (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                       (mock/header "Authorization" auth-header)
                                       (mock/header "X-Forwarded-For" "192.168.1.100")
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 302))))
      (t/testing "with not allowed ip"
        (let [response (ts/handler (-> (mock/request :get aineisto-url)
                                       (mock/header "Authorization" auth-header)
                                       (mock/header "X-Forwarded-For" "192.168.2.100")
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 403)))))))

(t/deftest generate-aineistot
  (t/testing "Aineistot generation endpoint responds with status code 200"
    (let [response (ts/handler (-> (mock/request :post "/api/internal/aineistot/update")))]
      (t/is (= (:status response) 200)))))
