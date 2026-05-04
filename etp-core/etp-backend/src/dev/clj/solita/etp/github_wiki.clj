(ns solita.etp.github-wiki
  "Helper functions for generating GitHub wiki documentation for aineistot."
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cognitect.aws.client.api :as aws]
            [solita.etp.service.energiatodistus-csv :as csv]
            [solita.etp.service.csv-to-s3 :as csv-to-s3-service]
            [solita.etp.service.json :as json]))

;; ---------------------------------------------------------------------------
;; API base URL and auth headers
;; ---------------------------------------------------------------------------

(def ^:private api-base-url "http://localhost:8080")

(def ^:private laatija-headers
  "Mock JWT headers for laatija@solita.fi (from modheaders.json).
   These are accepted by the dev backend with test JWT keys."
  {"x-amzn-oidc-accesstoken"
   "eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNjAxNTEwNDAwLCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.HAlEjQejKyvOoxHrORdnnTnfwiD5lUuEBMalTFKQtu_6luxqxJYfyn-etf2AkaoKWkqsT9g_-k3BV1hT-R1Y0gK3Xl21yT1MDk8QmEZlp1ztiOx4o5ufrX0t6C_Y-VKBxqQkRWLw8crSKfH2TpsTETDetA2gCReoHfHBt2_O63xL-y35glJIzHlc3egqlNFfXwduBZy8ON08h-hhZp0b8AtlaYVoY_OZY3jjfmA19jzf19rEUmK6qOJhJPr-Sgob_CDFf8G6KO4-lIF4FqdUYTvmiJoLiFmefRFscqVQTTurbsxIKmmz5JFY10vCvqp4uWcvO60O0-zgZEQcbV1Ltw"
   "x-amzn-oidc-identity"
   "laatija@solita.fi"
   "x-amzn-oidc-data"
   "eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImN1c3RvbTpGSV9uYXRpb25hbElOIjoiMDEwNDY5LTk5OVciLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.Uk3DCz8fVTqgE_ge0ywVYpeFXnt5x6orlE3cC1e3lgs_2tzv7WHKCtLSbMWXYrcwOgZ-eOOuF_StNovq-IyMVjKAGxu1qaAR20Q2AYYg3JnOUNj1YPBpyA1nF5FYeNDolhlQKxrCj07hXmSBxBeIqNgOnepRJ0Rx9QEBoGbLvzT9mBf_m7CZncTcg2PCdtXiNeww5fx0R2ip53BcdI5nYcKz_LOae6Y707vfbmgfV_zDTFATDAqquwNuhtsqXbmc6D9smkJOl7CNPXY4riDuqyCbi62JMme90HlcHBRnMDLJXEIkTCaox3vdztxBlYVQYUwsaV3eOdQ7_v3wOal18w"})

;; ---------------------------------------------------------------------------
;; HTTP helper
;; ---------------------------------------------------------------------------

(defn- api-post!
  "POST JSON to an API endpoint. Returns the parsed response body.
   Throws on non-2xx status."
  ([path body] (api-post! path body true))
  ([path body parse-json?]
   (let [resp (http/post (str api-base-url path)
                         {:headers          (merge laatija-headers
                                                   {"content-type" "application/json"
                                                    "accept"       "application/json"})
                          :body             (when body (json/write-value-as-string body))
                          :as               (if parse-json? :json :string)
                          :throw-exceptions false})]
     (when (>= (:status resp) 400)
       (throw (ex-info (str "API error " (:status resp) " on POST " path)
                       {:status (:status resp)
                        :body   (:body resp)})))
     (println (str "POST " path " -> " (:status resp)))
     (:body resp))))

;; ---------------------------------------------------------------------------
;; Energiatodistus data definitions
;; ---------------------------------------------------------------------------

