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
        :toimenpide-ehdotukset             "Toimenpide-ehdotukset"

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
                                            missa ei oteta huomioon kuluttajan käyttötottumusten tai energian hintojen muutoksia nykytilanteeseen\nverrattuna."

        :toimenpideseloste                 "Toimenpideseloste"
        :energiankulutus-vaiheen-jalkeen   "Energiankulutus vaiheen jälkeen, laskennallinen [kWh/vuosi]"
        :kaukolampo                        "Kaukolämpö"
        :sahko                             "Sähkö"
        :uusiutuvat-pat                    "Uusiutuvat PA:t"
        :fossiiliset-pat                   "Fossiiliset PA:t"
        :kaukojaahdytys                    "Kaukojäähdytys"
        :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen "Energiankulutus, kustannukset ja CO₂-päästöt vaiheen jälkeen"
        :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen "Ostoenergian kokonaistarve vaiheen jälkeen, laskennallinen"
        :uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta "Uusiutuvan energian osuus ostoenergian kokonaistarpeesta"
        :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus "Ostoenergian kokonaistarve vaiheen jälkeen, toteutunut kulutus"
        :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio "Toteutuneen ostoenergian vuotuinen energiakustannus, arvio*"
        :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen "Energiankäytöstä aiheutuvat hiilidioksidipäästöt, laskennallinen"
        :kwh-vuosi                         "kWh/vuosi"
        :prosenttia                        "%"
        :euroa-vuosi                       "€/vuosi"
        :tco2ekv-vuosi                     "tCO2ekv/vuosi"

        ;; Vaiheistuksen yhteenveto
        :laskennallinen-kokonaisenergiankulutus-e-luku "Laskennallinen kokonaisenergiankulutus (E-luku)"
        :energiatehokkuusluokka            "Energiatehokkuusluokka"
        :vakioidun-ostoenergian-muutokset  "Vakioidulla käytöllä lasketun ostoenergian muutokset edelliseen vaiheeseen verrattuna"
        :ostoenergian-muutos-kwh-vuosi     "Ostoenergian muutos [kWh/vuosi]"
        :ostoenergian-muutos-prosentti     "Ostoenergian muutos [%]"
        :painotetun-energiankulutuksen-muutos-kwh "Painotetun energiankulutuksen muutos [kWh/vuosi]"
        :painotetun-energiankulutuksen-muutos-prosentti "Painotetun energiankulutuksen muutos [%]"
        :saasto-energialaskussa            "Säästö energialaskussa [€/vuosi]*"
        :toteutuneen-mitatun-ostoenergian-muutokset "Toteutuneen mitatun ostoenergian muutokset edelliseen vaiheeseen verrattuna"
        :uusiutuvan-energian-laskennalliset-maarat "Uusiutuvan energian laskennalliset määrät vaiheittain"
        :uusiutuvan-energian-kokonaistuotto "Uusiutuvan energian kokonaistuotto [kWh/vuosi"
        :rakennuksen-hyodyntama-osuus      "Rakennuksen hyödyntämä osuus uusiutuvan energian tuotosta [%]"
        :hiilidioksidipaastojen-muutokset  "Hiilidioksidipäästöjen laskennalliset muutokset edelliseen vaiheeseen verrattuna"
        :hiilidioksidipaastojen-vahenema   "Energiankäytöstä aiheutuvien hiilidioksidipäästöjen vähenemä \n[tCO2ekv/vuosi]**"
        :energialaskuissa-kaytetyt-hinnat  "*Energialaskuissa käytetyt energian hinnat, sis. siirto- ja perusmaksut"
        :snt-kwh                           "snt/kWh"
        :uusiutuvat-polttoaineet           "Uusiutuvat polttoaineet"
        :fossiiliset-polttoaineet          "Fossiiliset polttoaineet"
        :co2ekv-vahenema-huomautus         "** Laskennallisen ostoenergian mukaisesti laskettava CO2ekv vähenemä"}
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
        :toimenpide-ehdotukset             "Åtgärdsförslag"

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
                                            missa ei oteta huomioon kuluttajan käyttötottumusten tai energian hintojen muutoksia nykytilanteeseen\nverrattuna.(sv)"

        :toimenpideseloste                 "Åtgärdsbeskrivning"
        :energiankulutus-vaiheen-jalkeen   "Förändring av energiförbrukning från utgångsläget, beräknad [kWh/år]"
        :kaukolampo                        "Fjärrvärme"
        :sahko                             "El"
        :uusiutuvat-pat                    "Förnybara bränslen"
        :fossiiliset-pat                   "Fossila bränslen"
        :kaukojaahdytys                    "Fjärrkyla"
        :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen "Energiförbrukning, kostnader och CO₂-utsläpp efter fasen"
        :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen "Total energiförbrukning efter fasen, beräknad"
        :uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta "Andel förnybar energi av total energiförbrukning"
        :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus "Total energiförbrukning efter fasen, faktisk förbrukning"
        :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio "Årlig energikostnad för faktisk energiförbrukning, uppskattning*"
        :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen "Koldioxidutsläpp från energianvändning, beräknad"
        :kwh-vuosi                         "kWh/år"
        :prosenttia                        "%"
        :euroa-vuosi                       "€/år"
        :tco2ekv-vuosi                     "tCO2ekv/år"

        ;; Vaiheistuksen yhteenveto
        :laskennallinen-kokonaisenergiankulutus-e-luku "Beräknad total energiförbrukning (E-tal)"
        :energiatehokkuusluokka            "Energieffektivitetsklass"
        :vakioidun-ostoenergian-muutokset  "Förändringar i köpt energi beräknad med standardiserad användning jämfört med föregående fas"
        :ostoenergian-muutos-kwh-vuosi     "Förändring i köpt energi [kWh/år]"
        :ostoenergian-muutos-prosentti     "Förändring i köpt energi [%]"
        :painotetun-energiankulutuksen-muutos-kwh "Förändring i viktad energiförbrukning [kWh/år]"
        :painotetun-energiankulutuksen-muutos-prosentti "Förändring i viktad energiförbrukning [%]"
        :saasto-energialaskussa            "Besparingar i energiräkningen [€/år]*"
        :toteutuneen-mitatun-ostoenergian-muutokset "Förändringar i faktisk uppmätt köpt energi jämfört med föregående fas"
        :uusiutuvan-energian-laskennalliset-maarat "Beräknade mängder förnybar energi per fas"
        :uusiutuvan-energian-kokonaistuotto "Total produktion av förnybar energi [kWh/år"
        :rakennuksen-hyodyntama-osuus      "Byggnadens utnyttjade andel av förnybar energiproduktion [%]"
        :hiilidioksidipaastojen-muutokset  "Beräknade förändringar i koldioxidutsläpp jämfört med föregående fas"
        :hiilidioksidipaastojen-vahenema   "Minskning av koldioxidutsläpp från energianvändning \n[tCO2ekv/år]**"
        :energialaskuissa-kaytetyt-hinnat  "*Energipriser som används i energiräkningarna, inkl. överföring och grundavgifter"
        :snt-kwh                           "cnt/kWh"
        :uusiutuvat-polttoaineet           "Förnybara bränslen"
        :fossiiliset-polttoaineet          "Fossila bränslen"
        :co2ekv-vahenema-huomautus         "** CO2ekv-minskning beräknad enligt beräknad köpt energi"}})
