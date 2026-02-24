(ns solita.etp.service.energiatodistus-pdf.lisamerkintoja
  (:require
    [solita.etp.service.localization :as loc]
    [hiccup.core :refer [h]]))

(defn description-list [key-vals]
  (into [:dl]
        (mapv #(vec [:div
                     [:dt (str (:dt %) ":")]
                     [:dd (:dd %)]]) key-vals)))


(defn lisamerkintoja [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "lisamerkintoja"}
     [:h1 (l :lisamerkintoja-otsikko)]
     [:div {:class "lisamerkintoja-separator"}]
     (-> energiatodistus
         (get (case kieli
                :fi :lisamerkintoja-fi
                :sv :lisamerkintoja-sv))
         h)]))

(defn lisatietoja [{:keys [kieli]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "lisamerkintoja"}
     [:h2 (l :lisatietoja-otsikko)]
     (description-list
       [{:dt (l :lisatietoja-energiatehokkuus)
         :dd (l :lisatietoja-urlit )}
        {:dt (l :lisatietoja-rahoitus)
         :dd (l :motiva)}
        {:dt (l :lisatietoja-energianeuvonta)
         :dd (l :motiva)}])
     ]))


(defn generate-lisamerkintoja [params]
  (into [:div]
        [(lisamerkintoja params)
         (lisatietoja params)]))
