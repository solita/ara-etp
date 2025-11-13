(ns solita.etp.schema.validation
  (:require [schema.core :as schema]))

(def Range
  {:min schema/Num
   :max schema/Num})

(def NumericValidation
  {:property schema/Str
   :warning  Range
   :error    Range})

