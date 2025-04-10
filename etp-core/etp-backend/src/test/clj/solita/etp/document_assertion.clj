(ns solita.etp.document-assertion
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :as t]
            [solita.etp.service.pdf :as pdf])
  (:import (java.io ByteArrayOutputStream FileOutputStream InputStream)
           (java.util Arrays)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.rendering ImageType PDFRenderer)
           (org.apache.pdfbox.tools.imageio ImageIOUtil)))

(def original-html->pdf pdf/html->pdf)
(def html-base-template-path "documents/base-template.html")

(defn html->pdf-with-update
  "Wraps solita.etp.service.pdf/html->pdf so that the rendered html document is written to a
  file. Used with html->pdf-with-assertion to update the snapshot, so that you remove the
  content of the document (note: don't remove it, or io/resource will not know where in the
  possible load paths is has to be) and run the tests to create a new snapshot."
  [doc-path-to-compare-to html->pdf-called? html-doc output-stream]
  (spit (io/resource doc-path-to-compare-to) (subs html-doc (-> html-base-template-path io/resource slurp count)))
  (reset! html->pdf-called? true)
  (original-html->pdf html-doc output-stream))

(defn html->pdf-with-assertion
  "Wraps solita.etp.service.pdf/html->pdf so that the document can be asserted in html format before it's
  rendered to pdf. Provide first two parameters by using partial and redef the original function with the
  returned function."
  [doc-path-to-compare-to html->pdf-called? html-doc output-stream]
  ;; Mocking the pdf rendering function so that the document contents can be asserted
  ;; Compare the created document to the snapshot
  (t/is (= html-doc
           (str
            (slurp (io/resource html-base-template-path))
            (slurp (io/resource doc-path-to-compare-to)))))
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

(defn save-new-snapshot
  "If you want to update the snapshot of the document, call this function with the document and the path to the snapshot.
   Example usage inside assert-pdf-matches-visually function: (save-new-snapshot pdf-under-testing baseline-pdf-resource-path)"
  [^PDDocument pdf-doc snapshot-path-in-resources]
  (let [target-path (str "./src/test/resources/" snapshot-path-in-resources)]
    (io/make-parents target-path)
    (.save pdf-doc target-path)))

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

    (t/is (not (zero? (.getNumberOfPages pdf-under-testing)))
          "Generated document should have pages")

    (t/is (not (zero? (.getNumberOfPages baseline-pdf)))
          "Baseline document should have pages")

    (t/is (= (.getNumberOfPages pdf-under-testing)
             (.getNumberOfPages baseline-pdf))
          "Page count of generated and baseline documents should match")

    ;; Asserts that every page matches visually
    (doseq [page-number (range 0 (.getNumberOfPages pdf-under-testing))
            :let [rendered-image (pdf-page->image-byte-array pdf-under-testing page-number)]]
      (t/testing (str "page " (inc page-number))
        (t/is (Arrays/equals (pdf-page->image-byte-array pdf-under-testing page-number)
                             (pdf-page->image-byte-array baseline-pdf page-number))
              "If change is intended, save new document snapshot in the test with save-new-snapshot function"))

      ;; Write the image in build directory so it can be compared manually
      (let [debug-image-path (str "./target/" filename "-page-" (inc page-number) ".png")]
        (io/make-parents debug-image-path)
        (with-open [output (FileOutputStream. debug-image-path)]
          (.write output rendered-image))))))

