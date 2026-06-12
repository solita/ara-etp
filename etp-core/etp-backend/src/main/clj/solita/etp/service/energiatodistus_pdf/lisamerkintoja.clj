(ns solita.etp.service.energiatodistus-pdf.lisamerkintoja
  (:require
    [solita.etp.service.localization :as loc]
    [hiccup.core :refer [h]]))

(defn lisamerkintoja [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "lisamerkintoja"}
     [:h2 {:class "top-of-page"} (l :lisamerkintoja-otsikko)]
     [:div {:class "lisamerkintoja-separator"}]
     [:div {:class "lisamerkintoja-teksti"}
      (-> energiatodistus
          (get (case kieli
                 :fi :lisamerkintoja-fi
                 :sv :lisamerkintoja-sv))
          h)]]))

(defn lisatietoja [{:keys [kieli]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "lisamerkintoja"}
     [:h3 (l :lisatietoja-otsikko)]
     [:p (l :lisatietoja-ehto)]
     [:p (l :lisatietoja-energia-tiedot) [:br] "https://www.motiva.fi/motivan-energianeuvonta/"]
     [:p (l :lisatietoja-rahoitus-info) [:br] "www.motiva.fi/rahoituksentietopalvelu"]
     ]))

(defn generate-lisamerkintoja [params]
  (into [:div]
        [(lisamerkintoja params)
         (lisatietoja params)]))
