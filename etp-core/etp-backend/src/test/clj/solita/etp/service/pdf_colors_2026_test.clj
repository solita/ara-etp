(ns solita.etp.service.pdf-colors-2026-test
  (:require [clojure.test :as t]
            [solita.etp.service.pdf-colors-2026 :as pdf-colors-2026]))

(t/deftest e-luokka-colors-test
  (t/testing "2026 PDF energy class colors match the energiatodistus palette"
    (t/is (= {"A+" "#009641"
              "A0" "#52ae32"
              "A"  "#c8d302"
              "B"  "#ffed00"
              "C"  "#fbb900"
              "D"  "#ec6608"
              "E"  "#e50104"
              "F"  "#e40202"
              "G"  "#e40202"}
             pdf-colors-2026/e-luokka-colors))))

(t/deftest e-luokka-text-colors-test
  (t/testing "2026 PDF energy class text is black for A+–D and white for E–G"
    (doseq [e-luokka ["A+" "A0" "A" "B" "C" "D"]]
      (t/is (= "#000000" (pdf-colors-2026/e-luokka-text-color e-luokka))))
    (doseq [e-luokka ["E" "F" "G"]]
      (t/is (= "#ffffff" (pdf-colors-2026/e-luokka-text-color e-luokka))))))

(t/deftest e-luokka-css-class-test
  (t/testing "2026 PDF energy class CSS class names are safe for all energy classes"
    (t/are [e-luokka expected-class] (= expected-class (pdf-colors-2026/e-luokka-css-class e-luokka))
      "A+" "energialuokka-aplus"
      "A0" "energialuokka-a0"
      "A"  "energialuokka-a"
      "B"  "energialuokka-b"
      "C"  "energialuokka-c"
      "D"  "energialuokka-d"
      "E"  "energialuokka-e"
      "F"  "energialuokka-f"
      "G"  "energialuokka-g"
      nil  nil)))

(t/deftest e-luokka-css-test
  (t/testing "generated CSS contains color and text color for every 2026 energy class"
    (let [css (pdf-colors-2026/e-luokka-css)]
      (doseq [e-luokka pdf-colors-2026/e-luokat]
        (t/is (re-find (re-pattern (str "\\." (pdf-colors-2026/e-luokka-css-class e-luokka) " \\{")) css))
        (t/is (re-find (re-pattern (str "background-color: " (pdf-colors-2026/e-luokka-color e-luokka) ";")) css))
        (t/is (re-find (re-pattern (str "color: " (pdf-colors-2026/e-luokka-text-color e-luokka) ";")) css))))))

