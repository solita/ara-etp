(ns solita.common.jdbc
  (:require [clojure.java.jdbc :as jdbc])
  (:import (java.sql Connection)
           (clojure.lang Associative)))

(defn set-client-info! [^Connection connection
                        ^String name
                        ^String value]
  (.setClientInfo connection name value)
  connection)

(defn set-application-name! [^Connection connection
                             ^String application-name]
  (set-client-info! connection "ApplicationName" application-name))

(defn add-connection-interceptor!
  "Add connection interceptor for clojure java jdbc.
  Interceptor can be used e.g. to set session specific connection attributes from db spec.
  The interceptor is called every time when a new connection is added to db spec."
  [interceptor]
  (extend-protocol jdbc/Connectable
    Associative
    (add-connection [m connection]
      (assoc m :connection (interceptor connection m)))
    (get-level [m] (or (:level m) 0))))
