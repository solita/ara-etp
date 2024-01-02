(ns solita.etp.schema.audit
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(def Audit
  {:modifytime common-schema/Instant
   :modifiedby-name schema/Str})
