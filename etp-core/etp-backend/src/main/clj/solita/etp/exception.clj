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

(defn- walk-coercion-errors
  "Recursively walks a schema coercion error structure and returns a flat
   sequence of {:field \"path.to.field\" :error \"error-type\"} maps."
  ([errors] (walk-coercion-errors [] errors))
  ([path errors]
   (cond
     (string? errors)
     [{:field (str/join "." (map name path))
       :error errors}]

     (map? errors)
     (mapcat (fn [[k v]]
               (walk-coercion-errors (conj path (if (keyword? k) k (keyword (str k)))) v))
             errors)

     (sequential? errors)
     (keep-indexed (fn [i v]
                     (when (some? v)
                       {:field (str (str/join "." (map name path)) "[" i "]")
                        :error (str v)}))
                   errors)

     :else
     [{:field (str/join "." (map name path))
       :error (str errors)}])))

(defn- coercion-error-message [error-str]
  (cond
    (= error-str "missing-required-key") "Pakollinen kenttä puuttuu."
    (= error-str "disallowed-key")       "Tuntematon kenttä."
    (= error-str "invalid-key")          "Virheellinen avain."
    (str/starts-with? (str error-str) "(not ") "Kentän arvo ei vastaa odotettua tyyppiä."
    :else                                (str "Validointivirhe: " error-str)))

(defn request-coercion-handler [e request]
  (let [{:keys [errors in]} (ex-data e)
        field-errors (walk-coercion-errors errors)]
    (log/info "Request coercion failed in service:"
              (service-name request)
              "location:" in
              "field-errors:" (count field-errors))
    {:status 400
     :body   {:error   "request-coercion"
              :message "Pyynnön data ei vastaa odotettua skeemaa."
              :details (mapv (fn [{:keys [field error]}]
                               {:field   field
                                :error   error
                                :message (coercion-error-message error)})
                             field-errors)}}))

(def exception-middleware
  (exception/create-exception-middleware
    (assoc exception/default-handlers
      ::exception/default default-handler
      :unique-violation unique-exception-handler
      :forbidden forbidden-handler
      :invalid-laatimisvaihe (fn [e request]
                               (log/info "Invalid laatimisvaihe in service:" (service-name request)
                                         (ex-message e))
                               {:status 422
                                :body   (ex-data e)})
      ::coercion/request-coercion request-coercion-handler
      ::coercion/response-coercion (create-coercion-handler))))
