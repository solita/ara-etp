(ns solita.etp.service.energiatodistus-pdf.tulokset
  "E-luvun laskennan tulokset page for Energiatodistus 2026 PDF"
  (:require [solita.common.formats :as formats]
            [solita.etp.service.localization :as loc]))

(defn- fmt
  "Format number with specified decimal places. Returns empty string for nil."
  ([value] (fmt value 0))
  ([value decimals] (or (formats/format-number value decimals false) "")))

(defn- ostoenergia-row
  [label em prefix]
  (let [ostoenergia       (get em (keyword prefix))
        ostoenergia-netto (get em (keyword (str prefix "-nettoala")))
        kerroin           (get em (keyword (str prefix "-kerroin")))
        kertoimella       (get em (keyword (str prefix "-kertoimella")))
        kertoimella-netto (get em (keyword (str prefix "-nettoala-kertoimella")))]
    [:tr
     [:td {:class "tulokset-label"} label]
     [:td {:class "num"} (fmt ostoenergia)]
     [:td {:class "num"} (fmt ostoenergia-netto)]
     [:td {:class "num"} (fmt kerroin 2)]
     [:td {:class "num"} (fmt kertoimella)]
     [:td {:class "num"} (fmt kertoimella-netto)]]))

(defn- ostoenergia-section
  [energiatodistus l]
  (let [em (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot])]
    [:div {:class "tulokset-section"}
     [:table {:class "tulokset-table"}
      [:thead
       [:tr
        [:th {:class "tulokset-th" :rowspan "2" :style "text-align:left;"} "\u00a0"]
        [:th {:class "tulokset-th" :colspan "2"} (l :tulokset-laskennallinen-ostoenergia)]
        [:th {:class "tulokset-th" :rowspan "2"}
         (l :tulokset-energiamuodon-kerroin)]
        [:th {:class "tulokset-th" :colspan "2"} (l :tulokset-painotettu-energiankulutus)]]
       [:tr
        [:th {:class "tulokset-th-sub"} "kWh/vuosi"]
        [:th {:class "tulokset-th-sub"} "kWh/m²/vuosi"]
        [:th {:class "tulokset-th-sub"} "kWh/vuosi"]
        [:th {:class "tulokset-th-sub"} "kWh/m²/vuosi"]]]
      [:tbody
       (ostoenergia-row (l :kaukolampo-table)             em "kaukolampo")
       (ostoenergia-row (l :sahko-table)                  em "sahko")
       (ostoenergia-row (l :uusiutuva-polttoaine-table)   em "uusiutuva-polttoaine")
       (ostoenergia-row (l :fossiilinen-polttoaine-table) em "fossiilinen-polttoaine")
       (ostoenergia-row (l :kaukojaahdytys-table)         em "kaukojaahdytys")
       [:tr
        [:td {:class "tulokset-label"} (l :tulokset-yhteensa)]
        [:td {:class "num"} "\u00a0"]
        [:td {:class "num"} "\u00a0"]
        [:td {:class "num"} "\u00a0"]
        [:td {:class "num"} [:strong (fmt (:kertoimella-summa em))]]
        [:td {:class "num"} [:strong (fmt (get em :kertoimella-summa-nettoala))]]]]]]))

(defn- uusiutuva-row
  [label omavarais kokonaistuotanto field-key]
  (let [hyodynnetty      (get omavarais field-key)
        hyodynnetty-netto (get omavarais (keyword (str (name field-key) "-nettoala")))
        kokonais          (get kokonaistuotanto field-key)]
    [:tr
     [:td {:class "tulokset-label"} label]
     [:td {:class "num"} (fmt hyodynnetty)]
     [:td {:class "num"} (fmt hyodynnetty-netto)]
     [:td {:class "num"} (fmt kokonais)]]))

