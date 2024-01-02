(ns solita.etp.service.laatija.email
  (:require [solita.etp.email :as email]
            [solita.etp.service.concurrent :as concurrent]
            [solita.common.logic :as logic]
            [solita.common.maybe :as maybe]))

(def ^:private signature-fi
  (str
    (email/paragraph
      "Tämä on energiatodistuspalvelun lähettämä automaattinen viesti."
      "Älä vastaa tähän viestiin.")
    (email/paragraph "Asumisen rahoitus- ja kehittämiskeskus")))

(def ^:private signature-sv
  (str
    (email/paragraph
      "Detta är ett automatiskt meddelande från energicertifikatstjänsten."
      "Svara inte på detta meddelande.")
    (email/paragraph "Finansierings- och utvecklingscentralen för boendet")))


(def ^:private general-info-fi
  (email/paragraph
    "Energiatodistusrekisterin osoite on <a href=\"https://www.energiatodistusrekisteri.fi\">www.energiatodistusrekisteri.fi</a>."
    "Energiatodistusrekisteriin kirjaudutaan Suomi.fi-tunnistautumisen kautta."
    "Energiatodistusrekisterin käyttöohjeet löytyvät energiatodistusrekisterin kohdasta <em>Laatijan ohjeet</em>."))

(def ^:private general-info-sv
  (email/paragraph
    "Energicertifikatregistrets adress är <a href=\"https://www.energiatodistusrekisteri.fi\">www.energiatodistusrekisteri.fi</a>."
    "Man loggar in i energicertifikatregistret med Suomi.fi-identifikation."
    "Bruksanvisningar till energicertifikatregistret finns under punkten <em>Anvisningar för upprättare</em> i energicertifikatregistret."))

(def messages
  {:new
   {:reply? false :subtype "html"
    :subject
    "Tervetuloa energiatodistusrekisterin uudeksi laatijaksi"
    :body
    (email/html
      (email/heading "Tervetuloa energiatodistusrekisterin uudeksi laatijaksi")
      (email/paragraph "Hei!")
      (email/paragraph
      "Sinut on lisätty energiatodistusrekisteriin."
      "Tarkemmat tiedot löytyvät energiatodistusrekisteristä.")
      general-info-fi
      signature-fi

      (email/heading "Välkommen till energicertifikatregistret som ny upprättare")
      (email/paragraph "Hej!")
      (email/paragraph
      "Du har lagts till i energicertifikatregistret."
      "Närmare uppgifter finns i energicertifikatregistret.")
      general-info-sv
      signature-sv)}

   :patevyystaso
   {:reply? false :subtype "html"
    :subject
    "Energiatodistuksen laatijan pätevyystason muutos"
    :body
    (email/html
      (email/heading "Energiatodistuksen laatijan pätevyystason muutos")
      (email/paragraph "Hei!")
      (email/paragraph
        "Pätevyystasoasi on muutettu energiatodistusrekisterissä.")
      general-info-fi
      signature-fi

      (email/heading "Ändring av behörighetsnivån för den som upprättar energicertifikat")
      (email/paragraph "Hej!")
      (email/paragraph
        "Din behörighetsnivå har ändrats i energicertifikatregistret.")
      general-info-sv
      signature-sv)}

   :toteamispaivamaara
   {:reply? false :subtype "html"
    :subject
    "Energiatodistuksen laatijan pätevyyden uusiminen"
    :body
    (email/html
      (email/heading "Energiatodistuksen laatijan pätevyyden uusiminen")
      (email/paragraph "Hei!")
      (email/paragraph
        "Pätevyytesi on uusittu energiatodistusrekisterissä.")
      general-info-fi
      signature-fi

      (email/heading "Förnyande av behörigheten för den som upprättar energicertifikat")
      (email/paragraph "Hej!")
      (email/paragraph
        "Din behörighet har förnyats i energicertifikatregistret.")
      general-info-sv
      signature-sv)}

   :patevyys-expiration
   {:reply? false :subtype "html"
    :subject
    "Muistutus energiatodistuksen laatijan pätevyyden päättymisestä"
    :body
    #(email/html
      (email/heading "Muistutus energiatodistuksen laatijan pätevyyden päättymisestä")
      (email/paragraph "Hei!")
      (email/paragraph
        "Pätevyytesi on päättymässä noin " % " kuukauden kuluttua."
        "Pätevyyden uusiminen onnistuu vain muutaman kerran vuodessa."
        "Selvitä hyvissä ajoin seuraavan pätevyyslautakunnan kokoontumisaika, jos haluat jatkaa laatijana."
        "Lisätietoja asiasta löytyy osoitteesta www.fise.fi"
        "ja energiatodistusrekisterin kohdasta <em>Laatijan ohjeet</em>.")
      general-info-fi
      signature-fi

      (email/heading "Påminnelse om att behörigheten för den som upprättar energicertifikat upphör")
      (email/paragraph "Hej!")
      (email/paragraph
        "Din behörighet håller på att ta slut om cirka " % " månad/månader."
        "Behörigheten kan förnyas endast några gånger om året."
        "Ta i god tid reda på när nästa behörighetsnämnd kommer att sammanträda om du vill fortsätta som upprättare."
        "Mer information finns på adressen www.fise.fi"
        "och under punkten <em>Anvisningar för upprättare</em> i energicertifikatregistret.")
      general-info-sv

      signature-sv)}})

(defn send-email! [recipient]
  (logic/if-let*
    [message-template (some-> recipient :type messages)
     message (update message-template :body
                     #(maybe/fold % % (:body recipient)))
     email (:email recipient)]
    (->
      #(email/send-text-email! (assoc message :to [email]))
      (concurrent/retry 3 1000 Throwable)
      (concurrent/safe (str "Sending email failed for laatija: " (:id recipient) " / " email))
      concurrent/call!)))

(defn send-emails! [recipients]
  (concurrent/run-background
    #(doseq [recipient recipients] (send-email! recipient))
    "Sending laatija emails failed."))
