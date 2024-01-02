(ns solita.postgresql.composite-test
  (:require [clojure.test :as t]
            [solita.postgresql.composite :as pg-composite]))

(t/deftest
  write-composite-type-literals
  (t/is (= (pg-composite/write-composite-type-literals
             {:a [:a]} {:a {:a 1}}) {:a "(1)"}))
  (t/is (= (pg-composite/write-composite-type-literals
             {:a [:a]} {:b 1}) {:b 1}))
  (t/is (= (pg-composite/write-composite-type-literals
             {:a {:a [:a]}} {:a {:a {:a 1}}}) {:a {:a "(1)"}})))
