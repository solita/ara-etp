(ns solita.etp.service.valvonta-kaytto.email
  (:require [clojure.string :as str]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]
            [solita.common.map :as map]
            [solita.common.time :as time]
            [solita.common.logic :as logic]
            [solita.etp.email :as email]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [solita.etp.service.geo :as geo-service]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.common.maybe :as maybe]
            [solita.etp.service.valvonta-kaytto.store :as store]
            [solita.etp.db :as db]
            [solita.common.smtp :as smtp])
  (:import (java.time LocalDate)))

(db/require-queries 'valvonta-kaytto)

(defn- heading [text] (str "<h1>" text "</h1>"))
(defn- paragraph [& body] (str "<p>" (str/join " " body) "</p>"))

(defn- mailto-link [email-address]
  (str "<a href=\"mailto:" email-address "\">" email-address "</a>"))

(def ^:private signature-reply-fi
  (paragraph "Tämä on energiatodistuspalvelun lähettämä automaattinen viesti."))

(def ^:private signature-reply-sv
  (paragraph "Detta är ett automatiskt meddelande från energicertifikatstjänsten."))

(defn- html [& body] (str "<html><body>"
                          (str/join "" body)
                          "</body></html>"))

(def ^:private address-fi
  (str "{valvonta.katuosoite}, "
       "{valvonta.postinumero} "
       "{valvonta.postitoimipaikka-fi}."))

(def ^:private address-sv
  (str "{valvonta.katuosoite}, "
       "{valvonta.postinumero} "
       "{valvonta.postitoimipaikka-sv}."))

(def ^:private rfi-order-description-fi
  (paragraph
    "Tämän sähköpostin liitteenä on tietopyyntö koskien rakennustasi: {valvonta.rakennustunnus}"
    address-fi))

(def ^:private rfi-order-description-sv
  (paragraph
    "Som bilaga till detta e-postmeddelande finns en begäran om information som gäller din byggnad: {valvonta.rakennustunnus}"
    address-sv))

(def ^:private templates-omistaja
  {:rfi-request
   {:subject
    "Tietopyyntö"
    :body
    (html
      (heading "Tietopyyntö")
      rfi-order-description-fi
      (paragraph
        "Tietopyyntöön on vastattava {toimenpide.deadline-date} mennessä.")
      signature-reply-fi

      (heading "Begäran om information")
      rfi-order-description-sv
      (paragraph
        "Du måste svara på begäran om information senast den {toimenpide.deadline-date}.")
      signature-reply-sv)}
   :rfi-order
   {:subject
    "Energiatodistusvalvonnan kehotus"
    :body
    (html
      (heading "Energiatodistusvalvonnan kehotus")
      (paragraph
        "Kehotamme vastaamaan tietopyyntöön {toimenpide.deadline-date} mennessä.")
      rfi-order-description-fi
      signature-reply-fi

      (heading "Uppmaning till tillsyn över energicertifikat")
      (paragraph
        "Vi uppmanar dig att svara på begäran om information senast den {toimenpite.deadline-date}.")
      rfi-order-description-sv
      signature-reply-sv)}
   :rfi-warning
   {:subject
    "Vastaa tietopyyntöön"
    :body
    (html
      (heading "Vastaa tietopyyntöön")
      (paragraph
        "ARA on lähettänyt teille kehotuksen."
        "ARA antaa varoituksen ja vaatii vastaamaan kehotukseen {toimenpide.deadline-date} mennessä.")
      rfi-order-description-fi
      signature-reply-fi

      (heading "Svara på uppmaningen")
      (paragraph
        "ARA har skickat dig en uppmaning."
        "ARA ger dig en varning och kräver att du svarar på uppmaningen senast den {toimenpite.deadline-date}.")
      rfi-order-description-sv
      signature-reply-sv)}})

(def ^:private templates-tiedoksi
  {:rfi-request
   {:subject
    "Tietopyyntö (tiedoksi)"
    :body
    (html
      (heading "Tietopyyntö (tiedoksi)")
      (paragraph
        "Sähköpostin liitteenä on tiedoksi energiatodistuvalvontaan liittyvä tietopyyntö rakennuksesta: {valvonta.rakennustunnus}"
        address-fi)
      (paragraph "Valvonta kohdistuu rakennuksen omistajaan ja tämä on vain teille tiedoksi.")
      (paragraph "Tarvittaessa lisätietoja voi kysyä osoitteesta "
                 (mailto-link "energiatodistus@ara.fi"))
      signature-reply-fi

      (heading "Begäran om information (för kännedom)")
      (paragraph
        "Som bilaga till e-postmeddelandet finns en begäran om information för kännedom om byggnaden som övervakningen av energicertifikatet gäller: {valvonta.rakennustunnus}"
        address-sv)
      (paragraph "Övervakningen gäller byggnadens ägare och är enbart till din kännedom.")
      (paragraph "Mer information fås vid behov på adressen"
                 (mailto-link "energiatodistus@ara.fi"))
      signature-reply-sv)}
   :rfi-order
   {:subject
    "Energiatodistusvalvonnan kehotus (tiedoksi)"
    :body
    (html
      (heading "Energiatodistusvalvonnan kehotus (tiedoksi)")
      (paragraph
        "Sähköpostin liitteenä on tiedoksi energiatodistuvalvontaan liittyvä kehotus rakennuksesta: {valvonta.rakennustunnus}"
        address-fi)
      (paragraph "Valvonta kohdistuu rakennuksen omistajaan ja tämä on vain teille tiedoksi.")
      (paragraph "Tarvittaessa lisätietoja voi kysyä osoitteesta "
                 (mailto-link "energiatodistus@ara.fi"))
      signature-reply-fi

      (heading "Uppmaning till tillsyn över energicertifikat (för kännedom)")
      (paragraph
        "Som bilaga till e-postmeddelandet finns en uppmaning om byggnaden som övervakningen av energicertifikatet gäller: {valvonta.rakennustunnus}"
        address-sv)
      (paragraph "Övervakningen gäller byggnadens ägare och är enbart till din kännedom.")
      (paragraph "Mer information fås vid behov på adressen"
                 (mailto-link "energiatodistus@ara.fi"))
      signature-reply-sv)}})

(defprotocol TemplateValue (view [value]))

(extend-protocol TemplateValue
  Object
  (view [value] (str value))

  LocalDate
  (view [date] (time/format-date date))

  nil
  (view [_] ""))

(defn interpolate [template values]
  (str/replace template #"\{(.*?)\}"
               #(view (get-in values (map keyword (-> % second (str/split #"\.")))))))

(defn- send-email! [valvonta toimenpide osapuoli documents templates]
  (logic/if-let*
    [template-type (-> toimenpide :type-id toimenpide/type-key)
     template-values
     {:valvonta    valvonta
      :toimenpide  toimenpide
      :osapuoli    osapuoli}
     template (template-type templates)
     message (map/map-values #(interpolate % template-values) template)]
    (email/send-multipart-email! (assoc message :to [(:email osapuoli)]
                                                :subtype "html"
                                                :reply? true
                                                :attachments documents))))

(def document-prefix
  {:rfi-request "tietopyynto"
   :rfi-order   "kehotus"
   :rfi-warning "varoitus"})

(defn- find-document [aws-s3-client valvonta toimenpide osapuoli]
  (logic/if-let* [document (store/find-document aws-s3-client (:id valvonta) (:id toimenpide) osapuoli)
                  prefix (-> toimenpide :type-id toimenpide/type-key document-prefix)]
    (smtp/input-stream->attachment document (str prefix ".pdf") "application/pdf")))

(defn- send-email-to-omistaja! [aws-s3-client valvonta toimenpide osapuoli]
  (when-let [document (find-document aws-s3-client valvonta toimenpide osapuoli)]
    (send-email! valvonta toimenpide osapuoli [document] templates-omistaja)))

(defn send-toimenpide-email! [db aws-s3-client valvonta toimenpide osapuolet]
  (let [tiedoksi (-> (valvonta-kaytto-db/select-template db {:id (:template-id toimenpide)})
                     first
                     :tiedoksi)
        postinumero (maybe/map* #(luokittelu-service/find-luokka
                                   (Integer/parseInt %)
                                   (geo-service/find-all-postinumerot db))
                                (:postinumero valvonta))
        valvonta (assoc valvonta
                   :postitoimipaikka-fi (:label-fi postinumero)
                   :postitoimipaikka-sv (:label-sv postinumero))
        email-osapuolet (filter osapuoli/email? osapuolet)
        documents (mapv (partial find-document aws-s3-client valvonta toimenpide)
                        (filter osapuoli/omistaja? osapuolet))]
    (doseq [vastaanottaja (filter osapuoli/omistaja? email-osapuolet)]
      (send-email-to-omistaja! aws-s3-client valvonta toimenpide vastaanottaja))
    (when (and tiedoksi
               (not (empty? documents)))
      (doseq [vastaanottaja (filter osapuoli/tiedoksi? email-osapuolet)]
        (send-email! valvonta toimenpide vastaanottaja documents templates-tiedoksi)))))
