(ns solita.etp.api.perusparannuspassi
  (:require
    [ring.util.response :as r]
    [schema.core :as schema]
    [solita.etp.api.response :as api-response]
    [solita.etp.service.rooli :as rooli-service]
    [solita.etp.schema.common :as common-schema]
    [solita.etp.schema.perusparannuspassi :as ppp-schema]
    [solita.etp.schema.validation :as validation-schema]
    [solita.etp.service.perusparannuspassi :as ppp-service]
    [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf]))

(defn not-implemented! [& _]
  (throw (ex-info "Not implemented" {:type :not-implemented})))

(defn- valid-ppp-pdf-filename? [filename id kieli]
  (= filename (format "perusparannuspassi-%s-%s.pdf" id kieli)))

(defn- valid-ppp-html-filename? [filename id kieli]
  (= filename (format "perusparannuspassi-%s-%s.html" id kieli)))

(defn pdf-route []
  ["/pdf/:kieli/:filename"
   {:get {:summary    "Lataa perusparannuspassi PDF-tiedostona"
          :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
          :parameters {:path {:id common-schema/Key
                              :kieli schema/Str
                              :filename schema/Str}}
          :responses  {200 {:body nil}
                       404 {:body schema/Str}}
          :handler    (fn [{{{:keys [id kieli filename]} :path} :parameters :keys [db whoami]}]
                        (if (valid-ppp-pdf-filename? filename id kieli)
                          (api-response/pdf-response
                            (ppp-pdf/find-perusparannuspassi-pdf db whoami id kieli)
                            filename
                            (str "Perusparannuspassi " id " does not exist."))
                          (r/not-found "File not found")))}}])

(defn html-route []
  ["/html/:kieli/:filename"
   {:get {:summary    "Lataa perusparannuspassi HTML-tiedostona"
          :access     (some-fn rooli-service/laatija? rooli-service/paakayttaja?)
          :parameters {:path {:id common-schema/Key
                              :kieli schema/Str
                              :filename schema/Str}}
          :responses  {200 {:body nil}
                       404 {:body schema/Str}}
          :handler    (fn [{{{:keys [id kieli filename]} :path} :parameters :keys [db whoami]}]
                        (if (valid-ppp-html-filename? filename id kieli)
                          (api-response/html-response
                            (ppp-pdf/find-perusparannuspassi-html db whoami id kieli)
                            (str "Perusparannuspassi " id " does not exist."))
                          (r/not-found "File not found")))}}])

(def private-routes
  [["/perusparannuspassit"
   ["/2026"
    [""
     {:post {:summary    "Lisää energiatodistukselle perusparannuspassi"
             :parameters {:body ppp-schema/PerusparannuspassiSave}
             :responses  {201 {:body common-schema/IdAndWarnings}}
             :access     rooli-service/ppp-laatija?
             :handler    (fn [{{:keys [body]} :parameters
                               :keys [db whoami uri]}]
                           (api-response/with-exceptions
                             (fn []
                               (api-response/created uri (ppp-service/insert-perusparannuspassi! db whoami body)))
                             [{:type :energiatodistus-not-found :response 404}
                              {:type :foreign-key-violation :response 400}
                              {:type :invalid-energiatodistus-versio :response 400}
                              {:type :invalid-energiatodistus-id :response 400}
                              {:type :invalid-value :response 400}]))}}]
    ["/:id"
     (pdf-route)
     (html-route)
     [""
      {:get    {:summary    "Hae perusparannuspassi tunnisteella (id)"
                :parameters {:path {:id common-schema/Key}}
                :responses  {200 {:body ppp-schema/Perusparannuspassi}
                             404 {:body schema/Str}}
                :access     rooli-service/ppp-reader?
                :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                              (api-response/get-response
                                (ppp-service/find-perusparannuspassi db whoami id)
                                (str "Perusparannuspassi " id " does not exist.")))}
       :put    {:summary    "Päivitä energiatodistuksen perusparannuspassin tietoja"
                :parameters {:path {:id common-schema/Key}
                             :body ppp-schema/PerusparannuspassiSave}
                :responses  {200 {:body nil}
                             404 {:body common-schema/GeneralError}}
                :access     (some-fn rooli-service/ppp-laatija? rooli-service/paakayttaja?)
                :handler    (fn [{{{:keys [id]} :path :keys [body]} :parameters :keys [db whoami]}]
                              (api-response/response-with-exceptions
                                #(ppp-service/update-perusparannuspassi! db whoami id body)
                                [{:type :energiatodistus-not-found :response 404}
                                 {:type :foreign-key-violation :response 400}
                                 {:type :invalid-energiatodistus-versio :response 400}
                                 {:type :invalid-energiatodistus-id :response 400}
                                 {:type :invalid-value :response 400}]))}
       :delete {:summary    "Poista energiatodistukselta perusparannuspassi"
                :parameters {:path {:id common-schema/Key}}
                :responses  {200 {:body nil}
                             404 {:body common-schema/GeneralError}}
                :access     rooli-service/ppp-laatija?
                :handler    (fn [{{{:keys [id]} :path} :parameters :keys [db whoami]}]
                              (api-response/ok|not-found
                                (ppp-service/delete-perusparannuspassi!
                                  db whoami id)
                                (str "perusparannuspassi " id " does not exists.")))}}]]

    ["/validation/numeric/:versio"
     {:get {:summary    "Hae perusparannuspassin numeroarvojen validointisäännöt"
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [validation-schema/NumericValidation]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-numeric-validations
                                        db versio)))}}]

    ["/validation/required/:versio/bypass"
     {:get {:summary    "Hae voimassaolevan perusparannuspassin pakolliset kentät,
                         joita ei voi ohittaa allekirjoituksessa"
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [schema/Str]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-required-properties
                                        db versio true)))}}]

    ["/validation/required/:versio/all"
     {:get {:summary    "Hae voimassaolevan perusparannuspassin kaikki pakolliset kentät.
                         Osa näistä on mahdollista ohittaa allekirjoituksessa."
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [schema/Str]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-required-properties
                                        db versio false)))}}]

    ["/vaihe/validation/numeric/:versio"
     {:get {:summary    "Hae perusparannuspassin vaiheen numeroarvojen validointisäännöt"
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [validation-schema/NumericValidation]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-vaihe-numeric-validations
                                        db versio)))}}]

    ["/vaihe/validation/required/:versio/bypass"
     {:get {:summary    "Hae voimassaolevan perusparannuspassin vaiheen pakolliset kentät,
                         joita ei voi ohittaa allekirjoituksessa"
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [schema/Str]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-vaihe-required-properties
                                        db versio true)))}}]

    ["/vaihe/validation/required/:versio/all"
     {:get {:summary    "Hae voimassaolevan perusparannuspassin vaiheen kaikki pakolliset kentät.
                         Osa näistä on mahdollista ohittaa allekirjoituksessa."
            :parameters {:path {:versio common-schema/Key}}
            :responses  {200 {:body [schema/Str]}}
            :handler    (fn [{{{:keys [versio]} :path} :parameters :keys [db]}]
                          (r/response (ppp-service/find-ppp-vaihe-required-properties
                                        db versio false)))}}]]]])
