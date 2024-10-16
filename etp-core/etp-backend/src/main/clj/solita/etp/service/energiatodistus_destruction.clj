(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.file :as file]
            [solita.etp.service.viesti :as viesti-service]))

(db/require-queries 'energiatodistus-destruction)

(defn- anonymize-energiatodistus! [db energiatodistus-id]
  (energiatodistus-destruction-db/anonymize-energiatodistus! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-audit-data! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-audit! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-virhe! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-tiedoksi! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide! db {:energiatodistus_id energiatodistus-id})

  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! db {:energiatodistus_id energiatodistus-id}))

(defn- delete-from-s3 [aws-s3-client file-key]
  (if (file/file-exists? aws-s3-client file-key)
    (do
      (file/delete-file aws-s3-client file-key)
      (log/info (str "Deleted " file-key " from S3")))
    (do
      (log/info (str "Tried to delete " file-key " but it does not exist!")))))

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

(defn- destroy-viesti! [db viesti-id]
  (energiatodistus-destruction-db/destroy-viesti-reader! db {:viesti_id viesti-id})
  (energiatodistus-destruction-db/destroy-viesti! db {:viesti_id viesti-id}))

(defn- delete-viestiketju-liite-s3 [aws-s3-client viestiketju-id liite-id]
  (let [file-key (viesti-service/file-path viestiketju-id liite-id)]
    ;; Some liitteet are only links and do not have files.
    (when (file/file-exists? aws-s3-client file-key)
      (delete-from-s3 aws-s3-client file-key))))

(defn- destroy-viestiketju-liitteet [db aws-s3-client viestiketju-id]
  (let [viestiketju-liite-ids (energiatodistus-destruction-db/select-liitteet-by-viestiketju db {:viestiketju_id viestiketju-id})]
    (mapv #(delete-viestiketju-liite-s3 aws-s3-client viestiketju-id (:viesti-liite-id %)) viestiketju-liite-ids)
    (energiatodistus-destruction-db/destroy-viestiketju-liite! db {:viestiketju_id viestiketju-id})
    (energiatodistus-destruction-db/destroy-viestiketju-liite-audit! db {:viestiketju_id viestiketju-id})))

(defn- destroy-viestiketju [db aws-s3-client viestiketju-id]
  (let [viestit (energiatodistus-destruction-db/select-viestit-by-viestiketju db {:viestiketju_id viestiketju-id})]
    (energiatodistus-destruction-db/destroy-vastaanottaja! db {:viestiketju_id viestiketju-id})
    (destroy-viestiketju-liitteet db aws-s3-client viestiketju-id)
    (mapv #(destroy-viesti! db (:viesti-id %)) viestit)
    (energiatodistus-destruction-db/destroy-viestiketju! db {:viestiketju_id viestiketju-id})
    (energiatodistus-destruction-db/destroy-viestiketju-audit! db {:viestiketju_id viestiketju-id})))

(defn- check-oikeellisuuden-valvonta-viestiketjut [db vo-toimenpide-id]
  (let [viestiketjut (energiatodistus-destruction-db/select-viestiketjut-by-oikeellisuuden-valvonta db {:vo_toimenpide_id vo-toimenpide-id})]
    ;; These should be empty as they are destroyed via energiatodistus? Do something if they are not?
    ;; Maybe unlink the valvonta from viestiketju or just delete them?
    ;; For now just log an error if this happens as this should not happen.
    (when-not (empty? viestiketjut)
      (log/error "There exists one or many viestiketju for oikeellisuuden valvonta (id: " vo-toimenpide-id ")"))))

(defn- check-oikeellisuuden-valvontojen-viestiketjut [db energiatodistus-id]
  (let [vo-toimenpide-ids (map :id (energiatodistus-destruction-db/select-vo-toimenpiteet-by-energiatodistus-id db {:energiatodistus_id energiatodistus-id}))]
    (mapv (partial check-oikeellisuuden-valvonta-viestiketjut db) vo-toimenpide-ids)))

(defn- destroy-energiatodistus-viestiketjut [db aws-s3-client energiatodistus-id]
  (let [viestiketjut (energiatodistus-destruction-db/select-vo-toimenpiteet-by-energiatodistus-id db {:energiatodistus_id energiatodistus-id})]
    (mapv #(destroy-viestiketju db aws-s3-client %) viestiketjut)
    (check-oikeellisuuden-valvontojen-viestiketjut db energiatodistus-id)))

(defn- destroy-expired-energiatodistus! [db aws-s3-client energiatodistus-id]
  (delete-energiatodistus-pdfs! db aws-s3-client energiatodistus-id)
  (jdbc/with-db-transaction [db db]
                            (destroy-energiatodistus-viestiketjut db aws-s3-client energiatodistus-id)
                            (destroy-energiatodistus-liitteet db aws-s3-client energiatodistus-id)
                            (destroy-energiatodistus-oikeellisuuden-valvonta! db energiatodistus-id)
                            (anonymize-energiatodistus! db energiatodistus-id)
                            (destroy-energiatodistus-audit-data! db energiatodistus-id))
  (log/info (str "Destroyed energiatodistus (id: " energiatodistus-id ")")))

(defn get-currently-expired-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db)
       (map :energiatodistus-id)))

(defn destroy-expired-energiatodistukset! [db aws-s3-client]
  (log/info (str "Destruction of expired energiatodistukset initiated."))
  (let [expired-todistukset-ids (get-currently-expired-todistus-ids db)]
    (doall (mapv #(destroy-expired-energiatodistus! db aws-s3-client %) expired-todistukset-ids))
    nil))

