(ns solita.etp.service.energiatodistus-signing
  "Contains functionality to sign specifically an energiatodistus."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [solita.etp.common.audit-log :as audit-log]
            [solita.etp.exception :as exception]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign])
  (:import (java.awt Color Font)
           (java.awt.image BufferedImage)
           (java.io File)
           (java.time Instant ZoneId)
           (java.time.format DateTimeFormatter)
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

(def test-sig-params-key "test-sig-params-key")
(def test-service-key "test-service-key")

(defn find-energiatodistus-digest
  "This is the function that does first real processing of the signature.

  Generate the pdf.
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
             signature-png (File. signature-png-path)
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

(defn sign-energiatodistus-pdf
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
                docdoc (pdf-sign/t-level->lt-level signed-pdf-t-level)
                ;;TODO: presist
                ]
            docdoc)))))
