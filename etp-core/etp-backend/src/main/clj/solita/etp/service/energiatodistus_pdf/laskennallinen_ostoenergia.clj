(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.complete-energiatodistus :as energiatodistus]))

(defn- table-ostoenergia [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:table.ostoenergia
     [:thead
      [:tr
       [:th.ostoenergia.empty]
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
  (into [:dl.et-etusivu-yleistiedot-ostoenergia]
        (mapv #(vec [:div
                     [:dt (str (:dt %) ":")]
                     [:dd (:dd %)]]) key-vals)))

(defn ostoenergia [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        painotetut (energiatodistus/painotetut-ostoenergiat energiatodistus)]
    (table-ostoenergia kieli
     [{:dt (l :laskennallinen-ostoenergia)
       :dd [(str "kWhE/m2/vuosi")
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukolampo])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :sahko])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys])]}
      {:dt (l :energimuodon-kerroin)
       :dd [(str "")
            (str "0.5")
            (str "1.2")
            (str "0.5")
            (str "1")
            (str "0.28")]}
      {:dt (l :energiakulutus)
       :dd [(str "kWhE/m2/vuosi")
            (:kaukolampo painotetut)
            (:sahko painotetut)
            (:uusiutuva-polttoaine painotetut)
            (:fossiilinen-polttoaine painotetut)
            (:kaukojaahdytys painotetut)]}])))

(defn ostoenergia-tiedot [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        tulokset (:tulokset energiatodistus)
        kasvihuonepaastot (energiatodistus/co2-paastot-et (:kaytettavat-energiamuodot tulokset))
        rounded (when kasvihuonepaastot (Math/round (double kasvihuonepaastot)))
        ;uusiutuvan-osuus (energiatodistus/uusiutuvan-osuus-paastoista (:tulokset energiatodistus))
        ;TODO get back to the uusiutuvan-energian-osuus when it's clear what to calculate

        ]

    (description-list
      [{:dt (l :energiakaytosta-syntyvat-kasvihuonepaastot)
        :dd (str rounded " kgCO2ekv/m2/vuosi")}
       {:dt (l :uusiutuva-energian-osuus)
        :dd (str "TODO: Do the calculations when we're sure what to calculate")}
       {:dt (l :kasvihuonepaastot)
        :dd (str "TODO: kunhan päästään GWP juttuihin")}])))
