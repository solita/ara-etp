(ns solita.etp.header-middleware
  (:require [clojure.string :as str]))

(def content-type "Content-Type")
(def text-plain "text/plain; charset=UTF-8")

(def cache-control "Cache-Control")
(def pragma "Pragma")
(def no-store "no-store")
(def no-cache "no-cache")

(defn contains-header? [{:keys [headers]} header]
  (->> headers
       (filter (fn [[header-k]] (= (str/lower-case header-k)
                                   (str/lower-case header))))
       empty?
       not))

(defn with-default-header [response header-k header-v]
  (cond-> response
          (not (contains-header? response header-k))
          (assoc-in [:headers header-k] header-v)))

(defn wrap-default-content-type [handler]
  (fn [req]
    (let [resp (handler req)]
      (cond-> resp
              (-> resp :body string?)
              (with-default-header content-type text-plain)))))

(defn wrap-default-cache [handler]
  #(-> (handler %)
       (with-default-header cache-control no-store)
       (with-default-header pragma no-cache)))

(defn wrap-disable-cache [handler]
  #(-> (handler %)
       (assoc-in [:headers cache-control] no-store)
       (assoc-in [:headers pragma] no-cache)))

(defn wrap-cache-control [handler seconds]
  #(-> (handler %)
       (with-default-header cache-control (str "max-age=" seconds ",public"))
       (with-default-header pragma nil)))
