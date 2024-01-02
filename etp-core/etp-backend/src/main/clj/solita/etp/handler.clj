(ns solita.etp.handler
  (:require [clojure.string :as str]
            [clojure.walk :as w]
            [muuntaja.core :as m]
            [reitit.coercion.schema]
            [reitit.dev.pretty :as pretty]
            [reitit.openapi :as openapi]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.spec :as rs]
            [schema.coerce]
            [reitit.swagger-ui :as swagger-ui]
            [ring.middleware.cookies :as cookies]
            [schema.core]
            [schema.core :as s]
            [solita.common.cf-signed-url :as signed-url]
            [solita.etp.api.aineisto :as aineisto-api]
            [solita.etp.api.energiatodistus :as energiatodistus-api]
            [solita.etp.api.geo :as geo-api]
            [solita.etp.api.kayttaja :as kayttaja-api]
            [solita.etp.api.laatija :as laatija-api]
            [solita.etp.api.laskutus :as laskutus-api]
            [solita.etp.api.palveluvayla :as palveluvayla]
            [solita.etp.api.sivu :as sivu-api]
            [solita.etp.api.statistics :as statistics-api]
            [solita.etp.api.valvonta :as valvonta-api]
            [solita.etp.api.valvonta-kaytto :as valvonta-kaytto-api]
            [solita.etp.api.valvonta-oikeellisuus :as valvonta-oikeellisuus-api]
            [solita.etp.api.viesti :as viesti-api]
            [solita.etp.api.yritys :as yritys-api]
            [solita.etp.config :as config]
            [solita.etp.exception :as exception]
            [solita.etp.header-middleware :as header-middleware]
            [solita.etp.jwt :as jwt]
            [solita.etp.schema.common :as schema.common]
            [solita.etp.security :as security])
  (:import (java.net URLEncoder)
           (java.nio.charset StandardCharsets)))

