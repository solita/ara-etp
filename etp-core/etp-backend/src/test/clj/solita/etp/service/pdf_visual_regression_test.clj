(ns solita.etp.service.pdf-visual-regression-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.document-assertion :as doc]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus]
            [solita.etp.service.energiatodistus :as energiatodistus]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi]
            [solita.etp.test-data.laatija :as laatija]
            [solita.etp.test-system :as ts])
  (:import (java.time LocalDate)
           (org.apache.pdfbox.pdmodel PDDocument)))

(t/use-fixtures :each ts/fixture)
(def et-add
  {:korvattu-energiatodistus-id          nil
   :yksinkertaistettu-paivitysmenettely false
   :draft-visible-to-paakayttaja        false
   :bypass-validation-limits            true
   :bypass-validation-limits-reason     "Wiki aineisto"
   :kommentti                            nil
   :laskutettava-yritys-id              nil
   :laskutusosoite-id                   -1
   :laskuriviviite                      nil
   :perustiedot
   {:katuosoite-fi            "Esimerkkitie 14"
    :katuosoite-sv            "Exempelvägen 14"
    :valmistumisvuosi         2025
    :julkinen-rakennus        false
    :havainnointikaynti       (LocalDate/of 2025 9 15)
    :havainnointikayntityyppi-id 0
    :tayttaa-aplus-vaatimukset false
    :tayttaa-a0-vaatimukset    false
    :rakennustunnus           "101089529J"
    :postinumero              "00100"
    :keskeiset-suositukset-fi "Paranna rakennuksen energiatehokkuutta lisäeristyksellä ja aurinkopaneeleilla."
    :keskeiset-suositukset-sv "Förbättra byggnadens energieffektivitet med tilläggsisolering och solpaneler."
    :laatimisvaihe            0
    :kiinteistotunnus         "091-001-0001-0001"
    :yritys                   {:nimi             "Esimkerkki Energia Oy"
                               :katuosoite       "Energiakatu 3"
                               :postinumero      "00100"
                               :postitoimipaikka  "Helsinki"}
    :rakennusosa              "Päärakennus"
    :kieli                    0
    :nimi-fi                  "Esimerkkitalo 2026"
    :nimi-sv                  "Exempelhus 2026"
    :kayttotarkoitus          "T"}
   :lahtotiedot
   {:lammitetty-nettoala 3000
    :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin true
    :rakennusvaippa
    {:ilmanvuotoluku    1.5
     :lampokapasiteetti 800
     :ilmatilavuus      9000
     :ulkoseinat        {:ala 2000 :U 0.12}
     :ylapohja          {:ala 1000 :U 0.08}
     :alapohja          {:ala 1000 :U 0.12}
     :ikkunat           {:ala 400  :U 0.7}
     :ulkoovet          {:ala 20   :U 0.8}
     :kylmasillat-UA    15}
    :ikkunat
    {:pohjoinen {:ala 50  :U 0.7 :g-ks 0.4}
     :koillinen {:ala 20  :U 0.7 :g-ks 0.4}
     :ita       {:ala 60  :U 0.7 :g-ks 0.4}
     :kaakko    {:ala 30  :U 0.7 :g-ks 0.4}
     :etela     {:ala 80  :U 0.7 :g-ks 0.4}
     :lounas    {:ala 30  :U 0.7 :g-ks 0.4}
     :lansi     {:ala 60  :U 0.7 :g-ks 0.4}
     :luode     {:ala 20  :U 0.7 :g-ks 0.4}
     :valokupu  {:ala 0   :U nil :g-ks nil}
     :katto     {:ala 50  :U 0.7 :g-ks 0.4}}
    :ilmanvaihto
    {:erillispoistot {:poisto 0 :tulo 0 :sfp 0}
     :ivjarjestelma  {:poisto 0 :tulo 0 :sfp 0}
     :tyyppi-id      4
     :kuvaus-fi      "Koneellinen tulo ja poisto lämmöntalteenotolla"
     :kuvaus-sv      "Maskinell till- och frånluft med värmeåtervinning"
     :lto-vuosihyotysuhde 0.8
     :tuloilma-lampotila   18
     :paaiv {:poisto 0.06 :tulo 0.06 :sfp 1.8
             :lampotilasuhde 0.75 :jaatymisenesto 0}}
    :lammitys
    {:lammitysmuoto-1   {:id 3 :kuvaus-fi "Maalämpöpumppu" :kuvaus-sv "Jordvärmepump"}
     :lammitysmuoto-2   {:id 0 :kuvaus-fi nil :kuvaus-sv nil}
     :lammonjako        {:id 3 :kuvaus-fi "Vesikiertoinen lattialämmitys" :kuvaus-sv "Vattenburet golvvärmesystem"}
     :lammonjako-lampotilajousto true
     :tilat-ja-iv       {:tuoton-hyotysuhde 1 :jaon-hyotysuhde 0.9
                          :lampokerroin 3.5 :apulaitteet 1.5
                          :lampopumppu-tuotto-osuus 0.95 :lampohavio-lammittamaton-tila 0}
     :lammin-kayttovesi {:tuoton-hyotysuhde 1 :jaon-hyotysuhde 0.9
                          :lampokerroin 3.0 :apulaitteet 1.0
                          :lampopumppu-tuotto-osuus 0.9 :lampohavio-lammittamaton-tila 0}
     :takka             {:maara 0 :tuotto 0}
     :ilmalampopumppu   {:maara 0 :tuotto 0}}
    :jaahdytysjarjestelma {:jaahdytyskauden-painotettu-kylmakerroin 3.0}
    :lkvn-kaytto {:ominaiskulutus 40 :lammitysenergian-nettotarve 40}
    :sis-kuorma {:henkilot          {:kayttoaste 0.65 :lampokuorma 5}
                 :kuluttajalaitteet {:kayttoaste 0.65 :lampokuorma 12}
                 :valaistus         {:kayttoaste 0.65 :lampokuorma 6}}}
   :tulokset
   {:kaytettavat-energiamuodot
    {:fossiilinen-polttoaine 0
     :sahko                  60000
     :kaukojaahdytys         0
     :kaukolampo             0
     :uusiutuva-polttoaine   0}
    :uusiutuvat-omavaraisenergiat
    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0
     :muulampo 0 :muusahko 0 :lampopumppu 40000}
    :uusiutuvat-omavaraisenergiat-kokonaistuotanto
    {:aurinkosahko 8000 :aurinkolampo 0 :tuulisahko 0
     :lampopumppu 50000 :muulampo 0 :muusahko 0}
    :kuukausierittely []
    :tekniset-jarjestelmat
    {:tilojen-lammitys      {:sahko 8000  :lampo 25000}
     :tuloilman-lammitys    {:sahko 2000  :lampo 8000}
     :kayttoveden-valmistus {:sahko 3000  :lampo 10000}
     :iv-sahko              5000
     :jaahdytys             {:sahko 3000 :lampo 0 :kaukojaahdytys 0}
     :kuluttajalaitteet-ja-valaistus-sahko 15000}
    :nettotarve
    {:tilojen-lammitys-vuosikulutus      30000
     :ilmanvaihdon-lammitys-vuosikulutus 10000
     :kayttoveden-valmistus-vuosikulutus 12000
     :jaahdytys-vuosikulutus             5000}
    :lampokuormat
    {:aurinko 8000 :ihmiset 4000 :kuluttajalaitteet 5000
     :valaistus 3000 :kvesi 2000}
    :laskentatyokalu "IDA ICE 5.0"}
   :toteutunut-ostoenergiankulutus
   {:ostettu-energia
    {:kaukolampo-vuosikulutus      0
     :kokonaissahko-vuosikulutus   55000
     :kiinteistosahko-vuosikulutus 25000
     :kayttajasahko-vuosikulutus   30000
     :kaukojaahdytys-vuosikulutus  0}
    :ostetut-polttoaineet
    {:kevyt-polttooljy      0
     :pilkkeet-havu-sekapuu 0
     :pilkkeet-koivu        0
     :puupelletit           0
     :muu []}
    :tietojen-alkuperavuosi                            2025
    :lisatietoja-fi                                    nil
    :lisatietoja-sv                                    nil
    :sahko-vuosikulutus-yhteensa                       55000
    :kaukolampo-vuosikulutus-yhteensa                  0
    :uusiutuvat-polttoaineet-vuosikulutus-yhteensa     0
    :fossiiliset-polttoaineet-vuosikulutus-yhteensa    0
    :kaukojaahdytys-vuosikulutus-yhteensa              0
    :uusiutuva-energia-vuosituotto-yhteensa            58000}
   :huomiot
   {:suositukset-fi    "Rakennuksen energiatehokkuus on hyvällä tasolla. Aurinkopaneelien lisääminen ja LED-valaistukseen siirtyminen parantaisivat energiatehokkuutta entisestään."
    :suositukset-sv    "Byggnadens energieffektivitet är på en god nivå. Att lägga till solpaneler och övergå till LED-belysning skulle ytterligare förbättra energieffektiviteten."
    :lisatietoja-fi    nil
    :lisatietoja-sv    nil
    :ymparys
    {:teksti-fi "Rakennuksen vaippa on hyvin eristetty ja ikkunat ovat energiatehokkaita."
     :teksti-sv "Byggnadens klimatskal är väl isolerat och fönstren är energieffektiva."
     :toimenpide [{:nimi-fi "Lisää aurinkopaneelit katolle."
                   :nimi-sv "Installera solpaneler på taket."
                   :lampo 0 :sahko -5000 :jaahdytys 0 :eluvun-muutos -10
                   :kasvihuonepaastojen-muutos -2.5}]}
    :alapohja-ylapohja
    {:teksti-fi "Ylä- ja alapohjat ovat hyvin eristetyt."
     :teksti-sv "Övre och nedre bjälklagen är väl isolerade."
     :toimenpide [{:nimi-fi "Tarkista yläpohjan eristyksen kunto."
                   :nimi-sv "Kontrollera isoleringens skick i övre bjälklaget."
                   :lampo 0 :sahko 0 :jaahdytys 0 :eluvun-muutos 0
                   :kasvihuonepaastojen-muutos 0}]}
    :lammitys
    {:teksti-fi "Maalämpöpumppu toimii tehokkaasti ja kattaa suurimman osan lämmitystarpeesta."
     :teksti-sv "Jordvärmepumpen fungerar effektivt och täcker den största delen av uppvärmningsbehovet."
     :kayttoikaa-jaljella-arvio-vuosina 20
     :toimenpide [{:nimi-fi "Huolla maalämpöpumppu säännöllisesti."
                   :nimi-sv "Underhåll jordvärmepumpen regelbundet."
                   :lampo 0 :sahko -500 :jaahdytys 0 :eluvun-muutos -2
                   :kasvihuonepaastojen-muutos -0.5}]}
    :iv-ilmastointi
    {:teksti-fi "Ilmanvaihtojärjestelmä on tehokas ja varustettu lämmöntalteenotolla."
     :teksti-sv "Ventilationssystemet är effektivt och utrustat med värmeåtervinning."
     :toimenpide [{:nimi-fi "Vaihda suodattimet säännöllisesti."
                   :nimi-sv "Byt filter regelbundet."
                   :lampo 0 :sahko -200 :jaahdytys 0 :eluvun-muutos -1
                   :kasvihuonepaastojen-muutos -0.1}]}
    :valaistus-muut
    {:teksti-fi "Valaistus on pääosin LED-tekniikkaa. Jäähdytysjärjestelmä on energiatehokas."
     :teksti-sv "Belysningen är huvudsakligen LED-teknik. Kylsystemet är energieffektivt."
     :toimenpide [{:nimi-fi "Vaihda loput valaisimet LED-tekniikkaan."
                   :nimi-sv "Byt ut resterande armaturer till LED-teknik."
                   :lampo 0 :sahko -1000 :jaahdytys 0 :eluvun-muutos -3
                   :kasvihuonepaastojen-muutos -0.8}]}}
   :ilmastoselvitys
   {:laatimisajankohta       (LocalDate/of 2025 9 20)
    :laatija                 "Ilmasto Asiantuntija"
    :yritys                  "Wiki Ilmasto Oy"
    :yritys-osoite           "Ilmastokatu 5"
    :yritys-postinumero      "00200"
    :yritys-postitoimipaikka "Helsinki"
    :laadintaperuste         0
    :hiilijalanjalki
    {:rakennus       {:rakennustuotteiden-valmistus 120
                      :kuljetukset-tyomaavaihe      15
                      :rakennustuotteiden-vaihdot   25
                      :energiankaytto               50
                      :purkuvaihe                   8}
     :rakennuspaikka {:rakennustuotteiden-valmistus 4
                      :kuljetukset-tyomaavaihe      1.5
                      :rakennustuotteiden-vaihdot   0.8
                      :energiankaytto               2
                      :purkuvaihe                   0.5}}
    :hiilikadenjalki
    {:rakennus       {:uudelleenkaytto              8
                      :kierratys                    15
                      :ylimaarainen-uusiutuvaenergia 3
                      :hyodyntaminen-energiana      5
                      :hiilivarastovaikutus         12
                      :karbonatisoituminen          2}
     :rakennuspaikka {:uudelleenkaytto              0.5
                      :kierratys                    1.5
                      :hyodyntaminen-energiana      5
                      :ylimaarainen-uusiutuvaenergia 0.3
                      :hiilivarastovaikutus         0.8
                      :karbonatisoituminen          0.3}}}
   :lisamerkintoja-fi nil
   :lisamerkintoja-sv nil})


