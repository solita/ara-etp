(ns solita.etp.api.laatija
  (:require [ring.util.response :as r]
            [schema.core :as schema]
            [solita.etp.api.response :as api-response]
            [solita.etp.header-middleware :as header]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.audit :as audit-schema]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.kayttaja-laatija :as kayttaja-laatija-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.security :as security]))

(def get-laatijat
  {:summary    "Hae laatijat"
   :responses  {200 {:body [laatija-schema/LaatijaFind]}}
   :handler    (fn [{:keys [db whoami]}]
                 (r/response (laatija-service/find-all-laatijat db whoami)))})

(def get-patevyydet
  {:summary   "Hae pätevyydet-luokittelu"
   :responses {200 {:body [laatija-schema/Patevyystaso]}}
   :handler   (fn [{:keys [db]}]
                (r/response (laatija-service/find-patevyystasot db)))})

(def get-count-public-laatijat
  {:summary "Hae laatijahaussa näkyvien laatijoiden lukumäärä"
   :responses {200 {:body {:count schema/Int}}}
   :handler (fn [{:keys [db]}]
              (r/response (laatija-service/count-public-laatijat db)))})

(def public-routes
  [["/laatijat"
    [""
     {:get get-laatijat}]
    ["/count"
     {:get        get-count-public-laatijat
      :middleware [[header/wrap-cache-control 3600]]}]]
   ["/patevyydet"
    {:get get-patevyydet}]])

(def private-routes
  [["/laatijat"
    [""
     {:get  get-laatijat
      :put  {:summary    (str
                           "Lisää uusia laatijoita ja päivitä olemassaolevien laatijoiden tietoa. "
                           "Laatijan yksilöinti perustuu hetuun.")
             :parameters {:body [laatija-schema/KayttajaLaatijaAdd]}
             :responses  {200 {:body [common-schema/Key]}}
             :access     rooli-service/patevyydentoteaja?
             :handler    (fn [{:keys [db parameters]}]
                           (r/response
                             (kayttaja-laatija-service/upsert-kayttaja-laatijat!
                               db (:body parameters))))}

      :post {:summary    "Lisää yksittäinen laatija"
             :parameters {:body laatija-schema/KayttajaLaatijaUpdate}
             :responses  {201 {:body common-schema/Id}}
             :access     rooli-service/paakayttaja?
             :handler    (fn [{:keys [db parameters uri]}]
                           (api-response/created
                             uri {:id (kayttaja-laatija-service/add-laatija!
                                        db (:body parameters))}))}}]
    ["/:id"
     [""
      {:put {:summary "Päivitä laatijan ja laatijaan liittyvän käyttäjän tiedot"
             :parameters {:path {:id common-schema/Key}
                         :body laatija-schema/KayttajaLaatijaUpdate}
             :responses {200 {:body nil}
                         404 {:body schema/Str}}
             :handler (fn [{{{:keys [id]} :path} :parameters
                           :keys [db whoami parameters]}]
                        (api-response/ok|not-found
                         (kayttaja-laatija-service/update-kayttaja-laatija!
                          db whoami id (:body parameters))
                         (str "Laatija " id " does not exists.")))}}]

     ["/history"
      {:get {:summary "Hae laatijan muutoshistoria"
             :parameters {:path {:id common-schema/Key}}
             :responses {200 {:body [(-> laatija-schema/Laatija
                                         (merge audit-schema/Audit)
                                         (dissoc :voimassa :voimassaolo-paattymisaika))]}}
             :handler (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                        (r/response
                         (laatija-service/find-history db whoami id)))}}]
     ["/laskutusosoitteet"
      {:get {:summary    "Hae laatijan laskutusosoitteet"
             :parameters {:path {:id common-schema/Key}}
             :responses  {200 {:body [laatija-schema/Laskutusosoite]}}
             :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                           (r/response
                             (laatija-service/find-laatija-laskutusosoitteet db whoami id)))}}]
     ["/yritykset"
      [""
       {:get {:summary    "Hae laatijan yritykset"
              :parameters {:path {:id common-schema/Key}}
              :responses  {200 {:body [laatija-schema/Yritys]}}
              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                            (r/response
                              (laatija-service/find-laatija-yritykset db whoami id)))}}]
      ["/:yritys-id"
       {:put    {:summary    "Liitä laatija yritykseen - liittämispyyntö"
                 :access     rooli-service/accredited-laatija?
                 :parameters {:path {:id        common-schema/Key
                                     :yritys-id common-schema/Key}}
                 :responses  {200 {:body nil}
                              404 common-schema/ConstraintError}
                 :handler    (fn [{{{:keys [id yritys-id]} :path} :parameters :keys [db whoami]}]
                               (api-response/response-with-exceptions
                                 #(laatija-service/add-laatija-yritys! db whoami id yritys-id)
                                 [{:constraint :laatija-yritys-laatija-id-fkey
                                   :response   404}
                                  {:constraint :laatija-yritys-yritys-id-fkey
                                   :response   404}]))}
        :delete {:summary    "Poista laatija yrityksestä"
                 :parameters {:path {:id        common-schema/Key
                                     :yritys-id common-schema/Key}}
                 :responses  {200 {:body nil}}
                 :handler    (fn [{{{:keys [id yritys-id]} :path} :parameters :keys [db whoami]}]
                               (api-response/ok|not-found
                                 (laatija-service/detach-laatija-yritys! db whoami id yritys-id)
                                 (str "Laatija and yritys liitos " id "/" yritys-id " does not exist.")))}}]]]]
   ["/patevyydet"
    {:get get-patevyydet}]])

(def internal-routes
  [["/laatijat"
    ["/patevyys-expiration-messages"
     {:middleware [[security/wrap-db-application-name
                    (kayttaja-service/system-kayttaja :communication)]]
      :post       {:summary   "Käynnistä pätevyysmuistutusten lähetys"
                   :parameters
                   {:query {:months-before-expiration              schema/Int
                            (schema/optional-key :fallback-window) schema/Int
                            (schema/optional-key :dryrun)          schema/Bool}}
                   :responses {200 {:body [{:id common-schema/Key
                                            :email schema/Str}]}}
                   :handler   (fn [{{:keys [query]} :parameters :keys [db]}]
                                (r/response (laatija-service/send-patevyys-expiration-messages!
                                  db query)))}}]]])
