(ns solita.etp.service.energiatodistus-pdf.etusivu-eluku
  (:require
    [solita.etp.service.localization :as loc]))

(defn- description-list [key-vals]
  (into [:div]
        (mapv #(vec [:div {:class "row"}
                     [:span {:class "label"} (str (:dt %) ":")]
                     [:span {:class "value"} (:dd %)]]) key-vals)))

(defn- a0-merkinta-teksti [l a0-merkinta-id]
  (when-let [localization-key
             (case a0-merkinta-id
               0 :a0-merkinta-ei-teksti
               1 :a0-merkinta-a0-teksti
               2 :a0-merkinta-a0-korjattu-teksti
               3 :a0-merkinta-a0-mpr-teksti
               nil)]
    (loc/with-subscripts (l localization-key))))


(defn et-etusivu-eluku-teksti [{:keys [kieli] :as params}]
  (let [l (kieli loc/et-pdf-localization)
        a0-merkinta-teksti (a0-merkinta-teksti
                             l
                             (-> params :energiatodistus :perustiedot :a0-merkinta-id))]
    [:div {:class "etusivu-eluku"}
     (when a0-merkinta-teksti
       [:div {:class "a0-merkinta"}
        [:span {:class "label"} a0-merkinta-teksti]])
     (description-list
      [{:dt (l :a+luokka-otsikko)
        :dd (l :a+luokka-teksti)}
       {:dt (l :a0luokka-otsikko)
        :dd (l :a0luokka-teksti)}
       {:dt (l :eluku-otsikko)
        :dd (l :eluku-teksti)}])]))
