(ns solita.etp.service.suomifi-viestit-rest
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:import (java.net URI)))

(def ^{:private true
       :dynamic true}
  *post!* http/post)

(defn- with-access-token [access-token request]
  (let [auth-header {"Authorization" (str "Bearer " access-token)}]
    (update request :headers #(merge auth-header (or % {})))))

(defn get-access-token! [{:keys [rest-base-url rest-password viranomaistunnus]}]
  (let [response (*post!* (str rest-base-url "/v1/token")
                          {:content-type :json
                           :as           :json
                           :form-params  {:password rest-password
                                          :username viranomaistunnus}
                           :throw-exceptions false})
        status (:status response)]
    (when (not (= 200 status))
      (throw (ex-info "Failed to get an access token from Suomifi viestit REST API"
                      {:type :suomifi-viestit-rest-access-token-get
                       :status status})))
    (:access_token (:body response))))

(defn- post-attachment-pdf! [access-token pdf-file pdf-file-name {:keys [rest-base-url]}]
  (let [request {:as        :json
                 :multipart [{:name      pdf-file-name
                              :part-name "file"
                              :content   pdf-file
                              :mime-type "application/pdf"}]
                 :throw-exceptions false}
        response (*post!* (str rest-base-url "/v2/attachments")
                          (with-access-token access-token request))]
    (when (not (= 201 (:status response)))
      (throw (ex-info "Failed to send attachment to Suomifi viestit REST API"
                      {:type    :suomifi-viestit-rest-attachment-send
                       :status  (:status response)
                       :response (:body response)})))
    (:body response)))

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

(defn- post-suomifi-message! [message-info attachment-ref access-token config]
  (let [messages (-> message-info
                    (dissoc :pdf-file)
                    (assoc :attachments [attachment-ref])
                    (->messages config))
        request {:form-params messages
                 :content-type :json
                 :as :json
                 :throw-exceptions false}
        response (*post!* (str (:rest-base-url config) "/v2/messages")
                          (with-access-token access-token request))]
    (when (not (= 200 (:status response)))
      (throw (ex-info (str "Expected 200 OK response from Suomifi viestit REST API, but got "
                           (:status response))
                      {:type    :suomifi-viestit-rest-message-send
                       :status  (:status response)
                       :response (:body response)})))))

(defn send-suomifi-viesti-with-pdf-attachment!
  [{:keys [external-id]
    :as   message-info}
   config]
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
  (log/info "Sending suomifi viesti with external-id: " external-id)
  (try
    (let [access-token (get-access-token! config)
          attachment-ref (post-attachment-pdf! access-token
                                               (:pdf-file message-info)
                                               (:pdf-file-name message-info)
                                               config)]
      (post-suomifi-message! message-info
                             attachment-ref
                             access-token
                             config)
      (log/info "Successfully sent suomifi viesti with external-id: " external-id))
    (catch Exception e
      (let [msg (str "Failed to send suomifi.fi viesti with id "
                     (:external-id message-info) ": " (.getMessage e))]
        (log/error e msg)
        (throw (ex-info msg {:type        :suomifi-viestit-rest-api-failure
                             :external-id external-id}
                        e))))))

(defn validate-config [{:keys [rest-base-url
                               laskutus-salasana
                               laskutus-tunniste
                               palvelutunnus
                               rest-password
                               viranomaistunnus
                               yhteyshenkilo-email]}]
  (flatten
    [(if (str/blank? rest-base-url)
       ["base-url is missing"]
       (try
         (URI. rest-base-url)
         []
         (catch Exception _ [(str "Invalid base URL: " rest-base-url)])))
     (if (str/blank? laskutus-salasana) ["laskutus-salasana is missing"] [])
     (if (str/blank? laskutus-tunniste) ["laskutus-tunniste is missing"] [])
     (if (str/blank? palvelutunnus) ["palvelutunnus is missing"] [])
     (if (str/blank? rest-password) ["rest-password is missing"] [])
     (if (str/blank? viranomaistunnus) ["viranomaistunnus is missing"] [])
     (if (str/blank? yhteyshenkilo-email) ["yhteyshenkilo-email is missing"] [])]))
