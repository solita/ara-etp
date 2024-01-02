(ns solita.etp.api.response
  (:require [ring.util.response :as r]
            [clojure.string :as str])
  (:import (clojure.lang ExceptionInfo)))

(defn msg-404 [entity & ids]
  (str (str/capitalize entity) ": " (str/join "/" ids) " does not exists."))

(defn get-response [body not-found]
  (if (nil? body)
    (r/not-found not-found)
    (r/response body)))

(defn ok|not-found [updated not-found]
  (if (or (nil? updated) (= updated 0))
    (r/not-found not-found)
    (r/response nil)))

(defn- matches-description? [error error-description]
  (let [matcher (dissoc error-description :response)
        matched-error (select-keys error (keys matcher))]
    (= matcher matched-error)))

(defn with-exceptions
  "Convert exceptions defined in error-descriptions to error responses.
  If exception data matches error description then it is returned as a response.
  The http response status is defined in error description.
  These exceptions must not contain any sensitive data."
  ([response-fn error-descriptions]
   (try
     (response-fn)
     (catch ExceptionInfo e
       (let [error (ex-data e)
             description (first (filter (partial matches-description? error)
                                        error-descriptions))]
         (if (nil? description)
           (throw e)
           {:status  (:response description)
            :headers {}
            :body    error}))))))

(defn response-with-exceptions
  ([service-fn error-descriptions] (response-with-exceptions 200 service-fn error-descriptions))
  ([status service-fn error-descriptions]
   (with-exceptions
     (fn []
       {:status  status
        :headers {}
        :body    (service-fn)})
     error-descriptions)))

(defn created [path {:keys [id] :as body}]
  (r/created (str path "/" id) body))

(def unauthorized {:status 401 :body "Unauthorized"})
(def forbidden {:status 403 :body "Forbidden"})

(defn file-response-headers [content-type inline? filename]
  {"Content-Type" (or content-type "application/octet-stream")
   "Content-Disposition" (str (if inline? "inline" "attachment")
                              (str "; filename=\"" filename "\""))})

(defn csv-response-headers [filename inline?]
  (file-response-headers "text/csv" inline? filename))

;; This is only intended for asynchronous responses. Otherwise, use
;; the wrap-cache-control middleware.
(defn async-cache-headers [ttl-seconds]
  {"Cache-Control" (str "max-age=" ttl-seconds ",public")})

(defn file-response [body filename content-type inline? not-found]
  (if (nil? body)
    (r/not-found not-found)
    {:status 200
     :headers (file-response-headers content-type inline? filename)
     :body body}))

(defn csv-response [body filename not-found]
  (file-response body filename "text/csv" true not-found))

(defn pdf-response [body filename not-found]
  (file-response body filename "application/pdf" true not-found))

(defn xlsx-response [body filename not-found]
  (file-response body
                 filename
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                 false
                 not-found))

(defn conflict [body]
  {:status 409
   :body body})

(defn signature-response [result id]
  (cond
    (= result :already-signed)
    (conflict (str "Energiatodistus " id " is already signed"))

    (= result :already-in-signing)
    (conflict (str "Energiatodistus " id " is already in signing process"))

    (= result :not-in-signing)
    (conflict (str "Signing process for energiatodistus " id " has not been started"))

    (nil? result)
    (r/not-found (str "Energiatodistus " id " does not exist"))

    (= result :ok)
    (r/response "Ok")

    :else
    (r/response result)))

(defn ->xml-response [response]
  (assoc-in response [:headers "Content-Type"] "text/xml"))

(defn bad-request [body]
  {:status 400
   :headers {}
   :body body})

(defn internal-server-error [body]
  {:status 500
   :headers {}
   :body body})
