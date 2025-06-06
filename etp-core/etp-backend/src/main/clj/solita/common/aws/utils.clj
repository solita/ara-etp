(ns solita.common.aws.utils
  (:require [cognitect.aws.client.api :as aws]
            [solita.etp.exception :as exception]))

;; Note that the status codes in the map below are just suggestions on what
;; to return to the client, unless you know better. There is no actual mapping
;; in place to apply these codes to the AWS errors.
(def anomalies->etp-codes
  {:cognitect.anomalies/forbidden   :resource-forbidden     ;http status code: 403
   :cognitect.anomalies/not-found   :resource-not-found     ;http status code: 404
   :cognitect.anomalies/busy        :resource-busy          ;http status code: 503
   :cognitect.anomalies/unavailable :resource-unavailable}) ;http status code: 504

(defn invoke [client op request]
  (let [result (aws/invoke client {:op      op
                                   :request request})]
    (if (contains? result :cognitect.anomalies/category)
      (exception/throw-ex-info! (-> result :cognitect.anomalies/category anomalies->etp-codes)
                                (or (-> result :Error :Message) (:cognitect.anomalies/message result)))
      result)))
