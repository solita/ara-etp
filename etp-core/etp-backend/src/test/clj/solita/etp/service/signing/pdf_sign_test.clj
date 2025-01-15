(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.model FileDocument)))

(t/use-fixtures :each ts/fixture)

(t/deftest sign-test
  (let [pdf-file (FileDocument. "src/test/resources/energiatodistukset/system-signing/not-signed.pdf")
        ]
    (t/testing "JUST TESTING"
      (-> (pdf-sign/sign-pdf ts/*aws-kms-client* pdf-file) (.save "TEST.pdf"))
      )))
