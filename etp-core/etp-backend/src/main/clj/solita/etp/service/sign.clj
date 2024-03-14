(ns solita.etp.service.sign
  (:require [solita.common.aws :as aws]))

(defn sign [aws-kms-client data]
  (-> (aws/sign aws-kms-client data) :Signature))

(defn verify [aws-kms-client signature data]
  (-> (aws/verify aws-kms-client signature data)) :SignatureValid)
