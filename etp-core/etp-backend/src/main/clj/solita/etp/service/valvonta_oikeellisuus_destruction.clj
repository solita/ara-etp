(ns solita.etp.service.valvonta-oikeellisuus-destruction
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.file :as file]))

(db/require-queries 'valvonta-oikeellisuus-destruction)

;; ------------------------------

(defn- tag-versions-for-destruction [aws-s3-client file-key]
  (let [;; This is needed so that the noncurrent versions are destroyed by a lifecycle rule.
        destruction-tag {:Key "ValvontaOikeellisuusDestruction" :Value "True"}
        version-ids (file/key->version-ids aws-s3-client file-key)]
    (run! #(file/put-file-tag aws-s3-client file-key destruction-tag %) version-ids)))

(defn- handle-deletion-from-s3
  "Same as in energiatodistus-destruction but "
  [aws-s3-client file-key]
  (tag-versions-for-destruction aws-s3-client file-key)
  (file/delete-file aws-s3-client file-key))

(defn- delete-from-s3 [aws-s3-client file-key {:keys [log-when-file-missing?]}]
  (if (file/file-exists? aws-s3-client file-key)
    (do
      (handle-deletion-from-s3 aws-s3-client file-key)
      (log/info (str "Deleted " file-key " from S3")))
    (when log-when-file-missing?
      (log/warn (str "Tried to delete " file-key " but it does not exist!")))))

(defn- delete-liite-s3 [aws-s3-client {:keys [liite-id]}]
  (let [file-key (liite-service/file-key liite-id)]
    ;; Some liitteet are only links and do not have files.
    (delete-from-s3 aws-s3-client file-key {:log-when-file-missing? false})))

;; ------------------------------

(defn find-expired-oikeellisuuden-valvonta-ids [db]
  (valvonta-oikeellisuus-destruction-db/select-expired-oikeellisuuden-valvonta-ids db))

(defn find-linked-liite-ids [db vo-toimenpide-id]
  (valvonta-oikeellisuus-destruction-db/select-linked-liite-ids db {:vo-toimenpide-id vo-toimenpide-id}))

;; ------------------------------

(defn handle-vo-virhe-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  ;; No audit table for vo_virhe
  (valvonta-oikeellisuus-destruction-db/destroy-vo-virhe-table-data! db {:vo-toimenpide-id vo-toimenpide-id}))

(defn handle-liite-table! [{:keys [db aws-s3-client]} {:keys [vo-toimenpide-id]}]
  (let [linked-liite-ids (find-linked-liite-ids db vo-toimenpide-id)]
    (run! (partial delete-liite-s3 aws-s3-client) linked-liite-ids))
  (valvonta-oikeellisuus-destruction-db/destroy-liite-table-data! db {:vo-toimenpide-id vo-toimenpide-id})
  (valvonta-oikeellisuus-destruction-db/destroy-audit-liite-table-data! db {:vo-toimenpide-id vo-toimenpide-id}))

;; ------------------------------

(defn anonymize-expired-vo-valvontas-and-destroy-linked-data [db aws-s3-client]
  (let [expired-vo-toimenpide-ids (find-expired-oikeellisuuden-valvonta-ids db)
        ;; Note that order of these is important
        handlers-in-order
        [handle-vo-virhe-table!
         handle-liite-table!]]
    (doseq [vo-toimenpide-id expired-vo-toimenpide-ids]
      (jdbc/with-db-transaction [db db]
        (doseq [handler handlers-in-order]
          (handler {:db db :aws-s3-client aws-s3-client} vo-toimenpide-id)))
      ;; TODO: Log info?
      (log/debug "Valvonta oikeellisuus destruction for " vo-toimenpide-id " finished."))))


