(ns solita.etp.service.kayttaja
  (:require [clojure.java.jdbc :as jdbc]
            [buddy.hashers :as hashers]
            [schema.coerce :as coerce]
            [solita.etp.exception :as exception]
            [solita.etp.db :as db]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.schema.kayttaja :as kayttaja-schema]
            [solita.etp.schema.common :as common-schema]
            [flathead.flatten :as flat]
            [schema.core :as schema]
            [clojure.set :as set]))

;; *** Require sql functions ***
(db/require-queries 'kayttaja)

;; *** Conversions from database data types ***
(defn coerce-kayttaja [schema]
  (coerce/coercer! schema
                   {(schema/maybe kayttaja-schema/VirtuId)
                    #(if (every? nil? (vals %)) nil %)}))

(defn db-row->kayttaja [schema]
  (comp
    (coerce-kayttaja schema)
    (partial flat/flat->tree #"\$")))

(defn find-kayttaja
  ([db id]
   (->> {:id id}
        (kayttaja-db/select-kayttaja db)
        (map (db-row->kayttaja kayttaja-schema/Kayttaja))
        first))
  ([db whoami id]
   (when-let [kayttaja (find-kayttaja db id)]
     (if (or (= id (:id whoami))
             (rooli-service/paakayttaja? whoami)
             (rooli-service/laskuttaja? whoami)
             (and (rooli-service/patevyydentoteaja? whoami)
                  (rooli-service/laatija? kayttaja)))
       kayttaja
       (exception/throw-forbidden!)))))

(defn find-kayttajat [db]
  (map (db-row->kayttaja kayttaja-schema/Kayttaja)
       (kayttaja-db/select-kayttajat db)))

(defn empty-virtuid [kayttaja]
  (if (-> kayttaja (get :virtu :undefined) nil?)
    (assoc kayttaja :virtu {:organisaatio nil :localid nil})
    kayttaja))

(defn api-key-hash [kayttaja]
  (if-let [api-key (:api-key kayttaja)]
    (assoc kayttaja :api-key-hash
                   (hashers/derive api-key {:alg :bcrypt+sha512}))
    kayttaja))

(defn- kayttaja->db-row [kayttaja]
  (-> kayttaja
      api-key-hash
      (dissoc :api-key)
      (set/rename-keys {:rooli :rooli-id})
      empty-virtuid
      (->> (flat/tree->flat "$"))
      (dissoc :virtu)))

(defn add-kayttaja! [db kayttaja]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :kayttaja (kayttaja->db-row kayttaja) db/default-opts)
      first :id))

(defn update-kayttaja!
  "Update all other users (kayttaja) except laatija."
  [db whoami id kayttaja]
  (if (or (and (= id (:id whoami))
               (common-schema/not-contains-keys
                kayttaja
                kayttaja-schema/KayttajaAdminUpdate))
          (rooli-service/paakayttaja? whoami))
    (db/with-db-exception-translation
      jdbc/update! db :kayttaja (kayttaja->db-row kayttaja)
        ["rooli_id > 0 and id = ?" id]
        db/default-opts)
    (exception/throw-forbidden!)))

(defn find-history [db whoami kayttaja-id]
  (if (or (rooli-service/paakayttaja? whoami)
          (= kayttaja-id (:id whoami)))
    (->> (kayttaja-db/select-kayttaja-history db {:id kayttaja-id})
         (map (db-row->kayttaja kayttaja-schema/KayttajaHistory)))
    (exception/throw-forbidden!)))

(def system-kayttaja
  {:communication -3
   :laskutus -2
   :presigned -4
   :aineisto -5})
