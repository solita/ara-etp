(ns solita.etp.service.energiatodistus-history
  (:require [clojure.string :as str]
            [flathead.flatten :as flat]
            [solita.etp.db :as db]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.schema.energiatodistus-history :as energiatodistus-history-schema])
  (:import (java.time Instant)))

(db/require-queries 'energiatodistus-history)

(def state-fields #{:tila-id :voimassaolo-paattymisaika :allekirjoitusaika
                    :korvaava-energiatodistus-id :laskutusaika})

(def omitted-fields #{:kommentti})

(def db-row->energiatodistus
  (energiatodistus-service/schema->db-row->energiatodistus
   energiatodistus-history-schema/Energiatodistus))

(defn audit-row->flat-energiatodistus [audit-row]
  (->> audit-row
       db-row->energiatodistus
       flat/sequence->map
       (flat/tree->flat ".")))

(defn audit-event [modifiedby-fullname modifytime k init-v new-v external-api?]
  (let [type (cond
               (string? new-v) :str
               (number? new-v) :number
               (boolean? new-v) :bool
               (instance? Instant new-v) :date
               :default :other)]
    {:modifiedby-fullname modifiedby-fullname
     :modifytime (if (and (contains? state-fields k)
                          (= type :date))
                   new-v
                   modifytime)
     :k k
     :init-v init-v
     :new-v new-v
     :type type
     :external-api external-api?}))

(defn diff [a b pred]
  (->> b
       (filter (fn [[k v]]
                 (pred (get a k) v)))
       (into {})))

(defn audit-history [{:keys [init prev] :as acc}
                     {:keys [modifiedby-fullname modifytime service-uri]
                      :as audit-row}]
  (let [flat-et (audit-row->flat-energiatodistus audit-row)]
    (if init
      (let [diff-to-prev (diff prev flat-et not=)
            reverted-keys (-> (diff init diff-to-prev =)
                              keys)
            new-history (reduce-kv (fn [acc k v]
                                     (assoc acc
                                            k
                                            (audit-event modifiedby-fullname
                                                         modifytime
                                                         k
                                                         (get init k)
                                                         v
                                                         false)))
                               {}
                               diff-to-prev)]
        (-> acc
            (assoc :prev flat-et)
            (update :state-history concat (-> new-history
                                              (select-keys state-fields)
                                              vals))
            (update :form-history merge (apply dissoc new-history state-fields))
            (update :form-history #(apply dissoc % (concat omitted-fields
                                                           reverted-keys)))))
      {:init flat-et
       :prev flat-et
       :state-history [(audit-event modifiedby-fullname
                                    modifytime
                                    :tila-id
                                    nil
                                    0
                                    (str/includes? service-uri
                                                   "/api/external/"))]
       :form-history {}})))

(defn find-audit-rows [db id]
  (energiatodistus-history-db/select-energiatodistus-audits db {:id id}))

(defn sort-by-modifytime [coll]
  (sort-by :modifytime coll))

(defn find-history [db whoami id]
  (when (energiatodistus-service/find-energiatodistus db whoami id)
    (let [audit-rows (find-audit-rows db id)]
      (as-> audit-rows $
        (reduce audit-history {} $)
        (select-keys $ [:state-history :form-history])
        (update $ :form-history (comp sort-by-modifytime vals))
        (update $ :state-history sort-by-modifytime)))))
