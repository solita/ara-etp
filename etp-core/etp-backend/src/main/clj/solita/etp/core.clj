(ns solita.etp.core
  (:require [solita.etp.system :as system]))

(defn add-shutdown-hook! [f]
  (.addShutdownHook (Runtime/getRuntime) (Thread. f)))

(defn -main []
  (let [system (system/start!)]
    (add-shutdown-hook! #(system/halt! system))))
