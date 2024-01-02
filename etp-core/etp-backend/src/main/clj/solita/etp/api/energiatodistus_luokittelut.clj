(ns solita.etp.api.energiatodistus-luokittelut
  (:require [ring.util.response :as r]
            [solita.etp.service.kielisyys :as kielisyys]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.api.response :as api-response]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.schema.kayttotarkoitus :as kayttotarkoitus-schema]
            [schema.core :as schema]
            [solita.etp.schema.e-luokka :as e-luokka-schema]))

(defn classification-route [path name find-all]
  [path
   {:get {:summary   (str "Hae energiatodistuksen " name)
          :responses {200 {:body [common-schema/Luokittelu]}}
          :handler   (fn [{:keys [db]}] (r/response (find-all db)))}}])

(def routes
  [(classification-route "/kielisyys" "kielisyydet" kielisyys/find-kielisyys)
   (classification-route "/laatimisvaiheet" "laatimisvaiheet" laatimisvaihe/find-laatimisvaiheet)
   (classification-route "/lammitysmuoto" "lämmitysmuodot" luokittelu-service/find-lammitysmuodot)
   (classification-route "/lammonjako" "lämmönjaot" luokittelu-service/find-lammonjaot)
   (classification-route "/ilmanvaihtotyyppi" "ilmanvaihtotyypit" luokittelu-service/find-ilmanvaihtotyypit)

   ["/kayttotarkoitusluokat/:versio"
    {:get {:summary    "Hae energiatodistuksen käyttötarkoitusluokat"
           :parameters {:path {:versio common-schema/Key}}
           :responses  {200 {:body [common-schema/Luokittelu]}}
           :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                         (r/response (kayttotarkoitus-service/find-kayttotarkoitukset db versio)))}}]

   ["/alakayttotarkoitusluokat/:versio"
    {:get {:summary    "Hae energiatodistuksen alakäyttötarkoitusluokat"
           :parameters {:path {:versio common-schema/Key}}
           :responses  {200 {:body [kayttotarkoitus-schema/Alakayttotarkoitusluokka]}}
           :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                         (r/response (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)))}}]

   ["/e-luokka/:versio/:alakayttotarkoitusluokka/:nettoala/:e-luku"
    {:get {:summary    "Laske energiatodistukselle energiatehokkuusluokka"
           :parameters {:path {:versio                   common-schema/Key
                               :alakayttotarkoitusluokka schema/Str
                               :nettoala                 common-schema/NonNegative
                               :e-luku                   common-schema/NonNegative}}
           :responses  {200 {:body e-luokka-schema/ELuokka}}
           :handler    (fn [{{{:keys [versio alakayttotarkoitusluokka nettoala e-luku]} :path}
                             :parameters :keys [db]}]
                         (api-response/get-response
                           (e-luokka-service/find-e-luokka db
                                                           versio
                                                           alakayttotarkoitusluokka
                                                           nettoala
                                                           e-luku)
                           "Could not find luokittelu with given versio and alakayttotarkoitusluokka"))}}]])
