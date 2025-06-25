(ns solita.etp.schema.aineisto
  (:require [clojure.string :as str]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]))

(defn- valid-ipv4? [addr]
  (let [octets (str/split addr #"\.")]
    (and (= 4 (count octets))
         (every? #(try
                    (let [n (Integer/parseInt %)]
                      (<= 0 n 255))
                    (catch Exception _ false))
                 octets))))

(defn- valid-cidr? [cidr]
  (let [[ip mask] (str/split cidr #"/")]
    (and (valid-ipv4? ip)
         (try
           (let [n (Integer/parseInt mask)]
             (and (<= 0 n 32)))
           (catch Exception _ false)))))

(def Aineisto common-schema/Luokittelu)

(def IPv4AddressOrCIDR
  (schema/pred #(or (valid-ipv4? %) (valid-cidr? %))
               "IPv4 address or CIDR range"))

(def KayttajaAineisto
  {:aineisto-id common-schema/Key
   :valid-until common-schema/Instant
   :ip-address IPv4AddressOrCIDR})
