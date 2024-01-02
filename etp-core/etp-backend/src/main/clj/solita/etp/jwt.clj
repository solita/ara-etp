(ns solita.etp.jwt
  (:require [clojure.string :as str]
            [buddy.core.keys :as keys]
            [buddy.sign.jwe :as jwe]
            [buddy.sign.jwt :as jwt]
            [clj-http.client :as http]
            [jsonista.core :as json]

            [solita.etp.config :as config]
            [solita.etp.exception :as exception]
            [solita.common.maybe :as maybe]))

(defn- http-get [url resolve error-message]
  (try
    (->> (http/get url {:unexceptional-status #(= 200 %)})
         :body
         resolve
         (maybe/require-some! "HTTP get resolve failed."))
    (catch Throwable t
      (throw (ex-info error-message
                      {:url url :message error-message}
                      t)))))

;;
;; Access token related
;;

(defn public-key-from-jwks [jwks kid]
  (some->> jwks :keys (filter #(= (:kid %) kid)) first keys/jwk->public-key))

(def ^:private mapper (json/object-mapper {:decode-key-fn keyword}))

(defn get-public-key-for-access-token* [trusted-iss kid]
  (http-get (str trusted-iss "/.well-known/jwks.json")
            #(-> % (json/read-value mapper) (public-key-from-jwks kid))
            "Unable to get public key for access token"))

(def ^:private get-public-key-for-access-token (memoize get-public-key-for-access-token*))

;;
;; Data token related
;;

(defn- get-public-key-for-data-token* [base-url kid]
  (http-get (str base-url kid) keys/str->public-key
            "Unable to get public key for data token"))

(def ^:private get-public-key-for-data-token (memoize get-public-key-for-data-token*))

;;
;; Common
;;

(defn jwt-error [jwt jwt-class jwt-part cause]
  {:type :invalid-jwt :part jwt-part :jwt-class jwt-class
   :message (str "Invalid " (name jwt-class) " JWT " (name jwt-part))
   :cause cause
   ;; data jwt contains gdpr information therefore only the header part is logged
   :jwt (if (= jwt-class :data) (first (str/split jwt #"\.")) jwt)})

(defn decode-jwt-payload [jwt public-key jwt-type]
  (exception/redefine-exception
    #(jwt/unsign jwt public-key {:alg    (-> jwt jwe/decode-header :alg)
                                 :leeway 15})
    #(jwt-error jwt jwt-type :payload %)))

(defn decode-kid [jwt jwt-type]
  (exception/redefine-exception
    #(-> jwt jwe/decode-header :kid)
    #(jwt-error jwt jwt-type :header %)))

(defn- get-header! [name headers]
  (->> name
       headers
       (maybe/map* str/trim)
       (maybe/filter* (complement empty?))
       (maybe/require-some!
         (str "Missing required AWS header: " name))))

(defn req->verified-jwt-payloads [{:keys [headers]}]
  (let [[id data access] (mapv #(get-header! % headers)
                               ["x-amzn-oidc-identity"
                                "x-amzn-oidc-data"
                                "x-amzn-oidc-accesstoken"])

        data-public-key (get-public-key-for-data-token
                          config/data-jwt-public-key-base-url
                          (decode-kid data :data))
        data-payload (decode-jwt-payload data data-public-key :data)

        access-public-key (get-public-key-for-access-token
                            config/trusted-jwt-iss
                            (decode-kid access :access))
        access-payload (decode-jwt-payload access access-public-key :access)]

    (when-not (= id (:sub data-payload) (:sub access-payload))
      (exception/illegal-argument!
        "Identity header and data and access token subs does not match."))
    {:data   data-payload
     :access access-payload}))
