(ns solita.etp.schema.valvonta-oikeellisuus
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [schema-tools.core :as schema-tools]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.viesti :as viesti-schema]))

(def ValvontaQuery
  {(schema/optional-key :valvoja-id) common-schema/Key
   (schema/optional-key :has-valvoja) schema/Bool
   (schema/optional-key :include-closed) schema/Bool
   (schema/optional-key :keyword) schema/Str
   (schema/optional-key :toimenpidetype-id) common-schema/Key
   (schema/optional-key :laatija-id) common-schema/Key
   (schema/optional-key :kayttotarkoitus-id) common-schema/Key})

(def ValvontaSave
  {:pending    schema/Bool
   :valvoja-id (schema/maybe common-schema/Key)})

(def Valvonta (assoc ValvontaSave
                :id common-schema/Key
                :ongoing schema/Bool))

(def Virhe
  {:description schema/Str
   :type-id     common-schema/Key})

(def Tiedoksi
  {:name schema/Str
   :email (schema/maybe common-schema/Email)})

(def ToimenpideUpdate
  (schema-tools/optional-keys
    {:deadline-date (schema/maybe common-schema/Date)
     :template-id   (schema/maybe common-schema/Key)
     :description   (schema/maybe schema/Str)
     :virheet       [Virhe]
     :severity-id   (schema/maybe common-schema/Key)
     :tiedoksi      [Tiedoksi]}))

(def ToimenpideAdd
  {:type-id       common-schema/Key
   :deadline-date (schema/maybe common-schema/Date)
   :template-id   (schema/maybe common-schema/Key)
   :description   (schema/maybe schema/Str)
   :virheet       [Virhe]
   :severity-id   (schema/maybe common-schema/Key)
   :tiedoksi      [Tiedoksi]})

(def Toimenpide
  (assoc ToimenpideAdd
    :id common-schema/Key
    :energiatodistus-id common-schema/Key
    :diaarinumero (schema/maybe schema/Str)
    :author common-schema/Kayttaja
    :create-time common-schema/Instant
    :publish-time (schema/maybe common-schema/Instant)
    :filename (schema/maybe schema/Str)))

(def Viesti
  {:from      viesti-schema/Kayttaja
   :sent-time common-schema/Instant
   :kasitelty schema/Bool})

(def ValvontaStatus
  (assoc Valvonta
    :last-toimenpide
    (schema/maybe (dissoc Toimenpide
                          :author :description :virheet :tiedoksi
                          :severity-id :filename))
    :last-viesti (schema/maybe Viesti)
    :energiatodistus energiatodistus-schema/Energiatodistus))

(def Virhetype
  (assoc common-schema/Luokittelu
    :ordinal schema/Int
    :description-fi schema/Str
    :description-sv schema/Str))

(def VirhetypeUpdate (dissoc Virhetype :id))

(def Note
  {:id          common-schema/Key
   :author-id   common-schema/Key
   :create-time common-schema/Instant
   :description schema/Str})

(def laatija? #(and (contains? % :etunimi) (contains? % :sukunimi)))
(def tiedoksi? #(and (contains? % :name)))

(def Energiatodistus+Valvonta
  (energiatodistus-schema/assoc-energiatodistus
    :valvonta (assoc ValvontaSave
                :ongoing schema/Bool
                :type-id (schema/maybe common-schema/Key))))
