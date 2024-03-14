(ns solita.etp.system
  (:require [integrant.core :as ig]
            [solita.etp.aws-kms-client]
            [solita.etp.aws-s3-client]
            [solita.etp.config :as config]
            [solita.etp.db]
            [solita.etp.http-server]))

(defn config []
  (merge (config/db) (config/http-server) (config/aws-s3-client) (config/aws-kms-client)))

(defn start! []
  (ig/init (config)))

(defn halt! [system]
  (ig/halt! system))
