(ns solita.etp.aws-kms-client
  (:require [cognitect.aws.client.api :as aws]
            [integrant.core :as ig]))

(defmethod ig/init-key :solita.etp/aws-kms-client
  [_ {:keys [client key-id]}]
  {:client (aws/client client)
   :key-id key-id})

(defmethod ig/halt-key! :solita.etp/aws-kms-client
  [_ aws-kms-client]
  (aws/stop (:client aws-kms-client)))
