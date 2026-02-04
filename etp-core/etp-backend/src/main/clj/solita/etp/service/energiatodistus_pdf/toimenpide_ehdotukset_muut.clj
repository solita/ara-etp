(ns solita.etp.service.energiatodistus-pdf.toimenpide-ehdotukset-muut
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset :as et]))

(defn toimenpide-ehdotukset-muut [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h1 (l :te-muut-otsikko)]
     (str (l :te-muut-teksti))
     [:h2 (l :te-muut-valaistus-otsikko  )]
     (get-in energiatodistus [:huomiot :valaistus-muut (case kieli
                                                   :fi :teksti-fi
                                                   :sv :teksti-sv)])]))

(defn toimenpide-ehdotukset-list-muut [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        toimenpide (get-in energiatodistus [:huomiot :valaistus-muut :toimenpide] [])
        nimi-key (case kieli :fi :nimi-fi :sv :nimi-sv)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h3 (l :te-toimenpide-muutokset)]
     (et/description-list
       (map-indexed (fn [idx item]
                      {:dt (str (inc idx) ".")
                       :dd (get item nimi-key "")})
                    toimenpide))]))


(defn toimenpide-ehdotukset-table-muut [{:keys [kieli energiatodistus]}]
  (let [toimenpide (get-in energiatodistus [:huomiot :valaistus-muut :toimenpide] [])]
    [:div {:class "toimenpide-ehdotukset"}
     (et/table-toimenpide-ehdotukset kieli
                                     (map-indexed (fn [idx item]
                                                   {:dt (str (inc idx) ".")
                                                    :dd [(:lampo item)
                                                         (:sahko item)
                                                         (:jaahdytys item)
                                                         (:eluvun-muutos item)
                                                         (str "todo")]})
                                                 toimenpide))]))

(defn toimenpide-ehdotukset-suositukset [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h2 (l :te-suositukset-otsikko)]
     (get-in energiatodistus [:huomiot  (case kieli
                                          :fi :suositukset-fi
                                          :sv :suositukset-sv)])]))

(defn generate-all-toimepide-ehdotukset-muut [params]
  (into [:div]
        [(toimenpide-ehdotukset-muut params)
         (toimenpide-ehdotukset-list-muut params)
         (toimenpide-ehdotukset-table-muut params)
         (toimenpide-ehdotukset-suositukset params)]))
