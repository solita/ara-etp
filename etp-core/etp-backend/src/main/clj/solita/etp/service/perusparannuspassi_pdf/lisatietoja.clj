(ns solita.etp.service.perusparannuspassi-pdf.lisatietoja
  (:require
    [solita.etp.service.localization :as loc]))

(defn- table-lisatietoja [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    (let [{:keys [dd]} (first items)]
      [:table.lisatietoja-sivu
       [:thead
        [:tr
         [:th.lt-otsikko (l :lisatietoja-otsikko)]]]
       [:tbody
        (map-indexed
          (fn [idx row]
            [:tr
             [:td {:class (when (zero? idx) "lisatietoja-field")}
              (or row "")]])
          dd)]])))

(defn lisatietoja [{:keys [kieli perusparannuspassi]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (table-lisatietoja kieli
      [{:dd [(get-in perusparannuspassi [:passin-perustiedot (case kieli
                                                               :fi :lisatietoja-fi
                                                               :sv :lisatietoja-sv)])
             (str (l :perusparannuspassi-info))]}])))
