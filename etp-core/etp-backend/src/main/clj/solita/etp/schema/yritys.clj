(ns solita.etp.schema.yritys
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.geo :as geo-schema]))

(def YritysSave
  "This schema is used in add-yritys and update-yritys services"
  (assoc
    geo-schema/Postiosoite
    :ytunnus                common-schema/Ytunnus
    :nimi                   schema/Str
    :verkkolaskuoperaattori (schema/maybe common-schema/Key)
    :verkkolaskuosoite      (schema/maybe common-schema/Verkkolaskuosoite)
    :laskutuskieli          (schema/enum 0 1 2)
    :type-id                common-schema/Key))

(def Yritys
  "Yritys schema contains basic information about persistent yritys"
  (assoc YritysSave
    :id common-schema/Key
    :deleted schema/Bool))

(def Verkkolaskuoperaattori
  (merge common-schema/Id {:valittajatunnus schema/Str
                           :nimi            schema/Str}))

(def Laatija
  {:id common-schema/Key
   :etunimi  schema/Str
   :sukunimi schema/Str
   :modifiedby-name schema/Str
   :modifytime common-schema/Instant
   :tila-id  common-schema/Key})
