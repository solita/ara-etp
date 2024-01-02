(ns solita.etp.service.e-luokka-test
  (:require [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.service.e-luokka :as service]
            [solita.common.map :as map]))

(t/use-fixtures :each ts/fixture)

(t/deftest raja-asteikko-without-kertoimet-test
  (t/is (= [[10 "A"] [100 "B"] [550 "C"]]
           (service/raja-asteikko-without-kertoimet
            [[100 0.9 "A"] [200 1 "B"] [300 -2.5 "C"]] 100))))

(t/deftest e-luokka-from-raja-asteikko-test
  (let [raja-asteikko [[10 "A"] [20 "B"] [30 "C"] [40 "D"] [50 "E"] [60 "F"]]]
    (t/is (= "A" (service/e-luokka-from-raja-asteikko raja-asteikko 9)))
    (t/is (= "A" (service/e-luokka-from-raja-asteikko raja-asteikko 10)))
    (t/is (= "B" (service/e-luokka-from-raja-asteikko raja-asteikko 11)))
    (t/is (= "C" (service/e-luokka-from-raja-asteikko raja-asteikko 29)))
    (t/is (= "D" (service/e-luokka-from-raja-asteikko raja-asteikko 31)))
    (t/is (= "E" (service/e-luokka-from-raja-asteikko raja-asteikko 41)))
    (t/is (= "F" (service/e-luokka-from-raja-asteikko raja-asteikko 59)))
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko 61)))))

