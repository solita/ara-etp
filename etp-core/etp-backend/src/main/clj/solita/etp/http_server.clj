(ns solita.etp.http-server
  (:require [integrant.core :as ig]
            [org.httpkit.server :as http-kit]
            [solita.etp.handler :as handler]
            [clojure.tools.logging :as log])
  (:import (com.openhtmltopdf.util XRLog)
           (com.openhtmltopdf.slf4j Slf4jLogger)))

(defn config-openhtmltopdf-logging! []
  (XRLog/setLoggerImpl (Slf4jLogger.)))

(defn wrap-ctx [ctx handler]
  (fn [req]
    (handler (merge req ctx))))

(defmethod ig/init-key :solita.etp/http-server
  [_ {:keys [ctx] :as opts}]
  (config-openhtmltopdf-logging!)
  (http-kit/run-server (wrap-ctx ctx #'handler/handler)
                       (-> opts
                           (dissoc :ctx)
                           (assoc :error-logger (fn [txt ex] (log/error ex txt)))
                           (assoc :warn-logger (fn [txt ex] (log/warn ex txt))))))

(defmethod ig/halt-key! :solita.etp/http-server
  [_ server]
  (server :timeout 100))
