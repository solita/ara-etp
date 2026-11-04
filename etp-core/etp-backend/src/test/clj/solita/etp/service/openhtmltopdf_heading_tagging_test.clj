(ns solita.etp.service.openhtmltopdf-heading-tagging-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [hiccup.core :as hiccup]
            [solita.etp.service.pdf :as pdf])
  (:import (java.io ByteArrayOutputStream)
           (java.nio.file Files)
           (org.apache.pdfbox Loader)
           (org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure
            PDStructureElement
            PDStructureNode)))

(def ^:private base-css
  "@page { size: A4; margin: 0; }
   body { margin: 0; font-family: carlito, sans-serif; }
   .page { position: relative; width: 210mm; min-height: 297mm; }
   .page-header { height: 37.4mm; width: 100%; background: #2c5234; }
   .page-title {
     color: white;
     margin: 0;
     padding: 8.6mm 16mm 0 15.1mm;
     font-weight: bold;
   }
   .vaihe-title { font-size: 30pt; }
   .page-content { padding: 10mm; }
   h3 { font-size: 13pt; color: #2c5234; }
   .ppp-vaihe-page-header { position: relative; }
   .ppp-vaihe-page-title {
     width: 152.7mm;
     padding-left: 15.45mm;
     position: relative;
   }
   .ppp-vaihe-title-arrow {
     position: absolute;
     top: 7mm;
     left: 156mm;
   }")

(defn- heading-hiccup [variant]
  (let [title
        (case variant
          :current
          [:h2 {:class "page-title vaihe-title ppp-vaihe-page-title"}
           "Vaiheessa 1" [:br] "toteutettavat toimenpiteet"]

          :without-svg
          [:h2 {:class "page-title vaihe-title ppp-vaihe-page-title"}
           "Vaiheessa 1" [:br] "toteutettavat toimenpiteet"]

          :without-positioning
          [:h2 {:class "page-title vaihe-title"}
           "Vaiheessa 1" [:br] "toteutettavat toimenpiteet"]

          :without-line-break
          [:h2 {:class "page-title vaihe-title ppp-vaihe-page-title"}
           "Vaiheessa 1 toteutettavat toimenpiteet"]

          :generic-header
          [:h2 {:class "page-title vaihe-title"}
           "Vaiheessa 1 toteutettavat toimenpiteet"])]
    [:html
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "description" :content "Heading tagging reproduction"}]
      [:title "Heading tagging reproduction"]
      [:style base-css]]
     [:body
      [:div {:class "page"}
        [:h1 {:class "page-title vaihe-title"} "Perusparannuspassi"]
        [:h2 "Perusparannuspassissa ehdotettujen toimenpiteiden vaikutukset"]
        [:h3 "Energiatehokkuusluokan ja E-luvun muutos"]]
       [:div {:class "page"}
        [:div {:class "page-header ppp-vaihe-page-header"}
         title
         (when (= variant :current)
          [:svg {:class "ppp-vaihe-title-arrow"
                 :xmlns "http://www.w3.org/2000/svg"
                 :viewBox "0 0 10 10"
                 :alt "Vaiheen energialuokka"}
           [:path {:d "M0 0 L10 5 L0 10 Z"}]])]
       [:div {:class "page-content"}
        [:h3 "Toimenpide-ehdotukset 2030–2050"]]]]]))

(defn- structure-children [node]
  (when (instance? PDStructureNode node)
    (.getKids ^PDStructureNode node)))

(defn- structure-elements [node]
  (if (instance? PDStructureElement node)
    (cons node (mapcat structure-elements (structure-children node)))
    (mapcat structure-elements (structure-children node))))

(defn- render-case! [output-directory variant]
  (let [html (hiccup/html (heading-hiccup variant))
        output-file (io/file output-directory (str (name variant) ".pdf"))
        html-file (io/file output-directory (str (name variant) ".html"))
        output-stream (ByteArrayOutputStream.)]
    (.mkdirs output-directory)
    (spit html-file html)
    (pdf/html->pdf html output-stream)
    (with-open [file-output (io/output-stream output-file)]
      (.write file-output (.toByteArray output-stream)))
    (with-open [document (Loader/loadPDF (.toByteArray output-stream))]
      (let [root (.getStructureTreeRoot (.getDocumentCatalog document))
            elements (when root (structure-elements root))]
        {:html-file (.getPath html-file)
         :pdf-file (.getPath output-file)
         :tagged? (some? root)
         :structure-types (mapv #(.getStructureType ^PDStructureElement %)
                                elements)
         :heading-types (->> elements
                             (map #(.getStructureType ^PDStructureElement %))
                             (filter #(re-matches #"H[1-6]" %))
                             vec)}))))

(defn- temporary-output-directory []
  (-> (Files/createTempDirectory
        "ara-etp-openhtmltopdf-heading-tagging-"
        (make-array java.nio.file.attribute.FileAttribute 0))
      .toFile))

(defn- delete-directory! [directory]
  (with-open [paths (Files/walk (.toPath directory)
                                (make-array java.nio.file.FileVisitOption 0))]
    (doseq [path (sort-by #(.getNameCount %) > (iterator-seq (.iterator paths)))]
      (Files/deleteIfExists path))))

(defn- with-output-directory [f]
  (let [directory (temporary-output-directory)]
    (try
      (f directory)
      (finally
        (when-not (= "true" (System/getProperty
                              "ara.etp.keep-heading-tagging-artifacts"))
          (delete-directory! directory))))))

(t/deftest openhtmltopdf-heading-tagging-reproduction-test
  (with-output-directory
    (fn [output-directory]
      (println "Heading tagging artifacts:" (.getPath output-directory))
      (doseq [[variant expected-heading-types]
              [[:current             ["H1" "H2" "H3" "H3"]]
               [:without-svg         ["H1" "H2" "H3" "H3"]]
               [:without-positioning ["H1" "H2" "H3" "H2" "H3"]]
               [:without-line-break   ["H1" "H2" "H3" "H3"]]
               [:generic-header      ["H1" "H2" "H3" "H2" "H3"]]]]
        (let [result (render-case! output-directory variant)]
          (println (pr-str (assoc result :variant variant)))
          (t/is (:tagged? result))
          (t/is (= expected-heading-types (:heading-types result))))))))

(t/deftest openhtmltopdf-positioned-heading-limitation-test
  (with-output-directory
    (fn [output-directory]
      (let [current (render-case! output-directory :current)
            without-positioning (render-case! output-directory :without-positioning)]
        ;; OpenHTMLToPDF currently drops this H2 from the PDF structure when the
        ;; heading itself has position: relative. Update this expected value if a
        ;; future OpenHTMLToPDF version preserves the heading structure.
        (t/is (= ["H1" "H2" "H3" "H3"]
                 (:heading-types current)))
        ;; This is the control case: removing position: relative restores the H2.
        (t/is (= ["H1" "H2" "H3" "H2" "H3"]
                 (:heading-types without-positioning)))))))
