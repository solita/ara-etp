(ns solita.common.maybe
  (:import (java.util Objects)))

(defn map* [fn optional] (when (some? optional) (fn optional)))

(defn filter* [predicate optional]
  (when (and (some? optional) (predicate optional)) optional))

(defn fold [default fn optional]
  (if (some? optional) (fn optional) default))

(defn lift1 [original-fn] #(map* original-fn %))

(defn require-some!
  ([value] (Objects/requireNonNull value))
  ([^String message value] (Objects/requireNonNull value message)))
