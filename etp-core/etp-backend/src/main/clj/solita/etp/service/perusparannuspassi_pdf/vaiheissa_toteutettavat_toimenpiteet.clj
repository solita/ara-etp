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
        title-text (str (l :toimenpide-ehdotukset) title-suffix)
        render-column (fn [items start-index column-class]
                        [:div {:class column-class}
                         (into
                          [:dl {:class "ppp-vaihe-ehdotukset-list" :role "presentation"}]
                          (map-indexed
                           (fn [idx item]
                             [:div
                              [:dt {:class "ppp-vaihe-ehdotukset-number"}
                               (str (+ start-index idx))]
                              [:dd {:class "ppp-vaihe-ehdotukset-item"}
                               (or item "\u00A0")]])
                           items))])]
    [:div {:class "ppp-vaihe-no-top-margin"}
     [:h2 {:class "ppp-vaihe-no-top-margin"} title-text]
     [:div {:class "ppp-vaihe-ehdotukset-columns"}
      (render-column col1 1 "ppp-vaihe-ehdotukset-column ppp-vaihe-ehdotukset-column-left")
      (render-column col2 4 "ppp-vaihe-ehdotukset-column ppp-vaihe-ehdotukset-column-right")]]))

(defn- render-toimenpideseloste [vaihe kieli l]
  (let [seloste-key (if (= kieli :sv) :toimenpideseloste-sv :toimenpideseloste-fi)
        seloste (get-in vaihe [:toimenpiteet seloste-key])]
    [:div {:class "ppp-vaihe-section"}
     [:h2 (l :toimenpideseloste)]
      [:p {:class "ppp-vaihe-toimenpideseloste"}
       (h (or seloste ""))]]))

(defn- render-energiankulutuksen-muutos [vaihe l]
  (let [tulokset (:tulokset vaihe)]
    [:div {:class "ppp-vaihe-section"}
     [:h2 (l :energiankulutus-vaiheen-jalkeen)]
     (into
      [:dl {:class "ppp-vaihe-energiankulutuksen-list" :role "presentation"}]
      (for [key [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
        [:div
         [:dt {:class "ppp-vaihe-energiankulutuksen-label"}
          (l key)]
         [:dd {:class "ppp-vaihe-energiankulutuksen-value"}
          (or (get tulokset (get perusparannuspassi-service/energy-keys-laskennallinen key)) "-")]]))]))

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
    [:div {:class "ppp-vaihe-section ppp-vaihe-energiankulutus-kustannukset-ja-co2-paastot"}
       [:h2 (l :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen)]
     [:table {:class "ppp-vaihe-kustannukset-table" :role "presentation"}
      (for [[label value unit] [[(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen) (get tulokset :ostoenergia) (l :kwh-vuosi)]
                                [(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus) (get tulokset :toteutunut-ostoenergia) (l :kwh-vuosi)]
                                [(l :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio) (-> tulokset :toteutunut-energia-kustannukset format-2dp) (l :euroa-vuosi)]
                                [(l :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen) (-> tulokset :co2-paastot format-2dp) (l :tco2ekv-vuosi)]]]
        [:tr
         [:th {:class "ppp-vaihe-row-header" :scope "row"} label]
         [:td {:class "ppp-vaihe-cell ppp-vaihe-value-cell"} (or value "-")]
         [:td {:class "ppp-vaihe-cell ppp-vaihe-unit-cell"} unit]])]
     [:p {:class "ppp-vaihe-kustannukset-info"} (l :katso-hinnat-info)]]))

(defn render-page [params vaihe-nro]
  (let [{:keys [kieli toimenpide-ehdotukset]} params
        l (kieli loc/ppp-pdf-localization)
        vaihe-data (get-vaihe-data params vaihe-nro)]
    ;; Only render the page if vaihe has data (including a start date)
    ;; Return nil for vaiheet without data so they can be filtered out
    (when vaihe-data
      (let [{:keys [vaihe e-luku e-luokka]} vaihe-data
            title-text (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)
            parts (str/split title-text (re-pattern (str "(?<=" vaihe-nro ")")))]
        {:title [:table {:class "ppp-vaihe-title-table"}
                 [:tr
                  [:td {:class "ppp-vaihe-top-align"}
                   (if (> (count parts) 1)
                     [:span (first parts) [:br] (clojure.string/join "" (rest parts))]
                     title-text)]
                  [:td {:class "ppp-vaihe-title-right"}
                   (let [color (get tv/colors-by-e-luokka e-luokka "#e8b63e")
                         vaihe-title (str (l :vaihe) " " vaihe-nro)
                         perf-label (str e-luokka " - " e-luku)]
                     [:svg {:xmlns "http://www.w3.org/2000/svg"
                            :viewBox "-6 -60 120 65"
                            :width "38.2mm"
                            :alt (format (l :vaihe-arrow-alt-text) vaihe e-luokka vaihe e-luku)
                            :height "20.5mm"}
                      (tv/arrow color 0 vaihe-title perf-label)])]]]
         :title-class "vaihe-title"
        :content [:div {:class "ppp-vaihe-no-top-margin"}
                   (render-toimenpide-ehdotukset vaihe (:seuraava-vaihe vaihe-data) kieli l toimenpide-ehdotukset)
                   (render-toimenpideseloste vaihe kieli l)
                   (render-energiankulutuksen-muutos vaihe l)
                   (render-energiankulutus-kustannukset-ja-co2-paastot vaihe l)]}))))
