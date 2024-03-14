(ns solita.common.cf-signed-url-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [solita.etp.config :as config]
            [solita.etp.service.json :as json]
            [solita.common.cf-signed-url :as signed-url]))

(t/deftest signed-url-roundtrip-test
  (let [private-key (-> "cf-signed-url/example.key.pem" io/resource slurp signed-url/pem-string->private-key)
        public-key (-> "cf-signed-url/example.pub.pem" io/resource slurp signed-url/pem-string->public-key)
        key-pair-id "L3G17K3Y"
        base-url "https://energiatodistusrekisteri.fi"
        intended-ip-address "10.0.7.10"
        other-ip-address "10.0.17.1"
        time-issued (signed-url/unix-time)
        expires (+ time-issued 60)
        time-late-test (+ expires 10)
        url (signed-url/url->signed-url base-url
                                        expires
                                        intended-ip-address
                                        {:key-pair-id key-pair-id
                                         :private-key private-key})
        verify-keys {:key-pair-id key-pair-id
                     :public-key  public-key}]

    ;; Positive verification
    (t/is (nil? (signed-url/signed-url-problem-now url
                                                   intended-ip-address
                                                   verify-keys)))

    ;; Verify after expiration
    (t/is (= :expired-url
             (signed-url/signed-url-problem url
                                            intended-ip-address
                                            time-late-test
                                            verify-keys)))

    ;; Check other ip adderss
    (t/is (= :invalid-ip-address
             (signed-url/signed-url-problem-now url
                                                other-ip-address
                                                verify-keys)))

    ;; Tamper by adding extra parameters. Extra parameters are not
    ;; expected after the signature part.
    (t/is (= :format
             (signed-url/signed-url-problem-now (str url "&asd=basd")
                                                intended-ip-address
                                                verify-keys)))))

(t/deftest signed-url-with-query-test
  (let [private-key (-> "cf-signed-url/example.key.pem" io/resource slurp signed-url/pem-string->private-key)
        public-key (-> "cf-signed-url/example.pub.pem" io/resource slurp signed-url/pem-string->public-key)
        key-pair-id "L3G17K3Y"
        base-url "https://energiatodistusrekisteri.fi/api/signed/example/1/data.csv?kayttaja=1"
        ip-address "10.0.7.11"
        time-issued (signed-url/unix-time)
        expires (+ time-issued 60)
        time-late-test (+ expires 10)
        optimistic-expiration (+ time-late-test 10)
        url (signed-url/url->signed-url base-url
                                        expires
                                        ip-address
                                        {:key-pair-id key-pair-id
                                         :private-key private-key})
        verify-keys {:key-pair-id key-pair-id
                     :public-key  public-key}]

    ;; Positive verification
    (t/is (nil? (signed-url/signed-url-problem-now url
                                                   ip-address
                                                   verify-keys)))

    ;; Tamper by changing the kayttaja parameter
    (t/is (= :invalid-url
             (signed-url/signed-url-problem-now (.replace url "?kayttaja=1&" "?kayttaja=2&")
                                                ip-address
                                                verify-keys)))))

(t/deftest signed-url-creation
  (t/testing "Signed url contains the expected information"
    (let [private-key (-> config/url-signing-private-key signed-url/pem-string->private-key)
          public-key (-> config/url-signing-public-key signed-url/pem-string->public-key)
          key-pair-id "L3G17K3Y"
          base-url "https://energiatodistusrekisteri.fi/api/signed/example/1/data.csv?kayttaja=1"
          ip-address "10.0.7.11"
          time-issued (signed-url/unix-time)
          expires (+ time-issued 60)
          signed-url (signed-url/url->signed-url base-url
                                                 expires
                                                 ip-address
                                                 {:key-pair-id key-pair-id
                                                  :private-key private-key})
          {:keys [_ policy]} (signed-url/signed-url->components signed-url)
          policy-bytes (signed-url/querystring-safe-base64->bytes policy)
          policy-doc (json/read-value policy-bytes)
          policy-url (-> policy-doc (get-in [:Statement 0 :Resource]))
          policy-expires (-> policy-doc (get-in [:Statement 0 :Condition :DateLessThan :AWS:EpochTime]))
          policy-ip-address (-> policy-doc (get-in [:Statement 0 :Condition :IpAddress :AWS:SourceIp]))
          verify-keys {:key-pair-id key-pair-id
                       :public-key  public-key}]

      (t/testing "The signed url is coherent"
        (t/is (nil? (signed-url/signed-url-problem-now signed-url
                                                       ip-address
                                                       verify-keys))))

      (t/testing "Url is the same in the signed url"
        (t/is (= base-url (-> signed-url signed-url/signed-url->components :base-url)))
        (t/is (= base-url policy-url)))

      (t/testing "key-pair-id is the same in the signed url"
        (t/is (= key-pair-id (-> signed-url signed-url/signed-url->components :key-pair-id))))

      (t/testing "ip-address is the same in the signed url"
        (t/is (= ip-address policy-ip-address)))

      (t/testing "expiration is the same in the signed url"
        (t/is (= expires policy-expires))))))
