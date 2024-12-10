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

(defn get-object-head
  ([aws-s3-client key]
   (get-object-head aws-s3-client key {:checking-for-existence? false}))
  ([{:keys [client bucket]} key options]
   (aws.utils/invoke client
                     :HeadObject
                     {:Bucket bucket
                      :Key    key}
                     options)))

(defn list-object-versions
  [{:keys [client bucket]} prefix]
  (aws.utils/invoke client
                    :ListObjectVersions
                    {:Bucket bucket
                     :Prefix prefix}))

(defn delete-object [{:keys [client bucket]} key]
  (aws.utils/invoke client
                    :DeleteObject
                    {:Bucket bucket
                     :Key    key}))

(defn get-object-tagging
  ([aws-s3-client key]
   (get-object-tagging aws-s3-client key nil))
  ([{:keys [client bucket]} key version-id]
   (let [base-params {:Bucket bucket
                      :Key    key}
         params (if version-id
                  (merge {:VersionId version-id} base-params)
                  base-params)]
     (aws.utils/invoke client
                       :GetObjectTagging
                       params))))

(defn put-object-tagging
  ([aws-s3-client key tag-set]
   (put-object-tagging aws-s3-client key tag-set nil))
  ([{:keys [client bucket]} key tag-set version-id]
   (let [base-params {:Bucket  bucket
                      :Key     key
                      :Tagging {:TagSet tag-set}}
         params (if version-id
                  (merge {:VersionId version-id} base-params)
                  base-params)]
     (aws.utils/invoke client
                       :PutObjectTagging
                       params))))

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