(defn- uusiutuva-section
  [energiatodistus l]
  (let [omavarais        (get-in energiatodistus [:tulokset :uusiutuvat-omavaraisenergiat])
        kokonaistuotanto (get-in energiatodistus [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto])]
    [:div {:class "tulokset-section"}
     [:div {:class "tulokset-section-bar"} (l :tulokset-uusiutuva-energia)]
     [:table {:class "tulokset-table"}
      [:thead
       [:tr
        [:th {:class "tulokset-th" :style "text-align:left;"} "\u00a0"]
        [:th {:class "tulokset-th" :colspan "2"} (l :tulokset-hyodynnetty-osuus)]
        [:th {:class "tulokset-th"} (str (l :tulokset-kokonaistuotanto) "\nkWh/vuosi")]]
       [:tr
        [:th {:class "tulokset-th"} "\u00a0"]
        [:th {:class "tulokset-th-sub"} "kWh/vuosi"]
        [:th {:class "tulokset-th-sub"} "kWh/m²/vuosi"]
        [:th {:class "tulokset-th-sub"} "\u00a0"]]]
      [:tbody
       (uusiutuva-row (l :tulokset-aurinkosahko)  omavarais kokonaistuotanto :aurinkosahko)
       (uusiutuva-row (l :tulokset-aurinkolampo)  omavarais kokonaistuotanto :aurinkolampo)
       (uusiutuva-row (l :tulokset-tuulisahko)    omavarais kokonaistuotanto :tuulisahko)
       (uusiutuva-row (l :tulokset-lampopumppu)   omavarais kokonaistuotanto :lampopumppu)
       (uusiutuva-row (l :tulokset-muusahko)      omavarais kokonaistuotanto :muusahko)
       (uusiutuva-row (l :tulokset-muulampo)      omavarais kokonaistuotanto :muulampo)]]]))

(defn- tekniset-cell
  [value]
  [:td {:class "num"} (if (some? value) (fmt value) "")])

(defn- tekniset-subgroup-row
  [label & {:keys [sahko lampo kaukojaahdytys]}]
  [:tr
   [:td {:class "tulokset-label tulokset-subgroup"} label]
   (tekniset-cell sahko)
   (tekniset-cell lampo)
   (tekniset-cell kaukojaahdytys)])

(defn- tekniset-indent-row
  [label & {:keys [sahko lampo kaukojaahdytys]}]
  [:tr
   [:td {:class "tulokset-label tulokset-indent"} label]
   (tekniset-cell sahko)
   (tekniset-cell lampo)
   (tekniset-cell kaukojaahdytys)])

(defn- tekniset-section
  [energiatodistus l]
  (let [tj (get-in energiatodistus [:tulokset :tekniset-jarjestelmat])]
    [:div {:class "tulokset-section"}
     [:div {:class "tulokset-section-bar"} (l :tulokset-tekniset-jarjestelmat)]
     [:table {:class "tulokset-table"}
      [:thead
       [:tr
        [:th {:class "tulokset-th" :style "text-align:left;"} "\u00a0"]
        [:th {:class "tulokset-th"} (str (l :sahko-table) "\nkWh/m²/vuosi")]
        [:th {:class "tulokset-th"} (str (l :tulokset-lampo) "\nkWh/m²/vuosi")]
        [:th {:class "tulokset-th"} (str (l :kaukojaahdytys-table) "\nkWh/m²/vuosi")]]]
      [:tbody
       (tekniset-subgroup-row (l :tulokset-lammitysjarjestelma))
       (tekniset-indent-row (l :tulokset-tilojen-lammitys)
                            :sahko (get-in tj [:tilojen-lammitys :sahko])
                            :lampo (get-in tj [:tilojen-lammitys :lampo]))
       (tekniset-indent-row (l :tulokset-tuloilman-lammitys)
                            :sahko (get-in tj [:tuloilman-lammitys :sahko])
                            :lampo (get-in tj [:tuloilman-lammitys :lampo]))
       (tekniset-indent-row (l :tulokset-kayttoveden-valmistus)
                            :sahko (get-in tj [:kayttoveden-valmistus :sahko])
                            :lampo (get-in tj [:kayttoveden-valmistus :lampo]))
       (tekniset-subgroup-row (l :tulokset-iv-sahko)
                              :sahko (:iv-sahko tj))
       (tekniset-subgroup-row (l :tulokset-jaahdytysjarjestelma)
                              :sahko (get-in tj [:jaahdytys :sahko])
                              :lampo (get-in tj [:jaahdytys :lampo])
                              :kaukojaahdytys (get-in tj [:jaahdytys :kaukojaahdytys]))
       (tekniset-subgroup-row (l :tulokset-kuluttajalaitteet-ja-valaistus)
                              :sahko (:kuluttajalaitteet-ja-valaistus-sahko tj))
       [:tr
        [:td {:class "tulokset-label"} (l :tulokset-yhteensa)]
        [:td {:class "num"} [:strong (fmt (:sahko-summa tj))]]
        [:td {:class "num"} [:strong (fmt (:lampo-summa tj))]]
        [:td {:class "num"} [:strong (fmt (:kaukojaahdytys-summa tj))]]]]]]))

