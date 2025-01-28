(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.valvonta-oikeellisuus.asha :as vo-asha-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.file :as file]
            [solita.etp.service.viesti :as viesti-service]))

(db/require-queries 'energiatodistus-destruction)

(defn- anonymize-energiatodistus! [db energiatodistus-id]
  (energiatodistus-destruction-db/anonymize-energiatodistus! db {:energiatodistus-id energiatodistus-id}))

(defn- destroy-energiatodistus-audit-data! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-audit! db {:energiatodistus-id energiatodistus-id}))

(defn- tag-versions-for-destruction [aws-s3-client file-key]
  (let [;; This is needed so that the noncurrent versions are destroyed by a lifecycle rule.
        destruction-tag {:Key "EnergiatodistusDestruction" :Value "True"}
        version-ids (file/key->version-ids aws-s3-client file-key)]
    (run! #(file/put-file-tag aws-s3-client file-key destruction-tag %) version-ids)))

(defn- handle-deletion-from-s3 [aws-s3-client file-key]
  (tag-versions-for-destruction aws-s3-client file-key)
  (file/delete-file aws-s3-client file-key))

(defn- delete-from-s3 [aws-s3-client file-key {:keys [log-when-file-missing?]}]
  (if (file/file-exists? aws-s3-client file-key)
    (do
      (handle-deletion-from-s3 aws-s3-client file-key)
      (log/info (str "Deleted " file-key " from S3")))
    (when log-when-file-missing?
      (log/warn (str "Tried to delete " file-key " but it does not exist!")))))

(defn- delete-energiatodistus-pdf! [aws-s3-client energiatodistus-id language lang-info-found?]
  (let [file-key (energiatodistus-service/file-key energiatodistus-id language)]
    (delete-from-s3 aws-s3-client file-key {:log-when-file-missing? lang-info-found?})))

(defn- delete-energiatodistus-pdfs! [db aws-s3-client energiatodistus-id]
  (let [language-codes (-> (complete-energiatodistus-service/find-complete-energiatodistus db energiatodistus-id)
                           :perustiedot
                           :kieli
                           energiatodistus-service/language-id->codes)
        ;; Energiatodistus' "pt$kieli" might be null for historical reasons and then we must just try to delete
        ;; both of the existing todistukset.
        lang-info-found? (not (nil? language-codes))]
    (if lang-info-found?
      (run! #(delete-energiatodistus-pdf! aws-s3-client energiatodistus-id % lang-info-found?) language-codes)
      (do
        (delete-energiatodistus-pdf! aws-s3-client energiatodistus-id "fi" lang-info-found?)
        (delete-energiatodistus-pdf! aws-s3-client energiatodistus-id "sv" lang-info-found?)))))

(defn- delete-energiatodistus-liite-s3 [aws-s3-client liite-id]
  (let [file-key (liite-service/file-key liite-id)]
    ;; Some liitteet are only links and do not have files.
    (delete-from-s3 aws-s3-client file-key {:log-when-file-missing? false})))

(defn- delete-energiatodistus-liite [db aws-s3-client liite-id]
  (energiatodistus-destruction-db/destroy-liite! db {:liite-id liite-id})
  (delete-energiatodistus-liite-s3 aws-s3-client liite-id)
  (energiatodistus-destruction-db/destroy-liite-audit! db {:liite-id liite-id}))

(defn- destroy-energiatodistus-liitteet [db aws-s3-client energiatodistus-id]
  (let [liitteet (map :liite-id (energiatodistus-destruction-db/select-to-be-destroyed-liitteet-by-energiatodistus-id db {:energiatodistus-id energiatodistus-id}))]
    (run! #(delete-energiatodistus-liite db aws-s3-client %) liitteet)))

(defn- destroy-toimenpide-s3! [aws-s3-client energiatodistus-id toimenpide-id]
  (let [file-key (vo-asha-service/file-path energiatodistus-id toimenpide-id)]
    ;; All the toimenpiteet do not create documents
    (delete-from-s3 aws-s3-client file-key {:log-when-file-missing? false})))

(defn- destroy-oikeellisuuden-valvonta-s3! [db aws-s3-client energiatodistus-id]
  (let [vo-toimenpide-ids (map :vo-toimenpide-id (energiatodistus-destruction-db/select-vo-toimenpiteet-by-energiatodistus-id db {:energiatodistus-id energiatodistus-id}))]
    (run! #(destroy-toimenpide-s3! aws-s3-client energiatodistus-id %) vo-toimenpide-ids)))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta! [db aws-s3-client energiatodistus-id]
  (destroy-oikeellisuuden-valvonta-s3! db aws-s3-client energiatodistus-id)
  ;; Valvonta liitteet are destroyed via energiatodistus_id in destruction of liitteet.

  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note! db {:energiatodistus-id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-virhe! db {:energiatodistus-id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-tiedoksi! db {:energiatodistus-id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide! db {:energiatodistus-id energiatodistus-id})

  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! db {:energiatodistus-id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! db {:energiatodistus-id energiatodistus-id}))

(defn- destroy-viesti! [db viesti-id]
  (energiatodistus-destruction-db/destroy-viesti-reader! db {:viesti-id viesti-id})
  (energiatodistus-destruction-db/destroy-viesti! db {:viesti-id viesti-id}))

(defn- delete-viestiketju-liite-s3 [aws-s3-client viestiketju-id liite-id]
  (let [file-key (viesti-service/file-path viestiketju-id liite-id)]
    ;; Some liitteet are only links and do not have files.
    (delete-from-s3 aws-s3-client file-key {:log-when-file-missing? false})))

(defn- destroy-viestiketju-liitteet [db aws-s3-client viestiketju-id]
  (let [viestiketju-liite-ids (energiatodistus-destruction-db/select-liitteet-by-viestiketju-id db {:viestiketju-id viestiketju-id})]
    (run! #(delete-viestiketju-liite-s3 aws-s3-client viestiketju-id (:viesti-liite-id %)) viestiketju-liite-ids)
    (energiatodistus-destruction-db/destroy-viestiketju-liite! db {:viestiketju-id viestiketju-id})
    (energiatodistus-destruction-db/destroy-viestiketju-liite-audit! db {:viestiketju-id viestiketju-id})))

(defn- destroy-viestiketju [db aws-s3-client viestiketju-id]
  (let [viestit (energiatodistus-destruction-db/select-viestit-by-viestiketju-id db {:viestiketju-id viestiketju-id})]
    (energiatodistus-destruction-db/destroy-vastaanottaja! db {:viestiketju-id viestiketju-id})
    (destroy-viestiketju-liitteet db aws-s3-client viestiketju-id)
    (run! #(destroy-viesti! db (:viesti-id %)) viestit)
    (energiatodistus-destruction-db/destroy-viestiketju! db {:viestiketju-id viestiketju-id})
    (energiatodistus-destruction-db/destroy-viestiketju-audit! db {:viestiketju-id viestiketju-id})))

(defn- check-oikeellisuuden-valvonta-viestiketjut [db vo-toimenpide-id]
  (let [viestiketjut (energiatodistus-destruction-db/select-viestiketjut-by-vo-toimenpide-id db {:vo-toimenpide-id vo-toimenpide-id})]
    ;; These should be empty as they are destroyed via energiatodistus? Do something if they are not?
    ;; Maybe unlink the valvonta from viestiketju or just delete them?
    ;; For now just log an error if this happens as this should not happen.
    (when-not (empty? viestiketjut)
      (log/error "There exists one or many viestiketju for oikeellisuuden valvonta (id: " vo-toimenpide-id ")"))))

(defn- check-oikeellisuuden-valvontojen-viestiketjut [db energiatodistus-id]
  (let [vo-toimenpide-ids (map :vo-toimenpide-id (energiatodistus-destruction-db/select-vo-toimenpiteet-by-energiatodistus-id db {:energiatodistus-id energiatodistus-id}))]
    (run! (partial check-oikeellisuuden-valvonta-viestiketjut db) vo-toimenpide-ids)))

(defn- destroy-energiatodistus-viestiketjut [db aws-s3-client energiatodistus-id]
  (let [viestiketjut-ids (map :viestiketju-id (energiatodistus-destruction-db/select-viestiketjut-by-energiatodistus-id db {:energiatodistus-id energiatodistus-id}))]
    (run! #(destroy-viestiketju db aws-s3-client %) viestiketjut-ids)
    (check-oikeellisuuden-valvontojen-viestiketjut db energiatodistus-id)))

(defn- destroy-expired-energiatodistus! [db aws-s3-client energiatodistus-id]
  (delete-energiatodistus-pdfs! db aws-s3-client energiatodistus-id)
  (jdbc/with-db-transaction [db db]
                            (destroy-energiatodistus-viestiketjut db aws-s3-client energiatodistus-id)
                            (destroy-energiatodistus-liitteet db aws-s3-client energiatodistus-id)
                            (destroy-energiatodistus-oikeellisuuden-valvonta! db aws-s3-client energiatodistus-id)
                            (anonymize-energiatodistus! db energiatodistus-id)
                            (destroy-energiatodistus-audit-data! db energiatodistus-id))
  (log/info (str "Destroyed energiatodistus (id: " energiatodistus-id ")")))

(defn get-currently-expired-todistus-ids [db {:keys [check-valvonta?]}]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db {:check-valvonta check-valvonta?})
       (map :energiatodistus-id)))

(defn- make-energiatodistus-vanhentunut
  "There is an edge case where todistus might be expired but can not be
   destroyed as there is still active valvonta on it. This function can be
   used to make it 'vanhentunut' so that it can be handled differently from"
  [db energiatodistus-id]
  (energiatodistus-destruction-db/make-energiatodistus-vanhentunut! db {:energiatodistus-id energiatodistus-id})
  (log/info (str "Set energiatodistus (id: " energiatodistus-id ") tila to vanhentunut.")))

(defn- check-whoami [whoami]
  (when-not (and (rooli-service/system? whoami)
                 (= (:id whoami) (kayttaja-service/system-kayttaja :expiration)))
    (exception/throw-forbidden! (str "Can not run destruction of expired todistukset as whoami (id: " (:id whoami) ") (rooli: " (:rooli whoami) ")"))))

(defn get-destroyed-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-destroyed-energiatodistus-ids db)
       (map :energiatodistus-id)))

(defn destroy-expired-energiatodistukset! [db aws-s3-client whoami]
  (check-whoami whoami)
  (log/info (str "Starting destruction of expired energiatodistukset."))
  (let [expired-todistukset-without-valvonta-ids (set (get-currently-expired-todistus-ids db {:check-vavonta? true}))
        all-expired-todistukset-ids (set (get-currently-expired-todistus-ids db {:check-valvonta? false}))
        expired-todistukset-with-valvonta-ids (set/difference all-expired-todistukset-ids expired-todistukset-without-valvonta-ids)]
    (run! #(make-energiatodistus-vanhentunut db %) expired-todistukset-with-valvonta-ids)
    (run! #(destroy-expired-energiatodistus! db aws-s3-client %) expired-todistukset-without-valvonta-ids))
  (log/info (str "Destruction of expired energiatodistukset finished.")))
