(ns solita.etp.service.valvonta-kaytto.osapuoli
  (:require [solita.etp.schema.valvonta-kaytto :as vk-schema]))

(def omistaja? #(= (:rooli-id %) 0))
(def tiedoksi? (complement omistaja?))
(def other-rooli? #(= (:rooli-id %) 2))

(defn- toimitustapa? [toimitustapa-id] #(= (:toimitustapa-id %) toimitustapa-id))

(def suomi-fi? (toimitustapa? 0))
(def email? (toimitustapa? 1))


(def henkilo? #(and (contains? % :etunimi) (contains? % :sukunimi)))
(def yritys? #(contains? % :nimi))

(defn ilmoituspaikka-other? [valvonta]
  (= (:ilmoituspaikka-id valvonta) 2))

(defn osapuoli->osapuoli-type
  "Given osapuoli map, returns whether the osapuoli is henkilo or yritys"
  [osapuoli]
  (if (henkilo? osapuoli)
    vk-schema/henkilo
    vk-schema/yritys))
