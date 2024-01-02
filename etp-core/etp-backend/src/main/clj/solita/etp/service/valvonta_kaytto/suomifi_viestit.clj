(ns solita.etp.service.valvonta-kaytto.suomifi-viestit
  (:require [clostache.parser :as clostache]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]
            [solita.etp.service.suomifi-viestit :as suomifi]
            [clojure.java.io :as io]
            [solita.etp.service.pdf :as pdf]
            [solita.common.time :as time]
            [solita.etp.service.valvonta-kaytto.store :as store]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets)
           (java.time Instant)
           (java.util Base64)))

(def lahettaja {:nimi             "Asumisen rahoitus- ja kehittämiskeskus"
                :jakeluosoite     "Kirkkokatu 12 / PL 30"
                :postinumero      "15141"
                :postitoimipaikka "Lahti"})

(defn- tunniste [toimenpide osapuoli]
  (str/join "-" [(or (:diaarinumero toimenpide) "ARA")
                 "ETP" "KV"
                 (:valvonta-id toimenpide)
                 (:id toimenpide)
                 (cond
                   (osapuoli/henkilo? osapuoli) "PERSON"
                   (osapuoli/yritys? osapuoli) "COMPANY")
                 (:id osapuoli)]))

(defn- ->sanoma [toimenpide osapuoli]
  {:tunniste (tunniste toimenpide osapuoli)
   :versio   "1.0"})

(defn- henkilo->asiakas [henkilo]
  {:tunnus (:henkilotunnus henkilo)
   :tyyppi "SSN"
   :osoite {:nimi             (str (:etunimi henkilo) " " (:sukunimi henkilo))
            :lahiosoite       (:jakeluosoite henkilo)
            :postinumero      (:postinumero henkilo)
            :postitoimipaikka (:postitoimipaikka henkilo)
            :maa              (:maa henkilo)}})

(defn- yritys->asiakas [yritys]
  {:tunnus (:ytunnus yritys)
   :tyyppi "CRN"
   :osoite {:nimi             (:nimi yritys)
            :lahiosoite       (:jakeluosoite yritys)
            :postinumero      (:postinumero yritys)
            :postitoimipaikka (:postitoimipaikka yritys)
            :maa              (:maa yritys)}})

(defn- osapuoli->asiakas [osapuoli]
  (cond
    (osapuoli/henkilo? osapuoli) (henkilo->asiakas osapuoli)
    (osapuoli/yritys? osapuoli) (yritys->asiakas osapuoli)))

(defn- kuvaus [type-key valvonta toimenpide]
  (clostache/render (str "Tämän viestin liitteenä on tietopyyntö koskien rakennustasi: {{rakennustunnus}}\n"
                         "{{katuosoite}}, {{postinumero}} {{postitoimipaikka-fi}}\n"
                         "{{#rfi-request}}Tietopyyntöön on vastattava {{deadline-date}} mennessä.{{/rfi-request}}"
                         "{{#rfi-order}}Kehotamme vastaamaan tietopyyntöön {{deadline-date}} mennessä.{{/rfi-order}}"
                         "{{#rfi-warning}}ARA on lähettänyt teille kehotuksen. "
                         "ARA antaa varoituksen ja vaatii vastaamaan tietopyyntöön {{deadline-date}} mennessä.{{/rfi-warning}}"
                         "\n\n"
                         "Som bilaga till detta meddelande finns en begäran om information som gäller din byggnad: {{rakennustunnus}}\n"
                         "{{katuosoite}}, {{postinumero}} {{postitoimipaikka-sv}}\n"
                         "{{#rfi-request}}Begäran om information ska besvaras senast den {{deadline-date}}.{{/rfi-request}}"
                         "{{#rfi-order}}Vi uppmanar dig att besvara begäran om information senast den {{deadline-date}}.{{/rfi-order}}"
                         "{{#rfi-warning}}ARA har skickat en uppmaning till dig. ARA ger en varning och kräver att du svarar på begäran om information senast den {{deadline-date}}.{{/rfi-warning}}")
                    {:rakennustunnus      (:rakennustunnus valvonta)
                     :katuosoite          (:katuosoite valvonta)
                     :postinumero         (:postinumero valvonta)
                     :postitoimipaikka-fi (:postitoimipaikka-fi valvonta)
                     :postitoimipaikka-sv (:postitoimipaikka-sv valvonta)
                     :deadline-date       (time/format-date (:deadline-date toimenpide))
                     type-key             true}))

