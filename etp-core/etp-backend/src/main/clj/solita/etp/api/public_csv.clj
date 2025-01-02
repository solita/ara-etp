(ns solita.etp.api.public-csv
  (:require    [ring.util.response :as r]
               [solita.etp.security :as security]
               [solita.etp.service.concurrent :as concurrent]
               [solita.etp.service.csv-to-s3 :as csv-to-s3]
               [solita.etp.service.kayttaja :as kayttaja-service]))


(def internal-routes
  [["/public-csv"
    ["/update"
     {:post {:summary    "Päivitä julkiset CSV-tiedostot S3:ssa."
             :middleware [[security/wrap-db-application-name]]
             :responses  {200 {:body nil}}
             :handler    (fn [{:keys [db whoami aws-s3-client]}]
                           (r/response
                            (concurrent/run-background
                             #(csv-to-s3/update-public-csv-in-s3!
                               db
                               whoami
                               aws-s3-client
                               {:where nil})
                             "Aineistot update failed")))}}]]])