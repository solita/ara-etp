(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [puumerkki.pdf :as puumerkki]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.service.signing.dss-augmentation :as pdf-aug]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.model FileDocument)))

(t/use-fixtures :each ts/fixture)

(t/deftest sign-test
  (let [pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
        ]
    (t/testing "JUST TESTING"
      (-> (pdf-sign/sign-pdf ts/*aws-kms-client* pdf-file) (.save "TEST.pdf"))
      )))

(t/deftest signed-pdf-has-laatija-id

  )

(t/deftest sign-test-wtf
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
        png-file (FileDocument. ^String signature-png-path)
        ]
    (t/testing "JUST TESTING"
      (let [signed-document (pdf-sign/wtf ts/*aws-kms-client* pdf-file png-file versio)]
        (Thread/sleep 10000)
        (-> signed-document
            (pdf-aug/augment-to-t-level)
            (pdf-aug/augment-to-lt)
            (.save "TEST.pdf")))
      )))
