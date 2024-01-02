(ns solita.etp.schema.geo
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(defn valid-maa? [maa] (= (count maa) 2))

(def Maa (schema/constrained schema/Str valid-maa? "country-code"))

(def Postiosoite {:jakeluosoite            schema/Str
                  :vastaanottajan-tarkenne (schema/maybe schema/Str)
                  :postinumero             schema/Str
                  :postitoimipaikka        schema/Str
                  :maa                     Maa})

(defn- valid-postinumero? [postinumero]
  (re-matches #"\d{5}" postinumero))

(def PostinumeroFI
  (schema/constrained schema/Str valid-postinumero? "FI postal code"))

(def Toimintaalue common-schema/Luokittelu)

(def Kunta (assoc common-schema/Luokittelu
                  :toimintaalue-id
                  common-schema/Key))

(def Postinumero (assoc common-schema/Luokittelu
                        :kunta-id common-schema/Key
                        :type-id (schema/maybe common-schema/Key)))

(def Country (assoc common-schema/Luokittelu :id schema/Str))

(def Search {:postinumero  Postinumero
             :kunta        Kunta
             :toimintaalue Toimintaalue})
