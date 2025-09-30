(ns solita.etp.test-data.perusparannuspassi
  (:require [schema-generators.generators :as g]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]
            [solita.etp.schema.perusparannuspassi :as perusparannuspassi-schema]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]))

(def generators {schema/Str                   generators/postgresql-safe-string-generator
                 schema/Num                   (g/always 1.0M)
                 common-schema/Num1           (g/always 1.0M)
                 common-schema/NonNegative    (g/always 1.0M)
                 common-schema/IntNonNegative (g/always 1)})

(defn generate-add
  "Generates a perusparannuspassi that can be added.

  Parameters:
  - `energiatodistus-id` - ppp can only be created for an existing et"
  [energiatodistus-id]
  (generators/complete
    {:energiatodistus-id energiatodistus-id}
    perusparannuspassi-schema/PerusparannuspassiSave
    generators))

(defn generate-adds [energiatodistus-ids]
  (mapv generate-add energiatodistus-ids))

(defn insert! [perusparannuspassi-adds laatija-id]
  (mapv #(:id (perusparannuspassi-service/insert-perusparannuspassi!
                (ts/db-user laatija-id)
                {:id laatija-id}
                %)) perusparannuspassi-adds))

(defn generate-and-insert!
  ([energiatodistus-ids laatija-id]
   (let [perusparannuspassi-adds (generate-adds energiatodistus-ids)]
     (zipmap (insert! perusparannuspassi-adds laatija-id) perusparannuspassi-adds))))

