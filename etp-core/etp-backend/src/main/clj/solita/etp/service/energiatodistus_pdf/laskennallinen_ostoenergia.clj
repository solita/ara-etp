(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia
  (:require
    [solita.etp.service.localization :as loc]))

(defn- table-ostoenergia [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:table.ostoenergia
     [:thead
      [:tr
       [:th.ostoenergia.empty]
       [:th.oe-otsikko (l :kaukolampo)]
       [:th.oe-otsikko (l :sahko)]
       [:th.oe-otsikko (l :uusiutuva-polttoaine)]
       [:th.oe-otsikko (l :fossiilinen-polttoaine)]
       [:th.oe-otsikko (l :kaukojaahdytys)]]]
     [:tbody
      (for [{:keys [dt dd]} items]
        (into
          [:tr [:td dt]]
          (for [row dd]
            [:td (or row "")])))]]))

(defn- description-list [key-vals]
  (into [:dl.et-etussivu-ostoenergia-tiedot]
        (mapv
          (fn [{:keys [dt dd dds]}]
            (into
              [:div]
              (cond
                dt  [[:dt (str dt ":")] [:dd dd]]
                dds (mapv (fn [dd] [:dd dd]) dds))))
          key-vals)))


(defn ostoenergia [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)]
       (table-ostoenergia kieli
        [{:dt (l :laskennallinen-ostoenergia)
          :dd [(get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukolampo])
               (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :sahko])
               (get-in energiatodistus [:tulokset :kautettavat-energiamuodot :uusiutuva-polttoaine])
               (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine])
               (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys])]}

         {:dt (l :energimuodon-kerroin)
          :dd [(get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin])
               (str "1.2")
               (str "0.5")
               (str "1.0")
               (str "0.28")]}
         {:dt (l :energiakulutus)
          :dd [(get-in energiatodistus [])
               (get-in energiatodistus [])
               (get-in energiatodistus [])
               (get-in energiatodistus [])
               (get-in energiatodistus [])]}])))

(defn ostoenergia-tiedot [{:keys [energiatodistus kieli]}]
  (let [l  (kieli loc/et-pdf-localization)]
    (description-list
      [{:dt (l :energiakaytosta-syntyvat)
        :dd (get-in energiatodistus [])}
       {:dt (l :uusiutuva-energian-osuus)
        :dd (get-in energiatodistus [])}
       {:dt (l :kasvihuonepaastot)
        :dd (get-in energiatodistus [])}])))
