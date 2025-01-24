(ns solita.etp.service.energiatodistus-signing
  "Contains functionality to sign specifically an energiatodistus."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [puumerkki.pdf :as puumerkki]
            [solita.common.certificates :as certificates]
            [solita.common.formats :as formats]
            [solita.common.libreoffice :as libreoffice]
            [solita.common.time :as common-time]
            [solita.common.xlsx :as xlsx]
            [solita.etp.common.audit-log :as audit-log]
            [solita.etp.config :as config]
            [solita.etp.exception :as exception]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.service.sign :as sign-service])
  (:import (clojure.lang ExceptionInfo)
           (java.awt Color Font)
           (java.awt.image BufferedImage)
           (java.io ByteArrayOutputStream File InputStream)
           (java.nio.charset StandardCharsets)
           (java.text Normalizer Normalizer$Form)
           (java.time Clock Instant LocalDate ZoneId ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Base64 Calendar Date GregorianCalendar HashMap)
           (javax.imageio ImageIO)
           (org.apache.pdfbox.multipdf Overlay Overlay$Position)
           (org.apache.pdfbox.pdmodel PDDocument
                                      PDPageContentStream
                                      PDPageContentStream$AppendMode)
           (org.apache.pdfbox.pdmodel.common PDMetadata)
           (org.apache.pdfbox.pdmodel.graphics.image PDImageXObject)
           (org.apache.xmpbox XMPMetadata)
           (org.apache.xmpbox.xml XmpSerializer)))

(def timezone (ZoneId/of "Europe/Helsinki"))
(def date-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy") timezone))
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

#_(defn signature-as-png [path laatija-fullname]
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

#_(defn get-digest [db aws-s3-client laatija]
    (let [energiatodistus-pdf nil
          cache-document (pdf-sign/get-unsigned-document-with-signature-field)
          digest (pdf-sign/get-digest cache-document)]
      (-> energiatodistus-pdf
          println
          )))

#_(defn sign-energiatodistus [db aws-s3-client whoami now id language signature-and-chain signing-method]
    (when-let [energiatodistus (energiatodistus-service/find-energiatodistus db id)]
      (do-when-signing

        ))
    ;; Find the cached todistus
    ;; Create T-level
    ;; Find the OCSP next update
    ;; Create LT-level
    ;; Finish?

    )

#_(defn sign-energiatodistus-pdf
    ([db aws-s3-client whoami now id language signature-and-chain]
     (sign-energiatodistus-pdf db aws-s3-client whoami now id language signature-and-chain :mpollux))
    ([db aws-s3-client whoami now id language
      {:keys [chain] :as signature-and-chain} signing-method]
     (when-let [energiatodistus
                (energiatodistus-service/find-energiatodistus db id)]
       (do-when-signing
         energiatodistus
         #(do
            (energiatodistus-pdf-service/validate-certificate! (:sukunimi whoami)
                                                               now
                                                               (first chain)
                                                               (= signing-method :mpollux))
            (let [key (energiatodistus-service/file-key id language)
                  content (file-service/find-file aws-s3-client key)
                  content-bytes (.readAllBytes content)
                  pkcs7 (puumerkki/make-pkcs7 signature-and-chain content-bytes)
                  filename (str key ".pdf")]
              (->> (pdf-sign/sign-document-as-pades-t-level content-bytes pkcs7)
                   (pdf-sign/get-seocnds-until-next-ocsp-update)
                   (pdf-sign/augment-pdf-to-pades-lt-level)
                   )


              filename))))))

(def test-sig-params-key "test-sig-params-key")
(def test-service-key "test-service-key")

(defn find-energiatodistus-digest
  "Generate the pdf.
  Upload to S3.
  Return the data that needs to be signed in base64"
  [db aws-s3-client id language laatija-allekirjoitus-id certs]
  (when-let [{:keys [laatija-fullname versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
    (do-when-signing
      complete-energiatodistus
      #(let [draft? false
             ^String pdf-path (energiatodistus-pdf-service/generate-pdf-as-file complete-energiatodistus language draft? laatija-allekirjoitus-id)
             signature-png-path (str/replace pdf-path #".pdf" "-signature.png")
             key (energiatodistus-service/file-key id language)
             energiatodistus-pdf (File. pdf-path)
             _ (signature-as-png signature-png-path laatija-fullname)
             digest-and-stuff (pdf-sign/get-digest energiatodistus-pdf {:versio versio :signature-png-path signature-png-path :laatija-fullname laatija-fullname} certs)]
         (file-service/upsert-file-from-file aws-s3-client
                                             key
                                             energiatodistus-pdf)
         (file-service/upsert-file-from-input-stream aws-s3-client
                                             test-sig-params-key
                                             (:sig-params-is digest-and-stuff))
         #_(file-service/upsert-file-from-file aws-s3-client
                                             test-service-key
                                             (:service-is digest-and-stuff))
         (io/delete-file pdf-path)
         (io/delete-file signature-png-path)
         {:digest (:digest digest-and-stuff)}))))

(defn sign-energiatodistus-pdf
  [db aws-s3-client id language laatija-allekirjoitus-id certs signature]
  (when-let [{:keys [laatija-fullname versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
     (do-when-signing
       complete-energiatodistus
       #(do
          (let [key (energiatodistus-service/file-key id language)
                unsigned-pdf-is (file-service/find-file aws-s3-client key)
                sig-params-is (file-service/find-file aws-s3-client test-sig-params-key)
                filename (str key ".pdf")
                signed-pdf-t-level (pdf-sign/sign-document-as-pades-t-level sig-params-is unsigned-pdf-is signature)
                ;;TODO: presist
                ]
            signed-pdf-t-level)))))
