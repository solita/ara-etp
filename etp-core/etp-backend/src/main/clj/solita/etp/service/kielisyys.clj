(ns solita.etp.service.kielisyys
  (:require [solita.etp.service.luokittelu :as luokittelu-service]))

(def find-kielisyys luokittelu-service/find-kielisyys)

(def ^:private kieli-keys
  [:fi,
   :sv,
   :bilingual])

(defn kieli-key [kieli-id] (nth kieli-keys kieli-id))

(defn- kielisyys? [kieli-id energiatodistus]
  (= (-> energiatodistus :perustiedot :kieli)
     kieli-id))

(def only-fi? (partial kielisyys? 0))
(def only-sv? (partial kielisyys? 1))
(def bilingual? (partial kielisyys? 2))

(def fi? (some-fn only-fi? bilingual?))
(def sv? (some-fn only-sv? bilingual?))