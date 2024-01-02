(ns solita.etp.service.asha-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [solita.etp.service.asha :as asha-service]
            [solita.etp.service.valvonta-kaytto.hallinto-oikeus-attachment :as hao]
            [solita.etp.test-system :as ts]
            [clostache.parser :refer [render-resource]])
  (:import (clojure.lang ExceptionInfo)
           (java.nio.charset StandardCharsets)
           (java.time Instant)
           (java.util Base64)))

(t/use-fixtures :each ts/fixture)

(defn- handle-request [request-resource response-resource response-status & [^String exception]]
  (fn [request]
    (t/is (= (str/trim request) (-> request-resource io/resource slurp str/trim)))
    (if exception
      (throw (Exception. exception))
      {:body   (-> response-resource io/resource slurp)
       :status response-status})))

(defn- handle-requests
  "Take a map of request bodies, and corresponding responses. If a request is received that is not in the map, throw an exception."
  [requests]
  (fn [request]
    (let [response (get requests request {:exception (Exception. (str "Unexpected request: " request))})
          {response-body   :response-body
           response-status :response-status
           exception       :exception
           request-atom    :request-received,
           :or
           {response-body nil
            request-atom  (atom 0)}}
          response]
      (swap! request-atom inc)
      (if exception
        (throw (Exception. exception))
        {:body   response-body
         :status response-status}))))

(t/deftest case-info-test
  (binding [asha-service/post! (handle-request "asha/case-info-request.xml"
                                               "asha/case-info-response.xml"
                                               200)]
    (t/is (= (asha-service/case-info "test@example.com" "ETP-1" "ARA-05.03.02-2021-31")
             {:case-number    "ARA-05.03.02-2021-31"
              :id             38444
              :status         "NEW"
              :classification "05.03.02"
              :name           "Asunnot Oy"
              :description    "Helsinki, Katu 1"
              :created        "2021-03-22T10:28:13+02:00"}))))

(t/deftest open-case-test
  (binding [asha-service/post! (handle-request "asha/case-create-request.xml"
                                               "asha/case-create-response.xml"
                                               200)]
    (t/is (= (asha-service/open-case!
               {:sender-id      "test@example.com"
                :request-id     "ETP-1"
                :classification "05.03.02"
                :service        "general"
                :name           "Asunnot Oy"
                :description    "Helsinki, Katu 1"})
             "ARA-05.03.02-2021-31"))))

(t/deftest action-info-test
  (binding [asha-service/post! (handle-request "asha/action-info-request.xml"
                                               "asha/action-info-response.xml"
                                               200)]
    (t/is (= (asha-service/action-info "test@example.com" "ETP-1" "ARA-05.03.02-2021-8" "Vireillepano")
             {:processing-action
              {:object-class         "ProcessingAction",
               :id                   578877,
               :version              8,
               :contacting-direction "NONE",
               :name                 "Vireillepano",
               :description          "Luodaan uusi asia.",
               :status               "READY",
               :created              "2021-03-25T12:47:08+02:00"},
              :assignee "FFAB23AE-E49F-4E3B-98D9-40C3DE0E4B46",
              :queue    24,
              :selected-decision
              {:decision "Siirry käsittelyyn",
               :next-processing-action
               {:object-class         "ProcessingAction",
                :id                   579176,
                :version              1,
                :contacting-direction "NONE",
                :name                 "Käsittely",
                :description
                "Asian käsittelyn toimenpiteitä, esim. lausunnon laatiminen, selvityksen tekeminen, päätöksen valmistelu",
                :status               "NEW",
                :created              "2021-03-25T14:01:07+02:00"}}}))))

(t/deftest case-create-without-sender-id-test
  (binding [asha-service/post! (handle-request "asha/case-create-request-without-sender-id.xml"
                                               "asha/case-create-response-without-sender-id.xml"
                                               400
                                               "clj-http: status 400")]
    (t/is (thrown-with-msg? ExceptionInfo
                            #"Asiahallinta request failed. Posting the request failed."
                            (asha-service/open-case!
                              {:request-id     "ETP-1"
                               :classification "05.03.02"
                               :service        "general"
                               :name           "Asunnot Oy"
                               :description    "Helsinki, Katu 1"})))))

(t/deftest execute-operation-attach-contact-test
  (binding [asha-service/post! (handle-request "asha/execute-operation-attach-contact-request.xml"
                                               "asha/execute-operation-response.xml"
                                               200)]
    (t/is (nil? (asha-service/execute-operation! {:sender-id  "test@example.com"
                                                  :request-id "ETP-1"
                                                  :identity   {:case              {:number "ARA-05.03.02-2021-8"}
                                                               :processing-action {:name-identity "Vireillepano"}}
                                                  :attach     {:contact {:type       "PERSON"
                                                                         :first-name "Liisa"
                                                                         :last-name  "Meikäläinen"}}})))))

