(ns solita.etp.service.perusparannuspassi-pdf.vaiheistuksen-yhteenveto-test
  (:require [clojure.test :as t]
            [solita.etp.service.pdf-colors-2026 :as pdf-colors-2026]))

(t/deftest e-luokka-class-test
  (t/testing "PPP PDF energy class names are safe CSS class names for all 2026 energy classes"
    (t/are [luokka expected-class] (= expected-class (pdf-colors-2026/e-luokka-css-class luokka))
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

