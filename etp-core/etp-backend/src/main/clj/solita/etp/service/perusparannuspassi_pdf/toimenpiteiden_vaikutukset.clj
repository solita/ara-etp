(ns solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset
  (:require [clojure.string :as str]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.localization :as loc]))

(defn- dark?
  "Determines if a color is dark based on relative luminance.
   Uses the standard luminance formula: 0.299*R + 0.587*G + 0.114*B
   Returns true if the color's luminance is below the threshold."
  [color]
  (let [;; Remove # prefix if present
        hex (if (.startsWith color "#") (subs color 1) color)
        ;; Parse RGB components (0-255)
        r (Integer/parseInt (subs hex 0 2) 16)
        g (Integer/parseInt (subs hex 2 4) 16)
        b (Integer/parseInt (subs hex 4 6) 16)
        ;; Calculate relative luminance (0-255 scale)
        luminance (+ (* 0.299 r) (* 0.587 g) (* 0.114 b))]
    ;; Threshold tuned so #7dae35 (luminance ~154) is dark
    ;; but #cad344 (luminance ~200) is not
    (< luminance 180)))

(defn- arrow [color x text1 text2]
  (let [text-fill (if (dark? color) "white" "#2c5234")
        path-d (str/join " "
                         ["M -5.6958636,-5.9998443"
                          "V -51.85213"
                          "c 0,-3.313708 2.6862916,-6 6.0000002,-6"
                          "H 72.101936"
                          "c 3.841715,0 7.526063,1.526213 10.242433,4.242849"
                          "l 20.439861,20.441862"
                          "c 2.34306,2.343295 2.34297,6.14231 -2.1e-4,8.48549"
                          "L 82.344577,-4.2424851"
                          "C 79.628059,-1.5259677 75.943671,1.5590552e-4 72.101936,1.5590552e-4"
                          "H 0.3041366"
                          "c -3.3137086,0 -6.0000002,-2.68629160552 -6.0000002,-6.00000020552"
                          "z"])]
    [:g {:transform (format "translate(%d,0)" x)}
     [:path {:d    path-d
             :fill color}]
     [:text {:x           "35"
             :y           "-31"
             :font-family "roboto"
             :font-size   "11"
             :font-weight "bold"
             :fill        text-fill
             :text-anchor "middle"}
      text1]
     [:text {:x           "35"
             :y           "-17"
             :font-family "roboto"
             :font-size   "11"
             :fill        text-fill
             :text-anchor "middle"}
      text2]]))

(def ^:private colors-by-e-luokka
  {"A" "#449841"
   "B" "#7dae35"
   "C" "#cad344"
   "D" "#fced4f"
   "E" "#e8b63e"})

(defn- arrow-alt [vaiheet kieli]
  (let [l (kieli loc/ppp-pdf-localization)]
    (str (l :energiatehokkuuden-kehitys) ": "
         (str/join ", "
                   (map-indexed
                     (fn [idx {:keys [e-luku e-luokka]}]
                       (let [vaihe-name (if (zero? idx)
                                          (l :lahtotilanne)
                                          (str (l :vaihe) " " idx))]
                         (str vaihe-name " " (l :energialuokka) " " e-luokka " " (l :e-luku) " " e-luku)))
                     vaiheet)))))

(defn- arrow-svg [vaiheet kieli]
  (let [l (kieli loc/ppp-pdf-localization)
        ;; Fixed positions for up to 5 arrows (right to left: 400, 300, 200, 100, 0)
        arrow-positions [0 100 200 300 400]
        ;; Generate arrows only for vaiheet that exist
        arrows (map-indexed
                 (fn [idx vaihe]
                   (let [{:keys [e-luku e-luokka]} vaihe
                         color (get colors-by-e-luokka e-luokka "#e8b63e")
                         x-position (get arrow-positions idx 0)
                         vaihe-title (if (zero? idx)
                                       (l :lahtotilanne)
                                       (str (l :vaihe) " " idx))
                         perf-label (str e-luokka " - " e-luku)]
                     (arrow color x-position vaihe-title perf-label)))
                 vaiheet)
        ;; Generate accessibility description from vaiheet data
        alt-text (arrow-alt vaiheet kieli)]
    [:svg {:xmlns      "http://www.w3.org/2000/svg"
           :viewBox    "-6 -60 520 65"
           :width      "100%"
           :role       "img"
           :aria-label alt-text
           :alt        alt-text}
     ;; Reverse the order so later vaiheet appear behind earlier ones
     (reverse arrows)]))

