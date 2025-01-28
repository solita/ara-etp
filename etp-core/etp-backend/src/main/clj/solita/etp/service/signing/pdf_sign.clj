(ns solita.etp.service.signing.pdf-sign
  (:require
    [clojure.java.io :as io]
    [solita.etp.service.sign :as sign-service]
    [solita.common.time :as time]
    [solita.etp.config :as config])
  (:import (eu.europa.esig.dss.alert LogOnStatusAlert)
           (eu.europa.esig.dss.enumerations SignatureLevel DigestAlgorithm SignatureAlgorithm SignaturePackaging EncryptionAlgorithm)
           (eu.europa.esig.dss.model BLevelParameters DSSDocument DSSMessageDigest Digest FileDocument InMemoryDocument ToBeSigned SignatureValue)
           (eu.europa.esig.dss.cades.signature CMSSignedDocument)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters PAdESUtils SignatureFieldParameters SignatureImageParameters SignatureImageTextParameters)
           (eu.europa.esig.dss.pades.signature ExternalCMSService PAdESService PAdESWithExternalCMSService)
           (eu.europa.esig.dss.pdf.pdfbox PdfBoxNativeObjectFactory)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.service.tsp OnlineTSPSource)
           (eu.europa.esig.dss.spi DSSMessageDigestCalculator DSSRevocationUtils DSSUtils)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509 CommonTrustedCertificateSource ListCertificateSource)
           (eu.europa.esig.dss.spi.x509.revocation.ocsp OCSPToken)
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

(defn pem->CertificateToken [cert-pem]
  ^CertificateToken (-> cert-pem
                        (DSSUtils/convertToDER)
                        (DSSUtils/loadCertificate)))

(defn get-system-signature-parameters
  "This should produce the same level of a signature as the card reader."
  []
  (let [leaf-cert config/system-signature-certificate-leaf
        int-cert config/system-signature-certificate-intermediate
        root-cert config/system-signature-certificate-root
        ^List cert-chain (ArrayList. ^Collection (->> [leaf-cert
                                                       int-cert
                                                       root-cert]
                                                      (mapv #(-> %
                                                                 (DSSUtils/convertToDER)
                                                                 (DSSUtils/loadCertificate)))))]
    (doto (PAdESSignatureParameters.)
      (#(doto (.bLevel %)
          (.setSigningDate (-> (^Instant time/now) Date/from))))
      (.setSigningCertificate (pem->CertificateToken leaf-cert))
      (.setDigestAlgorithm DigestAlgorithm/SHA256)
      (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
      (.setCertificateChain cert-chain))))

;; TODO: This belongs to sign service?
(defn get-signature-from-system-cms-service [aws-kms-client ^bytes digest]
  (let [certificate-verifier (CommonCertificateVerifier.)
        signature-parameters (get-system-signature-parameters)
        ^DSSMessageDigest dss-digest (DSSMessageDigest. DigestAlgorithm/SHA256 digest)
        _ (println (-> dss-digest (.toString)))
        ^ExternalCMSService system-signature-cms-service (ExternalCMSService. certificate-verifier)
        data-to-sign (.getDataToSign system-signature-cms-service dss-digest signature-parameters)

        ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 (.readAllBytes (sign-service/sign aws-kms-client (-> data-to-sign .getBytes))))

        cms-signature (-> system-signature-cms-service (.signMessageDigest dss-digest signature-parameters signature-value))]
    cms-signature))


;; Used to create and find the signature field since the pdf needs to be cached in between of
;; getting the digest and the signature.
(def signature-field-id "Signature Field")


(def timezone (ZoneId/of "Europe/Helsinki"))
(def time-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
                               timezone))

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

#_(defn sign-a-pdf-with-mocks [aws-kms-client pdf-document ^FileDocument signature-png versio]
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

(defn object->input-stream [^Object object]
  (let [baos (ByteArrayOutputStream.)
        _ (doto (ObjectOutputStream. baos)
            (.writeObject object))
        bais (ByteArrayInputStream. (.toByteArray baos))
        ]
    bais
    ))

(defn input-stream->object [^InputStream input-stream]
  (-> (ObjectInputStream. input-stream)
      .readObject))

