(ns solita.etp.service.energiatodistus-2026-pdf
  (:require [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.watermark-pdf :as watermark-pdf]
            [solita.etp.config :as config]))

(def draft-watermark-texts {"fi" "LUONNOS"
                            "sv" "UTKAST"})

(def test-watermark-texts {"fi" "TESTI"
                           "sv" "TEST"})

(defn styles []
  "@page {
     @bottom-center {
       content: counter(page) \" / \" counter(pages);
     }
     @bottom-right {
        content: string(id-string);
     }
   }
   .id-string {
      string-set: id-string content();
      display: none;
   }")

(defn generate-pdf [energiatodistus kieli draft?]
  (let [id (:id energiatodistus)
        html-content (str "<html><body><h1>ET2026 Placeholder</h1><div class='id-string'>" id "</div></body></html>") 
        pdf-bytes (pdf-service/generate-pdf->bytes {:data {:content html-content :styles (styles)}})]
    (cond
      draft?
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes (get draft-watermark-texts kieli "LUONNOS"))
      (contains? #{"local-dev" "dev" "test"} config/environment-alias)
      (watermark-pdf/apply-watermark-to-bytes pdf-bytes (get test-watermark-texts kieli "TESTI"))
      :else pdf-bytes)))
