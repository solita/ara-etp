(ns solita.etp.service.energiatodistus-pdf.lahtotiedot
  "E-luvun laskennan lähtötiedot page for Energiatodistus 2026 PDF"
  (:require [solita.common.formats :as formats]))

(defn- fmt
  "Format number with specified decimal places. Returns empty string for nil."
  ([value] (fmt value 2))
  ([value decimals] (or (formats/format-number value decimals false) "")))

(defn- fmt-pct
  "Format a ratio (0-1) as percentage without decimals."
  [value]
  (or (formats/format-number value 0 true) ""))

(defn- extract-rakennusvaippa-data
  "Extract rakennusvaippa table data from energiatodistus."
  [energiatodistus]
  (let [vaippa (get-in energiatodistus [:lahtotiedot :rakennusvaippa])]
    [{:nimi "Ulkoseinät"
      :a (fmt (get-in vaippa [:ulkoseinat :ala]))
      :u (fmt (get-in vaippa [:ulkoseinat :U]))
      :osuus (fmt-pct (get-in vaippa [:ulkoseinat :osuus-lampohaviosta]))}
     {:nimi "Yläpohja"
      :a (fmt (get-in vaippa [:ylapohja :ala]))
      :u (fmt (get-in vaippa [:ylapohja :U]))
      :osuus (fmt-pct (get-in vaippa [:ylapohja :osuus-lampohaviosta]))}
     {:nimi "Alapohja"
      :a (fmt (get-in vaippa [:alapohja :ala]))
      :u (fmt (get-in vaippa [:alapohja :U]))
      :osuus (fmt-pct (get-in vaippa [:alapohja :osuus-lampohaviosta]))}
     {:nimi "Ikkunat"
      :a (fmt (get-in vaippa [:ikkunat :ala]))
      :u (fmt (get-in vaippa [:ikkunat :U]))
      :osuus (fmt-pct (get-in vaippa [:ikkunat :osuus-lampohaviosta]))}
     {:nimi "Ulko-ovet"
      :a (fmt (get-in vaippa [:ulkoovet :ala]))
      :u (fmt (get-in vaippa [:ulkoovet :U]))
      :osuus (fmt-pct (get-in vaippa [:ulkoovet :osuus-lampohaviosta]))}
     {:nimi "Kylmäsillat"
      :a ""
      :u ""
      :osuus (fmt-pct (get-in vaippa [:kylmasillat-osuus-lampohaviosta]))}]))

(defn- extract-ilmanvaihto-data
  "Extract ilmanvaihto data from energiatodistus."
  [energiatodistus kieli]
  (let [iv (get-in energiatodistus [:lahtotiedot :ilmanvaihto])
        kuvaus-key (if (= kieli :sv) :label-sv :label-fi)
        paaiv (:paaiv iv)]
    {:kuvaus (get iv kuvaus-key)
     :jarjestelmat [{:ilmavirta (fmt (:tulo paaiv))
                     :sfp (fmt (:sfp paaiv))
                     :lto (fmt-pct (:lto-vuosihyotysuhde iv))}]}))

(defn- format-lampokerroin-tuotto-osuus
  "Format lämpökerroin and tuotto-osuus as 'kerroin/osuus'."
  [kerroin osuus]
  (str (fmt kerroin 1)
       "/"
       (fmt-pct osuus)))

(defn- extract-lammitys-data
  "Extract lämmitys data from energiatodistus."
  [energiatodistus kieli]
  (let [lammitys (get-in energiatodistus [:lahtotiedot :lammitys])
        kuvaus-key (if (= kieli :sv) :lammitysmuoto-label-sv :lammitysmuoto-label-fi)
        lammonjako-key (if (= kieli :sv) :lammonjako-label-sv :lammonjako-label-fi)
        tilat-ja-iv (:tilat-ja-iv lammitys)
        lammin-kayttovesi (:lammin-kayttovesi lammitys)
        takka (:takka lammitys)
        ilmalampopumppu (:ilmalampopumppu lammitys)]
    {:kuvaus (get lammitys kuvaus-key)
     :lammonjako (get lammitys lammonjako-key)
     :jarjestelmat [{:nimi "Tilojen ja ilmanvaihdon lämmitys"
                     :jaon-hyotysuhde (fmt (:jaon-hyotysuhde tilat-ja-iv))
                     :tuoton-hyotysuhde (fmt (:tuoton-hyotysuhde tilat-ja-iv))
                     :lampokerroin (format-lampokerroin-tuotto-osuus
                                    (:lampokerroin tilat-ja-iv)
                                    (:lampopumppu-tuotto-osuus tilat-ja-iv))
                     :apulaitteet (fmt (:apulaitteet tilat-ja-iv))}
                    {:nimi "Lämpimän käyttöveden valmistus"
                     :jaon-hyotysuhde (fmt (:jaon-hyotysuhde lammin-kayttovesi))
                     :tuoton-hyotysuhde (fmt (:tuoton-hyotysuhde lammin-kayttovesi))
                     :lampokerroin (format-lampokerroin-tuotto-osuus
                                    (:lampokerroin lammin-kayttovesi)
                                    (:lampopumppu-tuotto-osuus lammin-kayttovesi))
                     :apulaitteet (fmt (:apulaitteet lammin-kayttovesi))}]
     :lisalaitteet [{:nimi "Varaava tulisija"
                     :maara (str (:maara takka))
                     :tuotto (fmt (:tuotto takka) 0)}
                    {:nimi "Ilmalämpöpumppu"
                     :maara (str (:maara ilmalampopumppu))
                     :tuotto (fmt (:tuotto ilmalampopumppu) 0)}]}))

