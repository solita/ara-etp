(ns solita.etp.service.concurrent
  (:require [solita.etp.exception-email-handler :as exception-email-handler]
            [clojure.tools.logging :as log]))

(defn safe
  "Catch all exception and log exceptions with given error description"
  [fn error-description]
  #(try
    (fn)
    (catch Throwable t
      (log/error t error-description (or (ex-data t) ""))
      (exception-email-handler/exception-handler t)
      nil)))

(defn run-background
  "Executes the given function asynchronously as a background service.
  Returns immediately nil and exceptions are only logged.
  Execution is implemented using clojure future."
  [fn error-description]
  (future-call (safe fn error-description))
  nil)

(defn retry [fn amount wait exception]
  #(loop [counter amount]
    (if (> counter 0)
      (let [value (try (fn) (catch Throwable t t))]
        (if (instance? exception value)
          (do
            (when (> wait 0) (Thread/sleep wait))
            (recur (dec counter)))
          (if (instance? Throwable value)
            (throw value)
            value))))))

(defn call! "Execute a function synchronously for its side effects"
  [fn] (fn))