(ns solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot
  (:require
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]
    [hiccup.core :refer [h]]))

(defn et-etusivu-yleistiedot [{:keys [energiatodistus kieli alakayttotarkoitukset laatimisvaiheet]}]
  (let [l (kieli loc/et-pdf-localization)
        kuvaus-kieli (case kieli :fi :label-fi :sv :label-sv)
        laatimisvaihe-id (get-in energiatodistus [:perustiedot :laatimisvaihe])
        laatimisvaihe-kuvaus (some #(when (= (:id %) laatimisvaihe-id) (kuvaus-kieli %)) laatimisvaiheet)]
    [:div {:class "etusivu-yleistiedot"}
     [:dl {:class "table-description-list"}
      [:div
       [:dt (str (l :rakennuksen-nimi-ja-osoite) ":")]
       [:dd [:div
             (-> energiatodistus
                 (get-in [:perustiedot (case kieli
                                         :fi :nimi-fi
                                         :sv :nimi-sv)])
                 h)
             [:br]
             (-> energiatodistus
                 (get-in [:perustiedot (case kieli
                                         :fi :katuosoite-fi
                                         :sv :katuosoite-sv)])
                 h)
             [:br]
             (-> energiatodistus (get-in [:perustiedot :postinumero]) h)
             " "
             (-> energiatodistus
                 (get-in [:perustiedot (case kieli
                                         :fi :postitoimipaikka-fi
                                         :sv :poositoimipaikka-sv)])
                 h)]]]
      [:div
       [:dt (str (l :pysyva-rakennustunnus) ":")]
       [:dd (-> energiatodistus (get-in [:perustiedot :rakennustunnus]) h)]]
      [:div
       [:dt (str (l :rakennuksen-valmistumisvuosi) ":")]
       [:dd (get-in energiatodistus [:perustiedot :valmistumisvuosi])]]
      [:div
       [:dt (str (l :rakennuksen-kayttotarkoitusluokka) ":")]
       [:dd (-> energiatodistus
                (get-in [:perustiedot :kayttotarkoitus])
                (loc/et-perustiedot-kayttotarkoitus->description alakayttotarkoitukset kieli)
                h)]]
      [:div
       [:dt (str (l :energiatodistuksen-tunnus) ":")]
       [:dd (:id energiatodistus)]]
      [:div
       [:dt (str (l :energiatodistus-laadittu) ":")]
       [:dd (h laatimisvaihe-kuvaus)]]
      [:div {:class "etusivu-yleistiedot-last-row"}
       [:div
        [:dt (str (l :todistuksen-laatimispaiva) ": ")]
        [:dd (str (-> energiatodistus (get-in [:allekirjoitusaika]) time/format-date))]]
       [:div
        [:dt (str (l :todistuksen-voimassaolopaiva) ": ")]
        [:dd (str (-> energiatodistus (get-in [:voimassaolo-paattymisaika]) time/format-date))]]]]]))
