(ns solita.etp.service.valvonta-oikeellisuus.email
  (:require [solita.etp.config :as config]
            [clojure.string :as str]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.kielisyys :as kielisyys-service]
            [solita.etp.service.valvonta-oikeellisuus.toimenpide :as toimenpide]
            [solita.common.map :as map]
            [solita.common.time :as time]
            [solita.common.logic :as logic]
            [solita.etp.service.valvonta-oikeellisuus.asha :as asha-valvonta-oikeellisuus]
            [solita.etp.email :as email]
            [solita.common.smtp :as smtp])
  (:import (java.time LocalDate)))



(defn- link [title]
  (str "<a href=\"{host}/#/valvonta/oikeellisuus/{energiatodistus.versio}/{energiatodistus.id}\">" title "</a>"))

(defn heading [text] (str "<h1>" text "</h1>"))
(defn- paragraph [& body] (str "<p>" (str/join " " body) "</p>"))

(def ^:private signature-do-not-reply-fi
  (paragraph "Tämä on energiatodistuspalvelun lähettämä automaattinen viesti. Älä vastaa tähän viestiin."))

(def ^:private signature-do-not-reply-sv
  (paragraph "Detta är ett automatiskt meddelande från energicertifikatstjänsten. Svara inte på detta meddelande."))

(def ^:private signature-reply-fi
  (paragraph "Tämä on energiatodistuspalvelun lähettämä automaattinen viesti."))

(def ^:private signature-reply-sv
  (paragraph "Detta är ett automatiskt meddelande från energicertifikatstjänsten."))

(defn- html [& body] (str "<html><body>"
                          (str/join "" body)
                          "</body></html>"))

(def ^:private address-fi
  (str "{energiatodistus.perustiedot.nimi-fi?}, "
       "{energiatodistus.perustiedot.katuosoite-fi?}, "
       "{energiatodistus.perustiedot.postinumero} "
       "{energiatodistus.perustiedot.postitoimipaikka-fi}."))

(def ^:private address-sv
  (str "{energiatodistus.perustiedot.nimi-sv?}, "
       "{energiatodistus.perustiedot.katuosoite-sv?}, "
       "{energiatodistus.perustiedot.postinumero} "
       "{energiatodistus.perustiedot.postitoimipaikka-sv}."))

(def ^:private rfi-order-description-fi
  (paragraph
    "Sinulle on saapunut tietopyyntö koskien energiatodistusta {energiatodistus.id},"
    address-fi))

(def ^:private rfi-order-description-sv
  (paragraph
    "Du har fått en begäran om information om energicertifikatet {energiatodistus.id},"
    address-sv))

(def ^:private rfi-order-link-fi
  (link "Katso ja vastaa tietopyyntöön energiatodistuspalvelussa."))

(def ^:private rfi-order-link-sv
  (link "Se och svara på begäran om information i energicertifikatstjänsten."))

(def ^:private audit-report-link-fi
  (link "Katso valvontamuistio energiatodistuspalvelussa."))

(def ^:private audit-report-link-sv
  (link "Se övervakningspromemorian i energicertifikatstjänsten."))

