(ns solita.etp.service.perusparannuspassi-pdf.lisatietoja
  (:require
    [hiccup.core :refer [h]]
    [solita.etp.service.localization :as loc]))

(defn lisatietoja [{:keys [kieli perusparannuspassi]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:div
     [:h2.lisatietoja-sivu-otsikko (l :lisatietoja-otsikko)]
     [:div.lisatietoja-box
      [:div.lisatietoja-field (-> perusparannuspassi
                                  (get-in [:passin-perustiedot (case kieli
                                                                 :fi :lisatietoja-fi
                                                                 :sv :lisatietoja-sv)])
                                  h)]]
     [:div.lisatietoja-box
      [:div.lisatietoja-info (str (l :perusparannuspassi-info))]]]))
