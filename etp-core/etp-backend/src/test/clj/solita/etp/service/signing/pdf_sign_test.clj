(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [solita.etp.config :as config]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-signing :as et-signing]
            [solita.etp.service.sign :as sign-service]
            [puumerkki.pdf :as puumerkki]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.service.signing.dss-augmentation :as pdf-aug]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.model FileDocument)
           (java.io File)
           (java.nio.file Files Paths StandardCopyOption)
           (java.nio.charset StandardCharsets)
           (java.util Base64)))

(t/use-fixtures :each ts/fixture)

(defn get-allekirjoitus-id [laatija]
  ;; TODO: Need to update test-system to actually add an allekirjoitus-id
  ;; For the tests it does not currently matter: The allekirjoitus-id will be wrong
  ;; in the tests but it does not matter.
  "1")

(t/deftest sign-with-system-testing
  (let [; Add laatija
        laatija-add (->> (test-data.laatija/generate-adds 1) (first))
        laatija-id (first (test-data.laatija/insert! [laatija-add]))
        laatija (laatija-service/find-laatija-by-id
                  ts/*db* {:id laatija-id :rooli 0} laatija-id)
        laatija-allekirjoitus-id (get-allekirjoitus-id laatija)
        certs {:root-cert config/system-signature-certificate-root
               :int-cert  config/system-signature-certificate-intermediate
               :leaf-cert config/system-signature-certificate-leaf}

        todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))

        [todistus-2018-fi-id]
        (test-data.energiatodistus/insert! [todistus-2018-fi] laatija-id)]

    (t/testing "aaa"
      (energiatodistus-service/start-energiatodistus-signing! ts/*db* {:rooli 0 :id laatija-id} todistus-2018-fi-id)
      (let [digest (et-signing/find-energiatodistus-digest ts/*db* ts/*aws-s3-client* todistus-2018-fi-id "fi" laatija-allekirjoitus-id certs)
            _ (println "D:" digest)
            data (-> digest
                    :digest
                    (.getBytes StandardCharsets/UTF_8)
                    (#(.decode (Base64/getDecoder) %)))
            signature (pdf-sign/get-signature-from-system-cms-service ts/*aws-kms-client* data)
            b-level-doc (et-signing/sign-energiatodistus-pdf ts/*db* ts/*aws-s3-client* todistus-2018-fi-id "fi" laatija-allekirjoitus-id certs signature)
            _ (.save b-level-doc "WASD.pdf")
            ]))))

(t/deftest signed-pdf-has-laatija-id

  )

#_(t/deftest can-get-a-pdf-with-signature-field
    (let [pdf-path "src/test/resources/energiatodistukset/system-signing/not-signed.pdf"
          ;;pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
          versio 2018
          signable-pdf-path (str/replace pdf-path #".pdf" "-signable.pdf")
          signature-png-path (str/replace pdf-path #".pdf" "-signature.png")
          laatija-fullname "Specimen-Potex, Liisa"
          _ (pdf-sign/signature-as-png signature-png-path laatija-fullname)
          ;;signable-pdf-path
          #_(puumerkki/add-watermarked-signature-space
              pdf-path
              signable-pdf-path
              laatija-fullname
              signature-png-path
              75
              (case versio 2013 648 2018 666))
          ;;signable-pdf-data (puumerkki/read-file signable-pdf-path)
          pdf-file (FileDocument. ^String pdf-path)
          png-file (FileDocument. ^String signature-png-path)]
      (t/testing "JUST TESTING"
        (let [signed-document (pdf-sign/sign-a-pdf-with-mocks ts/*aws-kms-client* pdf-file png-file versio)]
          (Thread/sleep 10000)
          (-> signed-document
              #_(pdf-aug/augment-to-t-level)
              #_(pdf-aug/augment-to-lt)
              (.save "TEST.pdf")))
        ))
    )

#_(t/deftest sign-test-wtf
    (let [pdf-path "src/test/resources/energiatodistukset/system-signing/not-signed.pdf"
          ;;pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
          versio 2018
          signable-pdf-path (str/replace pdf-path #".pdf" "-signable.pdf")
          signature-png-path (str/replace pdf-path #".pdf" "-signature.png")
          laatija-fullname "Specimen-Potex, Liisa"
          _ (pdf-sign/signature-as-png signature-png-path laatija-fullname)
          ;;signable-pdf-path
          #_(puumerkki/add-watermarked-signature-space
              pdf-path
              signable-pdf-path
              laatija-fullname
              signature-png-path
              75
              (case versio 2013 648 2018 666))
          ;;signable-pdf-data (puumerkki/read-file signable-pdf-path)
          pdf-file (FileDocument. ^String pdf-path)
          png-file (FileDocument. ^String signature-png-path)]
      (t/testing "JUST TESTING"
        (let [signed-document (pdf-sign/sign-a-pdf-with-mocks ts/*aws-kms-client* pdf-file png-file versio)]
          (Thread/sleep 10000)
          (-> signed-document
              #_(pdf-aug/augment-to-t-level)
              #_(pdf-aug/augment-to-lt)
              (.save "TESTA.pdf")))
        )))
