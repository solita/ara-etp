(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.common.time :as time]
            [solita.etp.config :as config]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-signing :as energiatodistus-signing-service]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.io File InputStream)
           (java.nio.charset StandardCharsets)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.bouncycastle.cert.jcajce JcaX509v3CertificateBuilder)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.asn1.x500 X500Name)
           (java.security KeyPair KeyPairGenerator PrivateKey SecureRandom Security)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.security KeyPair KeyPairGenerator PrivateKey SecureRandom Security)
           (java.security.cert X509Certificate)
           (java.util ArrayList Date List)
           (org.bouncycastle.asn1.x500 X500Name)
           (org.bouncycastle.asn1.x509 ExtendedKeyUsage KeyPurposeId Extension)
           (org.bouncycastle.cert X509v3CertificateBuilder)
           (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.operator ContentSigner)
           (org.bouncycastle.operator.jcajce JcaContentSignerBuilder)
           (java.util Base64 Date)))

(t/use-fixtures :each ts/fixture)


(def tsp-key-and-cert
  (let [_ (Security/addProvider (BouncyCastleProvider.))    ;; TODO: Should this be done elsewhere?
        ^KeyPairGenerator keyPairGenerator (doto (KeyPairGenerator/getInstance "RSA")
                                             (.initialize 2048))
        ^KeyPair keyPair (-> keyPairGenerator .generateKeyPair)

        subjectDN "CN=Self-Signed, O=Example, C=FI"
        issuerDN subjectDN
        serialNumber (BigInteger. 64 (SecureRandom.))
        ^Date notBefore (Date.)
        ^Date notAfter (Date. ^long (+ (System/currentTimeMillis) (* 365 24 60 60 1000)))

        ^X509v3CertificateBuilder certBuilder (doto (JcaX509v3CertificateBuilder.
                                                      (X500Name. issuerDN)
                                                      serialNumber
                                                      notBefore
                                                      notAfter
                                                      (X500Name. subjectDN)
                                                      (-> keyPair .getPublic))
                                                (.addExtension Extension/extendedKeyUsage
                                                               true
                                                               (ExtendedKeyUsage. KeyPurposeId/id_kp_timeStamping)))

        ^ContentSigner signer (-> (JcaContentSignerBuilder. "SHA256withRSA") (.build (-> keyPair .getPrivate)))
        ^X509Certificate certificate (-> (doto (JcaX509CertificateConverter.) (.setProvider "BC"))
                                         (.getCertificate (-> certBuilder (.build signer))))]
    {:private-key (-> keyPair .getPrivate)
     :public-key  (-> keyPair .getPublic)
     :certificate certificate}))

(defn tsp-source-in-test []
  (doto (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                             ^X509Certificate (:certificate tsp-key-and-cert)
                             ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
    (.setTsaPolicy "1.2.3.4")))

(defn get-allekirjoitus-id [laatija]
  ;; TODO: Need to update test-system to actually add an allekirjoitus-id
  ;;       Maybe also schema?
  ;;       Maybe create a generator?
  ;; For the tests it does not currently matter: The allekirjoitus-id will be wrong
  ;; in the tests but it does not matter.
  "1")

(t/deftest sign-with-system-testing
  (with-bindings
    ;; Use an already existing pdf.
    {#'solita.etp.service.signing.pdf-sign/get-tsp-source tsp-source-in-test}
    (let [; Add laatija
          laatija-add (->> (test-data.laatija/generate-adds 1) (first))
          laatija-id (first (test-data.laatija/insert! [laatija-add]))
          laatija (laatija-service/find-laatija-by-id
                    ts/*db* {:id laatija-id :rooli 6} laatija-id)
          whoami {:id laatija-id :rooli 0}
          laatija-allekirjoitus-id (get-allekirjoitus-id laatija)

          todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))

          [todistus-2018-fi-id]
          (test-data.energiatodistus/insert! [todistus-2018-fi] laatija-id)]

      (t/testing "The energiatodistus is LT level"
        (energiatodistus-signing-service/sign-with-system
          {:db                       ts/*db*
           :aws-s3-client            ts/*aws-s3-client*
           :aws-kms-client           ts/*aws-kms-client*
           :whoami                   whoami
           :laatija-allekirjoitus-id laatija-allekirjoitus-id
           :now                      (time/now)
           :id                       todistus-2018-fi-id})
        ;;TODO: Do automatic testing
        (-> (PDDocument/load ^InputStream (file-service/find-file
                                            ts/*aws-s3-client*
                                            (solita.etp.service.energiatodistus/file-key (str todistus-2018-fi-id) "fi")))
            (.save "HELLO.pdf"))))))

(t/deftest signed-pdf-has-laatija-id

  )

(t/deftest create-stateful-signature-parameters-test
  ()

  )
