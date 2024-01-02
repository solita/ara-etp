(ns solita.etp.service.json
  (:require [jsonista.core :as json]
            [schema-tools.coerce :as st-coerce]))

(def mapper (json/object-mapper {:decode-key-fn keyword}))

(defn read-value [json] (json/read-value json mapper))

(def write-value-as-string json/write-value-as-string)

(def json-coercions st-coerce/+json-coercions+)

(defn merge-data [db-row]
  (let [data (-> db-row :data .getValue read-value)]
    (merge data (dissoc db-row :data))))

(defn data-db-row [object & keep]
  (assoc (select-keys object keep)
    :data (json/write-value-as-string (apply dissoc object keep))))