(defn- toimenpide->kohde [type-key valvonta toimenpide]
  (get {:rfi-request {:nimike "Tietopyyntö"
                      :kuvaus (kuvaus :rfi-request valvonta toimenpide)}
        :rfi-order   {:nimike "Kehotus vastata tietopyyntöön"
                      :kuvaus (kuvaus :rfi-order valvonta toimenpide)}
        :rfi-warning {:nimike "Vastaa tietopyyntöön"
                      :kuvaus (kuvaus :rfi-warning valvonta toimenpide)}}
       type-key))

(defn- toimenpide->tiedosto [type-key]
  (get {:rfi-request {:nimi   "tietopyynto.pdf"
                      :kuvaus "Tietopyyntö liitteenä"}
        :rfi-order   {:nimi   "kehotus.pdf"
                      :kuvaus "Tietopyynnön kehotus liitteenä"}
        :rfi-warning {:nimi   "varoitus.pdf"
                      :kuvaus "Tietopyynnön varoitus liitteenä"}}
       type-key))


(defn- create-cover-page [osapuoli]
  (pdf/generate-pdf->input-stream
    {:layout "pdf/ipost-address-page.html"
     :data   {:lahettaja lahettaja
              :vastaanottaja
              {:tarkenne?        (-> osapuoli :vastaanottajan-tarkenne str/blank? not)
               :tarkenne         (:vastaanottajan-tarkenne osapuoli)
               :nimi             (if (osapuoli/henkilo? osapuoli)
                                   (str (:etunimi osapuoli) " " (:sukunimi osapuoli))
                                   (:nimi osapuoli))
               :jakeluosoite     (:jakeluosoite osapuoli)
               :postinumero      (:postinumero osapuoli)
               :postitoimipaikka (:postitoimipaikka osapuoli)}}}))

(defn- tiedosto-sisalto [document osapuoli]
  (pdf/merge-pdf
    [(create-cover-page osapuoli)
     (io/input-stream document)
     (store/info-letter)]))

(defn- ^:dynamic bytes->base64 [bytes]
  (String. (.encode (Base64/getEncoder) bytes) StandardCharsets/UTF_8))

(defn- document->tiedosto [type-key osapuoli document]
  (let [{:keys [nimi kuvaus]} (toimenpide->tiedosto type-key)
        tiedosto (tiedosto-sisalto document osapuoli)]
    {:nimi    nimi
     :kuvaus  kuvaus
     :sisalto (bytes->base64 tiedosto)
     :muoto   "application/pdf"}))

(defn- ^:dynamic now []
  (Instant/now))

(defn- ->kohde [valvonta toimenpide osapuoli document]
  (let [type-key (toimenpide/type-key (:type-id toimenpide))
        {:keys [nimike kuvaus]} (toimenpide->kohde type-key valvonta toimenpide)]
    {:viranomaistunniste (tunniste toimenpide osapuoli)
     :nimike             nimike
     :kuvaus-teksti      kuvaus
     :lahetys-pvm        (now)
     :asiakas            (osapuoli->asiakas osapuoli)
     :tiedostot          (document->tiedosto type-key osapuoli document)}))

(defn send-message-to-osapuoli! [valvonta
                                 toimenpide
                                 osapuoli
                                 document
                                 & [config]]
  (suomifi/send-message!
    (->sanoma toimenpide osapuoli)
    (->kohde valvonta toimenpide osapuoli document)
    config))

(defn send-suomifi-viestit! [aws-s3-client
                             valvonta
                             toimenpide
                             osapuolet
                             & [config]]
  (doseq [osapuoli (->> osapuolet
                        (filter osapuoli/omistaja?)
                        (filter osapuoli/suomi-fi?))]

    (send-message-to-osapuoli!
      valvonta
      toimenpide
      osapuoli
      (store/find-document aws-s3-client (:valvonta-id toimenpide) (:id toimenpide) osapuoli)
      config)))