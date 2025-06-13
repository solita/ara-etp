(ns solita.etp.service.watermark-pdf
  (:import (java.util HashMap)
           (org.apache.pdfbox.multipdf Overlay Overlay$Position)
           [org.apache.pdfbox.pdmodel PDDocument PDPage PDPageContentStream]
           [org.apache.pdfbox.pdmodel.font PDType1Font]
           [org.apache.pdfbox.pdmodel.common PDRectangle]
           [org.apache.pdfbox.pdmodel.graphics.state PDExtendedGraphicsState]
           [org.apache.pdfbox.util Matrix]
           [java.awt Color]))

(defn- generate-watermark
  "Generates a watermark PDDocument with the given text diagonally across the page.
   The text is semi-transparent and large enough to cover most of the page.

   Parameters:
   - text: Watermark text (e.g. \"Luonnos\" or \"Utkast\")"
  [text]
  (let [document (PDDocument.)
        page (PDPage. PDRectangle/A4)
        media-box (.getMediaBox page)
        width (.getWidth media-box)
        height (.getHeight media-box)
        font PDType1Font/HELVETICA_BOLD
        opacity 0.23
        font-size 120]

    ;; Add the page to the document
    (.addPage document page)

    ;; Create content stream for the page
    (with-open [content-stream (PDPageContentStream. document page)]
      ;; Save graphics state to restore later
      (.saveGraphicsState content-stream)

      ;; Set transparency
      (.setGraphicsStateParameters content-stream
                                   (doto (PDExtendedGraphicsState.)
                                     (.setNonStrokingAlphaConstant (float opacity))))

      ;; Set color - just use regular black since we're controlling opacity separately
      (.setNonStrokingColor content-stream (Color. 0 128 255))

      ;; Begin text operations
      (.beginText content-stream)

      ;; Set font and size
      (.setFont content-stream font font-size)

      ;; Calculate approximate text width for centering
      (let [text-width (* 0.5 font-size (count text))
            center-x (/ width 2)
            center-y (/ height 2)
            angle-rad (Math/toRadians 60)
            cos-angle (float (Math/cos angle-rad))
            sin-angle (float (Math/sin angle-rad))
            ;; Create transformation matrix for rotation and positioning
            matrix (Matrix.)]

        ;; Position at center of page with rotation
        ;; Full matrix is: [cos, sin, -sin, cos, x, y]
        (.setValue matrix 0 0 cos-angle)       ;; a
        (.setValue matrix 0 1 sin-angle)       ;; b
        (.setValue matrix 1 0 (- sin-angle))   ;; c
        (.setValue matrix 1 1 cos-angle)       ;; d
        (.setValue matrix 2 0 center-x)        ;; x translation
        (.setValue matrix 2 1 center-y)   ;; y translation

        ;; Apply the matrix for rotation around center of page
        (.setTextMatrix content-stream matrix)

        ;; Offset to center text
        (.newLineAtOffset content-stream (/ text-width -2) 0))

      ;; Show text
      (.showText content-stream text)

      ;; End text operations
      (.endText content-stream)

      ;; Restore graphics state
      (.restoreGraphicsState content-stream))

    document))

(defn add-watermark [pdf-path text]
  (with-open [watermark (generate-watermark text)
              overlay (doto (Overlay.)
                        (.setInputFile pdf-path)
                        (.setDefaultOverlayPDF watermark)
                        (.setOverlayPosition Overlay$Position/FOREGROUND))
              result (.overlay overlay (HashMap.))]
    (.save result ^String pdf-path)
    (.close watermark)
    pdf-path))