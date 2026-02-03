(ns solita.etp.service.energiatodistus-pdf.lahtotiedot
  "E-luvun laskennan lähtötiedot page for Energiatodistus 2026 PDF")

;; Mock data - to be replaced with real values later
(def mock-data
  {:ilmanvuotoluku "12345"
   :rakennusvaippa [{:nimi "Ulkoseinät" :a "123" :u "123" :osuus "12"}
                    {:nimi "Yläpohja" :a "123" :u "123" :osuus "12"}
                    {:nimi "Alapohja" :a "123" :u "123" :osuus "12"}
                    {:nimi "Ikkunat" :a "123" :u "123" :osuus "12"}
                    {:nimi "Ulko-ovet" :a "123" :u "123" :osuus "12"}
                    {:nimi "Kylmäsillat" :a "" :u "" :osuus "12"}]
   :ilmanvaihto {:kuvaus "Koneellinen tulo ja poisto"
                 :jarjestelmat [{:ilmavirta "0,04" :sfp "1" :lto "78"}]}
   :lammitys {:kuvaus "Kaukolämpö"
              :lammonjako "Vesikiertoinen lattialämmitys"
              :jarjestelmat [{:nimi "Tilojen ja ilmanvaihdon lämmitys"
                              :jaon-hyotysuhde "1"
                              :tuoton-hyotysuhde "1"
                              :lampokerroin "1/1"
                              :apulaitteet "1"}
                             {:nimi "Lämpimän käyttöveden valmistus"
                              :jaon-hyotysuhde "1"
                              :tuoton-hyotysuhde "1"
                              :lampokerroin "1/1"
                              :apulaitteet "1"}]
              :lisalaitteet [{:nimi "Varaava tulisija" :maara "1" :tuotto "1"}
                             {:nimi "Ilmalämpöpumppu" :maara "1" :tuotto "1"}]}
   :jaahdytys {:kylmakerroin "XX"}
   :lampokuormat [{:nimi "Henkilöt" :kayttoaste "1" :lampokuorma "1"}
                  {:nimi "Kuluttajalaitteet" :kayttoaste "1" :lampokuorma "1"}
                  {:nimi "Valaistus" :kayttoaste "1" :lampokuorma "1"}]})

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
   Takes params map but currently uses mock data."
  [_params]
  [:div {:class "lahtotiedot-page"}
   [:h2 {:class "lahtotiedot-page-title"} "E-LUVUN LASKENNAN LÄHTÖTIEDOT"]
   [:div {:class "lahtotiedot-content"}
    (rakennusvaippa-section mock-data)
    (ilmanvaihto-section mock-data)
    (lammitys-section mock-data)
    (jaahdytys-section mock-data)
    (lampokuormat-section mock-data)]])
