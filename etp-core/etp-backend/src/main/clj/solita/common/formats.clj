(ns solita.common.formats
  (:require [clojure.string :as str])
  (:import (java.util Locale)
           (java.text DecimalFormatSymbols)
           (java.math RoundingMode)))

(def locale (Locale. "fi" "FI"))
(def format-symbols (DecimalFormatSymbols. locale))

(defn format-number [x dp percent?]
  (when x
    (let [format        (if percent? "#.# %" "#.#")
          number-format (doto (java.text.DecimalFormat. format format-symbols)
                          (.setMinimumFractionDigits (if dp dp 0))
                          (.setMaximumFractionDigits (if dp dp Integer/MAX_VALUE))
                          (.setRoundingMode RoundingMode/HALF_UP))]
      (.format number-format (bigdec x)))))


(defn string->int [s]
  (when (-> s str/blank? not)
    (try (Integer/parseInt s)
         (catch NumberFormatException e
           nil))))