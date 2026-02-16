(ns solita.etp.service.energiatodistus-pdf.lahtotiedot
  "E-luvun laskennan lähtötiedot page for Energiatodistus 2026 PDF"
  (:require [solita.common.formats :as formats]
            [solita.etp.service.localization :as loc]))

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
  [energiatodistus l]
  (let [vaippa (get-in energiatodistus [:lahtotiedot :rakennusvaippa])]
    [{:nimi (l :lahtotiedot-ulkoseinat)
      :a (fmt (get-in vaippa [:ulkoseinat :ala]))
      :u (fmt (get-in vaippa [:ulkoseinat :U]))
      :osuus (fmt-pct (get-in vaippa [:ulkoseinat :osuus-lampohaviosta]))}
     {:nimi (l :lahtotiedot-ylapohja)
      :a (fmt (get-in vaippa [:ylapohja :ala]))
      :u (fmt (get-in vaippa [:ylapohja :U]))
      :osuus (fmt-pct (get-in vaippa [:ylapohja :osuus-lampohaviosta]))}
     {:nimi (l :lahtotiedot-alapohja)
      :a (fmt (get-in vaippa [:alapohja :ala]))
      :u (fmt (get-in vaippa [:alapohja :U]))
      :osuus (fmt-pct (get-in vaippa [:alapohja :osuus-lampohaviosta]))}
     {:nimi (l :lahtotiedot-ikkunat)
      :a (fmt (get-in vaippa [:ikkunat :ala]))
      :u (fmt (get-in vaippa [:ikkunat :U]))
      :osuus (fmt-pct (get-in vaippa [:ikkunat :osuus-lampohaviosta]))}
     {:nimi (l :lahtotiedot-ulkoovet)
      :a (fmt (get-in vaippa [:ulkoovet :ala]))
      :u (fmt (get-in vaippa [:ulkoovet :U]))
      :osuus (fmt-pct (get-in vaippa [:ulkoovet :osuus-lampohaviosta]))}
     {:nimi (l :lahtotiedot-kylmasillat)
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
  [energiatodistus kieli l]
  (let [lammitys (get-in energiatodistus [:lahtotiedot :lammitys])
        kuvaus-key (if (= kieli :sv) :lammitysmuoto-label-sv :lammitysmuoto-label-fi)
        lammonjako-key (if (= kieli :sv) :lammonjako-label-sv :lammonjako-label-fi)
        tilat-ja-iv (:tilat-ja-iv lammitys)
        lammin-kayttovesi (:lammin-kayttovesi lammitys)
        takka (:takka lammitys)
        ilmalampopumppu (:ilmalampopumppu lammitys)]
    {:kuvaus (get lammitys kuvaus-key)
     :lammonjako (get lammitys lammonjako-key)
     :jarjestelmat [{:nimi (l :lahtotiedot-tilojen-iv-lammitys)
                     :jaon-hyotysuhde (fmt (:jaon-hyotysuhde tilat-ja-iv))
                     :tuoton-hyotysuhde (fmt (:tuoton-hyotysuhde tilat-ja-iv))
                     :lampokerroin (format-lampokerroin-tuotto-osuus
                                    (:lampokerroin tilat-ja-iv)
                                    (:lampopumppu-tuotto-osuus tilat-ja-iv))
                     :apulaitteet (fmt (:apulaitteet tilat-ja-iv))}
                    {:nimi (l :lahtotiedot-lampiman-kayttoveden-valmistus)
                     :jaon-hyotysuhde (fmt (:jaon-hyotysuhde lammin-kayttovesi))
                     :tuoton-hyotysuhde (fmt (:tuoton-hyotysuhde lammin-kayttovesi))
                     :lampokerroin (format-lampokerroin-tuotto-osuus
                                    (:lampokerroin lammin-kayttovesi)
                                    (:lampopumppu-tuotto-osuus lammin-kayttovesi))
                     :apulaitteet (fmt (:apulaitteet lammin-kayttovesi))}]
     :lisalaitteet [{:nimi (l :lahtotiedot-varaava-tulisija)
                     :maara (str (:maara takka))
                     :tuotto (fmt (:tuotto takka) 0)}
                    {:nimi (l :lahtotiedot-ilmalampopumppu)
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
  [energiatodistus l]
  (let [sis-kuorma (get-in energiatodistus [:lahtotiedot :sis-kuorma])]
    [{:nimi (l :lahtotiedot-henkilot)
      :kayttoaste (fmt (get-in sis-kuorma [:henkilot :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:henkilot :lampokuorma]))}
     {:nimi (l :lahtotiedot-kuluttajalaitteet)
      :kayttoaste (fmt (get-in sis-kuorma [:kuluttajalaitteet :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:kuluttajalaitteet :lampokuorma]))}
     {:nimi (l :lahtotiedot-valaistus)
      :kayttoaste (fmt (get-in sis-kuorma [:valaistus :kayttoaste]))
      :lampokuorma (fmt (get-in sis-kuorma [:valaistus :lampokuorma]))}]))

(defn- extract-lahtotiedot
  "Extract all lähtötiedot from energiatodistus for PDF display."
  [energiatodistus kieli l]
  {:ilmanvuotoluku (fmt
                     (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ilmanvuotoluku]))
   :rakennusvaippa (extract-rakennusvaippa-data energiatodistus l)
   :ilmanvaihto (extract-ilmanvaihto-data energiatodistus kieli)
   :lammitys (extract-lammitys-data energiatodistus kieli l)
   :jaahdytys (extract-jaahdytys-data energiatodistus)
   :lampokuormat (extract-lampokuormat-data energiatodistus l)})

(defn- section-title [title]
  [:h3 title])

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

(defn- rakennusvaippa-section [data l]
  [:section {:class "lahtotiedot-section"}
   (section-title (l :lahtotiedot-rakennusvaippa))
   (label-value-row
     [:span (l :lahtotiedot-ilmanvuotoluku) [:sub "50"]]
     (:ilmanvuotoluku data)
     "m³/(h m²)")
   [:table {:class "lahtotiedot-table"}
    [:thead
     [:tr
      [:th {:class "lahtotiedot-th"}]
      (table-header-cell [:span "A" [:br] [:span {:class "lahtotiedot-th-sub"} "m²"]])
      (table-header-cell [:span "U" [:br] [:span {:class "lahtotiedot-th-sub"} "W/(m²K)"]])
      (table-header-cell [:span (l :lahtotiedot-osuus-lampohavioista) [:br] [:span {:class "lahtotiedot-th-sub"} ""]])]]
    [:tbody
     (for [row (:rakennusvaippa data)]
       [:tr
        [:td [:strong (:nimi row)]]
        [:td {:class "num"} (:a row)]
        [:td {:class "num"} (:u row)]
        [:td {:class "num"} (:osuus row)]])]]])

(defn- ilmanvaihto-section [data l]
  (let [iv (:ilmanvaihto data)]
    [:section {:class "lahtotiedot-section"}
     (section-title (l :lahtotiedot-ilmanvaihtojarjestelma))
     (label-value-row (l :lahtotiedot-ilmanvaihto-kuvaus) (:kuvaus iv))
     [:table {:class "lahtotiedot-table"}
      [:thead
       [:tr
        [:th {:class "lahtotiedot-th"}]
        (table-header-cell [:span (l :lahtotiedot-ilmavirta) [:br] [:span {:class "lahtotiedot-th-sub"} "m³/s"]])
        (table-header-cell [:span (l :lahtotiedot-sfp-luku) [:br] [:span {:class "lahtotiedot-th-sub"} "kW/(m³/s)"]])
        [:th {:class "lahtotiedot-th"} (l :lahtotiedot-lto-vuosihyotysuhde)]]]
      [:tbody
       (for [row (:jarjestelmat iv)]
         [:tr
          [:td [:strong (l :lahtotiedot-ilmanvaihtojarjestelma)]]
          [:td {:class "num"} (:ilmavirta row)]
          [:td {:class "num"} (:sfp row)]
          [:td {:class "num"} (:lto row)]])]]]))

(defn- lammitys-section [data l]
  (let [lam (:lammitys data)]
    [:section {:class "lahtotiedot-section"}
     (section-title (l :lahtotiedot-lammitysjarjestelma))
     [:div {:class "lahtotiedot-two-col"}
      (label-value-row (l :lahtotiedot-lammitys-kuvaus) (:kuvaus lam))
      (label-value-row (l :lahtotiedot-lammonjako) (:lammonjako lam))]
     [:table {:class "lahtotiedot-table"}
      [:thead
       [:tr
        [:th {:class "lahtotiedot-th"}]
        [:th {:class "lahtotiedot-th"} (l :lahtotiedot-jaon-luovutuksen-hyotysuhde)]
        [:th {:class "lahtotiedot-th"} (l :lahtotiedot-tuoton-hyotysuhde)]
        [:th {:class "lahtotiedot-th"} (l :lahtotiedot-lampokerroin-tuotto-osuus)]
        (table-header-cell [:span (l :lahtotiedot-apulaitteiden-sahkonkaytto) [:br] [:span {:class "lahtotiedot-th-sub"} "kWh/m²/vuosi"]])]]
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
        (table-header-cell [:span (l :lahtotiedot-maara) [:br] [:span {:class "lahtotiedot-th-sub"} "kpl"]])
        (table-header-cell [:span (l :lahtotiedot-tuotto) [:br] [:span {:class "lahtotiedot-th-sub"} "kWh/vuosi"]])]]
      [:tbody
       (for [row (:lisalaitteet lam)]
         [:tr
          [:td [:strong (:nimi row)]]
          [:td {:class "num"} (:maara row)]
          [:td {:class "num"} (:tuotto row)]])]]]))

(defn- jaahdytys-section [data l]
  [:section {:class "lahtotiedot-section"}
   (section-title (l :lahtotiedot-jaahdytysjarjestelma))
   (label-value-row (l :lahtotiedot-kylmakerroin) (get-in data [:jaahdytys :kylmakerroin]))])

(defn- lampokuormat-section [data l]
  [:section {:class "lahtotiedot-section"}
   (section-title (l :lahtotiedot-sisaiset-lampokuormat))
   [:table {:class "lahtotiedot-table"}
    [:thead
     [:tr
      [:th {:class "lahtotiedot-th"}]
      [:th {:class "lahtotiedot-th"} (l :lahtotiedot-kayttoaste)]
      (table-header-cell [:span (l :lahtotiedot-lampokuorma) [:br] [:span {:class "lahtotiedot-th-sub"} "W/m²"]])]]
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
  (let [l (kieli loc/et-pdf-localization)
        data (extract-lahtotiedot energiatodistus kieli l)]
    [:div {:class "lahtotiedot-page"}
     [:h2 (l :lahtotiedot-title)]
     [:div {:class "lahtotiedot-content"}
      (rakennusvaippa-section data l)
      (ilmanvaihto-section data l)
      (lammitys-section data l)
      (jaahdytys-section data l)
      (lampokuormat-section data l)]]))
