(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [hiccup.core :as hiccup]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.config :as config]
            [solita.etp.service.localization :as loc]
            [solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot :as et-etusivu-yleistiedot]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as et-laskennallinen-ostoenergia])
  (:import [java.io ByteArrayOutputStream]))

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(defn- styles []
  (str
   "@page {
      size: A4;
      margin: 10mm;
      @bottom-center {
        content: counter(page) \" / \" counter(pages);
        font-family: roboto, sans-serif;
        font-size: 10pt;
      }
      @bottom-right {
        content: string(id-string);
        font-family: roboto, sans-serif;
        font-size: 10pt;
      }
    }

    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      padding: 0;
      font-family: roboto, sans-serif;
      font-size: 11pt;
    }

   .page {
      border: 8px solid #23323e;
      box-sizing: border-box;
      min-height: 257mm;
      position: relative;
      margin-bottom: 20mm;
    }

   .page-header {
      background-color: #23323e;
      height: 35mm;
      width: 100%;
      padding: 4mm 8mm 0 8mm;
      color: white;
      font-size: 24pt;
      font-weight: bold;
      font-family: roboto, sans-serif;
   }

    .page-title {
      color: white;
      font-size: 24pt;
      font-weight: bold;
      margin: 0;
      font-family: roboto, sans-serif;
    }

    h1 {
      font-size: 24pt;
      margin-bottom: 20px;
    }

    .id-string {
      string-set: id-string content();
      display: none;
    }"))

(defn- page-header [title]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]])

(defn- page-footer [et-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Energiatodistuksen tunnus: " et-tunnus " | " page-num "/" total-pages)])

(defn- render-page [page-data page-num total-pages et-tunnus]
  (let [{:keys [title content header]} page-data]
    [:div {:class "page"}
     (or header (page-header title))
     [:div {:class "page-content"}
      content]
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

(defn- generate-energiatodistus-ohtp-pdf
  "Use OpenHTMLToPDF to generate PDF, return as a byte array"
  [energiatodistus kieli]
  (let [_ (println "kieli:" kieli)
        output-stream (ByteArrayOutputStream.)
        l (kieli loc/et-pdf-localization kieli)

        pages [{:title (l :energiatodistus)
                  :content
                  [:div
                   ;(et-etusivu-yleistiedot/et-etusivu-yleistiedot [energiatodistus kieli alakayttotarkoitukset])
                   ;(et-laskennallinen-ostoenergia/ostoenergia energiatodistus kieli)
                   ;(et-laskennallinen-ostoenergia/ostoenergia-tiedot [energiatodistus kieli])
                   ]}]]

    (-> (generate-document-html pages (:id energiatodistus))
        (pdf-service/html->pdf output-stream))
    (-> output-stream .toByteArray)))

(defn generate-pdf [energiatodistus kieli draft?]
  (let [pdf-bytes (generate-energiatodistus-ohtp-pdf energiatodistus kieli)]
    (cond
      draft?
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes (get draft-watermark-texts kieli "LUONNOS"))
      (contains? #{"local-dev" "dev" "test"} config/environment-alias)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes (get test-watermark-texts kieli "TESTI"))
      :else pdf-bytes)))
