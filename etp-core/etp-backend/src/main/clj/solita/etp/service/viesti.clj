(ns solita.etp.service.viesti
  (:require [clojure.java.jdbc :as jdbc]
            [flathead.flatten :as flat]
            [solita.common.map :as map]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.valvonta :as valvonta-service]
            [solita.etp.service.file :as file-service]))

(db/require-queries 'viesti)

(def kasittelija? (some-fn rooli-service/paakayttaja? rooli-service/laskuttaja?))

(defn- add-vastaanottajat [db viestiketju-id vastaanottajat]
  (doseq [vastaanottaja-id vastaanottajat]
    (jdbc/insert! db :vastaanottaja
                  (map/bindings->map viestiketju-id vastaanottaja-id)
                  db/default-opts)))

(defn- insert-ketju! [db ketju]
  (jdbc/insert! db :viestiketju
                (select-keys ketju [:vastaanottajaryhma-id
                                    :energiatodistus-id :vo-toimenpide-id
                                    :kasitelty :kasittelija-id
                                    :subject])
                db/default-opts))

(defn- insert-viesti! [db viestiketju-id body]
  (jdbc/insert! db :viesti
                (map/bindings->map viestiketju-id body)
                db/default-opts))

(defn- assert-vastaanottajat! [whoami ketju]
  ;; laatija is only allowed to post new ketju for paakayttaja
  (when (and (rooli-service/laatija? whoami)
             (or (not= 0 (:vastaanottajaryhma-id ketju))
                 (not-empty (:vastaanottajat ketju))))
    (exception/throw-forbidden!
      (str "Laatija " (:id whoami)
           " is not allowed to post ketju: "
           (select-keys ketju [:vastaanottajaryhma-id :vastaanottajat])))))

(defn- assert-vastaanottajaryhma-or-vastaanottaja-exists! [ketju]
  (when (and (-> ketju :vastaanottajat empty?)
             (-> ketju :vastaanottajaryhma-id nil?))
    (exception/throw-ex-info!
     {:type :missing-vastaanottaja-or-vastaanottajaryhma-id
      :message "Missing either vastaanottaja or vastaanottajaryhma-id"})))

(defn default-kasittelija [whoami ketju]
  (if (and (kasittelija? whoami) (not (contains? ketju :kasittelija-id)))
    (assoc ketju :kasittelija-id (:id whoami))
    ketju))

(defn add-ketju! [db whoami ketju]
  (assert-vastaanottajat! whoami ketju)
  (assert-vastaanottajaryhma-or-vastaanottaja-exists! ketju)
  (db/with-db-exception-translation
    #(jdbc/with-db-transaction
       [tx db]
       (let [[{:keys [id]}] (insert-ketju! tx (default-kasittelija whoami ketju))]
         (insert-viesti! tx id (:body ketju))
         (viesti-db/read-ketju! tx {:viestiketju-id id})
         (when (kasittelija? whoami)
           (add-vastaanottajat tx id (:vastaanottajat ketju)))
         id))))

(defn update-ketju! [db id ketju-edit]
  (first (db/with-db-exception-translation
          jdbc/update! db :viestiketju ketju-edit ["id = ?" id] db/default-opts)))

(defn- find-viestit [db whoami viestiketju-id]
  (map #(flat/flat->tree #"\$" %)
       (viesti-db/select-viestit db {:id viestiketju-id :reader-id (:id whoami)})))

(defn- find-kayttajat [db]
  (->> db viesti-db/select-kayttajat (group-by :id) (map/map-values first)))

(defn- assoc-join-viestit [db whoami ketju]
  (assoc ketju :viestit  (find-viestit db whoami (:id ketju))))

(defn- assoc-join-vastaanottajat [kayttajat ketju]
  (->> ketju
       :vastaanottajat
       (map kayttajat (:vastaanottajat ketju))
       (assoc ketju :vastaanottajat)))

(defn builtin-vastaanottajaryhma-id [whoami]
  (case (:rooli whoami)
    0 1            ;; laatija
    2 0            ;; paakayttaja
    3 2            ;; laskuttaja
    (exception/throw-forbidden! (str "Viestit are not allowed for user: " (:id whoami)))))

(defn- visible-for? [whoami ketju]
  (or (kasittelija? whoami)
      (contains? (->> ketju :vastaanottajat (map :id) set) (:id whoami))
      (-> ketju :viestit first :from :id (= (:id whoami)))
      (= (builtin-vastaanottajaryhma-id whoami) (:vastaanottajaryhma-id ketju))))

(defn- assert-visibility! [whoami ketju]
  (if (visible-for? whoami ketju) ketju
    (exception/throw-forbidden!
      (str "Kayttaja " (:id whoami)
           " is not allowed to see viestiketju " (:id ketju)))))

