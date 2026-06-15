(ns solita.etp.service.perusparannuspassi-pdf.lisatietoja
  (:require
    [hiccup.core :refer [h]]
    [solita.etp.service.localization :as loc]))

(defn lisatietoja [{:keys [kieli perusparannuspassi]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:div
     [:div {:class "lisatietoja-box"}
      [:div {:class "lisatietoja-field"} (-> perusparannuspassi
                                              (get-in [:passin-perustiedot (case kieli
                                                                             :fi :lisatietoja-fi
                                                                             :sv :lisatietoja-sv)])
                                              h)]]
     [:div {:class "lisatietoja-box no-top-border" }
      [:div {:class "lisatietoja-info"} (str (l :perusparannuspassi-info))]]]))
