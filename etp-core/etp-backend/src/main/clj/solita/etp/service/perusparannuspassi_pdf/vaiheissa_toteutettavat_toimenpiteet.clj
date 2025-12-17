(ns solita.etp.service.perusparannuspassi-pdf.vaiheissa-toteutettavat-toimenpiteet
  (:require [solita.etp.service.localization :as loc]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :as tv]))

(defn- get-vaihe-data [params vaihe-nro]
  (let [{:keys [perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset]} params
        vaihe (first (filter #(= (:vaihe-nro %) vaihe-nro) (:vaiheet perusparannuspassi)))]
    (when vaihe
      (let [versio 2018
            e-luku (e-luokka-service/e-luku-from-ppp-vaihe versio energiatodistus vaihe)
            e-luokka (-> (e-luokka-service/e-luokka kayttotarkoitukset alakayttotarkoitukset versio
                                                    (get-in energiatodistus [:perustiedot :kayttotarkoitus])
                                                    (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
                                                    e-luku)
                         :e-luokka)]
        {:vaihe vaihe
         :e-luku e-luku
         :e-luokka e-luokka}))))

(defn- render-toimenpide-ehdotukset [vaihe kieli l]
  (let [toimenpide-ehdotukset (get-in vaihe [:toimenpiteet :toimenpide-ehdotukset])
        label-key (if (= kieli :sv) :label-sv :label-fi)
        slots (take 6 (concat toimenpide-ehdotukset (repeat nil)))
        [col1 col2] (split-at 3 slots)
        alku-pvm (get-in vaihe [:tulokset :vaiheen-alku-pvm])
        vuosi (if alku-pvm (.getYear alku-pvm) nil)
        title-suffix (if vuosi (str " " vuosi "–" (+ vuosi 3)) "")
        title-text (str (l :toimenpide-ehdotukset) title-suffix)]
    [:div
     [:h2 title-text]
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse; table-layout: fixed;"}
      (map-indexed
       (fn [idx item1]
         (let [item2 (nth col2 idx)]
           [:tr
            ;; Left Item
            [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
             (inc idx)]
            [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 40%; vertical-align: top;"}
             (or (get item1 label-key) "\u00A0")]

            ;; Spacer
            [:td {:style "width: 42px; border: none;"}]

            ;; Right Item
            [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
             (+ 4 idx)]
            [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 40%; vertical-align: top;"}
             (or (get item2 label-key) "\u00A0")]]))
       col1)]]))

(defn- render-toimenpideseloste [l]
  [:div {:style "margin-top: 24px;"}
   [:h2 (l :toimenpideseloste)]
   [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."]])

(defn- render-energiankulutuksen-muutos [l]
  [:div {:style "margin-top: 24px;"}
   [:h2 (l :energiankulutuksen-muutos-lahtotilanteesta)]
   [:table {:role "presentation" :style "width: 100%; border-collapse: collapse; table-layout: fixed;"}
    [:tr
     (for [header [:kaukolampo :sahko :uusiutuvat-pat :fossiiliset-pat :kaukojaahdytys]]
       [:th {:style "width: 20%; border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: left; padding: 10px 5px; font-size: 14px;"}
        (l header)])]
    [:tr
     (for [_ (range 5)]
       [:td {:style "border: 1px solid #2c5234; padding: 6px 8px;"} "100,000"])]]])

(defn- render-energiankulutus-kustannukset-ja-co2-paastot [l]
  [:div {:style "margin-top: 24px;"}
   [:h2 (l :energiankulutus-kustannukset-ja-co2-paastot-vaiheen-jalkeen)]
   [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
    (for [[label value unit] [[(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-laskennallinen) "-520 480" (l :kwh-vuosi)]
                              [(l :uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta) "3" (l :prosenttia)]
                              [(l :ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus) "453 600" (l :kwh-vuosi)]
                              [(l :toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio) "40 876" (l :euroa-vuosi)]
                              [(l :energiankaytosta-aiheutuvat-hiilidioksidipaastot-laskennallinen) "30,13" (l :tco2ekv-vuosi)]]]
      [:tr
       [:th {:scope "row" :style "border: 1px solid #2c5234; padding: 6px 8px; text-align: left; font-weight: normal; width: 70%;"} label]
       [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 12%;"} value]
       [:td {:style "border: 1px solid #2c5234; padding: 6px 8px; width: 18%;"} unit]])]])

(defn render-page [{:keys [kieli] :as params} vaihe-nro]
  (let [l (kieli loc/ppp-pdf-localization)
        {:keys [vaihe e-luku e-luokka]} (get-vaihe-data params vaihe-nro)]
    (if vaihe
      {:header [:div {:class "page-header" :style "height: auto; padding: 0;"}
                [:table {:style "width: 100%; border-collapse: collapse;"}
                 [:tr
                  [:td {:style "padding: 78px 0 78px 16mm; vertical-align: middle; width: 70%;"}
                   [:h1 {:class "page-title" :style "margin: 0;"} (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)]]
                  [:td {:style "padding: 78px 16mm 78px 0; vertical-align: middle; width: 30%; text-align: right;"}
                   (let [color (get tv/colors-by-e-luokka e-luokka "#e8b63e")
                         vaihe-title (str (l :vaihe) " " vaihe-nro)
                         perf-label (str e-luokka " - " e-luku)]
                     [:svg {:xmlns "http://www.w3.org/2000/svg"
                            :viewBox "-6 -60 120 65"
                            :width "120px"
                            :height "65px"}
                      (tv/arrow color 0 vaihe-title perf-label)])]]]]
       :content [:div
                 (render-toimenpide-ehdotukset vaihe kieli l)
                 (render-toimenpideseloste l)
                 (render-energiankulutuksen-muutos l)
                 (render-energiankulutus-kustannukset-ja-co2-paastot l)]}
      {:title (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)
       :content [:div
                 [:p (str "Vaihetta " vaihe-nro " ei määritelty.")]]})))
