(ns solita.etp.service.sivu
  (:require [clojure.java.jdbc :as jdbc]
            [solita.etp.db :as db]
            [solita.etp.service.rooli :as rooli-service]))

(db/require-queries 'sivu)

(defn find-all-sivut [db whoami]
  (sivu-db/select-all-sivut db {:paakayttaja (rooli-service/paakayttaja? whoami)}))

(defn find-sivu [db whoami id]
  (first (sivu-db/select-sivu db {:id id
                                  :paakayttaja (rooli-service/paakayttaja? whoami)})))

(defn default-ordinal [db id parent-id]
  (-> (sivu-db/select-child-count db {:id id :parent-id parent-id})
      first
      :count))

(defn add-sivu! [db sivu-in]
  (jdbc/with-db-transaction [db db]
    (let [sivu (if (nil? (:ordinal sivu-in))
                 (assoc sivu-in :ordinal (default-ordinal db nil (:parent-id sivu-in)))
                 sivu-in)]
      (sivu-db/bump-ordinals! db (select-keys sivu [:ordinal :parent-id]))
      (let [result (sivu-db/insert-sivu<! db sivu)]
        (sivu-db/compact-ordinals! db (select-keys sivu [:parent-id]))
        result))))

(defn delete-sivu! [db id]
  (jdbc/with-db-transaction [db db]
    (let [parent-id (-> (sivu-db/select-sivu db {:id id
                                                 :paakayttaja true})
                        first
                        :parent-id)
          result (sivu-db/delete-sivu! db {:id id})]
      (sivu-db/compact-ordinals! db {:parent-id parent-id})
      result)))

(defn update-sivu! [db id sivu-in]
  (jdbc/with-db-transaction [db db]
    (let [changing-hierarchy (or (contains? sivu-in :parent-id)
                                 (contains? sivu-in :ordinal))
          old-parent-id (-> (sivu-db/select-sivu db {:id id
                                                     :paakayttaja true})
                            first
                            :parent-id)
          new-parent-id (get sivu-in :parent-id old-parent-id)
          new-ordinal (get sivu-in :ordinal (default-ordinal db id new-parent-id))
          sivu (if changing-hierarchy
                 (assoc sivu-in :parent-id new-parent-id :ordinal new-ordinal)
                 sivu-in)

          result (do (when changing-hierarchy
                       (sivu-db/bump-ordinals! db {:parent-id new-parent-id
                                                   :ordinal new-ordinal}))
                     (first (db/with-db-exception-translation
                              jdbc/update! db :sivu sivu ["id = ?" id] db/default-opts)))]
      (when (and (not (= 0 result)) changing-hierarchy)
        (doseq [parent-id (set [old-parent-id new-parent-id])]
          (sivu-db/compact-ordinals! db {:parent-id parent-id})))
      result)))
