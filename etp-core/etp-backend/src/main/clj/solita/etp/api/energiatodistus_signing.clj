(ns solita.etp.api.energiatodistus-signing
  (:require [schema.core :as schema]
            [solita.etp.api.response :as api-response]
            [solita.etp.security :as security]
            [solita.etp.config :as config]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-signing :as energiatodistus-signing-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.rooli :as rooli-service])
  (:import (java.time Instant)))

(defn- find-laatija-allekirjoitus-id [db whoami]
  (->> whoami
       :henkilotunnus
       (laatija-service/find-laatija-by-henkilotunnus db)
       :allekirjoitus-id
       str))

(def routes
  ["/signature"
   ["/cancel"
    {:post {:summary    "Keskeytä allekirjoitus ja siirrä energiatodistus takaisin luonnokseksi"
            :parameters {:path {:id common-schema/Key}}
            :access     rooli-service/laatija?
            :responses  {200 {:body nil}
                         404 {:body schema/Str}}
            :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                          (api-response/signature-response
                            (energiatodistus-service/cancel-energiatodistus-signing! db whoami id)
                            id))}}]
   ["/system-sign"
    {:post {:summary    "Allekirjoita energiatodistus järjestelmällä"
            :parameters {:path {:id common-schema/Key}}
            :access     rooli-service/laatija?
            :middleware [[security/wrap-session-time-limit config/system-signature-session-timeout-minutes]]
            :responses  {200 {:body schema/Str}
                         404 {:body schema/Str}}
            :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db aws-s3-client aws-kms-client whoami]}]
                          (api-response/with-exceptions
                            #(api-response/signature-response
                               (energiatodistus-signing-service/sign-with-system
                                 {:db                       db
                                  :aws-s3-client            aws-s3-client
                                  :aws-kms-client           aws-kms-client
                                  :whoami                   whoami
                                  :laatija-allekirjoitus-id (find-laatija-allekirjoitus-id db whoami)
                                  :now                      (Instant/now)
                                  :id                       id})
                               id)
                            [{:type :name-does-not-match :response 403}
                             {:type :signed-pdf-exists :response 409}
                             {:type :expired-signing-certificate :response 400}
                             {:type :missing-value :response 400}
                             {:type :patevyys-expired :response 400}
                             {:type :laatimiskielto :response 400}
                             {:type :not-signed :response 400}]))}}]])
