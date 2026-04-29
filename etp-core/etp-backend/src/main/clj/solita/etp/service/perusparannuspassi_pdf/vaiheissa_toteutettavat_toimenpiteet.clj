(ns solita.etp.service.perusparannuspassi-pdf.vaiheissa-toteutettavat-toimenpiteet
  (:require [clojure.string :as str]
            [hiccup.core :refer [h]]
            [solita.etp.service.localization :as loc]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :as tv]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]))

(defn- get-vaihe-data [params vaihe-nro]
  (let [{:keys [perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset]} params
        vaiheet (:vaiheet perusparannuspassi)
        vaihe (first (filter #(= (:vaihe-nro %) vaihe-nro) vaiheet))
        seuraava-vaihe (first (filter #(= (:vaihe-nro %) (inc vaihe-nro)) vaiheet))]
    ;; Only return vaihe data if it has a starting date
    ;; This filters out vaiheet where data was added but later deleted
    (when (and vaihe (some? (get-in vaihe [:tulokset :vaiheen-alku-pvm])))
      (let [versio 2026
            passin-perustiedot (:passin-perustiedot perusparannuspassi)
            e-luku (e-luokka-service/e-luku-from-ppp-vaihe versio energiatodistus vaihe)
            e-luokka (-> (e-luokka-service/e-luokka kayttotarkoitukset alakayttotarkoitukset versio
                                                    (get-in energiatodistus [:perustiedot :kayttotarkoitus])
                                                    (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
                                                    e-luku
                                                    (:tayttaa-aplus-vaatimukset passin-perustiedot)
                                                    (:tayttaa-a0-vaatimukset passin-perustiedot))
                         :e-luokka)]
        {:vaihe vaihe
         :seuraava-vaihe seuraava-vaihe
         :e-luku e-luku
         :e-luokka e-luokka}))))

(def ^:private table-style "width: 100%; border-collapse: collapse; table-layout: fixed;")
(def ^:private cell-style "border: 1px solid #2c5234; padding: 6px 8px;")
(def ^:private header-cell-style "width: 20%; border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: left; padding: 10px 5px; font-size: 14px;")
(def ^:private number-cell-style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;")
(def ^:private row-header-style "border: 1px solid #2c5234; padding: 6px 8px; text-align: left; font-weight: normal; width: 70%;")

(defn- format-year-range [vaihe seuraava-vaihe]
  (let [alku-pvm (get-in vaihe [:tulokset :vaiheen-alku-pvm])
        seuraava-alku-pvm (when seuraava-vaihe
                            (get-in seuraava-vaihe [:tulokset :vaiheen-alku-pvm]))
        start-year (cond
                     (number? alku-pvm) (int alku-pvm)
                     alku-pvm (.getYear alku-pvm)
                     :else nil)
        next-start-year (cond
                          (number? seuraava-alku-pvm) (int seuraava-alku-pvm)
                          seuraava-alku-pvm (.getYear seuraava-alku-pvm)
                          :else nil)
        end-year (if next-start-year
                   (dec next-start-year)
                   ;; For the last vaihe, use 2050 as the end year
                   (when start-year 2050))]
    (if (and start-year end-year)
      (str " " start-year "–" end-year)
      (if start-year (str " " start-year) ""))))

(defn- render-toimenpide-ehdotukset [vaihe seuraava-vaihe kieli l toimenpide-ehdotukset-defs]
  (let [toimenpide-ehdotukset-items (get-in vaihe [:toimenpiteet :toimenpide-ehdotukset])
        label-key (if (= (keyword kieli) :sv) :label-sv :label-fi)
        toimenpide-ehdotukset (map (fn [item]
                                     (let [id (if (map? item) (:id item) item)
                                           def (first (filter #(= (:id %) id) toimenpide-ehdotukset-defs))]
                                       (if def
                                         (or (get def label-key)
                                             (get def :label-fi)
                                             id)
                                         item)))
                                   toimenpide-ehdotukset-items)
        slots (take 6 (concat toimenpide-ehdotukset (repeat nil)))
        [col1 col2] (split-at 3 slots)
        title-suffix (format-year-range vaihe seuraava-vaihe)
        title-text (str (l :toimenpide-ehdotukset) title-suffix)]
    [:div
     [:h2 {:style "margin-top: 0;"} title-text]
     [:table {:role "presentation" :style table-style}
      (map-indexed
       (fn [idx item1]
         (let [item2 (nth col2 idx)]
           [:tr
            ;; Left Item
            [:td {:style number-cell-style}
             (inc idx)]
            [:td {:style (str cell-style " width: 40%; vertical-align: middle;")}
             (or item1 "\u00A0")]

            ;; Spacer
            [:td {:style "width: 42px; border: none;"}]

            ;; Right Item
            [:td {:style number-cell-style}
             (+ 4 idx)]
            [:td {:style (str cell-style " width: 40%; vertical-align: middle;")}
             (or item2 "\u00A0")]]))
       col1)]]))

(defn- render-toimenpideseloste [vaihe kieli l]
  (let [seloste-key (if (= kieli :sv) :toimenpideseloste-sv :toimenpideseloste-fi)
        seloste (get-in vaihe [:toimenpiteet seloste-key])]
    (when (not-empty seloste)
      [:div {:style "margin-top: 24px;"}
       [:h2 (l :toimenpideseloste)]
       [:p {:style "white-space: pre-line; overflow-wrap: break-word;"} (h seloste)]])))

(defn- render-energiankulutuksen-muutos [vaihe l]
  (let [tulokset (:tulokset vaihe)]
    [:div {:style "margin-top: 24px;"}
     [:h2 (l :energiankulutus-vaiheen-jalkeen)]
     [:table {:role "presentation" :style table-style}
      [:tr
       (for [header [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
         [:th {:style header-cell-style}
          (l header)])]
      [:tr
       (for [key [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
         [:td {:style cell-style}
          (or (get tulokset (get perusparannuspassi-service/energy-keys-laskennallinen key)) "-")])]]]))

(defn- parse-double-safe [v]
  (cond
    (number? v) (double v)
    (string? v)
    (try
      (Double/parseDouble v)
      (catch NumberFormatException _
        nil))
    :else nil))

(defn- format-2dp [v]
  (when-let [d (parse-double-safe v)]
    (format "%.2f" d)))

(defn- render-energiankulutus-kustannukset-ja-co2-paastot [vaihe l]
  (let [tulokset (:tulokset vaihe)]
    [:div {:style "margin-top: 24px;"}
     (let [[before after] (str/split (l :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen) #"\{subscript\}")]
       [:h2 before [:sub "2"] after])
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
      (for [[label value unit] [[(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen) (get tulokset :ostoenergia) (l :kwh-vuosi)]
                                [(l :uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta) (get tulokset :uusiutuvan-energian-hyodynnetty-osuus) (l :prosenttia)]
                                [(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus) (get tulokset :toteutunut-ostoenergia) (l :kwh-vuosi)]
                                [(l :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio) (-> tulokset :toteutunut-energia-kustannukset format-2dp) (l :euroa-vuosi)]
                                [(l :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen) (-> tulokset :co2-paastot format-2dp) (l :tco2ekv-vuosi)]]]
        [:tr
         [:th {:scope "row" :style row-header-style} label]
         [:td {:style (str cell-style " width: 12%;")} (or value "-")]
         [:td {:style (str cell-style " width: 18%;")} unit]])]]))

(defn render-page [params vaihe-nro]
  (let [{:keys [kieli toimenpide-ehdotukset]} params
        l (kieli loc/ppp-pdf-localization)
        vaihe-data (get-vaihe-data params vaihe-nro)]
    ;; Only render the page if vaihe has data (including a start date)
    ;; Return nil for vaiheet without data so they can be filtered out
    (when vaihe-data
      (let [{:keys [vaihe e-luku e-luokka]} vaihe-data
            title-text (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)
            parts (clojure.string/split title-text (re-pattern (str "(?<=" vaihe-nro ")")))]
        {:title [:table {:style "width: 100%; border-collapse: collapse; margin-top: -10px;"}
                 [:tr
                  [:td {:style "vertical-align: top;"}
                   (if (> (count parts) 1)
                     [:span (first parts) [:br] (clojure.string/join "" (rest parts))]
                     title-text)]
                  [:td {:style "text-align: right; vertical-align: top;"}
                   (let [color (get tv/colors-by-e-luokka e-luokka "#e8b63e")
                         vaihe-title (str (l :vaihe) " " vaihe-nro)
                         perf-label (str e-luokka " - " e-luku)]
                     [:svg {:xmlns "http://www.w3.org/2000/svg"
                            :viewBox "-6 -60 120 65"
                            :width "120px"
                            :height "65px"}
                      (tv/arrow color 0 vaihe-title perf-label)])]]]
       :content [:table {:style "width: 100%; height: 100%; border-collapse: collapse;"}
                 [:tr
                  [:td {:style "vertical-align: top;"}
                   (render-toimenpide-ehdotukset vaihe (:seuraava-vaihe vaihe-data) kieli l toimenpide-ehdotukset)
                   (render-toimenpideseloste vaihe kieli l)]]
                 [:tr {:style "height: 100%;"}
                  [:td]]
                 [:tr
                  [:td {:style "vertical-align: bottom;"}
                   (render-energiankulutuksen-muutos vaihe l)
                   (render-energiankulutus-kustannukset-ja-co2-paastot vaihe l)]]]}))))
