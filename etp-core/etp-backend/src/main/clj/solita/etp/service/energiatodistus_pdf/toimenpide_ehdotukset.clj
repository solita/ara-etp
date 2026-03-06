(ns solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset
  (:require
    [solita.etp.service.localization :as loc]
    [solita.common.formats :as formats]))

(defn description-list [key-vals]
  (into [:dl]
        (mapv #(vec [:div
                     [:dt (str (:dt %))]
                     [:dd (:dd %)]]) key-vals)))

(defn table-toimenpide-ehdotukset [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:table {:class "table"}
     [:thead
      [:tr
       [:th.empty]
       [:th.header-text [:strong (str (l :te-lampo-muutos))]]
       [:th.header-text [:strong (str (l :te-sahko-muutos))]]
       [:th.header-text [:strong (str (l :te-jaahdytyden-muutos))]]
       [:th.header-text [:strong (str (l :eluvun-muutos))]]
       [:th.header-text [:strong (str (l :kasvihuonepaastojen-muutos))]]]
      [:tr
       [:th.empty]
       [:th.header-unit (str (l :kwh-vuosi))]
       [:th.header-unit (str (l :kwh-vuosi))]
       [:th.header-unit (str (l :kwh-vuosi))]
       [:th.header-unit (str (l :kwhE-m2-vuosi ))]
       [:th.header-unit (str (l :kgCO2ekv/vuosi ))]]]

     [:tbody
      (map
        (fn [{:keys [dt dd]}]
          [:tr
           [:td dt]
           (for [row dd]
             [:td (or row "")])])
        items)]]))


(defn fmt
  "Format number with specified decimal places. Returns empty string for nil."
  [value decimals] (or (formats/format-number value decimals false) ""))