(def ppp-add
  {:valid true
   :energiatodistus-id nil
   :passin-perustiedot {:havainnointikaynti (LocalDate/of 2026 11 1)
                         :passin-esittely (LocalDate/of 2026 11 2)
                         :tayttaa-a0-vaatimukset false
                         :tayttaa-aplus-vaatimukset false
                         :lisatietoja-fi nil :lisatietoja-sv nil}
   :vaiheet [{:vaihe-nro 1 :valid true
              :toimenpiteet {:toimenpideseloste-fi nil :toimenpideseloste-sv nil :toimenpide-ehdotukset []}
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2030 1 1) :vaiheen-loppu-pvm nil
                         :ostoenergian-tarve-kaukolampo 35000M :ostoenergian-tarve-sahko 8000M
                         :ostoenergian-tarve-uusiutuvat-pat 0M :ostoenergian-tarve-fossiiliset-pat 0M
                         :ostoenergian-tarve-kaukojaahdytys 0M :uusiutuvan-energian-kokonaistuotto 2000M
                         :uusiutuvan-energian-hyodynnetty-osuus 5M :toteutunut-ostoenergia-kaukolampo 33000M
                         :toteutunut-ostoenergia-sahko 7000M :toteutunut-ostoenergia-uusiutuvat-pat 0M
                         :toteutunut-ostoenergia-fossiiliset-pat 0M :toteutunut-ostoenergia-kaukojaahdytys 0M}}]
   :rakennuksen-perustiedot {:e-luvun-vahimmaistavoitetaso 80 :ulkoovet-ehdotettu-taso 1M
                             :ikkunat-ehdotettu-taso 0.8M :paalammitysjarjestelma-ehdotettu-taso 0
                             :jaahdytys-ehdotettu-taso nil :alapohja-ehdotettu-taso 0.15M
                             :uusiutuva-energia-lahtotilanne 0 :uusiutuva-energia-ehdotettu-taso 1
                             :ulkoseinat-ehdotettu-taso 0.17M :ylapohja-ehdotettu-taso 0.09M
                             :mahdollisuus-liittya-energiatehokkaaseen 0 :ilmanvaihto-ehdotettu-taso 8
                             :lisatietoja-fi nil :lisatietoja-sv nil}
   :tulokset {:kaukolampo-hinta 10M :kaukojaahdytys-hinta 18M :sahko-hinta 15M
              :uusiutuvat-pat-hinta 8M :fossiiliset-pat-hinta 14M :lisatietoja-fi nil :lisatietoja-sv nil}})

