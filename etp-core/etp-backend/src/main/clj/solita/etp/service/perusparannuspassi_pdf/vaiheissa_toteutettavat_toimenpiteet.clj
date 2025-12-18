(ns solita.etp.service.perusparannuspassi-pdf.vaiheissa-toteutettavat-toimenpiteet
  (:require [solita.etp.service.localization :as loc]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :as tv]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]))

(defn- get-vaihe-data [params vaihe-nro]
  (let [{:keys [perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset]} params
        vaiheet (:vaiheet perusparannuspassi)
        vaihe (first (filter #(= (:vaihe-nro %) vaihe-nro) vaiheet))
        seuraava-vaihe (first (filter #(= (:vaihe-nro %) (inc vaihe-nro)) vaiheet))]
    (when vaihe
      (let [versio 2018
            e-luku (e-luokka-service/e-luku-from-ppp-vaihe versio energiatodistus vaihe)
            e-luokka (-> (e-luokka-service/e-luokka kayttotarkoitukset alakayttotarkoitukset versio
                                                    (get-in energiatodistus [:perustiedot :kayttotarkoitus])
                                                    (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
                                                    e-luku)
                         :e-luokka)]
        {:vaihe vaihe
         :seuraava-vaihe seuraava-vaihe
         :e-luku e-luku
         :e-luokka e-luokka}))))

(defn- unwrap-monet [v]
  (if (and (map? v) (or (contains? v :val) (contains? v :value)))
    (let [val (or (:val v) (:value v))]
      (unwrap-monet val))
    v))

(defn- render-toimenpide-ehdotukset [vaihe seuraava-vaihe kieli l toimenpide-ehdotukset-defs]
  (let [toimenpide-ehdotukset-items (->> (get-in vaihe [:toimenpiteet :toimenpide-ehdotukset])
                                         (map unwrap-monet))
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
        alku-pvm (unwrap-monet (get-in vaihe [:tulokset :vaiheen-alku-pvm]))
        seuraava-alku-pvm (if seuraava-vaihe
                            (unwrap-monet (get-in seuraava-vaihe [:tulokset :vaiheen-alku-pvm]))
                            nil)
        vuosi (cond
                (number? alku-pvm) (int alku-pvm)
                alku-pvm (.getYear alku-pvm)
                :else nil)
        seuraava-vuosi (cond
                         (number? seuraava-alku-pvm) (int seuraava-alku-pvm)
                         seuraava-alku-pvm (.getYear seuraava-alku-pvm)
                         :else nil)
        end-year (if seuraava-vuosi
                   (dec seuraava-vuosi)
                   (if vuosi (+ vuosi 4) nil))
        title-suffix (if (and vuosi end-year)
                       (str " " vuosi "–" end-year)
                       (if vuosi (str " " vuosi) ""))
        title-text (str (l :toimenpide-ehdotukset) title-suffix)]
    [:div
     [:h2 {:style "margin-top: 0;"} title-text]
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse; table-layout: fixed;"}
      (map-indexed
       (fn [idx item1]
         (let [item2 (nth col2 idx)]
           [:tr
            ;; Left Item
            [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
             (inc idx)]
            [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 40%; vertical-align: middle;"}
             (or item1 "\u00A0")]

            ;; Spacer
            [:td {:style "width: 42px; border: none;"}]

            ;; Right Item
            [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
             (+ 4 idx)]
            [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 40%; vertical-align: middle;"}
             (or item2 "\u00A0")]]))
       col1)]]))

(defn- render-toimenpideseloste [vaihe kieli l]
  (let [seloste-key (if (= kieli :sv) :toimenpideseloste-sv :toimenpideseloste-fi)
        seloste (get-in vaihe [:toimenpiteet seloste-key])]
    (when (not-empty seloste)
      [:div {:style "margin-top: 24px;"}
       [:h2 (l :toimenpideseloste)]
       [:p seloste]])))

(defn- render-energiankulutuksen-muutos [vaihe l]
  (let [tulokset (:tulokset vaihe)]
    [:div {:style "margin-top: 24px;"}
     [:h2 (l :energiankulutuksen-muutos-lahtotilanteesta)]
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse; table-layout: fixed;"}
      [:tr
       (for [header [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
         [:th {:style "width: 20%; border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: left; padding: 10px 5px; font-size: 14px;"}
          (l header)])]
      [:tr
       (for [key [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
         [:td {:style "border: 1px solid #2c5234; padding: 6px 8px;"}
          (or (unwrap-monet (get tulokset (get perusparannuspassi-service/energy-keys-laskennallinen key))) "-")])]]]))

(defn- render-energiankulutus-kustannukset-ja-co2-paastot [vaihe l]
  (let [tulokset (:tulokset vaihe)
        get-val (fn [k] (or (unwrap-monet (get tulokset k)) 0))
        ostoenergian-kokonaistarve (reduce + (map get-val (vals perusparannuspassi-service/energy-keys-laskennallinen)))
        toteutunut-ostoenergian-kulutus (reduce + (map get-val perusparannuspassi-service/energy-keys-toteutunut))
        toteutunut-energiakustannus (or (unwrap-monet (get tulokset :toteutunut-energiakustannus))
                                        (reduce + (for [[short-key _] perusparannuspassi-service/energy-keys-laskennallinen]
                                                    (let [toteutunut-key (keyword (str "toteutunut-ostoenergia-" (name short-key)))
                                                          energy (get-val toteutunut-key)
                                                          price-key (get perusparannuspassi-service/price-keys short-key)
                                                          price (get-val price-key)]
                                                      (* energy price 0.01)))))
        hiilidioksidipaastot (or (unwrap-monet (get tulokset :hiilidioksidipaastot))
                                 (reduce + (for [[short-key laskennallinen-key] perusparannuspassi-service/energy-keys-laskennallinen]
                                             (let [energy (get-val laskennallinen-key)
                                                   factor (get perusparannuspassi-service/paastokertoimet short-key 0)]
                                               (* energy factor)))))]
    [:div {:style "margin-top: 24px;"}
     [:h2 (l :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen)]
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
      (for [[label value unit] [[(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen) ostoenergian-kokonaistarve (l :kwh-vuosi)]
                                [(l :uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta) (unwrap-monet (get tulokset :uusiutuvan-energian-hyodynnetty-osuus)) (l :prosenttia)]
                                [(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus) toteutunut-ostoenergian-kulutus (l :kwh-vuosi)]
                                [(l :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio) (format "%.2f" (double toteutunut-energiakustannus)) (l :euroa-vuosi)]
                                [(l :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen) (format "%.2f" (double hiilidioksidipaastot)) (l :tco2ekv-vuosi)]]]
        [:tr
         [:th {:scope "row" :style "border: 1px solid #2c5234; padding: 6px 8px; text-align: left; font-weight: normal; width: 70%;"} label]
         [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 12%;"} (or value "-")]
         [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 18%;"} unit]])]]))

(defn render-page [params vaihe-nro]
  (let [{:keys [kieli toimenpide-ehdotukset]} params
        l (kieli loc/ppp-pdf-localization)
        vaihe-data (get-vaihe-data params vaihe-nro)]
    (if vaihe-data
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
                   (render-energiankulutus-kustannukset-ja-co2-paastot vaihe l)]]]})
      {:title (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)
       :content [:div
                 [:p (str "Vaihetta " vaihe-nro " ei määritelty.")]]})))
