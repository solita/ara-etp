(ns solita.etp.service.perusparannuspassi
  (:require
    [clojure.java.jdbc :as jdbc]
    [solita.etp.db :as db]
    [solita.etp.exception :as exception]
    [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
    [solita.etp.service.rooli :as rooli-service]))

(db/require-queries 'perusparannuspassi)

(defn assert-2026! [versio]
  (when (not= 2026 versio)
    (db/with-db-exception-translation
      (exception/throw-ex-info! {:type    :invalid-energiatodistus-versio
                                 :message "Perusparannuspassi can only be added to energiatodistus versio 2026."
                                 :versio  versio})
      (exception/throw-forbidden!
        "Perusparannuspassi can only be added to energiatodistus version 2026."))))

(defn assert-draft! [tila-id]
  (when (not= :draft (energiatodistus-tila/tila-key tila-id))
    (db/with-db-exception-translation
      (exception/throw-forbidden!
        "Perusparannuspassi can only be added or modified to draft energiatodistus."))))

(defn assert-correct-et-owner! [{:keys [id]} et-laatija-id]
  (when (not= et-laatija-id id)
    (exception/throw-forbidden!
      (str "User " id " is not the laatija of energiatodistus"))))

(defn assert-patevyystaso! [whoami]
  (when-not (rooli-service/ppp-laatija? whoami)
    (exception/throw-forbidden!
      (str "User " (:id whoami) " does not have the correct patevyystaso to add or modify perusparannuspassi"))))

(defn assert-same-energiatodistus-id! [current-ppp new-ppp]
  (when (not= (:energiatodistus-id current-ppp) (:energiatodistus-id new-ppp))
    (exception/throw-ex-info!
      {:type                       :invalid-energiatodistus-id
       :message                    "Cannot change energiatodistus-id of existing perusparannuspassi"
       :current-energiatodistus-id (:energiatodistus-id current-ppp)
       :new-energiatodistus-id     (:energiatodistus-id new-ppp)})))

(defn assert-vaihe-numerot! [new-ppp]
  (let [vaihe-numerot (->> (:vaiheet new-ppp)
                          (mapv :vaihe-nro))]
    (when (not (every? #{1, 2, 3, 4} vaihe-numerot))
      (exception/throw-ex-info!
        {:type             :invalid-vaihe-nro
         :message          "The only allowed numbers for vaihe-nro are 1, 2, 3 and 4"
         :given-vaihe-nros vaihe-numerot}))
    (when (not (= (count vaihe-numerot) (count (set vaihe-numerot))))
      (exception/throw-ex-info!
        {:type             :invalid-combination-of-vaihe-nro
         :message          "You can give the same vaihe-nro only once"
         :given-vaihe-nros vaihe-numerot}))))

(defn find-perusparannuspassi [db whoami id]
  (jdbc/with-db-transaction
    [tx db]
    (when-let [ppp (-> (perusparannuspassi-db/select-perusparannuspassi tx {:id         id
                                                                            :laatija-id (:id whoami)})
                       first)]
      (assoc ppp :vaiheet (->> (perusparannuspassi-db/select-perusparannuspassi-vaiheet
                                 tx
                                 {:perusparannuspassi-id (:id ppp)})
                               (map #(dissoc % :perusparannuspassi-id))
                               (into []))))))

(defn assert-insert-requirements! [db whoami ppp]
  (let [{:keys [versio tila-id laatija-id]}
        (perusparannuspassi-db/select-for-ppp-add-requirements db ppp)]
    ;; This is first, to avoid leaking information about the existence of the ET.
    ;; A nil laatija-id produces the same forbidden error as a wrong laatija-id.
    (assert-correct-et-owner! whoami laatija-id)
    (when-not versio
      (exception/throw-ex-info!
        {:type    :energiatodistus-not-found
         :message (str "Energiaselvitys with id " (:energiatodistus-id ppp) " not found.")}))
    (assert-2026! versio)
    (assert-draft! tila-id)))

(defn insert-perusparannuspassi! [db whoami ppp]
  (assert-patevyystaso! whoami)
  (assert-vaihe-numerot! ppp)
  (jdbc/with-db-transaction
    [tx db]
    (assert-insert-requirements! tx whoami ppp)
    (let [{:keys [id]} (db/with-db-exception-translation
                         jdbc/insert! tx :perusparannuspassi (dissoc ppp :vaiheet)
                         db/default-opts)]

      (doseq [vaihe (:vaiheet ppp)]
        (db/with-db-exception-translation
          jdbc/insert! tx :perusparannuspassi-vaihe (assoc vaihe :perusparannuspassi-id id)
          db/default-opts))

      (doseq [vaihe-nro (clojure.set/difference #{1 2 3 4} (->> ppp :vaiheet (map :vaihe-nro)))]
        (jdbc/insert! tx :perusparannuspassi-vaihe
                      {:perusparannuspassi-id id
                       :vaihe-nro             vaihe-nro
                       :valid                 false}
                      db/default-opts))
      {:id       id
       :warnings []})))

(defn update-perusparannuspassi! [db whoami id ppp]
  (jdbc/with-db-transaction
    [tx db]
    (if-let [current-ppp (find-perusparannuspassi tx whoami id)]
      (do
        ;; This is a bit belt-and-suspenders at the moment of writing, because
        ;; find-perusparannuspassi already only looks for owned ppps.
        (assert-correct-et-owner! whoami (:laatija-id current-ppp))

        ;; energiatodistus-id cannot be changed, even though it is an accepted
        ;; input for the initial POST
        (assert-same-energiatodistus-id! current-ppp ppp)

        ;; Only draft PPP can be modified
        (assert-draft! (:tila-id current-ppp))

        (assert-vaihe-numerot! ppp)

        (db/with-db-exception-translation jdbc/update! tx :perusparannuspassi
                                          (dissoc ppp :energiatodistus-id :vaiheet)
                                          ["id = ?" id]
                                          db/default-opts)

        (doseq [vaihe (:vaiheet ppp)]
          (db/with-db-exception-translation jdbc/update! tx :perusparannuspassi-vaihe
                                            (dissoc vaihe :vaihe-nro)
                                            ["perusparannuspassi_id = ? and vaihe_nro = ?" id (:vaihe-nro vaihe)]
                                            db/default-opts)
          )
        (doseq [vaihe-nro (clojure.set/difference #{1 2 3 4} (->> ppp :vaiheet (map :vaihe-nro)))]
          (jdbc/update! tx :perusparannuspassi-vaihe
                        {:valid false}
                        ["perusparannuspassi_id = ? and vaihe_nro = ?" id vaihe-nro]
                        db/default-opts))
        nil)
      (exception/throw-ex-info!
        :not-found
        (str "Perusparannuspassi " id " does not exist.")))))