(defn- house [toimenpide-ehdotukset]
  [:g
   [:path {:id           "roof"
           :d            "M 2.8793241,22.92212 24.988592,5.0422558 47.114073,22.936696 45.809767,24.559126 25.002882,7.738184 4.1952653,24.564235 Z"
           :stroke       "#2c5234"
           :stroke-width "0.5"
           :fill         (if (:ylapohja toimenpide-ehdotukset) "#2c5234" "white")}]
   [:path {:id           "outer-shell"
           :d            "M 24.991756,9.0428589 8.3209391,22.487516 v 17.311088 h 4.7221949 l -0.0083,-12.137778 h 8.979297 l -0.03204,12.137778 H 41.673912 V 22.507153 Z m 1.953369,18.6179671 h 11.802381 v 7.078139 l -11.8184,-0.03152 z"
           :stroke       "#2c5234"
           :stroke-width "0.5"
           :fill         (if (:julkisivu toimenpide-ehdotukset) "#2c5234" "white")}]
   [:path {:id           "windows"
           :d            "m 27.978952,28.726542 v 4.915006 h 9.718663 v -4.915006 z"
           :stroke       "#2c5234"
           :stroke-width "0.5"
           :fill         (if (:ikkunat toimenpide-ehdotukset) "#2c5234" "white")}]
   [:path {:id           "doors"
           :d            "m 14.092866,28.726542 h 6.839653 V 39.7018 h -6.839653 z"
           :stroke       "#2c5234"
           :stroke-width "0.5"
           :fill         (if (:ulkoovet toimenpide-ehdotukset) "#2c5234" "white")}]
   [:path {:id           "undershell"
           :d            "m 8.3209391,41.006106 33.3530029,0.03181 v 3.944731 l -33.3530029,0.0159 z"
           :stroke       "#2c5234"
           :stroke-width "0.5"
           :fill         (if (:alapohja toimenpide-ehdotukset) "#2c5234" "white")}]])



(defn- circle [filled?]
  [:path {:d            "M 0,0 C 1.104,0 2,0.896 2,2 2,3.104 1.104,4 0,4 -1.104,4 -2,3.104 -2,2 -2,0.896 -1.104,0 0,0"
          :fill         (if filled? "#2c5234" "white")
          :stroke       (if filled? "none" "#2c5234")
          :stroke-width "0.4"}])

(defn- capitalize [s]
  (str (str/upper-case (subs s 0 1)) (subs s 1)))

(defn- circle-with-text [x y text filled?]
  [:g {:transform (format "translate(%d,%d)" x y)}
   (circle filled?)
   [:text {:x           "4"
           :y           "3.5"
           :font-family "roboto"
           :font-size   "4"
           :fill        "black"
           :text-anchor "start"}
    (capitalize text)]])

(defn- kohdistuminen-alt [toimenpide-ehdotukset kieli]
  (let [l (kieli loc/ppp-pdf-localization)
        {:keys [ylapohja julkisivu ikkunat ulkoovet alapohja
                lammitys lammin-kayttovesi ilmanvaihto jaahdytys
                valaistus uusiutuva-energia]} toimenpide-ehdotukset
        kaikki-kohteet [[(l :ylapohja) ylapohja]
                        [(l :julkisivu) julkisivu]
                        [(l :ikkunat) ikkunat]
                        [(l :ulko-ovet) ulkoovet]
                        [(l :alapohja) alapohja]
                        [(l :lammitys) lammitys]
                        [(l :lammin-kayttovesi) lammin-kayttovesi]
                        [(l :ilmanvaihto) ilmanvaihto]
                        [(l :jaahdytys) jaahdytys]
                        [(l :valaistus) valaistus]
                        [(l :uusiutuva-energia) uusiutuva-energia]]
        kuvaukset (map (fn [[nimi kohdistuu?]]
                         (str nimi " " (if kohdistuu? (l :kohdistuu) (l :ei-kohdistu))))
                       kaikki-kohteet)]
    (str (l :toimenpiteiden-kohdistuminen) ": "
         (str/join ", " kuvaukset))))
