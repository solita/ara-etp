(ns solita.common.schema-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.common.schema :as xschema]))

(defn- coerce [schema value]
  ((xschema/missing-maybe-values-coercer schema) value))

(t/deftest missing-maybe-values-coercer-test
           (t/is (= (coerce {:a schema/Str} {:a "a"}) {:a "a"}))
           (t/is (= (coerce {:a (schema/maybe schema/Str)} {}) {:a nil}))
           (t/is (= (coerce {:a [{:b (schema/maybe schema/Str)}]} {:a []})
                    {:a []}))
           (t/is (= (coerce {:a [{:b (schema/maybe schema/Str)}]} {:a [{}]})
                    {:a [{:b nil}]}))
           (t/is (= (coerce {:a [{:b (schema/maybe schema/Str)}]} {:a [{} {}]})
                    {:a [{:b nil} {:b nil}]})))

(t/deftest optional-key-for-maybe-test
  (t/is (= (xschema/optional-key-for-maybe {:a (schema/maybe schema/Str)})
           {(schema/optional-key :a) (schema/maybe schema/Str)}))
  (t/is (= (xschema/optional-key-for-maybe {:a schema/Str})
           {:a schema/Str}))
  (t/is (= (xschema/optional-key-for-maybe
             {:a schema/Str :b {:c (schema/maybe schema/Str)}})
           {:a schema/Str :b {(schema/optional-key :c)
                                       (schema/maybe schema/Str)}})))