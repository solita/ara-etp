(ns solita.etp.service.perusparannuspassi-pdf.laskennan-taustatiedot
  (:require
    [hiccup.core :refer [h]]
    [solita.etp.service.localization :as loc]))

(defn- table-u-arvot [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:table.lt-u-arvot
     [:thead
      [:tr
       [:th.lt-otsikko {:colspan 6}
        (l :lt-otsikko)]]
      [:tr.lt-sarakkeet
       [:th (l :U-arvot-lt)]
       [:th (l :ulkoseinat-lt)]
       [:th (l :ylapohja-lt)]
       [:th (l :alapohja-lt)]
       [:th (l :ikkunat-lt)]
       [:th (l :ulko-ovet-lt)]]]

     [:tbody
      (for [{:keys [dt dd]} items]
        [:tr
         [:td dt]
         (for [row dd]
           [:td (or row "")])])]]))

(defn- table-lammitys-ilmavaihto [kieli items]
  (let [l (kieli loc/ppp-pdf-localization)
        last-index (dec (count items))]
    [:table.lt-lammitys
     [:thead
      [:tr
       [:th.lt-otsikko.lammitys.empty]
       [:th.lt-otsikko.lammitys (l :paalammitysjarjestelma)]
       [:th.lt-otsikko.lammitys (l :ilmanvaihto-lt)]
       [:th.lt-otsikko.lammitys (l :uusiutuva-energia-lt)]]]

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


(defn- table-mahdollisuus-liittya [kieli items]
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

(defn- table-lisatietoja [kieli items]
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

(def minimum-u 0.45)

(defn- count-u
  [u-value threshold]
  (when u-value
    (let [value (max (* u-value minimum-u) threshold)]
      (/ (^[double] Math/round (* value 100.0)) 100.0)))
  )

(defn- lt-u-arvot
  [{:keys [perusparannuspassi energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)
        ulkoseinat (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoseinat :U])
        ylapohja (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ylapohja :U])
        alapohja (get-in energiatodistus [:lahtotiedot :rakennusvaippa :alapohja :U])

        ulkoseinat-vaatimus (count-u ulkoseinat 0.17)
        ylapohja-vaatimus (count-u ylapohja 0.09)
        alapohja-vaatimus (count-u alapohja 0.17)]


    (table-u-arvot kieli
        [{:dt (l :lahtotilanne-lt)
        :dd [(get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoseinat :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ylapohja :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :alapohja :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ikkunat :U])
             (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ulkoovet :U])]}

       {:dt (l :vahimmaisvaatimus)
        :dd [ulkoseinat-vaatimus ylapohja-vaatimus alapohja-vaatimus "0.70" "0.70"]}

       {:dt (l :ehdotettu-taso)
        :dd [(get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoseinat-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ylapohja-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :alapohja-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ikkunat-ehdotettu-taso])
             (get-in perusparannuspassi [:rakennuksen-perustiedot :ulkoovet-ehdotettu-taso])]}])))

