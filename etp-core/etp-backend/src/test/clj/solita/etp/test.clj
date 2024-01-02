(ns solita.etp.test
  (:import (clojure.lang ExceptionInfo)))

(defn catch-ex-data [f]
  (try (f) (catch ExceptionInfo e (ex-data e))))

(defn catch-ex-data-no-msg [f]
  (dissoc (catch-ex-data f) :message))
