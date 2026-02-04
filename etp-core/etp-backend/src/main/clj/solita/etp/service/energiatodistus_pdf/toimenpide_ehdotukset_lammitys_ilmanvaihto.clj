(ns solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset_lammitys_ilmanvaihto
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset :as et]))


(defn- description-list [key-vals]
  (into [:div]
        (mapv #(vec [:div {:class "row"}
                     [:span {:class "label"} (str (:dt %) ":")]
                     [:span {:class "value"} (:dd %)]]) key-vals)))


(defn toimenpide-ehdotukset-lammitys [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h1 (l :te-lammitys-ilmanvaihto-otsikko)]
     (str (l :lammitys-ilmanvaihto-teksti))
     [:h2 (l :huomiot-lammitys-otsikko)]
     (get-in energiatodistus [:huomiot :lammitys (case kieli
                                                  :fi :teksti-fi
                                                  :sv :teksti-sv)])]))

(defn toimenpide-ehdotukset-list-lammitys [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        toimenpide (get-in energiatodistus [:huomiot :lammitys :toimenpide] [])
        nimi-key (case kieli :fi :nimi-fi :sv :nimi-sv)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h3 (l :te-toimenpide-muutokset)]
     (et/description-list
       (map-indexed (fn [idx item]
                      {:dt (str (inc idx) ".")
                       :dd (get item nimi-key "")})
                    toimenpide))]))


(defn toimenpide-ehdotukset-table-lammitys [{:keys [kieli energiatodistus]}]
  (let [toimenpide (get-in energiatodistus [:huomiot :lammitys :toimenpide] [])]
    (et/table-toimenpide-ehdtukset kieli
                                (map-indexed (fn [idx item]
                                               {:dt (str (inc idx) ".")
                                                :dd [(:lampo item)
                                                     (:sahko item)
                                                     (:jaahdytys item)
                                                     (:eluvun-muutos item)
                                                     (str "todo")]})
                                             toimenpide))))

(defn arvio-teknisesta-kayttoiasta [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        arvio (get-in energiatodistus [:huomiot :lammitys :kayttoikaa-jaljella-arvio-vuosina])]
    [:div {:class "toimenpide-ehdotukset"}
     (description-list
       [{:dt (l :te-arvio-teknisesta-kayttoiasta-otsikko)
         :dd (str arvio " " (l :vuotta))}])]))

(defn toimenpide-ehdotukset-ilmanvaihto [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h2 (l :huomiot-ilmanvaihto-otsikko)]
     (get-in energiatodistus [:huomiot :iv-ilmastointi (case kieli
                                                            :fi :teksti-fi
                                                            :sv :teksti-sv)])]))

(defn toimenpide-ehdotukset-list-ilmanvaihto [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        toimenpide (get-in energiatodistus [:huomiot :iv-ilmastointi :toimenpide] [])
        nimi-key (case kieli :fi :nimi-fi :sv :nimi-sv)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h3 (l :te-toimenpide-muutokset)]
     (et/description-list
       (map-indexed (fn [idx item]
                      {:dt (str (inc idx) ".")
                       :dd (get item nimi-key "")})
                    toimenpide))]))

(defn toimenpide-ehdotukset-table-ilmanvaihto[{:keys [kieli energiatodistus]}]
  (let [toimenpide (get-in energiatodistus [:huomiot :iv-ilmastointi :toimenpide] [])]
    (et/table-toimenpide-ehdtukset kieli
                                (map-indexed (fn [idx item]
                                               {:dt (str (inc idx) ".")
                                                :dd [(:lampo item)
                                                     (:sahko item)
                                                     (:jaahdytys item)
                                                     (:eluvun-muutos item)
                                                     (str "todo")]})
                                             toimenpide))))

(defn generate-all-toimepide-ehdotukset-rakennuksen-vaippa [params]
  {:te-lammitys (toimenpide-ehdotukset-lammitys params)
   :te-list-lammitys (toimenpide-ehdotukset-list-lammitys params)
   :te-table-lammitys (toimenpide-ehdotukset-table-lammitys params)
   :te-arvio-teknisesta-kayttoiasta (arvio-teknisesta-kayttoiasta params)
   :te-ilmanvaihto (toimenpide-ehdotukset-ilmanvaihto params)
   :te-list-ilmanvaihto (toimenpide-ehdotukset-list-ilmanvaihto params)
   :te-table-ilmanvaihto (toimenpide-ehdotukset-table-ilmanvaihto params)})
