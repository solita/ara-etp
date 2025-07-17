(ns solita.etp.service.suomifi-viestit-rest
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [solita.etp.config :as config]))

(def base-url config/suomifi-viestit-rest-base-url)
(def token-endpoint (str base-url "/v1/token"))
(def attachment-endpoint (str base-url "/v2/attachments"))
(def messages-endpoint (str base-url "/v2/messages"))
(def expected-status
  {token-endpoint      200
   attachment-endpoint 201
   messages-endpoint   200})

;; Wrapper for post so that can be bound in tests.
(defn- ^:private ^:dynamic *post!* [url request]
  (if base-url
    (let [unexceptional-status (get expected-status url)]
      (http/post url (merge {:unexceptional-status #(= % unexceptional-status)}
                            request)))
    (do (log/info "Missing suomifi viestit rest base. Skipping request to suomifi viestit...")
        (:status 200))))

(defn- with-access-token [access-token request]
  (let [auth-header {"Authorization" (str "Bearer " access-token)}]
    (update request :headers #(merge auth-header (or % {})))))

(defn- post-json!
  ([url request access-token]
   (*post!* url (merge (with-access-token access-token request) {:content-type :json
                                                                 :as           :json}))))

(defn- get-access-token! [config]
  (let [response (*post!* token-endpoint {:content-type :json
                                          :as           :json
                                          :form-params
                                          {:password (:rest-salasana config)
                                           :username (:viranomaistunnus config)}})]
    (:access_token (:body response))))

(defn- send-attachment-pdf! [access-token pdf-file pdf-file-name]
  (let [request {:as :json
                 :multipart
                 [{:name pdf-file-name :part-name "file" :content pdf-file :mime-type "application/pdf"}
                  ]}]
    (:body (*post!* attachment-endpoint (with-access-token access-token request)))))

;; Stable API
(defn ->messages [{:keys [attachments title body external-id recipient-id city country-code name street-address zip-code]} config]
  {:electronic {:bodyFormat         "Text"
                :messageServiceType "Normal"
                :attachments        attachments
                :title              title
                :notifications      {:senderDetailsInNotifications "Organisation and service name"
                                     :unreadMessageNotification    {:reminder "Default reminder"}}
                :replyAllowedBy     "No one"
                :body               body
                :visibility         "Normal"}
   :externalId external-id
   :paperMail  {:messageServiceType           "Normal"
                :createAddressPage            false
                :rotateLandscapePages         true
                :attachments                  attachments
                :recipient                    {:address {:city          city
                                                         :countryCode   country-code
                                                         :name          name
                                                         :streetAddress street-address
                                                         :zipCode       zip-code}}
                :sender                       {:address {:city          "Valtioneuvosto"
                                                         :countryCode   "FI"
                                                         :name          "Ympäristöministeriö, Valtion tukeman asuntorakentamisen keskus"
                                                         :streetAddress "PL 35"
                                                         :zipCode       "00023"}}
                :twoSidedPrinting             true
                :colorPrinting                true
                :printingAndEnvelopingService {:postiMessaging {:contactDetails {:email (:yhteyshenkilo-email config)}
                                                                :password       (:laskutus-salasana config)
                                                                :username       (:laskutus-tunniste config)}}}
   :recipient  {:id recipient-id}
   :sender     {:serviceId (:palvelutunnus config)}})

(defn send-suomifi-viesti-with-pdf-attachment!
  "Sends a Suomi.fi-viesti that has the `pdf-file` as an attachment.

  The function expects a map containing the following keys:

  Required keys:
  - :pdf-file       InputStream  - The PDF attachment that already has address page for paper mail.
  - :pdf-file-name  string       - The name used for the PDF attachment.
  - :title          string       - The title of the message.
  - :body           string       - The body of the message.
  - :external-id    string       – A unique identifier for the message (tunniste).
  - :recipient-id   string       – The ID (hetu or y-tunnus) of the user submitting the job.
  - :city           string       - The recipient's city.
  - :country-code   string       - The recipient's country code.
  - :name           string       - The recipient's name.
  - :street-address string       - The recipient's street address.
  - :zip-code       string       - The recipient's zip code.

  Returns: response"
  [{:keys [pdf-file pdf-file-name external-id] :as message-info} &
   [config]]
  (try
    (let [default-config {:viranomaistunnus    config/suomifi-viestit-viranomaistunnus
                          :palvelutunnus       config/suomifi-viestit-palvelutunnus
                          :yhteyshenkilo-email config/suomifi-viestit-yhteyshenkilo-email
                          :laskutus-tunniste   config/suomifi-viestit-laskutus-tunniste
                          :laskutus-salasana   config/suomifi-viestit-laskutus-salasana
                          :rest-salasana       config/suomifi-viestit-rest-password}
          config (merge default-config config)
          _ (log/info "Getting access-token for suomifi viesti: " external-id)
          access-token (get-access-token! config)
          _ (log/info "Sending attachment for suomifi viesti: " external-id)
          attachment-ref (send-attachment-pdf! access-token pdf-file pdf-file-name)
          request (-> message-info
                      (dissoc :pdf-file)
                      (assoc :attachments [attachment-ref])
                      (->messages config))]
      (log/info "Sending suomifi viesti: " external-id)
      (post-json! messages-endpoint {:form-params request} access-token))
    (catch Exception cause
      (throw
        (ex-info
          (str "Sending suomifi message with external-id " external-id " failed.")
          {:type        :suomifi-viestit-rest-api-failure
           :external-id external-id
           :response    (-> cause ex-data :body)
           :cause       (ex-data cause)}
          cause)))))
