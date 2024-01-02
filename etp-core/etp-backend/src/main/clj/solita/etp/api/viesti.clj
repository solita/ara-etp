(ns solita.etp.api.viesti
  (:require [ring.util.response :as r]
            [solita.etp.api.response :as api-response]
            [solita.etp.schema.viesti :as viesti-schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.valvonta :as valvonta-schema]
            [solita.etp.service.viesti :as viesti-service]
            [schema.core :as schema]
            [clojure.java.io :as io]
            [solita.etp.schema.liite :as liite-schema]
            [solita.common.maybe :as maybe]))

(defn ketju-404 [id] (api-response/msg-404 "ketju" id))

(def routes
  [["/viestit"
    [""
     {:post {:summary    "Lisää uusi yleinen viestiketju."
             :parameters {:body viesti-schema/KetjuAdd}
             :responses  {201 {:body common-schema/Id}}
             :handler    (fn [{:keys [db whoami parameters uri]}]
                           (api-response/with-exceptions
                             #(api-response/created
                                uri
                                {:id (viesti-service/add-ketju! db whoami (:body parameters))})
                             [{:type     :missing-vastaanottaja-or-vastaanottajaryhma-id
                               :response 400}
                              {:type     :viestiketju-vastaanottajaryhma-id-fkey
                               :response 400}
                              {:type     :viestiketju-vastaanottaja-id-fkey
                               :response 400}]))}

      :get  {:summary    "Hae kaikki käyttäjän viestiketjut."
             :parameters {:query (merge viesti-schema/KetjuQuery
                                        viesti-schema/KetjuQueryWindow)}
             :responses  {200 {:body [viesti-schema/Ketju]}}
             :handler    (fn [{{:keys [query]} :parameters :keys [db whoami]}]
                           (r/response (viesti-service/find-ketjut db whoami query)))}}]
    ["/count"
     [""
      {:conflicting true
       :get         {:summary    "Hae viestiketjujen lukumäärä."
                     :parameters {:query viesti-schema/KetjuQuery}
                     :responses  {200 {:body {:count schema/Int}}}
                     :handler    (fn [{{:keys [query]} :parameters :keys [db whoami]}]
                                   (r/response (viesti-service/count-ketjut db whoami query)))}}]
     ["/unread"
      {:conflicting true
       :get         {:summary   "Hae lukemattomien viestiketjujen lukumäärä."
                     :responses {200 {:body {:count schema/Int}}}
                     :handler   (fn [{:keys [db whoami]}]
                                  (r/response (viesti-service/count-unread-ketjut db whoami)))}}]]
    ["/osapuolet"
     {:conflicting true
      :get {:summary   "Hae kaikki viesteihin liittyvät osapuolet (lähettäjät tai vastaanottajat)."
            :access    viesti-service/kasittelija?
            :responses {200 {:body [viesti-schema/Kayttaja]}}
            :handler   (fn [{:keys [db]}]
                         (r/response (viesti-service/find-osapuolet db)))}}]
    ["/:id"
     [""
      {:conflicting true
       :get         {:summary    "Hae viestiketjun tiedot"
                     :parameters {:path {:id common-schema/Key}}
                     :responses  {200 {:body viesti-schema/Ketju}
                                  404 {:body schema/Str}}
                     :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                                   (api-response/get-response
                                     (viesti-service/read-ketju! db whoami id)
                                     (ketju-404 id)))}
       :put         {:summary    "Päivitä viestiketjun tiedot"
                     :access     viesti-service/kasittelija?
                     :parameters {:path {:id common-schema/Key}
                                  :body viesti-schema/KetjuUpdate}
                     :responses  {200 {:body nil}
                                  404 {:body schema/Str}}
                     :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db parameters]}]
                                   (api-response/response-with-exceptions
                                     #(api-response/ok|not-found
                                        (viesti-service/update-ketju! db id (:body parameters))
                                        (ketju-404 id))
                                     [{:type :foreign-key-violation :response 400}]))}}]
     ["/viestit"
      {:post {:summary    "Lisää ketjuun uusi viesti"
              :parameters {:path {:id common-schema/Key}
                           :body schema/Str}
              :responses  {200 {:body nil}
                           404 {:body schema/Str}}
              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami parameters]}]
                            (api-response/ok|not-found
                              (viesti-service/add-viesti! db whoami id (:body parameters))
                              (ketju-404 id)))}}]
     ["/liitteet"
      [""
       {:get {:summary    "Hae viestiketjun liitteet."
              :parameters {:path {:id common-schema/Key}}
              :responses  {200 {:body [liite-schema/Liite]}
                           404 {:body schema/Str}}
              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                            (api-response/get-response
                              (viesti-service/find-liitteet db whoami id)
                              (ketju-404 id)))}}]
      ["/files"
       {:conflicting true
        :post        {:summary    "Viestiketjun liitteiden lisäys tiedostoista."
                      :parameters {:path      {:id common-schema/Key}
                                   :multipart liite-schema/MultipartFiles}
                      :responses  {201 {:body [common-schema/Key]}
                                   404 {:body common-schema/ConstraintError}}
                      :handler    (fn [{{{:keys [id]} :path {:keys [files]} :multipart}
                                        :parameters :keys [db aws-s3-client]}]
                                    (api-response/response-with-exceptions
                                      201
                                      #(viesti-service/add-liitteet-from-files!
                                         db aws-s3-client id
                                         (if (vector? files) files [files]))
                                      [{:constraint :viesti-liite-viestiketju-id-fkey :response 404}]))}}]
      ["/link"
       {:conflicting true
        :post        {:summary    "Liite-linkin lisäys viestiketjuun."
                      :parameters {:path {:id common-schema/Key}
                                   :body liite-schema/LiiteLinkAdd}
                      :responses  {201 {:body common-schema/Id}
                                   404 {:body common-schema/ConstraintError}}
                      :handler    (fn [{{{:keys [id]} :path :keys [body]}
                                        :parameters :keys [db uri]}]
                                    (api-response/with-exceptions
                                      #(api-response/created
                                         uri
                                         {:id (viesti-service/add-liite-from-link! db id body)})
                                      [{:constraint :viesti-liite-viestiketju-id-fkey :response 404}]))}}]
      ["/:liite-id"
       [""
        {:conflicting true
         :delete      {:summary    "Poista viestiketjun liite."
                       :parameters {:path {:id       common-schema/Key
                                           :liite-id common-schema/Key}}
                       :responses  {200 {:body nil}
                                    404 {:body schema/Str}}
                       :handler    (fn [{{{:keys [id liite-id]} :path} :parameters :keys [db whoami]}]
                                     (api-response/ok|not-found
                                       (viesti-service/delete-liite! db whoami liite-id)
                                       (api-response/msg-404 "liite" id liite-id)))}}]
       ["/:filename"
        {:conflicting true
         :get         {:summary    "Hae viestiketjun liitteen sisältö."
                       :parameters {:path {:id       common-schema/Key
                                           :liite-id common-schema/Key
                                           :filename schema/Str}}
                       :responses  {200 {:body nil}
                                    404 {:body schema/Str}}
                       :handler    (fn [{{{:keys [id liite-id filename]} :path} :parameters
                                         :keys                                  [db whoami aws-s3-client]}]
                                     (let [{:keys [content contenttype] :as file}
                                           (viesti-service/find-liite db whoami aws-s3-client id liite-id)]
                                       (if (or (= (:filename file) filename) (nil? file))
                                         (api-response/file-response
                                           (maybe/map* io/input-stream content) filename contenttype false
                                           (api-response/msg-404 "liite" id liite-id))
                                         (api-response/bad-request "Filename is invalid."))))}}]]]]]
   ["/vastaanottajaryhmat"
    {:get {:summary   "Hae kaikki vastaanottajaryhmat."
           :responses {200 {:body [common-schema/Luokittelu]}}
           :handler   (fn [{:keys [db]}]
                        (r/response (viesti-service/find-vastaanottajaryhmat db)))}}]
   ["/kasittelijat"
    {:get {:summary   "Hae kaikki kasittelijat."
           :responses {200 {:body [valvonta-schema/Valvoja]}}
           :handler   (fn [{:keys [db]}]
                        (r/response (viesti-service/find-kasittelijat db)))}}]])