(defn- kohdistuminen-svg [toimenpide-ehdotukset kieli]
  (let [l (kieli loc/ppp-pdf-localization)
        {:keys [lammitys lammin-kayttovesi ilmanvaihto
                jaahdytys valaistus uusiutuva-energia]} toimenpide-ehdotukset
        alt-text (kohdistuminen-alt toimenpide-ehdotukset kieli)]
    [:svg {:xmlns      "http://www.w3.org/2000/svg"
           :viewBox    "0 2 160 45"
           :width      "160mm"
           :role       "img"
           :aria-label alt-text
           :alt        alt-text}
     (house toimenpide-ehdotukset)
     (circle-with-text 60 10 (l :lammitys) lammitys)
     (circle-with-text 60 18 (l :lammin-kayttovesi) lammin-kayttovesi)
     (circle-with-text 60 26 (l :ilmanvaihto) ilmanvaihto)
     (circle-with-text 110 10 (l :jaahdytys) jaahdytys)
     (circle-with-text 110 18 (l :valaistus) valaistus)
     (circle-with-text 110 26 (l :uusiutuva-energia) uusiutuva-energia)
     [:rect {:x 55 :y 35.5 :width 103.8 :height 9 :ry 2 :fill "#c0cbc2"}]
     (circle-with-text 60 38 (l :kohdistuu-muutoksia) true)
     (circle-with-text 110 38 (l :ei-kohdistu-muutoksia) false)]))

(def ^:private vaikutus-luokat
  {0  :lammitys
   1  :lammitys
   2  :lammitys
   3  :lammitys
   4  :lammitys
   5  :lammitys
   6  :uusiutuva-energia
   7  :lammitys
   8  :lammin-kayttovesi
   9  :lammitys
   10 :lammin-kayttovesi
   11 :ilmanvaihto
   12 :ilmanvaihto
   13 :ilmanvaihto
   14 :ilmanvaihto
   15 :ilmanvaihto
   16 :valaistus
   17 :valaistus
   18 :valaistus
   19 :jaahdytys
   20 :jaahdytys
   21 :jaahdytys
   22 :jaahdytys
   23 :uusiutuva-energia
   24 :uusiutuva-energia
   25 :uusiutuva-energia
   26 :uusiutuva-energia
   27 :ikkunat
   28 :ulkoovet
   29 :ylapohja
   30 :alapohja
   31 :julkisivu
   32 :julkisivu
   33 :uusiutuva-energia
   34 :uusiutuva-energia
   35 :lammitys
   36 :lammitys
   37 :lammitys
   38 :uusiutuva-energia
   39 :lammitys
   40 :lammitys
   41 :lammin-kayttovesi
   42 :lammin-kayttovesi
   43 :lammin-kayttovesi
   44 :ilmanvaihto
   45 :uusiutuva-energia})

(defn- ppp-vaihe->et-ish-for-e-luku
  [et vaihe]
  {:lahtotiedot {:lammitetty-nettoala (get-in et [:lahtotiedot :lammitetty-nettoala])}
   :tulokset
   {:kaytettavat-energiamuodot {:fossiilinen-polttoaine (-> vaihe :tulokset :ostoenergian-tarve-fossiiliset-pat)
                                :sahko                  (-> vaihe :tulokset :ostoenergian-tarve-sahko)
                                :kaukojaahdytys         (-> vaihe :tulokset :ostoenergian-tarve-kaukojaahdytys)
                                :kaukolampo             (-> vaihe :tulokset :ostoenergian-tarve-kaukolampo)
                                :uusiutuva-polttoaine   (-> vaihe :tulokset :ostoenergian-tarve-uusiutuvat-pat)}}})

