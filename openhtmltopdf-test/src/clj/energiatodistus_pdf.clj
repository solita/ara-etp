(ns energiatodistus-pdf
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [hiccup2.core :as h])
  (:import
    (com.openhtmltopdf.pdfboxout PdfRendererBuilder PdfRendererBuilder$PdfAConformance)
    (com.openhtmltopdf.outputdevice.helper BaseRendererBuilder$FontStyle)
    (com.openhtmltopdf.svgsupport BatikSVGDrawer)
    (java.io ByteArrayOutputStream FileOutputStream InputStream)
    (com.openhtmltopdf.extend FSSupplier)))

(defn style
  "Convert a Clojure map of CSS properties into an inline style string."
  [m]
  (->> m
       (map (fn [[k v]]
              (str (name k) ": " v)))
       (str/join "; ")))

(def colors
  {:bg-blue  "#009ee0"
   :e-luku-a "#219741"
   :e-luku-b "#7daf36"
   :e-luku-c "#c9d217"
   :e-luku-d "#ffec01"
   :e-luku-e "#b2a83b"
   :e-luku-f "#cf6501"
   :e-luku-g "#c40109"})

(defn- add-font [builder font-resource font-weight font-style]
  (.useFont builder (reify FSSupplier
                      (supply [^InputStream _]
                        (-> font-resource io/resource io/input-stream))) "roboto" (Integer/valueOf font-weight) font-style true))

(defn build-pdf [html-doc output-stream]
  (doto (PdfRendererBuilder.)
    (.useFastMode)
    (.usePdfUaAccessibility true)
    (.usePdfAConformance PdfRendererBuilder$PdfAConformance/PDFA_3_U)
    (add-font "fonts/Roboto-Bold.ttf" 700 BaseRendererBuilder$FontStyle/NORMAL)
    (add-font "fonts/Roboto-BoldItalic.ttf" 700 BaseRendererBuilder$FontStyle/ITALIC)
    (add-font "fonts/Roboto-Italic.ttf" 400 BaseRendererBuilder$FontStyle/ITALIC)
    (add-font "fonts/Roboto-Regular.ttf" 400 BaseRendererBuilder$FontStyle/NORMAL)
    (.withHtmlContent html-doc (.toString (io/resource "")))
    (.toStream output-stream)
    (.useSVGDrawer (BatikSVGDrawer.))                       ; // Load SVG support plugin
    (.run)))

(defn hiccup-doc->html-doc [hiccup-doc]
  (str (h/html hiccup-doc)))

;; TODO: This would be much easire to adjust if just using maps {:x 10 :y 10} instead of strings...
(defn arrow-polygon-svg [block-length block-color mirrored?]
  (let [sw "10,10"
        se (str (+ 10 block-length) ",10")
        e (str (+ 10 block-length 17) ",17")
        ne (str (+ 10 block-length) ",24")
        nw "10,24"]
    [:polygon {:points    (str/join " " [sw se e ne nw sw])
               :fill      block-color
               :transform (when mirrored? "scale(-1,1) translate(-100,0)")}]))

(defn e-luokka-block-svg [e-luokka-desc block-length block-color]
  [:svg {:xmlns "http://www.w3.org/2000/svg" :height "40" :width "300"}
   (arrow-polygon-svg block-length block-color false)
   [:text {:x "18" :y "21" :fill "black" :font-weight "700" :font-size "10" :font-family "roboto"} e-luokka-desc]])

(defn e-luokka-indicator-svg [e-luokka-desc]
  [:svg {:xmlns "http://www.w3.org/2000/svg" :height "40" :width "100"}
   (arrow-polygon-svg 40 "black" true)
   [:text {:x "60" :y "19" :fill "white" :font-weight "700" :font-size "10" :font-family "roboto"} e-luokka-desc [:tspan {:baseline-shift "sub" :font-size 5} "2018"]]])