(defn tag [tag routes]
  (w/prewalk
    #(if (and (map? %) (contains? % :summary))
       (assoc % :tags #{tag}) %)
    routes))

(defn openapi-id
  "Adds an openapi and swagger id to all given routes. The id is used by openapi and swagger generators to include the routes in the generated documentation."
  [id routes]
  (w/prewalk
    #(if (and (map? %) ((some-fn :get :post :patch :delete :options :head) %))
       (-> %
           (assoc :swagger {:id #{id}})
           (assoc :openapi {:id #{id}}))
       %)
    routes))

(defn- req->jwt [request]
  (try
    (jwt/req->verified-jwt-payloads request)
    (catch Throwable _)))

(defn logout-location [req]
  (let [{:keys [data]} (req->jwt req)]
    (if-let [id-token (:custom:id_token data)]
      (if (:custom:VIRTU_localID data)
        (str config/keycloak-virtu-logout-url
             "?id_token_hint=" id-token
             "&post_logout_redirect_uri=" (URLEncoder/encode config/cognito-logout-url StandardCharsets/UTF_8))
        (str config/keycloak-suomifi-logout-url
             "?id_token_hint=" id-token
             "&post_logout_redirect_uri=" (URLEncoder/encode config/cognito-logout-url StandardCharsets/UTF_8)))
      (str config/index-url "/uloskirjauduttu"))))

(def empty-cookie {:value     ""
                   :path      "/"
                   :max-age   0
                   :http-only true
                   :secure    true})

(def system-routes
  [["/openapi.json"
    {:get {:no-doc  true
           :swagger {:info {:title       "Energiatodistuspalvelu API"
                            :description ""}}
           :handler (openapi/create-openapi-handler)}}]
   ["/health"
    {:get {:summary "Health check"
           :tags    #{"System"}
           :handler (constantly {:status 200})}}]
   ["/login"
    {:get {:summary    "Callback used to redirect user back to where they were"
           :tags       #{"System"}
           :parameters {:query {:redirect s/Str}}
           :handler    (fn [{:keys [parameters]}]
                         (let [redirect (-> parameters :query :redirect)]
                           {:status  302
                            :headers {"Location" (if (str/starts-with?
                                                       redirect
                                                       config/index-url)
                                                   redirect
                                                   config/index-url)}}))}}]
   ["/logout"
    {:get {:summary    "Callback used to redirect user to cognito logout"
           :tags       #{"System"}
           :middleware [[cookies/wrap-cookies]]
           :handler    (fn [req]
                         {:status  302
                          :headers {"Location" (logout-location req)}
                          :cookies {"AWSELBAuthSessionCookie-0" empty-cookie
                                    "AWSELBAuthSessionCookie-1" empty-cookie}})}}]
   ;; TODO Temporary endpoint for seeing headers added by load balancer
   ["/headers"
    {:get {:summary "Endpoint for seeing request headers"
           :tags    #{"System"}
           :handler (fn [{:keys [headers]}]
                      {:status 200
                       :body   headers})}}]])

(def routes
  ["" {:middleware [[header-middleware/wrap-default-cache]
                 [header-middleware/wrap-default-content-type]]}
   ["/api"
    system-routes
    ["/public" {:middleware [[security/wrap-db-application-name]]}
     (concat (tag "Laatijat Public API" laatija-api/public-routes)
             (tag "Geo Public API" geo-api/routes)
             (tag "Energiatodistus Public API"
                  energiatodistus-api/public-routes)
             (tag "Tilastointi Public API"
                  statistics-api/routes))]
    ["/private" {:middleware [[header-middleware/wrap-disable-cache]
                              [security/wrap-jwt-payloads]
                              [security/wrap-whoami-from-jwt-payloads]
                              [security/wrap-access]
                              [security/wrap-db-application-name]]}
     (concat (tag "Käyttäjä API" kayttaja-api/routes)
             (tag "Yritys API" yritys-api/routes)
             (tag "Laatijat Private API" laatija-api/private-routes)
             (tag "Geo Private API" geo-api/routes)
             (tag "Energiatodistus API" energiatodistus-api/private-routes)
             (tag "Oikeellisuuden valvonta API" valvonta-oikeellisuus-api/routes)
             (tag "Käytönvalvonta API" valvonta-kaytto-api/routes)
             (tag "Valvonta API" valvonta-api/routes)
             (tag "Viesti API" viesti-api/routes)
             (tag "Sivu API" sivu-api/routes)
             (tag "Tilastointi API" statistics-api/routes))]
    ["/external" {:middleware [[security/wrap-whoami-from-basic-auth
                                "Access to external API"]
                               [security/wrap-access]
                               [security/wrap-db-application-name]]}
     (concat (tag "Energiatodistus API" energiatodistus-api/external-routes)
             (tag "Aineisto API" aineisto-api/external-routes))]
    ["/signed" {:middleware [(if (every? (comp not empty?)
                                         [config/public-index-url
                                          config/url-signing-public-key])
                               ;; Parameters for checking the signature are available,
                               ;; so make use of them
                               [security/wrap-whoami-from-signed
                                config/public-index-url
                                {:key-pair-id config/url-signing-key-id
                                 :public-key  (signed-url/pem-string->public-key
                                                config/url-signing-public-key)}]
                               ;; Otherwise, assume that the reverse
                               ;; proxies on front of the backend
                               ;; service have verified the signature.
                               [security/wrap-whoami-assume-verified-signature])
                             [security/wrap-access]
                             [security/wrap-db-application-name]]}
     (concat (tag "Aineisto API" aineisto-api/signed-routes))]
    ["/internal"
     (concat (tag "Laskutus API" laskutus-api/routes)
             (tag "Laatija Internal API" laatija-api/internal-routes)
             (tag "Aineisto Internal API" aineisto-api/internal-routes))]]
   (when config/allow-palveluvayla-api
     ["/palveluvayla" ["/openapi.json" {:get {:no-doc  true :openapi {:info {:title "Energiatodistuspalvelu API" :description "Hae energiatodistuksia pdf tai json muodoissa"}
                                                                      :id   "Palveluväylä"}
                                              :handler (openapi/create-openapi-handler)}}]
      (openapi-id "Palveluväylä" palveluvayla/routes)])])

(def route-opts
  {;; Uncomment line below to see diffs of requests in middleware chain
   ;;:reitit.middleware/transform dev/print-request-diffs
   :exception pretty/exception
   :validate  rs/validate
   :data      {:coercion   reitit.coercion.schema/coercion
               :muuntaja   m/instance
               :middleware [openapi/openapi-feature
                            parameters/parameters-middleware
                            muuntaja/format-negotiate-middleware
                            muuntaja/format-response-middleware
                            exception/exception-middleware
                            muuntaja/format-request-middleware
                            coercion/coerce-response-middleware
                            coercion/coerce-request-middleware
                            multipart/multipart-middleware]}})

(def router (ring/router routes route-opts))

(def handler
  (ring/ring-handler router
                     (ring/routes
                       (swagger-ui/create-swagger-ui-handler
                         {:path             "/api/documentation"
                          :url              "/api/openapi.json"
                          :config           {:validationUrl nil}
                          :operationsSorter "alpha"})
                       (swagger-ui/create-swagger-ui-handler
                         {:path             "/api/palveluvayla/openapi"
                          :url              "/api/palveluvayla/openapi.json"
                          :config           {:validationUrl nil}
                          :operationsSorter "alpha"})
                       (ring/create-default-handler))))
