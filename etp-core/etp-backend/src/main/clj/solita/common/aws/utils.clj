(ns solita.common.aws.utils
  (:require [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [solita.etp.exception :as exception]))

(def anomalies->etp-codes
  {:cognitect.anomalies/forbidden   :resource-forbidden     ;http status code: 403
   :cognitect.anomalies/not-found   :resource-not-found     ;http status code: 404
   :cognitect.anomalies/busy        :resource-busy          ;http status code: 503
   :cognitect.anomalies/unavailable :resource-unavailable}) ;http status code: 504

(defn invoke
  ([client op request]
   (invoke client op request false))
  ([client op request checking-for-existence?]
   (let [result (aws/invoke client {:op      op
                                    :request request})]
     (if (contains? result :cognitect.anomalies/category)
       (let [error-code (-> result :cognitect.anomalies/category anomalies->etp-codes)]
         (do
           (when-not (and checking-for-existence? (= error-code :resource-not-found))
             (log/error "Unable to invoke aws client "
                        (merge {:op op :request request} result)))
           (exception/throw-ex-info! error-code
                                     (or (-> result :Error :Message) (:cognitect.anomalies/message result)))))
       result))))
