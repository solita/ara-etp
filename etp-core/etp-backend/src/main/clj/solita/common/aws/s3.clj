(ns solita.common.aws.s3
  (:require [solita.common.aws.utils :as aws.utils]))

(defn put-object [{:keys [client bucket]} key content]
  (aws.utils/invoke client
                    :PutObject
                    {:Bucket bucket
                     :Key    key
                     :Body   content}))

(defn get-object [{:keys [client bucket]} key]
  (when-let [result (aws.utils/invoke client
                                      :GetObject
                                      {:Bucket bucket
                                       :Key    key})]
    (:Body result)))

(defn get-object-head [{:keys [client bucket]} key]
  (aws.utils/invoke client
                    :HeadObject
                    {:Bucket bucket
                     :Key    key}))

(defn create-multipart-upload [{:keys [client bucket]} key]
  (aws.utils/invoke client
                    :CreateMultipartUpload
                    {:Bucket bucket
                     :Key    key}))

(defn upload-part [{:keys [client bucket]} {:keys [key part-number upload-id body]}]
  (aws.utils/invoke client
                    :UploadPart
                    {:Bucket     bucket
                     :Key        key
                     :UploadId   upload-id
                     :PartNumber part-number
                     :Body       body}))

(defn complete-multipart-upload [{:keys [client bucket]} {:keys [key upload-id uploaded-parts]}]
  (aws.utils/invoke client
                    :CompleteMultipartUpload
                    {:Bucket          bucket
                     :Key             key
                     :UploadId        upload-id
                     :MultipartUpload {:Parts uploaded-parts}}))
