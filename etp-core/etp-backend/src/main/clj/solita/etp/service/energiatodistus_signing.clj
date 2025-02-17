(ns solita.etp.service.energiatodistus-signing
  "Contains functionality to sign specifically an energiatodistus."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [solita.common.certificates :as certificates]
    [solita.common.time :as time]
    [solita.etp.common.audit-log :as audit-log]
    [solita.etp.config :as config]
    [solita.etp.exception :as exception]
    [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
    [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.sign :as sign-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign])
  (:import
    (java.text Normalizer Normalizer$Form)
    (java.time Instant ZoneId)
    (java.time.format DateTimeFormatter)
    (clojure.lang ExceptionInfo)
    (java.awt Color Font)
    (java.awt.image BufferedImage)
    (java.io File InputStream)
    (java.nio.charset StandardCharsets)
    (java.time Instant ZoneId)
    (java.time.format DateTimeFormatter)
    (java.util Base64 Base64$Decoder Date)
    (javax.imageio ImageIO)))

(def timezone (ZoneId/of "Europe/Helsinki"))
(def time-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
                               timezone))

(defn signature-as-png [path ^String laatija-fullname]
  (let [now (time/now)
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

(defn do-when-signing [{:keys [tila-id]} f]
  (case (energiatodistus-tila/tila-key tila-id)
    :in-signing (f)
    :draft :not-in-signing
    :deleted :not-in-signing
    :already-signed))

(def deletable-signature-process-object-tag
  {:Key "TemporarySignatureProcessFile" :Value "True"})

;; We store the stateful signing paramters into a path that can easily touched by a lifecycle rule so that the rule
;; does not touch anything else.
(defn stateful-signature-parameters-file-key [energiatodistus-id language]
  (when energiatodistus-id (format "energiatodistus-signing/stateful-signature-parameters-%s-%s" energiatodistus-id language)))

(defn create-stateful-signature-parameters [aws-s3-client energiatodistus-id language ^InputStream stateful-parameters-object]
  (let [stateful-parameters-key (stateful-signature-parameters-file-key energiatodistus-id language)]
    (file-service/upsert-file-from-input-stream aws-s3-client
                                                stateful-parameters-key
                                                stateful-parameters-object)
    (file-service/put-file-tag aws-s3-client
                               stateful-parameters-key
                               deletable-signature-process-object-tag)))

(defn find-energiatodistus-digest-new
  "This is the function that does first real processing of the signature.

  Generate the pdf.
  Upload to S3.
  Return the data that needs to be signed in base64"
  [db aws-s3-client id language laatija-allekirjoitus-id]
  (when-let [{:keys [laatija-fullname versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
    (do-when-signing
      complete-energiatodistus
      #(let [draft? false
             ^String pdf-path (energiatodistus-pdf-service/generate-pdf-as-file complete-energiatodistus language draft? laatija-allekirjoitus-id)
             signature-png-path (str/replace pdf-path #".pdf" "-signature.png")
             key (energiatodistus-service/file-key id language)
             energiatodistus-pdf (File. pdf-path)
             ;; TODO: How to document this?
             ;;_ (io/copy energiatodistus-pdf (io/file "./src/test/resources/energiatodistukset/signing-process/generate-pdf-as-file.pdf"))
             _ (signature-as-png signature-png-path laatija-fullname)
             signature-png (File. signature-png-path)
             ;; TODO: Check 2013 version positioning or is it even relevant?
             origin-y (case versio 2013 648 2018 666)
             digest-and-stuff (pdf-sign/unsigned-document->digest-and-params energiatodistus-pdf
                                                                             {:signature-png signature-png
                                                                             :page          1
                                                                             :origin-x      75
                                                                             :origin-y      origin-y
                                                                             :zoom          133})
             ]
         (file-service/upsert-file-from-file aws-s3-client
                                             key
                                             energiatodistus-pdf)
         (create-stateful-signature-parameters aws-s3-client
                                               id
                                               language
                                               (:stateful-parameters digest-and-stuff))
         (io/delete-file pdf-path)
         (io/delete-file signature-png-path)
         (select-keys digest-and-stuff [:digest])))))

(defn find-energiatodistus-digest
  "Generate the pdf.
  Add space for signature.
  Upload to S3.
  Return the data that needs to be signed in base64"
  [db aws-s3-client id language laatija-allekirjoitus-id]
  (find-energiatodistus-digest-new db aws-s3-client id language laatija-allekirjoitus-id))

(defn sign-energiatodistus-pdf-new
  "This is the function that receives the signature and continues the signing process."
  [db aws-s3-client id language signature cert-chain]
  (when-let [{:keys [laatija-fullname versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
    (do-when-signing
      complete-energiatodistus
      #(do
         (let [key (energiatodistus-service/file-key id language)
               unsigned-pdf-is (file-service/find-file aws-s3-client key)
               sig-params-is (file-service/find-file aws-s3-client (stateful-signature-parameters-file-key id language))
               sig-params-is (file-service/find-file aws-s3-client (stateful-signature-parameters-file-key id language))
               filename (str key ".pdf")
               ^Base64$Decoder decoder (Base64/getDecoder)
               signed-pdf-t-level (pdf-sign/unsigned-document-info-and-signature->t-level-signed-document unsigned-pdf-is sig-params-is (.decode decoder signature) cert-chain)
               signed-pdf-lt-level (pdf-sign/t-level->lt-level signed-pdf-t-level)]
           (file-service/upsert-file-from-input-stream aws-s3-client
                                                       key
                                                       signed-pdf-lt-level)
           filename)))))

(defn comparable-name [s]
  (-> s
      (Normalizer/normalize Normalizer$Form/NFD)
      str/lower-case
      (str/replace #"[^a-z]" "")))

(defn validate-surname! [last-name certificate]
  (let [surname (-> certificate
                    certificates/subject
                    :surname)]
    (when-not (= (comparable-name last-name) (comparable-name surname))
      (log/warn "Last name from certificate did not match with whoami info when signing energiatodistus PDF.")
      (exception/throw-ex-info!
        {:type    :name-does-not-match
         :message (format "Last names did not match. Whoami has '%s' and certificate has '%s'"
                          last-name
                          surname)}))))

(defn validate-not-after! [^Date now certificate]
  (let [not-after (-> certificate certificates/not-after)]
    (when (.after now not-after)
      (log/warn "Signing certificate validity ended at" not-after)
      (exception/throw-ex-info!
        {:type    :expired-signing-certificate
         :message (format "ET Signing certificate expired at %s, would have needed to be valid at least until %s"
                          not-after
                          now)}))))

(defn validate-certificate!
  "Validates that the certificate is not expired and optionally that the surname matches the name in the certificate.

  When using system signing the surname does not match the name in the certificate as it's issued for the whole
  system and not a specific person."
  ([surname now certificate-str]
   (validate-certificate! surname now certificate-str true))
  ([surname now certificate-str validate-surname?]
   (let [certificate (certificates/pem-str->certificate certificate-str)]
     (when validate-surname?
       (validate-surname! surname certificate))
     (validate-not-after! (-> now Instant/from Date/from) certificate))))

(defn sign-energiatodistus-pdf
  [db aws-s3-client whoami now id language signature-and-chain]
  (sign-energiatodistus-pdf-new db aws-s3-client id language (:signature signature-and-chain) (:chain signature-and-chain)))

(defn cert-pem->one-liner-without-headers [cert-pem]
  "Given a certificate in PEM format `cert-pem` removes
  headers and linebreaks from it."
  (-> cert-pem
      (str/replace #"-----BEGIN CERTIFICATE-----" "")
      (str/replace #"-----END CERTIFICATE-----" "")
      (str/replace #"\n" "")))

(def cert-chain-three-long-leaf-first
  (let [leaf config/system-signature-certificate-leaf
        intermediate config/system-signature-certificate-intermediate
        root config/system-signature-certificate-root]
    [leaf intermediate root]))

(defn- audit-log-message [laatija-allekirjoitus-id energiatodistus-id message]
  (str "Sign with system (laatija-allekirjoitus-id: " laatija-allekirjoitus-id ") (energiatodistus-id: " energiatodistus-id "): " message))

(defn- is-sign-with-system-error? [result]
  (contains? #{:already-signed :already-in-signing :not-in-signing nil} result))

(defn- do-sign-with-system [f error-msg]
  (let [result (f)]
    (when (is-sign-with-system-error? result)
      (audit-log/error error-msg)
      (exception/throw-ex-info! {:message "Signing with system failed." :type :sign-with-system-error :result result}))
    result))

(defn- sign-with-system-start
  [{:keys [db whoami id laatija-allekirjoitus-id]}]
  (audit-log/info (audit-log-message laatija-allekirjoitus-id id "Starting"))
  (do-sign-with-system
    #(energiatodistus-service/start-energiatodistus-signing! db whoami id)
    (audit-log-message laatija-allekirjoitus-id id "Starting failed!")))

(defn- sign-with-system-digest
  [{:keys [db laatija-allekirjoitus-id id aws-s3-client]} language]
  (audit-log/info (audit-log-message laatija-allekirjoitus-id id "Getting the digest"))
  (do-sign-with-system
    #(find-energiatodistus-digest db aws-s3-client id language laatija-allekirjoitus-id)
    (audit-log-message laatija-allekirjoitus-id id "Getting the digest failed!")))

(defn- sign-with-system-sign
  [{:keys [db whoami now id laatija-allekirjoitus-id aws-s3-client aws-kms-client]} language digest-response]
  (let [data-to-sign (-> digest-response
                         :digest
                         (.getBytes StandardCharsets/UTF_8)
                         (#(.decode (Base64/getDecoder) %)))
        chain cert-chain-three-long-leaf-first
        system-signature-cms-info {:cert-chain        chain
                                   :signing-cert      config/system-signature-certificate-leaf
                                   :digest->signature #(sign-service/sign aws-kms-client %)}
        ^bytes signature (.encode (Base64/getEncoder)
                           (pdf-sign/digest->cms-signature-with-system
                             data-to-sign
                             system-signature-cms-info))
        _ (println "SIG: " (-> ^bytes signature (String.)))
        chain-like-from-card-reader (mapv cert-pem->one-liner-without-headers chain)
        _ (mapv println chain-like-from-card-reader)
        signature-and-chain {:chain chain-like-from-card-reader :signature (String. signature)}]
    (audit-log/info (audit-log-message laatija-allekirjoitus-id id "Signing via KMS"))
    (do-sign-with-system
      #(sign-energiatodistus-pdf db aws-s3-client whoami now id language signature-and-chain)
      (audit-log-message laatija-allekirjoitus-id id "Signing via KMS failed!"))))

(defn- sign-with-system-end
  [{:keys [db whoami laatija-allekirjoitus-id id aws-s3-client]}]
  (audit-log/info (audit-log-message laatija-allekirjoitus-id id "End signing"))
  (do-sign-with-system
    #(energiatodistus-service/end-energiatodistus-signing! db aws-s3-client whoami id)
    (audit-log-message laatija-allekirjoitus-id id "End signing failed!")))

(defn- sign-single-pdf-with-system [params language]
  (->> (sign-with-system-digest params language)
       (#(do (println %) %))
       (sign-with-system-sign params language)))

(defn sign-with-system
  "Does the whole process of signing with the system."
  [{:keys [db id] :as params}]
  (let [language-id (-> (complete-energiatodistus-service/find-complete-energiatodistus db id) :perustiedot :kieli)]
    (try
      (condp = language-id
        energiatodistus-service/finnish-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system params "fi")
          (sign-with-system-end params))

        energiatodistus-service/swedish-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system params "sv")
          (sign-with-system-end params))

        energiatodistus-service/multilingual-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system params "fi")
          (sign-single-pdf-with-system params "sv")
          (sign-with-system-end params))

        (exception/throw-ex-info! {:message (format "Invalid language-id %s in energiatodistus %s" language-id id)}))
      (catch ExceptionInfo e
        (if (= (-> e ex-data :type) :sign-with-system-error)
          (-> e ex-data :result)
          (throw e))))))


