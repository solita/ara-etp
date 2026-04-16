(ns solita.etp.service.energiatodistus-csv
  (:require [clojure.string :as str]
            [schema-tools.core :as schema-tools]
            [solita.etp.service.csv :as csv]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.schema.public-energiatodistus :as public-energiatodistus-schema]
            [solita.etp.service.energiatodistus-search :as
             energiatodistus-search-service]
            [solita.etp.service.complete-energiatodistus
             :as complete-energiatodistus-service]))

(def private-columns
  (concat
   (for [k [:id :perusparannuspassi-id :versio :tila-id :laatija-id :laatija-fullname
            :allekirjoitusaika :voimassaolo-paattymisaika
            :laskutusaika :draft-visible-to-paakayttaja :bypass-validation-limits
            :bypass-validation-limits-reason :korvattu-energiatodistus-id
            :korvaava-energiatodistus-id ;; TODO is this valid?
            :laskutettava-yritys-id :laskuriviviite]]
     [k])
   [[:perustiedot :yritys :nimi]]
   (for [child [:tilaaja :kieli :kieli-fi :kieli-sv :laatimisvaihe
                 :laatimisvaihe-fi :laatimisvaihe-sv :havainnointikaynti
                 :uudisrakennus
                 :havainnointikayntityyppi-id :havainnointikayntityyppi-fi
                 :havainnointikayntityyppi-sv
                 :tayttaa-aplus-vaatimukset :tayttaa-a0-vaatimukset]]
      [:perustiedot child])
   [[:tulokset :laskentatyokalu]]
   (for [child [:nimi-fi :nimi-sv :valmistumisvuosi :rakennusosa :katuosoite-fi
                :katuosoite-sv :postinumero :postitoimipaikka-fi
                :postitoimipaikka-sv :rakennustunnus :kiinteistotunnus
                :paakayttotarkoitus-id :paakayttotarkoitus-fi :paakayttotarkoitus-sv
                :kayttotarkoitus :alakayttotarkoitus-fi :alakayttotarkoitus-sv
                :julkinen-rakennus]]
     [:perustiedot child])
   [[:tulokset :e-luku]
    [:tulokset :e-luokka]
    [:tulokset :e-luokka-rajat :raja-uusi-2018]
    [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]]
   (for [child [:keskeiset-suositukset-fi :keskeiset-suositukset-sv]]
     [:perustiedot child])
   [[:lahtotiedot :lammitetty-nettoala]]
   (for [child [:ilmanvuotoluku :lampokapasiteetti :ilmatilavuus]]
     [:lahtotiedot :rakennusvaippa child])
   (for [parent [:ulkoseinat :ylapohja :alapohja :ikkunat :ulkoovet]
         child [:ala :U :UA :osuus-lampohaviosta]]
     [:lahtotiedot :rakennusvaippa parent child])
   [[:lahtotiedot :rakennusvaippa :kylmasillat-UA]
    [:lahtotiedot :rakennusvaippa :kylmasillat-osuus-lampohaviosta]
    [:lahtotiedot :rakennusvaippa :UA-summa]]
   (for [parent [:pohjoinen :koillinen :ita :kaakko :etela :lounas :lansi
                 :luode :valokupu :katto]
         child [:ala :U :g-ks]]
     [:lahtotiedot :ikkunat parent child])
   (for [child [:tyyppi-id :label-fi :label-sv :kuvaus-fi :kuvaus-sv]]
     [:lahtotiedot :ilmanvaihto child])
   (for [child [:tulo :poisto :tulo-poisto :sfp :lampotilasuhde :jaatymisenesto]]
     [:lahtotiedot :ilmanvaihto :paaiv child])
   (for [parent [:erillispoistot :ivjarjestelma]
         child [:tulo :poisto :tulo-poisto :sfp]]
     [:lahtotiedot :ilmanvaihto parent child])
   [[:lahtotiedot :ilmanvaihto :lto-vuosihyotysuhde]
    [:lahtotiedot :ilmanvaihto :tuloilma-lampotila]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-label-sv]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammonjako :id]
    [:lahtotiedot :lammitys :lammonjako-label-fi]
    [:lahtotiedot :lammitys :lammonjako-label-sv]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-fi]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-sv]]
   (for [parent [:tilat-ja-iv :lammin-kayttovesi]
         child [:tuoton-hyotysuhde :jaon-hyotysuhde :lampokerroin :apulaitteet
                :lampopumppu-tuotto-osuus :lampohavio-lammittamaton-tila]]
     [:lahtotiedot :lammitys parent child])
   (for [parent [:takka :ilmalampopumppu]
         child [:maara :tuotto]]
     [:lahtotiedot :lammitys parent child])
   [[:lahtotiedot :jaahdytysjarjestelma :jaahdytyskauden-painotettu-kylmakerroin]
    [:lahtotiedot :lkvn-kaytto :ominaiskulutus]
    [:lahtotiedot :lkvn-kaytto :lammitysenergian-nettotarve]
    [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin]
    [:lahtotiedot :lammitys :lammonjako-lampotilajousto]]
   (for [parent [:henkilot :kuluttajalaitteet :valaistus]
         child [:kayttoaste :lampokuorma]]
     [:lahtotiedot :sis-kuorma parent child])
   (for [child [:kaukolampo :kaukolampo-nettoala :kaukolampo-kerroin
                :kaukolampo-kertoimella :kaukolampo-nettoala-kertoimella :sahko
                :sahko-nettoala :sahko-kerroin :sahko-kertoimella
                :sahko-nettoala-kertoimella :uusiutuva-polttoaine
                :uusiutuva-polttoaine-nettoala :uusiutuva-polttoaine-kerroin
                :uusiutuva-polttoaine-kertoimella
                :uusiutuva-polttoaine-nettoala-kertoimella
                :fossiilinen-polttoaine :fossiilinen-polttoaine-nettoala
                :fossiilinen-polttoaine-kerroin
                :fossiilinen-polttoaine-kertoimella
                :fossiilinen-polttoaine-nettoala-kertoimella :kaukojaahdytys
                :kaukojaahdytys-nettoala :kaukojaahdytys-kerroin
                :kaukojaahdytys-kertoimella
                :kaukojaahdytys-nettoala-kertoimella
                :valaistus-kuluttaja-sahko :valaistus-kuluttaja-sahko-nettoala]]
     [:tulokset :kaytettavat-energiamuodot child])
   (for [idx (range 3)
         child [:nimi :ostoenergia :muotokerroin :ostoenergia-nettoala
                :ostoenergia-kertoimella :ostoenergia-nettoala-kertoimella]]
     [:tulokset :kaytettavat-energiamuodot :muu idx child])
   [[:tulokset :kaytettavat-energiamuodot :summa]
    [:tulokset :kaytettavat-energiamuodot :kertoimella-summa]]
   [[:tulokset :kasvihuonepaastot]
    [:tulokset :kasvihuonepaastot-nettoala]]
   (for [child [:aurinkosahko :aurinkosahko-nettoala :aurinkolampo
                :aurinkolampo-nettoala :tuulisahko :tuulisahko-nettoala
                :lampopumppu :lampopumppu-nettoala :muusahko :muusahko-nettoala
                :muulampo :muulampo-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat child])
   (for [idx (range 6)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat idx child])
   (for [child [:aurinkosahko :aurinkolampo :tuulisahko :lampopumppu
                :muulampo :muusahko]]
     [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto child])
   (for [parent [:tilojen-lammitys :tuloilman-lammitys :kayttoveden-valmistus]
         child [:sahko :lampo]]
     [:tulokset :tekniset-jarjestelmat parent child])
   [[:tulokset :tekniset-jarjestelmat :iv-sahko]]
   (for [child [:sahko :lampo :kaukojaahdytys]]
     [:tulokset :tekniset-jarjestelmat :jaahdytys child])
   [[:tulokset :tekniset-jarjestelmat :kuluttajalaitteet-ja-valaistus-sahko]
    [:tulokset :tekniset-jarjestelmat :sahko-summa]
    [:tulokset :tekniset-jarjestelmat :lampo-summa]
    [:tulokset :tekniset-jarjestelmat :kaukojaahdytys-summa]]
   (for [child [:tilojen-lammitys-vuosikulutus
                :tilojen-lammitys-vuosikulutus-nettoala
                :ilmanvaihdon-lammitys-vuosikulutus
                :ilmanvaihdon-lammitys-vuosikulutus-nettoala
                :kayttoveden-valmistus-vuosikulutus
                :kayttoveden-valmistus-vuosikulutus-nettoala
                :jaahdytys-vuosikulutus :jaahdytys-vuosikulutus-nettoala]]
     [:tulokset :nettotarve child])
   (for [child [:aurinko :aurinko-nettoala :ihmiset :ihmiset-nettoala
                :kuluttajalaitteet :kuluttajalaitteet-nettoala
                :valaistus :valaistus-nettoala :kvesi :kvesi-nettoala]]
     [:tulokset :lampokuormat child])
   (for [child [:kaukolampo-vuosikulutus :kaukolampo-vuosikulutus-nettoala
                :kokonaissahko-vuosikulutus :kokonaissahko-vuosikulutus-nettoala
                :kiinteistosahko-vuosikulutus
                :kiinteistosahko-vuosikulutus-nettoala
                :kayttajasahko-vuosikulutus :kayttajasahko-vuosikulutus-nettoala
                :kaukojaahdytys-vuosikulutus
                :kaukojaahdytys-vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia child])
   (for [idx (range 5)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia :muu idx child])
   (for [child [:kevyt-polttooljy :kevyt-polttooljy-kerroin
                :kevyt-polttooljy-kwh :kevyt-polttooljy-kwh-nettoala
                :pilkkeet-havu-sekapuu :pilkkeet-havu-sekapuu-kerroin
                :pilkkeet-havu-sekapuu-kwh :pilkkeet-havu-sekapuu-kwh-nettoala
                :pilkkeet-koivu :pilkkeet-koivu-kerroin :pilkkeet-koivu-kwh
                :pilkkeet-koivu-kwh-nettoala :puupelletit :puupelletit-kerroin
                :puupelletit-kwh :puupelletit-kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet child])
   (for [idx (range 3)
         child [:nimi :maara-vuodessa :yksikko :muunnoskerroin :kwh
                :kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu idx child])
   (for [child [:sahko-vuosikulutus-yhteensa
                :sahko-vuosikulutus-yhteensa-nettoala
                :kaukolampo-vuosikulutus-yhteensa
                :kaukolampo-vuosikulutus-yhteensa-nettoala
                :polttoaineet-vuosikulutus-yhteensa
                :polttoaineet-vuosikulutus-yhteensa-nettoala
                :kaukojaahdytys-vuosikulutus-yhteensa
                :kaukojaahdytys-vuosikulutus-yhteensa-nettoala
                :summa :summa-nettoala
                :tietojen-alkuperavuosi
                :lisatietoja-fi :lisatietoja-sv
                :uusiutuvat-polttoaineet-vuosikulutus-yhteensa
                :uusiutuvat-polttoaineet-vuosikulutus-yhteensa-nettoala
                :fossiiliset-polttoaineet-vuosikulutus-yhteensa
                :fossiiliset-polttoaineet-vuosikulutus-yhteensa-nettoala
                :uusiutuva-energia-vuosituotto-yhteensa
                :uusiutuva-energia-vuosituotto-yhteensa-nettoala]]
     [:toteutunut-ostoenergiankulutus child])
   (apply concat
          (for [parent [:ymparys :alapohja-ylapohja :lammitys :iv-ilmastointi
                        :valaistus-muut]]
            (concat
             [[:huomiot parent :teksti-fi]
              [:huomiot parent :teksti-sv]]
             (for [idx (range 3)
                   child [:nimi-fi :nimi-sv :lampo :sahko :jaahdytys
                          :eluvun-muutos :kasvihuonepaastojen-muutos]]
               [:huomiot parent :toimenpide idx child]))))
   [[:huomiot :lammitys :kayttoikaa-jaljella-arvio-vuosina]]
   (for [child [:suositukset-fi :suositukset-sv :lisatietoja-fi
                :lisatietoja-sv]]
     [:huomiot child])
   [[:lisamerkintoja-fi]
    [:lisamerkintoja-sv]]
   ;; ET2026: Ilmastoselvitys
   (for [child [:laatimisajankohta :laatija :yritys :yritys-osoite
                :yritys-postinumero :yritys-postitoimipaikka :laadintaperuste]]
     [:ilmastoselvitys child])
   (for [parent [:rakennus :rakennuspaikka]
         child [:rakennustuotteiden-valmistus :kuljetukset-tyomaavaihe
                :rakennustuotteiden-vaihdot :energiankaytto :purkuvaihe]]
     [:ilmastoselvitys :hiilijalanjalki parent child])
   (for [parent [:rakennus :rakennuspaikka]
         child [:uudelleenkaytto :kierratys :ylimaarainen-uusiutuvaenergia
                :hiilivarastovaikutus :karbonatisoituminen]]
     [:ilmastoselvitys :hiilikadenjalki parent child])))

(def public-columns
  (let [extra-columns #{[:perustiedot :alakayttotarkoitus-fi]
                        [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]
                        [:tulokset :e-luokka-rajat :raja-uusi-2018]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :sahko-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]}
        hidden-columns #{[:tila-id]
                         [:korvaava-energiatodistus-id]
                         [:laatija-id]
                         [:laatija-fullname]
                         [:perustiedot :yritys :nimi]}]
    (filter
     (fn [column]
       (and
        (or (contains? extra-columns column)
            (schema-tools/get-in public-energiatodistus-schema/Energiatodistus2018 column))
        (not (contains? hidden-columns column))))
     private-columns)))

