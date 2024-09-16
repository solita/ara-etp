(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.file :as file])
  (:import (clojure.lang ExceptionInfo)
           (org.postgresql.util PSQLException)))

(db/require-queries 'energiatodistus-destruction)

(defn- get-currently-expired-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db)
       (map :id)))

(defn- hard-delete-energiatodistus!
  "Hard deletes energiatodistus."
  [db id]
  (energiatodistus-destruction-db/hard-delete-energiatodistus! db {:id id}))

(defn- anonymize-energiatodistus! [db id]
  (energiatodistus-destruction-db/anonymize-energiatodistus! db {:id id}))

(defn- linked-data-exist?
  "Returns whether there are linked database resources left to an energiatodistus or not."
  [db id]
  (try
    (jdbc/with-db-transaction [db db]
                              ;; This will only test that there are no links left to energiatodistus.
                              (jdbc/db-set-rollback-only! db)
                              (hard-delete-energiatodistus! db id))
    true
    ;; For some reason PSQLException can't be caught here.
    (catch ExceptionInfo e
      (if (instance? PSQLException (.getCause e))
        ;; https://www.postgresql.org/docs/15/errcodes-appendix.html
        ;; 23503 - foreign key violation
        (if (= "23503" (.getSQLState ^PSQLException (.getCause e)))
          false
          (throw e))
        (throw e)))))

(defn- delete-energiatodistus-pdf! [aws-s3-client id language]
  (let [file-key (energiatodistus-service/file-key id language)]
    (file/delete-file aws-s3-client file-key)
    (log/info (str "Deleted " file-key " from S3"))))

(defn- delete-energiatodistus-pdfs! [db aws-s3-client id]
  (let [language-codes (-> (complete-energiatodistus-service/find-complete-energiatodistus db id)
                           :perustiedot
                           :kieli
                           energiatodistus-service/language-id->codes)]
    (doseq [language-code language-codes]
      (delete-energiatodistus-pdf! aws-s3-client id language-code))))

(defn destroy-expired-energiatodistukset! [db aws-s3-client]
  (log/info (str "Destruction of expired energiatodistukset initiated."))
  (let [expired-todistukset (get-currently-expired-todistus-ids db)]
    (map #(delete-energiatodistus-pdfs! db aws-s3-client %) expired-todistukset)
    nil))

