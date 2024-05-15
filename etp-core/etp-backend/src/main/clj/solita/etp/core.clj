(ns solita.etp.core
  (:require
    [solita.etp.common.audit-log :as audit-log]
    [solita.etp.system :as system]))

(defn add-shutdown-hook! [f]
  (.addShutdownHook (Runtime/getRuntime) (Thread. f)))

(defn -main []
  (let [system (system/start!)]
    (audit-log/info "Starting ETP backend")
    (add-shutdown-hook! #(system/halt! system))))