(def energiatodistus-2013
  {:korvattu-energiatodistus-id          nil
   :yksinkertaistettu-paivitysmenettely false
   :laskutettava-yritys-id               nil
   :laskutusosoite-id                    -1
   :laskuriviviite                       nil
   :kommentti                            nil
   :draft-visible-to-paakayttaja         false
   :bypass-validation-limits             true
   :bypass-validation-limits-reason      "Wiki aineisto"
   :perustiedot
   {:katuosoite-fi            "Esimerkkitie 10"
    :katuosoite-sv            "Exempel väg 10"
    :valmistumisvuosi         2017
    :julkinen-rakennus        false
    :havainnointikaynti       "2020-04-15"
    :rakennustunnus           "101089529J"
    :postinumero              "33100"
    :keskeiset-suositukset-fi "Paranna eristystä: Lisää kiinteistön lämmöneristystä, erityisesti ullakolla ja seinissä, vähentääksesi lämpöhäviöitä."
    :keskeiset-suositukset-sv "Förbättra isoleringen: Öka fastighetens värmeisolering, särskilt på vinden och i väggarna, för att minska värmeförluster."
    :kiinteistotunnus         "123-456-789-10"
    :yritys                   {:nimi             "Esimerkki Oy"
                               :katuosoite       "Esimerkkikatu 1"
                               :postinumero      "33100"
                               :postitoimipaikka  "Tampere"}
    :tilaaja                  "Tilaaja Oy"
    :rakennusosa              "Prakennus"
    :kieli                    2
    :nimi-fi                  "Rakennuksen nimi"
    :nimi-sv                  "Byggnadens namn"
    :kayttotarkoitus          "RK"
    :uudisrakennus            false}
   :lahtotiedot
   {:lammitetty-nettoala 5000
    :rakennusvaippa
    {:ilmanvuotoluku    0.3
     :lampokapasiteetti 1.235
     :ilmatilavuus      12.345
     :ulkoseinat        {:ala 200   :U 0.81}
     :ylapohja          {:ala 0.5   :U 0.45}
     :alapohja          {:ala 25    :U 0.55}
     :ikkunat           {:ala 2     :U 2}
     :ulkoovet          {:ala 1500  :U 1.9}
     :kylmasillat-UA    1.5}
    :ikkunat
    {:pohjoinen {:ala 0   :U 1   :g-ks 0.75}
     :koillinen {:ala 1.2 :U 1.2 :g-ks 0.75}
     :ita       {:ala 1.5 :U 1.5 :g-ks 0.75}
     :kaakko    {:ala 0   :U 1.9 :g-ks 0.8}
     :etela     {:ala 1.4 :U 1.6 :g-ks 0.75}
     :lounas    {:ala 0.5 :U 0.9 :g-ks 0.75}
     :lansi     {:ala 1.2 :U 0.8 :g-ks 0.75}
     :luode     {:ala 1.3 :U 2.2 :g-ks 0.75}
     :valokupu  {:ala 0   :U 0.4 :g-ks 1}
     :katto     {:ala 1   :U 1   :g-ks 1}}
    :ilmanvaihto
    {:erillispoistot {:poisto 2   :tulo 1   :sfp 1.3}
     :ivjarjestelma  {:poisto 1   :tulo 2   :sfp 1}
     :tyyppi-id      0
     :kuvaus-fi      "ilmanvaihdon kuvaus fi"
     :kuvaus-sv      "ilmanvaihdon kuvaus sv"
     :lto-vuosihyotysuhde 0.65
     :tuloilma-lampotila   21
     :paaiv {:poisto 3 :tulo 1 :sfp 2
             :lampotilasuhde 0 :jaatymisenesto 0}}
    :lammitys
    {:lammitysmuoto-1   {:id 2 :kuvaus-fi "Puu" :kuvaus-sv "Trä"}
     :lammitysmuoto-2   {:id 5 :kuvaus-fi "Kaasu" :kuvaus-sv "Gas"}
     :lammonjako        {:id 7 :kuvaus-fi "Uunilämmitys" :kuvaus-sv "Ugnsvärme"}
     :tilat-ja-iv       {:tuoton-hyotysuhde 1 :jaon-hyotysuhde 0.5
                          :lampokerroin 2.24 :apulaitteet 1.23
                          :lampopumppu-tuotto-osuus 0.1 :lampohavio-lammittamaton-tila 10}
     :lammin-kayttovesi {:tuoton-hyotysuhde 1 :jaon-hyotysuhde 0.9
                          :lampokerroin 3 :apulaitteet 1.4
                          :lampopumppu-tuotto-osuus 0.1 :lampohavio-lammittamaton-tila 10}
     :takka             {:maara 1 :tuotto 2}
     :ilmalampopumppu   {:maara 1 :tuotto 2}}
    :jaahdytysjarjestelma {:jaahdytyskauden-painotettu-kylmakerroin 3.2}
    :lkvn-kaytto {:ominaiskulutus 555 :lammitysenergian-nettotarve 34}
    :sis-kuorma {:henkilot          {:kayttoaste 0.6 :lampokuorma 2}
                 :kuluttajalaitteet {:kayttoaste 0.6 :lampokuorma 3}
                 :valaistus         {:kayttoaste 0.1 :lampokuorma 8}}}
   :tulokset
   {:kaytettavat-energiamuodot
    {:fossiilinen-polttoaine 550000
     :sahko                  5000
     :kaukojaahdytys         0
     :kaukolampo             0
     :uusiutuva-polttoaine   0
     :muu [{:nimi "Turve" :muotokerroin 10.5 :ostoenergia 100000}]}
    :uusiutuvat-omavaraisenergiat
    [{:nimi-fi "Tuulivoima" :nimi-sv "Vindkraft" :vuosikulutus 100}]
    :kuukausierittely []
    :tekniset-jarjestelmat
    {:tilojen-lammitys      {:sahko 1 :lampo 2}
     :tuloilman-lammitys    {:sahko 3 :lampo 4}
     :kayttoveden-valmistus {:sahko 5 :lampo 6}
     :iv-sahko              7
     :jaahdytys             {:sahko 1 :lampo 2 :kaukojaahdytys 3}
     :kuluttajalaitteet-ja-valaistus-sahko 4}
    :nettotarve
    {:tilojen-lammitys-vuosikulutus      1000
     :ilmanvaihdon-lammitys-vuosikulutus 2000
     :kayttoveden-valmistus-vuosikulutus 3000
     :jaahdytys-vuosikulutus             4000}
    :lampokuormat
    {:aurinko 100 :ihmiset 200 :kuluttajalaitteet 300
     :valaistus 400 :kvesi 500}
    :laskentatyokalu "Excellent E tool v1.0"}
   :toteutunut-ostoenergiankulutus
   {:ostettu-energia
    {:kaukolampo-vuosikulutus      10000
     :kokonaissahko-vuosikulutus   20000
     :kiinteistosahko-vuosikulutus 30000
     :kayttajasahko-vuosikulutus   40000
     :kaukojaahdytys-vuosikulutus  50000
     :muu [{:nimi-fi "Turve" :nimi-sv "Torv" :vuosikulutus 100000}]}
    :ostetut-polttoaineet
    {:kevyt-polttooljy      1000
     :pilkkeet-havu-sekapuu 2000
     :pilkkeet-koivu        3000
     :puupelletit           4000
     :muu [{:nimi "Turve" :maara-vuodessa 1000 :yksikko "kg" :muunnoskerroin 0.8}]}
    :sahko-vuosikulutus-yhteensa          50000
    :kaukolampo-vuosikulutus-yhteensa     40000
    :polttoaineet-vuosikulutus-yhteensa   30000
    :kaukojaahdytys-vuosikulutus-yhteensa 20000}
   :huomiot
   {:suositukset-fi    "Säännöllinen huolto: Huolehdi kiinteistön säännöllisestä huollosta, kuten lämmitys- ja ilmastointijärjestelmien, vesijohtojen ja sähköjärjestelmien tarkastamisesta ja kunnossapidosta."
    :suositukset-sv    "Regelbunden underhåll: Se till att fastigheten underhålls regelbundet, inklusive inspektion och underhåll av uppvärmnings- och luftkonditioneringssystem, VVS och elsystem."
    :lisatietoja-fi    "Ei lisätietoja"
    :lisatietoja-sv    "ingen ytterligare information"
    :ymparys
    {:teksti-fi "Kiinnitä erityistä huomiota ulkoseinien eristykseen, ulko-ovien tiiveyteen ja ikkunoiden laatuun, sillä nämä ovat tärkeitä tekijöitä kiinteistön lämmöneristävyyden ja energiatehokkuuden kannalta."
     :teksti-sv "Lägg särskild vikt vid isoleringen av ytterväggarna, tätheten hos ytterdörrarna och kvaliteten på fönstren, eftersom dessa är viktiga faktorer för fastighetens värmeisolering och energieffektivitet"
     :toimenpide [{:nimi-fi "Asenna ulko-ovien ja ikkunoiden ympärille tiivisteet tai uudista ne tarvittaessa."
                   :nimi-sv "Montera tätningar runt ytterdörrar och fönster, eller byt ut dem om det behövs."
                   :lampo 9 :sahko 8 :jaahdytys 7 :eluvun-muutos 6}]}
    :alapohja-ylapohja
    {:teksti-fi "Kiinnitä erityistä huomiota ylä- ja alapohjan eristykseen ja ilmatiiviyteen, sillä ne vaikuttavat merkittävästi kiinteistön energiatehokkuuteen ja lämmöneristykseen."
     :teksti-sv "Lägg särskild vikt vid isoleringen och lufttätheten i övre och nedre bjälklaget, eftersom de har en betydande inverkan på fastighetens energieffektivitet och värmeisolering."
     :toimenpide [{:nimi-fi "Varmista, että ylä- ja alapohjan eristeet ovat riittävän paksut ja ehjiä."
                   :nimi-sv "Säkerställ att isoleringen i övre och nedre bjälklaget är tillräckligt tjock och intakt."
                   :lampo 1 :sahko 2 :jaahdytys 3 :eluvun-muutos 4}]}
    :lammitys
    {:teksti-fi "Tarkista tilojen ja käyttöveden lämmitysjärjestelmien kunto ja tehokkuus säännöllisesti, ja harkitse uusiutuvien energialähteiden käyttöönottoa energiatehokkuuden parantamiseksi."
     :teksti-sv "Kontrollera regelbundet skicket och effektiviteten av uppvärmningssystemen för utrymmen och användningsvatten, och överväg att använda förnybara energikällor för att förbättra energieffektiviteten."
     :toimenpide [{:nimi-fi "Asenna lämpöpumppu lämmitysjärjestelmään energiatehokkuuden parantamiseksi."
                   :nimi-sv "Installera en värmepump i uppvärmningssystemet för att förbättra energieffektiviteten."
                   :lampo -200 :sahko 204.52 :jaahdytys -123.45 :eluvun-muutos 456.679}]}
    :iv-ilmastointi
    {:teksti-fi "Varmista, että ilmanvaihto- ja ilmastointijärjestelmät ovat kunnossa ja toimivat tehokkaasti, jotta sisäilma pysyy terveellisenä ja energiankulutus on optimaalinen."
     :teksti-sv "Säkerställ att ventilationssystem och luftkonditionering fungerar korrekt och effektivt för att upprätthålla en hälsosam inomhusmiljö och optimera energiförbrukningen."
     :toimenpide [{:nimi-fi "Asenna ajastin ilmanvaihtojärjestelmään, jotta se voidaan ohjelmoida toimimaan vain tarvittaessa."
                   :nimi-sv "Installera en timer i ventilationssystemet för att kunna programmera det att fungera effektiväst."
                   :lampo 1 :sahko 5 :jaahdytys 10 :eluvun-muutos 20}
                  {:nimi-fi "Säädä ilmanvaihtoa" :nimi-sv "Justera ventilationen"
                   :lampo 10 :sahko 50 :jaahdytys 100 :eluvun-muutos 200}]}
    :valaistus-muut
    {:teksti-fi "Jäähdytysjärjestelmän valinta voi vaikuttaa merkittävästi kiinteistön energiatehokkuuteen ja mukavuuteen erityisesti kesäkuukausina. On tärkeää valita oikeanlainen jäähdytysjärjestelmä kiinteistön tarpeiden perusteella"
     :teksti-sv "Valet av kylsystem kan ha en betydande inverkan på fastighetens energieffektivitet och komfort, särskilt under sommarmånaderna. Det är viktigt att välja rätt typ av kylsystem baserat på fastighetens behov."
     :toimenpide [{:nimi-fi "Harkitse lämpöpumpun asentamista jäähdytysjärjestelmäksi"
                   :nimi-sv "Överväg att installera en värmepump som kylsystem"
                   :lampo 100 :sahko 200 :jaahdytys 300 :eluvun-muutos -400}]}}
   :lisamerkintoja-fi nil
   :lisamerkintoja-sv nil})

