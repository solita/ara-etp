(ns solita.etp.service.perusparannuspassi
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.set :as set]
   [clojure.string :as str]
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

(defn- without-toimenpide-ehdotukset [ppp-vaihe]
  (update ppp-vaihe :toimenpiteet #(dissoc % :toimenpide-ehdotukset)))

(defn replace-abbreviation->fullname [path]
  (reduce (fn [result [fullname abbreviation]]
            (if (str/starts-with? result (str (name abbreviation) "$"))
              (reduced (str/replace-first
                        result (name abbreviation) (name fullname)))
              result))
          path db-abbreviations))

(defn to-property-name [column-name]
  (-> column-name
      db/kebab-case
      replace-abbreviation->fullname
      (str/replace "$" ".")))

(defn- find-ppp-numeric-column-validations [db versio]
  (->>
   (perusparannuspassi-db/select-ppp-numeric-validations db {:versio versio})
   (map db/kebab-case-keys)
   (map #(flat/flat->tree #"\$" %))))

(defn- find-ppp-vaihe-numeric-column-validations [db versio]
  (->>
   (perusparannuspassi-db/select-ppp-vaihe-numeric-validations db {:versio versio})
   (map db/kebab-case-keys)
   (map #(flat/flat->tree #"\$" %))))

(defn- check-ppp-value [column-name value {:keys [min max]}]
  (when (and value (or (< value min) (> value max)))
    {:property (to-property-name column-name)
     :value    value
     :min      min
     :max      max}))

(defn- check-ppp-error! [column-name value interval]
  (when-let [error (check-ppp-value column-name value interval)]
    (exception/throw-ex-info!
     (assoc error
            :type :invalid-value
            :message (str "Property: " (to-property-name column-name)
                          " has an invalid value: " value)))))

(defn- validate-ppp-db-row! [db ppp-db-row versio]
  (->> (find-ppp-numeric-column-validations db versio)
       (keep (fn [{:keys [column-name] :as validation}]
               (let [value ((-> column-name str/lower-case db/kebab-case keyword)
                            ppp-db-row)]
                 (check-ppp-error! column-name value (:error validation))
                 (check-ppp-value column-name value (:warning validation)))))
       doall))

(defn- validate-ppp-vaihe-db-row! [db ppp-vaihe-db-row versio]
  (->> (find-ppp-vaihe-numeric-column-validations db versio)
       (map db/kebab-case-keys)
       (map #(flat/flat->tree #"\$" %))
       (keep (fn [{:keys [column-name] :as validation}]
               (let [value ((-> column-name str/lower-case db/kebab-case keyword)
                            ppp-vaihe-db-row)]
                 (check-ppp-error! column-name value (:error validation))
                 (check-ppp-value column-name value (:warning validation)))))
       doall))

(defn- ->vaihe-insert-db-row [vaihe perusparannuspassi-id]
  (-> vaihe
      without-toimenpide-ehdotukset
      (assoc :perusparannuspassi-id perusparannuspassi-id)
      ppp-vaihe->db-row))

(defn- ->vaihe-update-db-row [vaihe]
  (-> vaihe
      (dissoc :vaihe-nro :perusparannuspassi-id)
      without-toimenpide-ehdotukset
      ppp-vaihe->db-row))

(defn insert-perusparannuspassi! [db whoami ppp]
  (assert-patevyystaso! whoami)
  (jdbc/with-db-transaction
    [tx db]
    (let [{:keys [versio tila-id laatija-id]}
          (perusparannuspassi-db/select-for-ppp-add-requirements tx ppp)]
      ;; This is first, to avoid leaking information about the existence of the ET.
      ;; A nil laatija-id produces the same forbidden error as a wrong laatija-id.
      (assert-correct-et-owner! whoami laatija-id)
      (when-not versio
        (exception/throw-ex-info!
         {:type    :energiatodistus-not-found
          :message (str "Energiaselvitys with id " (:energiatodistus-id ppp) " not found.")}))
      (assert-2026! versio)
      (assert-draft! tila-id)

      (let [ppp-db-row (-> ppp (dissoc :vaiheet) ppp->db-row)
            warnings (validate-ppp-db-row! tx ppp-db-row versio)
            vaihe-warnings (reduce concat
                                   (for [vaihe-db-row (->> ppp :vaiheet (map ->vaihe-update-db-row))]
                                     (validate-ppp-vaihe-db-row! tx vaihe-db-row versio)))
            {:keys [id]} (db/with-db-exception-translation
                           jdbc/insert! tx :perusparannuspassi (dissoc ppp-db-row :vaiheet)
                           db/default-opts)]

        ;; Insert vaiheet
        (doseq [vaihe (:vaiheet ppp)]
          (db/with-db-exception-translation
            jdbc/insert! tx :perusparannuspassi-vaihe (->vaihe-insert-db-row vaihe id)
            db/default-opts)

          (doseq [[ordinal toimenpide-ehdotus-id]
                  (map vector (range) (->> vaihe :toimenpiteet :toimenpide-ehdotukset (map :id)))]
            (db/with-db-exception-translation
              jdbc/insert! tx :perusparannuspassi_vaihe_toimenpide_ehdotus
              {:perusparannuspassi_id id
               :vaihe_nro             (:vaihe-nro vaihe)
               :toimenpide_ehdotus_id toimenpide-ehdotus-id
               :ordinal               ordinal}
              db/default-opts)))

        (doseq [vaihe-nro (clojure.set/difference #{1 2 3 4} (->> ppp :vaiheet (map :vaihe-nro)))]
          (jdbc/insert! tx :perusparannuspassi-vaihe
                        {:perusparannuspassi-id id
                         :vaihe-nro             vaihe-nro
                         :valid                 false}
                        db/default-opts))
        {:id       id
         :warnings (concat warnings vaihe-warnings)}))))

(defn assert-update-requirements! [db whoami current-ppp new-ppp]
  (let [{:keys [tila-id laatija-id]}
        (perusparannuspassi-db/select-for-ppp-add-requirements db current-ppp)]
    ;; This is first, to avoid leaking information about the existence of the ET.
    ;; A nil laatija-id produces the same forbidden error as a wrong laatija-id.
    (assert-correct-et-owner! whoami laatija-id)
    (assert-same-energiatodistus-id! current-ppp new-ppp)
    (assert-draft! tila-id)))

(defn update-perusparannuspassi! [db whoami id ppp]
  (assert-patevyystaso! whoami)
  (jdbc/with-db-transaction
    [tx db]
    (if-let [current-ppp (find-perusparannuspassi tx whoami id)]
      (do
        ;; This is a bit belt-and-suspenders at the moment of writing, because
        ;; find-perusparannuspassi already only looks for owned ppps.
        (assert-update-requirements! tx whoami current-ppp ppp)

        (let [ppp-db-row (-> ppp
                             (dissoc :energiatodistus-id :vaiheet)
                             ppp->db-row)
              ;; Validate PPP main row numeric values (errors throw, warnings collected)
              warnings (validate-ppp-db-row! tx ppp-db-row 2026)
              vaihe-warnings (reduce concat
                                     (for [vaihe-db-row (->> ppp :vaiheet (map ->vaihe-update-db-row))]
                                       (validate-ppp-vaihe-db-row! tx vaihe-db-row 2026)))]

          ;; Update the main PPP row
          (db/with-db-exception-translation jdbc/update! tx :perusparannuspassi
            ppp-db-row
            ["id = ?" id]
            db/default-opts)

          ;; Update the vaihe rows
          (doseq [vaihe (:vaiheet ppp)]
            (db/with-db-exception-translation jdbc/update! tx :perusparannuspassi-vaihe
              (->vaihe-update-db-row vaihe)
              ["perusparannuspassi_id = ? and vaihe_nro = ?" id (:vaihe-nro vaihe)]
              db/default-opts)
            (jdbc/delete! tx :perusparannuspassi_vaihe_toimenpide_ehdotus
                          ["perusparannuspassi_id = ? and vaihe_nro = ?" id (:vaihe-nro vaihe)]
                          db/default-opts)
            (doseq [[ordinal toimenpide-ehdotus-id]
                    (map vector (range) (->> vaihe :toimenpiteet :toimenpide-ehdotukset (map :id)))]
              (db/with-db-exception-translation
                jdbc/insert! tx :perusparannuspassi_vaihe_toimenpide_ehdotus
                {:perusparannuspassi_id id
                 :vaihe_nro             (:vaihe-nro vaihe)
                 :toimenpide_ehdotus_id toimenpide-ehdotus-id
                 :ordinal               ordinal}
                db/default-opts)))
          (doseq [vaihe-nro (clojure.set/difference #{1 2 3 4} (->> ppp :vaiheet (map :vaihe-nro)))]
            (jdbc/update! tx :perusparannuspassi-vaihe
                          {:valid false}
                          ["perusparannuspassi_id = ? and vaihe_nro = ?" id vaihe-nro]
                          db/default-opts))
          {:id       id
           :warnings (concat warnings vaihe-warnings)}))
      (exception/throw-ex-info!
       :not-found
       (str "Perusparannuspassi " id " does not exist.")))))

(defn delete-perusparannuspassi! [db whoami perusparannuspassi-id]
  (jdbc/with-db-transaction [db db]
    (let [ppp (first (perusparannuspassi-db/select-perusparannuspassi
                      db {:id perusparannuspassi-id
                          :laatija-id (:id whoami)}))]
      (when-not ppp
        (exception/throw-ex-info!
         :not-found
         (str "perusparannuspassi " perusparannuspassi-id " does not exist.")))
      ;; Soft delete PPP and its vaiheet (set valid = false)
      ;; This removes the PPP from queries that filter by valid = true
      (perusparannuspassi-db/delete-perusparannuspassi-vaiheet! db {:perusparannuspassi-id perusparannuspassi-id})
      (perusparannuspassi-db/delete-perusparannuspassi! db {:id perusparannuspassi-id})
      perusparannuspassi-id)))

(defn find-ppp-numeric-validations [db versio]
  (->> (perusparannuspassi-db/select-ppp-numeric-validations db {:versio versio})
       (map #(update % :column-name to-property-name))
       (map #(set/rename-keys % {:column-name :property}))
       (map #(flat/flat->tree #"\$" %))))

(defn find-ppp-required-properties [db versio bypass-validation]
  (map (comp to-property-name :column-name)
       (perusparannuspassi-db/select-ppp-required-columns
        db {:versio versio :bypass-validation bypass-validation})))

(defn find-ppp-vaihe-numeric-validations [db versio]
  (->> (perusparannuspassi-db/select-ppp-vaihe-numeric-validations db {:versio versio})
       (map #(update % :column-name to-property-name))
       (map #(set/rename-keys % {:column-name :property}))
       (map #(flat/flat->tree #"\$" %))))

(defn find-ppp-vaihe-required-properties [db versio bypass-validation]
  (map (comp to-property-name :column-name)
       (perusparannuspassi-db/select-ppp-vaihe-required-columns
        db {:versio versio :bypass-validation bypass-validation})))
