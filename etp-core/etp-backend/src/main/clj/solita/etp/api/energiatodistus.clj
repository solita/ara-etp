(ns solita.etp.api.energiatodistus
  (:require [ring.util.response :as r]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.public-energiatodistus :as public-energiatodistus-schema]
            [solita.etp.schema.valvonta-oikeellisuus :as valvonta-schema]
            [solita.etp.api.energiatodistus-crud :as crud-api]
            [solita.etp.api.energiatodistus-xml :as xml-api]
            [solita.etp.api.energiatodistus-liite :as liite-api]
            [solita.etp.api.energiatodistus-signing :as signing-api]
            [solita.etp.api.energiatodistus-history :as history-api]
            [solita.etp.api.energiatodistus-luokittelut :as luokittelut-api]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-search :as energiatodistus-search-service]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.energiatodistus-csv :as energiatodistus-csv-service]
            [solita.etp.service.energiatodistus-xlsx :as energiatodistus-xlsx-service]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.api.response :as api-response]
            [solita.etp.api.stream :as api-stream]
            [solita.etp.service.json :as json]
            [solita.etp.exception :as exception]
            [solita.etp.schema.viesti :as viesti-schema]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.schema.geo :as geo-schema])
  (:import (com.fasterxml.jackson.core JsonParseException)))

(defn valid-pdf-filename? [filename id kieli]
  (= filename (format "energiatodistus-%s-%s.pdf" id kieli)))

(def search-exceptions [{:type :unknown-field :response 400}
                        {:type :unknown-predicate :response 400}
                        {:type :invalid-arguments :response 400}
                        {:type :schema.core/error :response 400}])

(defn pdf-route [versio]
  ["/pdf/:kieli/:filename"
   {:get {:summary    "Lataa energiatodistus PDF-tiedostona"
          :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
          :parameters {:path {:id common-schema/Key
                              :kieli schema/Str
                              :filename schema/Str}}
          :responses  {200 {:body nil}
                       404 {:body schema/Str}}
          :handler    (fn [{{{:keys [id kieli filename]} :path} :parameters :keys [db aws-s3-client whoami]}]
                        (if (valid-pdf-filename? filename id kieli)
                          (api-response/pdf-response
                           (energiatodistus-pdf-service/find-energiatodistus-pdf db
                                                                                 aws-s3-client
                                                                                 whoami
                                                                                 id
                                                                                 kieli)
                           filename
                           (str "Energiatodistus " id " does not exists."))
                          (r/not-found "File not found")))}}])

(defn- parse-where [where]
  (try (json/read-value where)
       (catch JsonParseException _
         (exception/throw-ex-info!
           :invalid-arguments
           (str "Invalid json in where: " where)))))

(defn search-route [schema]
  [""
   {:get {:summary    "Hae energiatodistuksia"
          :parameters {:query energiatodistus-schema/EnergiatodistusSearch}
          :responses  {200 {:body [schema]}}
          :access     rooli-service/energiatodistus-reader?
          :handler    (fn [{{:keys [query]} :parameters :keys [db whoami]}]
                        (api-response/response-with-exceptions
                         #(energiatodistus-search-service/search
                           db
                           whoami
                           (update query :where parse-where)
                           schema)
                         search-exceptions))}}])

(def search-count-route
  ["/count"
   {:get {:summary    "Hae energiatodistuksia - energiatodistusten lukumäärä"
          :parameters {:query energiatodistus-schema/EnergiatodistusSearch}
          :responses  {200 {:body {:count schema/Int}}}
          :access     rooli-service/energiatodistus-reader?
          :handler    (fn [{{:keys [query]} :parameters :keys [db whoami]}]
                        (api-response/response-with-exceptions
                          #(energiatodistus-search-service/search-count
                             db whoami
                             (update query :where parse-where))
                          search-exceptions))}}])



(defn csv-route [energiatodistukset-csv use-cache?]
  ["/csv/energiatodistukset.csv"
   {:get {:summary    "Hae energiatodistusten tiedot CSV-tiedostona"
          :parameters {:query energiatodistus-schema/EnergiatodistusSearch}
          :responses  {200 {:body nil}}
          :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
          :handler    (fn [{{:keys [query]} :parameters :keys [db whoami] :as request}]
                        (api-response/with-exceptions
                          #(let [result (energiatodistukset-csv
                                          db whoami (update query :where json/read-value))]
                             (api-stream/result->async-channel
                               request
                               (merge (api-response/csv-response-headers "energiatodistukset.csv" false)
                                      (when use-cache?
                                        (api-response/async-cache-headers 43200)))
                               result))
                          search-exceptions))}}])

(def public-routes
  (concat
   [["/energiatodistukset"
     (search-route public-energiatodistus-schema/Energiatodistus)
     search-count-route
     (csv-route energiatodistus-csv-service/energiatodistukset-public-csv true)
     luokittelut-api/routes]]))

