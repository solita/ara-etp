(ns solita.etp.service.energiatodistus-pdf.etusivu-grafiikka
  (:require
    [solita.etp.service.localization :as loc]))

(defn arrow
  [{:keys [x y height body-width tip-width color text text-color]}]
  (let [h height
        bw body-width
        tw tip-width
        cy (+ y (/ h 2))
        text-padding 5]
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
      {:x (+ x text-padding)
       :y (+ y (/ h 2))
       :dy "0.35em"
       :text-anchor "start"
       :font-size 11
       :font-family "roboto, sans-serif"
       :font-weight "bold"
       :fill text-color}
      text]]))

(def arrows
  [{:text "A+ ‹ A0-20%" :color "#02af50" :text-color "#ffffff" :luokka "A+"}
   {:text "A0 ‹ 40" :color "#39d611" :luokka "A0"}
   {:text "A ‹ 80" :color "#3bee07" :luokka "A"}
   {:text "B 80-100" :color "#93d14f" :luokka "B"}
   {:text "C 120-150" :color "#b5f40b" :luokka "C"}
   {:text "D 150-220" :color "#fff000" :luokka "D"}
   {:text "E 220-300" :color "#ffc300" :luokka "E"}
   {:text "F 300-400" :color "#ec670b" :text-color "#ffffff" :luokka "F"}
   {:text "G > 400" :color "#d82e10" :text-color "#ffffff" :luokka "G"}])

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
  [{:keys [e-luokka]}]
  (let [row-height (+ arrow-height arrow-spacing)
        arrow-index (first (keep-indexed (fn [i a] (when (= (:luokka a) e-luokka) i)) arrows))
        arrow-y (when arrow-index (* arrow-index row-height))
        indicator-width 90
        indicator-tip-width 15
        x-end (+ indicator-line-length e-luokka-indicator-margin)
        x-start (- x-end indicator-width)
        x-tip (- x-start indicator-tip-width)
        h arrow-height
        cy (when arrow-y (+ arrow-y (/ h 2)))]
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
        {:x (+ x-tip 15)
         :y cy
         :dy "0.35em"
         :text-anchor "start"
         :font-size 11
         :font-family "roboto, sans-serif"
         :font-weight "bold"
         :fill "#ffffff"}
        e-luokka]])))

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

(defn stacked-arrows [{:keys [kieli e-luokka]}]
  (let [l (kieli loc/et-pdf-localization)
        num-arrows (count arrows)
        row-height (+ arrow-height arrow-spacing)
        total-height (* row-height num-arrows)
        arrow-elements (for [[i {:keys [text color text-color]}] (map-indexed vector arrows)]
                         (let [body-width (+ min-body-width (* width-increment i))
                               tip-w (+ tip-width (* tip-width-increment i))]
                           (arrow
                             {:x 0
                              :y (* i row-height)
                              :height arrow-height
                              :body-width body-width
                              :tip-width tip-w
                              :color color
                              :text text
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
         (e-luokka-indicator {:e-luokka e-luokka})]))))

(defn et-etusivu-grafiikka [{:keys [kieli energiatodistus]}]
  (let [l (kieli loc/et-pdf-localization)
        e-luokka (get-in energiatodistus [:tulokset :e-luokka])]
    [:div {:class "etusivu-grafiikka"}
     [:div {:class "etusivu-grafiikka-otsikko"} (l :energiatehokkuusluokka-otsikko)]
     (stacked-arrows {:kieli kieli :e-luokka e-luokka})]))
