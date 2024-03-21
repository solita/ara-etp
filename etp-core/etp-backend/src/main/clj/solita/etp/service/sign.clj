(ns solita.etp.service.sign
  (:require [solita.common.aws.kms :as kms]))

(defn sign [aws-kms-client data]
  (-> (kms/sign aws-kms-client data) :Signature))

(defn verify [aws-kms-client signature data]
  (-> (kms/verify aws-kms-client signature data)) :SignatureValid)
