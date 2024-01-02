(ns solita.etp.schema.energiatodistus-history
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]))

(def AuditEvent {:modifiedby-fullname schema/Str
                 :modifytime common-schema/Instant
                 :k schema/Keyword
                 :init-v schema/Any
                 :new-v schema/Any
                 :type schema/Keyword
                 :external-api schema/Bool})

(def HistoryResponse
  {:state-history [AuditEvent]
   :form-history [AuditEvent]})

(defn- et-schema->et-history-schema [s]
  (assoc-in s [:perustiedot :rakennustunnus] (schema/maybe schema/Str)))

(def Energiatodistus2018
  (et-schema->et-history-schema energiatodistus-schema/Energiatodistus2018))
(def Energiatodistus2013
  (et-schema->et-history-schema energiatodistus-schema/Energiatodistus2013))

(def Energiatodistus
  (schema/conditional
   (partial energiatodistus-schema/versio? 2018) Energiatodistus2018
   (partial energiatodistus-schema/versio? 2013) Energiatodistus2013))
