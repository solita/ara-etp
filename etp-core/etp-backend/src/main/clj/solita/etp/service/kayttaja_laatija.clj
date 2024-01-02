(ns solita.etp.service.kayttaja-laatija
  (:require [clojure.java.jdbc :as jdbc]
            [schema-tools.core :as st]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.laatija.email :as email]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.schema.common :as common-schema]
            [flathead.flatten :as flat])
  (:import (java.time Instant)))

(defn- kayttaja->db-row [kayttaja]
  (-> kayttaja
      kayttaja-service/api-key-hash
      (dissoc :api-key :rooli)
      (->> (flat/tree->flat "$"))))

(defn- update-kayttaja! [db id kayttaja]
  (db/with-db-exception-translation
    jdbc/update!
    db
    :kayttaja
    (kayttaja->db-row kayttaja)
    ["rooli_id = 0 and id = ?" id]
    db/default-opts))

(defn- add-kayttaja! [db kayttaja]
  (kayttaja-service/add-kayttaja! db (assoc kayttaja :rooli 0)))

(defn add-laatija! [db kayttaja-laatija]
  (jdbc/with-db-transaction
    [db db]
    (let [kayttaja (st/select-schema kayttaja-laatija laatija-schema/KayttajaAdd)
          laatija (st/select-schema kayttaja-laatija laatija-schema/LaatijaUpdate)
          id (add-kayttaja! db kayttaja)]
      (laatija-service/add-laatija!
        db (assoc laatija :id id
                          :toteaja "ARA"
                          :partner true)))))

(defn- diff [new-laatija existing-laatija]
  (cond
    (not= (:patevyystaso new-laatija) (:patevyystaso existing-laatija))
    :patevyystaso
    (not= (:toteamispaivamaara new-laatija) (:toteamispaivamaara existing-laatija))
    :toteamispaivamaara
    :else :other))

(defn- upsert-kayttaja-laatija! [db {:keys [henkilotunnus] :as kayttaja-laatija}]
  "Upserts kayttaja and laatija WITHOUT transaction."
  (let [kayttaja (st/select-schema kayttaja-laatija laatija-schema/KayttajaAdd)
        laatija (st/select-schema kayttaja-laatija laatija-schema/LaatijaAdd)
        existing-laatija (laatija-service/find-laatija-by-henkilotunnus db henkilotunnus)
        id (:id existing-laatija)
        email (:email kayttaja-laatija)]
    (if existing-laatija
      (do
        (update-kayttaja! db id (dissoc kayttaja :henkilotunnus))
        (laatija-service/update-laatija-by-id! db id laatija)
        {:id id :type (diff laatija existing-laatija) :email email})
      (let [id (add-kayttaja! db kayttaja)]
        (laatija-service/add-laatija! db (assoc laatija :id id))
        {:id id :type :new :email email}))))

(defn upsert-kayttaja-laatijat! [db kayttaja-laatijat]
  (let [results (jdbc/with-db-transaction
                 [db db]
                 (mapv #(upsert-kayttaja-laatija! db %) kayttaja-laatijat))]
    (email/send-emails! results)
    (map :id results)))

(defn update-kayttaja-laatija! [db whoami id kayttaja-laatija]
  (if (or (and (= id (:id whoami))
               (common-schema/not-contains-keys
                kayttaja-laatija laatija-schema/LaatijaAdminUpdate)
               (common-schema/not-contains-keys
                kayttaja-laatija laatija-schema/KayttajaAdminUpdate))
          (rooli-service/paakayttaja? whoami))
    (let [kayttaja (st/select-schema kayttaja-laatija laatija-schema/KayttajaUpdate)
          laatija (st/select-schema kayttaja-laatija laatija-schema/LaatijaUpdate)
          verification (when (= id (:id whoami)) { :verifytime (Instant/now) })]
      (jdbc/with-db-transaction
        [db db]
        (update-kayttaja! db id (merge verification kayttaja))
        (laatija-service/update-laatija-by-id! db id laatija)))
    (exception/throw-forbidden!
      (str "User " (:id whoami) " is not allowed to update laatija " id))))
