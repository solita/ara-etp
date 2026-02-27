(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [solita.etp.config :as config]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.localization :as loc]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf]
            [solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot :as et-etusivu-yleistiedot]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as et-laskennallinen-ostoenergia]
            [solita.etp.service.energiatodistus-pdf.etusivu-eluku :as et-etusivu-eluku]
            [solita.etp.service.energiatodistus-pdf.etusivu-grafiikka :as et-etusivu-grafiikka]
            [solita.etp.service.energiatodistus-pdf.koontisivu :as koontisivu]
            [solita.etp.service.energiatodistus-pdf.lahtotiedot :as et-lahtotiedot]
            [solita.etp.service.energiatodistus-pdf.tulokset :as et-tulokset]
            [solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset_rakennuksen_vaippa :as te-rakennusvaippa]
            [solita.etp.service.energiatodistus-pdf.toimenpide_ehdotukset_lammitys_ilmanvaihto :as te-lammitys-ilmanvaihto]
            [solita.etp.service.energiatodistus-pdf.toimenpide-ehdotukset-muut :as te-muut]
            [solita.etp.service.energiatodistus-pdf.lisamerkintoja :as et-lisamerkintoja])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))


(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(defn- styles []
  (slurp (io/resource "energiatodistus-2026.css")))

(defn- page-header [title subtitle]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]
   [:p {:class "page-subtitle"} subtitle]])

(defn- page-footer [et-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Todistustunnus " et-tunnus " | " page-num " / " total-pages)])

(defn- wrap-page-border [page-border? content]
       (if page-border?
         [:div {:class "page-border-container"} content]
         content))

(defn- render-page [page-data page-num total-pages et-tunnus]
  (let [{:keys [content page-border?]} page-data]
    [:div {:class "page"}
     (wrap-page-border
       page-border?
       content)
     (page-footer et-tunnus page-num total-pages)]))

(defn generate-document-html
  "Generate complete HTML document for Energiatodistus 2026 PDF."
  [pages et-tunnus]
  (let [total-pages (count pages)
        pages-html (map-indexed
                     (fn [idx page-data]
                       (render-page page-data (inc idx) total-pages et-tunnus))
                     pages)]
  (hiccup/html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:title "Energiatodistus"]
     [:style (styles)]]
    [:body
     pages-html]])))

(defn- show-toimenpide-pages? [energiatodistus]
  (let [e-luokka (-> energiatodistus :tulokset :e-luokka)
        has-valid-ppp? (-> energiatodistus :perusparannuspassi-valid)]
    (and (not (contains? #{"A" "A0" "A+"} e-luokka))
         (not has-valid-ppp?))))


(defn generate-energiatodistus-html
  "Use OpenHTMLToPDF to generate PDF, return as a byte array"
  [{:keys [energiatodistus kieli] :as params}]
  (let [l (kieli loc/et-pdf-localization)
        show-toimenpide? (show-toimenpide-pages? energiatodistus)
        pages (concat
                [{:page-border? true
                  :content
                  [:div
                   (page-header (l :energiatodistus) (l :energiatodistus-2026-subtitle))
                   [:div {:class "page-section"}
                    (et-etusivu-yleistiedot/et-etusivu-yleistiedot params)]
                   [:div {:class "page-section"}
                    [:div {:class "etusivu-grafiikka-eluku-section"}
                     (et-etusivu-grafiikka/et-etusivu-grafiikka params)
                     (et-etusivu-eluku/et-etusivu-eluku-teksti params)]]
                   [:div {:class "page-section"}
                    [:div {:class "etusivu-ostoenergia-section"}
                     (et-laskennallinen-ostoenergia/ostoenergia params)
                     (et-laskennallinen-ostoenergia/ostoenergia-tiedot params)]]]}
                 (koontisivu/koontisivu params)]
                (if show-toimenpide?
                  [{:content
                    (te-rakennusvaippa/generate-all-toimepide-ehdotukset-rakennuksen-vaippa params)}
                   {:content
                    (te-lammitys-ilmanvaihto/generate-all-toimepide-ehdotukset-rakennuksen-vaippa params)}
                   {:content
                    (te-muut/generate-all-toimepide-ehdotukset-muut params)}]
                  [])
                [{:page-border? false
                  :content (et-lahtotiedot/lahtotiedot-page-content params)}
                 {:page-border? false
                  :content (et-tulokset/tulokset-page-content params)}
                 {:page-border? false
                  :content
                  [:div {:class "page-section"}
                   (et-lisamerkintoja/generate-lisamerkintoja params)]}])]
    (generate-document-html pages (:id energiatodistus))))

(defn generate-energiatodistus-ohtp-pdf
  "Use OpenHTMLToPDF to generate a energiatodistus PDF, return as a byte array"
  [params ppp-pdf-bytes]
  (let [output-stream (ByteArrayOutputStream.)
        html (generate-energiatodistus-html params)]
    (-> html
        (pdf-service/html->pdf output-stream))
    (let [et-pdf-bytes (.toByteArray output-stream)]
      (if (some? ppp-pdf-bytes)
        (pdf-service/merge-pdf
          [(ByteArrayInputStream. et-pdf-bytes)
           (ByteArrayInputStream. ppp-pdf-bytes)])
        et-pdf-bytes))))

(defn generate-energiatodistus-pdf
  "Generate the full energiatodistus 2026 PDF, including perusparannuspassi if present.
   This is the db-less variant — all data must be passed in as parameters.
   luokittelut is a map of classification data with keys:
     :alakayttotarkoitukset, :kayttotarkoitukset, :laatimisvaiheet,
     :mahdollisuus-liittya, :uusiutuva-energia, :lammitysmuodot,
     :ilmanvaihtotyypit, :toimenpide-ehdotukset"
  [complete-energiatodistus complete-perusparannuspassi kieli draft? luokittelut]
  (let [{:keys [alakayttotarkoitukset kayttotarkoitukset laatimisvaiheet]} luokittelut
        kieli-keyword (keyword kieli)
        ppp-pdf-bytes (when complete-perusparannuspassi
                        (ppp-pdf/generate-perusparannuspassi-pdf
                          complete-perusparannuspassi
                          complete-energiatodistus
                          luokittelut
                          kieli
                          draft?))
        pdf-bytes
        ;; Generate the PDF to byte array
        (generate-energiatodistus-ohtp-pdf
          {:energiatodistus       complete-energiatodistus
           :alakayttotarkoitukset alakayttotarkoitukset
           :laatimisvaiheet       laatimisvaiheet
           :kieli                 kieli-keyword
           :kayttotarkoitukset    kayttotarkoitukset}
          ppp-pdf-bytes)
        watermark-text (cond
                         draft? (draft-watermark-texts kieli)
                         (contains? #{"local-dev" "dev" "test"} config/environment-alias) (test-watermark-texts kieli)
                         :else nil)]

    (if (some? watermark-text)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes watermark-text)
      pdf-bytes)))