(def et-pdf-localization
  {:fi {:energiatodistus                            "Energiatodistus"
        :energiatodistus-2026-subtitle              "Vuoden 2026 lainsäädännön mukainen energiatodistus"
        :rakennuksen-nimi-ja-osoite                 "Rakennuksen nimi ja osoite"
        :pysyva-rakennustunnus                      "Pysyvä rakennustunnus"
        :rakennuksen-valmistumisvuosi               "Rakennuksen valmistumisvuosi"
        :rakennuksen-kayttotarkoitusluokka          "Rakennuksen käyttötarkoitusluokka"
        :energiatodistuksen-tunnus                  "Energiatodstuksen tunnus"
        :energiatodistus-laadittu                   "Energiatodistus on laadittu"
        :todistuksen-laatimispaiva                  "Todistuksen laatimispäivä"
        :todistuksen-voimassaolopaiva               "Todistuksen voimassaolopäivä"

        :kaukolampo                                 "Kauko-\nlämpö"
        :sahko                                      "Sähkö"
        :uusiutuva-polttoaine                       "Uusiutuva\npolttoaine"
        :fossiilinen-polttoaine                     "Fossiilinen\npolttoaine"
        :kaukojaahdytys                             "Kauko-\njäähdytys"
        :laskennallinen-ostoenergia                 "Laskennallinen ostoenergia"
        :energimuodon-kerroin                       "Energiamuodon kerroin"
        :energiakulutus                             "Energiamuodon\nkertoimella painotettu\nenergiakulutus"

        :energiakaytosta-syntyvat-kasvihuonepaastot "Energiakäytöstä syntyvät kasvihuonekaasupäästöt"
        :uusiutuva-energian-osuus                   "Paikan päällä tuotetun uusituvan energian osuus energiankäytöstä"
        :kasvihuonepaastot                          "Elinkaaren aikaiset kasvihuonepaastot(GWP)"
        :energiatehokkuusluokka-otsikko             "Rakennuksen laskennallinen energiatehokkuusluokka ja vertailuluku (E-luku)"
        :paastoton-rakennus                         "Päästötön rakennus"
        :a+luokka-otsikko                           "A+-luokka"
        :a+luokka-teksti                            "Rakennus täyttää A0-luokan vaatimukset, on sitä 20 % energiatehokkaampi ja tuottaa\n
                                                     uusiutuvaa energiaa yli oman tarpeensa."
        :a0luokka-otsikko                           "A0-luokka"
        :a0luokka-teksti                            "E-lukuvaatimuksen lisäksi paikan päällä tuotettu energia on fossiilivapaata ja rakennus kykenee\n
                                                    reagoimaan ulkoisiin signaaleihin mukauttaen energiankäyttöään, tuotantoaan ja varastointiaan"
        :eluku-otsikko                              "E-luku"
        :eluku-teksti                               "Perustuu rakennuksen laskennalliseen energiankulutukseen ja energiamuotojen kertoimiin.\n
                                                    Vakioituun käyttöön perustuva laskenta huomioi lämmitys-, ilmanvaihto- ja jäähdytysjärjestelmät sekä\n
                                                    kuluttajalaitteet ja valaistuksen. Vakioitu käyttö mahdollistaa rakennusten E-lukujen vertailun, muttei\n
                                                    vertailua toteutuneeseen kulutukseen."
        :te-rakennusvaippa-otsikko                  "Toimenpide-ehdotukset: Rakennuksen vaippa"
        :rakennusvaippa-teksti                      "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta, sisäympäristön laatua ja\n
                                                     vähentää energiankäytön kasvihuonekaasupäästöjä. Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä."
        :huomiot-ymparys-otsikko                    "Huomiot – ulkoseinät, ulko-ovet ja ikkunat"
        :te-toimenpide-muutokset                    "Toimenpide-ehdotukset ja arvioidut muutokset"
        :te-lampo-muutos                            "Lämpö,\nostoenergian\nmuutos"
        :te-sahko-muutos                            "Sähkö,\nostoenergian\nmuutos"
        :te-jaahdytyden-muutos                      "Jäähdytys,\nostoenergian\nmuutos"
        :eluvun-muutos                              "E-luku,\nmuuutos"
        :kasvihuonepaastojen-muutos                 "Energiankäytön\nkasvihuonekaasu-\npäästöjen muutos"
        :huomiot-pohjat-otsikko                     "Huomiot – ylä- ja alapohja"
        :te-lammitys-ilmanvaihto-otsikko            "Toimenpide-ehdotukset: Lämmitys- ja ilmanvaihtojärjestelmät"
        :lammitys-ilmanvaihto-teksti                "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta,
                                                    sisäympäristön laatua ja vähen-\ntää energiankäytöstä johtuvia kasvihuonekaasupäästöjä.
                                                    Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä.\n
                                                    LVI-järjestelmien osalta on arvioitava voidaanko ne mukauttaa toimimaan tehokkaammilla
                                                    lämpötila-asetuksilla"
        :huomiot-lammitys-otsikko                   "Huomiot - tilojen ja käyttöveden lämmitysjäjestelmät"
        :te-arvio-teknisesta-kayttoiasta-otsikko    "Arvio lämmitysjärjestelmän jäljellä olevasta teknisestä käyttöiästä"
        :vuotta                                     "vuotta"
        :huomiot-ilmanvaihto-otsikko                "Huomiot - ilmanvaihto- ja ilmastointijärjestelmät"
        :te-muut-otsikko                            "Toimenpide-ehdotukset: muut"
        :te-muut-teksti                             "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta,
                                                     sisäympäristön laatua ja vähen-\ntää energiankäytöstä johtuvia kasvihuonekaasupäästöjä.
                                                     Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä."
        :te-muut-valaistus-otsikko                  "Huomiot – valaistus, jäähdytysjärjestelmät, sähköiset erillislämmitykset ja muut järjestelmät"
        :te-suositukset-otsikko                     "Suosituksia rakennuksen käyttöön ja ylläpitoon (ei vaikuta E-lukuun)"

        :lisamerkintoja-otsikko                     "Lisämerkintöjä"
        :lisatietoja-otsikko                        "Lisätietoja energiatehokkuudesta"
        :lisatietoja-energiatehokkuus               "Hae tietoa toimista parantaa energiatehokkuutta ja lisätä uusiutuvan energian käyttöä"
        :lisatietoja-urlit                          "www.motiva.fi sekä\n wwww.energiatehokaskoti.fi"
        :lisatietoja-rahoitus                       "Rahoituksen tietopalvelu kokoaa tietoa energiatehokkuuden parantamiseen tarkoitetuista tuista ja muista\nrahoitusmuodoista"
        :lisatietoja-energianeuvonta                "Kuluttajien energianeuvonta tarjoaa maksutonta opastusta suomaisille kotitalouksille ja taloyhtiöille.\nKuluttajatvoivat
                                                    ottaa yhteyttä joko valtakunnalliseen Asiaa energiasta -neuvontaan tai maakunnissa toimiviin\nenergianeuvojiin. Lisätietoa osoitteessa"
        :motiva                                     "www.motiva.fi"

   }

   :sv {:rakennuksen-nimi-ja-osoite                 "Rakennuksen nimi ja osoite (sv)"
        :energiatodistus-2026-subtitle              "Energicertifikat i enlighet med 2026 års lagstiftning"
        :pysyva-rakennustunnus                      "Pysyvä rakennustunnus (sv)"
        :rakennuksen-valmistumisvuosi               "Rakennuksen valmistumisvuosi (sv)"
        :rakennuksen-kayttotarkoitusluokka          "Rakennuksen käyttötarkoitusluokka (sv)"
        :energiatodistuksen-tunnus                  "Energiatodstuksen tunnus (sv)"
        :energiatodistus-laadittu                   "Energiatodistus on laadittu (sv)"
        :todistuksen-laatimispaiva                  "Todistuksen laatimispäivä (sv)"
        :todistuksen-voimassaolopaiva               "Todistuksen voimassaolopäivä (sv)"

        :kaukolampo                                 "Kauko-\nlämpö (sv)"
        :sahko                                      "Sähkö (sv)"
        :uusiutuva-polttoaine                       "Uusiutuva\npolttoaine (sv)"
        :fossiilinen-polttoaine                     "Fossiilinen\npolttoaine (sv)"
        :kaukojaahdytys                             "Kauko-\njäähdytys (sv)"
        :laskennallinen-ostoenergia                 "Laskennallinen ostoenergia (sv)"
        :energimuodon-kerroin                       "Energiamuodon kerroin (sv)"
        :energiakulutus                             "Energiamuodon\nkertoimella painotettu\nenergiakulutus (sv)"

        :energiakaytosta-syntyvat-kasvihuonepaastot "Energiakäytöstä syntyvät kasvihuonekaasupäästöt (sv)"
        :uusiutuva-energian-osuus                   "Paikan päällä tuotetun uusituvan energian osuus energiankäytöstä (sv)"
        :kasvihuonepaastot                          "Elinkaaren aikaiset kasvihuonepaastot(GWP) (sv)"
        :energiatehokkuusluokka-otsikko             "Rakennuksen laskennallinen energiatehokkuusluokka ja vertailuluku (E-luku) (sv)"
        :paastoton-rakennus                         "Päästötön rakennus (sv)"
        :a+luokka-otsikko                           "A+-luokka"
        :a+luokka-teksti                            "Rakennus täyttää A0-luokan vaatimukset, on sitä 20 % energiatehokkaampi ja tuottaa\n
                                                     uusiutuvaa energiaa yli oman tarpeensa. (sv)"
        :a0luokka-otsikko                           "A0-luokka (sv)"
        :a0luokka-teksti                            "E-lukuvaatimuksen lisäksi paikan päällä tuotettu energia on fossiilivapaata ja rakennus kykenee\n
                                                     reagoimaan ulkoisiin signaaleihin mukauttaen energiankäyttöään, tuotantoaan ja varastointiaan (sv)"
        :eluku-otsikko                              "E-luku (sv)"
        :eluku-teksti                               "Perustuu rakennuksen laskennalliseen energiankulutukseen ja energiamuotojen kertoimiin.\n
                                                     Vakioituun käyttöön perustuva laskenta huomioi lämmitys-, ilmanvaihto- ja jäähdytysjärjestelmät sekä\n
                                                     kuluttajalaitteet ja valaistuksen. Vakioitu käyttö mahdollistaa rakennusten E-lukujen vertailun, muttei
                                                     \nvertailua toteutuneeseen kulutukseen. (sv)"
        :te-rakennusvaippa-otsikko                  "Toimenpide-ehdotukset: Rakennuksen vaippa (sv)"
        :rakennusvaippa-teksti                      "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta, sisäympäristön laatua ja\n
                                                     vähentää energiankäytön kasvihuonekaasupäästöjä. Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä. (sv)"
        :huomiot-ymparys-otsikko                    "Huomiot – ulkoseinät, ulko-ovet ja ikkunat (sv)"
        :te-toimenpide-muutokset                    "Toimenpide-ehdotukset ja arvioidut muutokset (sv)"
        :te-lampo-muutos                            "Lämpö,\nostoenergian\nmuutos (sv)"
        :te-sahko-muutos                            "Sähkö,\nostoenergian\nmuutos (sv)"
        :te-jaahdytyden-muutos                      "Jäähdytys,\nostoenergian\nmuutos (sv)"
        :eluvun-muutos                              "E-luku,\nmuuutos (sv)"
        :kasvihuonepaastojen-muutos                 "Energiankäytön\nkasvihuonekaasu-\npäästöjen muutos (sv)"
        :huomiot-pohjat-otsikko                     "Huomiot – ylä- ja alapohja"

        :te-lammitys-ilmanvaihto-otsikko            "Toimenpide-ehdotukset: Lämmitys- ja ilmanvaihtojärjestelmät(sv)"
        :lammitys-ilmanvaihto-teksti                "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta,
                                                    sisäympäristön laatua ja vähen-\ntää energiankäytöstä johtuvia kasvihuonekaasupäästöjä.
                                                    Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä.\n
                                                    LVI-järjestelmien osalta on arvioitava voidaanko ne mukauttaa toimimaan tehokkaammilla
                                                    lämpötila-asetuksilla (sv)"
        :huomiot-lammitys-otsikko                   "Huomiot - tilojen ja käyttöveden lämmitysjäjestelmät (sv)"
        :te-arvio-teknisesta-kayttoiasta-otsikko    "Arvio lämmitysjärjestelmän jäljellä olevasta teknisestä käyttöiästä (sv)"
        :vuotta                                     "vuotta (sv)"
        :huomiot-ilmanvaihto-otsikko                "Huomiot - ilmanvaihto- ja ilmastointijärjestelmät (sv)"
        :te-muut-otsikko                            "Huomiot – muut (sv)"
        :te-muut-teksti                             "Toimenpide-ehdotusten tavoitteena on parantaa rakennuksen energiatehokkuutta,
                                                     sisäympäristön laatua ja vähen-\ntää energiankäytöstä johtuvia kasvihuonekaasupäästöjä.
                                                     Vaikutukset arvioidaan rakennuksen vakioidulla käytöllä.(sv)"
        :te-muut-valaistus-otsikko                  "Huomiot – valaistus, jäähdytysjärjestelmät, sähköiset erillislämmitykset ja muut järjestelmät (sv)"
        :te-suositukset-otsikko                     "Suosituksia rakennuksen käyttöön ja ylläpitoon (ei vaikuta E-lukuun) (sv)"

        :lisamerkintoja-otsikko                     "Lisämerkintöjä (sv)"
        :lisatietoja-otsikko                        "Lisätietoja energiatehokkuudesta (sv)"
        :lisatietoja-energiatehokkuus               "Hae tietoa toimista parantaa energiatehokkuutta ja lisätä uusiutuvan energian käyttöä (sv)"
        :lisatietoja-urlit                          "www.motiva.fi sekä\n wwww.energiatehokaskoti.fi (sv)"
        :lisatietoja-rahoitus                       "Rahoituksen tietopalvelu kokoaa tietoa energiatehokkuuden parantamiseen tarkoitetuista tuista ja muista\nrahoitusmuodoista (sv)"
        :lisatietoja-energianeuvonta                "Kuluttajien energianeuvonta tarjoaa maksutonta opastusta suomaisille kotitalouksille ja taloyhtiöille.\nKuluttajatvoivat
                                                    ottaa yhteyttä joko valtakunnalliseen Asiaa energiasta -neuvontaan tai maakunnissa toimiviin\nenergianeuvojiin. Lisätietoa osoitteessa (sv)"
        :motiva                                     "www.motiva.fi (sv)"


        }})




(defn et-perustiedot-kayttotarkoitus->description
  "To use this you must provide the list of alakayttotarkoitukset as fetched from the DB for the correct version. At the
  time of writing the alakayttotarkoitukset can be fetched with `solita.etp.service.kayttotarkoitus/find-alakayttotarkoitukset`"
  [luokka alakayttotarkoitukset kieli]
  (let [kayttotarkoitus (luokittelu-service/find-luokka luokka alakayttotarkoitukset)]
    (case kieli
      :fi (:label-fi kayttotarkoitus)
      :sv (:label-sv kayttotarkoitus))))

