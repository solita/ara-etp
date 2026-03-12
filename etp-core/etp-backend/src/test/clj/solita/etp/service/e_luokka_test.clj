(ns solita.etp.service.e-luokka-test
  (:require [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.service.e-luokka :as service]
            [solita.etp.service.complete-energiatodistus :as complete-service]
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
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko 61))))

  ;; 8-element vector for 2026: A+, A0, A, B, C, D, E, F (G is default)
  (let [raja-asteikko-2026 [[78 "A+"] [98 "A0"] [98 "A"] [106 "B"]
                             [130 "C"] [181 "D"] [265 "E"] [310 "F"]]]
    ;; E-luku exactly at A+ boundary (inclusive)
    (t/is (= "A+" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 78)))
    ;; E-luku below A+ boundary
    (t/is (= "A+" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 0)))
    ;; E-luku above A+, at A0 boundary
    (t/is (= "A0" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 98)))
    ;; A0 and A share same boundary → first match wins → "A0"
    ;; E-luku above A+ but ≤ A0/A → "A0" (first match)
    (t/is (= "A0" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 79)))
    ;; E-luku above A boundary, at B boundary
    (t/is (= "B" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 106)))
    ;; E-luku above B, at C boundary
    (t/is (= "C" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 130)))
    ;; E-luku above F boundary → G (default)
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 311)))
    ;; E-luku very large
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko-2026 9999)))))

