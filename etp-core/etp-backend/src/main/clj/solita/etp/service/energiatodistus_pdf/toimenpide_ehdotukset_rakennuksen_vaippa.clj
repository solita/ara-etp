(ns solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset_rakennuksen_vaippa
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset :as et]
    [hiccup.core :refer [h]]))

(defn toimenpide-ehdotukset-ulkoseinat [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h1 (l :te-rakennusvaippa-otsikko)]
     (str (l :rakennusvaippa-teksti))
     [:h3 (l :huomiot-ymparys-otsikko)]
     (-> energiatodistus
         (get-in [:huomiot :ymparys (case kieli
                                      :fi :teksti-fi
                                      :sv :teksti-sv)])
         h)]))

(defn toimenpide-ehdotukset-list-ulko [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        toimenpide (get-in energiatodistus [:huomiot :ymparys :toimenpide] [])
        nimi-key (case kieli :fi :nimi-fi :sv :nimi-sv)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h4 (l :te-toimenpide-muutokset)]
     (et/description-list
       (map-indexed (fn [idx item]
                      {:dt (str (inc idx) ".")
                       :dd (-> item (get nimi-key "") h)})
                    toimenpide))]))


(defn toimenpide-ehdotukset-table-ulko [{:keys [kieli energiatodistus]}]
  (let [toimenpide (get-in energiatodistus [:huomiot :ymparys :toimenpide] [])]
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

(defn toimenpide-ehdotukset-pohjat [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h3 (l :huomiot-pohjat-otsikko)]
     (-> energiatodistus
         (get-in [:huomiot :alapohja-ylapohja (case kieli
                                                :fi :teksti-fi
                                                :sv :teksti-sv)])
         h)]))

(defn toimenpide-ehdotukset-list-pohjat [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        toimenpide (get-in energiatodistus [:huomiot :alapohja-ylapohja :toimenpide] [])
        nimi-key (case kieli :fi :nimi-fi :sv :nimi-sv)]
    [:div {:class "toimenpide-ehdotukset"}
     [:h4 (l :te-toimenpide-muutokset)]
     (et/description-list
       (map-indexed (fn [idx item]
                      {:dt (str (inc idx) ".")
                       :dd (-> item (get nimi-key "") h)})
                    toimenpide))]))

(defn toimenpide-ehdotukset-table-pohjat [{:keys [kieli energiatodistus]}]
  (let [toimenpide (get-in energiatodistus [:huomiot :alapohja-ylapohja :toimenpide] [])]
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

(defn generate-all-toimepide-ehdotukset-rakennuksen-vaippa [params]
  (into [:div]
        [(toimenpide-ehdotukset-ulkoseinat params)
         (toimenpide-ehdotukset-list-ulko params)
         (toimenpide-ehdotukset-table-ulko params)
         (toimenpide-ehdotukset-pohjat params)
         (toimenpide-ehdotukset-list-pohjat params)
         (toimenpide-ehdotukset-table-pohjat params)]))