(defn get-digest-for-external-cms-service
  [^File unsigned-pdf {:keys [^File signature-png page origin-x origin-y zoom]}]
  (let [service (PAdESWithExternalCMSService.)
        signature-png (FileDocument. signature-png)

        ;; TODO: versio and siganture-params are energiatodistus specific
        versio 2018
        ^SignatureFieldParameters sig-field-params (doto (SignatureFieldParameters.)
                                                     (.setPage 1)
                                                     (.setOriginX 75)
                                                     (.setOriginY (case versio 2013 648 2018 666)))

        ^SignatureImageParameters sig-img (doto (SignatureImageParameters.)
                                            (.setFieldParameters sig-field-params)
                                            (.setImage signature-png)
                                            (.setZoom 133))

        signature-parameters (doto (PAdESSignatureParameters.)
                               (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
                               (.setImageParameters sig-img)
                               (.setDigestAlgorithm DigestAlgorithm/SHA256))

        ^DSSMessageDigest message-digest (-> service (.getMessageDigest (FileDocument. unsigned-pdf) signature-parameters))
        _ (println (-> message-digest .toString))

        ]
    {:digest              (.getBase64Value message-digest)
     :stateful-parameters (object->input-stream signature-parameters)}))

(defn sign-with-external-cms-service-signature
  ""
  [^InputStream pdf-file stateful-parameters ^bytes cms-signature]
  (let [^PAdESSignatureParameters signature-parameters (input-stream->object stateful-parameters)
        unsigned-pdf (InMemoryDocument. pdf-file)

        service (PAdESWithExternalCMSService.)

        signed-pdf-b-level (-> service (.signDocument unsigned-pdf signature-parameters cms-signature))
        ]
    signed-pdf-b-level))

(defn b-level->lt-level []

  )

#_(defn get-digest
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
                                                           #_(.setCertificateChain cert-chain)
                                                           (.setReason "DSS testing") ;; This is seen in the signature.
                                                           #_(.setSigningCertificate signing-cert-token)
                                                           (.setEncryptionAlgorithm (EncryptionAlgorithm/RSA))
                                                           (.setGenerateTBSWithoutCertificate true)
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
      {:digest        (.getBase64Value digest)
       :sig-params-is (object->input-stream signature-parameters)}))

#_(defn sign-document-as-pades-t-level
    [sig-params-is unsigned-document-is {:keys [root-cert int-cert leaf-cert] :as certs} signature]
    (let [document (InMemoryDocument. ^InputStream unsigned-document-is)
          ^PAdESSignatureParameters signature-parameters (input-stream->object sig-params-is)
          ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 signature)

          tsp-source (get-tsp-source)

          ^CertificateToken trusted-cert (-> root-cert
                                             (DSSUtils/convertToDER)
                                             (DSSUtils/loadCertificate))
          ^CertificateToken signing-cert-token (-> leaf-cert
                                                   (DSSUtils/convertToDER)
                                                   (DSSUtils/loadCertificate))

          certs (into {} (map (fn [[k v]] [k (-> v (DSSUtils/convertToDER) (DSSUtils/loadCertificate)
                                                 )]) certs))
          _ (println certs)

          ocsp-source (OnlineOCSPSource.)

          cert-verifier (doto (CommonCertificateVerifier.)
                          (.setOcspSource ocsp-source)
                          (.setTrustedCertSources (doto (ListCertificateSource.)
                                                    (.add (doto
                                                            (CommonTrustedCertificateSource.)
                                                            (.addCertificate trusted-cert)))))
                          (.setAlertOnInvalidTimestamp (LogOnStatusAlert.)))

          ^PAdESService service (doto (PAdESService. cert-verifier)
                                  (.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                  (.setTspSource tsp-source))

          ^List cert-chain (ArrayList. ^Collection (->> [leaf-cert
                                                         int-cert
                                                         root-cert]
                                                        (mapv #(-> %
                                                                   (DSSUtils/convertToDER)
                                                                   (DSSUtils/loadCertificate)))))
          _ (doto signature-parameters
              (.setCertificateChain cert-chain)
              (.setSigningCertificate signing-cert-token))

          ^DSSDocument signed-document (-> service (.signDocument document signature-parameters signature-value))

          ;; Get the nex ocsp-update

          #_(

              ^OCSPToken ocsp-resp (-> cert-verifier .getOcspSource (.getRevocationToken (:leaf-cert certs) (:int-cert certs)))
                                   _ (println "AAA::" (.toString ocsp-resp))

                                   wait1 (- (.getEpochSecond (.toInstant ^Date (.getNextUpdate ocsp-resp)))
                                            (.getEpochSecond ^Instant (time/now)))

                                   ^OCSPToken ocsp-resp (-> ocsp-source (.getRevocationToken (:int-cert certs) (:root-cert certs)))
                                   _ (println "BBB::" (.toString ocsp-resp))

                                   wait2 (- (.getEpochSecond (.toInstant ^Date (.getNextUpdate ocsp-resp)))
                                            (.getEpochSecond ^Instant (time/now)))

                                   _ (when (>= (max wait1 wait2) 0)
                                       (Thread/sleep (+ 1 (max wait1 wait2))))

                                   updated-parameters (doto signature-parameters (.setSignatureLevel SignatureLevel/PAdES_BASELINE_LT))

                                   extended-document (-> service (.extendDocument signed-document updated-parameters))

                                   )
          ]
      ;;TODO: Hide
      ;;extended-document
      signed-document
      ))

