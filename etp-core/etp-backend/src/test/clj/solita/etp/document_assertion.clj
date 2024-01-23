(ns solita.etp.document-assertion
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :as t]
            [solita.etp.service.pdf :as pdf])
  (:import (java.io ByteArrayOutputStream FileOutputStream InputStream)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.rendering ImageType PDFRenderer)
           (org.apache.pdfbox.tools.imageio ImageIOUtil)))

(def original-html->pdf pdf/html->pdf)

(defn html->pdf-with-assertion
  "Wraps solita.etp.service.pdf/html->pdf so that the document can be asserted in html format before it's
  rendered to pdf. Provide first two parameters by using partial and redef the original function with the
  returned function."
  [doc-path-to-compare-to html->pdf-called? html-doc output-stream]
  ;; Mocking the pdf rendering function so that the document contents can be asserted
  ;; Compare the created document to the snapshot
  (t/is (= html-doc
           (slurp (io/resource doc-path-to-compare-to))))
  (reset! html->pdf-called? true)
  ;;Calling original implementation to ensure the functionality doesn't change
  (original-html->pdf html-doc output-stream))

(defn read-pdf
  "Reads a pdf into PDFBox Document class from an input stream. This object can then be used for assertions."
  ^PDDocument
  [^InputStream input]
  (PDDocument/load input))

(defn- pdf-page->image-byte-array
  "Renders the page of given pdf to a png image and converts that to a byte array"
  ^bytes
  [^PDDocument pdf page-number]
  (let [renderer (PDFRenderer. pdf)
        image (.renderImageWithDPI renderer page-number 72 ImageType/GRAY)]
    (with-open [output (ByteArrayOutputStream.)]
      (ImageIOUtil/writeImage image "png" output)
      (.toByteArray output))))

(defn assert-pdf-matches-visually
  "Checks that the given pdf document object matches visually to the baseline-pdf at the given resource path"
  [^PDDocument pdf-under-testing baseline-pdf-resource-path]
  (let [baseline-pdf (-> baseline-pdf-resource-path
                         io/resource
                         io/input-stream
                         read-pdf)
        filename (-> baseline-pdf-resource-path
                     io/resource
                     .getFile
                     (string/split #"/")
                     last)]
    ;; Assert that PDFs have pages and the number of pages match
    (t/is (not (zero? (.getNumberOfPages pdf-under-testing))))
    (t/is (not (zero? (.getNumberOfPages baseline-pdf))))
    (t/is (= (.getNumberOfPages pdf-under-testing)
             (.getNumberOfPages baseline-pdf)))

    ;; Asserts that every page matches visually
    (doseq [page-number (range 0 (.getNumberOfPages pdf-under-testing))
            :let [rendered-image (pdf-page->image-byte-array pdf-under-testing page-number)]]
      (t/testing (str "page " (inc page-number))
        (t/is (= (seq (pdf-page->image-byte-array pdf-under-testing page-number))
                 (seq (pdf-page->image-byte-array baseline-pdf page-number)))))

      ;; Write the image in build directory so it can be compared manually
      (with-open [output (FileOutputStream. (str "./target/" filename "-page-" (inc page-number) ".png"))]
        (.write output rendered-image)))))
