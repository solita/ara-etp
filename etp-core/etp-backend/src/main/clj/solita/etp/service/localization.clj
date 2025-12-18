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
        :a-plus-selite                     "A+ rakennus on erittäin energiatehokas, päästötön ja energiapositiivinen"

        :laskennan-taustatiedot-otsikko    "Laskennan taustatiedot"
        :lt-otsikko                        "Lähtötilanteen ja perusparannuksen tavoitetilanteen tiedot voimassa olevan lainsäädännön mukaisesti:"
        :U-arvot-lt                         "U-arvot [W/m2K]"
        :ulkoseinat-lt                     "Ulkoseinät"
        :ylapohja-lt                       "Yläpohja"
        :alapohja-lt                       "Alapohja"
        :ikkunat-lt                        "Ikkunat"
        :ulko-ovet-lt                      "Ulko-ovet"
        :lahtotilanne-lt                   "Lähtötilanne"
        :vahimmaisvaatimus                 "Vähimmäisvaatimus***"
        :ehdotettu-taso                    "Ehdotettu taso"
        :mahdollisuus-liittya-otsikko      "Kohteen on mahdollista liittyä energiatehokkaaseen kaukolämpöön tai kaukojäähdytykseen:"
        :mahdollisuus-liittya              "Mahdollisuus liittyä energiatehokkaaseen kaukolämpöön tai kaukojaahdytykseen"
        :lisatietoja-lt                    "Lisätietoja"
        :paalammitysjarjestelma            "Päälämmitysjärjestelmä"
        :ilmanvaihto-lt                    "Ilmanvaihto"
        :uusiutuva-energia-lt              "Uusiutuva energia"
        :vahimmaisvaatimustaso             "Korjausrakentamisen säädösten mukainen E-luvun vähimmäistavoitetaso***"
        :korjausrakentamisen-saadokset     "***Korjausrakentamisen energiatehokkuudesta määrätään ympäristöministeriön asetuksessa
                                            4/13, jota noudatetaan perusparannuspassin toteutuksessa sekä vähimmäisvaatimuksissa.
                                            Suunnitelmallinen kiinteistönpito\nedistää rakennuksen turvallisuutta, terveellisyyttä
                                            sekä asukkaiden viihtyvyyttä. Oikea-aikaisesti toteutetut ja\nhyvin suunnitellut korjaukset
                                            ja remontit auttavat rakennusta mukautumaa ilmastonmuutokseen sekä pien\nentävät energiankulutusta.
                                            Parantunut energiatehokkuus ja uusiutuvan energian hyödyntäminen vähentävät\n rakennuksen käytöstä
                                            aiheutuvia kasvihuonekaasupäästöjä. Rakennusmateriaalien uudelleenkäyttö ja kierrätys
                                            säästävät luonnonvaroja ja vähentävät uusien tuotteiden valmistuksessa syntyviä päästöjä."
        :yksikko                           "kWhE/m2vuosi"
        :lisatietoja-saatavilla            "Lisätietoja saatavilla"
        :lisatietoja1                      "Korjausrakentamisen lainsäädäntö: www.ym.fi/rakentamismaaraykset"
        :lisatietoja2                      "Lisätietoa rahoituksesta: www.motiva.fi/ratkaisut/energiatehokkuuden_rahoitus/rahoituksen_tietopalvelu"
        :lisatietoja3                      "Lisätietoa rakennusten energiatehokkuudesta: www.motiva.fi"
        :lisatietoja4                      "Perusparannuspassin digiversio saatavilla osoitteesta www.energiatodistusrekisteri.fi"
        :lisatietoja5                      "Laskentatyökalun nimi ja versionumero: www.laskentapalvelut.fi, versio 1.5"
        :voimassa-olo                      "Perusparannuspassi on voimassa 1.1.2050 asti tai voimassa olevan lainsäädännön mukaisesti"
        :lisatietoja-otsikko               "Lisätietoja"
        :perusparannuspassi-info           "Perusparannuspassi on tilaajalle vapaaehtoinen. Energiatodistus voidaan laatia ilman perusparannuspassia.\n
                                            Perusparannuspassissa esitetyt eri vaiheiden toteutuneet kulutukset ja kustannukset ovat arvioita. Laskel-\n
                                            missa ei oteta huomioon kuluttajan käyttötottumusten tai energian hintojen muutoksia nykytilanteeseen\nverrattuna."}

   :sv {:rakennuksen-nimi                  "Byggnadens namn"
        :rakennuksen-osoite                "Byggnadens adress"
        :pysyva-rakennustunnus             "Permanent byggnadsbeteckning"
        :energiatodistuksen-todistustunnus "Energicertifikatets beteckningsnummer"
        :perusparannuspassin-tunnus        "Grundrenoveringspassets beteckning"
        :havainnointikaynnin-paivamaara    "Datum för observationsbesök"
        :passin-esittelyn-paivamaara       "Datum för passens presentation"
        :rakennuksen-kayttotarkoitusluokka "Byggnadens användningsändamålsklass"
        :perusparannuspassin-laatija       "Perusparannuspassin laatija (sv)"


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
        :a-plus-selite                     "A+ byggnad är mycket energieffektiv, utsläppsfri och energipositiv"

        :laskennan-taustatiedot-otsikko    "Laskennan taustatiedot (sv)"
        :lt-otsikko                        "Lähtötilanteen ja perusparannuksen tavoitetilanteen tiedot voimassa olevan lainsäädännön mukaisesti: (sv)"
        :U-arvot-lt                        "U-arvot [W/m2K] (sv)"
        :ulkoseinat-lt                     "Ulkoseinät (sv)"
        :ylapohja-lt                       "Yläpohja (sv)"
        :alapohja-lt                       "Alapohja (sv)"
        :ikkunat-lt                        "Ikkunat (sv)"
        :ulko-ovet-lt                      "Ulko-ovet (sv)"
        :lahtotilanne-lt                   "Lähtötilanne (sv)"
        :vahimmaisvaatimus                 "Vähimmäisvaatimus*** (sv)"
        :ehdotettu-taso                    "Ehdotettu taso (sv)"
        :mahdollisuus-liittya-otsikko      "Kohteen on mahdollista liittyä energiatehokkaaseen kaukolämpöön tai kaukojäähdytykseen: (sv)"
        :mahdollisuus-liittya              "Mahdollisuus liitty' energiatehokkaaseen kaukolämpöön tai kaukojaahdytyksee (sv)"
        :lisatietoja-lt                    "Lisätietoja (SV)"
        :paalammitysjarjestelma            "Päälämmitysjärjestelmä (sv)"
        :ilmanvaihto-lt                    "Ilmanvaihto (sv)"
        :uusiutuva-energia-lt              "Uusiutuva energia (sv)"
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
        :voimassa-olo                      "Perusparannuspassi on voimassa 1.1.2050 asti tai voimassa olevan lainsäädännön mukaisesti (sv)"
        :lisatietoja-otsikko               "Lisätietoja (sv)"
        :perusparannuspassi-info           "Perusparannuspassi on tilaajalle vapaaehtoinen. Energiatodistus voidaan laatia ilman perusparannuspassia.\n
                                            Perusparannuspassissa esitetyt eri vaiheiden toteutuneet kulutukset ja kustannukset ovat arvioita. Laskel-\n
                                            missa ei oteta huomioon kuluttajan käyttötottumusten tai energian hintojen muutoksia nykytilanteeseen\nverrattuna.(sv)"}})


(defn et-perustiedot-kayttotarkoitus->description
  "To use this you must provide the list of alakayttotarkoitukset as fetched from the DB for the correct version. At the
  time of writing the alakayttotarkoitukset can be fetched with `solita.etp.service.kayttotarkoitus/find-alakayttotarkoitukset`"
  [luokka alakayttotarkoitukset kieli]
  (let [kayttotarkoitus (luokittelu-service/find-luokka luokka alakayttotarkoitukset)]
    (case kieli
      :fi (:label-fi kayttotarkoitus)
      :sv (:label-sv kayttotarkoitus))))

