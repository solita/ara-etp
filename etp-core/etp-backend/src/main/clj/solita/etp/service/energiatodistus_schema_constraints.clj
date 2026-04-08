(ns solita.etp.service.energiatodistus-schema-constraints
  "Walks the Prismatic Schema for an energiatodistus version and extracts
   every leaf field's type constraints (string max-length, numeric min/max).

   This is exposed via an internal API endpoint so that e2e tests can
   compare backend schema limits against the frontend's hardcoded limits."
  (:require [clojure.string :as str]
            [schema.core :as schema]
            [solita.etp.schema.energiatodistus :as et-schema])
  (:import [schema.core Maybe Constrained EqSchema EnumSchema Predicate]))

;; ---------------------------------------------------------------------------
;; Schema introspection
;; ---------------------------------------------------------------------------

(defn- unwrap-maybe [s]
  (if (instance? Maybe s)
    (:schema s)
    s))

(defn- constrained-name [^Constrained c]
  (str (:post-name c)))

(defn- extract-string-max
  "StringBase schemas are (constrained Str #(<= 1 (count %) max) \"[1, max]\")."
  [^Constrained c]
  (let [n (constrained-name c)]
    (when (= (:schema c) schema/Str)
      (when-let [[_ lo hi] (re-matches #"\[(\d+),\s*(\d+)\]" n)]
        {:type       "string"
         :min-length (Long/parseLong lo)
         :max-length (Long/parseLong hi)}))))

(defn- extract-numeric-range
  "LimitedNum schemas are (constrained Num/Int #(<= lo % hi) \"[lo, hi]\")
   or (constrained Num/Int #(<= lo % hi) \"[0, max]\") for NonNegative."
  [^Constrained c]
  (let [n        (constrained-name c)
        num-type (cond
                   (= (:schema c) schema/Num) "number"
                   (= (:schema c) schema/Int) "integer"
                   :else nil)]
    (when num-type
      (cond
        (= n "[0, max]")
        {:type "number" :numeric-type num-type :min 0 :max 9999999999}

        (= n "Year")
        {:type "number" :numeric-type "integer" :min 0 :max 9999}

        :else
        (when-let [[_ lo hi] (re-matches #"\[(.+),\s*(.+)\]" n)]
          (try
            {:type         "number"
             :numeric-type num-type
             :min          (Double/parseDouble lo)
             :max          (Double/parseDouble hi)}
            (catch Exception _ nil)))))))

(defn- classify-leaf
  "Return a constraint map for a leaf schema type, or nil if unrecognised."
  [s]
  (let [raw (unwrap-maybe s)]
    (cond
      ;; Constrained — could be string or numeric
      (instance? Constrained raw)
      (or (extract-string-max raw)
          (extract-numeric-range raw))

      ;; Raw Str — unbounded string
      (= raw schema/Str)
      {:type "string"}

      ;; Raw Num / Int — unbounded numeric
      (= raw schema/Num)
      {:type "number" :numeric-type "number"}
      (= raw schema/Int)
      {:type "number" :numeric-type "integer"}

      ;; Bool — skip
      (= raw schema/Bool) nil

      ;; Date class — skip
      (= raw java.time.LocalDate) nil
      (= raw java.time.Instant) nil

      ;; EqSchema, EnumSchema, Predicate — skip
      (instance? EqSchema raw) nil
      (instance? EnumSchema raw) nil
      (instance? Predicate raw) nil

      :else nil)))

(defn- schema-key->keyword
  "Handle both plain keywords and schema/optional-key wrappers."
  [k]
  (cond
    (keyword? k) k
    (instance? schema.core.OptionalKey k) (:k k)
    :else nil))

;; ---------------------------------------------------------------------------
;; Tree walker
;; ---------------------------------------------------------------------------

(defn- walk-schema
  "Recursively walk a Prismatic Schema, collecting leaf constraints.
   Returns a seq of {:property \"dotted.path\" :constraint {...}}."
  [prefix s]
  (let [raw (unwrap-maybe s)]
    (cond
      ;; Schema records (Constrained, EqSchema, etc.) are Clojure maps,
      ;; so we must check for leaf types *before* (map? raw).
      (instance? Constrained raw)
      (when-let [constraint (classify-leaf s)]
        [{:property prefix :constraint constraint}])

      (or (instance? EqSchema raw)
          (instance? EnumSchema raw)
          (instance? Predicate raw))
      nil

      ;; Plain map → recurse into values
      (map? raw)
      (mapcat
        (fn [[k v]]
          (when-let [kw (schema-key->keyword k)]
            (let [child-prefix (if (str/blank? prefix)
                                 (name kw)
                                 (str prefix "." (name kw)))]
              (walk-schema child-prefix v))))
        raw)

      ;; Vector of maps (e.g. toimenpide) → recurse into first element
      ;; We emit the constraints with [*] suffix to indicate array elements
      (and (vector? raw) (= 1 (count raw)) (map? (first raw)))
      (walk-schema (str prefix "[*]") (first raw))

      ;; Leaf — try to classify
      :else
      (when-let [constraint (classify-leaf s)]
        [{:property   prefix
          :constraint constraint}]))))

;; ---------------------------------------------------------------------------
;; Public API
;; ---------------------------------------------------------------------------

(defn save-schema-for-versio [versio]
  (case (int versio)
    2013 et-schema/EnergiatodistusSave2013
    2018 et-schema/EnergiatodistusSave2018
    2026 et-schema/EnergiatodistusSave2026))

(defn schema-constraints
  "Return all leaf-level constraints for the given energiatodistus version.
   Each entry is {:property \"dotted.path\" :constraint {:type ... :min ... :max ...}}."
  [versio]
  (->> (walk-schema "" (save-schema-for-versio versio))
       (sort-by :property)
       vec))

