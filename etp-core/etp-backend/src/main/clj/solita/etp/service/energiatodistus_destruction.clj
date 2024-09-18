(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.file :as file]))

(db/require-queries 'energiatodistus-destruction)

(defn- get-currently-expired-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db)
       (map :id)))

(defn- anonymize-energiatodistus! [db id]
  (energiatodistus-destruction-db/anonymize-energiatodistus! db {:id id}))

(defn- destroy-energiatodistus-audit-data! [db id]
  (energiatodistus-destruction-db/destroy-energiatodistus-audit-information! db {:id id}))

(defn- delete-energiatodistus-pdf! [aws-s3-client id language]
  (let [file-key (energiatodistus-service/file-key id language)]
(defn- destroy-energiatodistus-oikeellisuuden-valvonta! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-note! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-virhe! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-tiedoksi! db {:energiatodistus_id energiatodistus-id})
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta! db {:energiatodistus_id energiatodistus-id}))

(defn- destroy-energiatodistus-oikeellisuuden-valvonta-audit! [db energiatodistus-id]
  (energiatodistus-destruction-db/destroy-energiatodistus-oikeellisuuden-valvonta-audit! db {:energiatodistus_id energiatodistus-id}))

    (file/delete-file aws-s3-client file-key)
    (log/info (str "Deleted " file-key " from S3"))))

(defn- delete-energiatodistus-pdfs! [db aws-s3-client id]
  (let [language-codes (-> (complete-energiatodistus-service/find-complete-energiatodistus db id)
                           :perustiedot
                           :kieli
                           energiatodistus-service/language-id->codes)]
    (doseq [language-code language-codes]
      (delete-energiatodistus-pdf! aws-s3-client id language-code))))

(defn- destroy-expired-energiatodistus! [db aws-s3-client id]
  (jdbc/with-db-transaction [db db]
                            (anonymize-energiatodistus! db id)
                            (destroy-energiatodistus-audit-data! db id))
  (delete-energiatodistus-pdfs! db aws-s3-client id)
  (log/info (str "Destroyed energiatodistus (id: " id ")")))

(defn destroy-expired-energiatodistukset! [db aws-s3-client]
  (log/info (str "Destruction of expired energiatodistukset initiated."))
  (let [expired-todistukset (get-currently-expired-todistus-ids db)]
    (map #(destroy-expired-energiatodistus! db aws-s3-client %) expired-todistukset)
    nil))

