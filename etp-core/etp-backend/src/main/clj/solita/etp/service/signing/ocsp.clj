(ns solita.etp.service.signing.ocsp
  "Online Certificate Status Protocol"
  (:require [clj-http.client :as http]
            [solita.etp.exception :as exception]
            [clojure.tools.logging :as log])
  (:import
    (java.io ByteArrayInputStream File FileInputStream FileOutputStream IOException InputStream)
    (java.nio.charset StandardCharsets)
    (java.util Collection Comparator HashSet Optional)
    (java.security GeneralSecurityException InvalidKeyException MessageDigest NoSuchAlgorithmException Provider PublicKey Security SignatureException)
    (java.security.cert CertificateException X509Certificate CertificateFactory)
    (org.apache.pdfbox.cos COSBase COSName)
    (org.apache.pdfbox.pdmodel PDDocument)
    (org.apache.pdfbox.pdmodel.encryption SecurityProvider)
    (org.apache.pdfbox.pdmodel.interactive.digitalsignature PDSignature)
    (org.apache.pdfbox.util Hex)
    (org.bouncycastle.asn1.x509 AccessDescription AuthorityInformationAccess Extension Extensions)
    (org.bouncycastle.cert.jcajce JcaX509CertificateConverter JcaX509CertificateHolder JcaX509ExtensionUtils$SHA1DigestCalculator)
    (org.bouncycastle.cert.ocsp CertificateID OCSPReq OCSPReqBuilder OCSPResp)
    (org.bouncycastle.cms CMSException CMSSignedData SignerInformation)
    (org.bouncycastle.operator DefaultDigestAlgorithmIdentifierFinder)
    (org.bouncycastle.operator.jcajce JcaDigestCalculatorProviderBuilder)
    (org.bouncycastle.util Store)))

(def max-certificate-chain-depth 5)

(defn get-ocsp-uri
  "Gets the OCSP URI from the certificate's Authority Information Access"
  [^X509Certificate certificate]
  (some-> certificate
          JcaX509CertificateHolder.
          (.getExtension Extension/authorityInfoAccess)
          .getParsedValue
          AuthorityInformationAccess/getInstance
          .getAccessDescriptions
          (#(some (fn [access-desc]
                    (when (some-> access-desc .getAccessMethod (= AccessDescription/id_ad_ocsp))
                      (some-> access-desc .getAccessLocation .getName str)))
                  %))))

(defn create-ocsp-request
  "Creates an OCSP request for a given certificate and issuer certificate."
  [cert issuer-cert]
  (let [cert-id (CertificateID.
                  (some-> (JcaDigestCalculatorProviderBuilder.)
                          .build
                          (.get (.find (DefaultDigestAlgorithmIdentifierFinder.) "SHA-1")))
                  (JcaX509CertificateHolder. issuer-cert)
                  (.getSerialNumber cert))]
    ;;TODO: Nonce and exts
    (some-> (OCSPReqBuilder.)
            ;;(.setRequestExtensions (Extensions. (Extension[]. )))
            (.addRequest cert-id)
            .build)))

;;TODO: Should the issuing certificate be also retrieved from the certificate
(defn make-ocsp-request [^X509Certificate certificate ^X509Certificate issuing-cert]
  (let [ocspUrl (get-ocsp-uri certificate)
        ^OCSPReq request (create-ocsp-request certificate issuing-cert)
        response (http/post ocspUrl {:content-type     :ocsp-request
                                     :as               :stream
                                     :accept           :ocsp-response
                                     :throw-exceptions false
                                     :body             (.getEncoded request)})
        ;; TODO: Is the body enough, how to make the whole response into a stream?
        ^InputStream body (:body response)]
    (OCSPResp. body)))

(defn get-cert-information [^X509Certificate cert ^X509Certificate issuer-cert]
  {:certificate            cert
   :signatureHash          nil
   :self-signed?           nil
   :ocsp-url               (get-ocsp-uri cert)
   :issuer-url             nil
   :crl-url                nil
   :issuer-certificate     nil
   :cert-chain             nil
   :tsa-certs              nil
   :alternative-cert-chain nil})

(defn get-last-relevant-signature [^PDDocument document]
  (let [comparatorByOffset (Comparator/comparing (fn [^PDSignature sig] (-> sig .getByteRange (aget 1))))
        ^Optional optLastSignature (some-> document .getSignatureDictionaries .stream (.max comparatorByOffset))]
    (when (.isPresent optLastSignature)
      (let [^PDSignature lastSignature (.get optLastSignature)
            ^COSBase type (some-> lastSignature .getCOSObject (.getItem COSName/TYPE))]
        (when (or (= type nil) (.equals COSName/SIG type) (.equals COSName/DOC_TIME_STAMP type))
          lastSignature)))))

;; TODO: Do we need this?
(defn get-md-permissions [doc]
  ;; Here we could check the permissions
  ;; add changes. But we can add changes regardless
  ;; as additions to DSS are allowed. We don't even
  ;; mark the document to forbid changes, I think.
  true)

(defn get-sha1-hash [content]
  (try
    (some-> "SHA-1"
            MessageDigest/getInstance
            (.digest content)
            Hex/getString)
    (catch NoSuchAlgorithmException e
      (log/error "No SHA-1 Algorithm found" e))))

(defn get-cert-from-holder [^X509Certificate certificate-holder]
  (try
    (let [;; TODO: Does it matter that the converter is created here every time?
          cert-converter (JcaX509CertificateConverter.)]
      (-> cert-converter (.getCertificate certificate-holder)))
    (catch CertificateException e
      (log/error "Certificate Exception getting Certificate from certHolder." e)
      (exception/throw-ex-info! :certificate-processing-exception (.getMessage e)))))

