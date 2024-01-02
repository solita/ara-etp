(ns solita.etp.service.suomifi-viestit
  (:require [clostache.parser :as clostache]
            [solita.etp.config :as config]
            [clj-http.client :as http]
            [schema-tools.coerce :as sc]
            [clojure.tools.logging :as log]
            [solita.common.xml :as xml]
            [schema.core :as schema]
            [clj-http.conn-mgr :as conn-mgr]
            [clojure.string :as str]
            [solita.etp.exception :as exception])
  (:import (java.util Properties)
           (java.io ByteArrayInputStream ByteArrayOutputStream)
           (org.apache.xml.security Init)
           (org.apache.axis.soap MessageFactoryImpl)
           (org.apache.wss4j.common WSEncryptionPart)
           (org.apache.wss4j.common.crypto CryptoFactory)
           (org.apache.wss4j.dom WSConstants)
           (org.apache.wss4j.dom.message WSSecHeader WSSecSignature WSSecTimestamp)
           (org.apache.xml.security.c14n Canonicalizer)
           (org.apache.axis.configuration NullProvider)
           (org.apache.axis.client AxisClient)
           (org.apache.axis MessageContext Message)))

;; register default algorithms
(Canonicalizer/registerDefaultAlgorithms)
(Init/init)

(defn- request-create-xml [data]
  (clostache/render-resource (str "suomifi/viesti.xml") data))

(defn- ^:dynamic post! [request]
  (log/debug request)
  (if config/suomifi-viestit-endpoint-url
    (http/post config/suomifi-viestit-endpoint-url
               (cond-> {:body             request
                        :throw-exceptions false}
                       config/suomifi-viestit-proxy?
                       (assoc
                         :connection-manager
                         (conn-mgr/make-socks-proxied-conn-manager "localhost" 1080))))
    (do
      (log/info "Missing suomifi viestit endpoint url. Skip request to suomifi viestit...")
      {:status 200})))

(defn- raw-request->document [request]
  (with-open [inStream (ByteArrayInputStream. (.getBytes request))]
    (let [engine (AxisClient. (NullProvider.))
          msgContext (MessageContext. engine)
          axisMessage (doto (Message. inStream)
                        (.setMessageContext msgContext))]
      (-> axisMessage .getSOAPEnvelope .getAsDocument))))

(defn- document->signed-request [document]
  (let [c14n (Canonicalizer/getInstance Canonicalizer/ALGO_ID_C14N_WITH_COMMENTS)]
    (with-open [os (ByteArrayOutputStream.)]
      (.canonicalizeSubtree c14n document os)
      (-> (.createMessage (MessageFactoryImpl.) nil (ByteArrayInputStream. (.toByteArray os)))
          .getSOAPEnvelope .getAsString))))

(defn- signSOAPEnvelope [request keystore-file keystore-password keystore-alias]
  (let [properties (doto (Properties.)
                     (.setProperty "org.apache.ws.security.crypto.merlin.keystore.file" keystore-file)
                     (.setProperty "org.apache.ws.security.crypto.merlin.keystore.password", keystore-password)
                     (.setProperty "org.apache.ws.security.crypto.merlin.keystore.type", "JKS")
                     (.setProperty "signer.username" keystore-alias)
                     (.setProperty "signer.password" keystore-password))
        crypto (CryptoFactory/getInstance properties)
        doc (raw-request->document request)
        header (doto (WSSecHeader. doc)
                 (.setMustUnderstand true)
                 (.insertSecurityHeader))
        signer (doto (WSSecSignature. header)
                 (.setUserInfo keystore-alias keystore-password)
                 (.setKeyIdentifierType WSConstants/BST_DIRECT_REFERENCE)
                 (.setUseSingleCertificate true))
        timestampPart (WSEncryptionPart. "Timestamp", WSConstants/WSU_NS, "")
        bodyPart (WSEncryptionPart. WSConstants/ELEM_BODY, WSConstants/URI_SOAP11_ENV, "")]
    (doto (WSSecTimestamp. header)
      (.setTimeToLive 60)
      (.build))
    (.addAll (.getParts signer) (list timestampPart bodyPart))
    (document->signed-request (.build signer crypto))))

(defn- read-response->xml [response]
  (-> response xml/string->xml xml/without-soap-envelope first xml/with-kebab-case-tags))

