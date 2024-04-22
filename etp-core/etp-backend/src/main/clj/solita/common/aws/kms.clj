(ns solita.common.aws.kms
  (:require [solita.common.aws.utils :as aws.utils])
  (:import (java.security MessageDigest)))

(defn sha256 [data]
  (-> (MessageDigest/getInstance "SHA-256")
      (.digest data)))

(defn sign [{:keys [client key-id]} data]
  (aws.utils/invoke client
                    :Sign
                    {:KeyId            key-id
                     :SigningAlgorithm "RSASSA_PKCS1_V1_5_SHA_256"
                     :Message          (sha256 data)
                     :MessageType      "DIGEST"
                     }))

(defn verify [{:keys [client key-id]} signature data]
  (aws.utils/invoke client
                    :Verify
                    {:KeyId            key-id
                     :SigningAlgorithm "RSASSA_PKCS1_V1_5_SHA_256"
                     :Message          (sha256 data)
                     :Signature        signature
                     :MessageType      "DIGEST"}))
