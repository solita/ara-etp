(ns solita.etp.service.signing.pdf-sign
  "Encapsulates the usage of ESIG/DSS library"
  (:require
    [clojure.java.io :as io]
    [solita.common.time :as time]
    [solita.etp.config :as config])
  (:import (eu.europa.esig.dss.alert ExceptionOnStatusAlert)
           (eu.europa.esig.dss.cades.signature CMSSignedDocument)
           (eu.europa.esig.dss.enumerations SignatureLevel DigestAlgorithm SignatureAlgorithm)
           (eu.europa.esig.dss.model DSSMessageDigest FileDocument InMemoryDocument SignatureValue)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters SignatureFieldParameters SignatureImageParameters)
           (eu.europa.esig.dss.pades.signature ExternalCMSService PAdESService PAdESWithExternalCMSService)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.service.tsp OnlineTSPSource)
           (eu.europa.esig.dss.spi DSSUtils)
           (eu.europa.esig.dss.spi.validation CommonCertificateVerifier)
           (eu.europa.esig.dss.spi.x509.tsp KeyEntityTSPSource)
           (java.io ByteArrayInputStream File FileOutputStream InputStream ObjectInputStream ObjectOutputStream)
           (java.security KeyPair KeyPairGenerator PrivateKey SecureRandom Security)
           (java.security.cert X509Certificate)
           (java.time Duration Instant)
           (java.util ArrayList Collection Date List)
           (java.util ArrayList Date List)
           (java.util Date)
           (org.bouncycastle.asn1.x500 X500Name)
           (org.bouncycastle.asn1.x509 ExtendedKeyUsage Extension KeyPurposeId)
           (org.bouncycastle.cert X509v3CertificateBuilder)
           (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509v3CertificateBuilder)
           (org.bouncycastle.cms CMSSignedData)
           (org.apache.axis.utils ByteArrayOutputStream)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.operator ContentSigner)
           (org.bouncycastle.operator.jcajce JcaContentSignerBuilder)))

