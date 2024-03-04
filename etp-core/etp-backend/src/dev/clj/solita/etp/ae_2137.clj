(ns solita.etp.ae-2137
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [pandect.algo.sha256 :as sha256]
            [puumerkki.codec :as codec])
  (:import (java.io ByteArrayOutputStream)))

;; Helper function to get bytes from input stream. Not sure if there would
;; be something in our existing libraries for this.
(defn get-bytes [input-stream]
  (let [output-stream (ByteArrayOutputStream. )
        buf (byte-array 1024)]
    (loop [n (.read input-stream buf)]
      (when (pos? n)
        (.write output-stream buf 0 n)
        (recur (.read input-stream buf))))
    (.toByteArray output-stream)))

;; How to sign a message with KMS
(defn example-sign! [message key-id {:keys [access-key-id secret-access-key region]}]
  (let [kms (aws/client {:api :kms
                         :region region
                         :credentials-provider (credentials/basic-credentials-provider
                                                 {:access-key-id access-key-id
                                                  :secret-access-key secret-access-key})})
        digest (sha256/sha256-bytes message)]
    (-> (aws/invoke kms {:op :Sign
                         :request {:KeyId key-id
                                   :SigningAlgorithm "RSASSA_PKCS1_V1_5_SHA_256"
                                   :MessageType "DIGEST"
                                   :Message digest}})
        (update :Signature get-bytes))))

;; How to print out a public key. Mainly for demonstrating how you
;; can decode ASN.1 using puumerkki
(defn example-get-public-key [key-id {:keys [access-key-id secret-access-key region]}]
  (let [kms (aws/client {:api :kms
                         :region region
                         :credentials-provider (credentials/basic-credentials-provider
                                                 {:access-key-id access-key-id
                                                  :secret-access-key secret-access-key})})]
    (-> (aws/invoke kms {:op :GetPublicKey
                         :request {:KeyId key-id}})
        (update :PublicKey (comp codec/asn1-decode
                                 #(map (partial bit-and 255) %)
                                 get-bytes)))))