(def ^:private et2026-bank-columns
  "ET2026 columns appended at the end of bank CSV"
  [[:perustiedot :havainnointikayntityyppi-fi]
   [:perustiedot :tayttaa-aplus-vaatimukset]
   [:perustiedot :tayttaa-a0-vaatimukset]
   [:tulokset :kasvihuonepaastot]
   [:tulokset :kasvihuonepaastot-nettoala]])

(def bank-columns
  (let [extra-columns #{[:perustiedot :kieli-fi]
                        [:perustiedot :laatimisvaihe-fi]
                        [:perustiedot :paakayttotarkoitus-id]
                        [:perustiedot :paakayttotarkoitus-fi]
                        [:perustiedot :alakayttotarkoitus-fi]
                        [:perustiedot :kiinteistotunnus]
                        [:perustiedot :postitoimipaikka-fi]
                        [:perustiedot :postitoimipaikka-sv]
                        [:lahtotiedot :ilmanvaihto :label-fi]
                        [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
                        [:lahtotiedot :lammitys :lammonjako-label-fi]
                        [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]
                        [:tulokset :e-luokka-rajat :raja-uusi-2018]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :sahko-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]}
        hidden-columns #{[:tila-id]
                         [:korvaava-energiatodistus-id]
                         [:laatija-id]
                         [:laatija-fullname]
                         [:perustiedot :yritys :nimi]}
        et2026-set (set et2026-bank-columns)]
    (concat
     (filter
      (fn [column]
        (and
         (or (contains? extra-columns column)
             (schema-tools/get-in public-energiatodistus-schema/Energiatodistus2018 column))
         (not (contains? hidden-columns column))
         (not (contains? et2026-set column))))
      private-columns)
     et2026-bank-columns)))

