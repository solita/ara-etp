(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.complete-energiatodistus :as energiatodistus]
    [solita.etp.service.energiatodistus-pdf.ilmastoselvitys :as ilmastoselvitys]))

(defn- table-ostoenergia [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "etusivu-ostoenergia"}
     [:table
      [:thead
       [:tr
        [:th.empty]
        [:th.empty]
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
             [:td (or row "")])))]]]))

(defn- description-list [key-vals]
  (into [:dl]
        (mapv #(vec [:div
                     [:dt (str (:dt %) ":")]
                     [:dd (:dd %)]]) key-vals)))

(defn- laskennallinen-helper [value nettoala]
  (if nettoala
    (^[double] Math/round (/ (double (or value 0)) (double nettoala)))
    0))

(defn- painotettu-helper [value]
  (^[double] Math/round (or value 0)))

(defn ostoenergia [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        nettoala (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])

        laskennallinen-kaukolampo (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukolampo]) (laskennallinen-helper nettoala))
        laskennallinen-sahko (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :sahko]) (laskennallinen-helper nettoala))
        laskennallinen-uusiutuva-polttoaine (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine]) (laskennallinen-helper nettoala))
        laskennallinen-fossiilinen-polttoaine (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine]) (laskennallinen-helper nettoala))
        laskennallinen-kaukojaahdytys (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys]) (laskennallinen-helper nettoala))

        painotettu-kaukolampo (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala-kertoimella]) (painotettu-helper))
        painotettu-sahko (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :sahko-nettoala-kertoimella]) (painotettu-helper))
        painotettu-uusutuva (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala-kertoimella]) (painotettu-helper))
        painotettu-fossiilinen (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala-kertoimella]) (painotettu-helper))
        painotettu-kaukojaahdytys (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala-kertoimella]) (painotettu-helper))]

    (table-ostoenergia kieli
     [{:dt (l :laskennallinen-ostoenergia)
       :dd [(l :kwh-m2-vuosi)
            laskennallinen-kaukolampo
            laskennallinen-sahko
            laskennallinen-uusiutuva-polttoaine
            laskennallinen-fossiilinen-polttoaine
            laskennallinen-kaukojaahdytys]}
      {:dt (l :energimuodon-kerroin)
       :dd [(str "")
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :sahko-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin])]}
      {:dt (l :energiakulutus)
       :dd [(l :kwhE-m2-vuosi)
            painotettu-kaukolampo
            painotettu-sahko
            painotettu-uusutuva
            painotettu-fossiilinen
            painotettu-kaukojaahdytys]}])))

(defn ostoenergia-tiedot [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        tulokset (:tulokset energiatodistus)
        kasvihuonepaastot (energiatodistus/co2-paastot-et (:kaytettavat-energiamuodot tulokset))
        rounded (when kasvihuonepaastot (Math/round (double kasvihuonepaastot)))
        ;uusiutuvan-osuus (energiatodistus/uusiutuvan-osuus-paastoista (:tulokset energiatodistus))
        ;TODO get back to the uusiutuvan-energian-osuus when it's clear what to calculate
        ]

    [:div {:class "etusivu-ostoenergia"}
     (description-list
       [{:dt (l :energiakaytosta-syntyvat-kasvihuonepaastot)
         :dd (str rounded " " (l :kgCO2ekv-m2/vuosi))}
        {:dt (l :uusiutuva-energian-osuus)
         :dd (str "TODO: Add later when ready")}
        {:dt (l :kasvihuonepaastot)
         :dd (ilmastoselvitys/gwp-value-for-etusivu energiatodistus)}])]))