(defn toimenpiteiden-vaikutukset [{:keys [kieli
                                          energiatodistus
                                          perusparannuspassi
                                          kayttotarkoitukset
                                          alakayttotarkoitukset]}]
  (let [l (kieli loc/ppp-pdf-localization)
        ;; Build vaiheet array: start with energiatodistus (lähtötilanne), then add all PPP vaiheet
        lahtotilanne {:e-luku   (get-in energiatodistus [:tulokset :e-luku])
                      :e-luokka (get-in energiatodistus [:tulokset :e-luokka])}
        ppp-vaiheet (->> (:vaiheet perusparannuspassi)
                         (filter (fn [vaihe] (and (:valid vaihe)
                                                  (-> vaihe (get-in [:tulokset :vaiheen-alku-pvm]) some?)
                                                  #_(not (nil? (get-in vaihe [:tulokset :vaiheen-alku-pvm]))))))
                         (map (fn [vaihe]
                                (let [versio 2018
                                      e-luku (e-luokka-service/e-luku versio (ppp-vaihe->et-ish-for-e-luku energiatodistus vaihe))
                                      e-luokka (-> (e-luokka-service/e-luokka kayttotarkoitukset alakayttotarkoitukset versio
                                                                              (get-in energiatodistus [:perustiedot :kayttotarkoitus])
                                                                              (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
                                                                              e-luku)
                                                   :e-luokka)]
                                  {:e-luku e-luku
                                   :e-luokka e-luokka}))))
        all-vaiheet (cons lahtotilanne ppp-vaiheet)]
    [:div {:class "vaikutukset-box"}
     [:h3 (l :energiatehokkuusluokan-muutos)]
     (arrow-svg all-vaiheet kieli)
     [:h3 (l :toimenpiteiden-kohdistuminen)]
     (let [;; Collect all toimenpide-ehdotukset from all vaiheet
           all-ehdotukset (mapcat #(get-in % [:toimenpiteet :toimenpide-ehdotukset])
                                  (:vaiheet perusparannuspassi))
           ;; Create a map showing if any vaihe targets each element
           kohdistuminen (reduce (fn [acc ehdotus]
                                   (merge-with #(or %1 %2)
                                               acc
                                               {(-> ehdotus :id vaikutus-luokat) true}))
                                 {:ylapohja          false
                                  :julkisivu         false
                                  :ikkunat           false
                                  :ulkoovet          false
                                  :alapohja          false
                                  :lammitys          false
                                  :lammin-kayttovesi false
                                  :ilmanvaihto       false
                                  :jaahdytys         false
                                  :valaistus         false
                                  :uusiutuva-energia false}
                                 all-ehdotukset)]
       [:div {:class "kohdistuminen-box"}
        (kohdistuminen-svg kohdistuminen kieli)])
     [:h3 (l :rakennus-toimenpiteiden-jalkeen)]
     (let [;; Get the final vaihe's results (last in the list)
           final-vaihe (last (:vaiheet perusparannuspassi))
           final-tulokset (:tulokset final-vaihe)
           ;; Check if requirements are met based on the final vaihe
           tayttaa-a0 (-> perusparannuspassi :passin-perustiedot :tayttaa-a0-vaatimukset)
           tayttaa-a-plus (-> perusparannuspassi :passin-perustiedot :tayttaa-aplus-vaatimukset)]
       [:dl {:class "tayttaa-vaatimukset-list"}
        [:div
         [:dt (l :tayttaa-a0-luokan-vaatimukset)]
         [:dd (if tayttaa-a0 (l :kylla) (l :ei))]]
        [:div
         [:dt (l :tayttaa-a-plus-luokan-vaatimukset)]
         [:dd (if tayttaa-a-plus (l :kylla) (l :ei))]]])
     [:div {:class "vaatimukset-selitteet-box"}
      [:div {:class "vaatimukset-selitteet"}
       [:div (l :a0-selite)]
       [:div (l :a-plus-selite)]]]]))
