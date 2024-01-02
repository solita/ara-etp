(ns solita.etp.api.sivu
  (:require [schema.core :as schema]
            [schema-tools.core :as st]
            [solita.etp.api.response :as api-response]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.sivu :as sivu-schema]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.sivu :as sivu-service]))

(def sivu-exceptions [{:type :foreign-key-violation :response 400}])

(def routes
  [["/sivut"
    ["" {:get {:summary "Hae kaikki sivut"
               :responses {200 {:body [sivu-schema/SivuBrief]}}
               :handler (fn [{:keys [db whoami]}]
                          (api-response/get-response
                           (sivu-service/find-all-sivut db whoami) nil))}
         :post {:summary    "Lisää uusi sivu"
                :access     rooli-service/paakayttaja?
                :responses  {201 {:body common-schema/Id}}
                :parameters {:body sivu-schema/SivuSave}
                :handler    (fn [{:keys [db parameters uri]}]
                              (api-response/with-exceptions
                                #(api-response/created
                                  uri
                                  (sivu-service/add-sivu! db (:body parameters)))
                                sivu-exceptions))}}]
    ["/:id"
     [""
      {:get {:summary "Hae sivu"
             :parameters {:path {:id common-schema/Key}}
             :responses  {200 {:body sivu-schema/Sivu}
                          404 {:body schema/Str}}
             :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                        (api-response/get-response
                         (sivu-service/find-sivu db whoami id)
                         (str "Sivu " id " does not exist.")))}
       :put {:summary    "Päivitä sivu"
             :access     rooli-service/paakayttaja?
             :parameters {:path {:id common-schema/Key}
                          :body (st/optional-keys sivu-schema/SivuSave)}
             :responses  {200 {:body nil}
                          404 {:body schema/Str}}
             :handler  (fn [{{{:keys [id]} :path} :parameters :keys [db parameters]}]
                         (api-response/with-exceptions
                           #(api-response/ok|not-found
                             (sivu-service/update-sivu! db id (:body parameters))
                             (str "Sivu " id " does not exist."))
                           sivu-exceptions))}
       :delete {:summary "Poista sivu"
                :access  rooli-service/paakayttaja?
                :parameters {:path {:id common-schema/Key}}
                :responses {200 {:body nil}
                            404 {:body schema/Str}}
                :handler (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                           (api-response/with-exceptions
                             #(api-response/ok|not-found
                               (sivu-service/delete-sivu! db id)
                               (str "Sivu " id " does not exist."))
                             sivu-exceptions))}}]]]])
