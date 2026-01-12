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
      margin: 20mm;
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

    h1 {
      font-size: 24pt;
      margin-bottom: 20px;
    }

    .id-string {
      string-set: id-string content();
      display: none;
    }
   "))

(defn generate-document-html
  "Generate complete HTML document for Energiatodistus 2026 PDF."
  [content-hiccup id]
  (hiccup/html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:title "Energiatodistus"]
     [:style (styles)]]
    [:body
     [:div.id-string id]
     content-hiccup]]))

(defn- generate-energiatodistus-ohtp-pdf
  "Use OpenHTMLToPDF to generate PDF, return as a byte array"
  [energiatodistus kieli]
  (let [output-stream (ByteArrayOutputStream.)
        ;; Placeholder content for now
        content [:div
                 [:h1 "Energiatodistus 2026"]
                 [:p "Tämä on energiatodistuksen 2026 placeholder-sisältö."]
                 [:p (str "Todistuksen ID: " (:id energiatodistus))]
                 [:p (str "Kieli: " kieli)]]]
    (-> (generate-document-html content (:id energiatodistus))
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
