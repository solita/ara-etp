(ns solita.etp.common.audit-log
  (:require [clojure.tools.logging :as log]))

(def audit-logger "logger.audit.signing")                          ; References logback.xml

(defn print-audit-str
  "Like print-str, but prints only message from throwables"
  [& xs]
  (->> xs
       (map (fn [x] (if (instance? Throwable x)
                      (.getMessage x)
                      x)))
       (#(with-out-str
           (apply print %)))))

(defmacro audit-log [level msg & args]
  `(log/log audit-logger ~level nil (print-audit-str ~msg ~@args)))

(defmacro info
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(audit-log :info ~@args))
