(ns solita.etp.service.csv
  (:require [clojure.string :as str])
  (:import (java.util Locale)
           (java.text DecimalFormatSymbols DecimalFormat)
           (java.time ZoneId LocalDateTime Instant)))

(def column-separator ";")
(def locale (Locale. "fi" "FI"))
(def decimal-format-symbol (doto (DecimalFormatSymbols. locale)
                             (.setMinusSign \-)))
(def ^DecimalFormat decimal-format (doto (DecimalFormat. "#.###")
                                     (.setDecimalFormatSymbols decimal-format-symbol)))
(def timezone (ZoneId/of "Europe/Helsinki"))

(defn format-value [v]
  (cond
    (string? v) (format "\"%s\"" (str/replace v #"\"" "\"\""))
    (number? v) (.format decimal-format v)
    (= Instant (type v)) (str (LocalDateTime/ofInstant v timezone))
    :else (str v)))

(defn csv-line [coll]
  (as-> coll $
        (map format-value $)
        (str/join column-separator $)
        (str $ "\n")))
