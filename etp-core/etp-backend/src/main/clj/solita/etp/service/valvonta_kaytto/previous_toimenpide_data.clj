(ns solita.etp.service.valvonta-kaytto.previous-toimenpide-data
  (:require [solita.common.time :as time]
            [solita.etp.db :as db]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide])
  (:import (java.time LocalDate)))

(db/require-queries 'previous-toimenpide-data)

(defn- format-date-values
  "Takes a map and update all its LocalDate values to Finnish date format.
   Other values are left as is."
  [data]
  (update-vals data (fn [value]
                      (if (= (type value) LocalDate)
                        (time/format-date value)
                        value))))

(defmulti find-previous-toimenpide-data
          (fn [_db toimenpide _valvonta-id] (-> toimenpide :type-id toimenpide/type-key)))

(defmethod find-previous-toimenpide-data :decision-order-hearing-letter [db _toimenpide valvonta-id]
  (previous-toimenpide-data-db/kaskypaatos-kuulemiskirje db {:valvonta-id valvonta-id}))

(defmethod find-previous-toimenpide-data :decision-order-actual-decision [db _toimenpide valvonta-id]
  (previous-toimenpide-data-db/kaskypaatos-varsinainen-paatos db {:valvonta-id valvonta-id}))

(defmethod find-previous-toimenpide-data :penalty-decision-hearing-letter [db _toimenpide valvonta-id]
  (previous-toimenpide-data-db/sakkopaatos-kuulemiskirje db {:valvonta-id valvonta-id}))

(defmethod find-previous-toimenpide-data :penalty-decision-actual-decision [db _toimenpide valvonta-id]
  (previous-toimenpide-data-db/sakkopaatos-varsinainen-paatos db {:valvonta-id valvonta-id}))

(defmethod find-previous-toimenpide-data :default [_ _toimenpide _valvonta-id]
  {})

(defn formatted-previous-toimenpide-data
  "Returns data related to the previous toimenpiteet in the process based on the current toimenpide.
   Data returned differs based on the current toimenpide. Data is formatted to be used in document templates."
  [db toimenpide valvonta-id]
  (->> (find-previous-toimenpide-data db toimenpide valvonta-id)
       first
       format-date-values))