(ns solita.etp.service.energiatodistus-destruction
  "Contains functions for anonymizing the energiatodistus and removing
  data linked to it."
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [solita.etp.db :as db])
  (:import (clojure.lang ExceptionInfo)
           (org.postgresql.util PSQLException)))

(db/require-queries 'energiatodistus-destruction)

(defn- get-currently-expired-todistus-ids [db]
  (->> (energiatodistus-destruction-db/select-expired-energiatodistus-ids db)
       (map :id)))

(defn destroy-expired-energiatodistukset! [db aws-s3-client]
  (log/info (str "Destruction of expired energiatodistukset initiated."))
  (let [expired-todistukset (get-currently-expired-todistus-ids db)]
    ;; TODO:
    nil))

(defn- hard-delete-energiatodistus!
  "Hard deletes energiatodistus."
  [db id]
  (energiatodistus-destruction-db/hard-delete-energiatodistus! db {:id id}))

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
