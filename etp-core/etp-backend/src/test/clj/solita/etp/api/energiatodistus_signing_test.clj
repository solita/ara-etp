(ns solita.etp.api.energiatodistus-signing-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.config :as config]
    [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
    [solita.etp.test-data.laatija :as test-data.laatija]
    [solita.etp.test-timeserver :as test-timeserver]
    [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.enumerations SignatureLevel ValidationLevel)
           (eu.europa.esig.dss.model InMemoryDocument)
           (eu.europa.esig.dss.pades PAdESSignatureParameters)
           (eu.europa.esig.dss.pades.validation PDFDocumentValidator)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.validation SignedDocumentValidator)
           (java.io BufferedInputStream File FileInputStream ObjectInputStream)
           (java.time Clock Duration ZoneId)))

(defn energiatodistus-sign-url
  [et-id version]
  (str "/api/private/energiatodistukset/" version "/" et-id "/signature/system-sign"))

(t/use-fixtures :each ts/fixture)

(defn generate-pdf-as-file-mock [_ _ _ _]
  (let [in "src/test/resources/energiatodistukset/signing-process/generate-pdf-as-file.pdf"
        out "tmp-energiatodistukset/energiatodistus-in-system-signing-test.pdf"]
    (io/make-parents out)
    (io/copy (io/file in) (io/file out))
    out))

(def laatija-auth-time test-data.laatija/laatija-auth-time)

(defn get-parameters-in-test [_]
  (with-open [in (FileInputStream. "src/test/resources/energiatodistukset/signing-process/stateful-parameters")
              object-input-stream (ObjectInputStream. in)]
    ^PAdESSignatureParameters (.readObject object-input-stream)))

