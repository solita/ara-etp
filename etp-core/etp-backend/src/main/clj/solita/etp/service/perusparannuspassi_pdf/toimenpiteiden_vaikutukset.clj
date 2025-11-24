(ns solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset)

(defn dark? [color]
  "Determines if a color is dark based on relative luminance.
   Uses the standard luminance formula: 0.299*R + 0.587*G + 0.114*B
   Returns true if the color's luminance is below the threshold."
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

(defn arrow [color x text1 text2]
  (let [text-fill (if (dark? color) "white" "#2c5234")]
    [:g {:transform (format "translate(%d,0)" x)}
     [:path {:d    "M -5.6958636,-5.9998443 V -51.85213 c 0,-3.313708 2.6862916,-6 6.0000002,-6 H 72.101936 c 3.841715,0 7.526063,1.526213 10.242433,4.242849 l 20.439861,20.441862 c 2.34306,2.343295 2.34297,6.14231 -2.1e-4,8.48549 L 82.344577,-4.2424851 C 79.628059,-1.5259677 75.943671,1.5590552e-4 72.101936,1.5590552e-4 H 0.3041366 c -3.3137086,0 -6.0000002,-2.68629160552 -6.0000002,-6.00000020552 z"
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

(def colors-by-e-luokka
  {"A" "#449841"
   "B" "#7dae35"
   "C" "#cad344"
   "D" "#fced4f"
   "E" "#e8b63e"})

(defn arrow-alt [vaiheet]
  (str "Energiatehokkuuden kehitys: "
       (clojure.string/join ", "
                            (map-indexed
                              (fn [idx {:keys [e-luku e-luokka]}]
                                (let [vaihe-name (if (zero? idx)
                                                   "Lähtötilanne"
                                                   (str "Vaihe " idx))]
                                  (str vaihe-name " energialuokka " e-luokka " E-luku " e-luku)))
                              vaiheet))))

(defn arrow-svg [vaiheet]
  (let [;; Fixed positions for up to 5 arrows (right to left: 400, 300, 200, 100, 0)
        arrow-positions [0 100 200 300 400]
        ;; Generate arrows only for vaiheet that exist
        arrows (map-indexed
                 (fn [idx vaihe]
                   (let [{:keys [e-luku e-luokka]} vaihe
                         color (get colors-by-e-luokka e-luokka "#e8b63e")
                         x-position (get arrow-positions idx 0)
                         vaihe-title (if (zero? idx)
                                       "Lähtötilanne"
                                       (str "Vaihe " idx))
                         perf-label (str e-luokka " - " e-luku)]
                     (arrow color x-position vaihe-title perf-label)))
                 vaiheet)
        ;; Generate accessibility description from vaiheet data
        alt-text (arrow-alt vaiheet)]
    [:svg {:xmlns      "http://www.w3.org/2000/svg"
           :viewBox    "-6 -60 520 65"
           :width      "100%"
           :role       "img"
           :aria-label alt-text
           :alt       alt-text}
     ;; Reverse the order so later vaiheet appear behind earlier ones
     (reverse arrows)]))

(defn house [toimenpide-ehdotukset]
  [:g
   [:path {:id "roof"
           :d "M 2.8793241,22.92212 24.988592,5.0422558 47.114073,22.936696 45.809767,24.559126 25.002882,7.738184 4.1952653,24.564235 Z"
           :stroke "#2c5234"
           :stroke-width "0.5"
           :fill (if (contains? toimenpide-ehdotukset :ylapohja) "#2c5234" "white")}]
   [:path {:id           "outer-shell"
           :d            "M 24.991756,9.0428589 8.3209391,22.487516 v 17.311088 h 4.7221949 l -0.0083,-12.137778 h 8.979297 l -0.03204,12.137778 H 41.673912 V 22.507153 Z m 1.953369,18.6179671 h 11.802381 v 7.078139 l -11.8184,-0.03152 z"
           :stroke "#2c5234"
           :stroke-width "0.5"
           :fill (if (contains? toimenpide-ehdotukset :julkisivu) "#2c5234" "white")}]
   [:path {:id "windows"
           :d "m 27.978952,28.726542 v 4.915006 h 9.718663 v -4.915006 z"
           :stroke "#2c5234"
           :stroke-width "0.5"
           :fill (if (contains? toimenpide-ehdotukset :ikkunat) "#2c5234" "white")}]
   [:path {:id "doors"
           :d "m 14.092866,28.726542 h 6.839653 V 39.7018 h -6.839653 z"
           :stroke "#2c5234"
           :stroke-width "0.5"
           :fill (if (contains? toimenpide-ehdotukset :ulkoovet) "#2c5234" "white")}]
   [:path {:id "undershell"
           :d "m 8.3209391,41.006106 33.3530029,0.03181 v 3.944731 l -33.3530029,0.0159 z"
           :stroke "#2c5234"
           :stroke-width "0.5"
           :fill (if (contains? toimenpide-ehdotukset :alapohja) "#2c5234" "white")}]])



(defn circle [filled?]
  [:path {:d "M 0,0 C 1.104,0 2,0.896 2,2 2,3.104 1.104,4 0,4 -1.104,4 -2,3.104 -2,2 -2,0.896 -1.104,0 0,0"
          :fill (if filled? "#2c5234" "white")
          :stroke (if filled? "none" "#2c5234")
          :stroke-width "0.4"}])

(defn circle-with-text [x y text filled?]
  [:g {:transform (format "translate(%d,%d)" x y)}
   (circle filled?)
   [:text {:x           "4"
           :y           "3.5"
           :font-family "roboto"
           :font-size   "4"
           :fill        "black"
           :text-anchor "start"}
    text]])

(defn kohdistuminen-svg [toimenpide-ehdotukset]
  [:svg {:xmlns   "http://www.w3.org/2000/svg"
         :viewBox "0 2 160 45"
         :width   "160mm"}
   (house toimenpide-ehdotukset)
   (circle-with-text 60 10 "Lämmitys" (contains? toimenpide-ehdotukset :lammitys))
   (circle-with-text 60 18 "Lämmin käyttövesi" (contains? toimenpide-ehdotukset :lammin-kayttovesi))
   (circle-with-text 60 26 "Ilmanvaihto" (contains? toimenpide-ehdotukset :ilmanvaihto))
   (circle-with-text 110 10 "Jäähdytys" (contains? toimenpide-ehdotukset :jaahdytys))
   (circle-with-text 110 18 "Valaistus" (contains? toimenpide-ehdotukset :valaistus))
   (circle-with-text 110 26 "Uusiutuva energia" (contains? toimenpide-ehdotukset :uusiutuva-energia))
   [:rect {:x 55 :y 35.5 :width 103.8 :height 9 :ry 2 :fill "#c0cbc2"}]
   (circle-with-text 60 38 "Kohdistuu muutoksia" true)
   (circle-with-text 110 38 "Ei kohdistu muutoksia" false)])

(defn toimenpiteiden-vaikutukset [_energiatodistus _perusparannuspassi]
  [:div {:class "vaikutukset-box"}
   [:h3 "Energiatehokkuusluokan ja E-luvun muutos"]
   (arrow-svg [{:e-luku 181
                :e-luokka "E"}
               {:e-luku 145
                :e-luokka "D"}
               {:e-luku 123
                :e-luokka "C"}
               {:e-luku 98
                :e-luokka "B"}
               {:e-luku 76
                :e-luokka "A"}])
   [:h3 "Toimenpiteiden kohdistuminen rakennuksen osa-alueisiin"]
   [:div {:class "kohdistuminen-box"}
    (kohdistuminen-svg #{:ylapohja :ulkoovet})]
   [:h3 "Rakennus ehdotettujen toimenpiteiden jälkeen"]
   [:p "Lopputaulukot tähän"]])