(def ^:private et2026-laaja-aineisto-tagged-columns
  "ET2026 columns for tilastokeskus and anonymized (laaja) datasets.
   Each entry is a pair [visibility column-path] where visibility is:
   - :both              — included in both tilastokeskus and anonymized
   - :tilastokeskus     — only in tilastokeskus (contains personal data)"
  (into
   [;; Perustiedot
    [:both [:perustiedot :havainnointikayntityyppi-id]]
    [:both [:perustiedot :havainnointikayntityyppi-fi]]
    [:both [:perustiedot :tayttaa-aplus-vaatimukset]]
    [:both [:perustiedot :tayttaa-a0-vaatimukset]]

    ;; Lahtotiedot
    [:both [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin]]
    [:both [:lahtotiedot :lammitys :lammonjako-lampotilajousto]]

    ;; Tulokset: Kasvihuonepaastot
    [:both [:tulokset :kasvihuonepaastot]]
    [:both [:tulokset :kasvihuonepaastot-nettoala]]

    ;; Tulokset: Uusiutuvat omavaraisenergiat kokonaistuotanto
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkosahko]]
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkolampo]]
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :tuulisahko]]
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :lampopumppu]]
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muulampo]]
    [:both [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muusahko]]

    ;; Toteutunut ostoenergiankulutus
    [:both [:toteutunut-ostoenergiankulutus :tietojen-alkuperavuosi]]
    [:both [:toteutunut-ostoenergiankulutus :lisatietoja-fi]]
    [:both [:toteutunut-ostoenergiankulutus :lisatietoja-sv]]
    [:both [:toteutunut-ostoenergiankulutus :uusiutuvat-polttoaineet-vuosikulutus-yhteensa]]
    [:both [:toteutunut-ostoenergiankulutus :uusiutuvat-polttoaineet-vuosikulutus-yhteensa-nettoala]]
    [:both [:toteutunut-ostoenergiankulutus :fossiiliset-polttoaineet-vuosikulutus-yhteensa]]
    [:both [:toteutunut-ostoenergiankulutus :fossiiliset-polttoaineet-vuosikulutus-yhteensa-nettoala]]
    [:both [:toteutunut-ostoenergiankulutus :uusiutuva-energia-vuosituotto-yhteensa]]
    [:both [:toteutunut-ostoenergiankulutus :uusiutuva-energia-vuosituotto-yhteensa-nettoala]]]

   ;; Huomiot + ilmastoselvitys
   (concat
    (for [idx (range 3)] [:both [:huomiot :ymparys :toimenpide idx :kasvihuonepaastojen-muutos]])
    (for [idx (range 3)] [:both [:huomiot :alapohja-ylapohja :toimenpide idx :kasvihuonepaastojen-muutos]])
    (for [idx (range 3)] [:both [:huomiot :lammitys :toimenpide idx :kasvihuonepaastojen-muutos]])
    (for [idx (range 3)] [:both [:huomiot :iv-ilmastointi :toimenpide idx :kasvihuonepaastojen-muutos]])
    (for [idx (range 3)] [:both [:huomiot :valaistus-muut :toimenpide idx :kasvihuonepaastojen-muutos]])

    [[:both [:huomiot :lammitys :kayttoikaa-jaljella-arvio-vuosina]]

     ;; Ilmastoselvitys
     [:both          [:ilmastoselvitys :laatimisajankohta]]
     [:tilastokeskus [:ilmastoselvitys :laatija]]
     [:tilastokeskus [:ilmastoselvitys :yritys]]
     [:tilastokeskus [:ilmastoselvitys :yritys-osoite]]
     [:tilastokeskus [:ilmastoselvitys :yritys-postinumero]]
     [:tilastokeskus [:ilmastoselvitys :yritys-postitoimipaikka]]
     [:both          [:ilmastoselvitys :laadintaperuste]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-valmistus]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennus :kuljetukset-tyomaavaihe]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-vaihdot]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennus :energiankaytto]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennus :purkuvaihe]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :rakennustuotteiden-valmistus]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :kuljetukset-tyomaavaihe]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :rakennustuotteiden-vaihdot]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :energiankaytto]]
     [:both [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :purkuvaihe]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennus :uudelleenkaytto]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennus :kierratys]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennus :ylimaarainen-uusiutuvaenergia]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennus :hiilivarastovaikutus]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennus :karbonatisoituminen]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :uudelleenkaytto]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :kierratys]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :ylimaarainen-uusiutuvaenergia]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :hiilivarastovaikutus]]
     [:both [:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :karbonatisoituminen]]])))

