(ns solita.etp.service.laatija
  (:require [clojure.set :as set]
            [clojure.java.jdbc :as jdbc]
            [solita.common.map :as map]
            [solita.etp.exception :as exception]
            [solita.etp.db :as db]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.yritys :as yritys-service]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.service.laatija.email :as email]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

;; *** Require sql functions ***
(db/require-queries 'laatija)

(defn public-laatija [{:keys [login voimassa laatimiskielto partner julkinenpuhelin
                              julkinenemail julkinenwwwosoite julkinenosoite julkinenpostinumero]
                       :as laatija}]
  (when (and voimassa (not partner) (not laatimiskielto) (not (nil? login)))
    (select-keys laatija
                 (cond-> laatija-schema/always-public-kayttaja-laatija-ks
                   julkinenpuhelin (conj :puhelin)
                   julkinenemail (conj :email)
                   julkinenwwwosoite (conj :wwwosoite)
                   julkinenpostinumero (conj :postinumero :postitoimipaikka)
                   julkinenosoite (conj :jakeluosoite
                                        :postinumero
                                        :postitoimipaikka
                                        :maa)))))

(defn find-all-laatijat [db whoami]
  (if (rooli-service/laatija? whoami)
    (exception/throw-forbidden! "Laatija can't list all laatijas.")
    (->> (laatija-db/select-laatijat db)
         (keep (fn [laatija]
                 (cond (or (rooli-service/paakayttaja? whoami)
                           (rooli-service/laskuttaja? whoami))
                       laatija

                       (rooli-service/patevyydentoteaja? whoami)
                       (update laatija :henkilotunnus #(subs % 0 6))

                       (rooli-service/public? whoami)
                       (public-laatija laatija)))))))

(defn assert-read-access! [whoami id]
  (when-not (or (= id (:id whoami))
                (rooli-service/patevyydentoteaja? whoami)
                (rooli-service/paakayttaja? whoami)
                (rooli-service/laskuttaja? whoami))
    (exception/throw-forbidden!
      (str "User " (:id whoami)
           " is not allowed to access laatija: "
           id " information."))))

(defn find-laatija-by-id
  ([db id]
   (->> {:id id}
        (laatija-db/select-laatija-by-id db)
        first))
  ([db whoami id]
   (assert-read-access! whoami id)
   (find-laatija-by-id db id)))

(defn find-laatija-by-henkilotunnus [db henkilotunnus]
  (->> {:henkilotunnus henkilotunnus}
       (laatija-db/select-laatija-by-henkilotunnus db)
       first))

(def db-keymap {:muuttoimintaalueet :muut_toimintaalueet
                :julkinenpuhelin :julkinen_puhelin
                :julkinenemail :julkinen_email
                :julkinenosoite :julkinen_osoite
                :julkinenwwwosoite :julkinen_wwwosoite
                :julkinenpostinumero :julkinen_postinumero})

(defn- laatija->db-row [laatija]
  (-> laatija
      (set/rename-keys db-keymap)))

(defn add-laatija! [db laatija]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :laatija (laatija->db-row laatija) db/default-opts)
      first :id))

(defn update-laatija-by-id! [db id laatija]
  (jdbc/update! db :laatija
                (laatija->db-row laatija)
                ["id = ?" id] db/default-opts))

(defn validate-laatija-patevyys! [db user-id]
  (if-let [laatija (find-laatija-by-id db user-id)]
    (do
      (when-not (:voimassa laatija)
        (exception/throw-ex-info!
          {:type :patevyys-expired
           :paattymisaika (:voimassaolo-paattymisaika laatija)
           :message (str "Laatija: " user-id " pätevyys has expired.")}))
      (when (:partner laatija)
        (exception/throw-ex-info!
         :laatimiskielto (str "Laatija: " user-id " is expected to only test services.")))
      (when (:laatimiskielto laatija)
        (exception/throw-ex-info!
          :laatimiskielto (str "Laatija: " user-id " has laatimiskielto."))))
    (exception/throw-ex-info!
      :not-laatija (str "User: " user-id " is not a laatija."))))

(defn find-laatija-yritykset
  ([db whoami id]
    (assert-read-access! whoami id)
    (laatija-db/select-laatija-yritykset db {:id id})))

(defn find-laatija-laskutusosoitteet
  ([db id]
   (laatija-db/select-laatija-laskutusosoitteet db {:id id}))
  ([db whoami id]
   (assert-read-access! whoami id)
   (find-laatija-laskutusosoitteet db id)))

(defn add-laatija-yritys! [db whoami laatija-id yritys-id]
  (if (= laatija-id (:id whoami))
    (do
      (laatija-db/insert-laatija-yritys!
        db (map/bindings->map laatija-id yritys-id))
      nil)
    (exception/throw-forbidden!
      (str "User " (:id whoami) " is not allowed to add laatija: " laatija-id))))

(defn detach-laatija-yritys! [db whoami laatija-id yritys-id]
  (if (or (rooli-service/paakayttaja? whoami)
          (= laatija-id (:id whoami))
          (yritys-service/laatija-in-yritys? db (:id whoami) yritys-id))
    (laatija-db/delete-laatija-yritys!
      db (map/bindings->map laatija-id yritys-id))
    (exception/throw-forbidden!
      (str "User " (:id whoami) " is not allowed to detach laatija: " laatija-id))))

(defn count-public-laatijat [db]
  (first (laatija-db/select-count-public-laatijat db)))

(defn find-patevyystasot [db] (luokittelu-service/find-patevyystasot db))

(defn find-history [db whoami laatija-id]
  (if (or (rooli-service/paakayttaja? whoami)
          (= laatija-id (:id whoami)))
    (laatija-db/select-laatija-history db {:id laatija-id})
    (exception/throw-forbidden!)))

(def ^:private template (-> "viesti/patevyys-expiration.txt" io/resource slurp))

(defn send-patevyys-expiration-messages!
  [db {:keys [months-before-expiration dryrun]
       :or {dryrun false}
       :as   options}]
  (jdbc/with-db-transaction
    [db db]
    (let [options (merge {:dryrun false :fallback-window 10} options)
          viestit (laatija-db/insert-patevyys-expiration-viestit
                    db (assoc options
                         :subject "Muistutus pätevyyden päättymisestä"
                         :body (format template (str months-before-expiration))))]
      (log/info "Sending " (count viestit)
                " patevyys expiration messages - options: " options)

      (if dryrun
        (jdbc/db-set-rollback-only! db)
        (email/send-emails! (map #(assoc %
                                    :type :patevyys-expiration
                                    :body (str months-before-expiration))
                                 viestit)))
      viestit)))
