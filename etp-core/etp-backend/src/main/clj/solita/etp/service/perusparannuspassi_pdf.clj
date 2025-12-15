(ns solita.etp.service.perusparannuspassi-pdf
  (:require
    [hiccup.core :as hiccup]
    [solita.etp.config :as config]
    [solita.etp.service.localization :as loc]
    [solita.etp.service.pdf :as pdf-service]
    [solita.etp.service.watermark-pdf :as watermark-pdf]
    [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
    [solita.etp.service.perusparannuspassi-pdf.etusivu-yleistiedot :as etusivu-yleistiedot]
    [solita.etp.service.perusparannuspassi-pdf.etusivu-laatija :as etusivu-laatija]
    [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :refer [toimenpiteiden-vaikutukset]]
    [solita.etp.service.perusparannuspassi-pdf.laskennan-taustatiedot :as laskennan-taustatiedot]
    [solita.etp.service.luokittelu :as luokittelu-service]
    [solita.etp.service.perusparannuspassi-pdf.lisatietoja :as lisatietoja]
    [solita.etp.service.perusparannuspassi-pdf.vaiheissa-toteutettavat-toimenpiteet :as vaiheissa-toteutettavat-toimenpiteet])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

;; CSS styles for the document
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
      min-height: 227mm;
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

    h1 {
      font-size: 30pt;
    }

    h2 {
      font-size: 13pt;
      color: #2c5234;
    }

    h3 {
      font-size: 11pt;
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

    dl.etusivu-laatija-allekirjoitus {
      display: table;
      width: 80%;
      margin: auto;
      border-collapse: collapse;
      -fs-border-rendering: no-bevel;
    }

    dl.etusivu-laatija-allekirjoitus div{
      display: table-row;
    }

    dl.etusivu-laatija-allekirjoitus dt,
    dl.etusivu-laatija-allekirjoitus dd {
      display: table-cell;
      padding: 6.5px 8px;
      white-space: nowrap;
    }

    dl.etusivu-laatija-allekirjoitus dd {
      border: 1px solid #2c5234;
    }

    dl.etusivu-laatija-allekirjoitus dd.laatija-nimi {
      border-right: none;
      width: 100%
    }

    dt.hidden-dt {
      font-size: 0;
      border: 1px solid #2c5234;
      border-left: none;
    }

    .vaikutukset-box {
      background-color: #eaeeeb;
      border-radius: 3mm;
      padding: 3mm 6mm 6mm 6mm;
      width: 100%;
      min-height: 80mm;
    }

    .kohdistuminen-box {
      background-color: #d5dcd6;
      border-radius: 3mm;
      width: 100%;
      padding-left: 5mm;
    }

    dl.tayttaa-vaatimukset-list {
      display: table;
      margin: 0;
      padding: 0;
      width: 100%;
    }

    dl.tayttaa-vaatimukset-list > div {
      display: table-cell;
      padding-right: 10mm;
      vertical-align: top;
    }

    dl.tayttaa-vaatimukset-list > div:last-child {
      padding-right: 0;
    }

    dl.tayttaa-vaatimukset-list > div > dt,
    dl.tayttaa-vaatimukset-list > div > dd {
      display: inline-block;
      border: 1px solid #2c5234;
      padding: 6px 8px;
      margin: 0;
      vertical-align: top;
    }

    dl.tayttaa-vaatimukset-list > div > dt {
      width: 60mm;
      border-right: none;
      font-weight: normal;
      white-space: nowrap;
    }

    dl.tayttaa-vaatimukset-list > div > dd {
      width: 14mm;
    }

    .vaatimukset-selitteet-box {
      margin-top: 3mm;
    }

    .vaatimukset-selitteet {
      display: table-row;
      width: 100%;
    }

    .vaatimukset-selitteet > div {
      display: table-cell;
      width: 50%;
      margin-right: 10mm;
    }

    .vaatimukset-selitteet > div:last-child {
      margin-right: 0;
      padding-left: 5mm;
    }

    table.lt-u-arvot {
      display: table;
      width: 100%;
      border-collapse: collapse;
      -fs-border-rendering: no-bevel;
      margin-bottom: 30px;
    }

    table.lt-u-arvot th,
    table.lt-u-arvot td {
      display: table-cell;
      -fs-border-rendering: no-bevel;
      border: 1px solid #2c5234;
      padding: 5px 8px;
      font-size: 14px;
   }

   th.lt-otsikko {
     background-color: #2c5234;
     color: white;
     font-weight:bold;
     padding: 5px 8px;
     font-size: 14px;
   }

   table.lt-u-arvot .lt-sarakkeet th {
     font-weight: normal;
     font-size: 14px;
   }

   table.lt-lammitys th,
   table.lt-lammitys td {
     display: table-cell;
     -fs-border-rendering: no-bevel;
     border: 1px solid #2c5234;
     padding: 5px 8px;
     font-size: 14px;
   }

   tr.sarakkeet.lammitys-ilmanvaihto {
     background-color: #2c5234;
     color: white;
     text-align:center;
     font-weight:bold;
     padding: 5px 8px;
     font-size: 14px;
   }

   table.lt-lammitys th.empty {
    border: none;
    background: none;
   }

    table.lt-lammitys th {
     font-weight: normal;
     text-align: center;
     font-size: 14px;
    }

   table.lt-lammitys {
    display: table;
    width: 100%;
    border-collapse: collapse;
    -fs-border-rendering: no-bevel;
   }

   dl.lt-vahimmaisvaatimustaso {
    display: table;
    border-collapse: collapse;
    -fs-border-rendering: no-bevel;
    font-size: 14px;
    width: 100%;
    }

  dl.lt-vahimmaisvaatimustaso dd {
    display: table-cell;
    padding: 5px 8px;
    white-space: nowrap
   }

   dl.lt-vahimmaisvaatimustaso dd {
    border: 1px solid #2c5234;
   }

   dl.lt-korjausrakentamisen-saadokset {
    font-size: 14px;
   }

   table.lt-mahdollisuus-liittya {
     display: table;
     width: 100%;
     border-collapse: collapse;
     -fs-border-rendering: no-bevel;
     margin-bottom: 30px;
   }

   table.lt-mahdollisuus-liittya th,
   table.lt-mahdollisuus-liittya td {
     display: table-cell;
     border: 1px solid #2c5234;
     padding: 5px 8px;
     font-size: 14px;
   }

   table.lt-lisatietoja {
     display: table;
     width: 100%;
     border-collapse: collapse;
     -fs-border-rendering: no-bevel;
   }

   table.lt-lisatietoja th,
   table.lt-lisatietoja td {
     display: table-cell;
     -fs-border-rendering: no-bevel;
     border: 1px solid #2c5234;
     padding: 5px 8px;
     font-size: 14px;
   }

   dl.lt-voimassaolo {
     font-size: 14px;
   }

  .lisatietoja-box {
    display: block;
    border: 1px solid #2c5234;
    padding: 3px;
    border-top: none;
    font-size: 13.8px;
    }

   .lisatietoja-field {
     min-height: 180mm;
   }

   .lisatietoja-sivu-otsikko {
     background-color: #2c5234;
     color: white;
     font-weight: bold;
     padding: 5px 8px;
     font-size: 14px;
     margin: 0;
    }"))


(defn- page-header [title]
  [:div {:class "page-header"}
   [:h1 {:class "page-title"} title]])

(defn- page-footer [ppp-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Perusparannuspassin tunnus: " ppp-tunnus " | " page-num "/" total-pages)])

(defn- render-page [page-data page-num total-pages ppp-tunnus]
  (let [{:keys [title content header]} page-data]
    [:div {:class "page"}
     (or header (page-header title))
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
        [:meta {:name "subject" :content "Perusparannuspassi"}]
        [:title "Perusparannuspassi"]
        [:style (styles)]]
       [:body
        pages-html]])))

(defn- generate-perusparannuspassi-ohtp-pdf
  "Use OpenHTMLToPDF to generate a PPP PDF, return as a byte array"
  [{:keys [perusparannuspassi kieli] :as params}]
  (let [output-stream (ByteArrayOutputStream.)
        l (kieli loc/ppp-pdf-localization)
        pages [{:title (l :perusparannuspassi)
                :content
                [:div
                 (etusivu-yleistiedot/etusivu-yleistiedot params)
                 [:h2 (l :perusparannuspassissa-ehdotettujen-toimenpiteiden-vaikutukset)]
                 (toimenpiteiden-vaikutukset params)
                 (etusivu-laatija/etusivu-laatija params)]}
               (vaiheissa-toteutettavat-toimenpiteet/render-page params 1)
               (vaiheissa-toteutettavat-toimenpiteet/render-page params 2)
               (vaiheissa-toteutettavat-toimenpiteet/render-page params 3)
               (vaiheissa-toteutettavat-toimenpiteet/render-page params 4)
               {:title (l :vaiheistuksen-yhteenveto)
                :content
                [:div
                 [:p "Tähän se suuri taulukko"]]}
               {:title (l :laskennan-taustatiedot-otsikko)
                :content
                (into [:div]
                      (vals (laskennan-taustatiedot/generate-all-laskennan-taustatiedot params)))}
               {:title "Lisätietoja"
                :content
                [:div
                 (lisatietoja/lisatietoja params)]}]]
    (-> (generate-document-html pages (:id perusparannuspassi))
        (pdf-service/html->pdf output-stream))
    (-> output-stream .toByteArray)))

(defn- generate-perusparannuspassi-pdf
  "Generate a perusparannuspassi PDF and return it as a byte array."
  [perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset mahdollisuus-liittya
   uusiutuva-energia lammitysmuodot ilmanvaihtotyypit kieli draft?]
  (let [kieli-keyword (keyword kieli)
        pdf-bytes

        ;; Generate the PDF to byte array
        (generate-perusparannuspassi-ohtp-pdf
          {:energiatodistus       energiatodistus
           :perusparannuspassi    perusparannuspassi
           :kayttotarkoitukset    kayttotarkoitukset
           :alakayttotarkoitukset alakayttotarkoitukset
           :kieli                 kieli-keyword
           :mahdollisuus-liittya  mahdollisuus-liittya
           :uusiutuva-energia     uusiutuva-energia
           :lammitysmuodot        lammitysmuodot
           :ilmanvaihtotyypit     ilmanvaihtotyypit})
        watermark-text (cond
                         draft? (draft-watermark-texts kieli)
                         (contains? #{"local-dev" "dev" "test"} config/environment-alias) (test-watermark-texts kieli)
                         :else nil)]

    (if (some? watermark-text)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes watermark-text)
      pdf-bytes)))

(defn- generate-pdf-as-input-stream
  "Generate a perusparannuspassi PDF and return it as an InputStream.
   Applies watermarks via post-processing with PDFBox."
  [perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset mahdollisuus-liittya
   uusiutuva-energia lammitysmuodot ilmanvaihtotyypit kieli draft?]
  (let [pdf-bytes (generate-perusparannuspassi-pdf perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset
                                                   mahdollisuus-liittya uusiutuva-energia lammitysmuodot ilmanvaihtotyypit kieli draft?)]
    (ByteArrayInputStream. pdf-bytes)))

(defn find-perusparannuspassi-pdf
  "Find or generate a perusparannuspassi PDF.
   For now, always generates a new PDF (S3 caching not implemented yet).

   Parameters:
   - db: Database connection
   - whoami: Current user
   - ppp-id: Perusparannuspassi ID
   - kieli: Language code ('fi' or 'sv')

   Returns: InputStream of the PDF, or nil if not found"
  [db whoami ppp-id kieli]
  (when-let [perusparannuspassi (perusparannuspassi-service/find-perusparannuspassi db whoami ppp-id)]
    (let [energiatodistus-id (:energiatodistus-id perusparannuspassi)
          energiatodistus (energiatodistus-service/find-energiatodistus db whoami energiatodistus-id)
          versio (:versio energiatodistus)
          kayttotarkoitukset (kayttotarkoitus-service/find-kayttotarkoitukset db versio)
          alakayttotarkoitukset (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
          mahdollisuus-liittya (luokittelu-service/find-mahdollisuus-liittya db)
          uusiutuva-energia (luokittelu-service/find-uusiutuva-energia db)
          lammitysmuodot (luokittelu-service/find-lammitysmuodot db)
          ilmanvaihtotyypit (luokittelu-service/find-ilmanvaihtotyypit db)
          draft? true]
      ;; Always show draft watermark for now (no signing yet)
      (generate-pdf-as-input-stream perusparannuspassi energiatodistus kayttotarkoitukset alakayttotarkoitukset
                                    mahdollisuus-liittya uusiutuva-energia lammitysmuodot ilmanvaihtotyypit kieli draft?))))
