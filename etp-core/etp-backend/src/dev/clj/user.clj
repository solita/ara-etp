(ns user
  (:require [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [clojure.test :as t]
            [solita.common.schema :as xschema]
            [clojure.walk :as walk]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(integrant.repl/set-prep!
 (fn []
   (require 'solita.etp.system)
   ((resolve 'solita.etp.system/config))))

(defn db
  ([] (-> integrant.repl.state/system :solita.etp/db))
  ([kayttaja-id] (assoc (db) :application-name (str kayttaja-id "@core.etp.test"))))

(defn aws-s3-client []
  (-> integrant.repl.state/system :solita.etp/aws-s3-client))

(defn run-test [var-name]
  (t/test-vars [var-name]))

(defn run-tests
  ([]
   (require 'eftest.runner)
   (-> ((resolve 'eftest.runner/find-tests) "src/test")
       ((resolve 'eftest.runner/run-tests))))
  ([config]
   (require 'eftest.runner)
   (-> ((resolve 'eftest.runner/find-tests) "src/test")
       ((resolve 'eftest.runner/run-tests) config))))

(defn run-tests-and-exit! []
  (let [{:keys [fail error]} (run-tests)]
    (System/exit (if (and (zero? fail) (zero? error)) 0 1))))

(defn run-tests-with-junit-reporter-and-exit! []
  (require 'eftest.report)
  (require 'eftest.report.junit)
  (let [{:keys [fail error]} (run-tests {:report ((resolve 'eftest.report/report-to-file)
                                                  (resolve 'eftest.report.junit/report) "target/test.xml")})]
    (System/exit (if (and (zero? fail) (zero? error)) 0 1))))

(defn- process-key [key] (if (schema/optional-key? key) (:k key) key))
(defn- type-description [type]
  (cond
    (xschema/maybe? type)
    (cond
      (-> type :schema xschema/map-literal?)
      (-> type :schema type-description)
      (-> type :schema vector?)
      (-> type :schema type-description)
      :else (str (-> type :schema type-description) "?"))
    (xschema/constrained? type)
    (cond
      (-> type :schema xschema/map-literal?)
      (-> type :schema type-description)
      (-> type :schema vector?)
      (-> type :schema type-description)
      :else (str (-> type :schema type-description) " " (:post-name type)))
    (= schema/Int type) "Integer"
    (= common-schema/Date type) "String Date"
    (vector? type) (mapv type-description type)
    (class? type) (.getSimpleName type)
    :else type))

(defn p [v] (print v) v)

(defn external-schema-documentation [schema]
  (->> schema
       (walk/postwalk
         #(if (map-entry? %)
            [(-> % first process-key) (-> % second type-description)] %))
       (walk/postwalk
         #(if (xschema/map-literal? %) (into (sorted-map) %) %))))

(defn generate-energiatodistukset-for-performance-testing!
  "Adds `count` new energiatodistukset that are sufficient for performance testing into the database."
  [count]
  (require 'solita.etp.energiatodistus-generator)
  ((resolve 'solita.etp.energiatodistus-generator/generate-and-insert-energiatodistukset-for-performance-testing!)
   (db 2) count))