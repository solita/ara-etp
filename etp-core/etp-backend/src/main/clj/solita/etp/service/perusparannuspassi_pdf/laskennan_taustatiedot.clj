(ns solita.etp.service.perusparannuspassi-pdf.laskennan-taustatiedot
  (:require
    [solita.etp.service.localization :as loc]))

(defn taulukko1 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:table.lt-u-arvot
     [:thead
      [:tr
       [:th.lt-otsikko {:colspan 6}
        (l :lt-otsikko)]]
      [:tr.lt-sarakkeet
       [:th (l :U-arvot)]
       [:th (l :ulkoseinat)]
       [:th (l :ylapohja)]
       [:th (l :alapohja)]
       [:th (l :ikkunat)]
       [:th (l :ulko-ovet)]]]

     [:tbody
      (for [{:keys [dt dd]} items]
        [:tr
         [:td dt]
         (for [row dd]
           [:td (or row "")])])]]))

(defn taulukko2 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)
        last-index (dec (count items))]
    [:table.lt-lammitys
     [:thead
      [:tr
       [:th.lt-otsikko.lammitys.empty]
       [:th.lt-otsikko.lammitys (l :paalammitysjarjestelma)]
       [:th.lt-otsikko.lammitys (l :ilmanvaihto)]
       [:th.lt-otsikko.lammitys (l :uusiutuva-energia)]]]

     [:tbody
      (map-indexed
        (fn [idx {:keys [dt dd]}]
          (if (= idx last-index)
            [:tr
             [:td dt]
             [:td {:colspan 3} (or (first dd) "")]]
            [:tr
             [:td dt]
             (for [row dd]
               [:td (or row "")])]))
        items)]]))


(defn taulukko3 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    (let [{:keys [dd]} (first items)]
      [:table.lt-mahdollisuus-liittya
       [:thead
        [:tr
         [:th.lt-otsikko (l :mahdollisuus-liittya-otsikko)]]]
       [:tbody
        (for [row dd]
          [:tr
           [:td (or row "")]])]])))

(defn taulukko4 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    (let [{:keys [dd]} (first items)]
      [:table.lt-lisatietoja
       [:thead
        [:tr
         [:th.lt-otsikko (l :lisatietoja-saatavilla)]]]
       [:tbody
        (for [row dd]
          [:tr
           [:td (or row "")]])]])))

(defn lt-u-arvot
  [{:keys [perusparannuspassi energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko1 kieli
      [{:dt (l :lahtotilanne)
        :dd [(get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoseinat :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ylapohja :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :alapohja :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ikkunat :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoovet :U])]}

       {:dt (l :vahimmaisvaatimus)
        :dd ["0.17" "0.09" "0.17" "0.70" "0.70"]}

       {:dt (l :ehdotettu-taso)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoseinat-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ylapohja-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :alapohja-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ikkunat-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoovet-ehdotettu-taso])]}])))

(defn lt-lammitys-ilmanvaihto
  [{:keys [perusparannuspassi energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko2 kieli
      [{:dt (l :lahtotilanne)
        :dd [(get-in energiatodistus [:lahtotiedot :lammitys :lammitysmuoto-1 :id])
             (get-in energiatodistus [:lahtotiedot :ilmanvaihto :tyyppi-id])
             (get-in energiatodistus [""])]}

       {:dt (l :ehdotettu-taso)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :paalammitysjarjestelma-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ilmanvaihto-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :uusiutuva-energia-ehdotettu-taso])]}

       {:dt (l :lisatietoja)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot (case kieli
                                                                    :fi :lisatietoja-fi
                                                                    :sv :lisatietoja-sv)])]}])))

(defn lt-vahimmaisvaatimustaso [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-vahimmaisvaatimustaso
      [:dt (l :vahimmaisvaatimustaso)]
      [:dd.e-luku (str "154")]
      [:dd.yksikko (str (l :yksikko))]]))

(defn lt-korjausrakentamisen-saadokset [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-korjausrakentamisen-saadokset
    [:p.note (l :korjausrakentamisen-saadokset)]]))

(defn lt-mahdollisuus-liittya [{:keys [perusparannuspassi kieli mahdollisuus-liittya]}]
    (taulukko3 kieli
      [{:dd (-> perusparannuspassi (get-in [:rakennuksen-perustiedot :mahdollisuus-liittya-energiatehokkaaseen]) (loc/et-laskennan-taustatiedot-mahdollisuus-liittya-energiatehokkaaseen->description mahdollisuus-liittya kieli))}]))

(defn lt-lisatiedot [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko4 kieli
      [{:dd [(str (l :lisatietoja1))
             (str (l :lisatietoja2))
             (str (l :lisatietoja3))
             (str (l :lisatietoja4))
             (str (l :lisatietoja5))]}])))

(defn lt-voimassa-olo [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-voimassaolo
     [:p.note (l :voimassa-olo)]]))

