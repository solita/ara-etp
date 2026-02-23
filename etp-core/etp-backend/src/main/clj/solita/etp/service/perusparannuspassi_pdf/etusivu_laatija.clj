(ns solita.etp.service.perusparannuspassi-pdf.etusivu-laatija
  (:require
    [hiccup.core :refer [h]]
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

(defn etusivu-laatija [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.etusivu-laatija-allekirjoitus
     [:div
      [:dt (str (l :perusparannuspassin-laatija) ":")]
      [:dd.laatija-nimi (-> energiatodistus :laatija-fullname h)]

      [:dt.hidden-dt (l :allekirjoituspaiva)]
      [:dd (time/format-date (:allekirjoitusaika energiatodistus))]]]))
