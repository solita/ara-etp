(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.file :as file]))

(db/require-queries 'energiatodistus-destruction)

(defn- get-currently-expired-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db)
       (map :energiatodistus-id)))

(defn- anonymize-energiatodistus! [db energiatodistus-id]
  (energiatodistus-destruction-db/anonymize-energiatodistus! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-audit-data! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-audit! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-virhe! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-tiedoksi! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! db {:energiatodistus_id energiatodistus-id}))

(defn- delete-from-s3 [aws-s3-client file-key]
  (file/delete-file aws-s3-client file-key)
  (log/info (str "Deleted " file-key " from S3")))

(defn- delete-energiatodistus-pdf! [aws-s3-client energiatodistus-id language]
  (let [file-key (energiatodistus-service/file-key energiatodistus-id language)]
    (delete-from-s3 aws-s3-client file-key)))

(defn- delete-energiatodistus-pdfs! [db aws-s3-client energiatodistus-id]
  (let [language-codes (-> (complete-energiatodistus-service/find-complete-energiatodistus db energiatodistus-id)
                           :perustiedot
                           :kieli
                           energiatodistus-service/language-id->codes)]
    (doseq [language-code language-codes]
      (delete-energiatodistus-pdf! aws-s3-client energiatodistus-id language-code))))

(defn- delete-energiatodistus-liite-s3 [aws-s3-client liite-id]
  (let [file-key (liite-service/file-key liite-id)]
    ;; Some liitteet are only links and do not have files.
    (when (file/file-exists? aws-s3-client file-key)
      (delete-from-s3 aws-s3-client file-key))))

(defn- delete-energiatodistus-liite [db aws-s3-client liite]
  (let [liite-id (:id liite)]
    (energiatodistus-destruction-db/destroy-liite! db {:liite_id liite-id})
    (delete-energiatodistus-liite-s3 aws-s3-client liite-id)
    (energiatodistus-destruction-db/destroy-liite-audit! db {:liite_id liite-id})))

(defn- destroy-energiatodistus-liitteet [db aws-s3-client energiatodistus-id]
  (let [liitteet (energiatodistus-destruction-db/select-to-be-destroyed-liitteet-by-energiatodistus-id db {:energiatodistus_id energiatodistus-id})]
    (mapv #(delete-energiatodistus-liite db aws-s3-client %) liitteet)))

(defn- destroy-expired-energiatodistus! [db aws-s3-client energiatodistus-id]
  (jdbc/with-db-transaction [db db]
                            (destroy-energiatodistus-liitteet db aws-s3-client energiatodistus-id)
                            (destroy-energiatodistus-oikeellisuuden-valvonta! db energiatodistus-id)
                            (destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! db energiatodistus-id)
                            (destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! db energiatodistus-id)
                            (anonymize-energiatodistus! db energiatodistus-id)
                            (destroy-energiatodistus-audit-data! db energiatodistus-id))
  (delete-energiatodistus-pdfs! db aws-s3-client energiatodistus-id)
  (log/info (str "Destroyed energiatodistus (id: " energiatodistus-id ")")))

(defn destroy-expired-energiatodistukset! [db aws-s3-client]
  (log/info (str "Destruction of expired energiatodistukset initiated."))
  (let [expired-todistukset-ids (get-currently-expired-todistus-ids db)]
    (map #(destroy-expired-energiatodistus! db aws-s3-client %) expired-todistukset-ids)
    nil))


