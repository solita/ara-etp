(ns solita.etp.service.perusparannuspassi-pdf.lisatietoja
  (:require
    [solita.etp.service.localization :as loc]))

(defn lisatietoja [{:keys [kieli perusparannuspassi]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (println perusparannuspassi)
    [:div
     [:h2.lisatietoja-sivu-otsikko (l :lisatietoja-otsikko)]
     [:div.lisatietoja-box
      [:div.lisatietoja-field (get-in perusparannuspassi [:passin-perustiedot (case kieli
                                                                                :fi :lisatietoja-fi
                                                                                :sv :lisatietoja-sv)])]]
     [:div.lisatietoja-box
      [:div.lisatietoja-info (str (l :perusparannuspassi-info))]]]))

