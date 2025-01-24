(ns solita.etp.service.signing.pdf-sign
  (:require
    [clojure.java.io :as io]
    [solita.etp.service.sign :as sign-service]
    [solita.etp.config :as config])
  (:import (eu.europa.esig.dss.alert LogOnStatusAlert)
           (eu.europa.esig.dss.enumerations SignatureLevel DigestAlgorithm SignatureAlgorithm SignaturePackaging)
           (eu.europa.esig.dss.model BLevelParameters DSSDocument DSSMessageDigest Digest FileDocument InMemoryDocument ToBeSigned SignatureValue)
           (eu.europa.esig.dss.cades.signature CMSSignedDocument)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters PAdESUtils SignatureFieldParameters SignatureImageParameters SignatureImageTextParameters)
           (eu.europa.esig.dss.pades.signature ExternalCMSService PAdESService PAdESWithExternalCMSService)
           (eu.europa.esig.dss.pdf.pdfbox PdfBoxNativeObjectFactory)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.service.tsp OnlineTSPSource)
           (eu.europa.esig.dss.spi DSSMessageDigestCalculator DSSUtils)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509 CommonTrustedCertificateSource ListCertificateSource)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.awt Color Font)
           (java.awt.image BufferedImage)
           (java.io ByteArrayInputStream File InputStream ObjectInputStream ObjectOutputStream Serializable)
           (java.security KeyPair KeyPairGenerator PrivateKey SecureRandom Security)
           (java.security.cert X509Certificate)
           (java.time Instant ZoneId)
           (java.time.format DateTimeFormatter)
           (java.util ArrayList Collection Date List)
           (javax.imageio ImageIO)
           (org.apache.axis.utils ByteArrayOutputStream)
           (org.bouncycastle.asn1.x500 X500Name)
           (org.bouncycastle.asn1.x509 ExtendedKeyUsage KeyPurposeId Extension)
           (org.bouncycastle.cert X509v3CertificateBuilder)
           (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.operator ContentSigner)
           (org.bouncycastle.operator.jcajce JcaContentSignerBuilder)))


;; Used to create and find the signature field since the pdf needs to be cached in between of
;; getting the digest and the signature.
(def signature-field-id "Signature Field")


(def timezone (ZoneId/of "Europe/Helsinki"))
(def time-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
                               timezone))

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

(defn signature-as-png [path ^String laatija-fullname]
  (let [now (Instant/now)
        width (max 125 (* (count laatija-fullname) 6))
        img (BufferedImage. width 30 BufferedImage/TYPE_INT_ARGB)
        g (.getGraphics img)]
    (doto (.getGraphics img)
      (.setFont (Font. Font/SANS_SERIF Font/TRUETYPE_FONT 10))
      (.setColor Color/BLACK)
      (.drawString laatija-fullname 2 10)
      (.drawString (.format time-formatter now) 2 25)
      (.dispose))
    (ImageIO/write img "PNG" (io/file path))))

(defn sign-a-pdf-with-mocks [aws-kms-client pdf-document ^FileDocument signature-png versio]
  (let [key-entity-tsp-source (doto (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                                                         ^X509Certificate (:certificate tsp-key-and-cert)
                                                         ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
                                (.setTsaPolicy "1.2.3.4"))

        ;; TODO: config/leaf or cert provided by card reader
        ^CertificateToken trusted-cert (DSSUtils/loadCertificate (File. "src/test/resources/system-signature/local-signing-root.pem.crt"))

        ^CommonCertificateVerifier cert-verifier (doto (CommonCertificateVerifier.)
                                                   (.setOcspSource (OnlineOCSPSource.))
                                                   (.setTrustedCertSources (doto (ListCertificateSource.)
                                                                             (.add (doto
                                                                                     (CommonTrustedCertificateSource.)
                                                                                     (.addCertificate trusted-cert)))))
                                                   (.setAlertOnInvalidTimestamp (LogOnStatusAlert.)))

        ^PAdESService service (doto (PAdESService. cert-verifier)
                                (.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                (.setTspSource key-entity-tsp-source))

        ^CertificateToken signing-cert-token (-> config/system-signature-certificate-leaf
                                                 (DSSUtils/convertToDER)
                                                 (DSSUtils/loadCertificate))
        ^List cert-chain (ArrayList. ^Collection (->> [config/system-signature-certificate-leaf
                                                       config/system-signature-certificate-intermediate
                                                       config/system-signature-certificate-root]
                                                      (mapv #(-> %
                                                                 (DSSUtils/convertToDER)
                                                                 (DSSUtils/loadCertificate)))))
        ^BLevelParameters b-level-params (doto (BLevelParameters.)
                                           (.setSigningDate (Date.))
                                           (.setClaimedSignerRoles (ArrayList. ["Laatija"]))
                                           (.setSignedAssertions (ArrayList. ["SignedAssertion?"]))
                                           #_(.setCommitmentTypeIndications (ArrayList.))
                                           )

        ^SignatureImageTextParameters txt-params (doto (SignatureImageTextParameters.)
                                                   )

        ^SignatureFieldParameters sig-field-params (doto (SignatureFieldParameters.)
                                                     (.setPage 1)
                                                     (.setOriginX 75)
                                                     (.setOriginY (case versio 2013 648 2018 666))

                                                     )

        ^SignatureImageParameters sig-img (doto (SignatureImageParameters.)
                                            (.setFieldParameters sig-field-params)
                                            (.setImage signature-png)
                                            (.setZoom 133))

        ^PAdESSignatureParameters signature-parameters (doto (PAdESSignatureParameters.)
                                                         (.setBLevelParams b-level-params)
                                                         (.setSignatureLevel SignatureLevel/PAdES_BASELINE_LT)
                                                         (.setCertificateChain cert-chain)
                                                         (.setReason "DSS testing") ;; This is seen in the signature.
                                                         (.setSigningCertificate signing-cert-token)
                                                         (.setSignaturePackaging SignaturePackaging/ENVELOPED)
                                                         (.setDigestAlgorithm DigestAlgorithm/SHA256)
                                                         (.setSignerName "Testaaja Laatija")
                                                         (.setIncludeVRIDictionary true)
                                                         (.setImageParameters sig-img))

        ;; Is this the document I could save temporarily? No this is the digest :(
        ^ToBeSigned data-to-sign (-> service (.getDataToSign pdf-document signature-parameters))

        ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 (.readAllBytes (sign-service/sign aws-kms-client (-> data-to-sign .getBytes))))

        _ (assert (-> service (.isValidSignatureValue data-to-sign signature-value signing-cert-token)))

        ^DSSDocument signed-document (-> service (.signDocument pdf-document signature-parameters signature-value))

        ]
    ;;signed-document
    signed-document))

;; TODO: Make dynamic?
(defn- get-tsp-source []
  #_(let [tsa-url (config/tsa-endpoint-url)]
      (OnlineTSPSource. tsa-url))
  (doto (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                             ^X509Certificate (:certificate tsp-key-and-cert)
                             ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
    (.setTsaPolicy "1.2.3.4")))

(defn get-unsigned-document-with-signature-field
  [document-to-sing signature-options]

  #_(let [^PAdESService service (doto (PAdESService. cert-verifier)
                                  #_(.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                  (.setTspSource key-entity-tsp-source))

          ]
      )

  {:document-with-signature-field nil})

