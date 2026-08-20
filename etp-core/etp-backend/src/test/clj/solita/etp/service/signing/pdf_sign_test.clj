(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [solita.etp.service.signing.pdf-sign :as pdf-sign])
  (:import (com.sun.net.httpserver HttpExchange HttpHandler HttpServer)
           (eu.europa.esig.dss.service.http.commons CommonsDataLoader)
           (java.net InetSocketAddress)
           (java.nio.charset StandardCharsets)))

(defn- response-handler [response-body]
  (reify HttpHandler
    (^void handle [_ ^HttpExchange exchange]
      (.add (.getResponseHeaders exchange) "Content-Encoding" "binary")
      (.sendResponseHeaders exchange 200 (long (alength response-body)))
      (with-open [response-stream (.getResponseBody exchange)]
        (.write response-stream response-body)))))

(t/deftest data-loader-accepts-dvv-binary-content-encoding-test
  (let [response-body (.getBytes "timestamp-response" StandardCharsets/UTF_8)
        server (HttpServer/create (InetSocketAddress. "127.0.0.1" 0) 0)]
    (.createContext server "/" (response-handler response-body))
    (.start server)
    (try
      (let [port (.getPort (.getAddress server))
            url (str "http://127.0.0.1:" port "/")
            loader (#'pdf-sign/data-loader-with-disabled-content-compression)]
        (t/is (= (seq response-body)
                 (seq (.post loader url (.getBytes "timestamp-query" StandardCharsets/UTF_8))))))
      (finally
        (.stop server 0)))))
