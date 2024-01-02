(ns solita.etp.schema.sivu
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(def SivuSave
  {:parent-id (schema/maybe common-schema/Key)
   :ordinal (schema/maybe schema/Int)
   :published schema/Bool
   :title schema/Str
   :body schema/Str})

(def Sivu
  (merge common-schema/Id
         (assoc SivuSave :ordinal schema/Int)))

(def SivuBrief (dissoc Sivu :body))