#_(defn system-sign [digest]
    (let [^CommonCertificateVerifier cert-verifier (CommonCertificateVerifier.)
          ^ExternalCMSService padesCMSGeneratorService (cert-verifier)
          ]))

#_(defn sign-document-as-pades-t-level
    [sig-params-is unsigned-document-is {:keys [root-cert int-cert leaf-cert] :as certs} signature]
    (let [document (InMemoryDocument. ^InputStream unsigned-document-is)
          ^PAdESSignatureParameters signature-parameters (input-stream->object sig-params-is)
          ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 signature)

          tsp-source (get-tsp-source)

          ^CertificateToken trusted-cert (-> root-cert
                                             (DSSUtils/convertToDER)
                                             (DSSUtils/loadCertificate))
          ^CertificateToken signing-cert-token (-> leaf-cert
                                                   (DSSUtils/convertToDER)
                                                   (DSSUtils/loadCertificate))

          certs (into {} (map (fn [[k v]] [k (-> v (DSSUtils/convertToDER) (DSSUtils/loadCertificate)
                                                 )]) certs))
          _ (println certs)

          ocsp-source (OnlineOCSPSource.)

          cert-verifier (doto (CommonCertificateVerifier.)
                          (.setOcspSource ocsp-source)
                          (.setTrustedCertSources (doto (ListCertificateSource.)
                                                    (.add (doto
                                                            (CommonTrustedCertificateSource.)
                                                            (.addCertificate trusted-cert)))))
                          (.setAlertOnInvalidTimestamp (LogOnStatusAlert.)))

          ^PAdESService service (doto (PAdESService. cert-verifier)
                                  (.setPdfObjFactory (PdfBoxNativeObjectFactory.))
                                  (.setTspSource tsp-source))

          ^List cert-chain (ArrayList. ^Collection (->> [leaf-cert
                                                         int-cert
                                                         root-cert]
                                                        (mapv #(-> %
                                                                   (DSSUtils/convertToDER)
                                                                   (DSSUtils/loadCertificate)))))
          _ (doto signature-parameters
              (.setCertificateChain cert-chain)
              (.setSigningCertificate signing-cert-token))

          ^DSSDocument signed-document (-> service (.signDocument document signature-parameters signature-value))

          ;; Get the nex ocsp-update

          #_(

              ^OCSPToken ocsp-resp (-> cert-verifier .getOcspSource (.getRevocationToken (:leaf-cert certs) (:int-cert certs)))
                                   _ (println "AAA::" (.toString ocsp-resp))

                                   wait1 (- (.getEpochSecond (.toInstant ^Date (.getNextUpdate ocsp-resp)))
                                            (.getEpochSecond ^Instant (time/now)))

                                   ^OCSPToken ocsp-resp (-> ocsp-source (.getRevocationToken (:int-cert certs) (:root-cert certs)))
                                   _ (println "BBB::" (.toString ocsp-resp))

                                   wait2 (- (.getEpochSecond (.toInstant ^Date (.getNextUpdate ocsp-resp)))
                                            (.getEpochSecond ^Instant (time/now)))

                                   _ (when (>= (max wait1 wait2) 0)
                                       (Thread/sleep (+ 1 (max wait1 wait2))))

                                   updated-parameters (doto signature-parameters (.setSignatureLevel SignatureLevel/PAdES_BASELINE_LT))

                                   extended-document (-> service (.extendDocument signed-document updated-parameters))

                                   )
          ]
      ;;TODO: Hide
      ;;extended-document
      signed-document
      )
    )

(defn get-seocnds-until-next-ocsp-update []

  {:ocsp-next-update 10}
  )

(defn augment-pdf-to-pades-lt-level []

  )


