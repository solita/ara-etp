(ns solita.etp.service.file
  (:require [clojure.java.io :as io]
            [solita.common.aws :as aws])
  (:import (clojure.lang ExceptionInfo)
           (java.io File FileInputStream)))

(defn file->byte-array [^File file]
  (-> file FileInputStream. .readAllBytes))

(defn upsert-file-from-bytes [aws-s3-client key bytes]
  (aws/put-object aws-s3-client key bytes))

(defn upsert-file-from-file [aws-s3-client key file]
  (upsert-file-from-bytes aws-s3-client key (file->byte-array file)))

(defn upsert-file-from-input-stream [aws-s3-client key is]
  (upsert-file-from-bytes aws-s3-client key (.readAllBytes is)))

(defn find-file [aws-s3-client key]
  (some-> (aws/get-object aws-s3-client key)
          io/input-stream))

(defn file-exists? [aws-s3-client key]
  (try
    (aws/get-object-head aws-s3-client key)
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
  (let [{:keys [UploadId]} (aws/create-multipart-upload aws-s3-client key)
        uploaded-parts-vec (atom [])
        get-next-part-number (let [current-part-number (atom 0)]
                               ;The first returned value is 1
                               (fn [] (swap! current-part-number inc)
                                 @current-part-number))
        upload-part-fn (fn [content-byte-array]
                         (let [part-number (get-next-part-number)
                               {:keys [ETag]} (aws/upload-part aws-s3-client
                                                               {:key         key
                                                                :part-number part-number
                                                                :upload-id   UploadId
                                                                :body        content-byte-array})]
                           (swap! uploaded-parts-vec conj {:ETag ETag :PartNumber (str part-number)})))]
    (upload-parts-fn upload-part-fn)
    (aws/complete-multipart-upload aws-s3-client {:key            key
                                                  :upload-id      UploadId
                                                  :uploaded-parts @uploaded-parts-vec}))
  nil)
