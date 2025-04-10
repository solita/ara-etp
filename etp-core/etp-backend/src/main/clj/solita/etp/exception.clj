(ns solita.etp.exception
  (:require [reitit.ring.middleware.exception :as exception]
            [reitit.coercion :as coercion]
            [clojure.tools.logging :as log]
            [solita.common.map :as map]
            [solita.common.maybe :as maybe]
            [clojure.string :as str]))

(defn illegal-argument! [msg]
  (throw (IllegalArgumentException. msg)))

(defn throw-ex-info!
  ([map]
    (throw (ex-info (:message map) map)))
  ([type message] (throw-ex-info! (map/bindings->map type message))))

(defn require-some!
  ([type id value]
   (when (nil? value)
     (throw-ex-info!
       (-> type name (str "-not-found") keyword)
       (-> type name str/capitalize (str " " id " does not exist."))))
   value))

(defn redefine-exception [operation exception-resolver]
  (try
    (operation)
    (catch Throwable t
      (let [ex (exception-resolver (ex-data t))]
        (throw (ex-info (:message ex) ex t))))))

(defn throw-forbidden!
  ([] (throw (ex-info "Forbidden" {:type :forbidden})))
  ([reason] (throw (ex-info "Forbidden" {:type :forbidden :reason reason}))))

(defn service-name [{:keys [request-method uri]}]
  (str (maybe/map* name request-method) " " uri))

(defn unique-exception-handler [exception request]
  (let [error (ex-data exception)]
    (log/info (str "Unique violation: " (name (:constraint error))
                   " in service: " (service-name request)))
    {:status  409
     :body    error}))

(defn forbidden-handler [exception request]
  (let [{:keys [reason]} (ex-data exception)]
    (log/info (str "Service " (service-name request)
                   " forbidden from identity: "
                   (get-in request [:headers "x-amzn-oidc-identity"]) ".")
              (or reason ""))
    {:status 403
     :body   "Forbidden"}))

(defn class-name [object] (.getName (class object)))

(defn exception-type [^Throwable e] (or (:type (ex-data e)) (class-name e)))

(defn default-handler
  "Default safe handler for any exception."
  [^Throwable e request]
  (do
    (log/error e "Exception in service: "
               (service-name request)
               (or (ex-data e) ""))
    {:status 500
     :body {:type (exception-type e)
            :message "Internal system error - see logs for details."}}))

(defn create-coercion-handler []
  (let [base-handler (exception/create-coercion-handler 500)]
    (fn [e request]
      (log/error e "Failed to coerce response in service: "
                 (service-name request))
      (base-handler e request))))

(def exception-middleware
  (exception/create-exception-middleware
    (assoc exception/default-handlers
      ::exception/default default-handler
      :unique-violation unique-exception-handler
      :forbidden forbidden-handler
      ::coercion/response-coercion (create-coercion-handler))))
