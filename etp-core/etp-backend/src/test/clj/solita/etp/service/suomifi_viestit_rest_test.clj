(ns solita.etp.service.suomifi-viestit-rest-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [solita.etp.service.suomifi-viestit-rest :as rest-service])
  (:import (java.io ByteArrayInputStream)))

(def electronic-config
  {:rest-base-url    "http://example.com"
   :rest-password    "password"
   :viranomaistunnus "testuser"
   :palvelutunnus     "service-id"
   :yhteyshenkilo-email "hello@example.com"})

(def full-config
  (assoc electronic-config
    :laskutus-tunniste "dalskjfhadslkjfahsdlf"
    :laskutus-salasana "asdasdasdasd"))

(t/deftest validate-config-test
  (t/is (seq (rest-service/validate-config {})))
  (t/is (seq (rest-service/validate-config {:rest-base-url "http://example.com"
                                            :rest-password "password"
                                            :viranomaistunnus "testuser"})))
  (t/is (not (seq (rest-service/validate-config electronic-config)))))

(t/deftest get-access-token-test
  (let [url (atom nil)
        req (atom nil)]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (reset! url u)
                                             (reset! req r)
                                             {:status 200} ) }
      (rest-service/get-access-token! full-config))
    (t/is (= "http://example.com/v1/token" @url))))

(t/deftest post-attachment-pdf-test
  (let [url (atom nil)
        req (atom nil)]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (reset! url u)
                                             (reset! req r)
                                             {:status 201} ) }
      (rest-service/post-attachment-pdf! "dummy-access-token"
                                         (ByteArrayInputStream. (.getBytes "Some dummy content"))
                                         "ukaasi.pdf"
                                         full-config))
    (t/is (= "http://example.com/v2/attachments" @url))))

;; Something to match the first parameter to send-suomifi-viesti-with-pdf-attachment!
(def dummy-message
  {:pdf-file       (ByteArrayInputStream. (.getBytes "Some dummy content"))
   :pdf-file-name  "ukaasi.pdf"
   :title          "Dummy title"
   :body           "Dummy body"
   :external-id    "dummy-external-id"
   :recipient-id   "dummy-recipient-id"
   :city           "Dummy City"
   :country-code   "FI"
   :name           "Dummy Name"
   :street-address "Dummy Street Address"
   :zip-code       "12345"})

(def full-params {:electronic {:attachments        ["dummy-attachment-ref"]
                               :body               "Dummy body"
                               :bodyFormat         "Text"
                               :messageServiceType "Normal"
                               :notifications      {:senderDetailsInNotifications "Organisation and service name"
                                                    :unreadMessageNotification    {:reminder "Default reminder"}}
                               :replyAllowedBy     "No one"
                               :title              "Dummy title"
                               :visibility         "Normal"}
                  :externalId "dummy-external-id"
                  :paperMail  {:attachments                  ["dummy-attachment-ref"]
                               :colorPrinting                true
                               :createAddressPage            false
                               :messageServiceType           "Normal"
                               :printingAndEnvelopingService {:postiMessaging {:contactDetails {:email "hello@example.com"}
                                                                               :password       "asdasdasdasd"
                                                                               :username       "dalskjfhadslkjfahsdlf"}}
                               :recipient                    {:address {:city          "Dummy City"
                                                                        :countryCode   "FI"
                                                                        :name          "Dummy Name"
                                                                        :streetAddress "Dummy Street Address"
                                                                        :zipCode       "12345"}}
                               :rotateLandscapePages         true
                               :sender                       {:address {:city          "Valtioneuvosto"
                                                                        :countryCode   "FI"
                                                                        :name          "Ympäristöministeriö, Valtion tukeman asuntorakentamisen keskus"
                                                                        :streetAddress "PL 35"
                                                                        :zipCode       "00023"}}
                               :twoSidedPrinting             true}
                  :recipient  {:id "dummy-recipient-id"}
                  :sender     {:serviceId "service-id"}})

(def electronic-params (dissoc full-params :paperMail))

(t/deftest post-messages-electronic-test
  (let [url (atom nil)
        req (atom nil)]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (reset! url u)
                                             (reset! req r)
                                             {:status 200} ) }
      (rest-service/post-suomifi-message! dummy-message
                                          "dummy-attachment-ref"
                                          "dummy-access-token"
                                          electronic-config))
    (t/is (= "http://example.com/v2/messages/electronic" @url))
    (t/is (= electronic-params (:form-params @req)))))

(t/deftest post-messages-full-test
  (let [url (atom nil)
        req (atom nil)]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (reset! url u)
                                             (reset! req r)
                                             {:status 200} ) }
      (rest-service/post-suomifi-message! dummy-message
                                          "dummy-attachment-ref"
                                          "dummy-access-token"
                                          full-config))
    (t/is (= "http://example.com/v2/messages" @url))
    (t/is (= full-params (:form-params @req)))))

(t/deftest send-full-seq-test
  (let [calls (atom [])]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (swap! calls (fn [calls] (conj calls {:url u :request r})))
                                             (cond
                                               (str/ends-with? u "/v1/token") {:status 200}
                                               (str/ends-with? u "/v2/attachments") {:status 201}
                                               (str/ends-with? u "/v2/messages/electronic") {:status 200}
                                               (str/ends-with? u "/v2/messages") {:status 200}))}
      (try
        (rest-service/send-suomifi-viesti-with-pdf-attachment! dummy-message full-config)
        (catch Exception _ nil)))
    (t/is (= ["http://example.com/v1/token"
              "http://example.com/v2/attachments"
              "http://example.com/v2/messages"]
             (map :url @calls)))))

(t/deftest send-electronic-seq-test
  (let [calls (atom [])]
    (with-bindings {#'rest-service/*post!* (fn [u r]
                                             (swap! calls (fn [calls] (conj calls {:url u :request r})))
                                             (cond
                                               (str/ends-with? u "/v1/token") {:status 200}
                                               (str/ends-with? u "/v2/attachments") {:status 201}
                                               (str/ends-with? u "/v2/messages/electronic") {:status 200}
                                               (str/ends-with? u "/v2/messages") {:status 200}))}
      (try
        (rest-service/send-suomifi-viesti-with-pdf-attachment! dummy-message electronic-config)
        (catch Exception _ nil)))
    (t/is (= ["http://example.com/v1/token"
              "http://example.com/v2/attachments"
              "http://example.com/v2/messages/electronic"]
             (map :url @calls)))))