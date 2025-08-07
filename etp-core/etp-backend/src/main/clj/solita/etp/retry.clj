(ns solita.etp.retry
  (:require [clojure.tools.logging :as log]))

(defn run-with-retries [f retry-count op-description]
  "Attempt to run function `f` and return its value. If an exception happens,
  log it at error level and try running the function again at most `retry-count`
  times, until it succeeds. If there is no success within the retry count limit,
  throw the last exception"
  (loop [retry-count retry-count]
    (let [[res e]
          (try
            [(f) nil]
            (catch Exception e
              (log/error e "Exception in attempting to" (str op-description ":"))
              [nil e]))]

      (if e
        (if (< 0 retry-count)
          (do
            (log/info "Retrying " op-description " in 500 ms")
            (Thread/sleep 500)
            (recur (dec retry-count)))
          (throw e))
        res))))
