(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [hiccup.core :as hiccup]
            [solita.etp.config :as config]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.localization :as loc]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.service.energiatodistus-pdf.etusivu-yleistiedot :as et-etusivu-yleistiedot]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as et-laskennallinen-ostoenergia]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

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
      width: 210mm;
      height: 297mm;
      padding: 70px;
      box-sizing: border-box;
      page-break-after: always;
      position: relative;
    }

   .page:last-child {
      page-break-after: auto;
    }

   .page-border-container {
      border: 12px solid #23323e;
      height: calc(100% - 24px);
      padding: 0;
      position: relative;
      display: flex;
      flex-direction: column;
    }

   .page-content {
      flex: 1;
      padding: 0;
    }

   .page-section {
      border-bottom: 12px solid #23323e;
      padding: 10px 3px;
    }

   .page-section:last-child {
      border-bottom: none;
    }

   .todo-placeholder {
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16pt;
      color: #999;
    }

   .page-footer {
      position: absolute;
      bottom: 10px;
      left: 0;
      right: 0;
      font-size: 10pt;
      color: #666;
      text-align: center;
      font-family: roboto, sans-serif;
    }

   .page-header {
      background-color: #23323e;
      height: 100px;
      width: 100%;
      padding: 20px 10mm;
      margin: 0;
      color: white;
      font-family: roboto, sans-serif;
      text-align: center;
   }

    .page-title {
      color: white;
      font-size: 25px;
      font-weight: bold;
      margin: 0 0 8px 0;
      font-family: roboto, sans-serif;
      text-transform: uppercase;
    }

    .page-subtitle {
      color: white;
      font-size: 12px;
      font-weight: bold;
      margin: 0;
      font-family: roboto, sans-serif;
    }
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
    }

    dl.et-etusivu-yleistiedot {
      display: table;
      width: 90%;
      background-color: white;
      border-collapse: collapse;
      -fs-border-rendering: no-bevel;
      margin: 10px 15px;
    }

    dl.et-etusivu-yleistiedot div {
      display: table-row;
    }

    dl.et-etusivu-yleistiedot dt,
    dl.et-etusivu-yleistiedot dd {
      display: table-cell;
      fs-border-rendering: no-bevel;
      padding-top: 3px;
      padding-bottom: 3px;
    }

    dl.et-etusivu-yleistiedot dt {
      color: #23323e;
      font-weight: bold;
      white-space: nowrap;
      width: 45%;
      padding-right: 30px;
    }

    dl.et-etusivu-yleistiedot dd {
      background-color: white;
    }

    dl.et-etusivu-yleistiedot div:last-child dd {
      font-weight: bold;
      color: #23323e;
      white-space: nowrap;
    }

    table.ostoenergia {
      width: 95%;
      margin: 0 auto;
      border-collapse: collapse;
      margin: 10px 15px;
      table-layout: fixed;
    }

    table.ostoenergia th,
    table.ostoenergia td {
      border: 1px solid #bdc6cc;
      padding: 2px 2px;
      font-weight: normal;
      font-size: 13px;
      text-align: center;
      color: #23323e;
    }

    table.ostoenergia th:first-child,
    table.ostoenergia td:first-child {
      border-left: none;
      border-right: none;
      font-weight: bold;
      text-align: left;
      width: 25%;
    }

    table.ostoenergia th.oe-otsikko {
      font-weight: bold;
    }

    table.ostoenergia th:nth-child(2),
    table.ostoenergia td:nth-child(2) {
      border-left: none;
      width: 15%;
    }

    table.ostoenergia th:last-child,
    table.ostoenergia td:last-child {
      border-right: none;
      width: 70px;
    }

    table.ostoenergia th:nth-child(3),
    table.ostoenergia td:nth-child(3),
    table.ostoenergia th:nth-child(4),
    table.ostoenergia td:nth-child(4),
    table.ostoenergia th:nth-child(5),
    table.ostoenergia td:nth-child(5),
    table.ostoenergia th:nth-child(6),
    table.ostoenergia td:nth-child(6) {
      width: 70px;
    }

    table.ostoenergia th.empty,
    table.ostoenergia th.oe-otsikko {
      border: none;
      background: none;
    }

    dl.et-etusivu-yleistiedot-ostoenergia {
      display: table;
      width: 90%;
      background-color: white;
      border-collapse: collapse;
      -fs-border-rendering: no-bevel;
      margin: 10px 10px;
    }

    dl.et-etusivu-yleistiedot-ostoenergia div {
      display: table-row;
    }

    dl.et-etusivu-yleistiedot-ostoenergia dt,
    dl.et-etusivu-yleistiedot-ostoenergia dd {
      display: table-cell;
      padding: 5px 5px;
    }

    dl.et-etusivu-yleistiedot-ostoenergia dt {
      color: #23323e;
      font-weight: bold;
      white-space: nowrap;
      width: 1px;
    }"))

(defn- page-header [title]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]])

(defn- page-footer [et-tunnus page-num total-pages]
  [:div {:class "page-footer"}])

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
                  [:div {:class "todo-placeholder"} "TODO"]]
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
  [energiatodistus alakayttotarkoitukset kieli draft?]
  (let [kieli-keyword (keyword kieli)
        pdf-bytes

        ;; Generate the PDF to byte array
        (generate-energiatodistus-ohtp-pdf
          {:energiatodistus       energiatodistus
           :alakayttotarkoitukset alakayttotarkoitukset
           :kieli                 kieli-keyword})
        watermark-text (cond
                         draft? (draft-watermark-texts kieli)
                         (contains? #{"local-dev" "dev" "test"} config/environment-alias) (test-watermark-texts kieli)
                         :else nil)]

    (if (some? watermark-text)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes watermark-text)
      pdf-bytes)))

(defn- generate-pdf-as-input-stream
  "Generate a energiatodistus PDF and return it as an InputStream.
   Applies watermarks via post-processing with PDFBox."
  [energiatodistus alakayttotarkoitukset kieli draft?]
  (let [pdf-bytes (generate-energiatodistus-pdf energiatodistus alakayttotarkoitukset kieli draft?)]
    (ByteArrayInputStream. pdf-bytes)))


(defn find-energiatodistus2026-pdf
  [db whoami et-id kieli]
  (when-let [energiatodistus (energiatodistus-service/find-energiatodistus db whoami et-id)]
    (let [versio (:versio energiatodistus)
          alakayttotarkoitukset (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
          draft? true]
      ;; Always show draft watermark for now (no signing yet)
      (generate-pdf-as-input-stream energiatodistus alakayttotarkoitukset kieli draft?))))
