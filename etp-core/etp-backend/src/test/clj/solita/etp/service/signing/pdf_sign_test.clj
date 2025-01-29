(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [solita.etp.config :as config]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-signing :as et-signing]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-system :as ts])
  (:import (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.nio.charset StandardCharsets)
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

;; TODO: Something to used locally only. Is this the right place?
;;       Put into tests and use with-rebinds?
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
            ])))))

(t/deftest signed-pdf-has-laatija-id

  )