(def ^:private templates
  {;; kevyt valvontamenettely
   :anomaly
   {:subject
    "Poikkeamailmoitus koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Poikkeamailmoitus koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "Sinulle on saapunut poikkeamailmoitus koskien energiatodistusta {energiatodistus.id},"
        address-fi)
      (link "Katso poikkeamailmoitus energiatodistuspalvelussa.")
      signature-do-not-reply-fi

      (heading "Anmälan om avvikelse om energicertifikatet {energiatodistus.id}")
      (paragraph
        "Du har fått en anmälan om avvikelse om energicertifikatet {energiatodistus.id},"
        address-sv)
      (link "Se anmälan om avvikelse i energicertifikatstjänsten.")
      signature-do-not-reply-sv)}

   ;; tietopyynnön toimenpidetyypit
   :rfi-request
   {:subject
    "Tietopyyntö koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Tietopyyntö koskien energiatodistusta {energiatodistus.id}")
      rfi-order-description-fi
      rfi-order-link-fi
      (paragraph
        "Tietopyyntöön on vastattava {toimenpide.deadline-date} mennessä.")
      signature-do-not-reply-fi

      (heading "Begäran om information om energicertifikatet {energiatodistus.id}")
      rfi-order-description-sv
      rfi-order-link-sv
      (paragraph
        "Du måste svara på begäran om information senast den {toimenpide.deadline-date}.")
      signature-do-not-reply-sv)}
   :rfi-order
   {:subject
    "Kehotus vastata tietopyyntöön koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Kehotus vastata tietopyyntöön koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "Kehotamme vastaamaan tietopyyntöön {toimenpide.deadline-date} mennessä.")
      rfi-order-description-fi
      rfi-order-link-fi
      signature-do-not-reply-fi

      (heading "Uppmaning om att svara på begäran om  information om energicertifikatet {energiatodistus.id}")
      (paragraph
        "Vi uppmanar dig att svara på begäran om information senast den  {toimenpide.deadline-date}.")
      rfi-order-description-sv
      rfi-order-link-sv
      signature-do-not-reply-sv)}
   :rfi-warning
   {:subject
    "Vastaa tietopyyntöön koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Vastaa tietopyyntöön koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "ARA on lähettänyt teille tästä energiatodistuksesta tietopyynnön ja kehotuksen."
        "ARA antaa varoituksen ja vaatii vastaamaan tietopyyntöön {toimenpide.deadline-date} mennessä.")
      rfi-order-description-fi
      rfi-order-link-fi
      signature-do-not-reply-fi

      (heading "Svara på begäran om  information om energicertifikatet {energiatodistus.id}")
      (paragraph
        "ARA har sänt er en begäran om information och uppmaning om detta energicertifikat."
        "ARA ger dig en varning och kräver att du svarar på begäran om information senast den  {toimenpide.deadline-date}.")
      rfi-order-description-sv
      rfi-order-link-sv
      signature-do-not-reply-sv)}

   ;; Valvontamuistion toimenpidetyypit
   :audit-report
   {:subject
    "Valvontamuistio koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Valvontamuistio koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "Sinulle on saapunut valvontamuistio koskien energiatodistusta {energiatodistus.id},"
        address-fi)
      audit-report-link-fi
      signature-do-not-reply-fi

      (heading "Övervakningspromemoria om energicertifikatet {energiatodistus.id}")
      (paragraph
        "Du har fått en övervakningspromemoria om energicertifikatet {energiatodistus.id},"
        address-sv)
      audit-report-link-sv
      signature-do-not-reply-sv)}
   :audit-order
   {:subject
    "Kehotus vastata valvontamuistioon koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Kehotus vastata valvontamuistioon koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "Kehotamme vastaamaan valvontamuistioon {toimenpide.deadline-date} mennessä.")
      (paragraph
        "Sinulle on saapunut valvontamuistio koskien energiatodistusta {energiatodistus.id},"
        address-fi)
      audit-report-link-fi
      signature-do-not-reply-fi

      (heading "Uppmaning om att svara på övervakningspromemorian om energicertifikatet {energiatodistus.id}")
      (paragraph
        "Vi uppmanar dig att svara på övervakningspromemorian senast den  {toimenpide.deadline-date}.")
      (paragraph
        "Du har fått en övervakningspromemoria om energicertifikatet {energiatodistus.id},"
        address-sv)
      audit-report-link-sv
      signature-do-not-reply-sv)}

   :audit-warning
   {:subject
    "Vastaa valvontamuistioon koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Vastaa valvontamuistioon koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "ARA on lähettänyt teille tästä energiatodistuksesta valvontamuistion ja kehotuksen."
        "ARA antaa varoituksen ja vaatii vastaamaan valvontamuistioon {toimenpide.deadline-date} mennessä.")
      (paragraph
        "Sinulle on saapunut valvontamuistio koskien energiatodistusta {energiatodistus.id},"
        address-fi)
      audit-report-link-fi
      signature-do-not-reply-fi

      (heading "Svara på övervakningspromemorian om energicertifikatet {energiatodistus.id}")
      (paragraph
        "ARA har sänt er en övervakningspromemoria och uppmaning om detta energicertifikat."
        "ARA ger dig en varning och kräver att du svarar på övervakningspromemorian senast den  {toimenpide.deadline-date}.")
      (paragraph
        "Du har fått en övervakningspromemoria om energicertifikatet{energiatodistus.id},"
        address-sv)
      audit-report-link-sv
      signature-do-not-reply-sv)}

   ;; lisäselvityspyyntö
   :rfc-request
   {:subject
    "Lisäselvityspyyntö koskien energiatodistusta {energiatodistus.id}"
    :body
    (html
      (heading "Lisäselvityspyyntö koskien energiatodistusta {energiatodistus.id}")
      (paragraph
        "Sinulle on saapunut lisäselvityspyyntö koskien energiatodistusta {energiatodistus.id},"
        address-fi)
      (link "Katso ja vastaa lisäselvityspyyntöön energiatodistuspalvelussa.")
      (paragraph
        "Lisäselvityspyyntöön on vastattava {toimenpide.deadline-date} mennessä.")
      signature-do-not-reply-fi

      (heading "Begäran om tilläggsutredning om energicertifikatet {energiatodistus.id}")
      (paragraph
        "Du har fått en begäran om tilläggsutredning om energicertifikatet {energiatodistus.id},"
        address-sv)
      (link "Se och svara på begäran om tilläggsutredningen i energicertifikatstjänsten.")
      (paragraph
        "Du måste svara på begäran om tilläggsutredning senast den {toimenpide.deadline-date}.")
      signature-do-not-reply-sv)}})

