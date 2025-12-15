(ns solita.etp.service.perusparannuspassi-pdf.laskennan-taustatiedot
  (:require
    [solita.etp.service.localization :as loc]))

(defn taulukko1 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:table.laskennan-taustatiedot
     [:thead
      [:tr
       [:th.otsikko {:colspan 6}
        (l :laskennan-taustatiedot-otsikko)]]
      [:tr.sarakkeet
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
    [:table.lammitys
     [:thead
      [:tr
       [:th.otsikko.lammitys.empty]
       [:th.otsikko.lammitys (l :paalammitysjarjestelma)]
       [:th.otsikko.lammitys (l :ilmanvaihto)]
       [:th.otsikko.lammitys (l :uusiutuva-energia)]]]

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
      [:table.mahdollisuus-liittya
       [:thead
        [:tr
         [:th.otsikko (l :mahdollisuus-liittya-otsikko)]]]
       [:tbody
        (for [row dd]
          [:tr
           [:td (or row "")]])]])))

(defn taulukko4 [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    (let [{:keys [dd]} (first items)]
      [:table.lisatietoja
       [:thead
        [:tr
         [:th.otsikko (l :lisatietoja-saatavilla)]]]
       [:tbody
        (for [row dd]
          [:tr
           [:td (or row "")]])]])))

(defn laskennan-taustatiedot-u-arvot
  [{:keys [perusparannuspassi energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko1 kieli
      [{:dt (l :lahtotilanne)
        :dd [(get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoseinat :u])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ylapohja :u])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :alapohja :u])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ikkunat :u])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulko-ovet :u])]}

       {:dt (l :vahimmaisvaatimus)
        :dd ["0,17" "0,09" "0,17" "0,70" "0,70"]}

       {:dt (l :ehdotettu-taso)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoseinat-ehdotettu])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ylapohja-ehdotettu])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :alapohja-ehdotettu])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ikkunat-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoovet-ehdotettu-taso])]}])))

(defn lammitys-ilmanvaihto
  [{:keys [perusparannuspassi energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko2 kieli
      [{:dt (l :lahtotilanne)
        :dd [(get-in energiatodistus [:lahtotiedot :lammmitys :lammitysmuoto-1])
             (get-in energiatodistus [:lahtotiedot :ilmanvaihto :kuvaus])
             (get-in energiatodistus [""])]}

       {:dt (l :ehdotettu-taso)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :paalammitysjarjestelma-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ilmanvaihto-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :uusiutuva-energia-ehdotettu-taso])]}

       {:dt (l :lisatietoja)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :lisatietoja])]}])))

(defn vahimmaisvaatimustaso [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.vahimmaisvaatimustaso
      [:dt (l :vahimmaisvaatimustaso)]
      [:dd.e-luku (str "154")]
      [:dd.yksikko (str (l :yksikko))]]))

(defn korjausrakentamisen-saadokset [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.korjausrakentamisen-saadokset
    [:p.note (l :korjausrakentamisen-saadokset)]]))

(defn mahdollisuus-liittya [{:keys [perusparannuspassi kieli]}]
    (taulukko3 kieli
      [{:dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :mahdollisuus-liittya])]}]))

(defn lisatiedot [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (taulukko4 kieli
      [{:dd [(str (l :lisatietoja1))
             (str (l :lisatietoja2))
             (str (l :lisatietoja3))
             (str (l :lisatietoja4))
             (str (l :lisatietoja5))]}])))

(defn voimassa-olo [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.voimassaolo
     [:p.note (l :voimassa-olo)]]))

