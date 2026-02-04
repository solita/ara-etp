(ns solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset
  (:require
    [solita.etp.service.localization :as loc]))

(defn description-list [key-vals]
  (into [:dl.toimenpide-ehdotukset]
        (mapv #(vec [:div
                     [:dt (str (:dt %))]
                     [:dd (:dd %)]]) key-vals)))

(defn table-toimenpide-ehdtukset [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:table.toimenpide-ehdotukset
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
       [:th.header-unit (str "kWh/vuosi")]
       [:th.header-unit (str "kWh/vuosi")]
       [:th.header-unit (str "kWh/vuosi")]
       [:th.header-unit (str "kWhE/m2/vuosi")]
       [:th.header-unit (str "kgCO2ekv/vuosi")]]]

     [:tbody
      (map
        (fn [{:keys [dt dd]}]
          [:tr
           [:td dt]
           (for [row dd]
             [:td (or row "")])])
        items)]]))
