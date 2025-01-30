(ns solita.etp.service.energiatodistus-signing
  "Contains functionality to sign specifically an energiatodistus."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [puumerkki.pdf :as puumerkki]
    [solita.common.certificates :as certificates]
    [solita.etp.common.audit-log :as audit-log]
    [solita.etp.config :as config]
    [solita.etp.exception :as exception]
    [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
    [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.sign :as sign-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign]
    [clojure.java.io :as io])
  (:import
    (java.text Normalizer Normalizer$Form)
    (java.time Instant ZoneId)
    (java.time.format DateTimeFormatter)
    (clojure.lang ExceptionInfo)
    (java.awt Color Font)
    (java.awt Color Font)
    (java.awt.image BufferedImage)
    (java.awt.image BufferedImage)
    (java.io File InputStream)
    (java.io File)
    (java.nio.charset StandardCharsets)
    (java.time Instant ZoneId)
    (java.time.format DateTimeFormatter)
    (java.util Base64 Date)
    (javax.imageio ImageIO)
    (javax.imageio ImageIO)))

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

(defn do-when-signing [{:keys [tila-id]} f]
  (case (energiatodistus-tila/tila-key tila-id)
    :in-signing (f)
    :draft :not-in-signing
    :deleted :not-in-signing
    :already-signed))

(def test-sig-params-key "test-sig-params-key")

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
             _ (signature-as-png signature-png-path laatija-fullname)
             signature-png (File. signature-png-path)
             ;; TODO: Check 2013 version positioning or is it even relevant?
             origin-y (case versio 2013 648 2018 666)
             digest-and-stuff (pdf-sign/get-digest-for-external-cms-service energiatodistus-pdf {:signature-png signature-png :page 1 :origin-x 75 :origin-y origin-y})]
         (file-service/upsert-file-from-file aws-s3-client
                                             key
                                             energiatodistus-pdf)
         (file-service/upsert-file-from-input-stream aws-s3-client
                                                     test-sig-params-key
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
  [db aws-s3-client id language laatija-allekirjoitus-id certs signature]
  (when-let [{:keys [laatija-fullname versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
    (do-when-signing
      complete-energiatodistus
      #(do
         (let [key (energiatodistus-service/file-key id language)
               unsigned-pdf-is (file-service/find-file aws-s3-client key)
               sig-params-is (file-service/find-file aws-s3-client test-sig-params-key)
               filename (str key ".pdf")
               signed-pdf-t-level (pdf-sign/sign-with-external-cms-service-signature unsigned-pdf-is sig-params-is signature)
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

(defn write-signature! [id language pdf pkcs7]
  (try
    (puumerkki/write-signature! pdf pkcs7)
    (catch ArrayIndexOutOfBoundsException _
      (exception/throw-ex-info!
        :signed-pdf-exists
        (str "Signed PDF already exists for energiatodistus "
             id "/" language ". Get digest to sign again.")))))

(defn sign-energiatodistus-pdf
  ([db aws-s3-client whoami now id language signature-and-chain]
   (sign-energiatodistus-pdf db aws-s3-client whoami now id language signature-and-chain :mpollux))
  ([db aws-s3-client whoami now id language
    {:keys [chain] :as signature-and-chain} signing-method]
   (when-let [energiatodistus
              (energiatodistus-service/find-energiatodistus db id)]
     (do-when-signing
       energiatodistus
       #(do
          (validate-certificate! (:sukunimi whoami)
                                 now
                                 (first chain)
                                 (= signing-method :mpollux))
          (let [key (energiatodistus-service/file-key id language)
                content (file-service/find-file aws-s3-client key)
                content-bytes (.readAllBytes content)
                pkcs7 (puumerkki/make-pkcs7 signature-and-chain content-bytes)
                ;; TODO: Do something more robust here or remove this.
                ;; Decode the pkcs7 to get more confidence in that
                ;; puumerkki works.
                _ (puumerkki.codec/asn1-decode pkcs7)
                filename (str key ".pdf")]
            (->> (write-signature! id language content-bytes pkcs7)
                 (file-service/upsert-file-from-bytes aws-s3-client
                                                      key))
            filename))))))

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
    (mapv cert-pem->one-liner-without-headers [leaf intermediate root])))

(defn- data->signed-digest [data aws-kms-client]
  (->> data
       ^InputStream (sign-service/sign aws-kms-client)
       (.readAllBytes)))

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
        signed-digest (data->signed-digest data-to-sign aws-kms-client)
        chain cert-chain-three-long-leaf-first
        signature-and-chain {:chain chain :signature (.encode (Base64/getEncoder) signed-digest)}]
    (audit-log/info (audit-log-message laatija-allekirjoitus-id id "Signing via KMS"))
    (do-sign-with-system
      #(sign-energiatodistus-pdf db aws-s3-client whoami now id language signature-and-chain :kms)
      (audit-log-message laatija-allekirjoitus-id id "Signing via KMS failed!"))))

(defn- sign-with-system-end
  [{:keys [db whoami laatija-allekirjoitus-id id aws-s3-client]}]
  (audit-log/info (audit-log-message laatija-allekirjoitus-id id "End signing"))
  (do-sign-with-system
    #(energiatodistus-service/end-energiatodistus-signing! db aws-s3-client whoami id)
    (audit-log-message laatija-allekirjoitus-id id "End signing failed!")))

(defn- sign-single-pdf-with-system [params language]
  (->> (sign-with-system-digest language params)
       (#(do (println %) %))
       (sign-with-system-sign language params)))

(defn sign-with-system
  "Does the whole process of signing with the system."
  [{:keys [db id] :as params}]
  (let [language-id (-> (complete-energiatodistus-service/find-complete-energiatodistus db id) :perustiedot :kieli)]
    (try
      (condp = language-id
        energiatodistus-service/finnish-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system "fi" params)
          (sign-with-system-end params))

        energiatodistus-service/swedish-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system "sv" params)
          (sign-with-system-end params))

        energiatodistus-service/multilingual-language-id
        (do
          (sign-with-system-start params)
          (sign-single-pdf-with-system "fi" params)
          (sign-single-pdf-with-system "sv" params)
          (sign-with-system-end params))

        (exception/throw-ex-info! {:message (format "Invalid language-id %s in energiatodistus %s" language-id id)}))
      (catch ExceptionInfo e
        (if (= (-> e ex-data :type) :sign-with-system-error)
          (-> e ex-data :result)
          (throw e))))))