(defn e-luokka-table [luokka]
  (let [border-style (str "border-bottom: 1px solid" (:bg-blue colors) ";" " border-right: 1px solid" (:bg-blue colors) ";")
        td #(vector :td {:style border-style} [:td %])
        when-clause #(if (= luokka %) (e-luokka-indicator-svg luokka) "")
        tr #(vector :tr (td %1) (td %2))]
    [:figure {:role "img" :aria-label (str "Rakennuksen energiatehokkuusluokka on " luokka ".")}
     [:table {:style (str "background-color: white; width: 100%; margin: 60px; border-spacing: 0px;") :aria-hidden "true"}
      [:thead {:style (str "border-bottom: 1px solid " (:bg-blue colors) ";")}
       [:tr
        [:th {:style border-style} ""]
        [:th {:style border-style} "Energiatehokkuusluokka"]]]
      [:tbody
       (tr (e-luokka-block-svg "A" 35 (:e-luku-a colors)) (when-clause "A"))
       (tr (e-luokka-block-svg "B" 70 (:e-luku-b colors)) (when-clause "B"))
       (tr (e-luokka-block-svg "C" 105 (:e-luku-c colors)) (when-clause "C"))
       (tr (e-luokka-block-svg "D" 140 (:e-luku-d colors)) (when-clause "D"))
       (tr (e-luokka-block-svg "E" 175 (:e-luku-e colors)) (when-clause "E"))
       (tr (e-luokka-block-svg "F" 210 (:e-luku-f colors)) (when-clause "F"))
       (tr (e-luokka-block-svg "G" 245 (:e-luku-g colors)) (when-clause "G"))]]]))

(defn first-page [& content]
  [:div {:style (style (cond-> {:background-color  (:bg-blue colors)
                                :border-radius     "25px"
                                :height            "950px"
                                :padding-top       "6px"
                                :page-break-inside "avoid"}))} content])

(defn page [& content]
  [:div {:style (style {:page-break-inside "avoid"
                        :page-break-before "always"})} content])

(defn et26-test [{:keys [todistustunnus rakennustunnus e-luokka]}]
  [:html {:lang "fi-FI"}
   [:head
    [:title "Energiatodistus"]
    [:meta {:name "subject" :content "Hieno pytinki -rakennuksen energiatodistus"}]
    [:meta {:name "author" :content "Laatija Sejase"}]
    [:meta {:name "description" :content "Vuoden 2018 mukainen energiatodistus rakennukselle Hieno pytinki"}]
    [:style
     (str "@page {
        @bottom-center {
          font-family: roboto;
          content: 'Todistustunnus: " todistustunnus ", ' counter(page) '/' counter(pages);
        }
      }
      @page:first {
        @bottom-center {
          font-family: roboto;
          content: '';
        }
      }
    ")]]
   [:body {:style "font-family: roboto"}
    (first-page
      [:h1 {:style (str "background-color: white; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; border-top-left-radius: 25px; border-top-right-radius: 25px; text-align: center; margin: 15px; ")} "ENERGIATODISTUS 2018"]
      ;; Using a table since dl-element does not seem to work nicely with screen reader.
      [:table {:style (str "background-color: white; width: 100%; margin: 60px;")}
       [:thead {:style "display: none"}
        ;; This should be read by a screen reader.
        [:tr
         [:th "Tiedon kuvaus"]
         [:th "Tiedon sisältö"]]]
       [:tbody
        [:tr
         [:td "Rakennuksen nimi"]
         [:td "Hieno pytinki"]]
        [:tr
         [:td "Rakennuksen osoite"]
         [:td [:address "Hienonpytkinkinkatu 3" [:br] "33100 TAMPERE" [:br] "TÄHÄN VOI TULLA JOTAIN?"]]]
        [:tr
         [:td "Pysyvä rakennustunnus"]
         [:td "1010101A"]]
        [:tr
         [:td "Rakennuksen käyttötarkoitusluokka"]
         [:td "Tavaratalot"]]
        [:tr
         [:td "Todistustunnus"]
         [:td todistustunnus]]
        [:tr
         [:td "Energiatodistus on laadittu"]
         [:td "3"]]
        [:tr
         [:td "Olemassa olevalle rakennukselle, havainnointikäynnin päivämäärä"]
         [:td "3"]]]]
      (e-luokka-table e-luokka))
    (page
      [:h2 "E-LUVUN LASKENNAN LÄHTÖTIEDOT"]
      [:table
       [:thead
        [:tr
         [:th "Rakennuskohde"]]]
       [:tbody
        [:tr
         [:th {:scope "row"} "Rakennuksen käyttötarkoitusluokka"]
         [:td "Tavaratalot"]
         [:td {:style "colspan=2"}]]
        [:tr
         [:th {:scope "row"} "Rakennuksen valmistumisvuosi"]
         [:td "2018"]
         [:th {:scope "row"} "Lämmitetty nettoala"]
         [:td "150000 m^2"]]]
       ])]])

(defn hiccup-doc [{:keys [data]}]
  (with-open [baos (ByteArrayOutputStream.)
              fos (FileOutputStream. "hiccup-doc.testing-output.pdf")
              html-doc (io/writer "hiccup-html.testing-output.html")]
    (spit html-doc (hiccup-doc->html-doc (et26-test data)))
    (-> (hiccup-doc->html-doc (et26-test data))
        (build-pdf fos))
    (.toByteArray baos)))

(defn call-this-function2 []
  ;; TODO: Maybe it would be a good idea in the data map to convert every value to string at this point?
  (hiccup-doc->html-doc (hiccup-doc {:data {:todistustunnus "8"
                                            :rakennustunnus 123
                                            :e-luokka       "B"}})))

(call-this-function2)