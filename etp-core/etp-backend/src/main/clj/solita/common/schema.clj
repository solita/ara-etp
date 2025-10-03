(ns solita.common.schema
  (:require [schema.core :as schema]
            [schema.coerce :as coerce]
            [schema-tools.core :as schema-tools]
            [clojure.walk :as walk]
            [schema-tools.coerce :as schema-tools-coerce]
            [solita.common.map :as m])
  (:import (schema.core ConditionalSchema Constrained EnumSchema Maybe Predicate)
           (clojure.lang APersistentMap)))

(defn schema-record?
  "Tests if the parameter is schema record."
  [x]
  (and (record? x) (satisfies? schema/Schema x)))

(defn maybe? [schema]
  (instance? Maybe schema))

(defn conditional? [schema]
  (instance? ConditionalSchema schema))

(defn constrained? [schema]
  (instance? Constrained schema))

(defn predicate? [schema]
  (instance? Predicate schema))

(defn map-literal?
  "Does this schema represent a literal map object or is other map like structure e.g. record"
  [schema] (instance? APersistentMap schema))

(defn optional-key-for-maybe [schema]
  (let [convert
        (fn [[key value :as entry]]
          (if (and (keyword? key) (maybe? value))
            [(schema/optional-key key) value]
            entry))]
    (walk/postwalk
      #(if (map-entry? %) (convert %) %)
      schema)))

(defn- default-value-for-maybe [schema]
  (let [convert
        (fn [[key value :as entry]]
          (if (and (keyword? key) (maybe? value))
            [key (schema-tools/default value nil)]
            entry))]
    (walk/postwalk
      #(if (map-entry? %) (convert %) %)
      schema)))

(defn missing-maybe-values-coercer [schema]
  (schema-tools-coerce/coercer
    (default-value-for-maybe schema)
    schema-tools-coerce/default-key-matcher))

(def parse-big-decimal
  (coerce/safe #(if (string? %) (BigDecimal. ^String %) %)))

(defn- illegal-argument! [msg]
  (throw (IllegalArgumentException. msg)))

(defn optional-properties [schema]
  (if (schema-record? schema)
    (cond
      (maybe? schema) schema
      (instance? Constrained schema)
      (let [constrained-schema (optional-properties (:schema schema))]
        (if (maybe? constrained-schema)
          (schema/maybe schema)
          (assoc schema :schema constrained-schema)))
      (instance? EnumSchema schema) (schema/maybe schema)
      (instance? Predicate schema) (schema/maybe schema)
      :else (illegal-argument! (str "Unsupported schema record: " schema)))
    (cond
      (= schema schema/Bool) schema
      (class? schema) (schema/maybe schema)
      (map? schema) (m/map-values optional-properties schema)
      (vector? schema) (mapv optional-properties schema)
      (coll? schema) (map optional-properties schema)
      :else (illegal-argument! (str "Unsupported schema: " schema)))))