(defn find-ketju! [db whoami id]
  (let [kayttajat (find-kayttajat db)]
    (->> (viesti-db/select-viestiketju db {:id id})
         (map (comp (partial assoc-join-viestit db whoami)
                 (partial assoc-join-vastaanottajat kayttajat)))
         (map #(assert-visibility! whoami %))
         first)))

(defn read-ketju! [db whoami id]
  (viesti-db/read-ketju! db {:viestiketju-id id})
  (find-ketju! db whoami id))

(defn- query-for-other-users [whoami]
  {:kayttaja-id           (:id whoami)
   :vastaanottajaryhma-id (builtin-vastaanottajaryhma-id whoami)})

(def ^:private default-filters
  {:from-id nil
   :vastaanottaja-id  nil
   :has-kasittelija   nil
   :kasittelija-id    nil
   :valvonta          nil
   :keyword           nil
   :include-kasitelty false})

(defn find-ketjut [db whoami q]
  (let [query (merge {:limit 100 :offset 0} q)
        kayttajat (find-kayttajat db)]
    (pmap (comp (partial assoc-join-viestit db whoami)
                (partial assoc-join-vastaanottajat kayttajat))
          (if (kasittelija? whoami)
            (viesti-db/select-all-viestiketjut db (merge default-filters query))
            (viesti-db/select-viestiketjut-for-kayttaja
              db (merge query (query-for-other-users whoami)))))))

(defn count-ketjut [db whoami query]
  (-> (if (kasittelija? whoami)
        (viesti-db/select-count-all-viestiketjut db (merge default-filters query))
        (viesti-db/select-count-viestiketjut-for-kayttaja
          db (query-for-other-users whoami)))
      first))

(defn count-unread-ketjut [db whoami]
  (-> (if (kasittelija? whoami)
        (viesti-db/select-count-unread-for-kasittelija
          db {:kayttaja-id (:id whoami)})
        (viesti-db/select-count-unread-for-kayttaja
          db (query-for-other-users whoami)))
      first))

(defn add-viesti! [db whoami id body]
  (when (find-ketju! db whoami id)
    (jdbc/with-db-transaction
      [tx db]
      (insert-viesti! tx id body)
      (update-ketju! tx id {:kasitelty false})
      (viesti-db/read-ketju! tx {:viestiketju-id id}))))

(defn find-vastaanottajaryhmat [db]
  (luokittelu-service/find-vastaanottajaryhmat db))

(defn find-kasittelijat [db] (valvonta-service/find-valvojat db))

(defn find-osapuolet [db] (viesti-db/select-kayttajat db))

(defn find-energiatodistus-ketjut [db whoami energiatodistus-id]
  (when-not (nil? (energiatodistus-service/find-energiatodistus
                    db whoami energiatodistus-id))
    (let [kayttajat (find-kayttajat db)]
      (pmap (comp (partial assoc-join-viestit db whoami)
                  (partial assoc-join-vastaanottajat kayttajat))
            (viesti-db/select-energiatodistus-viestiketjut
              db {:energiatodistus-id energiatodistus-id})))))

;; Vietiketjun liitteet

(defn find-liitteet [db whoami viestiketju-id]
  (find-ketju! db whoami viestiketju-id)
  (viesti-db/select-liite-by-viestiketju-id db {:viestiketju-id viestiketju-id}))

(defn file-path [viestiketju-id liite-id]
  (str "/viesti/" viestiketju-id "/liitteet/" liite-id))

(defn- insert-liite! [db liite]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :viesti_liite liite db/default-opts)
      first
      :id))

(defn add-liitteet-from-files! [db aws-s3-client viestiketju-id liitteet]
  (doseq [liite liitteet]
    (let [liite-id (insert-liite! db (-> liite
                                         liite-service/temp-file->liite
                                         (assoc :viestiketju-id viestiketju-id)))]
      (file-service/upsert-file-from-file
        aws-s3-client
        (file-path viestiketju-id liite-id)
        (:tempfile liite)))))

(defn add-liite-from-link! [db viestiketju-id liite]
  (insert-liite! db (assoc liite :contenttype "text/uri-list"
                                 :viestiketju-id viestiketju-id)))

(defn assert-owner! [db whoami liite-id error-msg]
  (if-let [owner (first (viesti-db/select-owner db {:id liite-id}))]
    (when-not (= (:modifiedby-id owner) (:id whoami))
      (exception/throw-forbidden!
        (str error-msg " User: " (:id whoami) " is not the owner of liite: " liite-id)))))

(defn delete-liite! [db whoami liite-id]
  (when-not (rooli-service/paakayttaja? whoami)
    (assert-owner! db whoami liite-id "Delete liite is not allowed."))
  (viesti-db/delete-liite! db {:id liite-id}))

(defn find-liite [db whoami aws-s3-client viestiketju-id liite-id]
  (find-ketju! db whoami viestiketju-id)
  (when-let [liite (first (viesti-db/select-liite db {:id liite-id}))]
    (when (or (not (:deleted liite)) (rooli-service/paakayttaja? whoami))
      (assoc liite :content
                   (file-service/find-file
                     aws-s3-client (file-path viestiketju-id liite-id))))))
