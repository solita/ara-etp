(ns solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset-test
  (:require [clojure.test :as t]
            [solita.etp.service.perusparannuspassi-pdf.toimenpiteiden-vaikutukset :as toimenpiteiden-vaikutukset]))

(t/deftest colors-by-e-luokka-test
  (t/testing "PPP SVG arrows use the same energy class colors as the energiatodistus palette"
    (t/is (= {"A+" "#009641"
              "A0" "#52ae32"
              "A"  "#c8d302"
              "B"  "#ffed00"
              "C"  "#fbb900"
              "D"  "#ec6608"
              "E"  "#e50104"
              "F"  "#e40202"
              "G"  "#e40202"}
             toimenpiteiden-vaikutukset/colors-by-e-luokka))))

(defn- arrow-text-fills [e-luokka]
  (let [color (get toimenpiteiden-vaikutukset/colors-by-e-luokka e-luokka)
        arrow (toimenpiteiden-vaikutukset/arrow color 0 "Vaihe" (str e-luokka " - 100"))]
    (mapv #(-> % second :fill) (drop 3 arrow))))

(t/deftest arrow-text-color-test
  (t/testing "PPP SVG arrow text is black for A+–D energy classes"
    (doseq [e-luokka ["A+" "A0" "A" "B" "C" "D"]]
      (t/is (= ["#000000" "#000000"] (arrow-text-fills e-luokka)))))

  (t/testing "PPP SVG arrow text is white for E–G energy classes"
    (doseq [e-luokka ["E" "F" "G"]]
      (t/is (= ["#ffffff" "#ffffff"] (arrow-text-fills e-luokka))))))