(defn- extract-jaahdytys-data
  "Extract jäähdytys data from energiatodistus."
  [energiatodistus]
  {:kylmakerroin (fmt
                   (get-in energiatodistus
                           [:lahtotiedot :jaahdytysjarjestelma :jaahdytyskauden-painotettu-kylmakerroin]))})

(defn- extract-lampokuormat-data
  "Extract sisäiset lämpökuormat data from energiatodistus."
  [energiatodistus]
  (let [sis-kuorma (get-in energiatodistus [:lahtotiedot :sis-kuorma])]
    [{:nimi "Henkilöt"
      :kayttoaste (fmt (get-in sis-kuorma [:henkilot :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:henkilot :lampokuorma]))}
     {:nimi "Kuluttajalaitteet"
      :kayttoaste (fmt (get-in sis-kuorma [:kuluttajalaitteet :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:kuluttajalaitteet :lampokuorma]))}
     {:nimi "Valaistus"
      :kayttoaste (fmt (get-in sis-kuorma [:valaistus :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:valaistus :lampokuorma]))}]))

(defn- extract-lahtotiedot
  "Extract all lähtötiedot from energiatodistus for PDF display."
  [energiatodistus kieli]
  {:ilmanvuotoluku (fmt
                     (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ilmanvuotoluku]))
   :rakennusvaippa (extract-rakennusvaippa-data energiatodistus)
   :ilmanvaihto (extract-ilmanvaihto-data energiatodistus kieli)
   :lammitys (extract-lammitys-data energiatodistus kieli)
   :jaahdytys (extract-jaahdytys-data energiatodistus)
   :lampokuormat (extract-lampokuormat-data energiatodistus)})

(defn- section-title [title]
  [:h3 {:class "lahtotiedot-section-title"} title])

(defn- label-value-row [label value & [unit]]
  [:div {:class "lahtotiedot-row"}
   [:div {:class "lahtotiedot-label"} label]
   [:div {:class "lahtotiedot-value"} value]
   (when unit
     [:div {:class "lahtotiedot-unit"} unit])])

(defn- table-header-cell [content & [subtext]]
  [:th {:class "lahtotiedot-th"}
   content
   (when subtext
     [:span {:class "lahtotiedot-th-sub"} subtext])])

(defn- rakennusvaippa-section [data]
  [:section {:class "lahtotiedot-section"}
   (section-title "Rakennusvaippa")
   (label-value-row
     [:span "Ilmanvuotoluku q" [:sub "50"]]
     (:ilmanvuotoluku data)
     "m³/(h m²)")
   [:table {:class "lahtotiedot-table"}
    [:thead
     [:tr
      [:th {:class "lahtotiedot-th"}]
      (table-header-cell [:span "A" [:br] [:span {:class "lahtotiedot-th-sub"} "m²"]])
      (table-header-cell [:span "U" [:br] [:span {:class "lahtotiedot-th-sub"} "W/(m²K)"]])
      (table-header-cell [:span "Osuus" [:br] [:span {:class "lahtotiedot-th-sub"} "lämpöhäviöistä"]])]]
    [:tbody
     (for [row (:rakennusvaippa data)]
       [:tr
        [:td [:strong (:nimi row)]]
        [:td {:class "num"} (:a row)]
        [:td {:class "num"} (:u row)]
        [:td {:class "num"} (:osuus row)]])]]])

