(ns solita.etp.test-system
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [integrant.core :as ig]
            [solita.common.aws.utils :as aws.utils]
            [solita.etp.aws-s3-client]
            [solita.etp.aws-kms-client]
            [solita.etp.config :as config]
            [solita.etp.db]
            [solita.etp.handler :as handler]))

(def ^:dynamic *db* nil)
(def ^:dynamic *admin-db* nil)
(def ^:dynamic *aws-s3-client* nil)
(def ^:dynamic *aws-kms-client* nil)

(defn db-user
  ([kayttaja-id] (db-user *db* kayttaja-id))
  ([db kayttaja-id]
   (assoc db :application-name (str kayttaja-id "@core.etp.test")))
  ([db kayttaja-id service-uri]
   (assoc db :application-name (str kayttaja-id "@core.etp.test" service-uri))))

(defn config-for-management [bucket]
  (merge (config/db {:username       (config/env "DB_MANAGEMENT_USER" "etp")
                     :password       (config/env "DB_MANAGEMENT_PASSWORD" "etp")
                     :database-name  "template1"
                     :current-schema "public"})
         (config/aws-s3-client {:bucket bucket})))

(defn config-for-in-test-management [db-name]
  (config/db {:username      (config/env "DB_MANAGEMENT_USER" "etp")
              :password      (config/env "DB_MANAGEMENT_PASSWORD" "etp")
              :database-name db-name}))

(defn config-for-tests [db-name bucket]
  (merge (config/db {:database-name            db-name
                     :re-write-batched-inserts true})
         (config/aws-s3-client {:bucket bucket})
         (config/aws-kms-client)))

(defn create-db! [db db-name]
  (jdbc/execute! (db-user db "admin")
                 [(format "CREATE DATABASE %s TEMPLATE postgres" db-name)]
                 {:transaction? false}))

(defn drop-db! [db db-name]
  (jdbc/execute! (db-user db "admin")
                 [(format "DROP DATABASE IF EXISTS %s" db-name)]
                 {:transaction? false}))

(defn create-bucket! [{:keys [client bucket]}]
  (#'aws.utils/invoke client :CreateBucket {:Bucket bucket
                                            :CreateBucketConfiguration
                                            {:LocationConstraint "eu-central-1"}})
  (#'aws.utils/invoke client :PutBucketVersioning {:Bucket bucket
                                                   :VersioningConfiguration
                                                   {:Status "Enabled"}}))

(defn drop-bucket! [{:keys [client bucket]}]
  (let [versions (->> (#'aws.utils/invoke client :ListObjectVersions {:Bucket bucket})
                  :Versions
                  (map #(select-keys % [:Key :VersionId])))
        delete-markers (->> (#'aws.utils/invoke client :ListObjectVersions {:Bucket bucket})
                            :DeleteMarkers
                            (map #(select-keys % [:Key :VersionId])))
        objects (concat versions delete-markers)]
    (when (-> objects empty? not)
      (#'aws.utils/invoke client :DeleteObjects {:Delete {:Objects objects}
                                                 :Bucket bucket}))
    (#'aws.utils/invoke client :DeleteBucket {:Bucket bucket})))

(defn- config-plain-db [config]
  (merge (select-keys config [:server-name
                              :port-number
                              :password
                              :current-schema])
         {:dbtype "postgresql"
          :dbname (:database-name config)
          :user   (:username config)}))

(defn fixture [f]
  (let [uuid (-> (java.util.UUID/randomUUID)
                 .toString
                 (str/replace "-" ""))
        db-name (str "etp_test_" uuid)
        bucket-name (str "files-" uuid)
        management-system (ig/init (config-for-management bucket-name))
        management-db (:solita.etp/db management-system)
        management-aws-s3-client (:solita.etp/aws-s3-client management-system)]
    (try
      (create-db! management-db db-name)
      (create-bucket! management-aws-s3-client)
      (let [test-system (ig/init (config-for-tests db-name bucket-name))]
        (with-bindings
          {#'*db*             (db-user (:solita.etp/db test-system) 0)
           #'*admin-db*       (config-plain-db (:solita.etp/db (config-for-in-test-management db-name)))
           #'*aws-s3-client*  (:solita.etp/aws-s3-client test-system)
           #'*aws-kms-client* (:solita.etp/aws-kms-client test-system)}
          (try (f)
               (finally (ig/halt! test-system)))))
      (finally
        (drop-db! management-db db-name)
        (drop-bucket! management-aws-s3-client)
        (ig/halt! management-system)))))

(defn handler
  "Get a handler to use with ring-mock requests to test the api"
  [req]
  ; Mimics real handler usage with test assets
  (handler/handler (merge req {:db *db* :aws-s3-client *aws-s3-client* :aws-kms-client *aws-kms-client*})))
