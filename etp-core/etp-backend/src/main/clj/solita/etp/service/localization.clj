(ns solita.etp.service.localization)

(def ppp-pdf-localization
  {:fi {:rakennuksen-nimi                  "Rakennuksen nimi"
        :rakennuksen-osoite                "Rakennuksen osoite"
        :pysyva-rakennustunnus             "Pysyvä rakennustunnus"
        :energiatodistuksen-todistustunnus "Energiatodistuksen todistustunnus"
        :perusparannuspassin-tunnus        "Perusparannuspassin tunnus"
        :havainnointikaynnin-paivamaara    "Havainnointikäynnin päivämäärä"
        :passin-esittelyn-paivamaara       "Passin esittelyn päivämäärä"
        :rakennuksen-kayttotarkoitusluokka "Rakennuksen käyttötarkoitusluokka"

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
        :ei-kohdistu-muutoksia             "Ei kohdistu muutoksia"}
   :sv {:rakennuksen-nimi                  "Byggnadens namn"
        :rakennuksen-osoite                "Byggnadens adress"
        :pysyva-rakennustunnus             "Permanent byggnadsbeteckning"
        :energiatodistuksen-todistustunnus "Energicertifikatets beteckningsnummer"
        :perusparannuspassin-tunnus        "Grundrenoveringspassets beteckning"
        :havainnointikaynnin-paivamaara    "Datum för observationsbesök"
        :passin-esittelyn-paivamaara       "Datum för passens presentation"
        :rakennuksen-kayttotarkoitusluokka "Byggnadens användningsändamålsklass"

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
        :ei-kohdistu-muutoksia             "Inga förändringar riktas"}})


(defn et-perustiedot-kayttotarkoitus->description
  "To use this you must provide the list of alakayttotarkoitukset as fetched from the DB for the correct version. At the
  time of writing the alakayttotarkoitukset can be fetched with `solita.etp.service.kayttotarkoitus/find-alakayttotarkoitukset`"
  [luokka alakayttotarkoitukset kieli]
  (let [kayttotarkoitus (some #(when (= (:id %) luokka) %) alakayttotarkoitukset)]
    (case kieli
      :fi (:label-fi kayttotarkoitus)
      :sv (:label-sv kayttotarkoitus))))

