(ns solita.etp.service.perusparannuspassi-pdf
  (:require [hiccup.core :as hiccup]))

;; CSS styles for the document
(defn- styles []
  (str
  "<style>
    @page {
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
      font-size: 12pt;
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

    .page-header {
      background-color: #2c5234;
      height: 5cm;
      width: 100%;
      padding: 16mm 2cm 0 16mm;
    }

    .page-title {
      color: white;
      font-size: 24pt;
      font-weight: bold;
      margin: 0;
      font-family: roboto, sans-serif;
    }

    .page-content {
      padding: 16mm;
      min-height: calc(297mm - 5cm - 2cm);
    }

    .page-footer {
      position: absolute;
      bottom: 1cm;
      left: 2cm;
      right: 2cm;
      font-size: 10pt;
      color: #666;
      font-family: roboto, sans-serif;
    }

    h1, h2, h3, h4, h5, h6 {
      font-family: roboto, sans-serif;
    }

    p, div, span, li, ul, ol {
      font-family: roboto, sans-serif;
    }
  </style>"))

(defn- page-header [title]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]])

(defn- page-footer [ppp-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Perusparannuspassin tunnus: " ppp-tunnus " | " page-num "/" total-pages)])

(defn- render-page [page-data page-num total-pages ppp-tunnus]
  (let [{:keys [title content]} page-data]
    [:div {:class "page"}
     (page-header title)
     [:div {:class "page-content"}
      content]
     (page-footer ppp-tunnus page-num total-pages)]))

(defn generate-document-html
  "Generate complete HTML document for perusparannuspassi PDF.

   Parameters:
   - pages: A sequence of page data maps, each containing:
     - :title - The title to display in the header
     - :content - Hiccup data for the page body
   - ppp-tunnus: The perusparannuspassi identifier (e.g., \"1234567\")

   Returns: HTML string ready for PDF conversion."
  [pages ppp-tunnus]
  (let [total-pages (count pages)
        pages-html (map-indexed
                     (fn [idx page-data]
                       (render-page page-data (inc idx) total-pages ppp-tunnus))
                     pages)]
    (hiccup/html
      [:html
       [:head
        [:meta {:charset "UTF-8"}]
        [:title "Perusparannuspassi"]
        (styles)]
       [:body
        pages-html]])))