(def private-routes
  (concat
    [["/energiatodistukset"
      (search-route valvonta-schema/Energiatodistus+Valvonta)
      search-count-route
      (csv-route energiatodistus-csv-service/energiatodistukset-private-csv false)

      ["/xlsx/energiatodistukset.xlsx"
       {:get {:summary    "Hae energiatodistusten tiedot XLSX-tiedostona"
              :parameters {:query energiatodistus-schema/EnergiatodistusSearch}
              :responses  {200 {:body nil}}
              :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
              :handler    (fn [{{:keys [query]} :parameters :keys [db whoami]}]
                            (api-response/with-exceptions
                              #(api-response/xlsx-response
                                (energiatodistus-xlsx-service/find-energiatodistukset-xlsx
                                 db
                                 whoami
                                 (update query :where json/read-value))
                                "energiatodistukset.xlsx"
                                "Not found.")
                              search-exceptions))}}]
      ["/korvattavat"
       {:get {:summary    "Hae energiatodistukseen liittyvät energiatodistukset,
                           jotka mahdollisesti pitäisi korvata"
              :parameters {:query
                           {(schema/optional-key :rakennustunnus) schema/Str
                            (schema/optional-key :postinumero)    geo-schema/PostinumeroFI
                            (schema/optional-key :katuosoite-fi)  schema/Str
                            (schema/optional-key :katuosoite-sv)  schema/Str}}
              :responses  {200 {:body [energiatodistus-schema/EnergiatodistusForAnyLaatija]}}
              :access     rooli-service/energiatodistus-reader?
              :handler    (fn [{{:keys [query]} :parameters :keys [db]}]
                            (r/response (energiatodistus-service/find-korvattavat db query)))}}]
      ["/all"
       ["/:id"
        [""
         {:get {:summary    "Hae mikä tahansa yksittäinen energiatodistus tunnisteella (id)"
                :parameters {:path {:id common-schema/Key}}
                :responses  {200 {:body energiatodistus-schema/EnergiatodistusForAnyLaatija}
                             404 {:body schema/Str}}
                :access     rooli-service/energiatodistus-reader?
                :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                              (api-response/get-response
                                (energiatodistus-service/find-energiatodistus-any-laatija db id)
                                (str "Energiatodistus " id " does not exists.")))}}]
        ["/viestit"
         {:get {:summary    "Hae energiatodistuksen viestit"
                :parameters {:path {:id common-schema/Key}}
                :responses  {200 {:body [viesti-schema/Ketju]}
                             404 {:body schema/Str}}
                :access     rooli-service/energiatodistus-reader?
                :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                              (api-response/get-response
                                (viesti-service/find-energiatodistus-ketjut db whoami id)
                                (str "Energiatodistus " id " does not exists.")))}}]
        history-api/routes]]
      ["/2013"
       ["" (crud-api/post 2013 energiatodistus-schema/EnergiatodistusSave2013)]
       ["/:id"
        (crud-api/gpd-routes energiatodistus-schema/Energiatodistus2013
                             energiatodistus-schema/EnergiatodistusSave2013)
        (pdf-route 2013)
        crud-api/discarded
        liite-api/routes
        signing-api/routes]]

      ["/2018"
       ["" (crud-api/post 2018 energiatodistus-schema/EnergiatodistusSave2018)]
       ["/:id"
        (crud-api/gpd-routes energiatodistus-schema/Energiatodistus2018
                             energiatodistus-schema/EnergiatodistusSave2018)
        (pdf-route 2018)
        crud-api/discarded
        liite-api/routes
        signing-api/routes]]]

     ["/validation/numeric/:versio"
      {:get {:summary    "Hae energiatodistuksen numeroarvojen validointisäännöt"
             :parameters {:path {:versio common-schema/Key}}
             :responses  {200 {:body [energiatodistus-schema/NumericValidation]}}
             :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                           (r/response (energiatodistus-service/find-numeric-validations
                                         db versio)))}}]

     ["/validation/required/:versio/bypass"
      {:get {:summary    "Hae voimassaolevan energiatodistuksen pakolliset kentät,
                          joita ei voi ohittaa allekirjoituksessa"
             :parameters {:path {:versio common-schema/Key}}
             :responses  {200 {:body [schema/Str]}}
             :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                           (r/response (energiatodistus-service/find-required-properties
                                         db versio true)))}}]

     ["/validation/required/:versio/all"
      {:get {:summary    "Hae voimassaolevan energiatodistuksen kaikki pakolliset kentät.
                          Osa näistä on mahdollista ohittaa allekirjoituksessa."
             :parameters {:path {:versio common-schema/Key}}
             :responses  {200 {:body [schema/Str]}}
             :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                           (r/response (energiatodistus-service/find-required-properties
                                         db versio false)))}}]

     ["/validation/required/:versio"
      {:get {:summary    "Depraceted - same as /validation/required/:versio/all"
             :parameters {:path {:versio common-schema/Key}}
             :responses  {200 {:body [schema/Str]}}
             :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                           (r/response (energiatodistus-service/find-required-properties
                                         db versio false)))}}]

     ["/validation/sisaiset-kuormat/:versio"
      {:get {:summary    "Hae voimassaolevan energiatodistuksen pakolliset kentät"
             :parameters {:path {:versio common-schema/Key}}
             :responses  {200 {:body [(assoc energiatodistus-schema/SisKuormat
                                        :kayttotarkoitusluokka-id common-schema/Key)]}}
             :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                           (r/response (energiatodistus-service/find-sisaiset-kuormat
                                         db versio)))}}]]
    luokittelut-api/routes))

(def external-routes
  [["/energiatodistukset"
    ["/2013" (crud-api/post 2013 energiatodistus-schema/EnergiatodistusSave2013External)]
    ["/2018" (crud-api/post 2018 energiatodistus-schema/EnergiatodistusSave2018External)]
    ["/legacy"
     ["/2013" (xml-api/post 2013)]
     ["/2018" (xml-api/post 2018)]]]])