(def energiatodistus-2018
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
   {:katuosoite-fi            "Esimerkkitie 12"
    :katuosoite-sv            "Exempelvg 12"
    :valmistumisvuosi         2023
    :julkinen-rakennus        false
    :havainnointikaynti       nil
    :rakennustunnus           nil
    :postinumero              "33100"
    :keskeiset-suositukset-fi "Asenna paremmin eristetyt ikkunat ja ovet, jotta lämmitysenergian tarve vähenee."
    :keskeiset-suositukset-sv "Installera bättre isolerade fönster och dörrar för att minska behovet av uppvärmningsenergi."
    :laatimisvaihe            0
    :kiinteistotunnus         nil
    :yritys                   {:nimi             "Yritys Oy"
                               :katuosoite       "Yrityskatu 1"
                               :postinumero      "33100"
                               :postitoimipaikka  "Tampere"}
    :tilaaja                  "Tilaaja Oy"
    :rakennusosa              "Prakennus"
    :kieli                    2
    :nimi-fi                  "Paras rakennus"
    :nimi-sv                  "Paras rakennus"
    :kayttotarkoitus          "AK3"}
   :lahtotiedot
   {:lammitetty-nettoala 2500
    :rakennusvaippa
    {:ilmanvuotoluku    2
     :lampokapasiteetti 1000
     :ilmatilavuus      5000
     :ulkoseinat        {:ala 1500 :U 0.1}
     :ylapohja          {:ala 800  :U 0.1}
     :alapohja          {:ala 600  :U 0.1}
     :ikkunat           {:ala 200  :U 0.5}
     :ulkoovet          {:ala 10   :U 0.5}
     :kylmasillat-UA    21}
    :ikkunat
    {:pohjoinen {:ala 0 :U nil :g-ks nil}
     :koillinen {:ala 0 :U nil :g-ks nil}
     :ita       {:ala 0 :U nil :g-ks nil}
     :kaakko    {:ala 0 :U nil :g-ks nil}
     :etela     {:ala 0 :U nil :g-ks nil}
     :lounas    {:ala 0 :U nil :g-ks nil}
     :lansi     {:ala 0 :U nil :g-ks nil}
     :luode     {:ala 0 :U nil :g-ks nil}
     :valokupu  {:ala 0 :U nil :g-ks nil}
     :katto     {:ala 0 :U nil :g-ks nil}}
    :ilmanvaihto
    {:erillispoistot {:poisto 2 :tulo 1 :sfp 2}
     :ivjarjestelma  {:poisto 1 :tulo 2 :sfp 1}
     :tyyppi-id      7
     :kuvaus-fi      nil
     :kuvaus-sv      nil
     :lto-vuosihyotysuhde 0.8
     :tuloilma-lampotila   21
     :paaiv {:poisto 3 :tulo 1 :sfp 2
             :lampotilasuhde 0 :jaatymisenesto 0}}
    :lammitys
    {:lammitysmuoto-1   {:id 6 :kuvaus-fi nil :kuvaus-sv nil}
     :lammitysmuoto-2   {:id 0 :kuvaus-fi nil :kuvaus-sv nil}
     :lammonjako        {:id 1 :kuvaus-fi nil :kuvaus-sv nil}
     :tilat-ja-iv       {:tuoton-hyotysuhde 1 :jaon-hyotysuhde 0.5
                          :lampokerroin 2.3 :apulaitteet 1.3
                          :lampopumppu-tuotto-osuus 0.1 :lampohavio-lammittamaton-tila 10}
     :lammin-kayttovesi {:tuoton-hyotysuhde 0.3 :jaon-hyotysuhde 0.2
                          :lampokerroin 7 :apulaitteet 1
                          :lampopumppu-tuotto-osuus 0.1 :lampohavio-lammittamaton-tila 10}
     :takka             {:maara 0 :tuotto 0}
     :ilmalampopumppu   {:maara 0 :tuotto 0}}
    :jaahdytysjarjestelma {:jaahdytyskauden-painotettu-kylmakerroin 2.4}
    :lkvn-kaytto {:ominaiskulutus 223 :lammitysenergian-nettotarve 12}
    :sis-kuorma {:henkilot          {:kayttoaste 0.6 :lampokuorma 3}
                 :kuluttajalaitteet {:kayttoaste 0.6 :lampokuorma 4}
                 :valaistus         {:kayttoaste 0.1 :lampokuorma 9}}}
   :tulokset
   {:kaytettavat-energiamuodot
    {:fossiilinen-polttoaine 0
     :sahko                  180000
     :kaukojaahdytys         0
     :kaukolampo             0
     :uusiutuva-polttoaine   0}
    :uusiutuvat-omavaraisenergiat
    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
     :muulampo 0 :muusahko 0 :lampopumppu 100000}
    :kuukausierittely []
    :tekniset-jarjestelmat
    {:tilojen-lammitys      {:sahko 1 :lampo 2}
     :tuloilman-lammitys    {:sahko 3 :lampo 4}
     :kayttoveden-valmistus {:sahko 1 :lampo 2}
     :iv-sahko              8
     :jaahdytys             {:sahko 9 :lampo 0 :kaukojaahdytys 9}
     :kuluttajalaitteet-ja-valaistus-sahko 1}
    :nettotarve
    {:tilojen-lammitys-vuosikulutus      121212
     :ilmanvaihdon-lammitys-vuosikulutus 9059
     :kayttoveden-valmistus-vuosikulutus 99000
     :jaahdytys-vuosikulutus             19999}
    :lampokuormat
    {:aurinko 101 :ihmiset 31 :kuluttajalaitteet 232
     :valaistus 131 :kvesi 3}
    :laskentatyokalu "Trusty tool 1.1 beta 2"}
   :toteutunut-ostoenergiankulutus
   {:ostettu-energia
    {:kaukolampo-vuosikulutus      0
     :kokonaissahko-vuosikulutus   0
     :kiinteistosahko-vuosikulutus 40000
     :kayttajasahko-vuosikulutus   140000
     :kaukojaahdytys-vuosikulutus  0}
    :ostetut-polttoaineet
    {:kevyt-polttooljy      0
     :pilkkeet-havu-sekapuu 0
     :pilkkeet-koivu        0
     :puupelletit           0
     :muu []}
    :sahko-vuosikulutus-yhteensa          180000
    :kaukolampo-vuosikulutus-yhteensa     0
    :polttoaineet-vuosikulutus-yhteensa   0
    :kaukojaahdytys-vuosikulutus-yhteensa 0}
   :huomiot
   {:suositukset-fi    "Rakennuksen käyttöön ja ylläpitoon liittyen on tärkeää säännöllisesti tarkistaa ja huoltaa kaikki tekniset järjestelmät, kuten lämmitys, ilmanvaihto ja sähköjärjestelmät, varmistaen niiden optimaalinen toiminta. Lisäksi on suositeltavaa seurata energiankulutusta ja harkita energiatehokkaiden laitteiden käyttöönottoa. Myös ilmanvaihdon ja ilmastoinnin suodattimien vaihto säännöllisin väliajoin sekä valaistuksen tehokkuuden parantaminen voi edistää rakennuksen energiatehokkuutta."
    :suositukset-sv    "Angående användning och underhåll av byggnaden är det viktigt att regelbundet inspektera och underhålla alla tekniska system, såsom uppvärmning, ventilation och elektriska system, för att säkerställa deras optimala funktion. Det är också rekommenderat att övervaka energiförbrukningen och överväga användning av energieffektiva apparater. Byta luftfilter i ventilation och klimatanläggning med jämna mellanrum samt förbättra belysningens effektivitet kan också främja byggnadens energieffektivitet."
    :lisatietoja-fi    nil
    :lisatietoja-sv    nil
    :ymparys
    {:teksti-fi "Ulkoseinät, ulko-ovet ja ikkunat ovat keskeisiä tekijöitä rakennuksen energiatehokkuudessa. Parantamalla eristystä ulkoseinissä, valitsemalla energiatehokkaampia ikkunoita ja ovia sekä ehkäisemällä ilmanvuotoa, voidaan merkittävästi vähentää lämmitys- ja jäähdytystarvetta."
     :teksti-sv "Ytterväggar, ytterdörrar och fönster är centrala faktorer för byggnadens energieffektivitet. Genom att förbättra isoleringen i ytterväggarna, välja energieffektivare fönster och dörrar samt minska luftläckage kan uppvärmnings- och kylbehovet betydligt minskas."
     :toimenpide [{:nimi-fi "Lisätään ulkoseinien eristystä parantamaan lämmöneristystä ja vähentämään lämmitystarvetta."
                   :nimi-sv "Ökar isoleringen i ytterväggarna för att förbättra värmeisoleringen och minska uppvärmningsbehovet."
                   :lampo 0 :sahko 1000 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Vaihdetaan vanhat ikkunat energiatehokkaampiin malleihin pienentäen lämpöhäviöitä."
                   :nimi-sv "Byter ut gamla fönster mot energieffektivare modeller för att minska värmeförluster."
                   :lampo 0 :sahko 1000 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Asennetaan ilmanpitävät tiivisteet ulko-ovien ympärille vähentämään ilmanvuotoa."
                   :nimi-sv "Installerar lufttäta tätningar runt ytterdörrarna för att minska luftläckage."
                   :lampo 0 :sahko 1000 :jaahdytys 0 :eluvun-muutos 0}]}
    :alapohja-ylapohja
    {:teksti-fi "Ylä- ja alapohjat ovat keskeisiä osia rakennuksen eristämisessä ja energiatehokkuuden parantamisessa. Parantamalla näiden osien eristystä ja vedenpitävyyttä voidaan vähentää lämpöhäviöitä ja kosteusongelmien riskiä."
     :teksti-sv "Vind- och bottenvåningarna är centrala delar av byggnadens isolering och förbättring av energieffektiviteten. Genom att förbättra isoleringen och vattentätheten i dessa delar kan värmeförluster minskas och risken för fuktproblem minimeras."
     :toimenpide [{:nimi-fi "Lisätään yläpohjan eristystä parantamaan lämmöneristystä ja vähentämään lämpöhäviöitä."
                   :nimi-sv "Ökar isoleringen i takvåningen för att förbättra värmeisoleringen och minska värmeförlusterna."
                   :lampo 0 :sahko 1000 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Käytetään alapohjassa kosteutta eristäviä materiaaleja estääksemme kosteusongelmia."
                   :nimi-sv "Använder fuktavvisande material i bottenvåningen och tätar fogarna för att förebygga fuktproblem."
                   :lampo 0 :sahko 0 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Tarkistetaan ala- ja yläpohjien ilmanpitävyys, mikä auttaa säästämään energiaa."
                   :nimi-sv "Kontrollerar lufttätheten i bottenvåningen och säkerställer att det inte finns något luftläckage."
                   :lampo 0 :sahko 100 :jaahdytys 0 :eluvun-muutos 0}]}
    :lammitys
    {:teksti-fi "Tilojen ja käyttöveden lämmitysjärjestelmät ovat merkittäviä energiankuluttajia rakennuksessa. Tehokkaampien lämmitysjärjestelmien käyttö, kuten lämpöpumput tai aurinkokeräimet, voi huomattavasti vähentää energiankulutusta ja parantaa E-lukua."
     :teksti-sv "Utrymmenas och användningsvattnets värmesystem är betydande energiförbrukare i byggnaden. Användning av effektivare värmesystem, såsom värmepumpar eller solfångare, kan väsentligt minska energiförbrukningen och förbättra E-värdet."
     :toimenpide [{:nimi-fi "Asennetaan lämpöpumppu."
                   :nimi-sv "Installera en värmepump."
                   :lampo 0 :sahko 10 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Vaihdetaan energiatehokkaaseen lämmitysjärjestelmään."
                   :nimi-sv "Byt till ett energieffektivt värmesystem."
                   :lampo 0 :sahko 10 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Käytetään aurinkokeräimiä."
                   :nimi-sv "Använd solfångare."
                   :lampo 0 :sahko 100 :jaahdytys 0 :eluvun-muutos 0}]}
    :iv-ilmastointi
    {:teksti-fi "Ilmanvaihto- ja ilmastointijärjestelmät ovat olennaisia energiankulutuksen ja sisäilman laadun kannalta. Tehokkaampien ja säädettävien järjestelmien käyttö voi parantaa sekä E-lukua että asumismukavuutta."
     :teksti-sv "Ventilations- och luftkonditioneringssystem är avgörande för energiförbrukning och inomhusluftens kvalitet. Användning av effektivare och justerbara system kan förbättra både E-värdet och boendekomforten."
     :toimenpide [{:nimi-fi "Asenna lämmöntalteenottolaitteisto."
                   :nimi-sv "Installera värmeåtervinningsenhet."
                   :lampo 0 :sahko 12 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Säädä ilmanvaihtoa tarpeen mukaan."
                   :nimi-sv "Justera ventilationen efter behov."
                   :lampo 0 :sahko 421 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Käytä energiatehokkaita suodattimia."
                   :nimi-sv "Använd energieffektiva filter."
                   :lampo 0 :sahko 232 :jaahdytys 0 :eluvun-muutos 0}]}
    :valaistus-muut
    {:teksti-fi "Valaistus, jäähdytysjärjestelmät, sähköiset erillislämmitykset ja muut järjestelmät vaikuttavat merkittävästi rakennuksen energiankulutukseen. Käytä energiatehokkaita valaistusratkaisuja ja jäähdytysjärjestelmiä sekä harkitse uusiutuvaa energiantuotantoa."
     :teksti-sv "Belysning, kylsystem, elektriska uppvärmningar och andra system påverkar byggnadens energiförbrukning betydligt. Använd energieffektiva belysningslösningar och kylsystem samt överväg förnybar energiproduktion."
     :toimenpide [{:nimi-fi "Käytä LED-valaistusta."
                   :nimi-sv "Använd LED-belysning."
                   :lampo 0 :sahko 10 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Asenna energiatehokkaampi jäähdytysjärjestelmä."
                   :nimi-sv "Installera ett mer energieffektivt kylsystem."
                   :lampo 0 :sahko 10 :jaahdytys 0 :eluvun-muutos 0}
                  {:nimi-fi "Vaihda uusiutuvaan sähköntuotantoon."
                   :nimi-sv "Byt till förnybar elproduktion."
                   :lampo 0 :sahko 0 :jaahdytys 0 :eluvun-muutos 0}]}}
   :lisamerkintoja-fi nil
   :lisamerkintoja-sv nil})