(defn- lt-lammitys-ilmanvaihto
  [{:keys [perusparannuspassi energiatodistus lammitysmuodot ilmanvaihtotyypit uusiutuva-energia kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)
        kuvaus-kieli (case kieli :fi :label-fi :sv :label-sv)

        lammitys-id (get-in energiatodistus [:lahtotiedot :lammitys :lammitysmuoto-1 :id])
        ilmanvaihto-id (get-in energiatodistus [:lahtotiedot :ilmanvaihto :tyyppi-id])
        uusiutuva-energia-id (get-in energiatodistus [""])
        lammitys-kuvaus (some #(when (= (:id %) lammitys-id) (kuvaus-kieli %)) lammitysmuodot)
        ilmanvaihto-kuvaus (some #(when (= (:id %) ilmanvaihto-id) (kuvaus-kieli %)) ilmanvaihtotyypit)
        uusiutuva-energia-kuvaus (some #(when (= (:id %) uusiutuva-energia-id) (kuvaus-kieli %)) uusiutuva-energia)

        lammitys-ehdotus-id (get-in perusparannuspassi [:rakennuksen-perustiedot :paalammitysjarjestelma-ehdotettu-taso])
        ilmanvaihto-ehdotus-id (get-in perusparannuspassi [:rakennuksen-perustiedot :ilmanvaihto-ehdotettu-taso])
        uusiutuva-energia-ehdotus-id (get-in perusparannuspassi [:rakennuksen-perustiedot :uusiutuva-energia-ehdotettu-taso])
        lammitys-ehdotus-kuvaus (some #(when (= (:id %) lammitys-ehdotus-id) (kuvaus-kieli %)) lammitysmuodot)
        ilmanvaihto-ehdotus-kuvaus (some #(when (= (:id %) ilmanvaihto-ehdotus-id) (kuvaus-kieli %)) ilmanvaihtotyypit)
        uusiutuva-energia-ehdotus-kuvaus (some #(when (= (:id %) uusiutuva-energia-ehdotus-id) (kuvaus-kieli %)) uusiutuva-energia)]


    (table-lammitys-ilmavaihto kieli
       [{:dt (l :lahtotilanne-lt)
        :dd [lammitys-kuvaus
             ilmanvaihto-kuvaus
             uusiutuva-energia-kuvaus]}

       {:dt (l :ehdotettu-taso)
        :dd [lammitys-ehdotus-kuvaus
             ilmanvaihto-ehdotus-kuvaus
             uusiutuva-energia-ehdotus-kuvaus]}

       {:dt (l :lisatietoja-lt)
        :dd [(-> perusparannuspassi
                 (get-in [:rakennuksen-perustiedot (case kieli
                                                     :fi :lisatietoja-fi
                                                     :sv :lisatietoja-sv)])
                 h)]}])))

(defn- lt-vahimmaisvaatimustaso [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-vahimmaisvaatimustaso
      [:dt (l :vahimmaisvaatimustaso)]
      [:dd.e-luku (str "154")]
      [:dd.yksikko (str (l :yksikko))]]))

(defn- lt-korjausrakentamisen-saadokset [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-korjausrakentamisen-saadokset
    [:p.note (l :korjausrakentamisen-saadokset)]]))

(defn- lt-mahdollisuus-liittya [{:keys [perusparannuspassi mahdollisuus-liittya kieli]}]
  (let [id (get-in perusparannuspassi [:rakennuksen-perustiedot :mahdollisuus-liittya-energiatehokkaaseen])
        kuvauskieli (case kieli :fi :label-fi :sv :label-sv)
        kuvaus (some #(when (= (:id %) id) (kuvauskieli %)) mahdollisuus-liittya)]
    (table-mahdollisuus-liittya kieli [{:dd [kuvaus]}])))

(defn- lt-lisatiedot [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    (table-lisatietoja kieli [{:dd [(str (l :lisatietoja1))
             (str (l :lisatietoja2))
             (str (l :lisatietoja3))
             (str (l :lisatietoja4))
             (str (l :lisatietoja5))]}])))

(defn- lt-voimassa-olo [{:keys [kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl.lt-voimassaolo
     [:p.note (l :voimassa-olo)]]))

(defn generate-all-laskennan-taustatiedot [params]
  {:lt-u-arvot (lt-u-arvot params)
   :lt-lammitys-ilmanvaihto (lt-lammitys-ilmanvaihto params)
   :lt-vahimmaisvaatimustaso (lt-vahimmaisvaatimustaso params)
   :lt-korjausrakentamisen-saadokset (lt-korjausrakentamisen-saadokset params)
   :lt-mahdollisuus-liittya (lt-mahdollisuus-liittya params)
   :lt-lisatiedot (lt-lisatiedot params)
   :lt-voimassa-olo (lt-voimassa-olo params)})
