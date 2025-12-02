(ns solita.etp.service.perusparannuspassi-pdf.etusivu-laatija
  (:require
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

(defn etusivu-laatija [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.etusivu-laatija-allekirjoitus
     [:div
      [:dt (str (l :perusparannuspassin-laatija) ":")]
      [:dd.laatija-nimi (:laatija-fullname energiatodistus)]

      [:dt.hidden-dt (l :allekirjoituspaiva)]
      [:dd (time/format-date (:allekirjoitusaika energiatodistus))]]]))
