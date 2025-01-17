(ns solita.etp.service.signing.dss-augmentation
  (:import (eu.europa.esig.dss.alert LogOnStatusAlert)
           (eu.europa.esig.dss.enumerations SignatureLevel)
           (eu.europa.esig.dss.model DSSDocument FileDocument)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters)
           (eu.europa.esig.dss.pades.signature PAdESService)
           (eu.europa.esig.dss.pades.validation PAdESCertificateSource)
           (eu.europa.esig.dss.pdf PdfDocumentReader)
           (eu.europa.esig.dss.pdf.pdfbox PdfBoxDocumentReader)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.spi DSSUtils)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509 CommonCertificateSource CommonTrustedCertificateSource ListCertificateSource)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (eu.europa.esig.dss.validation SignedDocumentValidator)
           (java.io File)
           (java.security KeyPair KeyPairGenerator KeyStore PrivateKey SecureRandom Security)
           (java.security.cert X509Certificate)
           (java.util ArrayList Date List Map)
           (org.bouncycastle.asn1.x500 X500Name)
           (org.bouncycastle.asn1.x509 ExtendedKeyUsage Extension KeyPurposeId)
           (org.bouncycastle.cert X509v3CertificateBuilder)
           (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.operator ContentSigner)
           (org.bouncycastle.operator.jcajce JcaContentSignerBuilder)))

;; TODO: Something to used locally only. Is this the right place?
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

(defn augment-to-t-level [pdf-file]
  (let [parameters (PAdESSignatureParameters.)
        _ (-> parameters (.setSignatureLevel SignatureLevel/PAdES_BASELINE_T))

        certificate-verifier (doto (CommonCertificateVerifier.)
                               (.setOcspSource (OnlineOCSPSource.))
                               (.setAlertOnInvalidTimestamp (LogOnStatusAlert.)))

        key-entity-tsp-source (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                                                   ^X509Certificate (:certificate tsp-key-and-cert)
                                                   ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
        _ (-> key-entity-tsp-source (.setTsaPolicy "1.2.3.4"))

        pades-service (doto (PAdESService. certificate-verifier)
                        (.setTspSource key-entity-tsp-source))]
    (-> pades-service (.extendDocument pdf-file parameters))))

;; TODO: continue. Why?
(defn get-pades-signature-source [^DSSDocument dss-doc]
  (let [pdf-reader (PdfBoxDocumentReader. dss-doc)
        ^Map sigDirs (.extractSigDictionaries pdf-reader)
        vri-dir-name "VRI"
        signer-information ""
        ]
    #_(PAdESCertificateSource. pdf-sig-revision vri-dir-name signer-information)
    ))

(defn augment-to-lt [pdf-document]
  (let [^DSSDocument t-level (augment-to-t-level pdf-document)
        parameters (doto (PAdESSignatureParameters.) (.setSignatureLevel SignatureLevel/PAdES_BASELINE_LT))

        ;; TODO: config/leaf or cert provided by card reader
        ^CertificateToken trusted-cert (DSSUtils/loadCertificate (File. "src/test/resources/system-signature/local-signing-root.pem.crt"))

        ;; TODO: Should use a trust list here?
        certificate-verifier (doto (CommonCertificateVerifier.)
                               (.setOcspSource (OnlineOCSPSource.))
                               (.setTrustedCertSources (doto (ListCertificateSource.)
                                                         (.add (doto
                                                                 (CommonTrustedCertificateSource.)
                                                                 (.addCertificate trusted-cert)))))
                               (.setAlertOnMissingRevocationData (LogOnStatusAlert.)))

        key-entity-tsp-source (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                                                   ^X509Certificate (:certificate tsp-key-and-cert)
                                                   ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
        _ (-> key-entity-tsp-source (.setTsaPolicy "1.2.3.4"))

        pades-service (doto (PAdESService. certificate-verifier)
                        (.setTspSource key-entity-tsp-source))]
    (-> pades-service (.extendDocument t-level parameters))
    #_(-> profit? (.save "ocsp.pdf"))))

(def pdf-file (FileDocument. "src/test/resources/energiatodistukset/signed-with-ocsp-information.pdf"))
#_(get-pades-signature-source pdf-file)
#_(augment-to-lt pdf-file)