(t/deftest execute-operation-processing-action-test
  (binding [asha-service/post! (handle-request "asha/execute-operation-create-processing-action-request.xml"
                                               "asha/execute-operation-response.xml"
                                               200)]
    (t/is (nil? (asha-service/execute-operation! {:sender-id         "test@example.com"
                                                  :request-id        "ETP-1"
                                                  :identity          {:case              {:number "ARA-05.03.02-2021-8"}
                                                                      :processing-action {:name-identity "Vireillepano"}}
                                                  :processing-action {:name                 "Tietopyyntö"
                                                                      :reception-date       "2021-03-02T12:54:00"
                                                                      :contacting-direction "SENT"
                                                                      :contact              {:type       "PERSON"
                                                                                             :first-name "Liisa"
                                                                                             :last-name  "Meikäläinen"}}})))))

(t/deftest execute-operation-attach-document-test
  (binding [asha-service/post! (handle-request "asha/execute-operation-attach-document-request.xml"
                                               "asha/execute-operation-response.xml"
                                               200)]
    (t/is (nil? (asha-service/execute-operation! {:sender-id  "test@example.com"
                                                  :request-id "ETP-1"
                                                  :identity   {:case              {:number "ARA-05.03.02-2021-8"}
                                                               :processing-action {:name-identity "Tietopyyntö"}}
                                                  :attach     {:document [{:name    "Tietopyyntö.txt"
                                                                           :type    "Pyyntö"
                                                                           :content (String. (.encode (Base64/getEncoder) (.getBytes "Test")) StandardCharsets/UTF_8)}]}})))))

(t/deftest execute-operation-proceed-operation-test
  (binding [asha-service/post! (handle-request "asha/execute-operation-proceed-operation-request.xml"
                                               "asha/execute-operation-response.xml"
                                               200)]
    (t/is (nil? (asha-service/proceed-operation!
                  "test@example.com"
                  "ETP-1"
                  "ARA-05.03.02-2021-8"
                  "Vireillepano"
                  "Siirry käsittelyyn")))))

(defn- info-request [context]
  (render-resource "asha/logtoimenpide/info-request-template.xml" context))

(defn- info-response [context]
  (render-resource "asha/logtoimenpide/info-response-template.xml" context))

(defn- take-processing-action-request [context]
  (render-resource "asha/logtoimenpide/take-processing-action-request-template.xml" context))

(defn- processing-action-ready-request [context]
  (render-resource "asha/logtoimenpide/ready-request-template.xml" context))

