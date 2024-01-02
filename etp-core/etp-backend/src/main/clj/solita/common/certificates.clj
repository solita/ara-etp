(ns solita.common.certificates
  (:require [clojure.string :as str]
            [buddy.core.certificates :as buddy]
            [camel-snake-kebab.core :as csk])
  (:import (org.bouncycastle.cert X509CertificateHolder)))

(def certificate-start "-----BEGIN CERTIFICATE-----\n")
(def certificate-end "\n-----END CERTIFICATE-----")

(defn with-begin-and-end [s]
  (if (and (str/starts-with? s certificate-start)
           (str/ends-with? s certificate-end))
    s
    (str certificate-start s certificate-end)))

(defn pem-str->certificate [s]
  (-> s with-begin-and-end buddy/str->certificate))

(defn subject [certificate]
  (when (instance? X509CertificateHolder certificate)
    (reduce (fn [acc s]
              (let [[k v] (str/split s #"=")]
                (assoc acc (-> k csk/->kebab-case-keyword) v)))
            {}
            (-> certificate buddy/subject (str/split #",")))))

(defn not-after [^X509CertificateHolder certificate]
  (when (instance? X509CertificateHolder certificate)
    (.getNotAfter certificate)))