(defn- ilmanvaihto-section [data]
  (let [iv (:ilmanvaihto data)]
    [:section {:class "lahtotiedot-section"}
     (section-title "Ilmanvaihtojärjestelmä")
     (label-value-row "Ilmanvaihtojärjestelmän kuvaus:" (:kuvaus iv))
     [:table {:class "lahtotiedot-table"}
      [:thead
       [:tr
        [:th {:class "lahtotiedot-th"}]
        (table-header-cell [:span "Ilmavirta" [:br] [:span {:class "lahtotiedot-th-sub"} "m³/s"]])
        (table-header-cell [:span "SFP-luku" [:br] [:span {:class "lahtotiedot-th-sub"} "kW/(m³/s)"]])
        [:th {:class "lahtotiedot-th"} "LTO:n vuosihyötysuhde"]]]
      [:tbody
       (for [row (:jarjestelmat iv)]
         [:tr
          [:td [:strong "Ilmanvaihtojärjestelmä"]]
          [:td {:class "num"} (:ilmavirta row)]
          [:td {:class "num"} (:sfp row)]
          [:td {:class "num"} (:lto row)]])]]]))

(defn- lammitys-section [data]
  (let [lam (:lammitys data)]
    [:section {:class "lahtotiedot-section"}
     (section-title "Lämmitysjärjestelmä")
     [:div {:class "lahtotiedot-two-col"}
      (label-value-row "Lämmitysjärjestelmä kuvaus:" (:kuvaus lam))
      (label-value-row "Lämmönjako:" (:lammonjako lam))]
     [:table {:class "lahtotiedot-table"}
      [:thead
       [:tr
        [:th {:class "lahtotiedot-th"}]
        [:th {:class "lahtotiedot-th"} "Jaon ja luovutuksen hyötysuhde"]
        [:th {:class "lahtotiedot-th"} "Tuoton hyötysuhde"]
        [:th {:class "lahtotiedot-th"} "Lämpökerroin/ Tuotto-osuus"]
        (table-header-cell [:span "Apulaitteiden sähkönkäyttö" [:br] [:span {:class "lahtotiedot-th-sub"} "kWh/m²/vuosi"]])]]
      [:tbody
       (for [row (:jarjestelmat lam)]
         [:tr
          [:td [:strong (:nimi row)]]
          [:td {:class "num"} (:jaon-hyotysuhde row)]
          [:td {:class "num"} (:tuoton-hyotysuhde row)]
          [:td {:class "num"} (:lampokerroin row)]
          [:td {:class "num"} (:apulaitteet row)]])]]
     [:table {:class "lahtotiedot-table lahtotiedot-table-margin"}
      [:thead
       [:tr
        [:th {:class "lahtotiedot-th"}]
        (table-header-cell [:span "Määrä" [:br] [:span {:class "lahtotiedot-th-sub"} "kpl"]])
        (table-header-cell [:span "Tuotto" [:br] [:span {:class "lahtotiedot-th-sub"} "kWh/vuosi"]])]]
      [:tbody
       (for [row (:lisalaitteet lam)]
         [:tr
          [:td [:strong (:nimi row)]]
          [:td {:class "num"} (:maara row)]
          [:td {:class "num"} (:tuotto row)]])]]]))

(defn- jaahdytys-section [data]
  [:section {:class "lahtotiedot-section"}
   (section-title "Jäähdytysjärjestelmä")
   (label-value-row "Jäähdytyskauden painotettu kylmäkerroin" (get-in data [:jaahdytys :kylmakerroin]))])

(defn- lampokuormat-section [data]
  [:section {:class "lahtotiedot-section"}
   (section-title "Sisäiset lämpökuormat eri käyttöasteilla")
   [:table {:class "lahtotiedot-table"}
    [:thead
     [:tr
      [:th {:class "lahtotiedot-th"}]
      [:th {:class "lahtotiedot-th"} "Käyttöaste"]
      (table-header-cell [:span "Lämpökuorma" [:br] [:span {:class "lahtotiedot-th-sub"} "W/m²"]])]]
    [:tbody
     (for [row (:lampokuormat data)]
       [:tr
        [:td [:strong (:nimi row)]]
        [:td {:class "num"} (:kayttoaste row)]
        [:td {:class "num"} (:lampokuorma row)]])]]])

(defn lahtotiedot-page-content
  "Generate the content for the E-luvun laskennan lähtötiedot page.
   Extracts data from the energiatodistus in params."
  [{:keys [energiatodistus kieli]}]
  (let [data (extract-lahtotiedot energiatodistus kieli)]
    [:div {:class "lahtotiedot-page"}
     [:h2 {:class "lahtotiedot-page-title"} "E-LUVUN LASKENNAN LÄHTÖTIEDOT"]
     [:div {:class "lahtotiedot-content"}
      (rakennusvaippa-section data)
      (ilmanvaihto-section data)
      (lammitys-section data)
      (jaahdytys-section data)
      (lampokuormat-section data)]]))
