(ns solita.etp.service.perusparannuspassi-pdf
  (:require
    [hiccup.core :as hiccup]
    [solita.etp.service.perusparannuspassi-pdf.etusivu-yleistiedot :as etusivu-yleistiedot ]
    [solita.etp.service.pdf :as pdf-service]))

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
      height: 35mm;
      width: 100%;
      padding: 10mm 16mm 0 16mm;
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

    dl.etusivu-yleistiedot {
      display: table;
      width: 100%;
      background-color: #2c5234;
      border-collapse: collapse;
      -fs-border-rendering: no-bevel;
    }

    dl.etusivu-yleistiedot div {
      display: table-row;
    }

    dl.etusivu-yleistiedot dt,
    dl.etusivu-yleistiedot dd {
      display: table-cell;
      -fs-border-rendering: no-bevel;
      border: 1px solid #2c5234;
      padding: 6.5px 8px;
    }

    dl.etusivu-yleistiedot dt {
      color: white;
      white-space: nowrap;
      width: 1px;
    }

    dl.etusivu-yleistiedot dd {
      background-color: white;
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

(defn generate-perusparannuspassi-pdf [{:keys [perusparannuspassi output-stream] :as params}]
  (let [pages [{:title "Perusparannuspassi"
                :content
                [:div
                 (etusivu-yleistiedot/etusivu-yleistiedot params)
                 [:p "Etusivun sisältö tähän"]]}
               {:title "Vaiheessa 1 toteutettavat toimenpiteet"
                :content
                [:div
                 [:p "Sisältö vaiheesta 1 tähän"]]}
               {:title "Vaiheessa 2 toteutettavat toimenpiteet"
                :content
                [:div
                 [:p "Sisältö vaiheesta 2 tähän"]]}
               {:title "Vaiheessa 3 toteutettavat toimenpiteet"
                :content
                [:div
                 [:p "Sisältö vaiheesta 3 tähän"]]}
               {:title "Vaiheessa 4 toteutettavat toimenpiteet"
                :content
                [:div
                 [:p "Sisältö vaiheesta 4 tähän"]]}
               {:title "Vaiheistuksen yhteenveto"
                :content
                [:div
                 [:p "Tähän se suuri taulukko"]]}
               {:title "Laskennan taustatiedot"
                :content
                [:div
                 [:p "Laskennan taustatiedot tähän"]]}
               {:title "Lisätietoja"
                :content
                [:div
                 [:p "Viimeinen sivu tähän"]]}]]
    (-> (generate-document-html pages (:id perusparannuspassi))
        (pdf-service/html->pdf output-stream))))
