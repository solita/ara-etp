(ns solita.etp.service.perusparannuspassi-pdf.etusivu-yleistiedot
  (:require
    [hiccup.core :refer [h]]
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

;; We use dl instead of table so that screen reader reads the data more clearly.
(defn etusivu-yleistiedot
  [{:keys [energiatodistus perusparannuspassi kieli alakayttotarkoitukset]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl {:class "etusivu-yleistiedot"}
     [:div
      [:dt (str (l :rakennuksen-nimi) ":")]
      [:dd (-> energiatodistus
               (get-in [:perustiedot (case kieli
                                       :fi :nimi-fi
                                       :sv :nimi-sv)])
               h)]]
     [:div
      [:dt (str (l :rakennuksen-osoite) ":")]
      [:dd (-> energiatodistus
               (get-in [:perustiedot (case kieli
                                       :fi :katuosoite-fi
                                       :sv :katuosoite-sv)])
               h)]]
     [:div
      [:dt (str (l :pysyva-rakennustunnus) ":")]
      [:dd (-> energiatodistus (get-in [:perustiedot :rakennustunnus]) h)]]
     [:div
      [:dt (str (l :energiatodistuksen-todistustunnus) ":")]
      [:dd (:id energiatodistus)]]
     [:div
      [:dt (str (l :perusparannuspassin-tunnus) ":")]
      [:dd (:id perusparannuspassi)]]
     [:div
      [:dt (str (l :havainnointikaynnin-paivamaara) ":")]
      [:dd (-> energiatodistus (get-in [:perustiedot :havainnointikaynti]) time/format-date)]]
     [:div
      [:dt (str (l :passin-esittelyn-paivamaara) ":")]
      [:dd (-> perusparannuspassi (get-in [:passin-perustiedot :passin-esittely]) time/format-date)]]
     [:div {:class "potentially-two-rows"}
      [:dt (str (l :rakennuksen-kayttotarkoitusluokka) ":")]
      [:dd (-> energiatodistus
               (get-in [:perustiedot :kayttotarkoitus])
               (loc/et-perustiedot-kayttotarkoitus->description alakayttotarkoitukset kieli)
               h)]]]))
