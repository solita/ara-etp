(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [hiccup.core :as hiccup]
            [clojure.java.io :as io]
            [solita.etp.config :as config]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.localization :as loc]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot :as et-etusivu-yleistiedot]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as et-laskennallinen-ostoenergia]
            [solita.etp.service.energiatodistus-pdf.etusivu-eluku :as et-etusivu-eluku]
            [solita.etp.service.energiatodistus-pdf.etusivu-grafiikka :as et-etusivu-grafiikka])
  (:import [java.io ByteArrayOutputStream]))

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(defn- styles []
  (let [res (or (io/resource "energiatodistus-2026.css")
                (throw (ex-info "Styles resource 'energiatodistus-2026.css' not found on classpath"
                                {:resource "energiatodistus-2026.css"})))]
    (slurp res)))

(defn- page-header [title subtitle]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]
   [:p {:class "page-subtitle"} subtitle]])

(defn- page-footer [et-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Todistustunnus " et-tunnus " | " page-num " / " total-pages)])

(defn- render-page [page-data page-num total-pages et-tunnus]
  (let [{:keys [title subtitle content header]} page-data]
    [:div {:class "page"}
     [:div {:class "page-border-container"}
      (or header (page-header title subtitle))
      [:div {:class "page-content"}
       content]]
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


(defn generate-energiatodistus-html
  "Use OpenHTMLToPDF to generate PDF, return as a byte array"
  [{:keys [energiatodistus kieli] :as params}]
  (let [l (kieli loc/et-pdf-localization)
        pages [{:title (l :energiatodistus)
                :subtitle (l :energiatodistus-2026-subtitle)
                :content
                [:div
                 [:div {:class "page-section"}
                  (et-etusivu-yleistiedot/et-etusivu-yleistiedot params)]
                 [:div {:class "page-section"}
                  (et-etusivu-grafiikka/et-etusivu-grafiikka params)
                  (et-etusivu-eluku/et-etusivu-eluku-teksti params)]
                 [:div {:class "page-section"}
                  (et-laskennallinen-ostoenergia/ostoenergia params)
                  (et-laskennallinen-ostoenergia/ostoenergia-tiedot params)]]}]]

    (generate-document-html pages (:id energiatodistus))))

(defn- generate-energiatodistus-ohtp-pdf
  "Use OpenHTMLToPDF to generate a energiatodistus PDF, return as a byte array"
  [params]
  (let [output-stream (ByteArrayOutputStream.)]
    (-> (generate-energiatodistus-html params)
        (pdf-service/html->pdf output-stream))
    (-> output-stream .toByteArray)))

(defn generate-energiatodistus-pdf
  "Generate a energiatodistus PDF and return it as a byte array."
  [energiatodistus alakayttotarkoitukset laatimisvaiheet kieli draft?]
  (let [kieli-keyword (keyword kieli)
        pdf-bytes

        ;; Generate the PDF to byte array
        (generate-energiatodistus-ohtp-pdf
          {:energiatodistus       energiatodistus
           :alakayttotarkoitukset alakayttotarkoitukset
           :laatimisvaiheet       laatimisvaiheet
           :kieli                 kieli-keyword})
        watermark-text (cond
                         draft? (draft-watermark-texts kieli)
                         (contains? #{"local-dev" "dev" "test"} config/environment-alias) (test-watermark-texts kieli)
                         :else nil)]

    (if (some? watermark-text)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes watermark-text)
      pdf-bytes)))
