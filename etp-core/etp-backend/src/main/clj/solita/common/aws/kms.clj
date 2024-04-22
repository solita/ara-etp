(ns solita.common.aws.kms
  (:require [solita.common.aws.utils :as aws.utils])
  (:import (java.nio.charset StandardCharsets)
           (java.security MessageDigest)
           (java.util Base64)))

(defn sha256 [data]
  (-> (MessageDigest/getInstance "SHA-256")
      (.digest data)))

(defn sign [{:keys [client key-id]} data]
  (println "Hash is " (map (fn [it] it) data))
  (-> (aws.utils/invoke client
                        :Sign
                        {:KeyId            key-id
                         :SigningAlgorithm "RSASSA_PKCS1_V1_5_SHA_256"
                         :Message          (sha256 (.decode (Base64/getDecoder) (.getBytes data StandardCharsets/UTF_8)))
                         :MessageType      "DIGEST"
                         })
      ((fn [bytes] (println "Signature is " (map (fn [it] it) bytes))
         bytes))))

(defn verify [{:keys [client key-id]} signature data]
  (aws.utils/invoke client
                    :Verify
                    {:KeyId            key-id
                     :SigningAlgorithm "RSASSA_PKCS1_V1_5_SHA_256"
                     :Message          data
                     :Signature        signature
                     :MessageType      "DIGEST"}))
