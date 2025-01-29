(ns solita.etp.service.signing.pdf-sign
  (:require
    [clojure.java.io :as io]
    [solita.etp.service.sign :as sign-service]
    [solita.common.time :as time]
    [solita.etp.config :as config])
  (:import (eu.europa.esig.dss.alert ExceptionOnStatusAlert)
           (eu.europa.esig.dss.enumerations SignatureLevel DigestAlgorithm SignatureAlgorithm)
           (eu.europa.esig.dss.model DSSMessageDigest FileDocument InMemoryDocument SignatureValue)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters SignatureFieldParameters SignatureImageParameters)
           (eu.europa.esig.dss.pades.signature ExternalCMSService PAdESService PAdESWithExternalCMSService)
           (eu.europa.esig.dss.pades.validation PDFDocumentValidator)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.service.tsp OnlineTSPSource)
           (eu.europa.esig.dss.spi DSSUtils)
           (eu.europa.esig.dss.spi.signature AdvancedSignature)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509.revocation.ocsp OCSPToken)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.awt Color Font)
           (java.awt.image BufferedImage)
           (java.io ByteArrayInputStream File InputStream ObjectInputStream ObjectOutputStream)
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

(def timezone (ZoneId/of "Europe/Helsinki"))
(def time-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
                               timezone))

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

;; TODO: Make dynamic?
(defn- ^:dynamic get-tsp-source []
  (let [tsa-url (config/tsa-endpoint-url)]
      (OnlineTSPSource. tsa-url))
  #_(doto (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                             ^X509Certificate (:certificate tsp-key-and-cert)
                             ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
    (.setTsaPolicy "1.2.3.4")))

;; TODO: Make dynamic?
(defn- get-ocsp-source []
  (doto (OnlineOCSPSource.)
    (.setAlertOnInvalidUpdateTime (ExceptionOnStatusAlert.))))

(defn object->input-stream [^Object object]
  (let [baos (ByteArrayOutputStream.)
        _ (doto (ObjectOutputStream. baos)
            (.writeObject object))
        bais (ByteArrayInputStream. (.toByteArray baos))]
    bais))

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
     ;; At least the signature-png needs to be saved for use after getting the signature.
     ;; Parameters seem to also have some other state and differ somehow if recreated so
     ;; might as well save the whole SignatureParameters object.
     :stateful-parameters (object->input-stream signature-parameters)}))

(defn sign-with-external-cms-service-signature
  "Given an unsigned pdf `pdf-file` as InputStream and `stateful-parameters`"
  [^InputStream pdf-file stateful-parameters ^bytes cms-signature]
  (let [^PAdESSignatureParameters signature-parameters (input-stream->object stateful-parameters)
        unsigned-pdf (InMemoryDocument. pdf-file)
        tsp-source (get-tsp-source)
        service-external-cms (doto (PAdESWithExternalCMSService.))
        signed-pdf-b-level (-> service-external-cms (.signDocument unsigned-pdf signature-parameters cms-signature))


        service (doto (PAdESService. (CommonCertificateVerifier.))
                               (.setTspSource tsp-source))
        extend-parameters (doto (PAdESSignatureParameters.)
                            (.setSignatureLevel SignatureLevel/PAdES_BASELINE_T)
                            )
        signed-pdf-t-level (-> service (.extendDocument signed-pdf-b-level extend-parameters))
        ]
    (.openStream signed-pdf-t-level)))

;; TODO: Or how to do this? It is hard to determine who is whose issuer.
(defn cert-chain->slowest-next-update [^List certs-list ^CommonCertificateVerifier cert-verifier]
  (let [certs (into [] certs-list)
        next-updates (->> certs-list
                         (mapv #(.getOcspSource cert-verifier)))

        ^OCSPToken ocsp-resp (-> cert-verifier .getOcspSource (.getRevocationToken (:leaf-cert certs) (:int-cert certs)))
        _ (println "AAA::" (.toString ocsp-resp))

        wait1 (- (.getEpochSecond (.toInstant ^Date (.getNextUpdate ocsp-resp)))
                 (.getEpochSecond ^Instant (time/now)))]))

(defn t-level->lt-level [^InputStream signed-b-level-pdf]
  (let [signed-pdf (InMemoryDocument. signed-b-level-pdf)
        cert-verifier (CommonCertificateVerifier.)
        service (PAdESService. cert-verifier)
        validator (PDFDocumentValidator/fromDocument signed-pdf)
        ^AdvancedSignature signature (first (-> validator .getSignatures))
        a (-> signature (.getCompleteOCSPSource) )
        tsp-source (get-tsp-source)
        ocsp-source (get-ocsp-source)

        parameters (doto (PAdESSignatureParameters.)
                     (.setSignatureLevel SignatureLevel/PAdES_BASELINE_LT))

        service (doto (PAdESService. (doto (CommonCertificateVerifier.)
                                       (.setOcspSource ocsp-source)
                                       ;; TODO: Trust KMS root and DVV's certs?
                                       ;;       Like this this is the same behaviour as before.
                                       (.setCheckRevocationForUntrustedChains true)))
                  (.setTspSource tsp-source))
        lt-level-document (-> service (.extendDocument signed-pdf parameters))]
    lt-level-document))