(ns solita.etp.service.perusparannuspassi-pdf
  (:require
    [clojure.java.io :as io]
    [hiccup.core :as hiccup]
    [solita.etp.config :as config]
    [solita.etp.service.localization :as loc]
    [solita.etp.service.luokittelu :as luokittelu-service]
    [solita.etp.service.pdf :as pdf-service]
    [solita.etp.service.complete-perusparannuspassi :as complete-ppp]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
    [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
    [solita.etp.service.perusparannuspassi-pdf.etusivu-yleistiedot :as etusivu-yleistiedot]
    [solita.etp.service.perusparannuspassi-pdf.etusivu-laatija :as etusivu-laatija]
    [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :refer [toimenpiteiden-vaikutukset]]
    [solita.etp.service.perusparannuspassi-pdf.laskennan-taustatiedot :as laskennan-taustatiedot]
    [solita.etp.service.perusparannuspassi-pdf.vaiheissa-toteutettavat-toimenpiteet :as vaiheissa-toteutettavat-toimenpiteet]
    [solita.etp.service.perusparannuspassi-pdf.vaiheistuksen-yhteenveto :refer [vaiheistuksen-yhteenveto]]
    [solita.etp.service.perusparannuspassi-pdf.lisatietoja :as lisatietoja]
    [solita.etp.service.toimenpide-ehdotus :as toimenpide-ehdotus-service]
    [solita.etp.service.watermark-pdf :as watermark-pdf])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def ^:private draft-watermark-texts {"fi" "LUONNOS"
                                      "sv" "UTKAST"})

(def ^:private test-watermark-texts {"fi" "TESTI"
                                     "sv" "TEST"})

(defn- styles []
  (slurp (io/resource "perusparannuspassi.css")))

(defn- page-header [title title-class]
  [:div {:class "page-header"}
   [:h1 (cond-> {:class "page-title"}
          title-class (update :class str " " title-class))
    title]])

(defn- page-footer [ppp-tunnus page-num total-pages]
  [:div {:class "page-footer"}
   (str "Perusparannuspassin tunnus: " ppp-tunnus " | " page-num "/" total-pages)])

(defn- render-page [page-data page-num total-pages ppp-tunnus]
  (let [{:keys [title content header title-class content-class]} page-data]
    [:div {:class "page"}
     (or header (page-header title title-class))
     [:div (cond-> {:class "page-content"}
             content-class (update :class str " " content-class))
      content]
     (page-footer ppp-tunnus page-num total-pages)]))

(defn- generate-document-html
  "Generate complete HTML document for perusparannuspassi PDF.

   Parameters:
   - pages: A sequence of page data maps, each containing:
     - :title - The title to display in the header
     - :title-class - Optional additional CSS class for the page title h1
     - :content - Hiccup data for the page body
   - ppp-tunnus: The perusparannuspassi identifier (e.g., \"1234567\")

   Returns: HTML string ready for PDF conversion."
  [pages ppp-tunnus l]
  (let [total-pages (count pages)
        pages-html (map-indexed
                     (fn [idx page-data]
                       (render-page page-data (inc idx) total-pages ppp-tunnus))
                     pages)]
    (hiccup/html
      [:html
       [:head
        [:meta {:charset "UTF-8"}]
        [:meta {:name "subject" :content (l :perusparannuspassi)}]
        [:meta {:name "description" :content (l :perusparannuspassi)}]
        [:title (l :perusparannuspassi)]
        [:style (styles)]]
       [:body
        pages-html]])))

(defn- generate-perusparannuspassi-html [{:keys [perusparannuspassi kieli] :as params}]
  (let [l (kieli loc/ppp-pdf-localization)
        ;; Generate vaihe pages, filtering out nil (vaiheet without data)
        vaihe-pages (->> (range 1 5)
                         (map #(vaiheissa-toteutettavat-toimenpiteet/render-page params %))
                         (remove nil?))
        pages (concat
               [{:title (l :perusparannuspassi)
                 :title-class "etusivu-title"
                 :content-class "etusivu-content"
                 :content
                 [:div
                  (etusivu-yleistiedot/etusivu-yleistiedot params)
                  [:h2 (l :perusparannuspassissa-ehdotettujen-toimenpiteiden-vaikutukset)]
                  (toimenpiteiden-vaikutukset params)
                  (etusivu-laatija/etusivu-laatija params)]}]
               vaihe-pages
               [{:title (l :vaiheistuksen-yhteenveto)
                 :title-class "vaiheistuksen-yhteenveto-title"
                 :content (vaiheistuksen-yhteenveto params)}
                {:title (l :laskennan-taustatiedot-otsikko)
                 :title-class "laskennan-taustatiedot-title"
                 :content
                 (into [:div]
                       (vals (laskennan-taustatiedot/generate-all-laskennan-taustatiedot params)))}
                {:title "Lisätietoja"
                 :title-class "lisatietoja-title"
                 :content
                 [:div
                  (lisatietoja/lisatietoja params)]}])]
    (generate-document-html pages (:id perusparannuspassi) l)))

(defn- generate-perusparannuspassi-ohtp-pdf
  "Use OpenHTMLToPDF to generate a PPP PDF, return as a byte array"
  [params]
  (let [output-stream (ByteArrayOutputStream.)]
    (-> (generate-perusparannuspassi-html params)
        (pdf-service/html->pdf output-stream))
    (-> output-stream .toByteArray)))

(defn generate-perusparannuspassi-pdf
  "Generate a perusparannuspassi PDF and return it as a byte array.
   luokittelut is a map of classification data with keys:
     :kayttotarkoitukset, :alakayttotarkoitukset, :mahdollisuus-liittya,
     :uusiutuva-energia, :lammitysmuodot, :ilmanvaihtotyypit, :toimenpide-ehdotukset"
  [perusparannuspassi energiatodistus luokittelut kieli draft?]
  (let [kieli-keyword (keyword kieli)
        pdf-bytes

        ;; Generate the PDF to byte array
        (generate-perusparannuspassi-ohtp-pdf
          (merge luokittelut
                 {:energiatodistus    energiatodistus
                  :perusparannuspassi perusparannuspassi
                  :kieli              kieli-keyword}))
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
  [perusparannuspassi energiatodistus luokittelut kieli draft?]
  (let [pdf-bytes (generate-perusparannuspassi-pdf perusparannuspassi energiatodistus luokittelut kieli draft?)]
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
  (when-let [perusparannuspassi (complete-ppp/find-complete-perusparannuspassi db whoami ppp-id)]
    (let [energiatodistus-id (:energiatodistus-id perusparannuspassi)
          energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus db energiatodistus-id)
          versio (:versio energiatodistus)
          luokittelut {:kayttotarkoitukset    (kayttotarkoitus-service/find-kayttotarkoitukset db versio)
                       :alakayttotarkoitukset (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
                       :mahdollisuus-liittya  (luokittelu-service/find-mahdollisuus-liittya db)
                       :uusiutuva-energia     (luokittelu-service/find-uusiutuva-energia db)
                       :lammitysmuodot        (luokittelu-service/find-lammitysmuodot db)
                       :ilmanvaihtotyypit     (luokittelu-service/find-ilmanvaihtotyypit db)
                       :toimenpide-ehdotukset (toimenpide-ehdotus-service/find-all db)}
          draft? true]
      ;; Always show draft watermark for now (no signing yet)
      (generate-pdf-as-input-stream perusparannuspassi energiatodistus luokittelut kieli draft?))))

(defn find-perusparannuspassi-html
  "Find or generate HTML for a perusparannuspassi PDF.

   Parameters:
   - db: Database connection
   - whoami: Current user
   - ppp-id: Perusparannuspassi ID
   - kieli: Language code ('fi' or 'sv')

   Returns: HTML string representation of the PDF, or nil if not found"
  [db whoami ppp-id kieli]
  (when-let [perusparannuspassi (complete-ppp/find-complete-perusparannuspassi db whoami ppp-id)]
    (let [energiatodistus-id (:energiatodistus-id perusparannuspassi)
          energiatodistus (energiatodistus-service/find-energiatodistus db whoami energiatodistus-id)
          versio (:versio energiatodistus)
          luokittelut {:kayttotarkoitukset    (kayttotarkoitus-service/find-kayttotarkoitukset db versio)
                       :alakayttotarkoitukset (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
                       :mahdollisuus-liittya  (luokittelu-service/find-mahdollisuus-liittya db)
                       :uusiutuva-energia     (luokittelu-service/find-uusiutuva-energia db)
                       :lammitysmuodot        (luokittelu-service/find-lammitysmuodot db)
                       :ilmanvaihtotyypit     (luokittelu-service/find-ilmanvaihtotyypit db)
                       :toimenpide-ehdotukset (toimenpide-ehdotus-service/find-all db)}]
      (generate-perusparannuspassi-html (merge luokittelut
                                              {:energiatodistus    energiatodistus
                                               :perusparannuspassi perusparannuspassi
                                               :kieli              (keyword kieli)})))))
