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
     [:h2 {:style "font-size: 22px; margin: 0 0 12px 0; color: #2c5234;"} title-text]
     [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
      [:tr
       ;; Left column
       [:td {:style "width: 50%; padding-right: 21px; vertical-align: top;"}
        [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
         (map-indexed
          (fn [idx item]
            [:tr
             [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
              (inc idx)]
             [:td {:style "border: 1px solid #2c5234; padding: 10px 14px; font-size: 16px;"}
              (or (get item label-key) "")]])
          col1)]]
       ;; Right column
       [:td {:style "width: 50%; padding-left: 21px; vertical-align: top;"}
        [:table {:role "presentation" :style "width: 100%; border-collapse: collapse;"}
         (map-indexed
          (fn [idx item]
            [:tr
             [:td {:style "border: 1px solid #2c5234; background-color: #2c5234; color: #ffffff; font-weight: bold; text-align: center; width: 40px; padding: 10px 0;"}
              (+ 4 idx)]
             [:td {:style "border: 1px solid #2c5234; padding: 10px 14px; font-size: 16px;"}
              (or (get item label-key) "")]])
          col2)]]]]]))

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
                 (render-toimenpide-ehdotukset vaihe kieli l)]}
      {:title (format (l :vaiheessa-n-toteutettavat-toimenpiteet) vaihe-nro)
       :content [:div
                 [:p (str "Vaihetta " vaihe-nro " ei määritelty.")]]})))
