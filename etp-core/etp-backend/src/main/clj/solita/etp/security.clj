(ns solita.etp.security
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [solita.etp.jwt :as jwt]
            [solita.etp.basic-auth :as basic-auth]
            [solita.etp.api.response :as response]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.whoami :as whoami-service]
            [solita.common.cf-signed-url :as signed-url]
            [solita.common.maybe :as maybe]
            [solita.etp.exception :as exception]))

(defn- req->jwt [request]
  (try
    (jwt/req->verified-jwt-payloads request)
    (catch Throwable t
      (log/error t
        (str "Invalid JWT in service request: " (exception/service-name request) ".")
        (maybe/fold "" #(format "Exception: %s." %) (dissoc (ex-data t) :message))))))

(defn wrap-jwt-payloads [handler]
  (fn [req]
    (if-let [jwt (req->jwt req)]
      (handler (assoc req :jwt-payloads jwt))
      response/forbidden)))

(defn log-safe-henkilotunnus [henkilotunnus]
  (let [henkilotunnus (or henkilotunnus "")
        char-count (count henkilotunnus)]
    (->> (repeat "*")
         (take (- char-count (min 7 char-count)))
         (apply str)
         (str (subs henkilotunnus 0 (min 7 char-count))))))

(defn wrap-whoami-from-jwt-payloads [handler]
  (fn [{:keys [db jwt-payloads] :as req}]
    (let [{:keys [data]} jwt-payloads
          cognitoid (:sub data)
          whoami-opts {:henkilotunnus (:custom:FI_nationalIN data)
                       :virtu         {:localid     (:custom:VIRTU_localID data)
                                       :organisaatio (:custom:VIRTU_localOrg data)}}
          whoami (whoami-service/find-whoami db whoami-opts)]
      (if whoami
        (->> (cond-> whoami
                     (some? cognitoid)
                     (assoc :cognitoid cognitoid))
             (assoc req :whoami)
             handler)
        (do
          (log/error "Unable to find active kayttaja using the following opts data JWT: "
                     (update whoami-opts :henkilotunnus log-safe-henkilotunnus))
          response/forbidden)))))

(defn wrap-whoami-from-basic-auth [handler realm]
  (fn [{:keys [db] :as req}]
    (let [{:keys [id password]} (basic-auth/req->id-and-password req)
          whoami (whoami-service/find-whoami-by-email-and-api-key db
                                                                  id
                                                                  password)]
      (if whoami
        (handler (assoc req :whoami whoami))
        (do
          (log/error "Unable to find active kayttaja with Basic Auth"
                     {:id id})
          (-> response/unauthorized
              (merge {:headers {"WWW-Authenticate" (format "Basic realm=\"%s\""
                                                           realm)}})))))))

(defn wrap-whoami-assume-verified-signature [handler]
  (fn [req]
    (handler (assoc req :whoami {:id (:presigned kayttaja-service/system-kayttaja)
                                 :rooli -1}))))

(defn first-address [x-forwarded-for]
  (some-> x-forwarded-for (str/split #"[\s,]+") first))

(defn wrap-whoami-from-signed [handler index-url key-map]
  (fn [{:as req
        :keys [uri query-string]
        {:strs [x-forwarded-for]} :headers}]
    (let [signed-url (str index-url uri "?" query-string)]
      (if-let [problem (signed-url/signed-url-problem-now signed-url
                                                          (first-address x-forwarded-for)
                                                          key-map)]
        (do
          (log/warn "Failed to validate signed URL" signed-url problem)
          (assoc response/forbidden :body (str problem)))
        (handler (assoc req :whoami {:id (:presigned kayttaja-service/system-kayttaja)
                                     :rooli -1}))))))

(defn wrap-whoami-for-internal-aineisto-api [handler]
  (fn [req]
    (handler (assoc req :whoami {:id    (:aineisto kayttaja-service/system-kayttaja)
                                 :rooli -1}))))

(defn wrap-access [handler]
  (fn [{:keys [request-method whoami] :as req}]
    (let [access (-> req :reitit.core/match :data (get request-method) :access)]
      (if (or (nil? access)
              (access whoami))
        (handler req)
        (do
          (log/warn "Current käyttäjä did not satisfy the access predicate for route:"
                    {:method request-method
                     :url (-> req :reitit.core/match :template)
                     :whoami whoami})
          response/forbidden)))))

(defn wrap-db-application-name
  ([handler]
   (wrap-db-application-name handler "public"))
  ([handler default-id-or-name]
   (fn [{:keys [whoami] :as req}]
     (handler (assoc-in
               req
               [:db :application-name]
               (format "%s@core.etp%s"
                       (or (:id whoami) default-id-or-name)
                       (:uri req)))))))
