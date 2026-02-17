(ns solita.etp.service.energiatodistus-pdf.etusivu-grafiikka
  (:require
    [solita.etp.service.localization :as loc]
    [solita.etp.service.e-luokka :as e-luokka]))

(defn arrow
  [{:keys [x y height body-width tip-width color letter numbers text-color]}]
  (let [h height
        bw body-width
        tw tip-width
        cy (+ y (/ h 2))
        letter-padding 5
        numbers-x 32]
    [:g
     [:polygon
      {:points (str
                 x "," y " "
                 (+ x bw) "," y " "
                 (+ x bw tw) "," cy " "
                 (+ x bw) "," (+ y h) " "
                 x "," (+ y h))
       :fill color}]

     [:text
      {:x (+ x letter-padding)
       :y (+ y (/ h 2))
       :dy "0.35em"
       :text-anchor "start"
       :font-size 13
       :font-family "roboto, sans-serif"
       :font-weight "bold"
       :fill text-color}
      letter]

     [:text
      {:x (+ x numbers-x)
       :y (+ y (/ h 2))
       :dy "0.35em"
       :text-anchor "start"
       :font-size 13
       :font-family "roboto, sans-serif"
       :font-weight "bold"
       :fill text-color}
      numbers]]))

(def arrows
  [{:letter "A+" :color "#009641" :luokka "A+"}
   {:letter "A0" :color "#52ae32" :luokka "A0"}
   {:letter "A" :color "#c8d302" :luokka "A"}
   {:letter "B" :color "#ffed00" :luokka "B"}
   {:letter "C" :color "#fbb900" :luokka "C"}
   {:letter "D" :color "#ec6608" :luokka "D"}
   {:letter "E" :color "#e50104" :text-color "#ffffff" :luokka "E"}
   {:letter "F" :color "#e40202" :text-color "#ffffff" :luokka "F"}
   {:letter "G" :color "#e40202" :text-color "#ffffff" :luokka "G"}])

(defn build-ranges [raja-asteikko]
  (into {}
        (map-indexed
          (fn [idx [value letter]]
            (let [[prev _] (get raja-asteikko (dec idx))]
              [letter
               (if prev
                 (str (inc prev) "-" value)
                 (str "&lt; " value))]))
          raja-asteikko)))

(defn set-arrow-thresholds [rajat]
  (let [ranges   (build-ranges (:raja-asteikko rajat))
        last-val (-> rajat :raja-asteikko last first)]
    (map
      (fn [{:keys [letter] :as arrow}]
        (assoc arrow :numbers
                     (case letter
                     "A+" "&lt; A0-20%"
                     "A0" "&lt; 40"
                     "G"  (str "&gt; " last-val)
                     (get ranges letter))))
      arrows)))

(def arrow-height 26)
(def arrow-spacing 5)
(def min-body-width 110)
(def width-increment 20)
(def tip-width 10)
(def tip-width-increment 3)
(def indicator-line-length 370)
(def e-luokka-indicator-margin 190)
(def svg-width (+ indicator-line-length e-luokka-indicator-margin))

(defn e-luokka-indicator
  [{:keys [e-luokka e-luku arrows]}]
  (let [row-height (+ arrow-height arrow-spacing)
        arrow-index (first (keep-indexed (fn [i a] (when (= (:luokka a) e-luokka) i)) arrows))
        arrow-y (when arrow-index (* arrow-index row-height))
        indicator-width 160
        indicator-tip-width 20
        x-end (+ indicator-line-length e-luokka-indicator-margin)
        x-start (- x-end indicator-width)
        x-tip (- x-start indicator-tip-width)
        h arrow-height
        cy (when arrow-y (+ arrow-y (/ h 2)))
        luokka-x (+ x-tip 20)
        luku-x (+ x-tip 40)]
    (when arrow-index
      [:g
       [:polygon
        {:points (str
                   x-end "," arrow-y " "
                   x-start "," arrow-y " "
                   x-tip "," cy " "
                   x-start "," (+ arrow-y h) " "
                   x-end "," (+ arrow-y h))
         :fill "#000000"}]
       [:text
        {:x luokka-x
         :y cy
         :dy "0.35em"
         :text-anchor "start"
         :font-size 13
         :font-family "roboto, sans-serif"
         :font-weight "bold"
         :fill "#ffffff"}
        e-luokka]
       [:text
        {:x luku-x
         :y cy
         :dy "0.35em"
         :text-anchor "start"
         :font-size 13
         :font-family "roboto, sans-serif"
         :font-weight "bold"
         :fill "#ffffff"}
        e-luku " kWhE/m2/vuosi"]])))

(defn indicator-line
  [{:keys [arrow-index label]}]
  (let [row-height (+ arrow-height arrow-spacing)
        line-y (+ (* arrow-index row-height) arrow-height (/ arrow-spacing 2))
        line-end-x indicator-line-length]
    [:g
     [:line
      {:x1 0
       :y1 line-y
       :x2 line-end-x
       :y2 line-y
       :stroke "#000000"
       :stroke-width 1
       :stroke-dasharray "1,2"}]
     [:text
      {:x line-end-x
       :y (- line-y 4)
       :text-anchor "end"
       :font-size 13
       :fill "#000000"}
      label]]))

(defn stacked-arrows [{:keys [kieli e-luokka e-luku rajat]}]
  (let [l (kieli loc/et-pdf-localization)
        arrows (set-arrow-thresholds rajat)
        num-arrows (count arrows)
        row-height (+ arrow-height arrow-spacing)
        total-height (* row-height num-arrows)
        arrow-elements (for [[i {:keys [letter numbers color text-color]}] (map-indexed vector arrows)]
                         (let [body-width (+ min-body-width (* width-increment i))
                               tip-w (+ tip-width (* tip-width-increment i))]
                           (arrow
                             {:x 0
                              :y (* i row-height)
                              :height arrow-height
                              :body-width body-width
                              :tip-width tip-w
                              :color color
                              :letter letter
                              :numbers numbers
                              :text-color (or text-color "#000000")})))]
    (into
      [:svg
       {:xmlns "http://www.w3.org/2000/svg"
        :viewBox (str "0 0 " svg-width " " total-height)
        :width "100%"
        :height total-height
        :preserveAspectRatio "xMinYMin meet"}]
      (concat
        arrow-elements
        [(indicator-line {:arrow-index 1 :label (l :paastoton-rakennus)})
         (e-luokka-indicator {:e-luokka e-luokka :e-luku e-luku :arrows arrows})]))))

(defn et-etusivu-grafiikka [{:keys [kieli energiatodistus kayttotarkoitukset alakayttotarkoitukset]}]
  (let [l (kieli loc/et-pdf-localization)
        e-luokka (get-in energiatodistus [:tulokset :e-luokka])
        e-luku (get-in energiatodistus [:tulokset :e-luku])
        versio (get-in energiatodistus [:versio])
        alakayttotarkoitus-id (get-in energiatodistus [:perustiedot :kayttotarkoitus])
        nettoala (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
        rajat (e-luokka/e-luokka-rajat kayttotarkoitukset alakayttotarkoitukset versio alakayttotarkoitus-id nettoala)]
    (when (and e-luokka e-luku)
      [:div {:class "etusivu-grafiikka"}
       [:div {:class "etusivu-grafiikka-otsikko"} (l :energiatehokkuusluokka-otsikko)]
       (stacked-arrows {:kieli kieli :e-luokka e-luokka :e-luku e-luku :rajat rajat})])))
