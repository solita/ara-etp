(ns solita.etp.api.stream
  (:require [clojure.tools.logging :as log]
            [solita.etp.exception :as exception]
            [org.httpkit.server :as http-kit])
  (:import (org.httpkit.server Channel)))

(defn- send! [^Channel channel headers body]
  (when-not (http-kit/send! channel {:headers headers :body body} false)
    (exception/throw-ex-info! :channel-closed "Response async channel is closed.")))

(defn result->async-channel [request response-headers result]
  (http-kit/as-channel
    request
    {:on-open
     (fn [channel]
       (future
         (try
           (result (partial send! channel response-headers))
           (catch Throwable t
             (if (some-> t ex-data :type (= :channel-closed))
               (log/info "Async channel closed in service: "
                          (exception/service-name request))
               (do
                 (log/error t "Sending response to async channel failed in service: "
                            (exception/service-name request)
                            (or (ex-data t) ""))
                 (send! channel response-headers
                        (str "ERROR: " (exception/exception-type t))))))
           (finally
             (http-kit/close channel)))))}))