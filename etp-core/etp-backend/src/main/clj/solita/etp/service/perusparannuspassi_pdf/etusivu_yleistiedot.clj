(ns solita.etp.service.perusparannuspassi-pdf.etusivu-yleistiedot
  (:require
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

(defn- description-list [key-vals]
  (into [:dl.etusivu-yleistiedot]
        (mapv #(vec [:div
                     [:dt (str (:dt %) ":")]
                     [:dd (:dd %)]]) key-vals)))

(defn etusivu-yleistiedot [{:keys [energiatodistus perusparannuspassi kieli alakayttotarkoitukset]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (description-list
      [{:dt (l :rakennuksen-nimi)
        :dd (get-in energiatodistus [:perustiedot (case kieli
                                                    :fi :nimi-fi
                                                    :sv :nimi-sv)])}
       {:dt (l :rakennuksen-osoite)
        :dd (get-in energiatodistus [:perustiedot (case kieli
                                                    :fi :katuosoite-fi
                                                    :sv :katuosoite-sv)])}
       {:dt (l :pysyva-rakennustunnus)
        :dd (get-in energiatodistus [:perustiedot :rakennustunnus])}
       {:dt (l :energiatodistuksen-todistustunnus)
        :dd (:id energiatodistus)}
       {:dt (l :perusparannuspassin-tunnus)
        :dd (:id perusparannuspassi)}
       {:dt (l :havainnointikaynnin-paivamaara)
        :dd (-> energiatodistus (get-in [:perustiedot :havainnointikaynti]) time/format-date)}
       {:dt (l :passin-esittelyn-paivamaara)
        :dd (-> perusparannuspassi (get-in [:passin-perustiedot :passin-esittely]) time/format-date)}
       {:dt (l :rakennuksen-kayttotarkoitusluokka)
        :dd (-> energiatodistus (get-in [:perustiedot :kayttotarkoitus]) (loc/et-perustiedot-kayttotarkoitus->description alakayttotarkoitukset kieli))}])))