(defn find-e-luokka-info [versio alakayttotarkoitus-id nettoala e-luku]
  (-> (service/find-e-luokka ts/*db* versio alakayttotarkoitus-id nettoala e-luku)
      (map/dissoc-in [:kayttotarkoitus :valid])))

(t/deftest find-e-luokka-info-test
  (t/is (= {:e-luokka "A"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Erilliset pientalot"
                         :label-sv "1. Fristående småhus"}
            :raja-asteikko [[94 "A"] [164 "B"] [204 "C"] [284 "D"] [414 "E"]
                            [484 "F"]]}
           (find-e-luokka-info 2013 "YAT" 100 1)))
  (t/is (= {:e-luokka "A"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Erilliset pientalot"
                         :label-sv "1. Fristående småhus"}
            :raja-asteikko [[79 "A"] [125 "B"] [162 "C"] [242 "D"] [372 "E"]
                            [442 "F"]]}
           (find-e-luokka-info 2013 "YAT" 150 1)))
  (t/is (= {:e-luokka "A"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[90 "A"] [155 "B"] [192 "C"] [272 "D"] [402 "E"]
                            [472 "F"]]
            :raja-uusi-2018 140}
           (find-e-luokka-info 2018 "KAT" 100 1)))
  (t/is (= {:e-luokka "B"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Erilliset pientalot"
                         :label-sv "1. Fristående småhus"}
            :raja-asteikko [[94 "A"] [164 "B"] [204 "C"] [284 "D"] [414 "E"]
                            [484 "F"]]}
           (find-e-luokka-info 2013 "MAEP" 100 95)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 2
                         :label-fi "2. Rivi- ja ketjutalot"
                         :label-sv "2. Rad- och kedjehus"}
            :raja-asteikko [[80 "A"] [110 "B"] [150 "C"] [210 "D"] [340 "E"]
                            [410 "F"]]
            :raja-uusi-2018 105}
           (find-e-luokka-info 2013 "RK" 100 151)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[80 "A"] [110 "B"] [150 "C"] [210 "D"] [340 "E"]
                            [410 "F"]]
            :raja-uusi-2018 105}
           (find-e-luokka-info 2018 "RT" 100 151)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 2
                         :label-fi "2. Asuinkerrostalot"
                         :label-sv "2. Flervåningsbostadshus"}
            :raja-asteikko [[75 "A"] [100 "B"] [130 "C"] [160 "D"] [190 "E"]
                            [240 "F"]]
            :raja-uusi-2018 90}
           (find-e-luokka-info 2018 "AK3" 100 151)))
  (t/is (= {:e-luokka "E"
            :kayttotarkoitus {:id 4
                         :label-fi "4. Toimistorakennukset"
                         :label-sv "4. Kontorsbyggnader"}
            :raja-asteikko [[80 "A"] [120 "B"] [170 "C"] [200 "D"] [240 "E"]
                            [300 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2013 "T" 100 240)))
  (t/is (= {:e-luokka "E"
            :kayttotarkoitus {:id 3
                         :label-fi "3. Toimistorakennukset"
                         :label-sv "3. Kontorsbyggnader"}
            :raja-asteikko [[80 "A"] [120 "B"] [170 "C"] [200 "D"] [240 "E"]
                            [300 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2018 "TE" 100 240)))
  (t/is (= {:e-luokka "F"
            :kayttotarkoitus {:id 5
                         :label-fi "5. Liikerakennukset"
                         :label-sv "5. Affärsbyggnader"}
            :raja-asteikko  [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"]
                             [390 "F"]]
            :raja-uusi-2018 135}
           (find-e-luokka-info 2013 "TOKK" 100 380)))
  (t/is (= {:e-luokka "F"
            :kayttotarkoitus {:id 4
                         :label-fi "4. Liikerakennukset"
                         :label-sv "4. Affärsbyggnader"}
            :raja-asteikko [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"]
                            [390 "F"]]
            :raja-uusi-2018 135}
           (find-e-luokka-info 2018 "MH" 100 380)))
  (t/is (= {:e-luokka "C"
            :kayttotarkoitus {:id 5
                         :label-fi "5. Liikerakennukset"
                         :label-sv "5. Affärsbyggnader"}
            :raja-asteikko [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"]
                            [390 "F"]]
            :raja-uusi-2018 135}
           (find-e-luokka-info 2013 "N" 100 240)))
  (t/is (= {:e-luokka "C"
            :kayttotarkoitus {:id 4
                         :label-fi "4. Liikerakennukset"
                         :label-sv "4. Affärsbyggnader"}
            :raja-asteikko [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"]
                            [390 "F"]]
            :raja-uusi-2018 135}
           (find-e-luokka-info 2018 "N" 100 240)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 7
                         :label-fi "7. Opetusrakennukset ja päiväkodit"
                         :label-sv "7. Undervisningsbyggnader och daghem"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [230 "D"] [300 "E"]
                            [360 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2013 "AOR" 100 230)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 6
                         :label-fi "6. Opetusrakennukset ja päiväkodit"
                         :label-sv "6. Undervisningsbyggnader och daghem"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [230 "D"] [300 "E"]
                            [360 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2018 "OR" 100 230)))
  (t/is (= {:e-luokka "G"
            :kayttotarkoitus {:id 10
                              :label-fi "10. Liikuntahallit, uimahallit, jäähallit, liikenteen rakennukset"
                              :label-sv "10. Idrottshallar, simhallar, ishallar, byggnader för trafik"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"]
                            [280 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2013 "TSSJ" 100 282)))
  (t/is (= {:e-luokka "G"
            :kayttotarkoitus {:id 7
                         :label-fi "7. Liikuntahallit, lukuun ottamatta uimahalleja ja jäähalleja"
                         :label-sv "7. Idrottshallar, med undantag för simhallar och ishallar"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"]
                            [280 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2018 "LH" 100 282)))
  (t/is (= {:e-luokka "E"
            :kayttotarkoitus {:id 9
                         :label-fi "9. Sairaalat"
                         :label-sv "9. Sjukhus"}
            :raja-asteikko [[150 "A"] [350 "B"] [450 "C"] [550 "D"] [650 "E"]
                            [800 "F"]]
            :raja-uusi-2018 320}
           (find-e-luokka-info 2013 "MS" 100 650)))
  (t/is (= {:e-luokka "E"
            :kayttotarkoitus {:id 8
                         :label-fi "8. Sairaalat"
                         :label-sv "8. Sjukhus"}
            :raja-asteikko [[150 "A"] [350 "B"] [450 "C"] [550 "D"] [650 "E"]
                            [800 "F"]]
            :raja-uusi-2018 320}
           (find-e-luokka-info 2018 "S" 100 650)))
  (t/is (= {:e-luokka "B"
            :kayttotarkoitus {:id 9
                         :label-fi "9. Muut rakennukset"
                         :label-sv "9. Övriga byggnader"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"]
                            [280 "F"]]}
           (find-e-luokka-info 2018 "JH" 100 130)))
  (t/is (= {:e-luokka "G"
            :kayttotarkoitus {:id 10
                              :label-fi "10. Liikuntahallit, uimahallit, jäähallit, liikenteen rakennukset"
                              :label-sv "10. Idrottshallar, simhallar, ishallar, byggnader för trafik"}
            :raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"]
                            [280 "F"]]
            :raja-uusi-2018 100}
           (find-e-luokka-info 2013 "MUJH" 100 282)))
  (t/is (= {:e-luokka "D"
            :kayttotarkoitus {:id 11
                              :label-fi "11. Varastorakennukset ja erilliset moottoriajoneuvosuojat"
                              :label-sv "11. Lagerbyggnader och fristående utrymmen för motorfordon"}
            :raja-asteikko [[75 "A"] [115 "B"] [155 "C"] [175 "D"] [225 "E"]
                            [265 "F"]]}
           (find-e-luokka-info 2013 "MRVR" 1000 156)))
  (t/is (nil? (find-e-luokka-info 2018 "NONEXISTING" 100 130)))
  (t/is (nil? (find-e-luokka-info 2080 "MAEP" 100 130))) )
