(ns solita.etp.api.palveluvayla
  (:require [clojure.string :as str]
            [schema.core :as s]
            [solita.etp.api.response :as api-response]
            [solita.etp.schema.common :as schema.common]
            [solita.etp.schema.energiatodistus :as schema.energiatodistus]
            [solita.etp.service.palveluvayla :as service.palveluvayla]))

(def accept-language-header {(s/optional-key :accept-language) schema.common/AcceptLanguage})

(def energiatodistus-id-parameter {:id schema.common/EnergiatodistusId})

(defn- parse-lang
  "Parse a language from language tag i.e. drop subtags such as region separated by -"
  [s]
  (-> s (str/split #"-") first))

(defn- parse-locale [s]
  (let [parts (str/split s #";q=")]
    (case (count parts)
      1 [(parse-lang s) 1.0]
      2 [(-> parts first parse-lang) (Double/parseDouble (second parts))])))

(defn- parse-accept-language [s]
  (map parse-locale (map str/trim (str/split s #","))))

(defn parse-preferred-language-order
  "Sort Accept-Language headers by quality and return only the languages tags in order of preference"
  [accept-language]
  (some->> accept-language
           (parse-accept-language)
           (sort-by second)
           (reverse)
           (map first)))

(def routes ["/v1/energiatodistukset"
             ["/pdf/:id"
              ["" {:get {:summary    "Hae PDF-muotoinen energiatodistus tunnisteella"
                         :parameters {:path   energiatodistus-id-parameter
                                      :header accept-language-header}
                         :responses  {200 {:body nil}
                                      404 {:body s/Str}}
                         :handler    (fn [{:keys                               [db aws-s3-client],
                                           {{:keys [accept-language]} :header} :parameters
                                           {{:keys [id]} :path}                :parameters}]
                                       (let [language-preference-order (parse-preferred-language-order accept-language)]
                                         (api-response/pdf-response ; Return the first language version that exists if any
                                           (service.palveluvayla/find-first-existing-pdf id language-preference-order db aws-s3-client)
                                           "energiatodistus.pdf"
                                           (str "Energiatodistus " id " does not exists."))))
                         :openapi    {:responses {200 {:description "PDF-muotoinen energiatodistus"
                                                       :content     {:application/pdf {:schema {:type   "string"
                                                                                                :format "binary"}}}}}}}}]]
             ["/json"
              ["/any"
               ["" {:get {:summary    "Hae energiatodistusten perustietoja. Palauttaa tiedot sekä 2013, että 2018 lain mukaisista energiatodistuksista. Tarkemmat tiedot saa hakemalla todistuksen oikean version"
                          :parameters {:query {:rakennustunnus schema.common/Rakennustunnus}}
                          :responses  {200 {:body [schema.energiatodistus/EnergiatodistusForAnyLaatija]}}
                          :handler    (fn [{{:keys [query]} :parameters :keys [db]}]
                                        (api-response/get-response
                                          (service.palveluvayla/search-by-rakennustunnus (:rakennustunnus query) schema.energiatodistus/EnergiatodistusForAnyLaatija db)
                                          (str "Virhe haussa")))}}]
               ["/:id" {:get {:summary    "Hae yksittäinen energiatodistus todistuksen tunnuksen perusteella. Palauttaa perustiedot sekä 2013, että 2018 lain mukaisista energiatodistuksista. Tarkemmat tiedot saa hakemalla todistuksen oikean version"
                              :parameters {:path {:id schema.common/Key}}
                              :responses  {200 {:body schema.energiatodistus/EnergiatodistusForAnyLaatija}
                                           404 {:body s/Str}}
                              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                                            (api-response/get-response
                                              (service.palveluvayla/get-by-id id db)
                                              (str "Energiatodistus " id " does not exists.")))}}]]
              ["/2013"
               ["" {:get {:summary    "Hae json-muotoisia energiatodistuksia, jotka on laadittu vuoden 2013 säännösten mukaan"
                          :parameters {:query {:rakennustunnus schema.common/Rakennustunnus}}
                          :responses  {200 {:body [schema.energiatodistus/Energiatodistus2013]}}
                          :handler    (fn [{{:keys [query]} :parameters :keys [db]}]
                                        (api-response/get-response
                                          (service.palveluvayla/search-by-rakennustunnus (:rakennustunnus query) schema.energiatodistus/Energiatodistus2013 db 2013)
                                          (str "Virhe haussa")))}}]
               ["/:id" {:get {:summary    "Hae yksittäinen vuoden 2013 säännösten mukainen energiatodistus todistuksen tunnuksen perusteella json-muodossa"
                              :parameters {:path {:id schema.common/Key}}
                              :responses  {200 {:body schema.energiatodistus/Energiatodistus2013}
                                           404 {:body s/Str}}
                              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                                            (api-response/get-response
                                              (service.palveluvayla/get-by-id id db 2013)
                                              (str "Energiatodistus " id " does not exists.")))}}]]
              ["/2018"
               ["" {:get {:summary    "Hae json-muotoisia energiatodistuksia, jotka on laadittu vuoden 2018 säännösten mukaan"
                          :parameters {:query {:rakennustunnus schema.common/Rakennustunnus}}
                          :responses  {200 {:body [schema.energiatodistus/Energiatodistus2018]}}
                          :handler    (fn [{{:keys [query]} :parameters :keys [db]}]
                                        (api-response/get-response
                                          (service.palveluvayla/search-by-rakennustunnus (:rakennustunnus query) schema.energiatodistus/Energiatodistus2018 db 2018)
                                          (str "Virhe haussa")))}}]
               ["/:id" {:get {:summary    "Hae yksittäinen vuoden 2018 säännösten mukainen energiatodistus todistuksen tunnuksen perusteella json-muodossa"
                              :parameters {:path {:id schema.common/Key}}
                              :responses  {200 {:body schema.energiatodistus/Energiatodistus2018}
                                           404 {:body s/Str}}
                              :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db]}]
                                            (api-response/get-response
                                              (service.palveluvayla/get-by-id id db 2018)
                                              (str "Energiatodistus " id " does not exists.")))}}]]]])
