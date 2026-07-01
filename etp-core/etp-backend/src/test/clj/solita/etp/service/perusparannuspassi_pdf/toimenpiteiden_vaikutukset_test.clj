(ns solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset-test
  (:require [clojure.test :as t]
            [solita.etp.service.pdf-colors-2026 :as pdf-colors-2026]
            [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :as toimenpiteiden-vaikutukset]))

(defn- arrow-text-fills [e-luokka]
  (let [color (pdf-colors-2026/e-luokka-color e-luokka)
        arrow (toimenpiteiden-vaikutukset/arrow color 0 "Vaihe" (str e-luokka " - 100"))]
    (mapv #(-> % second :fill) (drop 3 arrow))))

(defn- arrow-text-fill-styles [e-luokka]
  (let [color (pdf-colors-2026/e-luokka-color e-luokka)
        arrow (toimenpiteiden-vaikutukset/arrow color 0 "Vaihe" (str e-luokka " - 100"))]
    (mapv #(-> % second :style) (drop 3 arrow))))

(t/deftest arrow-text-color-test
  (t/testing "PPP SVG arrow text is black for A+–D energy classes"
    (doseq [e-luokka ["A+" "A0" "A" "B" "C" "D"]]
      (t/is (= ["#000000" "#000000"] (arrow-text-fills e-luokka)))))

  (t/testing "PPP SVG arrow text is white for E–G energy classes"
    (doseq [e-luokka ["E" "F" "G"]]
      (t/is (= ["#ffffff" "#ffffff"] (arrow-text-fills e-luokka)))))

  (t/testing "PPP SVG arrow text fill is also set as an inline style for PDF rendering"
    (doseq [e-luokka ["A+" "A0" "A" "B" "C" "D"]]
      (t/is (= ["fill: #000000;" "fill: #000000;"] (arrow-text-fill-styles e-luokka))))
    (doseq [e-luokka ["E" "F" "G"]]
      (t/is (= ["fill: #ffffff;" "fill: #ffffff;"] (arrow-text-fill-styles e-luokka))))))
