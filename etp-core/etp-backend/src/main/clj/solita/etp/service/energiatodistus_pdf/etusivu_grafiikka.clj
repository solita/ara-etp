(ns solita.etp.service.energiatodistus-pdf.etusivu-grafiikka
  (:require
    [solita.etp.service.localization :as loc]))

(defn arrow
  [{:keys [x y height body-width tip-width color letter numbers text-color]}]
  (let [h height
        bw body-width
        tw tip-width
        cy (+ y (/ h 2))
        letter-padding 5
        numbers-x 25]
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
       :font-size 11
       :font-family "roboto, sans-serif"
       :font-weight "bold"
       :fill text-color}
      letter]

     [:text
      {:x (+ x numbers-x)
       :y (+ y (/ h 2))
       :dy "0.35em"
       :text-anchor "start"
       :font-size 11
       :font-family "roboto, sans-serif"
       :font-weight "bold"
       :fill text-color}
      numbers]]))

(def arrows
  [{:letter "A+" :numbers "&lt; A0-20%" :color "#02af50" :text-color "#ffffff" :luokka "A+"}
   {:letter "A0" :numbers "&lt; 40" :color "#39d611" :luokka "A0"}
   {:letter "A" :numbers "&lt; 80" :color "#3bee07" :luokka "A"}
   {:letter "B" :numbers "80-100" :color "#93d14f" :luokka "B"}
   {:letter "C" :numbers "120-150" :color "#b5f40b" :luokka "C"}
   {:letter "D" :numbers "150-220" :color "#fff000" :luokka "D"}
   {:letter "E" :numbers "220-300" :color "#ffc300" :luokka "E"}
   {:letter "F" :numbers "300-400" :color "#ec670b" :text-color "#ffffff" :luokka "F"}
   {:letter "G" :numbers "> 400" :color "#d82e10" :text-color "#ffffff" :luokka "G"}])

(def arrow-height 20)
(def arrow-spacing 4)
(def min-body-width 90)
(def width-increment 15)
(def tip-width 10)
(def tip-width-increment 2)
(def indicator-line-length 330)
(def e-luokka-indicator-margin 150)
(def svg-width (+ indicator-line-length e-luokka-indicator-margin))

(defn e-luokka-indicator
  [{:keys [e-luokka e-luku]}]
  (let [row-height (+ arrow-height arrow-spacing)
        arrow-index (first (keep-indexed (fn [i a] (when (= (:luokka a) e-luokka) i)) arrows))
        arrow-y (when arrow-index (* arrow-index row-height))
        indicator-width 140
        indicator-tip-width 15
        x-end (+ indicator-line-length e-luokka-indicator-margin)
        x-start (- x-end indicator-width)
        x-tip (- x-start indicator-tip-width)
        h arrow-height
        cy (when arrow-y (+ arrow-y (/ h 2)))
        luokka-x (+ x-tip 15)
        luku-x (+ x-tip 35)]
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
         :font-size 11
         :font-family "roboto, sans-serif"
         :font-weight "bold"
         :fill "#ffffff"}
        e-luokka]
       [:text
        {:x luku-x
         :y cy
         :dy "0.35em"
         :text-anchor "start"
         :font-size 11
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
       :font-size 11
       :fill "#000000"}
      label]]))

(defn stacked-arrows [{:keys [kieli e-luokka e-luku]}]
  (let [l (kieli loc/et-pdf-localization)
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
         (e-luokka-indicator {:e-luokka e-luokka :e-luku e-luku})]))))

(defn et-etusivu-grafiikka [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        e-luokka (get-in energiatodistus [:tulokset :e-luokka])
        e-luku (get-in energiatodistus [:tulokset :e-luku])]
    [:div {:class "etusivu-grafiikka"}
     [:div {:class "etusivu-grafiikka-otsikko"} (l :energiatehokkuusluokka-otsikko)]
     (stacked-arrows {:kieli kieli :e-luokka e-luokka :e-luku e-luku})]))
