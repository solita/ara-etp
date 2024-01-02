(ns solita.etp.api.valvonta-kaytto-toimenpiteet
  (:require
    [schema.core :as schema]
    [solita.etp.schema.common :as common-schema]
    [solita.etp.schema.valvonta-kaytto :as kaytto-schema]
    [solita.etp.api.response :as api-response]
    [solita.etp.service.rooli :as rooli-service]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]))

(defn toimenpide-404-msg [valvonta-id toimenpide-id]
  (api-response/msg-404 "Toimenpide" valvonta-id toimenpide-id))

(def routes
  ["/toimenpiteet"
   [""
    {:get  {:summary    "Hae käytönvalvonnan toimenpiteet."
            :parameters {:path {:id common-schema/Key}}
            :responses  {200 {:body [kaytto-schema/Toimenpide]}}
            :access     rooli-service/paakayttaja?
            :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                          (api-response/get-response
                            (valvonta-service/find-toimenpiteet db id)
                            (api-response/msg-404 "kaytonvalvonta" id)))}
     :post {:summary    "Lisää käytönvalvonnan toimenpide."
            :access     rooli-service/paakayttaja?
            :parameters {:path {:id common-schema/Key}
                         :body kaytto-schema/ToimenpideAdd}
            :responses  {201 {:body common-schema/Id}
                         404 common-schema/ConstraintError}
            :handler    (fn [{{{:keys [id]} :path :keys [body]}
                              :parameters :keys [db aws-s3-client whoami uri]}]
                          (api-response/with-exceptions
                            #(api-response/created uri
                                                   (valvonta-service/add-toimenpide! db aws-s3-client whoami id body))
                            [{:constraint :toimenpide-vk-valvonta-id-fkey
                              :response   404}]))}}]
   ["/henkilot/:henkilo-id/preview"
    {:conflicting true
     :post        {:summary    "Henkilö-osapuolen toimenpiteen esikatselu"
                   :parameters {:path {:id         common-schema/Key
                                       :henkilo-id common-schema/Key}
                                :body kaytto-schema/ToimenpideAdd}
                   :access     rooli-service/paakayttaja?
                   :responses  {200 {:body nil}
                                404 {:body schema/Str}}
                   :handler    (fn [{{{:keys [id henkilo-id]} :path :keys [body]}
                                     :parameters :keys [db whoami]}]
                                 (api-response/pdf-response
                                   (valvonta-service/preview-henkilo-toimenpide
                                     db whoami id (assoc body :valvonta-id id) henkilo-id)
                                   (valvonta-service/toimenpide-filename body)
                                   (api-response/msg-404 "henkilo" id henkilo-id)))}}]
   ["/yritykset/:yritys-id/preview"
    {:conflicting true
     :post        {:summary    "Yritys-osapuolen toimenpiteen esikatselu"
                   :parameters {:path {:id        common-schema/Key
                                       :yritys-id common-schema/Key}
                                :body kaytto-schema/ToimenpideAdd}
                   :access     rooli-service/paakayttaja?
                   :responses  {200 {:body nil}
                                404 {:body schema/Str}}
                   :handler    (fn [{{{:keys [id yritys-id]} :path :keys [body]}
                                     :parameters :keys [db whoami]}]
                                 (api-response/pdf-response
                                   (valvonta-service/preview-yritys-toimenpide
                                     db whoami id (assoc body :valvonta-id id) yritys-id)
                                   (valvonta-service/toimenpide-filename body)
                                   (api-response/msg-404 "yritys" id yritys-id)))}}]
   ["/:toimenpide-id"
    [""
     {:conflicting true
      :put         {:summary    "Muuta toimenpiteen tietoja."
                    :access     rooli-service/paakayttaja?
                    :parameters {:path {:id            common-schema/Key
                                        :toimenpide-id common-schema/Key}
                                 :body kaytto-schema/ToimenpideUpdate}
                    :responses  {200 {:body nil}
                                 404 {:body schema/Str}}
                    :handler    (fn [{{{:keys [id toimenpide-id]} :path :keys [body]}
                                      :parameters :keys [db]}]
                                  (api-response/ok|not-found
                                    (valvonta-service/update-toimenpide!
                                      db toimenpide-id body)
                                    (toimenpide-404-msg id toimenpide-id)))}}]
    ["/henkilot"
     ["/:henkilo-id"
      ["/document/:filename"
       {:get {:summary    "Henkilö-osapuolen toimenpiteen dokumentin lataus"
              :parameters {:path {:id            common-schema/Key
                                  :toimenpide-id common-schema/Key
                                  :henkilo-id    common-schema/Key
                                  :filename      schema/Str}}
              :access     rooli-service/paakayttaja?
              :responses  {200 {:body nil}
                           404 {:body schema/Str}}
              :handler    (fn [{{{:keys [id toimenpide-id henkilo-id filename]} :path}
                                :parameters :keys [db aws-s3-client]}]
                            (api-response/pdf-response
                              (valvonta-service/find-toimenpide-henkilo-document
                                db aws-s3-client id toimenpide-id henkilo-id)
                              filename
                              (api-response/msg-404 "henkilo" id toimenpide-id henkilo-id)))}}]
      ["/attachment"
       ["/hallinto-oikeus.pdf"
        {:get {:summary    "Henkilö-osapuolen toimenpiteeseen liittyvän hallinto-oikeus-liitteen lataus"
               :parameters {:path {:id            common-schema/Key
                                   :toimenpide-id common-schema/Key
                                   :henkilo-id    common-schema/Key}}
               :access     rooli-service/paakayttaja?
               :responses  {200 {:body nil}
                            404 {:body schema/Str}}
               :handler    (fn [{{{:keys [id toimenpide-id henkilo-id]} :path}
                                 :parameters :keys [db aws-s3-client]}]
                             (api-response/pdf-response
                               (valvonta-service/find-henkilo-hallinto-oikeus-attachment db aws-s3-client id toimenpide-id henkilo-id)
                               "hallinto-oikeus.pdf"
                               (api-response/msg-404 "henkilo" id toimenpide-id henkilo-id)))}}]]]]
    ["/yritykset"
     ["/:yritys-id"
      ["/document/:filename"
       {:get {:summary    "Yritys-osapuolen toimenpiteen esikatselu tai lataus"
              :parameters {:path {:id            common-schema/Key
                                  :toimenpide-id common-schema/Key
                                  :yritys-id     common-schema/Key
                                  :filename      schema/Str}}
              :access     rooli-service/paakayttaja?
              :responses  {200 {:body nil}
                           404 {:body schema/Str}}
              :handler    (fn [{{{:keys [id toimenpide-id yritys-id filename]} :path}
                                :parameters :keys [db aws-s3-client]}]
                            (api-response/pdf-response
                              (valvonta-service/find-toimenpide-yritys-document
                                db aws-s3-client id toimenpide-id yritys-id)
                              filename
                              (api-response/msg-404 "yritys " id toimenpide-id yritys-id)))}}]
      ["/attachment"
       ["/hallinto-oikeus.pdf"
        {:get {:summary    "Yritys-osapuolen toimenpiteeseen liittyvän hallinto-oikeus-liitteen lataus"
               :parameters {:path {:id            common-schema/Key
                                   :toimenpide-id common-schema/Key
                                   :yritys-id     common-schema/Key}}
               :access     rooli-service/paakayttaja?
               :responses  {200 {:body nil}
                            404 {:body schema/Str}}
               :handler    (fn [{{{:keys [id toimenpide-id yritys-id]} :path}
                                 :parameters :keys [db aws-s3-client]}]
                             (api-response/pdf-response
                               (valvonta-service/find-yritys-hallinto-oikeus-attachment db aws-s3-client id toimenpide-id yritys-id)
                               "hallinto-oikeus.pdf"
                               (api-response/msg-404 "yritys " id toimenpide-id yritys-id)))}}]]]]]])