(def ^:private tiedoksi-template
  {:subject
   "Valvontamuistio tiedoksi koskien energiatodistusta {energiatodistus.id}"
   :body
   (html
     (heading "Valvontamuistio tiedoksi koskien energiatodistusta {energiatodistus.id}")
     (paragraph
       "Ohessa valvontamuistio tiedoksi koskien energiatodistusta {energiatodistus.id},"
       address-fi)
     signature-reply-fi

     (heading "Övervakningspromemoria för kännedom om energicertifikatet {energiatodistus.id}")
     (paragraph
       "Bifogat finns en övervakningspromemoria för kännedom om energicertifikatet {energiatodistus.id},"
       address-sv)
     signature-reply-sv)})

(defprotocol TemplateValue (view [value]))

(extend-protocol TemplateValue
  Object
  (view [value] (str value))

  LocalDate
  (view [date] (time/format-date date))

  nil
  (view [_] ""))

(defn- resolve-language [template et-kieli]
  (str/replace template #"(-[a-z]{2})\?" (fn [[_ preferred-kieli]]
                                          (if (= et-kieli :bilingual)
                                            preferred-kieli
                                            (str "-" (name et-kieli))))))

(defn- et-kieli [et]
  (-> et :perustiedot :kieli kielisyys-service/kieli-key))

(defn interpolate [template values]
  (str/replace (resolve-language template (-> values :energiatodistus et-kieli)) #"\{(.*?)\}"
               #(view (get-in values (map keyword (-> % second (str/split #"\.")))))))

(defn- send-email-to-laatija! [db energiatodistus-id toimenpide]
  (logic/if-let*
    [energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus
                       db energiatodistus-id)
     laatija (kayttaja-service/find-kayttaja db (:laatija-id energiatodistus))
     template-type (-> toimenpide :type-id toimenpide/type-key)
     template-values
     {:energiatodistus energiatodistus
      :laatija         laatija
      :toimenpide      toimenpide
      :host            config/index-url}
     template (template-type templates)
     message (map/map-values #(interpolate % template-values) template)]
    (email/send-text-email! (assoc message :to [(:email laatija)]
                                           :subtype "html"))))

(defn- send-email-to-tiedoksi! [db aws-s3-client energiatodistus-id toimenpide-id email]
  (logic/if-let*
    [energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus
                       db energiatodistus-id)
     template-values {:energiatodistus energiatodistus
                      :host            config/index-url}
     message (map/map-values #(interpolate % template-values) tiedoksi-template)
     valvontamuistio (asha-valvonta-oikeellisuus/find-document aws-s3-client energiatodistus-id toimenpide-id)]
    (email/send-multipart-email! (assoc message :to [email]
                                                :subtype "html"
                                                :reply? true
                                                :attachments [(smtp/input-stream->attachment
                                                                valvontamuistio
                                                                "valvontamuistio.pdf"
                                                                "application/pdf")]))))

(defn send-toimenpide-email! [db aws-s3-client energiatodistus-id toimenpide]
  (send-email-to-laatija! db energiatodistus-id toimenpide)
  (when-let [tiedoksi (and (toimenpide/audit-report? toimenpide)
                           (seq (filter #(-> % :email seq) (:tiedoksi toimenpide))))]
    (doseq [vastaanottaja tiedoksi]
      (send-email-to-tiedoksi! db aws-s3-client energiatodistus-id (:id toimenpide) (:email vastaanottaja)))))
