(ns solita.etp.service.energiatodistus-validation-limits-test
  "Checks that the numeric validation limits stored in the database
   (validation_numeric_column) are consistent with the Prismatic Schema
   definitions in energiatodistus.clj.

   For every row in validation_numeric_column the DB error interval
   [error$min, error$max] must fit within the schema's allowed range.
   If the schema says NonNegative (0–9999999999) and the DB says
   error 0–50, that's fine.  But if the DB said error -1–50 for a
   NonNegative field, that would be a bug — the backend would reject
   the value at the schema level before the DB validation even runs."
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.service.energiatodistus :as et-service]
            [solita.etp.schema.energiatodistus :as et-schema])
  (:import [schema.core Maybe Constrained]))

(t/use-fixtures :each ts/fixture)

;; ---------------------------------------------------------------------------
;; Schema introspection helpers
;; ---------------------------------------------------------------------------

(defn unwrap-maybe
  "If `s` is (schema/maybe X), return X. Otherwise return s as-is."
  [s]
  (if (instance? Maybe s)
    (:schema s)
    s))

(defn extract-constrained-bounds
  "Given a Prismatic Constrained schema, try to extract [min max] from its
   postcondition name string. The common-schema types encode it as e.g.
   '[0, 1]' or '[0, max]' or 'Year'. Returns nil when the name is not
   a recognisable interval."
  [^Constrained c]
  (let [post-name (str (:post-name c))]
    (cond
      ;; "[0, max]" → NonNegative / IntNonNegative (max = 9999999999)
      (= post-name "[0, max]")
      [0 9999999999]

      ;; "[0, 1]" → Num1
      ;; "[min, max]" style
      (re-matches #"\[.*,.*\]" post-name)
      (let [[_ lo hi] (re-matches #"\[(.+),\s*(.+)\]" post-name)]
        (try
          [(Double/parseDouble lo) (Double/parseDouble hi)]
          (catch Exception _ nil)))

      ;; "Year" → 0–9999
      (= post-name "Year")
      [0 9999]

      :else nil)))

(defn schema-bounds
  "Walk into a (potentially maybe-wrapped, constrained) schema and return
   [min max] or nil."
  [s]
  (let [unwrapped (unwrap-maybe s)]
    (when (instance? Constrained unwrapped)
      (extract-constrained-bounds unwrapped))))

(defn resolve-schema-type
  "Given a save-schema (a nested map, possibly with maybe-wrapped leaves)
   and a property path like 'lahtotiedot.rakennusvaippa.ilmanvuotoluku',
   navigate into the schema tree and return the leaf type."
  [save-schema property-path]
  (let [ks (map keyword (str/split property-path #"\."))]
    (reduce
      (fn [s k]
        (when s
          (let [unwrapped (unwrap-maybe s)]
            (cond
              ;; Plain map — just look up the key
              (map? unwrapped) (get unwrapped k)
              ;; Maybe wrapping a map
              (and (instance? Maybe s) (map? (:schema s)))
              (get (:schema s) k)
              :else nil))))
      save-schema
      ks)))

(defn save-schema-for-versio
  "Return the 'raw' (pre-optional-properties) save schema for a version.
   We use the save schemas which have optional-properties already applied."
  [versio]
  (case (int versio)
    2013 et-schema/EnergiatodistusSave2013
    2018 et-schema/EnergiatodistusSave2018
    2026 et-schema/EnergiatodistusSave2026))

;; ---------------------------------------------------------------------------
;; The test
;; ---------------------------------------------------------------------------

(t/deftest db-validation-limits-fit-within-schema-constraints
  (doseq [versio [2013 2018 2026]]
    (let [db-validations (et-service/find-numeric-validations ts/*db* versio)
          save-schema    (save-schema-for-versio versio)]
      (t/testing (str "version " versio)
        (doseq [{:keys [property warning error]} db-validations]
          (let [schema-type  (resolve-schema-type save-schema property)
                bounds       (when schema-type (schema-bounds schema-type))]
            ;; Only check fields where we can extract schema bounds.
            ;; Fields backed by plain schema/Num have no bounds.
            (when bounds
              (let [[schema-min schema-max] bounds
                    err-min (:min error)
                    err-max (:max error)]
                (t/testing (str property " error [" err-min ", " err-max
                                "] vs schema [" schema-min ", " schema-max "]")
                  ;; The DB error interval must fit within the schema interval.
                  ;; i.e. schema-min <= error-min  AND  error-max <= schema-max
                  (t/is (<= schema-min err-min)
                        (str property ": DB error min " err-min
                             " is below schema min " schema-min))
                  (t/is (<= err-max schema-max)
                        (str property ": DB error max " err-max
                             " exceeds schema max " schema-max)))))))))))

(t/deftest every-db-numeric-validation-resolves-to-a-schema-property
  (doseq [versio [2013 2018 2026]]
    (let [db-validations (et-service/find-numeric-validations ts/*db* versio)
          save-schema    (save-schema-for-versio versio)]
      (t/testing (str "version " versio)
        (doseq [{:keys [property]} db-validations]
          (let [schema-type (resolve-schema-type save-schema property)]
            (t/testing (str property " should resolve to a schema type")
              (t/is (some? schema-type)
                    (str property " from DB does not exist in the "
                         versio " schema")))))))))

(t/deftest db-warning-interval-is-inside-error-interval
  (doseq [versio [2013 2018 2026]]
    (let [db-validations (et-service/find-numeric-validations ts/*db* versio)]
      (t/testing (str "version " versio)
        (doseq [{:keys [property warning error]} db-validations]
          (when (and warning error)
            (t/testing (str property " warning ⊆ error")
              (t/is (<= (:min error) (:min warning))
                    (str property ": warning min " (:min warning)
                         " < error min " (:min error)))
              (t/is (<= (:max warning) (:max error))
                    (str property ": warning max " (:max warning)
                         " > error max " (:max error))))))))))

