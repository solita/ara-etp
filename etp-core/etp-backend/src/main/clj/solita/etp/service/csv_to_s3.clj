(ns solita.etp.service.csv-to-s3
  (:require [clojure.tools.logging :as log]
            [solita.etp.service.energiatodistus-csv :as energiatodistus-csv]
            [solita.etp.service.file :as file]
            [solita.etp.service.aineisto :as aineisto-service])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]))

(def ^:private buffer-size (* 8 1024 1024))  ; 8MB
(def ^:private upload-threshold (* 5 1024 1024))  ; 5MB

(defn- create-buffer []
  (ByteBuffer/allocate buffer-size))

(def public-csv-key
  "/api/csv/public/energiatodistukset.csv")

(defn aineisto-key [aineisto-id]
  (str "/api/signed/aineistot/" aineisto-id "/energiatodistukset.csv"))

(defn- create-upload-parts-fn [csv-reducible-query]
  (let [current-part (create-buffer)]
    (fn [upload-part-fn]
      (csv-reducible-query
       (fn [^String row]
         (let [row-bytes (.getBytes row StandardCharsets/UTF_8)]
           (.put current-part row-bytes)
           (when (> (.position current-part) upload-threshold)
             (upload-part-fn (aineisto-service/extract-byte-array-and-reset! current-part))))))
      ;; Upload any remaining data
      (when (not= 0 (.position current-part))
        (upload-part-fn (aineisto-service/extract-byte-array-and-reset! current-part))))))

(defn- process-csv-to-s3! [aws-s3-client key csv-reducible-query log-start log-end]
  (log/info log-start)
  (let [upload-parts-fn (create-upload-parts-fn csv-reducible-query)]
    (file/upsert-file-in-parts aws-s3-client key upload-parts-fn)
    (log/info log-end)))

(defn update-aineisto-in-s3! [db whoami aws-s3-client aineisto-id]
  (let [csv-query (aineisto-service/aineisto-reducible-query db whoami aineisto-id)
        key (aineisto-key aineisto-id)
        start-msg (str "Starting updating of aineisto (id: " aineisto-id ").")
        end-msg (str "Updating of aineisto (id: " aineisto-id ") finished.")]
    (process-csv-to-s3! aws-s3-client key csv-query start-msg end-msg)))

(defn update-public-csv-in-s3! [db whoami aws-s3-client query]
  (let [csv-query (energiatodistus-csv/energiatodistukset-public-csv db whoami query)]
    (process-csv-to-s3!
     aws-s3-client
     public-csv-key
     csv-query
     "Starting updating of public energiatodistus."
     "Updating of public energiatodistus finished.")))

(defn update-aineistot-in-s3! [db whoami aws-s3-client]
  (doseq [id [1 2 3]]
    (update-aineisto-in-s3! db whoami aws-s3-client id)))
