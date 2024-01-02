(ns solita.etp.api.statistics
  (:require [ring.util.response :as r]
            [schema.core :as schema]
            [solita.etp.header-middleware :as header]
            [solita.etp.schema.statistics :as statistics-schema]
            [solita.etp.service.statistics :as statistics-service]
            [solita.etp.schema.common :as common-schema]))

(def routes
  [["/statistics"
    [""
     {:get {:summary    "Hae energiatehokkuuteen liittyvää tilastotietoa"
            :middleware [[header/wrap-cache-control 3600]]
            :parameters {:query statistics-schema/StatisticsQuery}
            :responses  {200 {:body statistics-schema/StatisticsResponse}}
            :handler    (fn [{{:keys [query]} :parameters :keys [db]}]
                          (r/response (statistics-service/find-statistics db query)))}}]
    ["/count"
     {:get {:summary   "Voimassa olevien energiatodistusten lukumäärä"
            :responses {200 {:body {:count schema/Int}}}
            :middleware [[header/wrap-cache-control 3600]]
            :handler (fn [{:keys [db]}]
                    (r/response (statistics-service/find-count db)))}}]
    ["/kayttotarkoitukset"
     {:get {:summary    "Hae tilastoinnissa käytettävät käyttötarkoitukset"
            :middleware [[header/wrap-cache-control 3600]]
            :responses  {200 {:body [common-schema/Luokittelu]}}
            :handler    (fn [{:keys [db]}]
                          (r/response (statistics-service/find-kayttotarkoitukset db)))}}]]])
