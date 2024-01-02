(ns solita.etp.schema.aineisto
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(def Aineisto common-schema/Luokittelu)

(def KayttajaAineisto
  {:aineisto-id common-schema/Key
   :valid-until common-schema/Instant
   :ip-address schema/Str})
