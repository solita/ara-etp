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
   ["/start"
    {:post {:summary    "Tarkista energiatodistus ja siirrä energiatodistus allekirjoitus-tilaan"
            :parameters {:path {:id common-schema/Key}}
            :access     rooli-service/laatija?
            :responses  {200 {:body nil}
                         404 {:body schema/Str}}
            :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                          (api-response/with-exceptions
                            #(api-response/signature-response
                               (energiatodistus-service/start-energiatodistus-signing! db whoami id)
                               id)
                            [{:type :missing-value :response 400}
                             {:type :patevyys-expired :response 400}
                             {:type :laatimiskielto :response 400}]))}}]
   ["/digest/:language"
    {:get {:summary    "Hae PDF-tiedoston digest allekirjoitusta varten"
           :parameters {:path {:id       common-schema/Key
                               :language schema/Str}}
           :access     rooli-service/laatija?
           :responses  {200 {:body nil}
                        404 {:body schema/Str}}
           :handler    (fn [{{{:keys [id language]} :path} :parameters :keys [db aws-s3-client whoami]}]
                         (api-response/signature-response
                           (let [laatija-allekirjoitus-id (find-laatija-allekirjoitus-id db whoami)]
                             (energiatodistus-signing-service/find-energiatodistus-digest
                               db aws-s3-client id language laatija-allekirjoitus-id))
                           (str id "/" language)))}}]
   ["/pdf/:language"
    {:put {:summary    "Luo allekirjoitettu PDF"
           :parameters {:path {:id       common-schema/Key
                               :language schema/Str}
                        :body energiatodistus-schema/Signature}
           :access     rooli-service/laatija?
           :responses  {200 {:body nil}
                        404 {:body schema/Str}}
           :handler    (fn [{{{:keys [id language]} :path} :parameters :keys [db aws-s3-client whoami parameters]}]
                         (api-response/with-exceptions
                           #(api-response/signature-response
                              (energiatodistus-signing-service/sign-energiatodistus-pdf
                                db aws-s3-client whoami
                                (Instant/now)
                                id language
                                (:body parameters))
                              (str id "/" language))
                           [{:type :name-does-not-match :response 403}
                            {:type :signed-pdf-exists :response 409}
                            {:type :expired-signing-certificate :response 400}]))}}]
   ["/finish"
    {:post {:summary    "Siirrä energiatodistus allekirjoitettu-tilaan"
            :parameters {:path {:id common-schema/Key}}
            :access     rooli-service/laatija?
            :responses  {200 {:body nil}
                         404 {:body schema/Str}}
            :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami aws-s3-client]}]
                          (api-response/with-exceptions
                            #(api-response/signature-response
                               (energiatodistus-service/end-energiatodistus-signing! db aws-s3-client whoami id)
                               id)
                            [{:type :not-signed :response 400}]))}}]
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
