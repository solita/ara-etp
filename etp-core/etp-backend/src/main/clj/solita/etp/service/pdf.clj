(ns solita.etp.service.pdf
  (:require
    [clojure.java.io :as io]
    [clostache.parser :as clostache])
  (:import
    [com.openhtmltopdf.pdfboxout PdfRendererBuilder PdfRendererBuilder$PdfAConformance]
    [com.openhtmltopdf.svgsupport BatikSVGDrawer]
    (com.openhtmltopdf.outputdevice.helper BaseRendererBuilder$FontStyle)
    (java.io ByteArrayOutputStream InputStream)
    (com.openhtmltopdf.extend FSSupplier)
    (org.apache.pdfbox.multipdf PDFMergerUtility)
    (org.apache.pdfbox.io MemoryUsageSetting)))

(defn- add-font [builder font-resource font-weight font-style]
  (.useFont builder (reify FSSupplier
                      (supply [^InputStream _]
                        (-> font-resource io/resource io/input-stream)))  "roboto" (Integer/valueOf font-weight) font-style true))

(defn- render-template-with-content [template data layout]
  (if-let [content (when template
                     (clostache/render template data))]
    (clostache/render-resource layout {:content content})
    (clostache/render-resource layout data)))

(defn ^:dynamic html->pdf
  [html-doc output-stream]
  (let [builder (PdfRendererBuilder.)]
    (.useFastMode builder)
    (.withHtmlContent builder html-doc nil)

    (.usePdfAConformance builder PdfRendererBuilder$PdfAConformance/PDFA_1_A)

    (add-font builder "fonts/Roboto-Bold.ttf" 700 BaseRendererBuilder$FontStyle/NORMAL)
    (add-font builder "fonts/Roboto-BoldItalic.ttf" 700 BaseRendererBuilder$FontStyle/ITALIC)
    (add-font builder "fonts/Roboto-Italic.ttf" 400 BaseRendererBuilder$FontStyle/ITALIC)
    (add-font builder "fonts/Roboto-Regular.ttf" 400 BaseRendererBuilder$FontStyle/NORMAL)

    (.useSVGDrawer builder (BatikSVGDrawer.))

    (.toStream builder output-stream)
    (.run builder)))

(defn generate-pdf->bytes [{:keys [layout template data]
                            :or {layout "pdf/template.html"}}]
   (with-open [baos (ByteArrayOutputStream.)]
     (-> template
         (render-template-with-content data layout)
         (html->pdf baos))
     (.toByteArray baos)))


(defn generate-pdf->input-stream [opts]
  (io/input-stream (generate-pdf->bytes opts)))

(defn merge-pdf [pdfs]
  (with-open [baos (ByteArrayOutputStream.)]
    (let [pmu (PDFMergerUtility.)]
      (doseq [pdf pdfs]
        (.addSource pmu pdf))
      (.setDestinationStream pmu baos)
      (.mergeDocuments pmu (MemoryUsageSetting/setupMainMemoryOnly)))
    (.toByteArray baos)))
