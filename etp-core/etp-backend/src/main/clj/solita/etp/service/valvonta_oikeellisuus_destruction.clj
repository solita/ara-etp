(ns solita.etp.service.valvonta-oikeellisuus-destruction
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.valvonta-oikeellisuus.asha :as vo-asha-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.service.file :as file]))

(db/require-queries 'valvonta-oikeellisuus-destruction)

;; ------------------------------

(defn- tag-versions-for-destruction! [aws-s3-client file-key]
  (let [;; This is needed so that the noncurrent versions are destroyed by a lifecycle rule.
        destruction-tag {:Key "ValvontaOikeellisuusDestruction" :Value "True"}
        version-ids (file/key->version-ids aws-s3-client file-key)]
    (run! #(file/put-file-tag aws-s3-client file-key destruction-tag %) version-ids)))

(defn- prepare-deletion-from-s3!
  [aws-s3-client file-key]
  (tag-versions-for-destruction! aws-s3-client file-key)
  (file/delete-file aws-s3-client file-key))

(defn- delete-from-s3! [aws-s3-client file-key {:keys [log-when-file-missing?]}]
  (if (file/file-exists? aws-s3-client file-key)
    (do
      (prepare-deletion-from-s3! aws-s3-client file-key)
      (log/info (str "Deleted " file-key " from S3")))
    (when log-when-file-missing?
      (log/warn (str "Tried to delete " file-key " but it does not exist!")))))

(defn- delete-liite-s3! [aws-s3-client {:keys [liite-id]}]
  (let [file-key (liite-service/file-key liite-id)]
    ;; Some liitteet are only links and do not have files.
    (delete-from-s3! aws-s3-client file-key {:log-when-file-missing? false})))

(defn- delete-viesti-liite-s3! [aws-s3-client {:keys [viesti-liite-id viestiketju-id]}]
  (let [file-key (viesti-service/file-path viestiketju-id viesti-liite-id)]
    ;; Some liitteet are only links and do not have files.
    (delete-from-s3! aws-s3-client file-key {:log-when-file-missing? false})))

(defn- delete-vo-toimenpide-s3! [aws-s3-client energiatodistus-id vo-toimenpide-id]
  (let [file-key (vo-asha-service/file-path energiatodistus-id vo-toimenpide-id)]
    ;; All the toimenpiteet do not create documents
    (delete-from-s3! aws-s3-client file-key {:log-when-file-missing? false})))

;; ------------------------------

(defn find-expired-oikeellisuuden-valvonta-ids [db]
  (valvonta-oikeellisuus-destruction-db/select-expired-oikeellisuuden-valvonta-ids db))

(defn find-linked-liite-ids [db vo-toimenpide-id]
  (valvonta-oikeellisuus-destruction-db/select-linked-liite-ids db {:vo-toimenpide-id vo-toimenpide-id}))

(defn find-linked-viesti-liite-ids [db vo-toimenpide-id]
  (valvonta-oikeellisuus-destruction-db/select-linked-viesti-liite-ids db {:vo-toimenpide-id vo-toimenpide-id}))

(defn find-linked-vo-note-ids [db energiatodistus-id]
  (valvonta-oikeellisuus-destruction-db/select-linked-vo-note-ids db {:energiatodistus-id energiatodistus-id}))

;; ------------------------------

(defn- affected-row-count [result]
  (cond
    (number? result) result
    (and (sequential? result)
         (= 1 (count result))
         (number? (first result)))
    (first result)
    :else
    (throw (ex-info "Unexpected database update result"
                    {:result result}))))

(defn- add-counts [total counts]
  (merge-with + total counts))

(def empty-destruction-counts
  {:vo-toimenpide-anonymized 0
   :vo-note                  0
   :vo-virhe                 0
   :liite                    0
   :vo-tiedoksi              0
   :vastaanottaja            0
   :viesti-reader            0
   :viesti                   0
   :viesti-liite             0
   :viestiketju              0})

(defn destroy-vo-note-table! [{:keys [db]} {:keys [energiatodistus-id]}]
  (let [linked-vo-note-ids (find-linked-vo-note-ids db energiatodistus-id)]
    {:vo-note
     (reduce
       +
       (for [{vo-note-id :vo-note-id} linked-vo-note-ids]
         (let [result
               (valvonta-oikeellisuus-destruction-db/destroy-vo-note-table-data!
                 db
                 {:vo-note-id vo-note-id})]
           (valvonta-oikeellisuus-destruction-db/destroy-audit-vo-note-table-data!
             db
             {:vo-note-id vo-note-id})
           (affected-row-count result))))}))

(defn destroy-vo-virhe-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  ;; No audit table for vo_virhe
  {:vo-virhe
   (affected-row-count
     (valvonta-oikeellisuus-destruction-db/destroy-vo-virhe-table-data!
       db
       {:vo-toimenpide-id vo-toimenpide-id}))})

(defn destroy-liite-table! [{:keys [db aws-s3-client]} {:keys [vo-toimenpide-id]}]
  (let [linked-liite-ids (find-linked-liite-ids db vo-toimenpide-id)]
    (run! (partial delete-liite-s3! aws-s3-client) linked-liite-ids)
    (let [result
          (valvonta-oikeellisuus-destruction-db/destroy-liite-table-data!
            db
            {:vo-toimenpide-id vo-toimenpide-id})]
      (valvonta-oikeellisuus-destruction-db/destroy-audit-liite-table-data!
        db
        {:vo-toimenpide-id vo-toimenpide-id})
      {:liite (affected-row-count result)})))

(defn destroy-vo-tiedoksi-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  {:vo-tiedoksi
   (affected-row-count
     (valvonta-oikeellisuus-destruction-db/destroy-vo-tiedoksi-table-data!
       db
       {:vo-toimenpide-id vo-toimenpide-id}))})

(defn destroy-vastaanottaja-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  {:vastaanottaja
   (affected-row-count
     (valvonta-oikeellisuus-destruction-db/destroy-vastaanottaja-table-data!
       db
       {:vo-toimenpide-id vo-toimenpide-id}))})

(defn destroy-viesti-reader-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  {:viesti-reader
   (affected-row-count
     (valvonta-oikeellisuus-destruction-db/destroy-viesti-reader-table-data!
       db
       {:vo-toimenpide-id vo-toimenpide-id}))})

(defn destroy-viesti-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  {:viesti
   (affected-row-count
     (valvonta-oikeellisuus-destruction-db/destroy-viesti-table-data!
       db
       {:vo-toimenpide-id vo-toimenpide-id}))})

(defn destroy-viesti-liite-table! [{:keys [db aws-s3-client]} {:keys [vo-toimenpide-id]}]
  (let [linked-liite-ids (find-linked-viesti-liite-ids db vo-toimenpide-id)]
    (run! (partial delete-viesti-liite-s3! aws-s3-client) linked-liite-ids)
    (let [result
          (valvonta-oikeellisuus-destruction-db/destroy-viesti-liite-table-data!
            db
            {:vo-toimenpide-id vo-toimenpide-id})]
      (valvonta-oikeellisuus-destruction-db/destroy-audit-viesti-liite-table-data!
        db
        {:vo-toimenpide-id vo-toimenpide-id})
      {:viesti-liite (affected-row-count result)})))

(defn destroy-viestiketju-table! [{:keys [db]} {:keys [vo-toimenpide-id]}]
  (let [result
        (valvonta-oikeellisuus-destruction-db/destroy-viestiketju-table-data!
          db
          {:vo-toimenpide-id vo-toimenpide-id})]
    (valvonta-oikeellisuus-destruction-db/destroy-audit-viestiketju-table-data!
      db
      {:vo-toimenpide-id vo-toimenpide-id})
    {:viestiketju (affected-row-count result)}))

(defn anonymize-and-destroy-vo-toimenpide-table! [{:keys [db aws-s3-client]} {:keys [vo-toimenpide-id energiatodistus-id]}]
  (delete-vo-toimenpide-s3! aws-s3-client energiatodistus-id vo-toimenpide-id)
  (let [result
        (valvonta-oikeellisuus-destruction-db/anonymize-vo-toimenpide-table-data!
          db
          {:vo-toimenpide-id vo-toimenpide-id})]
    (valvonta-oikeellisuus-destruction-db/destroy-audit-vo-toimenpide-table-data!
      db
      {:vo-toimenpide-id vo-toimenpide-id})
    {:vo-toimenpide-anonymized (affected-row-count result)}))

;; ------------------------------

(defn- check-whoami [whoami]
  (when-not (and (rooli-service/system? whoami)
                 (= (:id whoami) (kayttaja-service/system-kayttaja :valvonta-oikeellisuus-expiration)))
    (exception/throw-forbidden! (str "Can not run destruction of expired oikeellisuuden valvonta as whoami (id: " (:id whoami) ") (rooli: " (:rooli whoami) ")"))))

(defn- destroy-expired-vo-toimenpiteet!
  [db aws-s3-client vo-toimenpide-rows]
  (let [energiatodistus-id (:energiatodistus-id (first vo-toimenpide-rows))]
    (valvonta-oikeellisuus-destruction-db/lock-energiatodistus-and-vo-toimenpiteet!
      db
      {:energiatodistus-id energiatodistus-id})
    (let [expired-vo-toimenpide-rows
          (filter
            #(= energiatodistus-id (:energiatodistus-id %))
            (find-expired-oikeellisuuden-valvonta-ids db))]
      (if (seq expired-vo-toimenpide-rows)
        (let [handlers-in-order
              [destroy-vo-virhe-table!
               destroy-liite-table!
               destroy-vo-tiedoksi-table!
               destroy-vastaanottaja-table!
               destroy-viesti-reader-table!
               destroy-viesti-table!
               destroy-viesti-liite-table!
               destroy-viestiketju-table!
               anonymize-and-destroy-vo-toimenpide-table!]]
          (add-counts
            (destroy-vo-note-table!
              {:db db}
              {:energiatodistus-id energiatodistus-id})
            (reduce
              (fn [counts {:keys [vo-toimenpide-id]}]
                (add-counts
                  counts
                  (reduce
                    (fn [counts handler]
                      (add-counts
                        counts
                        (handler {:db db :aws-s3-client aws-s3-client}
                                 {:vo-toimenpide-id vo-toimenpide-id
                                  :energiatodistus-id energiatodistus-id})))
                    empty-destruction-counts
                    handlers-in-order)))
              empty-destruction-counts
              expired-vo-toimenpide-rows)))
        empty-destruction-counts))))

(defn anonymize-expired-vo-valvontas-and-destroy-linked-data [db aws-s3-client whoami]
  (check-whoami whoami)
  (let [expired-vo-toimenpide-rows (find-expired-oikeellisuuden-valvonta-ids db)
        expired-vo-toimenpiteet-by-energiatodistus
        (group-by :energiatodistus-id expired-vo-toimenpide-rows)
        destruction-counts
        (reduce
          (fn [total [_ vo-toimenpide-rows]]
            (add-counts
              total
              (jdbc/with-db-transaction [db db]
                (destroy-expired-vo-toimenpiteet!
                  db
                  aws-s3-client
                  vo-toimenpide-rows))))
          empty-destruction-counts
          expired-vo-toimenpiteet-by-energiatodistus)]
    (log/info "Valvonta oikeellisuus destruction finished. Affected resources: " destruction-counts)
    destruction-counts))
