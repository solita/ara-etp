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

        :allekirjoituspaiva                "Allekirjoituspäivä"
        :laskennan-taustatiedot-otsikko    "Lähtötilanteen ja perusparannuksen tavoitetilanteen tiedot voimassa olevan lainsäädännön mukaisesti:"
        :U-arvot                           "U-arvot [W/m2K]"
        :ulkoseinat                        "Ulkoseinät"
        :ylapohja                          "Yläpohja"
        :alapohja                          "Alapohja"
        :ikkunat                           "Ikkunat"
        :ulko-ovet                         "Ulko-ovet"
        :lahtotilanne                      "Lähtötilanne"
        :vahimmaisvaatimus                 "Vähimmäisvaatimus***"
        :ehdotettu-taso                    "Ehdotettu taso"
        :mahdollisuus-liittya-otsikko      "Kohteen on mahdollista liittyä energiatehokkaaseen kaukolämpöön tai kaukojäähdytykseen:"
        :mahdollisuus-liittya              "Mahdollisuus liittyä energiatehokkaaseen kaukolämpöön tai kaukojaahdytykseen"
        :lisatietoja                       "Lisätietoja"
        :paalammitysjarjestelma            "Päälämmitysjärjestelmä"
        :ilmanvaihto                       "Ilmanvaihto"
        :uusiutuva-energia                 "Uusiutuva energia"
        :vahimmaisvaatimustaso             "Korjausrakentamisen säädösten mukainen E-luvun vähimmäistavoitetaso***"
        :korjausrakentamisen-saadokset     "***Korjausrakentamisen energiatehokkuudesta määrätään ympäristöministeriön asetuksessa
                                            4/13, jota noudatetaan perusparannuspassin toteutuksessa sekä vähimmäisvaatimuksissa.
                                            Suunnitelmallinen kiinteistönpito\nedistää rakennuksen turvallisuutta, terveellisyyttä
                                            sekä asukkaiden viihtyvyyttä. Oikea-aikaisesti toteutetut ja\nhyvin suunnitellut korjaukset
                                            ja remontit auttavat rakennusta mukautumaa ilmastonmuutokseen sekä pien\nentävät energiankulutusta.
                                            Parantunut energiatehokkuus ja uusiutuvan energian hyödyntäminen vähentävät\n rakennuksen käytöstä
                                            aiheutuvia kasvihuonekaasupäästöjä. Rakennusmateriaalien uudelleenkäyttö ja kierrä\ntys
                                            säästävät luonnonvaroja ja vähentävät uusien tuotteiden valmistuksessa syntyviä päästöjä."
        :yksikko                           "kWhE/m2vuosi"
        :lisatietoja-saatavilla            "Lisätietoja saatavilla"
        :lisatietoja1                      "Korjausrakentamisen lainsäädäntö: www.ym.fi/rakentamismaaraykset"
        :lisatietoja2                      "Lisätietoa rahoituksesta: www.motiva.fi/ratkaisut/energiatehokkuuden_rahoitus/rahoituksen_tietopalvelu"
        :lisatietoja3                      "Lisätietoa rakennusten energiatehokkuudesta: www.motiva.fi"
        :lisatietoja4                      "Perusparannuspassin digiversio saatavilla osoitteesta www.energiatodistusrekisteri.fi"
        :lisatietoja5                      "Laskentatyökalun nimi ja versionumero: www.laskentapalvelut.fi, versio 1.5"
        :voimassa-olo                      "Perusparannuspassi on voimassa 1.1.2050 asti tai voimassa olevan lainsäädännön mukaisesti"}
   :sv {:rakennuksen-nimi                  "Rakennuksen nimi (sv)"
        :rakennuksen-osoite                "Rakennuksen osoite (sv)"
        :pysyva-rakennustunnus             "Pysyvä rakennustunnus (sv)"
        :energiatodistuksen-todistustunnus "Energiatodistuksen todistustunnus (sv)"
        :perusparannuspassin-tunnus        "Perusparannuspassin tunnus (sv)"
        :havainnointikaynnin-paivamaara    "Havainnointikäynnin päivämäärä (sv)"
        :passin-esittelyn-paivamaara       "Passin esittelyn päivämäärä (sv)"
        :rakennuksen-kayttotarkoitusluokka "Rakennuksen käyttötarkoitusluokka (sv)"
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
        :allekirjoituspaiva                "Allekirjoituspäivä (sv)"
        :laskennan-tautatiedot-otsikko     "Lähtötilanteen ja perusparannuksen tavoitetilanteen tiedot voimassa olevan lainsäädännön mukaisesti: (sv)"
        :U-arvot                           "U-arvot [W/m2K] (sv)"
        :ulkoseinat                        "Ulkoseinät (sv)"
        :ylapohja                          "Yläpohja (sv)"
        :alapohja                          "Alapohja (sv)"
        :ikkunat                           "Ikkunat (sv)"
        :ulko-ovet                         "Ulko-ovet (sv)"
        :lahtotilanne                      "Lähtötilanne (sv)"
        :vahimmaisvaatimus                 "Vähimmäisvaatimus*** (sv)"
        :ehdotettu-taso                    "Ehdotettu taso (sv)"
        :mahdollisuus-liittya-otsikko      "Kohteen on mahdollista liittyä energiatehokkaaseen kaukolämpöön tai kaukojäähdytykseen: (sv)"
        :mahdollisuus-liittya              "Mahdollisuus liitty' energiatehokkaaseen kaukolämpöön tai kaukojaahdytyksee (sv)"
        :lisatietoja                       "Lisätietoja (SV)"
        :paalammitysjarjestelma            "Päälämmitysjärjestelmä (sv)"
        :ilmanvaihto                       "Ilmanvaihto (sv)"
        :uusiutuva-energia                 "Uusiutuva energia (sv)"
        :korjausrakentamisen-saadokset     "***Korjausrakentamisen energiatehokkuudesta määrätään ympäristöministeriön asetuksessa
                                            4/13, jota noudatetaan perusparannuspassin toteutuksessa sekä vähimmäisvaatimuksissa.
                                            Suunnitelmallinen kiinteistönpito\nedistää rakennuksen turvallisuutta, terveellisyyttä
                                            sekä asukkaiden viihtyvyyttä. Oikea-aikaisesti toteutetut ja\nhyvin suunnitellut korjaukset
                                            ja remontit auttavat rakennusta mukautumaa ilmastonmuutokseen sekä pien\nentävät energiankulutusta.
                                            Parantunut energiatehokkuus ja uusiutuvan energian hyödyntäminen vähentävät\n rakennuksen käytöstä
                                            aiheutuvia kasvihuonekaasupäästöjä. Rakennusmateriaalien uudelleenkäyttö ja kierrä\ntys
                                            säästävät luonnonvaroja ja vähentävät uusien tuotteiden valmistuksessa syntyviä päästöjä. (sv)"
        :yksikko                           "kWhE/m2vuosi (sv)"
        :lisatietoja-saatavilla            "Lisätietoja saatavilla (sv)"
        :lisatietoja1                      "Korjausrakentamisen lainsäädäntö: www.ym.fi/rakentamismaaraykset (sv)"
        :lisatietoja2                      "Lisätietoa rahoituksesta: www.motiva.fi/ratkaisut/energiatehokkuuden_rahoitus/rahoituksen_tietopalvelu (sv)"
        :lisatietoja3                      "Lisätietoa rakennusten energiatehokkuudesta: www.motiva.fi (sv)"
        :lisatietoja4                      "Perusparannuspassin digiversio saatavilla osoitteesta www.energiatodistusrekisteri.fi (sv)"
        :lisatietoja5                      "Laskentatyökalun nimi ja versionumero: www.laskentapalvelut.fi, versio 1.5 (sv)"
        :voimassa-olo                      "Perusparannuspassi on voimassa 1.1.2050 asti tai voimassa olevan lainsäädännön mukaisesti (sv)"}})

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

