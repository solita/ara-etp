(ns solita.common.certificates-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [solita.common.certificates :as certificates]))

(def test-cert-str "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")

(def test-cert (certificates/pem-str->certificate test-cert-str))

(t/deftest with-begin-and-end-test
  (let [test-cert-str-with-begin-and-end (certificates/with-begin-and-end
                                           test-cert-str)]
    (t/is (str/starts-with? test-cert-str-with-begin-and-end
                            certificates/certificate-start))
    (t/is (str/ends-with? test-cert-str-with-begin-and-end
                          certificates/certificate-end))
    (t/is (= test-cert-str-with-begin-and-end
             (certificates/with-begin-and-end test-cert-str-with-begin-and-end)))))

(t/deftest pem-str->certificate
  (t/is (thrown? org.bouncycastle.util.encoders.DecoderException
                 (certificates/pem-str->certificate "nothing meaningful")))
  (t/is (instance? org.bouncycastle.cert.X509CertificateHolder test-cert)))

(t/deftest subject-test
  (t/is (nil? (certificates/subject nil)))
  (t/is (nil? (certificates/subject "some string")))
  (t/is (= {:c "FI"
            :serialnumber "999004228"
            :givenname "TIMO"
            :surname "SPECIMEN-POTEX"
            :cn "SPECIMEN-POTEX TIMO 999004228"}
           (certificates/subject test-cert))))
