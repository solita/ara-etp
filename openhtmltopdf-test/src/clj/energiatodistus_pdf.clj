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

(defn e-luokka-block-svg [e-luokka-desc y block-length block-color]
  [:svg {:xmlns "http://www.w3.org/2000/svg" :height "40" :width "300" :x "100" :y (str y)}
   (arrow-polygon-svg block-length block-color false)
   [:text {:x "18" :y "21" :fill "black" :font-weight "700" :font-size "10" :font-family "roboto"} e-luokka-desc]])

(defn e-luokka-indicator-svg [e-luokka-desc y]
  [:svg {:xmlns "http://www.w3.org/2000/svg" :height "40" :width "100" :x "400" :y (str y)}
   (arrow-polygon-svg 40 "black" true)
   [:text {:x "60" :y "19" :fill "white" :font-weight "700" :font-size "10" :font-family "roboto"} e-luokka-desc [:tspan {:baseline-shift "sub" :font-size 5} "2018"]]])

(defn e-luokka-table [luokka]
  (let [when-clause #(when (= luokka %1) (e-luokka-indicator-svg luokka %2))]
    [:figure {:style {:margin "0 auto" :width "550px"} :role "img"}
     [:svg {:style "background-color: white;" :xmlns "http://www.w3.org/2000/svg" :height "300" :width "550" :alt (str "Rakennuksen energiatehokkuusluokka on " luokka ".")}
      ;; TODO: Could add lines here to make this look like a table.
      (e-luokka-block-svg "A" 35 35 (:e-luku-a colors)) (when-clause "A" 35)
      (e-luokka-block-svg "B" 70 70 (:e-luku-b colors)) (when-clause "B" 70)
      (e-luokka-block-svg "C" 105 105 (:e-luku-c colors)) (when-clause "C" 105)
      (e-luokka-block-svg "D" 140 140 (:e-luku-d colors)) (when-clause "D" 140)
      (e-luokka-block-svg "E" 175 175 (:e-luku-e colors)) (when-clause "E" 175)
      (e-luokka-block-svg "F" 210 210 (:e-luku-f colors)) (when-clause "F" 210)
      (e-luokka-block-svg "G" 245 245 (:e-luku-g colors)) (when-clause "G" 245)]]))

(defn first-page [& content]
  [:div {:style (style (cond-> {:background-color  (:bg-blue colors)
                                :border-radius     "25px"
                                :height            "950px"
                                :padding-top       "6px"
                                :padding-left      "6px"
                                :padding-right     "6px"
                                :page-break-inside "avoid"}))} content])

(defn page [& content]
  [:div {:style (style {:border-color      (:bg-blue colors)
                        :border-width      "2px"
                        :border-style      "solid"
                        :height            "950px"
                        :page-break-inside "avoid"
                        :page-break-before "always"})} content])

;; Something that will be read
(defn description-list [key-vals]
  (into [:dl {:style "background-color: white; display: table; margin: 0 auto; width: 550px"}]
        (mapv #(vec [:div {:style "display: table-row"}
                     [:dt {:style (style {:display     "table-cell"
                                          :font-weight "bold"
                                          :color       (:bg-blue colors)})} (:dt %)]
                     [:dd {:style "display: table-cell;"} (:dd %)]]) key-vals)))

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
          color: " (:bg-blue colors) ";
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
      [:h1 {:style (str "background-color: white; border-bottom-left-radius: 45px; border-bottom-right-radius: 45px; border-top-left-radius: 25px; border-top-right-radius: 25px; text-align: center; margin: 4px; color:" (:bg-blue colors))} "ENERGIATODISTUS 2018"]
      ;; Using a table since dl-element does not seem to work nicely with screen reader.
      [:br {:style (style {:background-color (:bg-blue colors)
                           :height           "10px"
                           })}]
      [:div {:style (style {:margin "0 auto" :width "550px" :background-color "white"})}
       (description-list
         [{:dt "Rakennuksen nimi"
           :dd "Hieno pytinki"}
          {:dt "Rakennuksen osoite"
           :dd [:address "Hienonpytkinkinkatu 3" [:br] "33100 TAMPERE" [:br] "TÄHÄN VOI TULLA JOTAIN?"]}
          {:dt "Pysyvä rakennustunnus"
           :dd "1010101A"}
          {:dt "Rakennuksen käyttötarkoitusluokka"
           :dd "Tavaratalot"}
          {:dt "Todistustunnus"
           :dd todistustunnus}
          {:dt "Energiatodistus on laadittu"
           :dd "3"}])
       (description-list
         [{:dt "Rakennuksen nimi"
           :dd "Hieno pytinki"}])]
      [:br {:style (style {:background-color (:bg-blue colors)
                           :height           "10px"
                           })}]
      (e-luokka-table e-luokka)
      [:br {:style (style {:background-color (:bg-blue colors)
                           :height           "10px"
                           })}]
      [:table {:style (style {:width            "550px"
                              :background-color "white"
                              :display          "table"
                              :margin           "0 auto"
                              :margin-bottom    "0px"})}
       [:thead
        [:tr
         [:th]
         [:th "kWhE /(m2vuosi)"]]]
       [:tbody
        [:tr [:th {:scope "row"} "Rakennuksen laskennallinenenergiatehokkuuden vertailuluku eli E-luku"] [:td "104"]]
        [:tr [:th {:scope "row"} "Uuden rakennuksen E-luvun vaatimus"] [:td "<= 135"]]]]
      [:br {:style (style {:background-color (:bg-blue colors)
                           :height           "10px"
                           })}]
      [:div {:style (style {:width            "670px"
                            :background-color "white"
                            :display          "table"
                            :margin           "0 auto"})}
       [:div {:style "display: table-row; border: 3px solid green;"}
        [:div {:style (str "background-color: white; display: table-cell;")}
         [:dl {:style "background-color: white; display: table; "}
          [:dt "Todistuksen laatija:"]
          [:dd "Specimen-Potex, Liisa"]
          [:dt "Sähköinen allekirjoitus:"]
          [:dd ""]]]

        [:div {:style "background-color: white; display: table-cell;"}
         [:dl {:style "background-color: white; display: table;"}
          [:dt "Yritys"]
          [:dd "Mun Yritys Oy"]]]]

       [:div {:style (str "display: table-row;")}
        [:div {:style (str "background-color: white; display: table-cell; border-top: 2px solid " (:bg-blue colors) "; border-right: 1px solid " (:bg-blue colors))}
         [:dl {:style "background-color: white; display: table; "}
          [:dt "Todistuksen laatimispäivä:"]
          [:dd "18.08.2025"]]]

        [:div {:style (str "background-color: white; display: table-cell; border-top: 2px solid " (:bg-blue colors) "; border-right: 1px solid " (:bg-blue colors))}
         [:dl {:style "background-color: white; display: table;"}
          [:dt "Viimeinen voimassaolopäivä"]
          [:dd "18.08.2035"]]]]])
    (page
      [:h2 {:style (style {:text-transform "uppercase" :color (:bg-blue colors)})} "Yhteenveto rakennuksen energiatehokkuudesta"]
      [:h3
       "Laskennallinen ostoenergiankulutus ja energiatehokkuuden vertailuluku (E-luku)"]
      [:table
       [:tbody
        [:tr
         [:th {:scope "row"} "Lämmitetty nettoala"]
         [:td "150000,0"]]
        [:tr
         [:th {:scope "row"} "Lämmitysjärjestelmän kuvaus"]
         ;; TODO: What is going on here?
         [:td "Kaukolämpö, Toissijaisen lämmitysjärjestelmän kuvaus" [:br] "Vesikiertoinen lattialämmitys"]]
        [:tr
         [:th {:scope "row"} "Ilmanvaihtojärjestelmän kuvaus"]
         [:td "Koneellinen poistoilmanvaihtojärjestelmä"]]]]
      [:table
       [:thead
        [:tr
         [:th "Käytettävä energiamuoto"]
         [:th {:colspan "2"} "Vakioidulla käytöllä laskettu ostoenergia"]
         [:th "Energiamuodon kerroin"]
         [:th "Energiamuodon kertoimella painotettu energiankulutus"]]
        [:tr
         [:th ""]
         [:th "kWh/vuosi"]
         [:th "kWh/(m" [:sup 2] "vuosi)"]
         [:th ""]
         [:th "kWh" [:sub "E"] "/m" [:sup 2] "vuosi)"]]]
       [:tbody
        [:tr
         [:th {:scope "row"} "kaukolämpö"]
         [:td "70000000"]
         [:td "47"]
         [:td "0,5"]
         [:td "23"]]
        [:tr
         [:th {:scope "row"} "sähkö"]
         [:td "10000000"]
         [:td "47"]
         [:td "1,2"]
         [:td "80"]]
        [:tr
         [:th {:scope "row"} "uusiutuva polttoaine"]
         [:td "7000"]
         [:td "67"]
         [:td "0,5"]
         [:td "0"]]
        [:tr
         [:th {:scope "row"} "fossiilinen polttoaine"]
         [:td "0"]
         [:td "47"]
         [:td "1"]
         [:td "0"]]
        [:tr
         [:th {:scope "row"} "kaukojäähdytys"]
         [:td "0"]
         [:td "47"]
         [:td "0,28"]
         [:td "0"]]
        [:tr
         [:th {:colspan "4" :scope "row"} "Enregiatehokkuuden vertailuluku (E-luku)"]
         [:td "104"]]]])
    (page
      [:h2 "E-LUVUN LASKENNAN LÄHTÖTIEDOT"]
      [:h3 {:style (style {:background-color (:bg-blue colors)
                           :color            "white"})} "Rakennuskohde"]
      [:div {:style "background-color: white; display: table;"}
       [:div {:style "background-color: white; display: table-cell;"}
        [:dl {:style "background-color: white; display: table;"}
         [:dt "Rakennuksen käyttötarkoitusluokka"]
         [:dd "Tavaratalot"]
         [:dt "Rakennuksen valmistumisvuosi"]
         [:dd "2018"]]]
       [:div {:style "background-color: white; display: table-cell;"}
        [:dl {:style "background-color: white; display: table;"}
         [:dt "Lämmitetty nettoala"]
         [:dd "150000 m^2"]]]])]])

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