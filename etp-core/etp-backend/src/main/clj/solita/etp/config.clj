(ns solita.etp.config
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [cognitect.aws.credentials :as credentials]
            [clojure.edn :as edn]))

; use local evn credentials in codebuild and local env
; only ecs use s3
(def use-local-env-credentials?
  (not (System/getenv "FILES_BUCKET_NAME")))

(defn find-configuration
  "Return configuration value for given key. Returns first hit in order from given reader"
  ([key readers default]
   (-> (some identity (->> readers (map #(% key)))) (or default))))

(defn env
  ([name] (env name nil))
  ([name default] (or (System/getenv name) default)))

(defn property
  [name] (System/getProperty name))

(defn env-or-resource [env-name resource-path]
  (or (System/getenv env-name)
      (-> resource-path io/resource slurp)))

;;
;; For Integrant components
;;

(defn db
  ([] (db {}))
  ([opts]
   {:solita.etp/db (merge {:adapter        "postgresql"
                           :server-name    (env "DB_HOST" "localhost")
                           :port-number    (env "DB_PORT" 5432)
                           :username       (env "DB_USER" "etp_app")
                           :password       (env "DB_PASSWORD" "etp")
                           :database-name  (env "DB_DATABASE" "etp_dev")
                           :current-schema (env "DB_SCHEMA" "etp")}
                          opts)}))

(defn http-server
  ([] (http-server {}))
  ([opts]
   {:solita.etp/http-server (merge {:port     (env "HTTP_SERVER_PORT" 8080)
                                    :max-body (* 1024 1024 50)
                                    :thread   20
                                    :ctx      {:db            (ig/ref :solita.etp/db)
                                               :aws-s3-client (ig/ref :solita.etp/aws-s3-client)}}
                                   opts)}))

(defn aws-s3-client
  ([] (aws-s3-client {}))
  ([{:keys [client bucket]}]
   {:solita.etp/aws-s3-client
    {:client (merge
               {:api    :s3
                :region "eu-central-1"}
               (when use-local-env-credentials?
                 {:credentials-provider (credentials/basic-credentials-provider
                                          {:access-key-id     "minio"
                                           :secret-access-key "minio123"})
                  :endpoint-override    {:protocol :http
                                         :hostname "localhost"
                                         :port     9000}})
               client)
     :bucket (or bucket (env "FILES_BUCKET_NAME" "files"))}}))

(defn- prepare-emails [name default]
  (->> (str/split (env name default) #",")
       (map str/trim)
       (remove str/blank?)))

;;
;; Misc config
;;

(def environment-alias (env "ENVIRONMENT_ALIAS" "test"))

;; Base URLs
(def service-host (env "SERVICE_HOST" "localhost:3000"))
(def index-url (if (str/starts-with? service-host "localhost")
                 (str "https://" service-host)
                 (str "https://private." service-host)))

(def public-index-url (str "https://" service-host))

;; JWT
(def trusted-jwt-iss (env "TRUSTED_JWT_ISS" "https://raw.githubusercontent.com/solita/etp-core/develop/etp-backend/src/test/resources/"))
(def data-jwt-public-key-base-url (env "DATA_JWT_PUBLIC_KEY_BASE_URL" "https://raw.githubusercontent.com/solita/etp-core/develop/etp-backend/src/test/resources/"))

;; Logout
(def keycloak-suomifi-logout-url (env "KEYCLOAK_SUOMIFI_LOGOUT_URL" index-url))
(def keycloak-virtu-logout-url (env "KEYCLOAK_VIRTU_LOGOUT_URL" index-url))
(def cognito-logout-url (env "COGNITO_LOGOUT_URL" (str index-url "/uloskirjauduttu")))

;; Laskutus
(def laskutus-sftp-host (env "LASKUTUS_SFTP_HOST" "localhost"))
(def laskutus-sftp-port (try
                          (Integer/parseInt (env "LASKUTUS_SFTP_PORT" "2222"))
                          (catch java.lang.NumberFormatException e
                            nil)))
(def laskutus-sftp-username (env "LASKUTUS_SFTP_USERNAME" "etp"))
(def laskutus-sftp-password (env "LASKUTUS_SFTP_PASSWORD" "etp"))
(def known-hosts-path "known_hosts")
(def laskutus-tasmaytysraportti-email-to (prepare-emails "LASKUTUS_TASMAYTYSRAPORTTI_EMAIL_TO" "etp@example.com"))

;; SMTP
(def smtp-host (env "SMTP_HOST" "localhost"))
(def smtp-port (env "SMTP_PORT" "2525"))
(def smtp-username (env "SMTP_USERNAME" ""))
(def smtp-password (env "SMTP_PASSWORD" ""))

;; General email
(def email-from-email (env "EMAIL_FROM_EMAIL" "no-reply@example.com"))
(def email-from-name (env "EMAIL_FROM_NAME" "Energiatodistusrekisteri [Dev]"))
(def email-reply-to-email (env "EMAIL_REPLY_TO_EMAIL" "reply@example.com"))
(def email-reply-to-name (env "EMAIL_REPLY_TO_NAME" "Energiatodistusrekisteri [Dev]"))
(def email-exception-info (prepare-emails "EMAIL_EXCEPTION_INFO" ""))

;; Asha

(def asha-endpoint-url (env "ASHA_ENDPOINT_URL" nil))
(def asha-proxy? (edn/read-string (env "ASHA_PROXY" "false")))

;; Suomifi / viestit

(def suomifi-viestit-proxy? (edn/read-string (env "SUOMIFI_VIESTIT_PROXY" "false")))
(def suomifi-viestit-paperitoimitus? (edn/read-string (env "SUOMIFI_VIESTIT_PAPERITOIMITUS" "false")))
(def suomifi-viestit-laheta-tulostukseen? (edn/read-string (env "SUOMIFI_VIESTIT_LAHETA_TULOSTUKSEEN" "false")))
(def suomifi-viestit-endpoint-url (env "SUOMIFI_VIESTIT_ENDPOINT_URL" nil))
(def suomifi-viestit-viranomaistunnus (env "SUOMIFI_VIESTIT_VIRANOMAISTUNNUS" nil))
(def suomifi-viestit-palvelutunnus (env "SUOMIFI_VIESTIT_PALVELUTUNNUS" nil))
(def suomifi-viestit-varmenne (env "SUOMIFI_VIESTIT_VARMENNE" nil))
(def suomifi-viestit-tulostustoimittaja (env "SUOMIFI_VIESTIT_TULOSTUSTOIMITTAJA" nil))
(def suomifi-viestit-yhteyshenkilo-nimi (env "SUOMIFI_VIESTIT_YHTEYSHENKILO_NIMI" nil))
(def suomifi-viestit-yhteyshenkilo-email (env "SUOMIFI_VIESTIT_YHTEYSHENKILO_EMAIL" nil))
(def suomifi-viestit-laskutus-tunniste (env "SUOMIFI_VIESTIT_LASKUTUS_TUNNISTE" nil))
(def suomifi-viestit-laskutus-salasana (env "SUOMIFI_VIESTIT_LASKUTUS_SALASANA" nil))
(def suomifi-viestit-keystore-file (env "SUOMIFI_VIESTIT_KEYSTORE_FILE" nil))
(def suomifi-viestit-keystore-password (env "SUOMIFI_VIESTIT_KEYSTORE_PASSWORD" nil))
(def suomifi-viestit-keystore-alias (env "SUOMIFI_VIESTIT_KEYSTORE_ALIAS" nil))

;; Url signing
(def url-signing-key-id (env "URL_SIGNING_KEY_ID" "DEVENV_KEY_ID"))
(def url-signing-public-key (env-or-resource "URL_SIGNING_PUBLIC_KEY" "cf-signed-url/example.pub.pem"))
(def url-signing-private-key (env-or-resource "URL_SIGNING_PRIVATE_KEY" "cf-signed-url/example.key.pem"))

;; Feature flags
(def allow-palveluvayla-api (Boolean/parseBoolean (find-configuration "ALLOW_PALVELUVAYLA_API" [env property] "false")))
