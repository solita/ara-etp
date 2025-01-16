(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [puumerkki.pdf :as puumerkki]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.service.energiatodistus-pdf :as pdf-service]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.model FileDocument)))

(t/use-fixtures :each ts/fixture)

(t/deftest sign-test
  (let [pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
        ]
    (t/testing "JUST TESTING"
      (-> (pdf-sign/sign-pdf ts/*aws-kms-client* pdf-file) (.save "TEST.pdf"))
      )))

(t/deftest sign-test-wtf
  (let [pdf-path "src/test/resources/energiatodistukset/system-signing/not-signed.pdf"
        ;;pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
        versio 2018
        signable-pdf-path (str/replace pdf-path #".pdf" "-signable.pdf")
        signature-png-path (str/replace pdf-path #".pdf" "-signature.png")
        laatija-fullname "Testaaja Laatija"
        _ (pdf-service/signature-as-png signature-png-path laatija-fullname)
        signable-pdf-path (puumerkki/add-watermarked-signature-space
                            pdf-path
                            signable-pdf-path
                            laatija-fullname
                            signature-png-path
                            75
                            (case versio 2013 648 2018 666))
        ;;signable-pdf-data (puumerkki/read-file signable-pdf-path)
        pdf-file (FileDocument. ^String signable-pdf-path)
        ]
    (t/testing "JUST TESTING"
      (-> (pdf-sign/wtf ts/*aws-kms-client* pdf-file) (.save "TEST.pdf"))
      )))