(def tsp-key-and-cert
  (let [_ (Security/addProvider (BouncyCastleProvider.))
        ^KeyPairGenerator keyPairGenerator (doto (KeyPairGenerator/getInstance "RSA")
                                             (.initialize 2048))
        ^KeyPair keyPair (-> keyPairGenerator .generateKeyPair)

        subjectDN "CN=Self-Signed, O=Example, C=FI"
        issuerDN subjectDN
        serialNumber (BigInteger. 64 (SecureRandom.))
        ^Date notBefore (Date/from (-> (time/now) (.minus (Duration/ofDays 1))))
        ^Date notAfter (Date/from (-> (time/now) (.plus (Duration/ofDays 1))))

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

;; TODO: We could just usa a TSA with openssl instead
(defn default-tsp []
  (doto (KeyEntityTSPSource. ^PrivateKey (:private-key tsp-key-and-cert)
                             ^X509Certificate (:certificate tsp-key-and-cert)
                             ^List (doto (ArrayList.) (.add (:certificate tsp-key-and-cert))))
    (.setTsaPolicy "1.2.3.4")))

(defn- ^:dynamic get-tsp-source []
  (if-let [tsa-url config/tsa-endpoint-url]
    (OnlineTSPSource. tsa-url)
    (default-tsp)))

(defn- ^:dynamic get-ocsp-source []
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

(defn ^:dynamic get-signature-parameters [{:keys [^File signature-png page origin-x origin-y zoom]}]
  (let [signature-png (InMemoryDocument. (io/input-stream signature-png))

        ^SignatureFieldParameters sig-field-params (doto (SignatureFieldParameters.)
                                                     (.setPage page)
                                                     (.setOriginX origin-x)
                                                     (.setOriginY origin-y))

        ^SignatureImageParameters sig-img (doto (SignatureImageParameters.)
                                            (.setFieldParameters sig-field-params)
                                            (.setImage signature-png)
                                            (.setZoom zoom))

        signature-parameters (doto (PAdESSignatureParameters.)
                               (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
                               (.setImageParameters sig-img)
                               (.setDigestAlgorithm DigestAlgorithm/SHA256))]
    signature-parameters))

(defn unsigned-document->digest-and-params
  "Given an unsigned pdf and signature options produces the digest and the stateful parameters.
  The digest should then be used to create a signature using an external cms service.
  Example of a cms service is a FINEID card reader producing \"cms-pades\" signature.

  Arguments:
  - `unsigned-pdf`: A `java.io.File` instance representing the file to be signed.
  - `signature-options`: A map with the following required key-value pairs:
      - `:signature-png` - A file containing the image used for signature.
      - `:page` - An integer representing what page to put the signature on.
      - `:origin-x` - Where to position the signature-png respective to the x-axis.
      - `:origin-y` - Where to position the signature-png respective to the y-axis.
      - `:zoom` - Used for scaling the signature-png

  Returns:
  A map containing:
  - `:digest` - The digest that should be used to retrieve the pades-cms signature.
  - `:stateful-parameters` - Parameters needed to continue the process once the signature is retrieved"
  [^File unsigned-pdf signature-options]
  (let [service (PAdESWithExternalCMSService.)
        signature-parameters (get-signature-parameters signature-options)
        ;;_
        #_(with-open [fos (FileOutputStream. "src/test/resources/energiatodistukset/signing-process/stateful-parameters")
                    oos (ObjectOutputStream. fos)]
          (.writeObject oos signature-parameters))
        ^DSSMessageDigest message-digest (-> service (.getMessageDigest (FileDocument. unsigned-pdf) signature-parameters))]
    {:digest              (.getBase64Value message-digest)
     ;; At least the signature-png needs to be saved for use after getting the signature.
     ;; Parameters seem to also have some other state and differ somehow if recreated so
     ;; might as well save the whole SignatureParameters object.
     :stateful-parameters (object->input-stream signature-parameters)}))

(defn unsigned-document-info-and-signature->t-level-signed-document
  [^InputStream pdf-file stateful-parameters ^bytes cms-signature cert-chain]
  (let [^PAdESSignatureParameters signature-parameters (input-stream->object stateful-parameters)
        unsigned-pdf (InMemoryDocument. pdf-file)
        tsp-source (get-tsp-source)
        service-external-cms (doto (PAdESWithExternalCMSService.))
        signed-pdf-b-level (-> service-external-cms (.signDocument unsigned-pdf
                                                                   signature-parameters
                                                                   (CMSSignedDocument. (CMSSignedData. cms-signature))))
        service (doto (PAdESService. (CommonCertificateVerifier.))
                  (.setTspSource tsp-source))
        extend-parameters (doto (PAdESSignatureParameters.)
                            (.setSignatureLevel SignatureLevel/PAdES_BASELINE_T))
        signed-pdf-t-level (-> service (.extendDocument signed-pdf-b-level extend-parameters))]
    (.openStream signed-pdf-t-level)))

(defn t-level->lt-level [^InputStream signed-t-level-pdf]
  (let [signed-pdf (InMemoryDocument. signed-t-level-pdf)
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
    (.openStream lt-level-document)))

;; External cms with system (same what the card reader does)
(defn pem->CertificateToken [cert-pem]
  ^CertificateToken (-> cert-pem
                        (DSSUtils/convertToDER)
                        (DSSUtils/loadCertificate)))

(defn get-parameters-for-external-cms-service
  "This should produce the same level of a signature as the card reader."
  [{:keys [signing-cert cert-chain] :as external-cms-info}]
  (let [^List cert-chain (ArrayList. ^Collection (->> cert-chain
                                                      (mapv pem->CertificateToken)))]
    (doto (PAdESSignatureParameters.)
      (#(doto (.bLevel %)
          (.setSigningDate (-> (^Instant time/now) Date/from))))
      (.setSigningCertificate (pem->CertificateToken signing-cert))
      (.setDigestAlgorithm DigestAlgorithm/SHA256)
      (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
      (.setCertificateChain cert-chain))))

(defn digest->cms-signature-with-system [^bytes digest
                                               {:keys [cert-chain
                                                       signing-cert
                                                       digest->signature] :as external-cms-info}]
  (let [certificate-verifier (CommonCertificateVerifier.)
        signature-parameters (get-parameters-for-external-cms-service external-cms-info)
        ^DSSMessageDigest dss-digest (DSSMessageDigest. DigestAlgorithm/SHA256 digest)
        ^ExternalCMSService system-signature-cms-service (ExternalCMSService. certificate-verifier)
        data-to-sign (.getDataToSign system-signature-cms-service dss-digest signature-parameters)

        ^SignatureValue signature-value (SignatureValue.
                                          SignatureAlgorithm/RSA_SHA256
                                          (.readAllBytes
                                            (digest->signature
                                              (-> data-to-sign .getBytes))))
        cms-signature (-> system-signature-cms-service
                          (.signMessageDigest dss-digest signature-parameters signature-value))]
    (.getBytes cms-signature)))