(defn- filter-tagged-columns [allowed-tags tagged-columns]
  (let [allowed (set allowed-tags)]
    (->> tagged-columns
         (filter (fn [[tag _]] (contains? allowed tag)))
         (mapv second))))

(def ^:private et2026-anonymized-columns
  (filter-tagged-columns [:both] et2026-laaja-aineisto-tagged-columns))

(def ^:private et2026-tilastokeskus-columns
  (filter-tagged-columns [:both :tilastokeskus] et2026-laaja-aineisto-tagged-columns))

(def anonymized-columns
  (concat
   (for [k [:versio :tila-id
            :allekirjoitusaika :voimassaolo-paattymisaika]]
     [k])
   (for [child [:kieli :laatimisvaihe
                :havainnointikaynti
                :uudisrakennus]]
     [:perustiedot child])
   (for [child [:valmistumisvuosi
                :postinumero :postitoimipaikka-fi
                :paakayttotarkoitus-fi
                :kayttotarkoitus
                :alakayttotarkoitus-fi
                :julkinen-rakennus]]
     [:perustiedot child])
   [[:tulokset :e-luku]
    [:tulokset :e-luokka]
    [:tulokset :e-luokka-rajat :raja-uusi-2018]
    [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]]
   (for [child [:keskeiset-suositukset-fi :keskeiset-suositukset-sv]]
     [:perustiedot child])
   [[:lahtotiedot :lammitetty-nettoala]]
   (for [child [:ilmanvuotoluku :lampokapasiteetti :ilmatilavuus]]
     [:lahtotiedot :rakennusvaippa child])
   (for [parent [:ulkoseinat :ylapohja :alapohja :ikkunat :ulkoovet]
         child [:ala :U]]
     [:lahtotiedot :rakennusvaippa parent child])
   [[:lahtotiedot :rakennusvaippa :kylmasillat-UA]
    [:lahtotiedot :rakennusvaippa :kylmasillat-osuus-lampohaviosta]
    [:lahtotiedot :rakennusvaippa :UA-summa]]
   (for [parent [:pohjoinen :koillinen :ita :kaakko :etela :lounas :lansi
                 :luode :valokupu :katto]
         child [:ala :U :g-ks]]
     [:lahtotiedot :ikkunat parent child])
   (for [child [:tyyppi-id :label-fi :kuvaus-fi :kuvaus-sv]]
     [:lahtotiedot :ilmanvaihto child])
   (for [child [:tulo :poisto :tulo-poisto :sfp :lampotilasuhde :jaatymisenesto]]
     [:lahtotiedot :ilmanvaihto :paaiv child])
   (for [parent [:erillispoistot :ivjarjestelma]
         child [:tulo :poisto :tulo-poisto :sfp]]
     [:lahtotiedot :ilmanvaihto parent child])
   [[:lahtotiedot :ilmanvaihto :lto-vuosihyotysuhde]
    [:lahtotiedot :ilmanvaihto :tuloilma-lampotila]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammonjako :id]
    [:lahtotiedot :lammitys :lammonjako-label-fi]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-fi]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-sv]]
   (for [parent [:tilat-ja-iv :lammin-kayttovesi]
         child [:tuoton-hyotysuhde :jaon-hyotysuhde :lampokerroin :apulaitteet
                :lampopumppu-tuotto-osuus :lampohavio-lammittamaton-tila]]
     [:lahtotiedot :lammitys parent child])
   (for [parent [:takka :ilmalampopumppu]
         child [:maara :tuotto]]
     [:lahtotiedot :lammitys parent child])
   [[:lahtotiedot :jaahdytysjarjestelma :jaahdytyskauden-painotettu-kylmakerroin]
    [:lahtotiedot :lkvn-kaytto :ominaiskulutus]
    [:lahtotiedot :lkvn-kaytto :lammitysenergian-nettotarve]]
   (for [parent [:henkilot :kuluttajalaitteet :valaistus]
         child [:kayttoaste :lampokuorma]]
     [:lahtotiedot :sis-kuorma parent child])
   (for [child [:kaukolampo :kaukolampo-nettoala :kaukolampo-kerroin
                :kaukolampo-kertoimella :kaukolampo-nettoala-kertoimella :sahko
                :sahko-nettoala :sahko-kerroin :sahko-kertoimella
                :sahko-nettoala-kertoimella :uusiutuva-polttoaine
                :uusiutuva-polttoaine-nettoala :uusiutuva-polttoaine-kerroin
                :uusiutuva-polttoaine-kertoimella
                :uusiutuva-polttoaine-nettoala-kertoimella
                :fossiilinen-polttoaine :fossiilinen-polttoaine-nettoala
                :fossiilinen-polttoaine-kerroin
                :fossiilinen-polttoaine-kertoimella
                :fossiilinen-polttoaine-nettoala-kertoimella :kaukojaahdytys
                :kaukojaahdytys-nettoala :kaukojaahdytys-kerroin
                :kaukojaahdytys-kertoimella
                :kaukojaahdytys-nettoala-kertoimella]]
     [:tulokset :kaytettavat-energiamuodot child])
   (for [idx (range 3)
         child [:nimi :ostoenergia :muotokerroin :ostoenergia-nettoala
                :ostoenergia-kertoimella :ostoenergia-nettoala-kertoimella]]
     [:tulokset :kaytettavat-energiamuodot :muu idx child])
   [[:tulokset :kaytettavat-energiamuodot :summa]
    [:tulokset :kaytettavat-energiamuodot :kertoimella-summa]]
   (for [child [:aurinkosahko :aurinkosahko-nettoala :aurinkolampo
                :aurinkolampo-nettoala :tuulisahko :tuulisahko-nettoala
                :lampopumppu :lampopumppu-nettoala :muusahko :muusahko-nettoala
                :muulampo :muulampo-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat child])
   (for [idx (range 6)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat idx child])
   (for [parent [:tilojen-lammitys :tuloilman-lammitys :kayttoveden-valmistus]
         child [:sahko :lampo]]
     [:tulokset :tekniset-jarjestelmat parent child])
   [[:tulokset :tekniset-jarjestelmat :iv-sahko]]
   (for [child [:sahko :lampo :kaukojaahdytys]]
     [:tulokset :tekniset-jarjestelmat :jaahdytys child])
   [[:tulokset :tekniset-jarjestelmat :kuluttajalaitteet-ja-valaistus-sahko]
    [:tulokset :tekniset-jarjestelmat :sahko-summa]
    [:tulokset :tekniset-jarjestelmat :lampo-summa]
    [:tulokset :tekniset-jarjestelmat :kaukojaahdytys-summa]]
   (for [child [:tilojen-lammitys-vuosikulutus
                :tilojen-lammitys-vuosikulutus-nettoala
                :ilmanvaihdon-lammitys-vuosikulutus
                :ilmanvaihdon-lammitys-vuosikulutus-nettoala
                :kayttoveden-valmistus-vuosikulutus
                :kayttoveden-valmistus-vuosikulutus-nettoala
                :jaahdytys-vuosikulutus :jaahdytys-vuosikulutus-nettoala]]
     [:tulokset :nettotarve child])
   (for [child [:aurinko :aurinko-nettoala :ihmiset :ihmiset-nettoala
                :kuluttajalaitteet :kuluttajalaitteet-nettoala
                :valaistus :valaistus-nettoala :kvesi :kvesi-nettoala]]
     [:tulokset :lampokuormat child])
   (for [child [:kaukolampo-vuosikulutus :kaukolampo-vuosikulutus-nettoala
                :kokonaissahko-vuosikulutus :kokonaissahko-vuosikulutus-nettoala
                :kiinteistosahko-vuosikulutus
                :kiinteistosahko-vuosikulutus-nettoala
                :kayttajasahko-vuosikulutus :kayttajasahko-vuosikulutus-nettoala
                :kaukojaahdytys-vuosikulutus
                :kaukojaahdytys-vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia child])
   (for [idx (range 5)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia :muu idx child])
   (for [child [:kevyt-polttooljy :kevyt-polttooljy-kerroin
                :kevyt-polttooljy-kwh :kevyt-polttooljy-kwh-nettoala
                :pilkkeet-havu-sekapuu :pilkkeet-havu-sekapuu-kerroin
                :pilkkeet-havu-sekapuu-kwh :pilkkeet-havu-sekapuu-kwh-nettoala
                :pilkkeet-koivu :pilkkeet-koivu-kerroin :pilkkeet-koivu-kwh
                :pilkkeet-koivu-kwh-nettoala :puupelletit :puupelletit-kerroin
                :puupelletit-kwh :puupelletit-kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet child])
   (for [idx (range 3)
         child [:nimi :maara-vuodessa :yksikko :muunnoskerroin :kwh
                :kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu idx child])
   (for [child [:sahko-vuosikulutus-yhteensa
                :sahko-vuosikulutus-yhteensa-nettoala
                :kaukolampo-vuosikulutus-yhteensa
                :kaukolampo-vuosikulutus-yhteensa-nettoala
                :polttoaineet-vuosikulutus-yhteensa
                :polttoaineet-vuosikulutus-yhteensa-nettoala
                :kaukojaahdytys-vuosikulutus-yhteensa
                :kaukojaahdytys-vuosikulutus-yhteensa-nettoala
                :summa :summa-nettoala]]
     [:toteutunut-ostoenergiankulutus child])
   (apply concat
          (for [parent [:ymparys :alapohja-ylapohja :lammitys :iv-ilmastointi
                        :valaistus-muut]]
            (concat
             [[:huomiot parent :teksti-fi]
              [:huomiot parent :teksti-sv]]
             (for [idx (range 3)
                   child [:nimi-fi :nimi-sv :lampo :sahko :jaahdytys
                          :eluvun-muutos]]
               [:huomiot parent :toimenpide idx child]))))
   (for [child [:suositukset-fi :suositukset-sv]]
     [:huomiot child])
   [[:lisamerkintoja-fi]
    [:lisamerkintoja-sv]]

   ;; ET2026: Uudet sarakkeet (lisatty loppuun)
   et2026-anonymized-columns))

(def tilastokeskus-columns
  (concat
   (for [k [:id :versio :tila-id :laatija-id
            :allekirjoitusaika :voimassaolo-paattymisaika
            :korvattu-energiatodistus-id :korvaava-energiatodistus-id]]
     [k])
   (for [child [:kieli :laatimisvaihe
                :havainnointikaynti
                :uudisrakennus]]
     [:perustiedot child])
   [[:tulokset :laskentatyokalu]]
   (for [child [:nimi-fi :nimi-sv :valmistumisvuosi :rakennusosa
                :katuosoite-fi :katuosoite-sv :postinumero :postitoimipaikka-fi
                :rakennustunnus :kiinteistotunnus
                :paakayttotarkoitus-fi
                :kayttotarkoitus
                :alakayttotarkoitus-fi
                :julkinen-rakennus]]
     [:perustiedot child])
   [[:tulokset :e-luku]
    [:tulokset :e-luokka]
    [:tulokset :e-luokka-rajat :raja-uusi-2018]
    [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]]
   (for [child [:keskeiset-suositukset-fi :keskeiset-suositukset-sv]]
     [:perustiedot child])
   [[:lahtotiedot :lammitetty-nettoala]]
   (for [child [:ilmanvuotoluku :lampokapasiteetti :ilmatilavuus]]
     [:lahtotiedot :rakennusvaippa child])
   (for [parent [:ulkoseinat :ylapohja :alapohja :ikkunat :ulkoovet]
         child [:ala :U]]
     [:lahtotiedot :rakennusvaippa parent child])
   [[:lahtotiedot :rakennusvaippa :kylmasillat-UA]
    [:lahtotiedot :rakennusvaippa :kylmasillat-osuus-lampohaviosta]
    [:lahtotiedot :rakennusvaippa :UA-summa]]
   (for [parent [:pohjoinen :koillinen :ita :kaakko :etela :lounas :lansi
                 :luode :valokupu :katto]
         child [:ala :U :g-ks]]
     [:lahtotiedot :ikkunat parent child])
   (for [child [:tyyppi-id :label-fi :kuvaus-fi :kuvaus-sv]]
     [:lahtotiedot :ilmanvaihto child])
   (for [child [:tulo :poisto :tulo-poisto :sfp :lampotilasuhde :jaatymisenesto]]
     [:lahtotiedot :ilmanvaihto :paaiv child])
   (for [parent [:erillispoistot :ivjarjestelma]
         child [:tulo :poisto :tulo-poisto :sfp]]
     [:lahtotiedot :ilmanvaihto parent child])
   [[:lahtotiedot :ilmanvaihto :lto-vuosihyotysuhde]
    [:lahtotiedot :ilmanvaihto :tuloilma-lampotila]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :id]
    [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi]
    [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv]
    [:lahtotiedot :lammitys :lammonjako :id]
    [:lahtotiedot :lammitys :lammonjako-label-fi]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-fi]
    [:lahtotiedot :lammitys :lammonjako :kuvaus-sv]]
   (for [parent [:tilat-ja-iv :lammin-kayttovesi]
         child [:tuoton-hyotysuhde :jaon-hyotysuhde :lampokerroin :apulaitteet
                :lampopumppu-tuotto-osuus :lampohavio-lammittamaton-tila]]
     [:lahtotiedot :lammitys parent child])
   (for [parent [:takka :ilmalampopumppu]
         child [:maara :tuotto]]
     [:lahtotiedot :lammitys parent child])
   [[:lahtotiedot :jaahdytysjarjestelma :jaahdytyskauden-painotettu-kylmakerroin]
    [:lahtotiedot :lkvn-kaytto :ominaiskulutus]
    [:lahtotiedot :lkvn-kaytto :lammitysenergian-nettotarve]]
   (for [parent [:henkilot :kuluttajalaitteet :valaistus]
         child [:kayttoaste :lampokuorma]]
     [:lahtotiedot :sis-kuorma parent child])
   (for [child [:kaukolampo :kaukolampo-nettoala :kaukolampo-kerroin
                :kaukolampo-kertoimella :kaukolampo-nettoala-kertoimella :sahko
                :sahko-nettoala :sahko-kerroin :sahko-kertoimella
                :sahko-nettoala-kertoimella :uusiutuva-polttoaine
                :uusiutuva-polttoaine-nettoala :uusiutuva-polttoaine-kerroin
                :uusiutuva-polttoaine-kertoimella
                :uusiutuva-polttoaine-nettoala-kertoimella
                :fossiilinen-polttoaine :fossiilinen-polttoaine-nettoala
                :fossiilinen-polttoaine-kerroin
                :fossiilinen-polttoaine-kertoimella
                :fossiilinen-polttoaine-nettoala-kertoimella :kaukojaahdytys
                :kaukojaahdytys-nettoala :kaukojaahdytys-kerroin
                :kaukojaahdytys-kertoimella
                :kaukojaahdytys-nettoala-kertoimella]]
     [:tulokset :kaytettavat-energiamuodot child])
   (for [idx (range 3)
         child [:nimi :ostoenergia :muotokerroin :ostoenergia-nettoala
                :ostoenergia-kertoimella :ostoenergia-nettoala-kertoimella]]
     [:tulokset :kaytettavat-energiamuodot :muu idx child])
   [[:tulokset :kaytettavat-energiamuodot :summa]
    [:tulokset :kaytettavat-energiamuodot :kertoimella-summa]]
   (for [child [:aurinkosahko :aurinkosahko-nettoala :aurinkolampo
                :aurinkolampo-nettoala :tuulisahko :tuulisahko-nettoala
                :lampopumppu :lampopumppu-nettoala :muusahko :muusahko-nettoala
                :muulampo :muulampo-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat child])
   (for [idx (range 6)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:tulokset :uusiutuvat-omavaraisenergiat idx child])
   (for [parent [:tilojen-lammitys :tuloilman-lammitys :kayttoveden-valmistus]
         child [:sahko :lampo]]
     [:tulokset :tekniset-jarjestelmat parent child])
   [[:tulokset :tekniset-jarjestelmat :iv-sahko]]
   (for [child [:sahko :lampo :kaukojaahdytys]]
     [:tulokset :tekniset-jarjestelmat :jaahdytys child])
   [[:tulokset :tekniset-jarjestelmat :kuluttajalaitteet-ja-valaistus-sahko]
    [:tulokset :tekniset-jarjestelmat :sahko-summa]
    [:tulokset :tekniset-jarjestelmat :lampo-summa]
    [:tulokset :tekniset-jarjestelmat :kaukojaahdytys-summa]]
   (for [child [:tilojen-lammitys-vuosikulutus
                :tilojen-lammitys-vuosikulutus-nettoala
                :ilmanvaihdon-lammitys-vuosikulutus
                :ilmanvaihdon-lammitys-vuosikulutus-nettoala
                :kayttoveden-valmistus-vuosikulutus
                :kayttoveden-valmistus-vuosikulutus-nettoala
                :jaahdytys-vuosikulutus :jaahdytys-vuosikulutus-nettoala]]
     [:tulokset :nettotarve child])
   (for [child [:aurinko :aurinko-nettoala :ihmiset :ihmiset-nettoala
                :kuluttajalaitteet :kuluttajalaitteet-nettoala
                :valaistus :valaistus-nettoala :kvesi :kvesi-nettoala]]
     [:tulokset :lampokuormat child])
   (for [child [:kaukolampo-vuosikulutus :kaukolampo-vuosikulutus-nettoala
                :kokonaissahko-vuosikulutus :kokonaissahko-vuosikulutus-nettoala
                :kiinteistosahko-vuosikulutus
                :kiinteistosahko-vuosikulutus-nettoala
                :kayttajasahko-vuosikulutus :kayttajasahko-vuosikulutus-nettoala
                :kaukojaahdytys-vuosikulutus
                :kaukojaahdytys-vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia child])
   (for [idx (range 5)
         child [:nimi-fi :nimi-sv :vuosikulutus :vuosikulutus-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostettu-energia :muu idx child])
   (for [child [:kevyt-polttooljy :kevyt-polttooljy-kerroin
                :kevyt-polttooljy-kwh :kevyt-polttooljy-kwh-nettoala
                :pilkkeet-havu-sekapuu :pilkkeet-havu-sekapuu-kerroin
                :pilkkeet-havu-sekapuu-kwh :pilkkeet-havu-sekapuu-kwh-nettoala
                :pilkkeet-koivu :pilkkeet-koivu-kerroin :pilkkeet-koivu-kwh
                :pilkkeet-koivu-kwh-nettoala :puupelletit :puupelletit-kerroin
                :puupelletit-kwh :puupelletit-kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet child])
   (for [idx (range 3)
         child [:nimi :maara-vuodessa :yksikko :muunnoskerroin :kwh
                :kwh-nettoala]]
     [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu idx child])
   (for [child [:sahko-vuosikulutus-yhteensa
                :sahko-vuosikulutus-yhteensa-nettoala
                :kaukolampo-vuosikulutus-yhteensa
                :kaukolampo-vuosikulutus-yhteensa-nettoala
                :polttoaineet-vuosikulutus-yhteensa
                :polttoaineet-vuosikulutus-yhteensa-nettoala
                :kaukojaahdytys-vuosikulutus-yhteensa
                :kaukojaahdytys-vuosikulutus-yhteensa-nettoala
                :summa :summa-nettoala]]
     [:toteutunut-ostoenergiankulutus child])
   (apply concat
          (for [parent [:ymparys :alapohja-ylapohja :lammitys :iv-ilmastointi
                        :valaistus-muut]]
            (concat
             [[:huomiot parent :teksti-fi]
              [:huomiot parent :teksti-sv]]
             (for [idx (range 3)
                   child [:nimi-fi :nimi-sv :lampo :sahko :jaahdytys
                          :eluvun-muutos]]
               [:huomiot parent :toimenpide idx child]))))
   (for [child [:suositukset-fi :suositukset-sv :lisatietoja-fi
                :lisatietoja-sv]]
     [:huomiot child])
   [[:lisamerkintoja-fi]
    [:lisamerkintoja-sv]]

   ;; ET2026: Uudet sarakkeet (lisatty loppuun)
   et2026-tilastokeskus-columns))

(defn column-ks->str [ks]
  (->> ks
       (map #(if (keyword? %) (name %) %))
       (map str/capitalize)
       (str/join #" / ")))

(defn energiatodistus->csv-line [columns energiatodistus]
  (->> columns
       (map #(get-in energiatodistus %))
       csv/csv-line))

(defn headers-csv-line [columns]
  (->> columns
       (map column-ks->str)
       csv/csv-line))

(defn energiatodistukset-csv-with-filter [db whoami query columns pred]
  (let [luokittelut (complete-energiatodistus-service/luokittelut db)
        energiatodistukset (energiatodistus-search-service/reducible-search
                            db whoami query {:raw false
                                             :result-type :forward-only
                                             :concurrency :read-only
                                             :fetch-size  100})]
    (fn [write!]
      (write! (headers-csv-line columns))
      (run! (comp (fn [et]
                    (when (or (nil? pred) (pred et))
                      (write! (energiatodistus->csv-line columns et))))
                  #(complete-energiatodistus-service/complete-energiatodistus
                    % luokittelut)
                  energiatodistus-service/db-row->energiatodistus)
            energiatodistukset))))

(defn energiatodistukset-csv [db whoami query columns]
  (energiatodistukset-csv-with-filter db whoami query columns nil))

(defn energiatodistukset-private-csv [db whoami query]
  (energiatodistukset-csv db whoami query private-columns))

(defn energiatodistukset-public-csv [db whoami query]
  (energiatodistukset-csv db whoami query public-columns))

(defn energiatodistukset-bank-csv [db whoami]
  (energiatodistukset-csv db whoami {:where [[["=" "energiatodistus.tila-id" 2]]]} bank-columns))

(defn energiatodistukset-tilastokeskus-csv [db whoami]
  (energiatodistukset-csv db whoami {:where nil} tilastokeskus-columns))

(defn energiatodistukset-anonymized-csv [db whoami]
  (let [protected (energiatodistus-service/find-protected-postinumerot db 4)]
    (energiatodistukset-csv-with-filter
     db whoami {:where nil} anonymized-columns
     (fn [{{:keys [kayttotarkoitus postinumero]} :perustiedot
           :keys [versio]}]
       (not (contains? protected {:versio versio
                                  :kayttotarkoitus kayttotarkoitus
                                  :postinumero postinumero}))))))