(defn- nettotarve-row [label vuosikulutus nettoala]
  [:tr
   [:td {:class "tulokset-label"} label]
   [:td {:class "num"} (fmt vuosikulutus)]
   [:td {:class "num"} (fmt nettoala)]])

(defn- nettotarve-section
  [energiatodistus l]
  (let [nt (get-in energiatodistus [:tulokset :nettotarve])]
    [:div {:class "tulokset-section"}
     [:div {:class "tulokset-section-bar"} (l :tulokset-nettotarve)]
     [:table {:class "tulokset-table"}
      [:thead
       [:tr
        [:th {:class "tulokset-th" :style "text-align:left;"} "\u00a0"]
        [:th {:class "tulokset-th"} "kWh/vuosi"]
        [:th {:class "tulokset-th"} "kWh/m²/vuosi"]]]
      [:tbody
       (nettotarve-row (l :tulokset-tilojen-lammitys)
                       (:tilojen-lammitys-vuosikulutus nt)
                       (:tilojen-lammitys-vuosikulutus-nettoala nt))
       (nettotarve-row (l :tulokset-ilmanvaihdon-lammitys)
                       (:ilmanvaihdon-lammitys-vuosikulutus nt)
                       (:ilmanvaihdon-lammitys-vuosikulutus-nettoala nt))
       (nettotarve-row (l :tulokset-kayttoveden-valmistus)
                       (:kayttoveden-valmistus-vuosikulutus nt)
                       (:kayttoveden-valmistus-vuosikulutus-nettoala nt))
       (nettotarve-row (l :tulokset-jaahdytys)
                       (:jaahdytys-vuosikulutus nt)
                       (:jaahdytys-vuosikulutus-nettoala nt))]]]))

(defn- lampokuormat-row [label vuosikulutus nettoala]
  [:tr
   [:td {:class "tulokset-label"} label]
   [:td {:class "num"} (fmt vuosikulutus)]
   [:td {:class "num"} (fmt nettoala)]])

(defn- lampokuormat-section
  [energiatodistus l]
  (let [lk (get-in energiatodistus [:tulokset :lampokuormat])]
    [:div {:class "tulokset-section"}
     [:div {:class "tulokset-section-bar"} (l :tulokset-lampokuormat)]
     [:table {:class "tulokset-table"}
      [:thead
       [:tr
        [:th {:class "tulokset-th" :style "text-align:left;"} "\u00a0"]
        [:th {:class "tulokset-th"} "kWh/vuosi"]
        [:th {:class "tulokset-th"} "kWh/m²/vuosi"]]]
      [:tbody
       (lampokuormat-row (l :tulokset-aurinko)
                         (:aurinko lk) (:aurinko-nettoala lk))
       (lampokuormat-row (l :tulokset-henkilot)
                         (:ihmiset lk) (:ihmiset-nettoala lk))
       (lampokuormat-row (l :lahtotiedot-kuluttajalaitteet)
                         (:kuluttajalaitteet lk) (:kuluttajalaitteet-nettoala lk))
       (lampokuormat-row (l :lahtotiedot-valaistus)
                         (:valaistus lk) (:valaistus-nettoala lk))
       (lampokuormat-row (l :tulokset-kvesi-haviot)
                         (:kvesi lk) (:kvesi-nettoala lk))]]]))

(defn- e-luku
  [energiatodistus l]
  (let [e-luku (get-in energiatodistus [:tulokset :e-luku])]
    [:div {:class "tulokset-pill-row"}
     [:div {:class "tulokset-pill"}
      (l :eluku-otsikko)
      [:span {:class "tulokset-pill-value"} (fmt e-luku)]
      "kWh/m²/vuosi"]]))

(defn tulokset-page-content
  [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "tulokset-page"}
     [:h2 {:class "tulokset-page-title"} (l :tulokset-title)]
     (e-luku energiatodistus l)
     [:div {:class "tulokset-content"}
      (ostoenergia-section energiatodistus l)
      (uusiutuva-section energiatodistus l)
      (tekniset-section energiatodistus l)
      (nettotarve-section energiatodistus l)
      (lampokuormat-section energiatodistus l)]]))
