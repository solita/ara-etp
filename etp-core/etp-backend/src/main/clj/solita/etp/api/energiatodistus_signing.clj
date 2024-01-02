(ns solita.etp.api.energiatodistus-signing
  (:require [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.api.response :as api-response]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.schema.common :as common-schema]
            [schema.core :as schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema])
  (:import (java.time Instant)))

(def routes
  ["/signature"
   ["/start"
    {:post {:summary "Tarkista energiatodistus ja siirrä energiatodistus allekirjoitus-tilaan"
            :parameters {:path {:id common-schema/Key}}
            :access rooli-service/laatija?
            :responses {200 {:body nil}
                        404 {:body schema/Str}}
            :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                       (api-response/with-exceptions
                         #(api-response/signature-response
                           (energiatodistus-service/start-energiatodistus-signing! db whoami id)
                           id)
                         [{:type :missing-value :response 400}
                          {:type :patevyys-expired :response 400}
                          {:type :laatimiskielto :response 400}]))}}]
   ["/digest/:language"
    {:get {:summary "Hae PDF-tiedoston digest allekirjoitusta varten"
           :parameters {:path {:id common-schema/Key
                               :language schema/Str}}
           :access rooli-service/laatija?
           :responses {200 {:body nil}
                       404 {:body schema/Str}}
           :handler (fn [{{{:keys [id language]} :path} :parameters :keys [db aws-s3-client]}]
                      (api-response/signature-response
                       (energiatodistus-pdf-service/find-energiatodistus-digest
                        db aws-s3-client id language)
                       (str id "/" language)))}}]
   ["/pdf/:language"
    {:put {:summary "Luo allekirjoitettu PDF"
           :parameters {:path {:id common-schema/Key
                               :language schema/Str}
                        :body energiatodistus-schema/Signature}
           :access rooli-service/laatija?
           :responses {200 {:body nil}
                       404 {:body schema/Str}}
           :handler (fn [{{{:keys [id language]} :path} :parameters :keys [db aws-s3-client whoami parameters]}]
                      (api-response/with-exceptions
                        #(api-response/signature-response
                          (energiatodistus-pdf-service/sign-energiatodistus-pdf
                           db aws-s3-client whoami
                           (Instant/now)
                           id language
                           (:body parameters))
                          (str id "/" language))
                        [{:type :name-does-not-match :response 403}
                         {:type :signed-pdf-exists :response 409}
                         {:type :expired-signing-certificate :response 400}]))}}]
   ["/finish"
    {:post {:summary "Siirrä energiatodistus allekirjoitettu-tilaan"
            :parameters {:path {:id common-schema/Key}}
            :access rooli-service/laatija?
            :responses {200 {:body nil}
                        404 {:body schema/Str}}
            :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami aws-s3-client]}]
                       (api-response/with-exceptions
                         #(api-response/signature-response
                            (energiatodistus-service/end-energiatodistus-signing! db aws-s3-client whoami id)
                            id)
                         [{:type :not-signed :response 400}]))}}]

   ["/cancel"
    {:post {:summary "Keskeytä allekirjoitus ja siirrä energiatodistus takaisin luonnokseksi"
            :parameters {:path {:id common-schema/Key}}
            :access rooli-service/laatija?
            :responses {200 {:body nil}
                        404 {:body schema/Str}}
            :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                       (api-response/signature-response
                         (energiatodistus-service/cancel-energiatodistus-signing! db whoami id)
                         id))}}]])
