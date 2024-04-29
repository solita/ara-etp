(ns solita.etp.api.energiatodisuts-signing-test
  (:require
    [clojure.test :as t]
    [solita.etp.test-data.generators :as generators]
    [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
    [solita.etp.test-data.laatija :as test-data.laatija]
    [ring.mock.request :as mock]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-system :as ts])
  (:import (java.time Duration Instant)
           (java.util Base64)))

(def allowed-network "192.168.1.1/32")
(def user-ip "192.168.1.1")
(defn energiatodistus-sign-url
  [et-id version language]
  (str "/api/private/energiatodistukset/" version "/" et-id "/signature/system-sign/" language)
  )

(t/use-fixtures :each ts/fixture)

(t/deftest asd
  (let [; Add laatija
        laatija-id (test-data.laatija/insert-virtu-laatija!)

        ; Generate two different rakennustunnus
        rakennustunnus-1 (generators/generate-rakennustunnus)
        rakennustunnus-2 (generators/generate-rakennustunnus)

        ; Create six energiatodistus. Eech with rakennustunnus 1, all language options (fi, sv, multilingual) in both versions (2013, 2018)
        todistus-2013-fi (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 0))
        todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 0))
        todistus-2013-sv (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 1))
        todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 1))
        todistus-2013-multilingual (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 2))
        todistus-2018-multilingual (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 2))
        old-todistus-2013-fi (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 0))

        ; Create two energiatodistus with rakennustunnus 2
        todistus-2013-rakennustunnus-2 (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-2))
        todistus-2018-rakennustunnus-2 (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-2))
        ; Insert all energiatodistus
        [todistus-2013-fi-id todistus-2018-fi-id todistus-2013-sv-id todistus-2018-sv-id todistus-2013-multilingual-id todistus-2018-multilingual-id todistus-2013-rakennustunnus-2-id todistus-2018-rakennustunnus-2-id old-todistus-2013-fi-id] (test-data.energiatodistus/insert! [todistus-2013-fi todistus-2018-fi todistus-2013-sv todistus-2018-sv todistus-2013-multilingual todistus-2018-multilingual todistus-2013-rakennustunnus-2 todistus-2018-rakennustunnus-2 old-todistus-2013-fi] laatija-id)]

    (t/testing "Test 123"
      (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018 "fi")
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))
            ]
        (println response)
        (t/is (= (:status response) 200))


        ))))

#_(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (merge (energiatodistus-test-data/generate-and-insert!
                                    1
                                    2013
                                    true
                                    laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                    1
                                    2018
                                    true
                                    laatija-id))]
    {:laatijat           laatijat
     :energiatodistukset energiatodistukset}))

#_(t/deftest fetch-aineisto
  (let [kayttaja-id (test-kayttajat/insert! (->> (test-kayttajat/generate-adds 1)
                                                 (map #(merge % test-kayttajat/laatija))
                                                 (map #(assoc %
                                                         :email "yhteyshenkilo@example.com"
                                                         :api-key "password"
                                                         ))))
        api-key (->> "yhteyshenkilo@example.com:password"
                     .getBytes
                     (.encode (Base64/getEncoder))
                     (String.))
        auth-header (str "Basic " api-key)
        aineisto-url "/api/private/energiatodistukset/2018/4/signature/system-sign/fi"]
    (t/testing "User with access receives redirect response"
      (let [response (ts/handler (-> (mock/request :put aineisto-url)
                                     (mock/header "Accept" "application/json")))]
        (t/is (some? (-> response :headers (get "Location"))))
        (t/is (= (:status response) 302))))))