(defn- trim [s]
  (when s
    (-> s (str/replace #"\s+" " ") str/trim)))

(defn- response-parser [response-soap]
  (let [response-xml (read-response->xml response-soap)]
    {:tila-koodi        (xml/get-content response-xml [:laheta-viesti-result :tila-koodi :tila-koodi])
     :tila-koodi-kuvaus (trim (xml/get-content response-xml [:laheta-viesti-result :tila-koodi :tila-koodi-kuvaus]))
     :sanoma-tunniste   (xml/get-content response-xml [:laheta-viesti-result :tila-koodi :sanoma-tunniste])}))

(def ^:private coerce-response!
  (sc/coercer {:tila-koodi        schema/Int
               :tila-koodi-kuvaus schema/Str
               :sanoma-tunniste   schema/Str}
              sc/string-coercion-matcher))

(defn select-keys* [m paths]
  (into {} (map (fn [p] (assoc-in {} p (get-in m p)))) paths))

(defn- throw-ex-info! [type request response cause]
  (throw
    (ex-info
      (str "Sending suomifi message " (-> request :sanoma :tunniste) " failed.")
      {:type         type
       :endpoint-url config/suomifi-viestit-endpoint-url
       :request      (select-keys* request [[:sanoma :tunniste]
                                            [:kysely :kohteet :nimike]])
       :response     response
       :cause        (ex-data cause)}
      cause)))

(defn- assert-status! [response]
  (when-not (#{200 201 202 203 204 205 206 207 300 301 302 303 304 307} (:status response))
    (exception/illegal-argument! (str "Invalid response status: " (:status response))))
  response)

(defn- read-response [request response]
  (try
    (some-> response assert-status! :body response-parser coerce-response!)
    (catch Throwable t
      (throw-ex-info! :suomifi-viestit-invalid-response request response t))))

(defn- handle-request! [request keystore-file keystore-password keystore-alias]
  (try
    (let [request-xml (request-create-xml request)]
      (if (and keystore-file keystore-password keystore-alias)
        (post! (signSOAPEnvelope request-xml keystore-file keystore-password keystore-alias))
        (post! request-xml)))
    (catch Throwable t
      (throw-ex-info! :suomifi-viestit-failure request nil t))))

(defn- assert-tila-koodi! [request response]
  (if (not= (:tila-koodi response) 202)
    (throw-ex-info! :suomifi-viestit-invalid-request request response nil)
    response))

(defn- send-request! [request keystore-file keystore-password keystore-alias]
  (some->> (handle-request! request keystore-file keystore-password keystore-alias)
           (read-response request)
           (assert-tila-koodi! request)))

(defn send-message! [sanoma
                     kohde
                     & [{:keys [viranomaistunnus palvelutunnus varmenne tulostustoimittaja
                                yhteyshenkilo-nimi yhteyshenkilo-email
                                laskutus-tunniste laskutus-salasana
                                paperitoimitus? laheta-tulostukseen?
                                keystore-file keystore-password keystore-alias]
                         :or   {viranomaistunnus     config/suomifi-viestit-viranomaistunnus
                                palvelutunnus        config/suomifi-viestit-palvelutunnus
                                varmenne             config/suomifi-viestit-varmenne
                                tulostustoimittaja   config/suomifi-viestit-tulostustoimittaja
                                yhteyshenkilo-nimi   config/suomifi-viestit-yhteyshenkilo-nimi
                                yhteyshenkilo-email  config/suomifi-viestit-yhteyshenkilo-email
                                laskutus-tunniste    config/suomifi-viestit-laskutus-tunniste
                                laskutus-salasana    config/suomifi-viestit-laskutus-salasana
                                paperitoimitus?      config/suomifi-viestit-paperitoimitus?
                                laheta-tulostukseen? config/suomifi-viestit-laheta-tulostukseen?
                                keystore-file        config/suomifi-viestit-keystore-file
                                keystore-password    config/suomifi-viestit-keystore-password
                                keystore-alias       config/suomifi-viestit-keystore-alias}}]]
  (let [data {:viranomainen {:viranomaistunnus viranomaistunnus
                             :palvelutunnus    palvelutunnus
                             :yhteyshenkilo    {:nimi  yhteyshenkilo-nimi
                                                :email yhteyshenkilo-email}}
              :sanoma       (assoc sanoma :varmenne varmenne)
              :kysely       (cond-> {:kohteet              kohde
                                     :tulostustoimittaja   tulostustoimittaja
                                     :paperitoimitus?      paperitoimitus?
                                     :laheta-tulostukseen? laheta-tulostukseen?}
                                    (and (seq laskutus-tunniste) (seq laskutus-salasana))
                                    (assoc :laskutus {:tunniste laskutus-tunniste
                                                      :salasana laskutus-salasana}))}]
    (send-request! data keystore-file keystore-password keystore-alias)))