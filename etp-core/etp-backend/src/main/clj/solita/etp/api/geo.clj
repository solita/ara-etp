(ns solita.etp.api.geo
  (:require [ring.util.response :as r]
            [solita.etp.schema.geo :as geo-schema]
            [solita.etp.service.geo :as geo-service]
            [solita.etp.schema.common :as common-schema]))

(def routes
  [["/toimintaalueet/"
    {:get {:summary    "Hae kaikki toiminta-alueet"
           :responses  {200 {:body [geo-schema/Toimintaalue]}}
           :handler    (fn [{:keys [db]}]
                         (r/response (geo-service/find-all-toiminta-alueet db)))}}]
   ["/countries/"
    {:get {:summary    "Hae kaikki maat"
           :responses  {200 {:body [geo-schema/Country]}}
           :handler    (fn [{:keys [db]}]
                         (r/response (geo-service/find-all-countries db)))}}]
   ["/postinumerot"
    {:get {:summary   "Hae kaikki postinumerot"
           :responses {200 {:body [geo-schema/Postinumero]}}
           :handler   (fn [{:keys [db]}]
                        (r/response (geo-service/find-all-postinumerot db)))}}]
   ["/kunnat"
    {:get {:summary   "Hae kaikki kunnat"
           :responses {200 {:body [geo-schema/Kunta]}}
           :handler   (fn [{:keys [db]}]
                        (r/response (geo-service/find-all-kunnat db)))}}]])
