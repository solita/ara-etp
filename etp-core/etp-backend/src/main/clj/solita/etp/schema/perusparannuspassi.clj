(ns solita.etp.schema.perusparannuspassi
  (:require
    [schema.core :as schema]
    [solita.etp.schema.common :as common-schema]))

(def PerusparannuspassiVaihe
  {:vaihe-nro (common-schema/LimitedInt 1 4)
   :valid     schema/Bool})

(def PerusparannuspassiSave
  {:valid              schema/Bool
   :energiatodistus-id common-schema/Key
   :vaiheet            [PerusparannuspassiVaihe]})

(def Perusparannuspassi
  (merge common-schema/Id
         {:laatija-id common-schema/Key
          :tila-id    common-schema/Key}
         PerusparannuspassiSave))
