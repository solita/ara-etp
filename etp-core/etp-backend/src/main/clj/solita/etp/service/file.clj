(ns solita.etp.service.file
  (:require [clojure.java.io :as io]
            [solita.etp.exception :as exception]
            [solita.common.aws.s3 :as s3])
  (:import (clojure.lang ExceptionInfo)
           (java.io File FileInputStream)))

(defn file->byte-array [^File file]
  (with-open [stream (FileInputStream. file)]
    (.readAllBytes stream)))

(defn upsert-file-from-bytes [aws-s3-client key bytes]
  (s3/put-object aws-s3-client key bytes))

(defn upsert-file-from-file [aws-s3-client key file]
  (upsert-file-from-bytes aws-s3-client key (file->byte-array file)))

(defn upsert-file-from-input-stream [aws-s3-client key is]
  (upsert-file-from-bytes aws-s3-client key (.readAllBytes is)))

(defn delete-file [aws-s3-client key]
  (s3/delete-object aws-s3-client key))

(defn get-file-tags
  ([aws-s3-client key]
   (get-file-tags aws-s3-client key nil))
  ([aws-s3-client key version-id]
   (:TagSet (s3/get-object-tagging aws-s3-client key version-id))))

(defn get-file-tag
  ([aws-s3-client file-key tag-key]
   (get-file-tag aws-s3-client file-key tag-key nil))
  ([aws-s3-client file-key tag-key version-id]
   (let [tag-set (get-file-tags aws-s3-client file-key version-id)]
     (->> tag-set
          (filter #(= tag-key (:Key %)))
          ;; Keys returned by S3 can't be the same
          first))))

(defn put-file-tag
  ([aws-s3-client key tag]
   (put-file-tag aws-s3-client key tag nil))
  ([aws-s3-client key {:keys [Key Value]} version-id]
   (let [current-tag-set (get-file-tags aws-s3-client key version-id)
         updated-tag-set (as-> current-tag-set $
                               (remove #(= Key (:Key %)) $)
                               (conj $ {:Key Key :Value Value}))]
     (s3/put-object-tagging aws-s3-client key updated-tag-set version-id))))

(defn find-file [aws-s3-client key]
  (some-> (s3/get-object aws-s3-client key)
          io/input-stream))

(defn key->version-ids [aws-s3-client key]
  "Given a key returns the ids of its versions"
  (let [all-versions-with-key-prefix (s3/list-object-versions aws-s3-client key)]
    ;; Only max 1000 results are returned.
    (when (:IsTruncated all-versions-with-key-prefix)
      (exception/throw-ex-info! :unimplemented-exception "Implement pagination for ListObjectVersions!"))
    ;; Take only the version-ids where key is an exact match.
    (->> (:Versions all-versions-with-key-prefix)
         (filter #(= key (:Key %)))
         (map :VersionId))))

(defn file-exists? [aws-s3-client key]
  (try
    (s3/get-object-head aws-s3-client key {:checking-for-existence? true})
    true
    (catch ExceptionInfo e
      (let [{:keys [type]} (ex-data e)]
        (if (= type :resource-not-found)
          false
          (throw e))))))

(defn upsert-file-in-parts
  "Uploads a file to S3 in parts.

  Given `aws-s3-client`, `key` and a function `upload-parts-fn` uploads
  the file constructed by parts created by `upload-parts-fn` to S3.

  Does not abort failed multipart uploads. The cleaning of failed multipart
  uploads depends on the S3 bucket's policy.

  Parameters:
  - aws-s3-client: The AWS S3 Client.
  - key (string): The location inside the bucket.
  - upload-parts-fn (fn [(fn [byte-array -> nil]) -> nil]): A function that
      is provided the function to upload a single part. All the uploaded parts
      (except the last one) should be >=5MB.

  Returns:
  - nil

  Example:
  (upsert-file-in-parts
    (user/aws-s3-client)
    \"foo/bar/baz.txt\"
    (fn [upload-part]
           ;; Uploading a part of size 5MB (the minimum requirement).
           (upload-part (byte-array (repeatedly (* 5 1024 1024) #(rand-int 256))))
           ;; The last part can be less than 5MB.
           (upload-part (byte-array (repeatedly (* 2 1024 1024) #(rand-int 256))))))"
  [aws-s3-client key upload-parts-fn]
  (let [{:keys [UploadId]} (s3/create-multipart-upload aws-s3-client key)
        uploaded-parts-vec (atom [])
        get-next-part-number (let [current-part-number (atom 0)]
                               ;The first returned value is 1
                               (fn [] (swap! current-part-number inc)
                                 @current-part-number))
        upload-part-fn (fn [content-byte-array]
                         (let [part-number (get-next-part-number)
                               {:keys [ETag]} (s3/upload-part aws-s3-client
                                                              {:key         key
                                                               :part-number part-number
                                                               :upload-id   UploadId
                                                               :body        content-byte-array})]
                           (swap! uploaded-parts-vec conj {:ETag ETag :PartNumber (str part-number)})))]
    (upload-parts-fn upload-part-fn)
    (s3/complete-multipart-upload aws-s3-client {:key            key
                                                 :upload-id      UploadId
                                                 :uploaded-parts @uploaded-parts-vec}))
  nil)
