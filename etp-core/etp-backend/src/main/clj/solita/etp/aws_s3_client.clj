(ns solita.etp.aws-s3-client
  (:require [integrant.core :as ig]
            [cognitect.aws.client.api :as aws]))

(defmethod ig/init-key :solita.etp/aws-s3-client
  [_ {:keys [client bucket]}]
  {:client (aws/client client)
   :bucket bucket})

(defmethod ig/halt-key! :solita.etp/aws-s3-client
  [_ aws-s3-client]
  (aws/stop (:client aws-s3-client)))
