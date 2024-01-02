(ns solita.common.aws
  (:require [cognitect.aws.client.api :as aws]
            [clojure.tools.logging :as log]
            [solita.etp.exception :as exception]))

(def anomalies->etp-codes
  {:cognitect.anomalies/forbidden   :resource-forbidden     ;http status code: 403
   :cognitect.anomalies/not-found   :resource-not-found     ;http status code: 404
   :cognitect.anomalies/busy        :resource-busy          ;http status code: 503
   :cognitect.anomalies/unavailable :resource-unavailable}) ;http status code: 504

(defn- invoke [client op request]
  (let [result (aws/invoke client {:op      op
                                   :request request})]
    (if (contains? result :cognitect.anomalies/category)
      (do
        (log/error "Unable to invoke aws client "
                   (merge {:op op :request request} result))
        (exception/throw-ex-info! (-> result :cognitect.anomalies/category anomalies->etp-codes)
                                  (or (-> result :Error :Message) (:cognitect.anomalies/message result))))
      result)))

(defn put-object [{:keys [client bucket]} key content]
  (invoke client
          :PutObject
          {:Bucket bucket
           :Key    key
           :Body   content}))

(defn get-object [{:keys [client bucket]} key]
  (when-let [result (invoke client
                            :GetObject
                            {:Bucket bucket
                             :Key    key})]
    (:Body result)))

(defn get-object-head [{:keys [client bucket]} key]
  (invoke client
          :HeadObject
          {:Bucket bucket
           :Key    key}))

(defn create-multipart-upload [{:keys [client bucket]} key]
  (invoke client
          :CreateMultipartUpload
          {:Bucket bucket
           :Key    key}))

(defn upload-part [{:keys [client bucket]} {:keys [key part-number upload-id body]}]
  (invoke client
          :UploadPart
          {:Bucket     bucket
           :Key        key
           :UploadId   upload-id
           :PartNumber part-number
           :Body       body}))

(defn complete-multipart-upload [{:keys [client bucket]} {:keys [key upload-id uploaded-parts]}]
  (invoke client
          :CompleteMultipartUpload
          {:Bucket          bucket
           :Key             key
           :UploadId        upload-id
           :MultipartUpload {:Parts uploaded-parts}}))
