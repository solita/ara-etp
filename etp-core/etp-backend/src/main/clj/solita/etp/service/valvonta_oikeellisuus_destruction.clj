(ns solita.etp.service.valvonta-oikeellisuus-destruction
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]))

(db/require-queries 'valvonta-oikeellisuus-destruction)

(defn find-expired-oikeellisuuden-valvonta-ids [db]
  (valvonta-oikeellisuus-destruction-db/select-expired-oikeellisuuden-valvonta-ids db))

(defn handle-vo-virhe-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  ;; No audit table for vo_virhe
  (valvonta-oikeellisuus-destruction-db/destroy-vo-virhe-table-data! db {:vo-toimenpide-id vo-toimenpide-id}))

(defn anonymize-expired-vo-valvontas-and-destroy-linked-data [db aws-s3-client]
  (let [expired-vo-toimenpide-ids (find-expired-oikeellisuuden-valvonta-ids db)
        ;; Note that order of these is important
        handlers-in-order
        [handle-vo-virhe-table!]]
    (doseq [vo-toimenpide-id expired-vo-toimenpide-ids]
      (jdbc/with-db-transaction [db db]
        (doseq [handler handlers-in-order]
          (handler {:db db :aws-s3-client aws-s3-client} vo-toimenpide-id)))
      ;; TODO: Log info?
      (log/debug "Valvonta oikeellisuus destruction for " vo-toimenpide-id " finished."))))


