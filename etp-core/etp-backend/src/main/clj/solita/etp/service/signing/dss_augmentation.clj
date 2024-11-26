(ns solita.etp.service.signing.dss-augmentation
  (:import (eu.europa.esig.dss.alert LogOnStatusAlert)
           (eu.europa.esig.dss.enumerations SignatureLevel)
           (eu.europa.esig.dss.model FileDocument)
           (eu.europa.esig.dss.pades PAdESSignatureParameters)
           (eu.europa.esig.dss.pades.signature PAdESService)
           (eu.europa.esig.dss.service.crl OnlineCRLSource)
           (eu.europa.esig.dss.service.tsp OnlineTSPSource)
           (eu.europa.esig.dss.spi.x509.aia DefaultAIASource)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (eu.europa.esig.dss.utils.apache.impl ApacheCommonsUtils)
           (java.io File)
           (java.nio.file Files OpenOption)
           (java.security KeyPair KeyPairGenerator KeyStore PrivateKey SecureRandom Security)
           (java.security.cert X509Certificate)
           (java.util ArrayList Date List)
           (org.bouncycastle.asn1.x500 X500Name)
           (org.bouncycastle.cert X509v3CertificateBuilder)
           (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.operator ContentSigner)
           (org.bouncycastle.operator.jcajce JcaContentSignerBuilder)))



(def key-and-cert
  (let [_ (Security/addProvider (BouncyCastleProvider.))
        ^KeyPairGenerator keyPairGenerator (KeyPairGenerator/getInstance "RSA")
        _ (-> keyPairGenerator (.initialize 2048))
        ^KeyPair keyPair (-> keyPairGenerator .generateKeyPair)

        subjectDN "CN=Self-Signed, O=Example, C=FI"
        issuerDN subjectDN
        serialNumber (BigInteger. 64 (SecureRandom.))
        ^Date notBefore (Date.)
        ^Date notAfter (Date. ^long (+ (System/currentTimeMillis) (* 365 24 60 60 1000)))

        ^X509v3CertificateBuilder certBuilder (JcaX509v3CertificateBuilder.
                                                (X500Name. issuerDN)
                                                serialNumber
                                                notBefore
                                                notAfter
                                                (X500Name. subjectDN)
                                                (-> keyPair .getPublic))

        ^ContentSigner signer (-> (JcaContentSignerBuilder. "SHA256withRSA") (.build (-> keyPair .getPrivate)))
        ^X509Certificate certificate (-> (JcaX509CertificateConverter.) (.setProvider "BC") (.getCertificate (-> certBuilder (.build signer))))]
    {:private-key (-> keyPair .getPrivate)
     :public-key  (-> keyPair .getPublic)
     :certificate certificate}))


(def pdf-file (FileDocument. "src/test/resources/energiatodistukset/signed-with-ocsp-information.pdf"))

(defn create-longer-validation-document [pdf-file]
  (let [parameters (PAdESSignatureParameters.)
        _ (-> parameters (.setSignatureLevel SignatureLevel/PAdES_BASELINE_T))

        certificate-verifier (doto (CommonCertificateVerifier.)
                               (.setOcspSource (OnlineOCSPSource.))
                               (.setAlertOnInvalidTimestamp (LogOnStatusAlert.)))

        #_(-> certificate-verifier (.setTrustedCertSources ""))
        key-store-file (File. "/tmp/test-key-store")
        key-store-pw (char-array "kissa")
        ^KeyStore key-store (doto (KeyStore/getInstance "PKCS12")
                              (.load nil key-store-pw))
        ;;key-entity-tsp-source (KeyEntityTSPSource. key-store "self-signed-tsa" key-store-pw)
        key-entity-tsp-source (KeyEntityTSPSource. ^PrivateKey (:private-key key-and-cert)
                                                   ^X509Certificate (:certificate key-and-cert)
                                                   ^List (doto (ArrayList.) (.add (:certificate key-and-cert))))
        _ (-> key-entity-tsp-source (.setTsaPolicy "1.2.3.4"))

        pades-service (doto (PAdESService. certificate-verifier)
                        (.setTspSource key-entity-tsp-source))

        ;;hopefully-lt-level-document (-> pades-service (.extendDocument pdf-file parameters))
        ]))

(create-longer-validation-document pdf-file)

#_(defn hmm2 [document]
    (let [cv (CommonCertificateVerifier.)
          (-> cv (.setAIASource (DefaultAIASource.)))
          (-> cv (.setOcspSource (OnlineOCSPSource.)))
          (-> cv (.setCrlSource (OnlineCRLSource.)))

          root-cert ()

          #_(-> certificate-verifier (.setTrustedCertSources ""))

          pades-service (PAdESService. certificate-verifier)
          ;;_ (-> padesService ())

          hopefully-lt-level-document (-> pades-service (.extendDocument pdf-file parameters))
          ]))