(defn find-e-luokka-info
  ([versio alakayttotarkoitus-id nettoala e-luku]
   (find-e-luokka-info versio alakayttotarkoitus-id nettoala e-luku true true))
  ([versio alakayttotarkoitus-id nettoala e-luku
    tayttaa-aplus-vaatimukset tayttaa-a0-vaatimukset]
   (-> (service/find-e-luokka ts/*db* versio alakayttotarkoitus-id nettoala e-luku
                               tayttaa-aplus-vaatimukset tayttaa-a0-vaatimukset)
       (map/dissoc-in [:kayttotarkoitus :valid]))))

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
  (t/is (nil? (find-e-luokka-info 2080 "MAEP" 100 130)))

  ;; ===== 2026 raja-asteikko tests =====

  ;; Pienet asuinrakennukset, 50-150 m², kaavapohjaiset (2026)
  ;; A_netto = 100: A+ = int(114-0.36*100)=78, A0 = int(143-0.45*100)=98, A=98
  ;; B = int(146-0.40*100)=106, C = int(169.5-0.39*100)=130
  ;; D = int(220.5-0.39*100)=181, E = int(303-0.38*100)=265, F = int(349.5-0.39*100)=310
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[78 "A+"] [98 "A0"] [98 "A"] [106 "B"]
                            [130 "C"] [181 "D"] [265 "E"] [310 "F"]]}
           (find-e-luokka-info 2026 "YAT" 100 1)))

  ;; A_netto = 50 (alaraja)
  ;; A+ = int(114-0.36*50)=96, A0 = int(143-0.45*50)=120, A=120
  ;; B = int(146-0.40*50)=126, C = int(169.5-0.39*50)=150
  ;; D = int(220.5-0.39*50)=201, E = int(303-0.38*50)=284, F = int(349.5-0.39*50)=330
  (t/is (= [[96 "A+"] [120 "A0"] [120 "A"] [126 "B"]
             [150 "C"] [201 "D"] [284 "E"] [330 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 50 1))))

  ;; A_netto = 150 (yläraja)
  ;; A+ = int(114-0.36*150)=60, A0 = int(143-0.45*150)=75, A=75
  ;; B = int(146-0.40*150)=86, C = int(169.5-0.39*150)=111
  ;; D = int(220.5-0.39*150)=162, E = int(303-0.38*150)=246, F = int(349.5-0.39*150)=291
  (t/is (= [[60 "A+"] [75 "A0"] [75 "A"] [86 "B"]
             [111 "C"] [162 "D"] [246 "E"] [291 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 150 1))))

  ;; A_netto = 30 (alle 50 m², käytetään samaa 50-150 taulukkoa)
  ;; A+ = int(114-0.36*30)=103, A0 = int(143-0.45*30)=129, A=129
  ;; B = int(146-0.40*30)=134, C = int(169.5-0.39*30)=157
  ;; D = int(220.5-0.39*30)=208, E = int(303-0.38*30)=291, F = int(349.5-0.39*30)=337
  (t/is (= [[103 "A+"] [129 "A0"] [129 "A"] [134 "B"]
             [157 "C"] [208 "D"] [291 "E"] [337 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 30 1))))

  ;; Desimaalien katkaisu: A_netto = 120, A+ = int(114-0.36*120) = int(70.8) = 70
  (t/is (= 70
           (first (first (:raja-asteikko (find-e-luokka-info 2026 "YAT" 120 1))))))

  ;; KAT ja KREP samassa luokassa
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2026 "KAT" 100 1))))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2026 "KREP" 100 1))))

  ;; Pienet asuinrakennukset, 150-600 m² (2026)
  ;; A_netto = 200: A+ = int(64-0.024*200)=59, A0 = int(80-0.030*200)=74, A=74
  ;; B = int(90.1-0.027*200)=84, C = int(118.1-0.047*200)=108
  ;; D = int(168.8-0.045*200)=159, E = int(253.1-0.047*200)=243, F = int(298.1-0.047*200)=288
  (t/is (= [[59 "A+"] [74 "A0"] [74 "A"] [84 "B"]
             [108 "C"] [159 "D"] [243 "E"] [288 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 200 1))))

  ;; A_netto = 600 (150-600 yläraja)
  ;; A+ = int(64-0.024*600)=49, A0 = int(80-0.030*600)=62, A=62
  ;; B = int(90.1-0.027*600)=73, C = int(118.1-0.047*600)=89
  ;; D = int(168.8-0.045*600)=141, E = int(253.1-0.047*600)=224, F = int(298.1-0.047*600)=269
  (t/is (= [[49 "A+"] [62 "A0"] [62 "A"] [73 "B"]
             [89 "C"] [141 "D"] [224 "E"] [269 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 600 1))))

  ;; A_netto = 400, desimaalien katkaisu
  ;; A+ = int(64-0.024*400) = int(54.4) = 54
  ;; A0 = int(80-0.030*400) = int(68.0) = 68
  ;; B = int(90.1-0.027*400) = int(79.3) = 79
  ;; C = int(118.1-0.047*400) = int(99.3) = 99
  ;; D = int(168.8-0.045*400) = int(150.8) = 150
  ;; E = int(253.1-0.047*400) = int(234.3) = 234
  ;; F = int(298.1-0.047*400) = int(279.3) = 279
  (t/is (= [[54 "A+"] [68 "A0"] [68 "A"] [79 "B"]
             [99 "C"] [150 "D"] [234 "E"] [279 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 400 1))))

  ;; Pienet asuinrakennukset, > 600 m² (kiinteä)
  (t/is (= [[50 "A+"] [62 "A0"] [62 "A"] [74 "B"]
             [90 "C"] [142 "D"] [225 "E"] [270 "F"]]
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 700 1))))
  ;; Sama tulos eri nettoaloilla > 600
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 700 1))
           (:raja-asteikko (find-e-luokka-info 2026 "YAT" 1000 1))))

  ;; Nettoala-rajapisteet: 150 vs 200 (eri kaavataulukot), 600 vs 601
  ;; Huom: 150 vs 151 tuottaa identtiset int-katkaisutulokset koska kaavat
  ;; ovat jatkuvia 150 m² kohdalla. Käytetään 200 m² jotta kaltevuuserot näkyvät.
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 150 1))
              (:raja-asteikko (find-e-luokka-info 2026 "YAT" 200 1))))
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 600 1))
              (:raja-asteikko (find-e-luokka-info 2026 "YAT" 601 1))))

  ;; Rivitalot ja 2-kerroksiset (RT, AK2) — 2026, kiinteä
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[57 "A+"] [71 "A0"] [71 "A"] [76 "B"]
                            [103 "C"] [142 "D"] [225 "E"] [270 "F"]]}
           (find-e-luokka-info 2026 "RT" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "RT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2026 "AK2" 100 1))))

  ;; Asuinkerrostalot (AK3) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 2
                         :label-fi "2. Asuinkerrostalot"
                         :label-sv "2. Flervåningsbostadshus"}
            :raja-asteikko [[50 "A+"] [63 "A0"] [63 "A"] [72 "B"]
                            [92 "C"] [111 "D"] [130 "E"] [162 "F"]]}
           (find-e-luokka-info 2026 "AK3" 100 1)))

  ;; Toimistorakennukset (T, TE) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 3
                         :label-fi "3. Toimistorakennukset"
                         :label-sv "3. Kontorsbyggnader"}
            :raja-asteikko [[57 "A+"] [71 "A0"] [71 "A"] [86 "B"]
                            [119 "C"] [139 "D"] [166 "E"] [206 "F"]]}
           (find-e-luokka-info 2026 "T" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "T" 100 1))
           (:raja-asteikko (find-e-luokka-info 2026 "TE" 100 1))))

  ;; Liikerakennukset — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 4
                         :label-fi "4. Liikerakennukset"
                         :label-sv "4. Affärsbyggnader"}
            :raja-asteikko [[78 "A+"] [97 "A0"] [97 "A"] [121 "B"]
                            [167 "C"] [193 "D"] [233 "E"] [267 "F"]]}
           (find-e-luokka-info 2026 "N" 100 1)))

  ;; Majoitusliikerakennukset — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 5
                         :label-fi "5. Majoitusliikerakennukset"
                         :label-sv "5. Byggnader för inkvarteringsanläggningar"}
            :raja-asteikko [[91 "A+"] [114 "A0"] [114 "A"] [123 "B"]
                            [168 "C"] [194 "D"] [234 "E"] [308 "F"]]}
           (find-e-luokka-info 2026 "H" 100 1)))

  ;; Opetusrakennukset ja päiväkodit (OR, PK) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 6
                         :label-fi "6. Opetusrakennukset ja päiväkodit"
                         :label-sv "6. Undervisningsbyggnader och daghem"}
            :raja-asteikko [[57 "A+"] [71 "A0"] [71 "A"] [89 "B"]
                            [114 "C"] [154 "D"] [200 "E"] [240 "F"]]}
           (find-e-luokka-info 2026 "OR" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2026 "OR" 100 1))
           (:raja-asteikko (find-e-luokka-info 2026 "PK" 100 1))))

  ;; Liikuntahallit (LH) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 7
                         :label-fi "7. Liikuntahallit, lukuun ottamatta uimahalleja ja jäähalleja"
                         :label-sv "7. Idrottshallar, med undantag för simhallar och ishallar"}
            :raja-asteikko [[57 "A+"] [71 "A0"] [71 "A"] [93 "B"]
                            [119 "C"] [133 "D"] [166 "E"] [192 "F"]]}
           (find-e-luokka-info 2026 "LH" 100 1)))

  ;; Sairaalat (S) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 8
                         :label-fi "8. Sairaalat"
                         :label-sv "8. Sjukhus"}
            :raja-asteikko [[184 "A+"] [230 "A0"] [230 "A"] [251 "B"]
                            [312 "C"] [377 "D"] [444 "E"] [546 "F"]]}
           (find-e-luokka-info 2026 "S" 100 1)))

  ;; Muut rakennukset (käyttötarkoitusluokka 9) — 2026
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 9
                         :label-fi "9. Muut rakennukset"
                         :label-sv "9. Övriga byggnader"}
            :raja-asteikko [[57 "A+"] [71 "A0"] [71 "A"] [93 "B"]
                            [119 "C"] [133 "D"] [166 "E"] [192 "F"]]}
           (find-e-luokka-info 2026 "JH" 100 1)))

  ;; Vector structure tests
  ;; 2026 vector has 8 elements
  (t/is (= 8 (count (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1)))))
  ;; 2013 vector still has 6 elements (regression)
  (t/is (= 6 (count (:raja-asteikko (find-e-luokka-info 2013 "YAT" 100 1)))))
  ;; 2018 vector still has 6 elements (regression)
  (t/is (= 6 (count (:raja-asteikko (find-e-luokka-info 2018 "KAT" 100 1)))))
  ;; First three labels are A+, A0, A
  (let [asteikko (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1))]
    (t/is (= "A+" (second (nth asteikko 0))))
    (t/is (= "A0" (second (nth asteikko 1))))
    (t/is (= "A" (second (nth asteikko 2)))))
  ;; A0 and A have same boundary value
  (let [asteikko (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1))]
    (t/is (= (first (nth asteikko 1)) (first (nth asteikko 2)))))
  ;; 2026 response does NOT contain :raja-uusi-2018
  (t/is (nil? (:raja-uusi-2018 (find-e-luokka-info 2026 "YAT" 100 1))))

  ;; Integration tests — e-luokka determination
  ;; A+ with very low e-luku
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 1))))
  ;; E-luku = 78 (A+ raja) → A+
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 78))))
  ;; E-luku = 79 (A+ < x ≤ A0) → A0
  (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 79))))
  ;; E-luku = 98 (A0/A raja) → A0 (first match)
  (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 98))))
  ;; E-luku = 99 (> A raja) → B
  (t/is (= "B" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 99))))
  ;; E-luku = 500 (very large) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 500))))
  ;; RT: A+ raja = 57
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "RT" 100 57))))
  ;; RT: A0 raja = 71
  (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "RT" 100 71))))
  ;; AK3: A+ raja = 50
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "AK3" 100 50))))
  ;; AK3: > F raja (162) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2026 "AK3" 100 163))))
  ;; Sairaalat: A+ raja = 184
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "S" 100 184))))
  ;; Sairaalat: > F raja (546) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2026 "S" 100 547))))
  ;; Liikuntahallit: A0 raja = 71
  (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "LH" 100 71))))
  ;; Liikerakennukset: A0 raja = 97
  (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "N" 100 97))))

  ;; Workaround removal regression
  ;; 2026 raja-asteikko differs from 2018
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2026 "YAT" 100 1))
              (:raja-asteikko (find-e-luokka-info 2018 "KAT" 100 1)))) )


(t/deftest luokittelut-2026-test
  (let [luokittelut (complete-service/luokittelut ts/*db*)]
    ;; 2026 kayttotarkoitukset should be present
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2026])))
    ;; 2026 alakayttotarkoitukset should be present
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2026])))
    ;; 2026 kayttotarkoitukset should contain classes 1-9
    (let [kt-2026 (get-in luokittelut [:kayttotarkoitukset 2026])
          ids (set (map :id kt-2026))]
      (t/is (every? ids (range 1 10))))
    ;; 2013 and 2018 are still present (regression)
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2013])))
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2018])))
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2013])))
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2018])))))


(t/deftest downgrade-e-luokka-test
  (t/testing "A+ with different tayttaa-aplus/a0-vaatimukset combinations"
    ;; Given: raakaluokka on "A+"
    ;; When: molemmat vaatimukset täyttyvät
    ;; Then: palautus "A+"
    (t/is (= "A+" (service/downgrade-e-luokka "A+" true true)))
    ;; When: aplus = true, a0 = false → A+ edellyttää molempia
    ;; Then: palautus "A"
    (t/is (= "A" (service/downgrade-e-luokka "A+" true false)))
    ;; When: aplus = false, a0 = true → A+-vaatimus ei täyty, mutta A0 täyttyy
    ;; Then: palautus "A0"
    (t/is (= "A0" (service/downgrade-e-luokka "A+" false true)))
    ;; When: kumpikaan ei täyty
    ;; Then: palautus "A"
    (t/is (= "A" (service/downgrade-e-luokka "A+" false false))))

  (t/testing "A0 with different tayttaa-aplus/a0-vaatimukset combinations"
    ;; Given: raakaluokka on "A0"
    ;; When: a0 = true
    ;; Then: palautus "A0"
    (t/is (= "A0" (service/downgrade-e-luokka "A0" false true)))
    ;; When: a0 = false
    ;; Then: palautus "A"
    (t/is (= "A" (service/downgrade-e-luokka "A0" false false)))
    ;; When: aplus = true, a0 = true → A+-rasti irrelevantti A0-alueella
    ;; Then: palautus "A0"
    (t/is (= "A0" (service/downgrade-e-luokka "A0" true true)))
    ;; When: aplus = true, a0 = false → pelkkä A+-rasti ei riitä A0:een
    ;; Then: palautus "A"
    (t/is (= "A" (service/downgrade-e-luokka "A0" true false))))

  ;; 1.3 Muut luokat: no-op riippumatta rastien tilasta
  (t/testing "Other e-luokka values are never downgraded"
    (t/is (= "A" (service/downgrade-e-luokka "A" true true)))
    (t/is (= "B" (service/downgrade-e-luokka "B" false false)))
    (t/is (= "C" (service/downgrade-e-luokka "C" true true)))
    (t/is (= "D" (service/downgrade-e-luokka "D" false false)))
    (t/is (= "E" (service/downgrade-e-luokka "E" true false)))
    (t/is (= "F" (service/downgrade-e-luokka "F" false true)))
    (t/is (= "G" (service/downgrade-e-luokka "G" true true)))))


(t/deftest find-e-luokka-downgrade-test
  ;; 2.1 Versio 2026 ja downgrade
  (t/testing "2026 e-luokka integrates downgrade logic"
    ;; Given: YAT, nettoala 100, e-luku 1 (A+-alue, A+ raja = 78)
    ;; When: aplus = true, a0 = true
    ;; Then: "A+"
    (t/is (= "A+" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 1 true true))))
    ;; When: aplus = false, a0 = false → downgrade A+ → A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 1 false false))))
    ;; When: aplus = false, a0 = true → downgrade A+ → A0
    (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 1 false true))))

    ;; Given: YAT, nettoala 100, e-luku 79 (A0-alue, A+ raja = 78, A0 raja = 98)
    ;; When: a0 = true
    (t/is (= "A0" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 79 false true))))
    ;; When: a0 = false → downgrade A0 → A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 79 false false))))

    ;; Given: YAT, nettoala 100, e-luku 99 (B-alue)
    ;; When: aplus = true, a0 = true → ei muutu
    (t/is (= "B" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 99 true true))))

    ;; Given: YAT, nettoala 100, e-luku 500 (G-alue)
    ;; When: aplus = true, a0 = true → ei muutu
    (t/is (= "G" (:e-luokka (find-e-luokka-info 2026 "YAT" 100 500 true true)))))

  ;; 2.2 Versio 2013/2018 — no-op regressiotesti
  (t/testing "2013/2018 are unaffected by downgrade params (regression)"
    ;; Given: versio 2018, YAT, e-luku pieni → A
    ;; When: aplus = false, a0 = false
    ;; Then: sama tulos "A" — 2018 ei tuota A+ tai A0
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2018 "KAT" 100 1 false false))))
    ;; Given: versio 2013, YAT, e-luku pieni → A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2013 "YAT" 100 1 false false))))

    ;; Olemassa olevat 2013/2018-testit passaavat jo 4-parametrisellä
    ;; find-e-luokka-info-kutsulla (oletusarvot true, true) — regressio OK
    ))