(def energiatodistus-2026
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
    :havainnointikaynti       "2025-09-15"
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
    :tilaaja                  "Esimkerkki Tilaaja Oy"
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
    :lkvn-kaytto {:ominaiskulutus 40 :lammitysenergian-nettotarve 4000}
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
   {:laatimisajankohta       "2025-09-20"
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
                      :hiilivarastovaikutus         12
                      :karbonatisoituminen          2}
     :rakennuspaikka {:uudelleenkaytto              0.5
                      :kierratys                    1.5
                      :ylimaarainen-uusiutuvaenergia 0.3
                      :hiilivarastovaikutus         0.8
                      :karbonatisoituminen          0.3}}}
   :lisamerkintoja-fi nil
   :lisamerkintoja-sv nil})

;; ---------------------------------------------------------------------------
;; Insert energiatodistukset via API
;; ---------------------------------------------------------------------------

(defn insert-energiatodistukset!
  "Insert the energiatodistukset via the private API (POST).
   This exercises all API-level validations.
   Returns a map {:et-2013-id <id> :et-2018-id <id> :et-2026-id <id>}."
  []
  (let [resp-2013 (api-post! "/api/private/energiatodistukset/2013" energiatodistus-2013)
        id-2013   (:id resp-2013)
        resp-2018 (api-post! "/api/private/energiatodistukset/2018" energiatodistus-2018)
        id-2018   (:id resp-2018)
        resp-2026 (api-post! "/api/private/energiatodistukset/2026" energiatodistus-2026)
        id-2026   (:id resp-2026)]
    (println (str "Inserted ET2013 id=" id-2013 " (warnings: " (:warnings resp-2013) ")"))
    (println (str "Inserted ET2018 id=" id-2018 " (warnings: " (:warnings resp-2018) ")"))
    (println (str "Inserted ET2026 id=" id-2026 " (warnings: " (:warnings resp-2026) ")"))
    {:et-2013-id id-2013
     :et-2018-id id-2018
     :et-2026-id id-2026}))