(t/deftest sign-with-system-test
  (with-bindings
    ;; Use an already existing pdf.
    {#'solita.etp.service.energiatodistus-pdf/generate-pdf-as-file  generate-pdf-as-file-mock
     ;; Mock the clock because laatija is not allowed to use system signing if the session is too old.
     #'time/clock                                                   (Clock/fixed (.plus laatija-auth-time (Duration/ofSeconds 1))
                                                                                 (ZoneId/systemDefault))
     #'solita.etp.service.signing.pdf-sign/get-tsp-source           test-timeserver/get-tsp-source-in-test
     #'solita.etp.service.signing.pdf-sign/get-signature-parameters get-parameters-in-test}
    (let [; Add laatija
          laatija-id (test-data.laatija/insert-suomifi-laatija!)

          todistus-2013-fi (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 0))
          todistus-2013-sv (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 1))
          todistus-2013-multilingual (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :kieli] 2))
          todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))
          todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 1))
          todistus-2018-multilingual (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))

          todistus-2018-future (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))
          todistus-2018-future-2 (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))

          ; Insert all energiatodistus
          [todistus-2013-fi-id
           todistus-2013-sv-id
           todistus-2013-multilingual-id
           todistus-2018-fi-id
           todistus-2018-sv-id
           todistus-2018-multilingual-id
           todistus-2018-future-id
           todistus-2018-future-2-id]
          (test-data.energiatodistus/insert! [todistus-2013-fi
                                              todistus-2013-sv
                                              todistus-2013-multilingual
                                              todistus-2018-fi
                                              todistus-2018-sv
                                              todistus-2018-multilingual
                                              todistus-2018-future
                                              todistus-2018-future-2] laatija-id)

          ; Add another laatija
          [other-laatija-id _] (test-data.laatija/generate-and-insert!)
          other-laatija-todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))

          [other-laatija-todistus-2018-sv-id]
          (test-data.energiatodistus/insert! [other-laatija-todistus-2018-sv] other-laatija-id)]
      (t/testing "Can not sign other laatija's todistus"
        (let [url (energiatodistus-sign-url other-laatija-todistus-2018-sv-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:body response) "Forbidden"))
          (t/is (= (:status response) 403))))
      (t/testing "Can sign 2013 fi version"
        (let [url (energiatodistus-sign-url todistus-2013-fi-id 2013)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Can sign 2013 sv version"
        (let [url (energiatodistus-sign-url todistus-2013-sv-id 2013)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Can sign 2013 multilingual version"
        (let [url (energiatodistus-sign-url todistus-2013-multilingual-id 2013)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Can sign 2018 fi version"
        (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Can sign 2018 sv version"
        (let [url (energiatodistus-sign-url todistus-2018-sv-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Can sign 2018 multilingual version"
        (let [url (energiatodistus-sign-url todistus-2018-multilingual-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 200))))
      (t/testing "Trying to sign 2018 fi version again should fail"
        (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:body response) (format "Energiatodistus %s is already signed" todistus-2018-fi-id)))
          (t/is (= (:status response) 409))))
      (t/testing "Trying to sing a pdf already in the signing process should fail"
        (let [url (energiatodistus-sign-url todistus-2018-future-id 2018)
              ;; Start a signing process in another thread.
              signing-process (future
                                (with-bindings
                                  ;; Use an already existing pdf.
                                  {#'solita.etp.service.energiatodistus-pdf/generate-pdf-as-file generate-pdf-as-file-mock}
                                  (ts/handler (-> (mock/request :post url)
                                                  (test-data.laatija/with-suomifi-laatija)
                                                  (mock/header "Accept" "application/json")))))
              ;; Wait naively so that the signing process starts in the other thread.
              _ (Thread/sleep 100)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              ;; Wait for the signing process to finish. Otherwise, the test-system fails.
              _ @signing-process]
          (t/is (= (:status response) 409))
          (t/is (= (:body response) (format "Energiatodistus %s is already in signing process" todistus-2018-future-id)))))
      (t/testing "Trying to cancel the signing and then sign again should work"
        (let [url (energiatodistus-sign-url todistus-2018-future-2-id 2018)
              cancel-url (str "/api/private/energiatodistukset/" 2018 "/" todistus-2018-future-2-id "/signature/cancel")
              response-cancel (atom nil)
              ;; Start a signing process in another thread.
              signing-process (future
                                (with-bindings
                                  ;; Use an already existing pdf.
                                  {#'solita.etp.service.energiatodistus-pdf/generate-pdf-as-file generate-pdf-as-file-mock}
                                  (ts/handler (-> (mock/request :post url)
                                                  (test-data.laatija/with-suomifi-laatija)
                                                  (mock/header "Accept" "application/json")))))
              ;; Try to cancel in a loop for two seconds
              _ (let [time-before (time/now)
                      timeout-seconds (Duration/ofSeconds 2)]
                  (loop [elapsed-time-seconds (Duration/ofSeconds 0)]
                    (when (every? true? [(not= (:body @response-cancel) "Ok")
                                         (> 0 (.compareTo elapsed-time-seconds timeout-seconds))])
                      (do
                        (reset! response-cancel (ts/handler (-> (mock/request :post cancel-url)
                                                                (test-data.laatija/with-suomifi-laatija)
                                                                (mock/header "Accept" "application/json"))))
                        (recur (Duration/between (time/now) time-before))))))
              ;; Wait for the signing process to finish. Otherwise, the test-system fails.
              _ @signing-process
              response-sign (ts/handler (-> (mock/request :post url)
                                            (test-data.laatija/with-suomifi-laatija)
                                            (mock/header "Accept" "application/json")))]
          (t/is (= (:status @response-cancel) 200))
          (t/is (= (:body @response-cancel) "Ok"))
          (t/is (= (:status response-sign) 200))
          (t/is (= (:body response-sign) "Ok")))))))

(t/deftest session-timeout-test
  (let [_ (test-data.laatija/insert-suomifi-laatija!)
        check-session-url (str "/api/private/energiatodistukset/validate-session")]
    (t/testing "Signing is allowed one second after auth_time"
      (with-bindings {#'config/system-signature-session-timeout-minutes config/system-signature-session-timeout-default-value
                      #'time/clock                                      (Clock/fixed (.plus laatija-auth-time (Duration/ofSeconds 1))
                                                                                     (ZoneId/systemDefault))}
        (let [response (ts/handler (-> (mock/request :get check-session-url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= response-body {:signing-allowed true})))))
    (t/testing "Signing is allowed 89 minutes after auth_time"
      (with-bindings {#'config/system-signature-session-timeout-minutes config/system-signature-session-timeout-default-value
                      #'time/clock                                      (Clock/fixed (.plus laatija-auth-time (Duration/ofMinutes 89))
                                                                                     (ZoneId/systemDefault))}
        (let [response (ts/handler (-> (mock/request :get check-session-url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= response-body {:signing-allowed true})))))
    (t/testing "Signing is not allowed 1 min before auth_time"
      (with-bindings {#'config/system-signature-session-timeout-minutes config/system-signature-session-timeout-default-value
                      #'time/clock                                      (Clock/fixed (.minus laatija-auth-time (Duration/ofMinutes 1))
                                                                                     (ZoneId/systemDefault))}
        (let [response (ts/handler (-> (mock/request :get check-session-url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= response-body {:signing-allowed false})))))
    (t/testing "Signing is not allowed 91 min after auth_time"
      (with-bindings {#'config/system-signature-session-timeout-minutes config/system-signature-session-timeout-default-value
                      #'time/clock                                      (Clock/fixed (.plus laatija-auth-time (Duration/ofMinutes 91))
                                                                                     (ZoneId/systemDefault))}
        (let [response (ts/handler (-> (mock/request :get check-session-url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= response-body {:signing-allowed false})))))))

(t/deftest sign-with-system-session-test
  (with-bindings
    ;; Use an already existing pdf.
    {#'solita.etp.service.energiatodistus-pdf/generate-pdf-as-file generate-pdf-as-file-mock}
    (let [; Add laatija
          laatija-id (test-data.laatija/insert-suomifi-laatija!)
          todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))
          [todistus-2018-fi-id]
          (test-data.energiatodistus/insert! [todistus-2018-fi] laatija-id)]

      (t/testing "Test that laatija can not sign a pdf when authenticated more than 90 min ago"
        ;; The auth_time in the JWT that is used in `insert-virtu-laatija!` is:
        ;;   Your time zone: Tuesday, March 3, 2020 12:22:49 PM GMT+02:00
        (let [url (energiatodistus-sign-url todistus-2018-fi-id 2018)
              response (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= (:status response) 401)))))))

(defn energiatodistus-get-url
  [et-id version language]
  (str "/api/private/energiatodistukset/" version "/" et-id "/pdf/" language "/energiatodistus-" et-id "-" language ".pdf"))

(defn lt-level? [^BufferedInputStream document-stream]
  (let [document (InMemoryDocument. document-stream)
        cert-verifier (CommonCertificateVerifier.)
        document-validator (doto (PDFDocumentValidator/fromDocument document)
                             (.setCertificateVerifier cert-verifier))
        reports (-> document-validator .validateDocument)
        simple-report (.getSimpleReport reports)
        signature-id (first (.getSignatureIdList simple-report))
        signature-level (.getSignatureFormat simple-report signature-id)]
    (= signature-level SignatureLevel/PAdES_BASELINE_LT)))

(t/deftest sign-with-system-lt-level-test
  (with-bindings
    ;; Here we don't mock the pdf generation or paramters as we want to see the real behaviour.
    {;; Mock the clock because laatija is not allowed to use system signing if the session is too old.
     #'time/clock                                         (Clock/fixed (.plus laatija-auth-time (Duration/ofSeconds 1))
                                                                       (ZoneId/systemDefault))
     #'solita.etp.service.signing.pdf-sign/get-tsp-source test-timeserver/get-tsp-source-in-test}
    (let [laatija-id (test-data.laatija/insert-suomifi-laatija!)
          todistus-2018-multilingual (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))

          [todistus-2018-multilingual-id]
          (test-data.energiatodistus/insert! [todistus-2018-multilingual] laatija-id)]
      (t/testing "Both signed pdfs are PAdES_BASELINE_LT level"
        (let [url (energiatodistus-sign-url todistus-2018-multilingual-id 2018)
              _ (ts/handler (-> (mock/request :post url)
                                       (test-data.laatija/with-suomifi-laatija)
                                       (mock/header "Accept" "application/json")))
              fi-response (ts/handler (-> (mock/request :get (energiatodistus-get-url todistus-2018-multilingual-id "2018" "fi"))
                                          (test-data.laatija/with-suomifi-laatija)
                                          (mock/header "Accept" "application/pdf")))
              sv-response (ts/handler (-> (mock/request :get (energiatodistus-get-url todistus-2018-multilingual-id "2018" "sv"))
                                          (test-data.laatija/with-suomifi-laatija)
                                          (mock/header "Accept" "application/pdf")))]
          (t/is (lt-level? (:body fi-response)))
          (t/is (lt-level? (:body sv-response))))))))
