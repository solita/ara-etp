(ns solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot
  (:require
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]
    [hiccup.core :refer [h]]))

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
  (let [l (kieli loc/et-pdf-localization)
        kuvaus-kieli (case kieli :fi :label-fi :sv :label-sv)
        laatimisvaihe-id (get-in energiatodistus [:perustiedot :laatimisvaihe])
        laatimisvaihe-kuvaus (some #(when (= (:id %) laatimisvaihe-id) (kuvaus-kieli %)) laatimisvaiheet)]
    [:div {:class "etusivu-yleistiedot"}
     (description-list
       [{:dt (l :rakennuksen-nimi-ja-osoite)
         :dd [:div
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
                                          :sv :postitoimipaikka-sv)])
                  h)]}
        {:dt (l :pysyva-rakennustunnus)
         :dd (-> energiatodistus (get-in [:perustiedot :rakennustunnus]) h)}
        {:dt (l :rakennuksen-valmistumisvuosi)
         :dd (get-in energiatodistus [:perustiedot :valmistumisvuosi])}
        {:dt (l :rakennuksen-kayttotarkoitusluokka)
         :dd (-> energiatodistus
                 (get-in [:perustiedot :kayttotarkoitus])
                 (loc/et-perustiedot-kayttotarkoitus->description alakayttotarkoitukset kieli)
                 h)}
        {:dt (l :energiatodistuksen-tunnus)
         :dd (:id energiatodistus)}
        {:dt (l :energiatodistus-laadittu)
         :dd (h laatimisvaihe-kuvaus)}
        {:dds
         [(str (l :todistuksen-laatimispaiva) ": "
               (-> energiatodistus (get-in [:allekirjoitusaika]) time/format-date))

          (str (l :todistuksen-voimassaolopaiva) ": "
               (-> energiatodistus (get-in [:voimassaolo-paattymisaika]) time/format-date))]}])]))
