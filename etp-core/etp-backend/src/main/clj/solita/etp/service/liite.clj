(ns solita.etp.service.liite
  (:require [solita.etp.db :as db]
            [solita.etp.schema.liite :as liite-schema]
            [solita.etp.service.json :as json]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.exception :as exception]
            [schema.coerce :as coerce]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]))

; *** Require sql functions ***
(db/require-queries 'liite)

; *** Conversions from database data types ***
(def coerce-liite (coerce/coercer liite-schema/Liite json/json-coercions))

(defn- insert-liite! [liite db]
  (-> (db/with-db-exception-translation jdbc/insert! db :liite liite db/default-opts)
      first
      :id))

(defn- file-key [liite-id]
  (str "liitteet/" liite-id))

(defn- insert-file! [key aws-s3-client file]
  (file-service/upsert-file-from-file aws-s3-client key file))

(defn assert-permission! [db whoami energiatodistus-id]
  (when-not (energiatodistus-service/find-energiatodistus
              db whoami energiatodistus-id)
    (exception/throw-forbidden!)))

(defn temp-file->liite [temp-file]
  (-> temp-file
      (dissoc :tempfile :size)
      (set/rename-keys {:content-type :contenttype
                        :filename :nimi})))

(defn add-liite-from-file! [db aws-s3-client energiatodistus-id file]
  (jdbc/with-db-transaction [db db]
    (let [id (-> file
                 temp-file->liite
                 (assoc :energiatodistus-id energiatodistus-id)
                 (insert-liite! db))]
      (-> id file-key (insert-file! aws-s3-client (:tempfile file)))
      id)))

(defn add-liitteet-from-files! [db aws-s3-client whoami energiatodistus-id files]
  (jdbc/with-db-transaction [db db]
    (assert-permission! db whoami energiatodistus-id)
    (mapv #(add-liite-from-file!
            db aws-s3-client energiatodistus-id %)
          files)))

(defn add-liite-from-link!
  ([db energiatodistus-id liite]
    (-> liite
        (assoc :energiatodistus-id energiatodistus-id)
        (assoc :contenttype "text/uri-list")
        (insert-liite! db)))
  ([db whoami energiatodistus-id liite]
   (assert-permission! db whoami energiatodistus-id)
   (add-liite-from-link! db energiatodistus-id liite)))

(defn find-energiatodistus-liitteet [db whoami energiatodistus-id]
  (jdbc/with-db-transaction [db db]
    (assert-permission! db whoami energiatodistus-id)
    (map coerce-liite
         (liite-db/select-liite-by-energiatodistus-id
          db {:energiatodistus-id energiatodistus-id}))))

(defn find-energiatodistus-liite-content [db whoami aws-s3-client liite-id]
  (when-let [liite (first (liite-db/select-liite db {:id liite-id}))]
    (assert-permission! db whoami (:energiatodistus-id liite))
    (assoc liite :content (file-service/find-file aws-s3-client (file-key liite-id)))))

(defn delete-liite! [db whoami liite-id]
  (let [energiatodistus-id (some->> {:id liite-id}
                                    (liite-db/select-liite db)
                                    first
                                    :energiatodistus-id)]
    (assert-permission! db whoami energiatodistus-id)
    (liite-db/delete-liite! db {:id liite-id})))
