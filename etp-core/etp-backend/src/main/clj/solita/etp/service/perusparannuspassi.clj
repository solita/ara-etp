(ns solita.etp.service.perusparannuspassi
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.set :as set]
    [flathead.flatten :as flat]
    [solita.etp.db :as db]
    [solita.etp.exception :as exception]
    [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
    [solita.etp.service.rooli :as rooli-service]))

(db/require-queries 'perusparannuspassi)

(def db-abbreviations
  {:passin-perustiedot      :ppt
   :rakennuksen-perustiedot :rpt
   :tulokset                :t
   :toimenpiteet            :tp})

(defn tree->flat [ppp]
  (->> ppp
       (flat/tree->flat "$")))

(defn flat->tree [ppp]
  (->> ppp
       (flat/flat->tree #"\$")))

(defn ppp->db-row [ppp]
  (-> ppp
      (set/rename-keys db-abbreviations)
      tree->flat))

(defn ppp-vaihe->db-row [ppp-vaihe]
  (-> ppp-vaihe
      (set/rename-keys db-abbreviations)
      tree->flat))

(defn db-row->ppp [db-row]
  (-> db-row
      flat->tree
      (set/rename-keys (set/map-invert db-abbreviations))))

(defn db-row->ppp-vaihe [db-row]
  (-> db-row
      flat->tree
      (set/rename-keys (set/map-invert db-abbreviations))))

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

(defn- add-perusparannuspassi-vaihe-toimpide-ehdotukset [db ppp-vaihe]
  (assoc-in ppp-vaihe
            [:toimenpiteet :toimenpide-ehdotukset]
            (perusparannuspassi-db/select-perusparannuspassi-vaihe-toimenpide-ehdotukset
              db
              {:perusparannuspassi-id (:perusparannuspassi-id ppp-vaihe)
               :vaihe-nro             (:vaihe-nro ppp-vaihe)})))

(defn find-perusparannuspassi [db whoami id]
  (jdbc/with-db-transaction
    [tx db]
    (when-let [ppp (-> (perusparannuspassi-db/select-perusparannuspassi tx {:id         id
                                                                            :laatija-id (:id whoami)})
                       first
                       db-row->ppp)]
      (assoc ppp :vaiheet (->> (perusparannuspassi-db/select-perusparannuspassi-vaiheet
                                 tx
                                 {:perusparannuspassi-id (:id ppp)})
                               (map db-row->ppp-vaihe)
                               (map #(add-perusparannuspassi-vaihe-toimpide-ehdotukset tx %))
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
  (jdbc/with-db-transaction
    [tx db]
    (assert-insert-requirements! tx whoami ppp)
    (let [ppp (ppp->db-row ppp)
          {:keys [id]} (db/with-db-exception-translation
                         jdbc/insert! tx :perusparannuspassi (dissoc ppp :vaiheet)
                         db/default-opts)]

      ;; Insert
      (doseq [vaihe (:vaiheet ppp)]
        (db/with-db-exception-translation
          jdbc/insert! tx :perusparannuspassi-vaihe (-> vaihe
                                                        (update :toimenpiteet #(dissoc % :toimenpide-ehdotukset))
                                                        (assoc :perusparannuspassi-id id)
                                                        ppp-vaihe->db-row)
          db/default-opts)

        (doseq [toimenpide-ehdotus-id (->> vaihe :toimenpiteet :toimenpide-ehdotukset (map :id))]
          (db/with-db-exception-translation
            jdbc/insert! tx :perusparannuspassi_vaihe_toimenpide_ehdotus
            {:perusparannuspassi_id id
             :vaihe_nro             (:vaihe-nro vaihe)
             :toimenpide_ehdotus_id toimenpide-ehdotus-id}
            db/default-opts)))

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
