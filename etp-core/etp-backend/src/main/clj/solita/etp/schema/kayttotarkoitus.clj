(ns solita.etp.schema.kayttotarkoitus
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(def Alakayttotarkoitusluokka
  (assoc common-schema/Luokittelu
         :kayttotarkoitusluokka-id common-schema/Key
         :id schema/Str))
