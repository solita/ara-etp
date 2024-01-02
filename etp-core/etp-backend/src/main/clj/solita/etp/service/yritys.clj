(ns solita.etp.service.yritys
  (:require [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.laatija-yritys :as laatija-yritys]
            [solita.etp.service.luokittelu :as luokittelu]
            [solita.etp.service.rooli :as rooli-service]
            [solita.common.map :as map]
            [clojure.java.jdbc :as jdbc]))

; *** Require sql functions ***
(db/require-queries 'yritys)

(defn find-yritys [db id]
  (first (yritys-db/select-yritys db {:id id})))

(defn find-all-yritykset [db]
  (yritys-db/select-all-yritykset db))

(defn- laatija-in-yritys-laatijat? [laatija-id yritys-laatijat]
  (some #(= laatija-id (:id %))
        (filter laatija-yritys/accepted? yritys-laatijat)))

(defn- assert-permission! [whoami yritys-id yritys-laatijat]
  (if (or (rooli-service/paakayttaja? whoami)
          (rooli-service/laskuttaja? whoami)
          (laatija-in-yritys-laatijat? (:id whoami) yritys-laatijat))
    yritys-laatijat
    (exception/throw-forbidden!
      (str "User " (:id whoami) " is not paakayttaja or "
           "does not belong to organization: " yritys-id))))

(defn find-laatijat
  ([db id] (yritys-db/select-laatijat db {:id id}))
  ([db whoami id] (assert-permission! whoami id (find-laatijat db id))))

(defn- assert-not-partner! [db laatija-id]
  (let [{:keys [partner]} (first (yritys-db/select-laatija-by-id db {:id laatija-id}))]
    (when partner
      (exception/throw-ex-info!
        :partner-not-allowed
        (str "Partner user " laatija-id " is not allowed for yritys")))))

(defn db-assert-permission! [db whoami id] (find-laatijat db whoami id))

(defn find-all-laskutuskielet [db]
  (yritys-db/select-all-laskutuskielet db))

(defn find-all-verkkolaskuoperaattorit [db]
  (yritys-db/select-all-verkkolaskuoperaattorit db))

(def find-all-yritystyypit luokittelu/find-yritystypes)

(defn laatija-in-yritys? [db laatija-id yritys-id]
  (laatija-in-yritys-laatijat? laatija-id (find-laatijat db yritys-id)))

(defn update-yritys!
  [db whoami id yritys]
  (db-assert-permission! db whoami id)
  (yritys-db/update-yritys! db (assoc yritys :id id)))

(defn add-laatija-yritys!
  ([db whoami laatija-id yritys-id]
   (db-assert-permission! db whoami yritys-id)
   (add-laatija-yritys! db laatija-id yritys-id))
  ([db laatija-id yritys-id]
   (assert-not-partner! db laatija-id)
   (yritys-db/insert-laatija-yritys!
     db (map/bindings->map laatija-id yritys-id))
   nil))

(defn add-yritys!
  ([db whoami yritys]
   (jdbc/with-db-transaction
     [db db]
     (let [id (:id (yritys-db/insert-yritys<! db yritys))]
       (add-laatija-yritys! db (:id whoami) id)
       id))))

(defn set-yritys-deleted! [db whoami id deleted]
  (db-assert-permission! db whoami id)
  (yritys-db/update-yritys-deleted! db {:id id :deleted deleted}))