(defn add-all-certs [^Collection all-certs ^HashSet certificate-set]
  (doseq [certificate-holder all-certs]
    (try
      (-> certificate-holder
          get-cert-from-holder
          (#(certificate-set (.add %))))
      (catch Exception e
        (if (= (-> e ex-data :type) :certificate-processing-exception)
          (log/warn "Certificate Exception getting Certificate from certHolder." e)
          (throw e))))))


(defn traverse-chain [certificate {:keys [^X509Certificate certificate] :as cert-info} max-certificate-chain-depth]
  (let [^bytes authority-extension-value (-> certificate (.getExtensionValue (-> Extension/authorityInfoAccess .getId)))]
    (when authority-extension-value
      #_(get-authority-info-extension-value))))

(defn process-signer-store [^CMSSignedData signed-data {:keys [certificate] :as cert-info}]
  (let [^Collection signers (-> signed-data .getSignerInfos .getSigners)
        ^SignerInformation signer-information (-> signers .iterator .next)
        ^Store certificates-store (-> signed-data .getCertificates)
        ^Collection matches (-> certificates-store (.getMatches (-> signer-information .getSID)))
        ^X509Certificate certificate (-> matches .iterator .next get-cert-from-holder)
        certificate-set (-> (HashSet.) (.add certificate))
        ^Collection all-certs (-> certificates-store (.getMatches nil))]
    (add-all-certs all-certs certificate-set)
    (traverse-chain certificate cert-info max-certificate-chain-depth)
    ))

(defn get-cert-info [^bytes signature-content]
  (let [root-cert-info {:signature-hash (get-sha1-hash signature-content)}]
    (try
      (let [^CMSSignedData signed-data (CMSSignedData. signature-content)
            ^SignerInformation signer-information (process-signer-store signed-data root-cert-info)
            ]
        ()
        )

      (catch CMSException e
        (log/error "Error occurred getting Certificate Information from Signature" e)
        (exception/throw-ex-info! [:certificate-processing-exception :message (.getMessage e)])))))

(defn get-last-cert-info [^PDSignature signature filename]
  (with-open [document-input (FileInputStream. filename)]
    (let [signature-content (.getContents signature)]
      (get-cert-info signature-content))))


(defn do-validation [filename output {:keys [document]}]
  (let [^PDSignature signature (get-last-relevant-signature document)
        cert-info (get-last-cert-info signature filename)

        ]
    (.saveIncremental document output)

    ))

(defn validate-signature [^File in-file ^File out-file]
  ;; TODO: Handle file errors? Will change to S3 eventually anyway...
  (with-open [^PDDocument document (PDDocument/load in-file)
              ^FileOutputStream fos (FileOutputStream. out-file)]
    ;; TODO: Handle access-permissions?
    (do-validation (.getAbsolutePath in-file) fos {:document document})))

(defn add-ocsp-information [^String in-filepath ^String out-filepath]
  (with-open [in-file (File. in-filepath)
              ;; We probably want just to store this in memory and send to S3.
              ;; But for now just implement the example.
              out-file (File. out-filepath)]

    ;; TODO: Do we need this? This is for some "exotic" algorithms.
    ;; And how does this even work? Is this some static class? Singleton?
    (Security/addProvider (SecurityProvider/getProvider))

    (validate-signature in-file out-file)))


(defn- self-signed? [^X509Certificate certificate]
  (try
    (let [^PublicKey key (-> certificate .getPublicKey)]
      (-> certificate (.verify key (-> (SecurityProvider/getProvider) .getName))))
    ;; If nothing is thrown the certificate is self-signed.
    true
    (catch Exception e
      (if (or (instance? SignatureException e)
              (instance? InvalidKeyException e)
              (instance? IOException e))
        false
        (throw e)))))

(defn- find-issuer [certificate all-certificates]
  (if (self-signed? certificate)
    {:self-signed? true}
    (some
      (fn [potential-issuer]
        (try
          (-> certificate (.verify (-> potential-issuer .getPublicKey) (-> (SecurityProvider/getProvider) .getName)))
          ;; If nothing was thrown we have found the issuer
          {:issuer potential-issuer}

          ;; This means that this is not the issuer.
          (catch GeneralSecurityException e)))
      all-certificates)))

(defn get-certificates [^PDDocument document]
  (let [last-relevant-signature (get-last-relevant-signature document)
        ^bytes signature-content (.getContents last-relevant-signature)
        signed-data (CMSSignedData. signature-content)

        signers (-> signed-data .getSignerInfos .getSigners)
        ^SignerInformation signer-information (-> signers .iterator .next)
        certificate-store (-> signed-data .getCertificates)
        matches (-> certificate-store (.getMatches (-> signer-information .getSID)))

        cert-converter (JcaX509CertificateConverter.)

        certificate-holder (-> matches .iterator .next)
        leaf-cert (-> cert-converter (.getCertificate certificate-holder))

        all-cert-holders (-> certificate-store (.getMatches nil))
        all-certs (map #(-> cert-converter (.getCertificate %)) all-cert-holders)

        intermediate-cert (:issuer (find-issuer leaf-cert all-certs))
        root-cert (:issuer (find-issuer intermediate-cert all-certs))]
    {:root-certificate         root-cert
     :intermediate-certificate intermediate-cert
     :leaf-certificate         leaf-cert}))

(def pdf-file (PDDocument/load (File. "src/test/resources/energiatodistukset/signed-with-ocsp-information.pdf")))
(get-certificates pdf-file)
