(ns solita.etp.schema.e-luokka
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(def ELuokka {:e-luokka schema/Str
              :kayttotarkoitus common-schema/Luokittelu
              :raja-asteikko [[(schema/one schema/Int "raja") (schema/one schema/Str "luokka")]]
              (schema/optional-key :raja-uusi-2018) schema/Int})
