(ns solita.etp.api.laskutus
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as r]
            [schema.core :as schema]
            [solita.etp.security :as security]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.laskutus :as laskutus-service]
            [solita.etp.service.concurrent :as concurrent]))

(def routes
  [["/laskutus"
    {:middleware [[security/wrap-db-application-name
                   (kayttaja-service/system-kayttaja :laskutus)]]
     :post       {:summary    "Käynnistä laskutusajo"
                  :parameters {:query {(schema/optional-key :dryrun) schema/Bool}}
                  :responses  {200 {:body nil}}
                  :handler    (fn [{{:keys [query]} :parameters :keys [db aws-s3-client]}]
                                (concurrent/run-background
                                  #(laskutus-service/do-kuukauden-laskutus
                                     db
                                     aws-s3-client
                                     (:dryrun query))
                                  "Laskutus failed")
                                (r/response {}))}}]])