(t/deftest energiatodistus-2026-with-perusparannuspassi-visual-regression-test
  (let [laatija-id (laatija/insert-suomifi-laatija! {:etunimi "Liisa" :sukunimi "Specimen-Potex"
                                                     :puhelin "0501234567" :patevyystaso 4
                                                     :jakeluosoite "Testikatu 1" :postinumero "00100"
                                                     :postitoimipaikka "Helsinki" :maa "FI"
                                                     :toteamispaivamaara (LocalDate/of 2025 1 1)
                                                     :toteaja "ARA"})
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}
        et-id (:id (energiatodistus/add-energiatodistus! (ts/db-user laatija-id) whoami 2026 et-add))
        _ (:id (perusparannuspassi/insert-perusparannuspassi! (ts/db-user laatija-id) whoami
                                                               (assoc ppp-add :energiatodistus-id et-id)))
        complete-et (complete-energiatodistus/find-complete-energiatodistus ts/*db* whoami et-id)
        pdf-path (energiatodistus-pdf/generate-et-pdf-as-file ts/*db* whoami complete-et "fi" true "visual-regression")]
    (try
      (with-open [pdf-document (PDDocument/load (io/file pdf-path))]
        (doc/assert-pdf-matches-visually pdf-document "documents/energiatodistus-2026-with-perusparannuspassi.pdf"))
      (finally (io/delete-file pdf-path true)))))
