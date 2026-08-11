(ns solita.etp.service.kayttaja
  (:require [clojure.java.jdbc :as jdbc]
            [buddy.hashers :as hashers]
            [schema.coerce :as coerce]
            [solita.etp.exception :as exception]
            [solita.etp.db :as db]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.viesti :as viesti-service]
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

(defn- mask-henkilotunnus
  "Returns kayttaja with :henkilotunnus removed (set to nil) unless whoami is
   allowed to see it: whoami is the same user as kayttaja, whoami is a
   paakayttaja, whoami is a patevyydentoteaja and kayttaja is a laatija, or
   whoami is a laskuttaja and kayttaja is a laatija.

   The access controls for using find-kayttaja-for are essentially the same, so
   at the moment this serves simply as a defensive extra layer to limit access
   to hetu."
  [whoami kayttaja]
  (if (or (= (:id kayttaja) (:id whoami))
          (rooli-service/paakayttaja? whoami)
          (and (rooli-service/patevyydentoteaja? whoami)
               (rooli-service/laatija? kayttaja))
          (and (rooli-service/laskuttaja? whoami)
               (rooli-service/laatija? kayttaja)))
    kayttaja
    (assoc kayttaja :henkilotunnus nil)))

(defn find-kayttaja
  "Finds the kayttaja with the given id, or returns nil when no kayttaja is found."
  [db id]
  (->> {:id id}
       (kayttaja-db/select-kayttaja db)
       (map (db-row->kayttaja kayttaja-schema/Kayttaja))
       first))

(defn find-kayttaja-for
  "Finds the kayttaja with the given id, with access controls.

   Access is allowed for the kayttaja themselves, paakayttaja users, and
   laskuttaja or patevyydentoteaja users when the kayttaja is a laatija.
   Returns nil when no kayttaja is found and throws forbidden when access is
   denied.

   The :henkilotunnus is masked unless whoami is allowed to see it."
  [db whoami id]
  (when-let [kayttaja (find-kayttaja db id)]
    (if (or (= id (:id whoami))
            (rooli-service/paakayttaja? whoami)
            (and (rooli-service/laskuttaja? whoami)
                 (rooli-service/laatija? kayttaja))
            (and (rooli-service/patevyydentoteaja? whoami)
                 (rooli-service/laatija? kayttaja)))
      (mask-henkilotunnus whoami kayttaja)
      (exception/throw-forbidden!))))

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
  (let [new-kayttaja-id (-> (db/with-db-exception-translation
                              jdbc/insert! db :kayttaja (kayttaja->db-row kayttaja) db/default-opts)
                            first :id)
        new-kayttaja-rooli (:rooli kayttaja)
        new-kayttaja-whoami {:id new-kayttaja-id :rooli new-kayttaja-rooli}
        laatijat-vastaanottajaryhma-id 1]
    ;; Make old viestit read for a new laatijat.
    (when (rooli-service/laatija? new-kayttaja-whoami)
      (let [ketju-ids (mapv :id (viesti-service/find-ketjut-for-vastaanottajaryhma db laatijat-vastaanottajaryhma-id))]
        (run! #(viesti-service/read-ketju-for-newly-created-user! db new-kayttaja-whoami % new-kayttaja-id) ketju-ids)))
    new-kayttaja-id))

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
   :laskutus      -2
   :presigned     -4
   :aineisto      -5
   :expiration    -6})

(defn stamp-logout!
  "Sets kayttaja.logged_out_at = now() for the given id, revoking all of
   that user's existing sessions. Degrades gracefully (no-op, no
   exception) when id does not resolve to an existing kayttaja."
  [db id]
  (kayttaja-db/stamp-logout! db {:id id})
  nil)
