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
  ["/perusparannuspassit"
   ["/2026"
    [""
     {:post {:summary    "Lisää energiatodistukselle perusparannuspassi"
             :parameters {:body ppp-schema/PerusparannuspassiSave}
             :responses  {201 {:body common-schema/IdAndWarnings}}
             :access     rooli-service/ppp-laatija?
             :handler    not-implemented!}}]
    ["/:id"
     {:get    {:summary    "Hae perusparannuspassi tunnisteella (id)"
               :parameters {:path {:id common-schema/Key}}
               :responses  {200 {:body ppp-schema/Perusparannuspassi}
                            404 {:body schema/Str}}
               :access     rooli-service/ppp-reader?
               :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                             (api-response/get-response
                               (ppp-service/find-perusparannuspassi db id)
                               (str "Perusparannuspassi " id " does not exist.")))}
      :put    {:summary    "Päivitä energiatodistuksen perusparannuspassin tietoja"
               :parameters {}
               :responses  {200 {:body nil}
                            404 {:body common-schema/GeneralError}}
               :access     (some-fn rooli-service/ppp-laatija? rooli-service/paakayttaja?)
               :handler    not-implemented!}
      :delete {:summary    "Poista energiatodistukselta perusparannuspassi"
               :parameters {:path {:id common-schema/Key}}
               :responses  {200 {:body nil}
                            404 {:body common-schema/GeneralError}}
               :access     rooli-service/ppp-laatija?
               :handler    not-implemented!}}]]])
