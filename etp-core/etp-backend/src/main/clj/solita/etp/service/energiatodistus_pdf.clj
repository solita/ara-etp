(ns solita.etp.service.energiatodistus-pdf
  "Contains functionality to specifically create an energiatodistus as a pdf."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [solita.common.formats :as formats]
            [solita.common.libreoffice :as libreoffice]
            [solita.common.time :as common-time]
            [solita.common.xlsx :as xlsx]
            [solita.etp.config :as config]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.watermark-pdf :as watermark-pdf])
  (:import (java.io ByteArrayOutputStream File)
           (java.time Clock LocalDate ZoneId ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Calendar GregorianCalendar)
           (org.apache.pdfbox.pdmodel PDDocument
                                      PDPageContentStream
                                      PDPageContentStream$AppendMode)
           (org.apache.pdfbox.pdmodel.common PDMetadata)
           (org.apache.pdfbox.pdmodel.graphics.image PDImageXObject)
           (org.apache.xmpbox XMPMetadata)
           (org.apache.xmpbox.xml XmpSerializer)))

;; TODO replace with real templates when it exists
(def xlsx-template-paths {2013 {"fi" "energiatodistus-2013-fi.xlsx"
                                "sv" "energiatodistus-2013-sv.xlsx"}
                          2018 {"fi" "energiatodistus-2018-fi.xlsx"
                                "sv" "energiatodistus-2018-sv.xlsx"}})

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})
(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(def sheet-count 8)
(def tmp-dir "tmp-energiatodistukset/")

(def timezone (ZoneId/of "Europe/Helsinki"))
(def date-formatter (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy") timezone))

(defn sis-kuorma [energiatodistus]
  (->> energiatodistus
       :lahtotiedot
       :sis-kuorma
       (reduce-kv (fn [acc k {:keys [kayttoaste lampokuorma]}]
                    (update acc kayttoaste #(merge % {k lampokuorma})))
                  {})
       (into (sorted-map))
       seq
       (into [])))

(defn find-raja [energiatodistus e-luokka]
  (->> energiatodistus
       :tulokset
       :e-luokka-rajat
       :raja-asteikko
       (filter #(contains? #{% (last %)} e-luokka))
       ffirst))

(def mappings
  {0 [{:path [:id]}
      {:path [:perustiedot :nimi-fi]}
      {:path [:perustiedot :katuosoite-fi]}
      {:path [:perustiedot :katuosoite-sv]}
      {:f #(str (-> % :perustiedot :postinumero)
                " "
                (-> % :perustiedot :postitoimipaikka-fi))}
      {:f #(str (-> % :perustiedot :postinumero)
                " "
                (-> % :perustiedot :postitoimipaikka-sv))}
      {:path [:perustiedot :rakennustunnus]}
      {:path [:perustiedot :valmistumisvuosi]}
      {:path [:perustiedot :alakayttotarkoitus-fi]}
      {:path [:perustiedot :alakayttotarkoitus-sv]}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 0)
             "☒ Uudelle rakennukselle rakennuslupaa haettaessa"
             "☐ Uudelle rakennukselle rakennuslupaa haettaessa")}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 0)
             "☒ för en ny byggnad I samband med att bygglov söks"
             "☐ för en ny byggnad I samband med att bygglov söks")}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 1)
             "☒ Uudelle rakennukselle käyttöönottovaiheessa"
             "☐ Uudelle rakennukselle käyttöönottovaiheessa")}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 1)
             "☒ för en ny byggnad när den tas I bruk"
             "☐ för en ny byggnad när den tas I bruk")}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 2)
             "☒ Olemassa olevalle rakennukselle, havainnointikäynnin päivämäärä:"
             "☐ Olemassa olevalle rakennukselle, havainnointikäynnin päivämäärä:")}
      {:f #(if (= (-> % :perustiedot :laatimisvaihe) 2)
             "☒ för en befintlig byggnad, datum för iakttagelser på plats"
             "☐ för en befintlig byggnad, datum för iakttagelser på plats")}
      {:f #(some->> % :perustiedot :havainnointikaynti (.format date-formatter))}
      {:path [:tulokset :e-luku]}
      {:path [:tulokset :e-luokka-rajat :raja-uusi-2018]}
      {:path [:laatija-fullname]}
      {:path [:perustiedot :yritys :nimi]}
      {:f (fn [_] (.format date-formatter (LocalDate/now)))}
      {:f (fn [{:keys [tila-id]}]
            (when (and tila-id (= (energiatodistus-tila/tila-key tila-id) :in-signing))
              (.format date-formatter (.plusYears (LocalDate/now) 10))))}
      {:path [:perustiedot :nimi-sv]}]
   1 [{:path [:id]}
      {:f #(-> % :lahtotiedot :lammitetty-nettoala (formats/format-number 1 false) (str " m²"))}
      {:path [:lahtotiedot :lammitys :lammitysmuoto-label-fi]}
      {:path [:lahtotiedot :lammitys :lammitysmuoto-label-sv]}
      {:path [:lahtotiedot :lammitys :lammonjako-label-fi]}
      {:path [:lahtotiedot :lammitys :lammonjako-label-sv]}
      {:path [:lahtotiedot :ilmanvaihto :label-fi]}
      {:path [:lahtotiedot :ilmanvaihto :label-sv]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :nimi]}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-nettoala] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :muotokerroin] :dp 1}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :valaistus-kuluttaja-sahko] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :valaistus-kuluttaja-sahko-nettoala] :dp 0}
      {:path [:tulokset :e-luku]}
      {:path [:tulokset :e-luokka-rajat :kayttotarkoitus :label-fi]}
      {:path [:tulokset :e-luokka-rajat :kayttotarkoitus :label-sv]}
      {:f #(str "A: ... " (find-raja % "A"))}
      {:f #(str "B: " (some-> (find-raja % "A") inc) " ... " (find-raja % "B"))}
      {:f #(str "C: " (some-> (find-raja % "B") inc) " ... " (find-raja % "C"))}
      {:f #(str "D: " (some-> (find-raja % "C") inc) " ... " (find-raja % "D"))}
      {:f #(str "E: " (some-> (find-raja % "D") inc) " ... " (find-raja % "E"))}
      {:f #(str "F: " (some-> (find-raja % "E") inc) " ... " (find-raja % "F"))}
      {:f #(str "G: " (some-> (find-raja % "F") inc) " ...")}
      {:path [:tulokset :e-luokka]}
      {:path [:perustiedot :keskeiset-suositukset-fi]}
      {:path [:perustiedot :keskeiset-suositukset-sv]}]
   2 [{:path [:id]}
      {:path [:perustiedot :alakayttotarkoitus-fi]}
      {:path [:perustiedot :alakayttotarkoitus-sv]}
      {:path [:perustiedot :valmistumisvuosi]}
      {:path [:lahtotiedot :lammitetty-nettoala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ilmanvuotoluku] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ulkoseinat :ala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ulkoseinat :U] :dp 2}
      {:path [:lahtotiedot :rakennusvaippa :ulkoseinat :UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ulkoseinat :osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :rakennusvaippa :ylapohja :ala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ylapohja :U] :dp 2}
      {:path [:lahtotiedot :rakennusvaippa :ylapohja :UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ylapohja :osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :rakennusvaippa :alapohja :ala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :alapohja :U] :dp 2}
      {:path [:lahtotiedot :rakennusvaippa :alapohja :UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :alapohja :osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :rakennusvaippa :ikkunat :ala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ikkunat :U] :dp 2}
      {:path [:lahtotiedot :rakennusvaippa :ikkunat :UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ikkunat :osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :rakennusvaippa :ulkoovet :ala] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ulkoovet :U] :dp 2}
      {:path [:lahtotiedot :rakennusvaippa :ulkoovet :UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :ulkoovet :osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :rakennusvaippa :kylmasillat-UA] :dp 1}
      {:path [:lahtotiedot :rakennusvaippa :kylmasillat-osuus-lampohaviosta] :dp 0 :percent? true}
      {:path [:lahtotiedot :ikkunat :pohjoinen :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :pohjoinen :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :pohjoinen :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :koillinen :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :koillinen :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :koillinen :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :ita :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :ita :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :ita :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :kaakko :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :kaakko :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :kaakko :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :etela :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :etela :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :etela :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :lounas :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :lounas :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :lounas :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :lansi :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :lansi :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :lansi :g-ks] :dp 2}
      {:path [:lahtotiedot :ikkunat :luode :ala] :dp 1}
      {:path [:lahtotiedot :ikkunat :luode :U] :dp 2}
      {:path [:lahtotiedot :ikkunat :luode :g-ks] :dp 2}
      {:path [:lahtotiedot :ilmanvaihto :label-fi]}
      {:path [:lahtotiedot :ilmanvaihto :label-sv]}
      {:path [:lahtotiedot :ilmanvaihto :paaiv :tulo-poisto]}
      {:path [:lahtotiedot :ilmanvaihto :paaiv :sfp] :dp 2}
      {:path [:lahtotiedot :ilmanvaihto :paaiv :lampotilasuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :ilmanvaihto :paaiv :jaatymisenesto] :dp 2}
      {:path [:lahtotiedot :ilmanvaihto :erillispoistot :tulo-poisto]}
      {:path [:lahtotiedot :ilmanvaihto :erillispoistot :sfp] :dp 2}
      {:path [:lahtotiedot :ilmanvaihto :ivjarjestelma :tulo-poisto]}
      {:path [:lahtotiedot :ilmanvaihto :ivjarjestelma :sfp] :dp 2}
      {:path [:lahtotiedot :ilmanvaihto :lto-vuosihyotysuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :lammitys :lammitysmuoto-label-fi]}
      {:path [:lahtotiedot :lammitys :lammitysmuoto-label-sv]}
      {:path [:lahtotiedot :lammitys :lammonjako-label-fi]}
      {:path [:lahtotiedot :lammitys :lammonjako-label-sv]}
      {:path [:lahtotiedot :lammitys :tilat-ja-iv :tuoton-hyotysuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :lammitys :tilat-ja-iv :jaon-hyotysuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :lammitys :tilat-ja-iv :lampokerroin] :dp 1}
      {:path [:lahtotiedot :lammitys :tilat-ja-iv :apulaitteet] :dp 1}
      {:path [:lahtotiedot :lammitys :lammin-kayttovesi :tuoton-hyotysuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :lammitys :lammin-kayttovesi :jaon-hyotysuhde] :dp 0 :percent? true}
      {:path [:lahtotiedot :lammitys :lammin-kayttovesi :lampokerroin] :dp 1}
      {:path [:lahtotiedot :lammitys :lammin-kayttovesi :apulaitteet] :dp 1}
      {:path [:lahtotiedot :lammitys :takka :maara]}
      {:path [:lahtotiedot :lammitys :takka :tuotto] :dp 0}
      {:path [:lahtotiedot :lammitys :ilmalampopumppu :maara]}
      {:path [:lahtotiedot :lammitys :ilmalampopumppu :tuotto] :dp 0}
      {:path [:lahtotiedot :jaahdytysjarjestelma :jaahdytyskauden-painotettu-kylmakerroin] :dp 2}
      {:path [:lahtotiedot :lkvn-kaytto :ominaiskulutus] :dp 0}
      {:path [:lahtotiedot :lkvn-kaytto :lammitysenergian-nettotarve] :dp 0}
      {:f #(-> % sis-kuorma (get 0) first) :dp 0 :percent? true}
      {:f #(-> % sis-kuorma (get 0) second :henkilot) :dp 1}
      {:f #(-> % sis-kuorma (get 0) second :kuluttajalaitteet) :dp 1}
      {:f #(-> % sis-kuorma (get 0) second :valaistus) :dp 1}
      {:f #(-> % sis-kuorma (get 1) first) :dp 0 :percent? true}
      {:f #(-> % sis-kuorma (get 1) second :henkilot) :dp 1}
      {:f #(-> % sis-kuorma (get 1) second :kuluttajalaitteet) :dp 1}
      {:f #(-> % sis-kuorma (get 1) second :valaistus) :dp 1}
      {:f #(-> % sis-kuorma (get 2) first) :dp 0 :percent? true}
      {:f #(-> % sis-kuorma (get 2) second :henkilot) :dp 1}
      {:f #(-> % sis-kuorma (get 2) second :kuluttajalaitteet) :dp 1}
      {:f #(-> % sis-kuorma (get 2) second :valaistus) :dp 1}]
   3 [{:path [:id]}
      {:path [:perustiedot :alakayttotarkoitus-fi]}
      {:path [:perustiedot :alakayttotarkoitus-sv]}
      {:path [:perustiedot :valmistumisvuosi]}
      {:path [:lahtotiedot :lammitetty-nettoala] :dp 1}
      {:path [:tulokset :e-luku]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :sahko-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin]}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :nimi]}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :muotokerroin] :dp 1}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 1 :nimi]}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 1 :ostoenergia] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 1 :muotokerroin] :dp 1}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 1 :ostoenergia-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 1 :ostoenergia-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 2 :nimi]}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 2 :ostoenergia] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 2 :muotokerroin] :dp 1}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 2 :ostoenergia-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :muu 2 :ostoenergia-nettoala-kertoimella] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :summa] :dp 0}
      {:path [:tulokset :kaytettavat-energiamuodot :kertoimella-summa] :dp 0}
      {:path [:tulokset :e-luku] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :aurinkosahko] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :aurinkosahko-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :aurinkolampo] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :aurinkolampo-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :tuulisahko] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :tuulisahko-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :lampopumppu] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :lampopumppu-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :muusahko] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :muusahko-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :muulampo] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat :muulampo-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 0 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 0 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 0 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 0 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 1 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 1 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 1 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 1 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 2 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 2 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 2 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 2 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 3 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 3 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 3 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 3 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 4 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 4 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 4 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 4 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 5 :nimi-fi]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 5 :nimi-sv]}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 5 :vuosikulutus] :dp 0}
      {:path [:tulokset :uusiutuvat-omavaraisenergiat 5 :vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :tekniset-jarjestelmat :tilojen-lammitys :sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :tilojen-lammitys :lampo] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :tuloilman-lammitys :sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :tuloilman-lammitys :lampo] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :kayttoveden-valmistus :sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :kayttoveden-valmistus :lampo] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :iv-sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :jaahdytys :sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :jaahdytys :lampo] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :jaahdytys :kaukojaahdytys] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :kuluttajalaitteet-ja-valaistus-sahko] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :sahko-summa] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :lampo-summa] :dp 1}
      {:path [:tulokset :tekniset-jarjestelmat :kaukojaahdytys-summa] :dp 1}
      {:path [:tulokset :nettotarve :tilojen-lammitys-vuosikulutus] :dp 0}
      {:path [:tulokset :nettotarve :tilojen-lammitys-vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :nettotarve :ilmanvaihdon-lammitys-vuosikulutus] :dp 0}
      {:path [:tulokset :nettotarve :ilmanvaihdon-lammitys-vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :nettotarve :kayttoveden-valmistus-vuosikulutus] :dp 0}
      {:path [:tulokset :nettotarve :kayttoveden-valmistus-vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :nettotarve :jaahdytys-vuosikulutus] :dp 0}
      {:path [:tulokset :nettotarve :jaahdytys-vuosikulutus-nettoala] :dp 0}
      {:path [:tulokset :lampokuormat :aurinko] :dp 0}
      {:path [:tulokset :lampokuormat :aurinko-nettoala] :dp 0}
      {:path [:tulokset :lampokuormat :ihmiset] :dp 0}
      {:path [:tulokset :lampokuormat :ihmiset-nettoala] :dp 0}
      {:path [:tulokset :lampokuormat :kuluttajalaitteet] :dp 0}
      {:path [:tulokset :lampokuormat :kuluttajalaitteet-nettoala] :dp 0}
      {:path [:tulokset :lampokuormat :valaistus] :dp 0}
      {:path [:tulokset :lampokuormat :valaistus-nettoala] :dp 0}
      {:path [:tulokset :lampokuormat :kvesi] :dp 0}
      {:path [:tulokset :lampokuormat :kvesi-nettoala] :dp 0}
      {:path [:tulokset :laskentatyokalu]}]
   4 [{:path [:id]}
      {:path [:lahtotiedot :lammitetty-nettoala] :dp 1}
      {:path [:lahtotiedot :lammitetty-nettoala] :dp 1}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukolampo-vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukolampo-vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kokonaissahko-vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kokonaissahko-vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kiinteistosahko-vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kiinteistosahko-vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kayttajasahko-vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kayttajasahko-vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukojaahdytys-vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukojaahdytys-vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 0 :nimi-fi]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 0 :nimi-sv]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 0 :vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 0 :vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 1 :nimi-fi]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 1 :nimi-sv]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 1 :vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 1 :vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 2 :nimi-fi]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 2 :nimi-sv]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 2 :vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 2 :vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 3 :nimi-fi]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 3 :nimi-sv]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 3 :vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 3 :vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 4 :nimi-fi]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 4 :nimi-sv]}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 4 :vuosikulutus] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 4 :vuosikulutus-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy-kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy-kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu-kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu-kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu-kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu-kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit-kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit-kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :nimi]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :maara-vuodessa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :yksikko]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :muunnoskerroin]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 0 :kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :nimi]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :maara-vuodessa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :yksikko]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :muunnoskerroin]}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :kwh] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu 1 :kwh-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa-nettoala] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :summa] :dp 0}
      {:path [:toteutunut-ostoenergiankulutus :summa-nettoala] :dp 0}]
   5 [{:path [:id]}
      {:path [:huomiot :ymparys :teksti-fi]}
      {:path [:huomiot :ymparys :teksti-sv]}
      {:path [:huomiot :ymparys :toimenpide 0 :nimi-fi]}
      {:path [:huomiot :ymparys :toimenpide 0 :nimi-sv]}
      {:path [:huomiot :ymparys :toimenpide 1 :nimi-fi]}
      {:path [:huomiot :ymparys :toimenpide 1 :nimi-sv]}
      {:path [:huomiot :ymparys :toimenpide 2 :nimi-fi]}
      {:path [:huomiot :ymparys :toimenpide 2 :nimi-sv]}
      {:path [:huomiot :ymparys :toimenpide 0 :lampo] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 0 :sahko] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 0 :jaahdytys] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 0 :eluvun-muutos] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 1 :lampo] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 1 :sahko] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 1 :jaahdytys] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 1 :eluvun-muutos] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 2 :lampo] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 2 :sahko] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 2 :jaahdytys] :dp 0}
      {:path [:huomiot :ymparys :toimenpide 2 :eluvun-muutos] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :teksti-fi]}
      {:path [:huomiot :alapohja-ylapohja :teksti-sv]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :nimi-fi]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :nimi-sv]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :nimi-fi]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :nimi-sv]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :nimi-fi]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :nimi-sv]}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :lampo] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :sahko] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :jaahdytys] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 0 :eluvun-muutos] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :lampo] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :sahko] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :jaahdytys] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 1 :eluvun-muutos] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :lampo] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :sahko] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :jaahdytys] :dp 0}
      {:path [:huomiot :alapohja-ylapohja :toimenpide 2 :eluvun-muutos] :dp 0}
      {:path [:huomiot :lammitys :teksti-fi]}
      {:path [:huomiot :lammitys :teksti-sv]}
      {:path [:huomiot :lammitys :toimenpide 0 :nimi-fi]}
      {:path [:huomiot :lammitys :toimenpide 0 :nimi-sv]}
      {:path [:huomiot :lammitys :toimenpide 1 :nimi-fi]}
      {:path [:huomiot :lammitys :toimenpide 1 :nimi-sv]}
      {:path [:huomiot :lammitys :toimenpide 2 :nimi-fi]}
      {:path [:huomiot :lammitys :toimenpide 2 :nimi-sv]}
      {:path [:huomiot :lammitys :toimenpide 0 :lampo] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 0 :sahko] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 0 :jaahdytys] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 0 :eluvun-muutos] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 1 :lampo] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 1 :sahko] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 1 :jaahdytys] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 1 :eluvun-muutos] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 2 :lampo] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 2 :sahko] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 2 :jaahdytys] :dp 0}
      {:path [:huomiot :lammitys :toimenpide 2 :eluvun-muutos] :dp 0}]
   6 [{:path [:id]}
      {:path [:huomiot :iv-ilmastointi :teksti-fi]}
      {:path [:huomiot :iv-ilmastointi :teksti-sv]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :nimi-fi]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :nimi-sv]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :nimi-fi]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :nimi-sv]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :nimi-fi]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :nimi-sv]}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :lampo] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :sahko] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :jaahdytys] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 0 :eluvun-muutos] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :lampo] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :sahko] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :jaahdytys] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 1 :eluvun-muutos] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :lampo] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :sahko] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :jaahdytys] :dp 0}
      {:path [:huomiot :iv-ilmastointi :toimenpide 2 :eluvun-muutos] :dp 0}
      {:path [:huomiot :valaistus-muut :teksti-fi]}
      {:path [:huomiot :valaistus-muut :teksti-sv]}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :nimi-fi]}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :nimi-sv]}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :nimi-fi]}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :nimi-sv]}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :nimi-fi]}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :nimi-sv]}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :lampo] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :sahko] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :jaahdytys] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 0 :eluvun-muutos] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :lampo] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :sahko] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :jaahdytys] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 1 :eluvun-muutos] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :lampo] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :sahko] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :jaahdytys] :dp 0}
      {:path [:huomiot :valaistus-muut :toimenpide 2 :eluvun-muutos] :dp 0}
      {:path [:huomiot :suositukset-fi]}
      {:path [:huomiot :suositukset-sv]}
      {:path [:huomiot :lisatietoja-fi]}
      {:path [:huomiot :lisatietoja-sv]}]
   7 (concat [{:path [:id]}
              {:path [:lisamerkintoja-fi]}
              {:path [:lisamerkintoja-sv]}
              {:path [:lahtotiedot :rakennusvaippa :lampokapasiteetti] :dp 1}
              {:path [:lahtotiedot :rakennusvaippa :ilmatilavuus] :dp 1}
              {:path [:lahtotiedot :ilmanvaihto :tuloilma-lampotila] :dp 1}
              {:path [:lahtotiedot :lammitys :tilat-ja-iv :lampopumppu-tuotto-osuus] :dp 0 :percent? true}
              {:path [:lahtotiedot :lammitys :lammin-kayttovesi :lampopumppu-tuotto-osuus] :dp 0 :percent? true}
              {:path [:lahtotiedot :lammitys :lampohavio-lammittamaton-tila] :dp 1}
              ;; The repeating item below is just to fill in some
              ;; value, to keep the next items starting at cell
              ;; A11. A10 was expected to be required, but the value
              ;; inserted there turned out to be unnecessary.
              {:path [:lahtotiedot :lammitys :lampohavio-lammittamaton-tila] :dp 1}]
             (flatten (for [i (range 12)]
                        [{:path [:tulokset :kuukausierittely i :tuotto :aurinkosahko] :dp 0}
                         {:path [:tulokset :kuukausierittely i :tuotto :tuulisahko] :dp 0}
                         {:path [:tulokset :kuukausierittely i :tuotto :muusahko] :dp 0}
                         {:path [:tulokset :kuukausierittely i :tuotto :aurinkolampo] :dp 0}
                         {:path [:tulokset :kuukausierittely i :tuotto :lampopumppu] :dp 0}
                         {:path [:tulokset :kuukausierittely i :tuotto :muulampo] :dp 0}
                         {:path [:tulokset :kuukausierittely i :kulutus :sahko] :dp 0}
                         {:path [:tulokset :kuukausierittely i :kulutus :lampo] :dp 0}
                         {:path [:tulokset :kuukausierittely i :hyoty :sahko] :dp 0}
                         {:path [:tulokset :kuukausierittely i :hyoty :lampo] :dp 0}]))
             [{:path [:tulokset :kuukausierittely-summat :tuotto :aurinkosahko] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :tuotto :tuulisahko] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :tuotto :muusahko] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :tuotto :aurinkolampo] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :tuotto :lampopumppu] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :tuotto :muulampo] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :kulutus :sahko] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :kulutus :lampo] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :hyoty :sahko] :dp 0}
              {:path [:tulokset :kuukausierittely-summat :hyoty :lampo] :dp 0}])})