(t/deftest log-toimenpide!-test
  (t/testing "The case is new. The processing action operation is set to Vireillepano, instead of the default Käsittely. The case is not moved"
    (let [request-id "request-id"
          case-number 100
          sender-id "solita"
          original-processing-action "Käsittely"
          used-processing-action "Vireillepano"
          processing-action-operation-name "Kehotus"
          description "Kuvaus"
          now (Instant/now)
          take-processing-action-called (atom 0)
          create-processing-action-operation (atom 0)
          take-processing-action-for-operation (atom 0)
          mark-processing-action-operation-ready (atom 0)
          render-context {:request-id  request-id
                          :case-number case-number
                          :sender-id   sender-id}]
      (binding
        [asha-service/post!
         (handle-requests
           {(info-request (merge render-context {:processing-action "Vireillepano"}))
            {:response-body   (info-response (merge render-context {:processing-action "Vireillepano" :processing-action-status "NEW"}))
             :response-status 200}

            (info-request (merge render-context {:processing-action "Käsittely"}))
            {:exception (Exception. "No such processing action")} ;; NOTE: Not sure if this is actually how ASHA behaves, but the effect is the same - non-existing processing actions do not exist in the constructed status map

            (info-request (merge render-context {:processing-action "Päätöksenteko"}))
            {:exception (Exception. "No such processing action")}

            (info-request (merge render-context {:processing-action "Tiedoksianto ja toimeenpano"}))
            {:exception (Exception. "No such processing action")}

            (take-processing-action-request (merge render-context {:processing-action used-processing-action}))
            {:response-status  200
             :request-received take-processing-action-called}

            (render-resource "asha/logtoimenpide/create-processing-action-operation-template.xml"
                             (merge render-context {:processing-action                used-processing-action
                                                    :processing-action-operation-name processing-action-operation-name
                                                    :description                      description
                                                    :reception-date                   now}))
            {:response-status  200
             :request-received create-processing-action-operation}

            (take-processing-action-request (merge render-context {:processing-action processing-action-operation-name}))
            {:response-status  200
             :request-received take-processing-action-for-operation}

            (processing-action-ready-request (merge render-context {:processing-action processing-action-operation-name}))
            {:response-status  200
             :request-received mark-processing-action-operation-ready}})]
        (asha-service/log-toimenpide! sender-id
                                      request-id
                                      case-number
                                      {:identity          {:case              {:number case-number}
                                                           :processing-action {:name-identity original-processing-action}}
                                       :processing-action {:name           processing-action-operation-name
                                                           :reception-date now
                                                           :description    description}}))
      (t/is (= 1 @take-processing-action-called))
      (t/is (= 1 @create-processing-action-operation))
      (t/is (= 1 @take-processing-action-for-operation))
      (t/is (= 1 @mark-processing-action-operation-ready))))

  (t/testing "Exception is thrown when log-toimenpide is called with attachments but the toimenpidetype doesn't have attachments defined"
    (let [request-id "request-id"
          case-number 100
          sender-id "solita"
          original-processing-action "Käsittely"
          processing-action-operation-name "Kehotus"
          description "Kuvaus"
          now (Instant/now)]
      (t/is (thrown-with-msg?
              Exception
              #"Received attachment for processing action Kehotus but it has no attachments defined"
              (asha-service/log-toimenpide! sender-id
                                            request-id
                                            case-number
                                            {:identity          {:case              {:number case-number}
                                                                 :processing-action {:name-identity original-processing-action}}
                                             :processing-action {:name           processing-action-operation-name
                                                                 :reception-date now
                                                                 :description    description}}
                                            ;; Using hallinto-oikeus attachment as a document and an attachment
                                            ;; here to just have some file
                                            [(hao/attachment-for-hallinto-oikeus-id ts/*db* 3)]
                                            (hao/attachment-for-hallinto-oikeus-id ts/*db* 4)))))))

(t/deftest move-processing-action-test
  (binding [asha-service/post! (handle-requests {})]        ;; There should be no requests - fail all
    (let [sender-id "sender-id"
          request-id "request-id"
          case-number 100]
      (t/testing "Move ignores the toimenpide if the action is"
        (t/testing "Vireillepano"
          (asha-service/move-processing-action! sender-id request-id case-number {} "Vireillepano")))
      (t/testing "Move is skipped if the toimenpide is already in correct state"
        (let [processing-action-states {"Vireillepano" "READY" "Käsittely" "NEW"}
              processing-action "Käsittely"]
          (asha-service/move-processing-action! sender-id request-id case-number processing-action-states processing-action)))
      (t/testing "Move from Vireillepano to Käsittely"
        (let [move-called (atom 0)]
          (binding
            [asha-service/post!
             (handle-requests
               {(render-resource "asha/moveaction/move-template.xml" {:sender-id         sender-id
                                                                      :request-id        request-id
                                                                      :case-number       case-number
                                                                      :processing-action "Vireillepano"
                                                                      :proceed-decision  "Siirry käsittelyyn"})
                {:response-body    "Irrelevant"
                 :response-status  200
                 :request-received move-called}})]
            (asha-service/move-processing-action! sender-id request-id case-number {} "Käsittely")
            (t/is (= 1 @move-called)))))
      (t/testing "Move from Käsittely to Päätöksenteko"
        (let [move-called (atom 0)]
          (binding [asha-service/post! (handle-requests {(render-resource "asha/moveaction/move-template.xml" {:sender-id         sender-id
                                                                                                               :request-id        request-id
                                                                                                               :case-number       case-number
                                                                                                               :processing-action "Käsittely"
                                                                                                               :proceed-decision  "Siirry päätöksentekoon"})
                                                         {:response-body    "Irrelevant"
                                                          :response-status  200
                                                          :request-received move-called}})]
            (asha-service/move-processing-action! sender-id request-id case-number {} "Päätöksenteko")
            (t/is (= 1 @move-called)))))

      (t/testing "Move from Päätöksenteko to Tiedoksianto ja toimeenpano"
        (let [move-called (atom 0)]
          (binding [asha-service/post! (handle-requests {(render-resource "asha/moveaction/move-template.xml" {:sender-id         sender-id
                                                                                                               :request-id        request-id
                                                                                                               :case-number       case-number
                                                                                                               :processing-action "Päätöksenteko"
                                                                                                               :proceed-decision  "Siirry tiedoksiantoon"})
                                                         {:response-body    "Irrelevant"
                                                          :response-status  200
                                                          :request-received move-called}})]
            (asha-service/move-processing-action! sender-id request-id case-number {} "Tiedoksianto ja toimeenpano")
            (t/is (= 1 @move-called)))))

      (t/testing "Move from Tiedoksianto ja toimeenpano to Käsittely using 'uudelleenkäsittele asia' decision"
        (let [move-called (atom 0)]
          (binding [asha-service/post! (handle-requests {(render-resource "asha/moveaction/move-template.xml" {:sender-id         sender-id
                                                                                                               :request-id        request-id
                                                                                                               :case-number       case-number
                                                                                                               :processing-action "Tiedoksianto ja toimeenpano"
                                                                                                               :proceed-decision  "Uudelleenkäsittele asia"})
                                                         {:response-body    "Irrelevant"
                                                          :response-status  200
                                                          :request-received move-called}})]
            (asha-service/move-processing-action! sender-id request-id case-number {"Tiedoksianto ja toimeenpano" "UNFINISHED"
                                                                                    "Käsittely" "FINISHED"} "Käsittely")
            (t/is (= 1 @move-called))))))))
