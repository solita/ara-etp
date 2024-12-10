(ns solita.etp.service.file-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.service.file :as service])
  (:import (clojure.lang ExceptionInfo)
           (java.io InputStream)))

(t/use-fixtures :each ts/fixture)

(def file-info-1 {:id "id-1"
                  :bytes (byte-array (map byte "Some text"))})

(def file-info-2 {:id "id-2"
                  :path "src/dev/clj/user.clj"
                  :bytes (-> "src/dev/clj/user.clj"
                             io/input-stream
                             .readAllBytes)})

(def file-info-3 {:id "id-3"
                  :path "deps.edn"
                  :bytes (-> "deps.edn" io/input-stream .readAllBytes)})

(t/deftest file->byte-array-test
  (doseq [file-info [file-info-2 file-info-3]]
    (t/is (= (-> file-info :path io/file service/file->byte-array type str)
             "class [B"))))

(t/deftest upsert-file-and-find-test
  (service/upsert-file-from-bytes ts/*aws-s3-client*
                                  (:id file-info-1)
                                  (:bytes file-info-1))
  (service/upsert-file-from-file ts/*aws-s3-client*
                                 (:id file-info-2)
                                 (-> file-info-2 :path io/file))
  (service/upsert-file-from-input-stream ts/*aws-s3-client*
                                         (:id file-info-3)
                                         (-> file-info-3 :path io/input-stream))
  (doseq [file-info [file-info-1 file-info-2 file-info-3]
          :let [content (service/find-file ts/*aws-s3-client* (:id file-info))]]
    (t/is (true? (instance? InputStream content)))
    (t/is (= (into [] (:bytes file-info))
             (into [] (.readAllBytes content)))))
  (t/is (thrown-with-msg? ExceptionInfo
                          #"The specified key does not exist."
                          (service/find-file ts/*aws-s3-client* "nonexisting"))))

(t/deftest rewrite-test
  (let [id (str (:id file-info-1) "-rewrite-test")]
    (service/upsert-file-from-bytes ts/*aws-s3-client*
                                    id
                                    (:bytes file-info-1))
    (service/upsert-file-from-input-stream ts/*aws-s3-client*
                                           id
                                           (-> file-info-2 :path io/input-stream))
    (let [content (service/find-file ts/*aws-s3-client* id)]
      (t/is (true? (instance? InputStream content)))
      (t/is (= (into [] (:bytes file-info-2))
               (into [] (.readAllBytes content)))))))

(t/deftest tag-file-test
  (let [id (str (:id file-info-1) "-tag-file-test")
        tag-1 {:Key "key-1" :Value "key-1-value"}
        tag-2 {:Key "key-2" :Value "key-2-value"}
        tag-1-updated {:Key "key-1" :Value "key-1-new-value"}]
    (service/upsert-file-from-bytes ts/*aws-s3-client*
                                    id
                                    (:bytes file-info-1))
    (let [tags (service/get-file-tags ts/*aws-s3-client* id)]
      (t/is (empty? tags)))
    (service/put-file-tag ts/*aws-s3-client* id tag-1)
    (let [tags (service/get-file-tags ts/*aws-s3-client* id)]
      (t/is (some #(= % tag-1) tags)))
    (service/put-file-tag ts/*aws-s3-client* id tag-2)
    (let [tags (service/get-file-tags ts/*aws-s3-client* id)]
      (t/is (some #(= % tag-1) tags))
      (t/is (some #(= % tag-2) tags)))
    (service/put-file-tag ts/*aws-s3-client* id tag-1-updated)
    (let [tags (service/get-file-tags ts/*aws-s3-client* id)]
      (t/is (nil? (some #(= % tag-1) tags)))
      (t/is (some #(= % tag-2) tags))
      (t/is (some #(= % tag-1-updated) tags)))))

(t/deftest get-file-tag-test
  (let [id (str (:id file-info-1) "-get-file-tag-test")
        tag-1 {:Key "key-1" :Value "key-1-value"}
        tag-2 {:Key "key-2" :Value "key-2-value"}]
    (service/upsert-file-from-bytes ts/*aws-s3-client*
                                    id
                                    (:bytes file-info-1))
    (t/is (nil? (service/get-file-tag ts/*aws-s3-client*
                                      id
                                      (:Key tag-1))))
    (service/put-file-tag ts/*aws-s3-client* id tag-1)
    (t/is (= tag-1 (service/get-file-tag ts/*aws-s3-client*
                                      id
                                      (:Key tag-1))))
    (service/put-file-tag ts/*aws-s3-client* id tag-2)
    (t/is (= tag-2 (service/get-file-tag ts/*aws-s3-client*
                                         id
                                         (:Key tag-2))))))

(t/deftest file-version-tag-test
  (let [key (str (:id file-info-1) "-file-version-tag-test")
        tag {:Key "key" :Value "value"}]
    ;;Upsert file twice to get two versions
    (service/upsert-file-from-bytes ts/*aws-s3-client*
                                    key
                                    (:bytes file-info-1))
    (service/upsert-file-from-bytes ts/*aws-s3-client*
                                    key
                                    (:bytes file-info-1))
    (let [[version-1 version-2] (service/key->version-ids ts/*aws-s3-client* key)]
      (service/put-file-tag ts/*aws-s3-client* key tag version-2)
      (t/is (nil? (service/get-file-tag ts/*aws-s3-client*
                                           key
                                           (:Key tag))))
      (t/is (nil? (service/get-file-tag ts/*aws-s3-client*
                                         key
                                         (:Key tag)
                                         version-1)))
      (t/is (= tag (service/get-file-tag ts/*aws-s3-client*
                                         key
                                         (:Key tag)
                                         version-2))))))

(t/deftest file->versions-test
  (t/testing "Only versions of the exact key are returned"
    (let [key-1 (str (:id file-info-1) "-versions-test")
          ;; Key with the key-1 as prefix
          key-2 (str (:id file-info-1) "-versions-test-2")]
      (service/upsert-file-from-bytes ts/*aws-s3-client*
                                      key-1
                                      (:bytes file-info-1))
      (service/upsert-file-from-bytes ts/*aws-s3-client*
                                      key-2
                                      (:bytes file-info-2))
      (t/is (= 1 (count (service/key->version-ids ts/*aws-s3-client*
                                                  key-1)))))))

(t/deftest bucket-versioning-test
  (t/testing "The test system's bucket is versioned."
    (let [id (str (:id file-info-1) "-bucket-versioning-test")]
      (service/upsert-file-from-bytes ts/*aws-s3-client*
                                      id
                                      (:bytes file-info-1))
      (t/is (= 1 (count (service/key->version-ids ts/*aws-s3-client*
                                                  id))))

      (service/delete-file ts/*aws-s3-client* id)
      (t/is (= 1 (count (service/key->version-ids ts/*aws-s3-client*
                                                  id))))
      (service/upsert-file-from-bytes ts/*aws-s3-client*
                                      id
                                      (:bytes file-info-1))
      (t/is (= 2 (count (service/key->version-ids ts/*aws-s3-client*
                                                  id))))
      (service/upsert-file-from-bytes ts/*aws-s3-client*
                                      id
                                      (:bytes file-info-1))
      (t/is (= 3 (count (service/key->version-ids ts/*aws-s3-client*
                                                  id)))))))