(defn fill-xlsx-template [{:keys [versio] :as complete-energiatodistus} kieli draft?]
  (with-open [is (-> xlsx-template-paths
                     (get-in [versio kieli])
                     io/resource
                     io/input-stream)
              loaded-xlsx (xlsx/load-xlsx is)]
    (let [sheets (map #(xlsx/get-sheet loaded-xlsx %) (range sheet-count))
          path (->> (java.util.UUID/randomUUID)
                    .toString
                    (format "energiatodistus-%s.xlsx")
                    (str tmp-dir))]
      (doseq [[sheet-idx sheet-mappings] mappings]
        (doseq [[row-idx {:keys [path f dp percent?]}] (map-indexed vector sheet-mappings)
                :let [sheet (nth sheets sheet-idx)
                      row (xlsx/get-row sheet row-idx)
                      cell (xlsx/get-cell row 0)
                      v (if path
                          (get-in complete-energiatodistus path)
                          (f complete-energiatodistus))
                      v (cond

                          (number? v)
                          (formats/format-number v dp percent?)

                          (string? v)
                          (if (str/blank? v) " " v)

                          :else
                          (or v " "))]]
          (xlsx/set-cell-value cell v)))
      (xlsx/evaluate-formulas loaded-xlsx)
      (io/make-parents path)
      (xlsx/save-xlsx loaded-xlsx path)
      path)))

;; Uses current Libreoffice export settings. Make sure they are set
;; for PDFA-2B.
(defn xlsx->pdf [path]
  "Uses current LibreOffice export settings. Make sure they are set
   to PDFA-2B. Path must be a path on disk, not on classpath."
  (let [file (io/file path)
        filename (.getName file)
        dir (.getParent file)
        result-pdf (str/replace path #".xlsx$" ".pdf")
        {:keys [exit] :as sh-result} (libreoffice/run-with-args
                                       "--convert-to"
                                       "pdf"
                                       filename
                                       :dir
                                       dir)]
    (if (and (zero? exit) (.exists (io/as-file result-pdf)))
      result-pdf
      (throw (ex-info "XLSX to PDF conversion failed"
                      (assoc sh-result
                        :type :xlsx-pdf-conversion-failure
                        :xlsx filename
                        :pdf-result? (.exists (io/as-file result-pdf))))))))

(defn input-stream->bytes [is]
  (with-open [is is
              xout (java.io.ByteArrayOutputStream.)]
    (io/copy is xout)
    (.toByteArray xout)))

(defn add-image [pdf-path image-path page
                 ^Float x ^Float y
                 ^Float width ^Float height]
  (with-open
    [doc (PDDocument/load (io/file pdf-path))
     contents (PDPageContentStream. doc
                                    (.getPage doc page)
                                    PDPageContentStream$AppendMode/APPEND
                                    true)]
    (let
      [image-bytes (-> image-path io/resource io/input-stream input-stream->bytes)
       image (PDImageXObject/createFromByteArray doc image-bytes image-path)]

      (.drawImage contents ^PDImageXObject image x y width height)
      (.close contents)
      (.save doc pdf-path))))

(def e-luokka-y-coords-2013 (zipmap ["A" "B" "C" "D" "E" "F" "G"] (iterate #(- % 21) 487)))
(def e-luokka-y-coords-2018 (zipmap ["A" "B" "C" "D" "E" "F" "G"] (iterate #(- % 21) 457)))

(defn add-e-luokka-image [pdf-path e-luokka versio]
  (when e-luokka
    (add-image pdf-path
               (format "%d%s.png" versio (str/lower-case e-luokka))
               0
               (case versio 2013 360 2018 392)
               (get (case versio 2013 e-luokka-y-coords-2013 e-luokka-y-coords-2018) e-luokka)
               75
               17.5)))

(defn- make-xmp-metadata
  "Create an XMPMetadata object, which can be rendered as XML into the PDF file.
  This is the 'new' common kind of of PDF metadata"
  [author-name title ^Calendar create-date]

  (let [xmp-metadata (XMPMetadata/createXMPMetadata)]
    ;; The XMP metadata can contain multiple schemas, each with its own properties.
    ;; We need items in three different schemas, so below we are creating
    ;; and filling each of those separately.
    (doto (.createAndAddPFAIdentificationSchema xmp-metadata)
      ;; Mark as PDF/A-2B conformant
      ;; 2 refers to a version of the PDF/A specification. LibreOffice
      ;; produces files that claim to conform to part 2, so we retain
      ;; that mark here.
      ;; B here stands for Basic, which is the lower level of conformance
      (.setPart (Integer/valueOf 2))
      (.setConformance "B"))

    (doto (.createAndAddDublinCoreSchema xmp-metadata)
      ;; Set the title and author
      (.setTitle title)
      (.addCreator author-name))

    (doto (.createAndAddXMPBasicSchema xmp-metadata)
      ;; Set the creation date
      (.setCreateDate create-date))

    xmp-metadata))

(defn- xmp-metadata->bytearray [^XMPMetadata metadata]
  (let [xmp-serializer (XmpSerializer.)
        buf (ByteArrayOutputStream.)]
    (.serialize xmp-serializer metadata buf true)
    (.toByteArray buf)))

(defn- clock->calendar [^Clock clock]
  (-> clock ZonedDateTime/now GregorianCalendar/from))

(defn- set-metadata [^String pdf-path ^String author laatija-allekirjoitus-id ^String title]
  (with-open [document (PDDocument/load (File. pdf-path))]
    (let [creation-date (clock->calendar common-time/clock)
          metadata (PDMetadata. document)
          catalog (.getDocumentCatalog document)
          xmp-metadata-bytes (-> (make-xmp-metadata author title creation-date) xmp-metadata->bytearray)]
      (.importXMPMetadata metadata xmp-metadata-bytes)
      (.setMetadata catalog metadata)

      (doto (.getDocumentInformation document)
        (.setTitle title)
        (.setAuthor author)
        (.setCustomMetadataValue "laatija-allekirjoitus-id", laatija-allekirjoitus-id)
        (.setSubject nil)
        (.setKeywords nil)
        (.setCreator nil)
        (.setProducer nil)
        (.setCreationDate creation-date)
        (.setModificationDate creation-date)))

    (.save document pdf-path)))

;; Set as dynamic so that it can be mocked in tests.
(defn ^:dynamic generate-pdf-as-file [complete-energiatodistus kieli draft? laatija-allekirjoitus-id]
  (let [xlsx-path (fill-xlsx-template complete-energiatodistus kieli draft?)
        pdf-path (xlsx->pdf xlsx-path)]
    (io/delete-file xlsx-path)
    (add-e-luokka-image pdf-path
                        (-> complete-energiatodistus
                            :tulokset
                            :e-luokka)
                        (:versio complete-energiatodistus))

    (set-metadata pdf-path
                  (:laatija-fullname complete-energiatodistus)
                  laatija-allekirjoitus-id
                  (or (get-in complete-energiatodistus [:perustiedot (keyword (str "nimi-" kieli))]) "Energiatodistus"))
    (cond
      draft?
      (watermark-pdf/add-watermark pdf-path (draft-watermark-texts kieli))
      (contains? #{"dev" "test"} config/environment-alias)
      (watermark-pdf/add-watermark pdf-path (test-watermark-texts kieli))
      :else pdf-path)))

(defn generate-pdf-as-input-stream [energiatodistus kieli draft? laatija-allekirjoitus-id]
  (let [pdf-path (generate-pdf-as-file energiatodistus kieli draft? laatija-allekirjoitus-id)
        is (io/input-stream pdf-path)]
    (io/delete-file pdf-path)
    is))

(defn find-existing-pdf [aws-s3-client id kieli]
  (let [file-key (energiatodistus-service/file-key id kieli)]
    (if (file-service/file-exists? aws-s3-client file-key)
      (->> file-key
           (file-service/find-file aws-s3-client)
           io/input-stream)
      (log/warn "requested file" file-key "not found in S3"))))

(defn find-energiatodistus-pdf [db aws-s3-client whoami id kieli]
  (when-let [{:keys [allekirjoitusaika] :as complete-energiatodistus}
             (-> (complete-energiatodistus-service/find-complete-energiatodistus db whoami id)
                 (#(when (or (= (-> % :perustiedot :kieli) 2) ; Accept the todistus if it's multilingual (kieli is 2) or the language code matches
                             (contains? (-> % :perustiedot :kieli energiatodistus-service/language-id->codes set) kieli)
                             (= (-> % :perustiedot :kieli) nil)) %)))] ; Old todistus entries have language set as nil. We'll just have to give it a try
    (if allekirjoitusaika
      (find-existing-pdf aws-s3-client id kieli)
      (generate-pdf-as-input-stream complete-energiatodistus kieli true nil))))

