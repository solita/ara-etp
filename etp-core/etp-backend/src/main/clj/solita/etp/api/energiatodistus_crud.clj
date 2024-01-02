(ns solita.etp.api.energiatodistus-crud
  (:require [solita.etp.api.response :as api-response]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]))

(defn post
  ([version save-schema coerce]
  {:post
    {:summary    "Lisää luonnostilaisen energiatodistuksen"
     :parameters {:body save-schema}
     :responses  {201 {:body common-schema/IdAndWarnings}}
     :access     rooli-service/laatija?
     :handler    (fn [{:keys [db whoami parameters uri]}]
                   (api-response/with-exceptions
                     #(api-response/created uri
                       (energiatodistus-service/add-energiatodistus!
                         db whoami version (coerce (:body parameters))))
                     [{:type :invalid-replace :response 400}
                      {:type :foreign-key-violation :response 400}
                      {:type :invalid-value :response 400}
                      {:type :invalid-sisainen-kuorma :response 400}
                      {:type :invalid-laskutusosoite :response 400}]))}})

  ([version save-schema] (post version save-schema identity)))

(defn gpd-routes [get-schema save-schema]
  [""
   {:get    {:summary    "Hae yksittäinen energiatodistus tunnisteella (id)"
             :parameters {:path {:id common-schema/Key}}
             :responses  {200 {:body get-schema}
                          404 {:body schema/Str}}
             :access     rooli-service/energiatodistus-reader?
             :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                           (api-response/get-response
                             (energiatodistus-service/find-energiatodistus db whoami id)
                             (str "Energiatodistus " id " does not exists.")))}

    :put    {:summary    "Päivitä energiatodistuksen tietoja"
             :parameters {:path {:id common-schema/Key}
                          :body save-schema}
             :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
             :responses  {200 {:body nil}
                          404 {:body common-schema/GeneralError}}
             :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami parameters]}]
                           (api-response/response-with-exceptions
                             #(energiatodistus-service/update-energiatodistus!
                               db whoami id
                               (:body parameters))
                             [{:type :not-found :response 404}
                              {:type :update-conflict :response 409}
                              {:type :invalid-replace :response 400}
                              {:type :foreign-key-violation :response 400}
                              {:type :invalid-value :response 400}
                              {:type :missing-value :response 400}
                              {:type :invalid-sisainen-kuorma :response 400}
                              {:type :invalid-laskutusosoite :response 400}]))}

    :delete {:summary    "Poista luonnostilainen energiatodistus"
             :parameters {:path {:id common-schema/Key}}
             :access     rooli-service/laatija?
             :responses  {200 {:body nil}
                          404 {:body schema/Str}}
             :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                           (api-response/ok|not-found
                             (energiatodistus-service/delete-energiatodistus-luonnos!
                               db whoami id)
                             (str "Energiatodistus luonnos " id " does not exists.")))}}])

(def discarded
  ["/discarded"
   {:post {:summary    "Hylkää allekirjoitettu energiatodistus tai
                        palauta hylätty energiatodistus allekirjoitetuksi"
           :parameters {:path {:id common-schema/Key}
                        :body schema/Bool}
           :access     rooli-service/paakayttaja?
           :responses  {200 {:body nil}
                        404 {:body schema/Str}}
           :handler    (fn [{{{:keys [id]} :path :keys [body]} :parameters :keys [db]}]
                         (api-response/ok|not-found
                           (energiatodistus-service/set-energiatodistus-discarded! db id body)
                           (str (if body "Signed" "Discarded")
                                " energiatodistus " id " does not exists.")))}}])
