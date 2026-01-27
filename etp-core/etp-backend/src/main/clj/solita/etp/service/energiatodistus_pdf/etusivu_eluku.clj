(ns solita.etp.service.energiatodistus-pdf.etusivu-eluku
  (:require
    [solita.etp.service.localization :as loc]))

(defn- description-list [key-vals]
  (into [:div]
        (mapv #(vec [:div {:class "row"}
                     [:span {:class "label"} (str (:dt %) ":")]
                     [:span {:class "value"} (:dd %)]]) key-vals)))


(defn et-etusivu-eluku-teksti [{:keys [kieli] }]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "etusivu-eluku"}
     (description-list
      [{:dt (l :a+luokka-otsikko)
        :dd (l :a+luokka-teksti)}
       {:dt (l :a0luokka-otsikko)
        :dd (l :a0luokka-teksti)}
       {:dt (l :eluku-otsikko)
        :dd (l :eluku-teksti)}])]))
