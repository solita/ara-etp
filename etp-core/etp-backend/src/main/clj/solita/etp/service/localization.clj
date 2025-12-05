(ns solita.etp.service.localization
  (:require
    [solita.etp.service.luokittelu :as luokittelu-service]))

(def ppp-pdf-localization
  {:fi {:rakennuksen-nimi                  "Rakennuksen nimi"
        :rakennuksen-osoite                "Rakennuksen osoite"
        :pysyva-rakennustunnus             "Pysyvä rakennustunnus"
        :energiatodistuksen-todistustunnus "Energiatodistuksen todistustunnus"
        :perusparannuspassin-tunnus        "Perusparannuspassin tunnus"
        :havainnointikaynnin-paivamaara    "Havainnointikäynnin päivämäärä"
        :passin-esittelyn-paivamaara       "Passin esittelyn päivämäärä"
        :rakennuksen-kayttotarkoitusluokka "Rakennuksen käyttötarkoitusluokka"
        :perusparannuspassin-laatija       "Perusparannuspassin laatija"
        :allekirjoituspaiva                 "Allekirjoituspäivä"

        ;; Toimenpiteiden vaikutukset
        :energiatehokkuusluokan-muutos     "Energiatehokkuusluokan ja E-luvun muutos"
        :toimenpiteiden-kohdistuminen      "Toimenpiteiden kohdistuminen rakennuksen osa-alueisiin"
        :rakennus-toimenpiteiden-jalkeen   "Rakennus ehdotettujen toimenpiteiden jälkeen"
        :lahtotilanne                      "Lähtötilanne"
        :vaihe                             "Vaihe"
        :energiatehokkuuden-kehitys        "Energiatehokkuuden kehitys"
        :energialuokka                     "energialuokka"
        :e-luku                            "E-luku"

        ;; Building parts
        :ylapohja                          "yläpohja"
        :julkisivu                         "julkisivu"
        :ikkunat                           "ikkunat"
        :ulko-ovet                         "ulko-ovet"
        :alapohja                          "alapohja"

        ;; Systems
        :lammitys                          "lämmitys"
        :lammin-kayttovesi                 "lämmin käyttövesi"
        :ilmanvaihto                       "ilmanvaihto"
        :jaahdytys                         "jäähdytys"
        :valaistus                         "valaistus"
        :uusiutuva-energia                 "uusiutuva energia"

        ;; Status
        :kohdistuu                         "kohdistuu"
        :ei-kohdistu                       "ei kohdistu"
        :kohdistuu-muutoksia               "Kohdistuu muutoksia"
        :ei-kohdistu-muutoksia             "Ei kohdistu muutoksia"

        ;; Page titles and headings
        :perusparannuspassi                "Perusparannuspassi"
        :perusparannuspassissa-ehdotettujen-toimenpiteiden-vaikutukset "Perusparannuspassissa ehdotettujen toimenpiteiden vaikutukset"
        :vaiheessa-n-toteutettavat-toimenpiteet "Vaiheessa %s toteutettavat toimenpiteet"
        :vaiheistuksen-yhteenveto          "Vaiheistuksen yhteenveto"
        :laskennan-taustatiedot            "Laskennan taustatiedot"
        :lisatietoja                       "Lisätietoja"
        :perusparannuspassin-tunnus-footer "Perusparannuspassin tunnus"

        ;; Requirements fulfillment
        :tayttaa-a0-luokan-vaatimukset     "Täyttää A0 -luokan vaatimukset"
        :tayttaa-a-plus-luokan-vaatimukset "Täyttää A+ -luokan vaatimukset"
        :kylla                             "Kyllä"
        :ei                                "Ei"
        :a0-selite                         "A0 on päästötön rakennus"
        :a-plus-selite                     "A+ rakennus on erittäin energiatehokas, päästötön ja energiapositiivinen"}
   :sv {:rakennuksen-nimi                  "Byggnadens namn"
        :rakennuksen-osoite                "Byggnadens adress"
        :pysyva-rakennustunnus             "Permanent byggnadsbeteckning"
        :energiatodistuksen-todistustunnus "Energicertifikatets beteckningsnummer"
        :perusparannuspassin-tunnus        "Grundrenoveringspassets beteckning"
        :havainnointikaynnin-paivamaara    "Datum för observationsbesök"
        :passin-esittelyn-paivamaara       "Datum för passens presentation"
        :rakennuksen-kayttotarkoitusluokka "Byggnadens användningsändamålsklass"

        :perusparannuspassin-laatija       "Perusparannuspassin laatija (sv)"
        :allekirjoituspaiva                 "Allekirjoituspäivä (sv)"

        ;; Toimenpiteiden vaikutukset
        :energiatehokkuusluokan-muutos     "Förändring av energieffektivitetsklass och E-tal"
        :toimenpiteiden-kohdistuminen      "Åtgärdernas inriktning på byggnadens delområden"
        :rakennus-toimenpiteiden-jalkeen   "Byggnaden efter föreslagna åtgärder"
        :lahtotilanne                      "Utgångsläge"
        :vaihe                             "Fas"
        :energiatehokkuuden-kehitys        "Utveckling av energieffektivitet"
        :energialuokka                     "energiklass"
        :e-luku                            "E-tal"

        ;; Building parts
        :ylapohja                          "överbjälklag"
        :julkisivu                         "fasad"
        :ikkunat                           "fönster"
        :ulko-ovet                         "ytterdörrar"
        :alapohja                          "bottenbjälklag"

        ;; Systems
        :lammitys                          "uppvärmning"
        :lammin-kayttovesi                 "varmvatten"
        :ilmanvaihto                       "ventilation"
        :jaahdytys                         "kylning"
        :valaistus                         "belysning"
        :uusiutuva-energia                 "förnybar energi"

        ;; Status
        :kohdistuu                         "riktas"
        :ei-kohdistu                       "riktas inte"
        :kohdistuu-muutoksia               "Förändringar riktas"
        :ei-kohdistu-muutoksia             "Inga förändringar riktas"

        ;; Page titles and headings
        :perusparannuspassi                "Grundrenoveringspass"
        :perusparannuspassissa-ehdotettujen-toimenpiteiden-vaikutukset "Effekterna av de åtgärder som föreslås i grundrenoveringspasset"
        :vaiheessa-n-toteutettavat-toimenpiteet "Åtgärder som ska genomföras i fas %s"
        :vaiheistuksen-yhteenveto          "Sammanfattning av fasindelningen"
        :laskennan-taustatiedot            "Bakgrundsinformation om beräkningen"
        :lisatietoja                       "Ytterligare information"
        :perusparannuspassin-tunnus-footer "Grundrenoveringspassets beteckning"

        ;; Requirements fulfillment
        :tayttaa-a0-luokan-vaatimukset     "Uppfyller A0-klassens krav"
        :tayttaa-a-plus-luokan-vaatimukset "Uppfyller A+-klassens krav"
        :kylla                             "Ja"
        :ei                                "Nej"
        :a0-selite                         "A0 är en utsläppsfri byggnad"
        :a-plus-selite                     "A+ byggnad är mycket energieffektiv, utsläppsfri och energipositiv"}})

(defn et-perustiedot-kayttotarkoitus->description
  "To use this you must provide the list of alakayttotarkoitukset as fetched from the DB for the correct version. At the
  time of writing the alakayttotarkoitukset can be fetched with `solita.etp.service.kayttotarkoitus/find-alakayttotarkoitukset`"
  [luokka alakayttotarkoitukset kieli]
  (let [kayttotarkoitus (luokittelu-service/find-luokka luokka alakayttotarkoitukset)]
    (case kieli
      :fi (:label-fi kayttotarkoitus)
      :sv (:label-sv kayttotarkoitus))))
