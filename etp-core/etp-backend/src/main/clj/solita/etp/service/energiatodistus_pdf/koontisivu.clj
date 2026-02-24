(ns solita.etp.service.energiatodistus-pdf.koontisivu
  (:require
    [hiccup.core :refer [h]]
    [solita.common.formats :as formats]
    [solita.etp.service.localization :as loc])
  (:import
    (java.time Instant ZoneId)
    (java.time.format DateTimeFormatter)))

(def ^:private datetime-formatter
  (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
             (ZoneId/of "Europe/Helsinki")))

(defn- fmt
  "Format number with specified decimal places. Returns empty string for nil."
  [value decimals] (or (formats/format-number value decimals false) ""))

(defn format-allekirjoitusaika
  "Format an instant into the desired format in Europe/Helsinki time.
  Returns empty string for nil."
  [^Instant time]
  (if time
    (.format datetime-formatter time)
    ""))

(defn koontisivu [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)]
    {:page-border? true
     :content
     [:div {:class "koontisivu"}

      [:div {:class "page-section"
             :id    "koontisivu-yleiset-tiedot"}
       [:div {:id "koontisivu-suuruudet"}
        [:dl
         [:div
          [:dt (l :vertailupinta-ala)]
          [:dd (-> energiatodistus
                   (get-in [:lahtotiedot :lammitetty-nettoala])
                   (fmt 2)
                   (str " " (l :m2)))]
          [:dt (l :lammin-ilmatilavuus)]
          [:dd (-> energiatodistus
                   (get-in [:lahtotiedot :rakennusvaippa :ilmatilavuus])
                   (fmt 2)
                   (str " " (l :m3)))]]
         [:div
          [:dt (l :rakennus-kykenee-reagoimaan)]
          [:dd (-> energiatodistus
                   :lahtotiedot :lammitys :lammonjako-lampotilajousto
                   {false :ei true :kylla}
                   l)]]]]
       [:h2 (l :lammitysjarjestelma-kuvaus)]
       [:dl {:class "table-description-list"}
        [:div
         [:dt (l :lammitysjarjestelma)]
         [:dd (-> energiatodistus
                  (get-in [:lahtotiedot :lammitys (kieli {:fi :lammitysmuoto-label-fi
                                                          :sv :lammitysmuoto-label-sv})])
                  h)]]
        [:div
         [:dt (l :lammonjako)]
         [:dd (-> energiatodistus
                  (get-in [:lahtotiedot :lammitys (kieli {:fi :lammonjako-label-fi
                                                          :sv :lammonjako-label-sv})])
                  h)]]]
       [:dl {:id    "koontisivu-lammonjakojarjestelma"
             :class "table-description-list"}
        [:div
         [:dt (l :lammonjakojarjestelma-lampotila)]
         [:dd (-> energiatodistus
                  :lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin
                  {false :ei true :kylla}
                  l)]]]
       [:h2 (l :ilmanvaihtojärjestelmän-kuvaus)]
       [:dl {:class "table-description-list"}
        [:div
         [:dt (l :ilmanvaihtojärjestelmä)]
         [:dd (-> energiatodistus
                  (get-in [:lahtotiedot :ilmanvaihto (kieli {:fi :label-fi
                                                             :sv :label-sv})])
                  h)]]]
       [:h2 (l :toteutunut-ostoenergy-ja-uusiutuva)]
       [:dl
        [:dt (l :tiedot-ovat-vuodelta)]
        [:dd (-> energiatodistus :toteutunut-ostoenergiankulutus :tietojen-alkuperavuosi (fmt 0))]]
       [:p (-> energiatodistus
               :toteutunut-ostoenergiankulutus
               (kieli {:fi :lisatietoja-fi
                       :sv :lisatietoja-sv})
               h)]
       [:table {:class "common-table"}
        [:thead
         [:tr
          [:th]
          [:th (l :kaukolampo-table)]
          [:th (l :sahko-table)]
          [:th (l :uusiutuva-polttoaine-table)]
          [:th (l :fossiilinen-polttoaine-table)]
          [:th (l :kaukojaahdytys-table)]
          [:th (l :uusiutuva-energia-table)]]]
        [:tbody
         [:tr
          [:th (l :kwh-vuosi)]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa (fmt 0))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa (fmt 0))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :uusiutuvat-polttoaineet-vuosikulutus-yhteensa (fmt 0))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :fossiiliset-polttoaineet-vuosikulutus-yhteensa (fmt 0))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa (fmt 0))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :uusiutuva-energia-vuosituotto-yhteensa (fmt 0))]]
         [:tr
          [:th (l :kwh-m2-vuosi)]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa-nettoala (fmt 1))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa-nettoala (fmt 1))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :uusiutuvat-polttoaineet-vuosikulutus-yhteensa-nettoala (fmt 1))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :fossiiliset-polttoaineet-vuosikulutus-yhteensa-nettoala (fmt 1))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa-nettoala (fmt 1))]
          [:td (-> energiatodistus :toteutunut-ostoenergiankulutus :uusiutuva-energia-vuosituotto-yhteensa-nettoala (fmt 1))]]]]
       [:div
        [:div {:id "toteutunut-energia-info"}
         [:p (l :toteutunut-ostoenergia-info)]]]]
      [:div {:class "page-section"
             :id    "koontisivu-keskeiset-toimenpiteet-section"}
       [:h2 (l :keskeiset-toimenpiteet-otsikko)]
       [:p [:strong (l :ei-koske-teksti)]]
       (if (-> energiatodistus :perusparannuspassi-valid)
         [:p (str (l :perusparannuspassi-laadinta)
                  " "
                  (-> energiatodistus :perusparannuspassi-id h))]
         [:div
          [:p {:id "koontisivu-keskeiset-toimenpiteet"}
           (-> energiatodistus :perustiedot (get-in [(kieli {:fi :keskeiset-suositukset-fi})]) h)]
          [:p (l :toimenpiteet-yksityiskohtaisemmin)]])]

      [:div {:class "page-section"
             :id    "koontisivu-havaintokaynti-tyokalu"}
       [:dl
        [:div
         [:dt (l :havainnointikaynti-ajankohta)]
         [:dd (str (-> energiatodistus :perustiedot :havainnointikaynti h)
                   " "
                   (-> energiatodistus
                       :perustiedot
                       (get-in [(kieli {:fi :havainnointikayntityyppi-fi
                                        :sv :havainnointikayntityyppi-sv})])
                       (or (l :havainnointikayntityyppi-ei-asetettu))
                       h))]]
        [:div
         [:dt (l :laskentatyokalu-nimi-versio)]
         [:dd (-> energiatodistus :tulokset :laskentatyokalu h)]]]]
      [:div {:class "page-section"
             :id    "koontisivu-laatijan-tiedot"}
       [:dl
        [:div
         [:dt (l :yritys)]
         [:dd (-> energiatodistus :perustiedot :yritys :nimi h)]]
        [:div
         [:dt (l :sahkoinen-allekirjoitus)]
         [:dd
          (str (-> energiatodistus :laatija-fullname h) " - "
               (-> energiatodistus :allekirjoitusaika format-allekirjoitusaika))]]]]]}))
