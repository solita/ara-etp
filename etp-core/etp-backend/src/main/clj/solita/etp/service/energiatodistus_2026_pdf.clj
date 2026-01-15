(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [hiccup.core :as hiccup]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.config :as config])
  (:import [java.io ByteArrayOutputStream]))

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(defn- styles []
  (str
   "@page {
      size: A4;
      margin: 0;
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
      page-break-after: always;
      position: relative;
      width: 210mm;
      min-height: 297mm;
      padding: 0;
    }

    .page:last-child {
      page-break-after: auto;
    }

    .page-content {
      padding: 20mm;
    }

    .page-footer {
       position: absolute;
       bottom: 1cm;
       left: 0;
       right: 0;
       text-align: center;
       font-size: 10pt;
       color: #666;
       font-family: roboto, sans-serif;
    }

    h1 {
      font-size: 24pt;
      margin-bottom: 20px;
    }"))

(defn- page-footer [id page-num]
  [:div {:class "page-footer"}
   (str "Todistustunnus: " id " | " page-num)])

(defn- render-page [page-data page-num id]
  (let [{:keys [title content]} page-data]
    [:div {:class "page"}
     [:div {:class "page-content"}
      (when title [:h1 title])
      content]
     (page-footer id page-num)]))

(defn generate-document-html
  "Generate complete HTML document for Energiatodistus 2026 PDF."
  [pages id]
  (let [pages-html (map-indexed
                     (fn [idx page-data]
                       (render-page page-data (inc idx) id))
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
  (let [output-stream (ByteArrayOutputStream.)
        ;; Placeholder content for now
        pages [{:title "Energiatodistus 2026"
                :content [:div
                          [:p "Tämä on energiatodistuksen 2026 placeholder-sisältö."]
                          [:p (str "Kieli: " kieli)]]}]]
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