;; ---------------------------------------------------------------------------
;; Sign energiatodistukset via API
;; ---------------------------------------------------------------------------

(defn sign-energiatodistukset!
  "Sign the energiatodistukset via the system-sign API endpoint.
   This exercises all signing validations (required fields, pätevyys, etc.).
   `ids` is a map with :et-2013-id, :et-2018-id, and :et-2026-id."
  [{:keys [et-2013-id et-2018-id et-2026-id]}]
  (let [id->versio {et-2013-id 2013 et-2018-id 2018 et-2026-id 2026}]
    (doseq [id [et-2013-id et-2018-id et-2026-id]
            :let [versio (id->versio id)]]
      (let [resp (api-post! (str "/api/private/energiatodistukset/" versio "/" id "/signature/system-sign") nil false)]
        (println (str "Signed energiatodistus id=" id " -> " resp))))))

;; ---------------------------------------------------------------------------
;; System access helpers — for aineisto generation
;; ---------------------------------------------------------------------------

(defn- get-db
  ([] (-> @(resolve 'integrant.repl.state/system) :solita.etp/db))
  ([kayttaja-id] (assoc (get-db) :application-name (str kayttaja-id "@wiki.etp.dev"))))

(defn- get-aws-s3-client []
  (-> @(resolve 'integrant.repl.state/system) :solita.etp/aws-s3-client))

;; ---------------------------------------------------------------------------
;; Resource directory for the CSV and markdown files
;; ---------------------------------------------------------------------------

(def ^:private resource-dir "src/dev/clj/solita/etp/github_wiki/")

;; ---------------------------------------------------------------------------
;; Aineisto CSV generation
;; ---------------------------------------------------------------------------

(defn- download-aineisto-from-s3
  "Download an aineisto CSV from S3 (MinIO) and return it as a string."
  [{:keys [client bucket]} aineisto-id]
  (let [key    (str "api/signed/aineistot/" aineisto-id "/energiatodistukset.csv")
        result (aws/invoke client {:op      :GetObject
                                   :request {:Bucket bucket
                                             :Key    key}})]
    (if (:cognitect.anomalies/category result)
      (throw (ex-info (str "Failed to download aineisto " aineisto-id " from S3")
                      result))
      (slurp (:Body result)))))

(defn- parse-csv-header-indices
  "Parse semicolon-delimited CSV header and return a map of column-name -> index."
  [header]
  (into {}
        (map-indexed (fn [i col] [(str/replace col "\"" "") i]))
        (str/split header #";")))

(defn- filter-csv-rows-by-column
  "Filter semicolon-delimited CSV keeping only rows where the value at `col-idx`
   is in `allowed-values`."
  [csv-content col-name allowed-values]
  (let [lines   (str/split-lines csv-content)
        header  (first lines)
        data    (remove str/blank? (rest lines))
        col-idx (get (parse-csv-header-indices header) col-name)]
    (when (nil? col-idx)
      (throw (ex-info (str "Column " col-name " not found in CSV header") {})))
    (let [allowed (set allowed-values)
          kept    (filter (fn [line]
                            (let [fields (str/split line #";" -1)]
                              (allowed (get fields col-idx))))
                          data)]
      (str/join "\n" (concat [header] kept [""])))))

(defn- extract-column-values-for-ids
  "From a semicolon-delimited CSV with an Id column, extract the values of
   `target-col` for the given `ids`. Returns a set of values."
  [csv-content ids target-col]
  (let [lines   (str/split-lines csv-content)
        header  (first lines)
        indices (parse-csv-header-indices header)
        id-idx  (get indices "Id")
        tgt-idx (get indices target-col)
        id-strs (set (map str ids))
        data    (remove str/blank? (rest lines))]
    (->> data
         (filter (fn [line]
                   (let [fields (str/split line #";" -1)]
                     (id-strs (get fields id-idx)))))
         (map (fn [line]
                (let [fields (str/split line #";" -1)]
                  (get fields tgt-idx))))
         (remove nil?)
         set)))

(defn update-aineistot!
  "Regenerate all three aineisto CSV files into S3 (MinIO), then download
   them back and save as esimerkki_energiatodistukset-{1,2,3}.csv under
   the github_wiki directory.
   When `ids` map is provided, filters CSVs to keep only those rows."
  ([] (update-aineistot! nil))
  ([{:keys [et-2013-id et-2018-id et-2026-id] :as ids}]
   (let [db     (get-db)
         whoami {:id -5 :rooli -1}
         s3     (get-aws-s3-client)
         id-set (when ids [et-2013-id et-2018-id et-2026-id])]
     ;; Upload to S3
     (csv-to-s3-service/update-aineistot-in-s3! db whoami s3)
     (println "Aineistot updated in S3.")
     ;; Download all three CSVs from S3
     (let [raw-csvs (into {} (map (fn [id] [id (download-aineisto-from-s3 s3 id)]) [1 2 3]))]
       ;; Extract Allekirjoitusaika values for our ids from CSV 1 (which has Id column)
       (let [signing-times (when id-set
                             (extract-column-values-for-ids
                               (get raw-csvs 1) id-set "Allekirjoitusaika"))]
         (doseq [id [1 2 3]]
           (let [csv-content (if id-set
                               (let [raw (get raw-csvs id)
                                     header (first (str/split-lines raw))
                                     has-id? (contains? (parse-csv-header-indices header) "Id")]
                                 (if has-id?
                                   ;; CSVs 1 & 2: filter by Id
                                   (filter-csv-rows-by-column raw "Id" (map str id-set))
                                   ;; CSV 3: filter by Allekirjoitusaika
                                   (filter-csv-rows-by-column raw "Allekirjoitusaika" signing-times)))
                               (get raw-csvs id))
                 filename    (str resource-dir "esimerkki_energiatodistukset-" id ".csv")]
             (spit filename csv-content)
             (println (str "Downloaded and wrote " filename)))))
       (println "All three aineistot downloaded from MinIO and saved locally.")))))

;; ---------------------------------------------------------------------------
;; All-in-one: insert, sign, and generate aineistot
;; ---------------------------------------------------------------------------

(defn create-and-sign-energiatodistukset!
  "Insert energiatodistukset via API, sign them via API, and regenerate aineistot.
   Returns the inserted ids."
  []
  (let [ids (insert-energiatodistukset!)]
    (sign-energiatodistukset! ids)
    (update-aineistot! ids)
    ids))
