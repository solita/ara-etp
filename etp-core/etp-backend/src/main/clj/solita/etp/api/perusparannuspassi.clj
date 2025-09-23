(ns solita.etp.api.perusparannuspassi
  (:require
    [schema.core :as schema]
    [solita.etp.api.response :as api-response]
    [solita.etp.service.rooli :as rooli-service]
    [solita.etp.schema.common :as common-schema]
    [solita.etp.schema.perusparannuspassi :as ppp-schema]
    [solita.etp.service.perusparannuspassi :as ppp-service]))

(defn not-implemented! [& _]
  (throw (ex-info "Not implemented" {:type :not-implemented})))

(def private-routes
  [["/perusparannuspassit"
   ["/2026"
    [""
     {:post {:summary    "Lisää energiatodistukselle perusparannuspassi"
             :parameters {:body ppp-schema/PerusparannuspassiSave}
             :responses  {201 {:body common-schema/IdAndWarnings}}
             :access     rooli-service/ppp-laatija?
             :handler    (fn [{{:keys [body]} :parameters
                               :keys [db whoami uri]}]
                           (api-response/with-exceptions
                             (fn []
                               (api-response/created uri (ppp-service/insert-perusparannuspassi! db whoami body)))
                             [{:type :energiatodistus-not-found :response 404}
                              {:type :foreign-key-violation :response 400}
                              {:type :invalid-energiatodistus-versio :response 400}
                              {:type :invalid-energiatodistus-id :response 400}]))}}]
    ["/:id"
     {:get    {:summary    "Hae perusparannuspassi tunnisteella (id)"
               :parameters {:path {:id common-schema/Key}}
               :responses  {200 {:body ppp-schema/Perusparannuspassi}
                            404 {:body schema/Str}}
               :access     rooli-service/ppp-reader?
               :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                             (api-response/get-response
                               (ppp-service/find-perusparannuspassi db whoami id)
                               (str "Perusparannuspassi " id " does not exist.")))}
      :put    {:summary    "Päivitä energiatodistuksen perusparannuspassin tietoja"
               :parameters {:path {:id common-schema/Key}
                            :body ppp-schema/PerusparannuspassiSave}
               :responses  {200 {:body nil}
                            404 {:body common-schema/GeneralError}}
               :access     (some-fn rooli-service/ppp-laatija? rooli-service/paakayttaja?)
               :handler    (fn [{{{:keys [id]} :path :keys [body]} :parameters :keys [db whoami uri]}]
                             (api-response/response-with-exceptions
                               #(ppp-service/update-perusparannuspassi! db whoami id body)
                               [{:type :energiatodistus-not-found :response 404}
                                {:type :foreign-key-violation :response 400}
                                {:type :invalid-energiatodistus-versio :response 400}
                                {:type :invalid-energiatodistus-id :response 400}]))}
      :delete {:summary    "Poista energiatodistukselta perusparannuspassi"
               :parameters {:path {:id common-schema/Key}}
               :responses  {200 {:body nil}
                            404 {:body common-schema/GeneralError}}
               :access     rooli-service/ppp-laatija?
               :handler    not-implemented!}}]]]])
