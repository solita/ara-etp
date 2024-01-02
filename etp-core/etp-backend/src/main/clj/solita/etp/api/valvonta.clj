(ns solita.etp.api.valvonta
  (:require [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.valvonta :as valvonta-service]
            [solita.etp.schema.valvonta :as valvonta-schema]
            [ring.util.response :as r]))

(def routes
  [["/valvonta"
    ["/valvojat"
     {:conflicting true
      :get         {:summary   "Hae kaikki käyttäjät, jotka voi olla valvojia."
                    :responses {200 {:body [valvonta-schema/Valvoja]}}
                    :access    (some-fn rooli-service/paakayttaja? rooli-service/laatija?)
                    :handler   (fn [{:keys [db]}]
                                 (r/response (valvonta-service/find-valvojat db)))}}]]])
