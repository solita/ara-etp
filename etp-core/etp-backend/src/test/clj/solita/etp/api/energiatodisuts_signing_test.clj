(ns solita.etp.api.energiatodisuts-signing-test
  (:require
    [clojure.test :as t]
    [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
    [solita.etp.test-data.laatija :as test-data.laatija]
    [ring.mock.request :as mock]
    [solita.etp.test-system :as ts]))

(defn energiatodistus-sign-url
  [et-id version]
  (str "/api/private/energiatodistukset/" version "/" et-id "/signature/system-sign"))

(t/use-fixtures :each ts/fixture)

(t/deftest sign-with-system-test
  (let [; Add laatija
        laatija-id (test-data.laatija/insert-virtu-laatija!)

        todistus-2013-fi (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 0))
        todistus-2013-sv (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 1))
        todistus-2013-multilingual (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 2))
        todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))
        todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 1))
        todistus-2018-multilingual (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))

        ; Insert all energiatodistus
        [todistus-2013-fi-id
         todistus-2013-sv-id
         todistus-2013-multilingual-id
         todistus-2018-fi-id
         todistus-2018-sv-id
         todistus-2018-multilingual-id]
        (test-data.energiatodistus/insert! [todistus-2013-fi
                                            todistus-2013-sv
                                            todistus-2013-multilingual
                                            todistus-2018-fi
                                            todistus-2018-sv
                                            todistus-2018-multilingual] laatija-id)

        ; Add another laatija
        [other-laatija-id _] (test-data.laatija/generate-and-insert!)
        other-laatija-todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))

        [other-laatija-todistus-2018-sv-id]
        (test-data.energiatodistus/insert! [other-laatija-todistus-2018-sv] other-laatija-id)]
    (t/testing "Can not sign other laatija's todistus"
      (let [url (energiatodistus-sign-url other-laatija-todistus-2018-sv-id 2018)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:body response) "Forbidden"))
        (t/is (= (:status response) 403))))
    (t/testing "Can sign 2013 fi version"
      (let [url (energiatodistus-sign-url todistus-2013-fi-id 2013)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Can sign 2013 sv version"
      (let [url (energiatodistus-sign-url todistus-2013-sv-id 2013)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Can sign 2013 multilingual version"
      (let [url (energiatodistus-sign-url todistus-2013-multilingual-id 2013)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Can sign 2018 fi version"
      (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Can sign 2018 sv version"
      (let [url (energiatodistus-sign-url todistus-2018-sv-id 2018)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Can sign 2018 multilingual version"
      (let [url (energiatodistus-sign-url todistus-2018-multilingual-id 2018)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))
    (t/testing "Trying to sign 2018 fi version again should fail"

      (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018)
            response (ts/handler (-> (mock/request :put url)
                                     (test-data.laatija/with-virtu-laatija)
                                     (mock/header "Accept" "application/json")))
            ]
        (t/is (= (:body response) (format "Energiatodistus %s is already signed" todistus-2018-fi-id)))
        (t/is (= (:status response) 409))))
    ;; TODO: Is there actually a difference when calling with a wrong version? ? ?? ? ? ??
    ;; TODO: Is it a problem that you can sign a 2018 version via 2013 url?
    #_(t/testing "Can sign all laatija's energiatodistukset"
        (let [url (energiatodistus-sign-url todistus-2018-fi-id 2013)
              response (ts/handler (-> (mock/request :put url)
                                       (test-data.laatija/with-virtu-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
    ;; TODO: A test where trying to sign a pdf that's already in signing?
    #_(t/testing "Can sign all laatija's energiatodistukset"
        (let [url (energiatodistus-sign-url todistus-2013-multilingual-id 2013)
              what (pmap #((%)) [
                                 #(ts/handler (-> (mock/request :put url)
                                                  (test-data.laatija/with-virtu-laatija)
                                                  (mock/header "Accept" "application/json")))
                                 #(ts/handler (-> (mock/request :put url)
                                                  (test-data.laatija/with-virtu-laatija)
                                                  (mock/header "Accept" "application/json")))])]
          (println "WHAT " what)
          #_(t/is (= (:status response-first) 200))
          #_(t/is (= (:status response-second) 200))))
    ;;TODO: korvattu?
    #_(t/testing "Can sign all laatija's energiatodistukset"
        (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018 "fi")
              response (ts/handler (-> (mock/request :put url)
                                       (test-data.laatija/with-virtu-laatija)
                                       (mock/header "Accept" "application/json")))]
          (println response)
          (t/is (= (:status response) 200))))))