(defn object->input-stream [^Object object]
  (let [baos (ByteArrayOutputStream.)
        oos (ObjectOutputStream. baos)
        _ (-> oos (.writeObject object))
        bais (ByteArrayInputStream. (.toByteArray baos))
        ]
    bais
    ))

(defn input-stream->object [^InputStream input-stream]
  (-> (ObjectInputStream. input-stream)
      .readObject))


(defn get-digest
  [^File file {:keys [signature-png-path versio laatija-fullname] :as signature-options} {:keys [root-cert int-cert leaf-cert] :as certs}]
  (let [document (FileDocument. file)
        tsp-source (get-tsp-source)

        cert-verifier (CommonCertificateVerifier.)
        signature-png (FileDocument. ^String signature-png-path)

        ^CertificateToken signing-cert-token (-> leaf-cert
                                                 (DSSUtils/convertToDER)
                                                 (DSSUtils/loadCertificate))
        ^List cert-chain (ArrayList. ^Collection (->> [leaf-cert
                                                       int-cert
                                                       root-cert]
                                                      (mapv #(-> %
                                                                 (DSSUtils/convertToDER)
                                                                 (DSSUtils/loadCertificate)))))

        ^BLevelParameters b-level-params (doto (BLevelParameters.)
                                           (.setSigningDate (Date.))
                                           (.setClaimedSignerRoles (ArrayList. ["Laatija"]))
                                           (.setSignedAssertions (ArrayList. ["SignedAssertion?"]))
                                           #_(.setCommitmentTypeIndications (ArrayList.))
                                           )

        ^SignatureFieldParameters sig-field-params (doto (SignatureFieldParameters.)
                                                     (.setPage 1)
                                                     (.setOriginX 75)
                                                     (.setOriginY (case versio 2013 648 2018 666)))

        ^SignatureImageParameters sig-img (doto (SignatureImageParameters.)
                                            (.setFieldParameters sig-field-params)
                                            (.setImage signature-png)
                                            (.setZoom 133))

        ^PAdESSignatureParameters signature-parameters (doto (PAdESSignatureParameters.)
                                                         (.setBLevelParams b-level-params)
                                                         (.setSignatureLevel SignatureLevel/PAdES_BASELINE_T)
                                                         (.setCertificateChain cert-chain)
                                                         (.setReason "DSS testing") ;; This is seen in the signature.
                                                         (.setSigningCertificate signing-cert-token)
                                                         (.setSignaturePackaging SignaturePackaging/ENVELOPED)
                                                         (.setDigestAlgorithm DigestAlgorithm/SHA256)
                                                         (.setSignerName laatija-fullname)
                                                         (.setIncludeVRIDictionary true)
                                                         (.setImageParameters sig-img))

        ^PAdESService service (doto (PAdESService. cert-verifier)
                                (.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                (.setTspSource tsp-source))

        ;; Is this the document I could save temporarily? No this is the digest :(
        ^ToBeSigned data-to-sign (-> service (.getDataToSign document signature-parameters))

        ^Digest digest (Digest. DigestAlgorithm/SHA256 (.getBytes data-to-sign))]
    {:digest (.getBase64Value digest)
     :sig-params-is (object->input-stream signature-parameters)}))

(defn sign-document-as-pades-t-level
  [sig-params-is unsigned-document-is signature]
  (let [document (InMemoryDocument. ^InputStream unsigned-document-is)
        ^PAdESSignatureParameters signature-parameters (input-stream->object sig-params-is)
        ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 signature)

        tsp-source (get-tsp-source)

        cert-verifier (CommonCertificateVerifier.)

        ^PAdESService service (doto (PAdESService. cert-verifier)
                                (.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                (.setTspSource tsp-source))

        ^DSSDocument signed-document (-> service (.signDocument document signature-parameters signature-value))
        ]
    ;;TODO: Hide
    signed-document
    )
  )

(defn get-seocnds-until-next-ocsp-update []

  {:ocsp-next-update 10}
  )

(defn augment-pdf-to-pades-lt-level []

  )


