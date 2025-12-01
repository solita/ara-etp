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
        :allekirjoituspaiva                 "Allekirjoituspäivä"}
   :sv {:rakennuksen-nimi                  "Rakennuksen nimi (sv)"
        :rakennuksen-osoite                "Rakennuksen osoite (sv)"
        :pysyva-rakennustunnus             "Pysyvä rakennustunnus (sv)"
        :energiatodistuksen-todistustunnus "Energiatodistuksen todistustunnus (sv)"
        :perusparannuspassin-tunnus        "Perusparannuspassin tunnus (sv)"
        :havainnointikaynnin-paivamaara    "Havainnointikäynnin päivämäärä (sv)"
        :passin-esittelyn-paivamaara       "Passin esittelyn päivämäärä (sv)"
        :rakennuksen-kayttotarkoitusluokka "Rakennuksen käyttötarkoitusluokka (sv)"
        :perusparannuspassin-laatija       "Perusparannuspassin laatija (sv)"
        :allekirjoituspaiva                 "Allekirjoituspäivä (sv)"}})


(defn et-perustiedot-kayttotarkoitus->description
  "To use this you must provide the list of alakayttotarkoitukset as fetched from the DB for the correct version. At the
  time of writing the alakayttotarkoitukset can be fetched with `solita.etp.service.kayttotarkoitus/find-alakayttotarkoitukset`"
  [luokka alakayttotarkoitukset kieli]
  (let [kayttotarkoitus (luokittelu-service/find-luokka luokka alakayttotarkoitukset)]
    (case kieli
      :fi (:label-fi kayttotarkoitus)
      :sv (:label-sv kayttotarkoitus))))

