(ns solita.etp.api.energiatodistus-liite
  (:require [ring.util.response :as r]
            [reitit.ring.schema :as reitit-schema]
            [solita.etp.schema.liite :as liite-schema]
            [solita.etp.service.liite :as liite-service]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.api.response :as api-response]
            [solita.etp.service.rooli :as rooli-service]))

(def liite-access (some-fn rooli-service/laatija? rooli-service/paakayttaja?))

(def routes
  ["/liitteet"
   ["/files"
    {:conflicting true
     :post {:summary    "Energiatodistuksen liitteiden lisäys tiedostoista."
            :access     rooli-service/paakayttaja?
            :parameters {:path {:id common-schema/Key}
                         :multipart {:files (schema/conditional
                                              vector? [reitit-schema/TempFilePart]
                                              :else reitit-schema/TempFilePart)}}
            :responses  {201 {:body nil}
                        404 common-schema/ConstraintError}
            :handler    (fn [{{{:keys [id]} :path {:keys [files]} :multipart} :parameters
                           :keys [db aws-s3-client whoami]}]
                          (api-response/response-with-exceptions
                           201
                           (fn []
                             (liite-service/add-liitteet-from-files!
                              db
                              aws-s3-client
                              whoami
                              id
                              (if (vector? files) files [files]))
                             nil)
                         [{:constraint :liite-energiatodistus-id-fkey :response 404}]))}}]

   ["/link"
    {:conflicting true
     :post {:summary    "Liite linkin lisäys energiatodistukseen."
            :access     rooli-service/paakayttaja?
            :parameters {:path {:id common-schema/Key}
                         :body liite-schema/LiiteLinkAdd}
            :responses  {201 {:body nil}
                        404 common-schema/ConstraintError}
            :handler    (fn [{{{:keys [id]} :path :keys [body]} :parameters
                           :keys [db whoami]}]
                          (api-response/response-with-exceptions
                           201
                           (fn []
                             (liite-service/add-liite-from-link! db whoami id body)
                             nil)
                         [{:constraint :liite-energiatodistus-id-fkey :response 404}]))}}]

   [""
    {:get {:summary "Hae energiatodistuksen liitteet."
           :access  liite-access
           :parameters {:path {:id common-schema/Key}}
           :responses {200 {:body [liite-schema/Liite]}}
           :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                      (r/response (liite-service/find-energiatodistus-liitteet
                                   db
                                   whoami
                                   id)))}}]

   ["/:liite-id"
    {:conflicting true
     :delete {:summary    "Poista liite energiatodistuksesta."
              :access     rooli-service/paakayttaja?
              :parameters {:path {:id common-schema/Key
                                  :liite-id common-schema/Key}}
              :responses  {200 {:body nil}
                          404 {:body schema/Str}}
              :handler    (fn [{{{:keys [id liite-id]} :path}
                               :parameters
                               :keys [db whoami]}]
                          (api-response/ok|not-found
                              (liite-service/delete-liite! db whoami liite-id)
                              (api-response/msg-404 "energiatodistusliite" id liite-id)))}}]

   ["/:liite-id/:filename"
    {:get {:summary    "Hae energiatodistuksen yhden liitteen sisältö."
           :access     liite-access
           :parameters {:path {:id       common-schema/Key
                               :liite-id common-schema/Key
                               :filename schema/Str}}
           :responses  {200 {:body nil}
                        404 {:body schema/Str}}
           :handler    (fn [{{{:keys [id liite-id filename]} :path} :parameters
                             :keys [db whoami aws-s3-client]}]
                         (let [{:keys [content nimi contenttype]}
                               (liite-service/find-energiatodistus-liite-content
                                 db whoami aws-s3-client liite-id)]
                           (if (or (= nimi filename) (nil? content))
                             (api-response/file-response
                               content nimi contenttype false
                               (api-response/msg-404 "energiatodistusliite" id liite-id))
                             (api-response/bad-request "Filename is invalid."))))}}]])
