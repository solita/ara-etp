(ns solita.etp.service.perusparannuspassi-pdf.vaiheistuksen-yhteenveto
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [solita.etp.service.localization :as loc])
  (:import
    [java.text DecimalFormat DecimalFormatSymbols]))

(defn- format-number [n]
  (str/replace (pp/cl-format nil "~:d" (Math/round (double n))) "," " "))

(def format-float
  (let [dfs (doto (DecimalFormatSymbols.)
              (.setGroupingSeparator \space))
        fmt (DecimalFormat. "#,###.##", dfs)]
    (fn [n] (.format fmt n))))

(defn- format-percentage [value]
  (when value
    (str value " %")))

(defn- safe-divide [numerator denominator]
  (when (and numerator denominator (not (zero? denominator)))
    (/ (double numerator) (double denominator))))

(defn- percentage-change [old-value new-value]
  (when (and old-value new-value)
    (when-let [ratio (safe-divide (- new-value old-value) old-value)]
      (long (Math/round (* 100.0 (double ratio)))))))

(defn- build-vaihe-data
  "Build a vec of lahtotilanne (shaped like a vaihe) and valid PPP vaiheet.
   Only includes vaiheet that have a starting date (vaiheen-alku-pvm).
   This filters out vaiheet where data was added but later deleted."
  [perusparannuspassi]
  (let [lahtotilanne (-> perusparannuspassi :lahtotilanne)
        ppp-vaiheet (->> (:vaiheet perusparannuspassi)
                         ;; Filter to only include vaiheet with a starting date
                         ;; This excludes vaiheet where the user added then deleted data
                         (filter #(some? (get-in % [:tulokset :vaiheen-alku-pvm]))))]    (vec (cons lahtotilanne ppp-vaiheet))))

(defn mid-header-tr [text]
  [:tr [:th {:class "th1" :scope "rowgroup" :colspan 6} text]])

(defn row-header-th [text]
  [:th {:class "th2" :scope "row"} text])

(defn- vaihe-difference [prev curr]
  (when (and prev curr)
    (let [ostoenergia-prev (get-in prev [:tulokset :ostoenergia])
          ostoenergia-curr (get-in curr [:tulokset :ostoenergia])

          painotettu-prev (get-in prev [:tulokset :painotettu-ostoenergia])
          painotettu-curr (get-in curr [:tulokset :painotettu-ostoenergia])

          lasku-prev (get-in prev [:tulokset :energia-kustannukset])
          lasku-curr (get-in curr [:tulokset :energia-kustannukset])

          toteutunut-prev (get-in prev [:tulokset :toteutunut-ostoenergia])
          toteutunut-curr (get-in curr [:tulokset :toteutunut-ostoenergia])

          toteutunut-lasku-prev (get-in prev [:tulokset :toteutunut-energia-kustannukset])
          toteutunut-lasku-curr (get-in curr [:tulokset :toteutunut-energia-kustannukset])

          co2-prev (get-in prev [:tulokset :co2-paastot])
          co2-curr (get-in curr [:tulokset :co2-paastot])]
      {:ostoenergia-muutos      (when (and ostoenergia-prev ostoenergia-curr)
                                  (- ostoenergia-curr ostoenergia-prev))
       :ostoenergia-muutos-pct  (percentage-change ostoenergia-prev ostoenergia-curr)
       :painotettu-muutos       (when (and painotettu-prev painotettu-curr)
                                  (- (double painotettu-curr) (double painotettu-prev)))
       :painotettu-muutos-pct   (percentage-change painotettu-prev painotettu-curr)
       :lasku-saasto            (when (and lasku-prev lasku-curr)
                                  (- lasku-curr lasku-prev))
       :toteutunut-muutos       (when (and toteutunut-prev toteutunut-curr)
                                  (- toteutunut-curr toteutunut-prev))
       :toteutunut-muutos-pct   (percentage-change toteutunut-prev toteutunut-curr)
       :toteutunut-lasku-saasto (when (and toteutunut-lasku-prev toteutunut-lasku-curr)
                                  (- toteutunut-lasku-curr toteutunut-lasku-prev))
       :co2-muutos              (when (and co2-prev co2-curr)
                                  (- co2-curr co2-prev))})))

(defn vaiheistuksen-yhteenveto [{:keys [kieli perusparannuspassi]}]
  (let [l (kieli loc/ppp-pdf-localization)
        vaiheet (build-vaihe-data perusparannuspassi)
        ppp-tulokset (:tulokset perusparannuspassi)

        ;; Build data rows for up to 5 columns (lähtötilanne + 4 vaiheet)
        ;; Pad with nils if fewer than 5 columns
        padded-vaiheet (take 5 (concat vaiheet (repeat nil)))

        ;; Calculate changes between consecutive vaiheet
        changes (mapv vaihe-difference
                      padded-vaiheet
                      (rest padded-vaiheet))

        ;; Helper to get class for E-luokka
        e-luokka-class (fn [luokka]
                         (when luokka
                           (str "energialuokka-" (str/lower-case luokka))))]

    [:div {:class "vaiheistuksen-yhteenveto"}
     [:table
      [:colgroup
       [:col {:style "width: 250px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]]
      [:thead
       [:tr
        [:th]
        [:th {:class "th1" :scope "col"} (l :lahtotilanne)]
        (for [i (range 1 5)]
          (let [vaihe (nth padded-vaiheet i)]
            [:th {:class "th1" :scope "col"}
             (str (l :vaihe) " " i)
             (when-let [year-range (:year-range vaihe)]
               [:br [:span {:class "year-range"} year-range]])]))]]
      [:tbody
       ;; E-luku row
       [:tr
        (row-header-th (l :laskennallinen-kokonaisenergiankulutus-e-luku))
        (for [vaihe padded-vaiheet]
          (if-let [e-luku (get-in vaihe [:tulokset :e-luku])]
            [:td {:class (e-luokka-class (get-in vaihe [:tulokset :e-luokka]))} (Math/round (double e-luku))]
            [:td]))]

       ;; E-luokka row
       [:tr
        (row-header-th (l :energiatehokkuusluokka))
        (for [vaihe padded-vaiheet]
          (if-let [e-luokka (get-in vaihe [:tulokset :e-luokka])]
            [:td {:class (e-luokka-class e-luokka)} e-luokka]
            [:td]))]

       ;; Vakioidun ostoenergian muutokset
       (mid-header-tr (l :vakioidun-ostoenergian-muutokset))

       [:tr
        (row-header-th (l :ostoenergian-muutos-kwh-vuosi))
        [:td {:class "shaded"} (format-number (-> padded-vaiheet first :tulokset :ostoenergia))]
        (for [change changes]
          (if-let [muutos (:ostoenergia-muutos change)]
            [:td (format-number muutos)]
            [:td]))]

       [:tr
        (row-header-th (l :ostoenergian-muutos-prosentti))
        [:td {:class "shaded"} "-"]
        (for [change changes]
          (if-let [pct (:ostoenergia-muutos-pct change)]
            [:td (format-percentage pct)]
            [:td]))]

       [:tr
        (row-header-th (l :painotetun-energiankulutuksen-muutos-kwh))
        [:td {:class "shaded"} (when-let [painotettu (get-in (first padded-vaiheet) [:tulokset :painotettu-ostoenergia])]
                                 (format-number (double painotettu)))]
        (for [change changes]
          (if-let [muutos (:painotettu-muutos change)]
            [:td (format-number muutos)]
            [:td]))]

       [:tr
        (row-header-th (l :painotetun-energiankulutuksen-muutos-prosentti))
        [:td {:class "shaded"} "-"]
        (for [change changes]
          (if-let [pct (:painotettu-muutos-pct change)]
            [:td (format-percentage pct)]
            [:td]))]

       [:tr
        (row-header-th (l :saasto-energialaskussa))
        [:td {:class "shaded"} "-"]
        (for [change changes]
          (if-let [saasto (:lasku-saasto change)]
            [:td (format-number saasto)]
            [:td]))]

       ;; Toteutuneen mitatun ostoenergian muutokset
       (mid-header-tr (l :toteutuneen-mitatun-ostoenergian-muutokset))

       [:tr
        (row-header-th (l :ostoenergian-muutos-kwh-vuosi))
        [:td {:class "shaded"}
         (when-let [toteutunut (get-in (first padded-vaiheet) [:tulokset :toteutunut-ostoenergia])]
          (format-number (double toteutunut)))]
        (for [change changes]
          (if-let [muutos (:toteutunut-muutos change)]
            [:td (format-number muutos)]
            [:td]))]

       [:tr
        (row-header-th (l :ostoenergian-muutos-prosentti))
        [:td {:class "shaded"} "-"]
        (for [change changes]
          (if-let [pct (:toteutunut-muutos-pct change)]
            [:td (format-percentage pct)]
            [:td]))]

       [:tr
        (row-header-th (l :saasto-energialaskussa))
        [:td {:class "shaded"} "-"]
        (for [change changes]
          (if-let [saasto (:toteutunut-lasku-saasto change)]
            [:td (format-number saasto)]
            [:td]))]

       ;; Uusiutuvan energian määrät
       (mid-header-tr (l :uusiutuvan-energian-laskennalliset-maarat))

       [:tr
        (row-header-th (l :uusiutuvan-energian-kokonaistuotto))
        [:td {:class "shaded"}
         (when-let [tuotto (-> padded-vaiheet first :tulokset :uusiutuvan-energian-kokonaistuotto)]
           (format-number tuotto))]
        (for [i (range 1 5)]
          (if-let [tulokset (-> padded-vaiheet (nth i) :tulokset)]
            (if-let [tuotto (:uusiutuvan-energian-kokonaistuotto tulokset)]
              [:td (format-number tuotto)]
              [:td])
            [:td]))]

       [:tr
        (row-header-th (l :rakennuksen-hyodyntama-osuus))
        [:td {:class "shaded"}]
        (for [i (range 1 5)]
          (if-let [tulokset (-> padded-vaiheet (nth i) :tulokset)]
            (if-let [osuus (:uusiutuvan-energian-hyodynnetty-osuus tulokset)]
              [:td (str (long (Math/round (double osuus))) " %")]
              [:td])
            [:td]))]

       ;; CO2 päästöt
       (mid-header-tr (l :hiilidioksidipaastojen-muutokset))

       [:tr
        (row-header-th (l :hiilidioksidipaastojen-vahenema))
        [:td {:class "shaded"} (when-let [co2 (get-in (first padded-vaiheet) [:tulokset :co2-paastot])]
                                 (format-float co2))]
        (for [change changes]
          (if-let [muutos (:co2-muutos change)]
            [:td (format-float muutos)]
            [:td]))]]]

     ;; Energy prices table
     [:div {:style "height: 20px;"}]
     [:table {:class "shaded"}
      [:colgroup
       [:col {:style "width: 165px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 165px;"}]
       [:col {:style "width: 80px;"}]
       [:col {:style "width: 80px;"}]]

      (mid-header-tr (l :energialaskuissa-kaytetyt-hinnat))
      [:tr [:td (l :kaukolampo)]
       [:td (when-let [hinta (:kaukolampo-hinta ppp-tulokset)] (format-float hinta))]
       [:td (l :snt-kwh)]
       [:td (l :uusiutuvat-polttoaineet)]
       [:td (when-let [hinta (:uusiutuvat-pat-hinta ppp-tulokset)] (format-float hinta))]
       [:td (l :snt-kwh)]]
      [:tr [:td (l :sahko)]
       [:td (when-let [hinta (:sahko-hinta ppp-tulokset)] (format-float hinta))]
       [:td (l :snt-kwh)]
       [:td (l :fossiiliset-polttoaineet)]
       [:td (when-let [hinta (:fossiiliset-pat-hinta ppp-tulokset)] (format-float hinta))]
       [:td (l :snt-kwh)]]
      [:tr [:td]
       [:td]
       [:td]
       [:td (l :kaukojaahdytys)]
       [:td (when-let [hinta (:kaukojaahdytys-hinta ppp-tulokset)] (format-float hinta))]
       [:td (l :snt-kwh)]]]
     [:p (l :co2ekv-vahenema-huomautus)]]))
