(ns solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot
  (:require
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

(defn- description-list [key-vals]
  (into [:dl]
        (mapv
          (fn [{:keys [dt dd dds]}]
            (into
              [:div]
              (cond
                dt  [[:dt (str dt ":")] [:dd dd]]
                dds (mapv (fn [dd] [:dd dd]) dds))))
          key-vals)))

(defn et-etusivu-yleistiedot [{:keys [energiatodistus kieli alakayttotarkoitukset laatimisvaiheet]}]
  (let [l  (kieli loc/et-pdf-localization)
        kuvaus-kieli (case kieli :fi :label-fi :sv :label-sv)
        laatimisvaihe-id (get-in energiatodistus [:perustiedot :laatimisvaihe])
        laatimisvaihe-kuvaus (some #(when (= (:id %) laatimisvaihe-id) (kuvaus-kieli %)) laatimisvaiheet)]
    [:div {:class "etusivu-yleistiedot"}
     (description-list
       [{:dt (l :rakennuksen-nimi-ja-osoite)
        :dd [:div
             (get-in energiatodistus [:perustiedot (case kieli
                                                     :fi :nimi-fi
                                                     :sv :nimi-sv)])
              [:br]
             (get-in energiatodistus [:perustiedot (case kieli
                                                     :fi :katuosoite-fi
                                                     :sv :katuosoite-sv)])
             [:br]
             (get-in energiatodistus [:perustiedot :postinumero])
             " "
             (get-in energiatodistus [:perustiedot (case kieli
                                                     :fi :postitoimipaikka-fi
                                                     :sv :postitoimipaikka-sv)])]}
       {:dt (l :pysyva-rakennustunnus)
        :dd (get-in energiatodistus [:perustiedot :rakennustunnus])}
       {:dt (l :rakennuksen-valmistumisvuosi)
        :dd (get-in energiatodistus [:perustiedot :valmistumisvuosi])}
       {:dt (l :rakennuksen-kayttotarkoitusluokka)
        :dd (-> energiatodistus (get-in [:perustiedot :kayttotarkoitus]) (loc/et-perustiedot-kayttotarkoitus->description alakayttotarkoitukset kieli))}
       {:dt (l :energiatodistuksen-tunnus)
        :dd (:id energiatodistus)}
       {:dt (l :energiatodistus-laadittu)
        :dd laatimisvaihe-kuvaus}
       {:dds
        [(str (l :todistuksen-laatimispaiva) ": "
              (-> energiatodistus (get-in [:allekirjoitusaika]) time/format-date))

         (str (l :todistuksen-voimassaolopaiva) ": "
              (-> energiatodistus (get-in [:voimassaolo-paattymisaika]) time/format-date))]}])]))
