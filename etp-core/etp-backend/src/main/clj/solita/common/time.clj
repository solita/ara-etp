(ns solita.common.time
  (:import (java.time Clock ZoneId LocalDate)
           (java.time.format DateTimeFormatter)))

(def timezone (ZoneId/of "Europe/Helsinki"))
(def date-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy") timezone))

(def ^:dynamic ^Clock clock (Clock/systemDefaultZone))

(defn format-date [date]
  (when date
    (.format ^DateTimeFormatter date-formatter date)))

(defn today []
  (format-date (LocalDate/now clock)))
