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

  ;; 7-element vector for 2027: A+, A, B, C, D, E, F (G is default)
  (let [raja-asteikko-2027 [[78 "A+"] [98 "A"] [106 "B"]
                             [130 "C"] [181 "D"] [265 "E"] [310 "F"]]]
    ;; E-luku exactly at A+ boundary (inclusive)
    (t/is (= "A+" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 78)))
    ;; E-luku below A+ boundary
    (t/is (= "A+" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 0)))
    ;; E-luku above A+, at A boundary
    (t/is (= "A" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 98)))
    ;; E-luku above A+ (in the range of the former A0 class) → "A"
    (t/is (= "A" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 79)))
    ;; E-luku above A boundary, at B boundary
    (t/is (= "B" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 106)))
    ;; E-luku above B, at C boundary
    (t/is (= "C" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 130)))
    ;; E-luku above F boundary → G (default)
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 311)))
    ;; E-luku very large
    (t/is (= "G" (service/e-luokka-from-raja-asteikko raja-asteikko-2027 9999)))))

(defn find-e-luokka-info
  ([versio alakayttotarkoitus-id nettoala e-luku]
   (find-e-luokka-info versio alakayttotarkoitus-id nettoala e-luku
                       true service/a0-merkinta-a0))
  ([versio alakayttotarkoitus-id nettoala e-luku
    tayttaa-aplus-vaatimukset a0-merkinta-id]
   (-> (service/find-e-luokka ts/*db* versio alakayttotarkoitus-id nettoala e-luku
                               tayttaa-aplus-vaatimukset a0-merkinta-id)
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

  ;; ===== 2027 raja-asteikko tests =====

  ;; Pienet asuinrakennukset, 50-150 m², kaavapohjaiset (2027)
  ;; A_netto = 100: A+ = int(114-0.36*100)=78, A = int(143-0.45*100)=98
  ;; B = int(146-0.40*100)=106, C = int(169.5-0.39*100)=130
  ;; D = int(220.5-0.39*100)=181, E = int(303-0.38*100)=265, F = int(349.5-0.39*100)=310
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[78 "A+"] [98 "A"] [106 "B"]
                            [130 "C"] [181 "D"] [265 "E"] [310 "F"]]}
           (find-e-luokka-info 2027 "YAT" 100 1)))

  ;; A_netto = 50 (alaraja)
  ;; A+ = int(114-0.36*50)=96, A = int(143-0.45*50)=120
  ;; B = int(146-0.40*50)=126, C = int(169.5-0.39*50)=150
  ;; D = int(220.5-0.39*50)=201, E = int(303-0.38*50)=284, F = int(349.5-0.39*50)=330
  (t/is (= [[96 "A+"] [120 "A"] [126 "B"]
             [150 "C"] [201 "D"] [284 "E"] [330 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 50 1))))

  ;; A_netto = 150 (yläraja)
  ;; A+ = int(114-0.36*150)=60, A = int(143-0.45*150)=75
  ;; B = int(146-0.40*150)=86, C = int(169.5-0.39*150)=111
  ;; D = int(220.5-0.39*150)=162, E = int(303-0.38*150)=246, F = int(349.5-0.39*150)=291
  (t/is (= [[60 "A+"] [75 "A"] [86 "B"]
             [111 "C"] [162 "D"] [246 "E"] [291 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 150 1))))

  ;; A_netto = 30 (alle 50 m², käytetään samaa 50-150 taulukkoa)
  ;; A+ = int(114-0.36*30)=103, A = int(143-0.45*30)=129
  ;; B = int(146-0.40*30)=134, C = int(169.5-0.39*30)=157
  ;; D = int(220.5-0.39*30)=208, E = int(303-0.38*30)=291, F = int(349.5-0.39*30)=337
  (t/is (= [[103 "A+"] [129 "A"] [134 "B"]
             [157 "C"] [208 "D"] [291 "E"] [337 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 30 1))))

  ;; Desimaalien katkaisu: A_netto = 120, A+ = int(114-0.36*120) = int(70.8) = 70
  (t/is (= 70
           (first (first (:raja-asteikko (find-e-luokka-info 2027 "YAT" 120 1))))))

  ;; KAT ja KREP samassa luokassa
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2027 "KAT" 100 1))))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2027 "KREP" 100 1))))

  ;; Pienet asuinrakennukset, 150-600 m² (2027)
  ;; A_netto = 200: A+ = int(64-0.024*200)=59, A = int(80-0.030*200)=74
  ;; B = int(90.1-0.027*200)=84, C = int(118.1-0.047*200)=108
  ;; D = int(168.8-0.045*200)=159, E = int(253.1-0.047*200)=243, F = int(298.1-0.047*200)=288
  (t/is (= [[59 "A+"] [74 "A"] [84 "B"]
             [108 "C"] [159 "D"] [243 "E"] [288 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 200 1))))

  ;; A_netto = 600 (150-600 yläraja)
  ;; A+ = int(64-0.024*600)=49, A = int(80-0.030*600)=62
  ;; B = int(90.1-0.027*600)=73, C = int(118.1-0.047*600)=89
  ;; D = int(168.8-0.045*600)=141, E = int(253.1-0.047*600)=224, F = int(298.1-0.047*600)=269
  (t/is (= [[49 "A+"] [62 "A"] [73 "B"]
             [89 "C"] [141 "D"] [224 "E"] [269 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 600 1))))

  ;; A_netto = 400, desimaalien katkaisu
  ;; A+ = int(64-0.024*400) = int(54.4) = 54
  ;; A = int(80-0.030*400) = int(68.0) = 68
  ;; B = int(90.1-0.027*400) = int(79.3) = 79
  ;; C = int(118.1-0.047*400) = int(99.3) = 99
  ;; D = int(168.8-0.045*400) = int(150.8) = 150
  ;; E = int(253.1-0.047*400) = int(234.3) = 234
  ;; F = int(298.1-0.047*400) = int(279.3) = 279
  (t/is (= [[54 "A+"] [68 "A"] [79 "B"]
             [99 "C"] [150 "D"] [234 "E"] [279 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 400 1))))

  ;; Pienet asuinrakennukset, > 600 m² (kiinteä)
  (t/is (= [[50 "A+"] [62 "A"] [74 "B"]
             [90 "C"] [142 "D"] [225 "E"] [270 "F"]]
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 700 1))))
  ;; Sama tulos eri nettoaloilla > 600
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 700 1))
           (:raja-asteikko (find-e-luokka-info 2027 "YAT" 1000 1))))

  ;; Nettoala-rajapisteet: 150 vs 200 (eri kaavataulukot), 600 vs 601
  ;; Huom: 150 vs 151 tuottaa identtiset int-katkaisutulokset koska kaavat
  ;; ovat jatkuvia 150 m² kohdalla. Käytetään 200 m² jotta kaltevuuserot näkyvät.
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 150 1))
              (:raja-asteikko (find-e-luokka-info 2027 "YAT" 200 1))))
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 600 1))
              (:raja-asteikko (find-e-luokka-info 2027 "YAT" 601 1))))

  ;; Rivitalot ja 2-kerroksiset (RT, AK2) — 2027, kiinteä
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 1
                         :label-fi "1. Pienet asuinrakennukset"
                         :label-sv "1. Små bostadsbyggnader"}
            :raja-asteikko [[57 "A+"] [71 "A"] [76 "B"]
                            [103 "C"] [142 "D"] [225 "E"] [270 "F"]]}
           (find-e-luokka-info 2027 "RT" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "RT" 100 1))
           (:raja-asteikko (find-e-luokka-info 2027 "AK2" 100 1))))

  ;; Asuinkerrostalot (AK3) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 2
                         :label-fi "2. Asuinkerrostalot"
                         :label-sv "2. Flervåningsbostadshus"}
            :raja-asteikko [[50 "A+"] [63 "A"] [72 "B"]
                            [92 "C"] [111 "D"] [130 "E"] [162 "F"]]}
           (find-e-luokka-info 2027 "AK3" 100 1)))

  ;; Toimistorakennukset (T, TE) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 3
                         :label-fi "3. Toimistorakennukset"
                         :label-sv "3. Kontorsbyggnader"}
            :raja-asteikko [[57 "A+"] [71 "A"] [86 "B"]
                            [119 "C"] [139 "D"] [166 "E"] [206 "F"]]}
           (find-e-luokka-info 2027 "T" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "T" 100 1))
           (:raja-asteikko (find-e-luokka-info 2027 "TE" 100 1))))

  ;; Liikerakennukset — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 4
                         :label-fi "4. Liikerakennukset"
                         :label-sv "4. Affärsbyggnader"}
            :raja-asteikko [[78 "A+"] [97 "A"] [121 "B"]
                            [167 "C"] [193 "D"] [233 "E"] [267 "F"]]}
           (find-e-luokka-info 2027 "N" 100 1)))

  ;; Majoitusliikerakennukset — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 5
                         :label-fi "5. Majoitusliikerakennukset"
                         :label-sv "5. Byggnader för inkvarteringsanläggningar"}
            :raja-asteikko [[91 "A+"] [114 "A"] [123 "B"]
                            [168 "C"] [194 "D"] [234 "E"] [308 "F"]]}
           (find-e-luokka-info 2027 "H" 100 1)))

  ;; Opetusrakennukset ja päiväkodit (OR, PK) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 6
                         :label-fi "6. Opetusrakennukset ja päiväkodit"
                         :label-sv "6. Undervisningsbyggnader och daghem"}
            :raja-asteikko [[57 "A+"] [71 "A"] [89 "B"]
                            [114 "C"] [154 "D"] [200 "E"] [240 "F"]]}
           (find-e-luokka-info 2027 "OR" 100 1)))
  (t/is (= (:raja-asteikko (find-e-luokka-info 2027 "OR" 100 1))
           (:raja-asteikko (find-e-luokka-info 2027 "PK" 100 1))))

  ;; Liikuntahallit (LH) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 7
                         :label-fi "7. Liikuntahallit, lukuun ottamatta uimahalleja ja jäähalleja"
                         :label-sv "7. Idrottshallar, med undantag för simhallar och ishallar"}
            :raja-asteikko [[57 "A+"] [71 "A"] [93 "B"]
                            [119 "C"] [133 "D"] [166 "E"] [192 "F"]]}
           (find-e-luokka-info 2027 "LH" 100 1)))

  ;; Sairaalat (S) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 8
                         :label-fi "8. Sairaalat"
                         :label-sv "8. Sjukhus"}
            :raja-asteikko [[184 "A+"] [230 "A"] [251 "B"]
                            [312 "C"] [377 "D"] [444 "E"] [546 "F"]]}
           (find-e-luokka-info 2027 "S" 100 1)))

  ;; Muut rakennukset (käyttötarkoitusluokka 9) — 2027
  (t/is (= {:e-luokka "A+"
            :kayttotarkoitus {:id 9
                         :label-fi "9. Muut rakennukset"
                         :label-sv "9. Övriga byggnader"}
            :raja-asteikko [[57 "A+"] [71 "A"] [93 "B"]
                            [119 "C"] [133 "D"] [166 "E"] [192 "F"]]}
           (find-e-luokka-info 2027 "JH" 100 1)))

  ;; Vector structure tests
  ;; 2027 vector has 7 elements (A0 is not an energy class)
  (t/is (= 7 (count (:raja-asteikko (find-e-luokka-info 2027 "YAT" 100 1)))))
  ;; 2013 vector still has 6 elements (regression)
  (t/is (= 6 (count (:raja-asteikko (find-e-luokka-info 2013 "YAT" 100 1)))))
  ;; 2018 vector still has 6 elements (regression)
  (t/is (= 6 (count (:raja-asteikko (find-e-luokka-info 2018 "KAT" 100 1)))))
  ;; First two labels are A+, A and there is no A0
  (let [asteikko (:raja-asteikko (find-e-luokka-info 2027 "YAT" 100 1))]
    (t/is (= "A+" (second (nth asteikko 0))))
    (t/is (= "A" (second (nth asteikko 1))))
    (t/is (not-any? #(= "A0" (second %)) asteikko)))
  ;; 2027 response does NOT contain :raja-uusi-2018
  (t/is (nil? (:raja-uusi-2018 (find-e-luokka-info 2027 "YAT" 100 1))))

  ;; Integration tests — e-luokka determination
  ;; A+ with very low e-luku
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1))))
  ;; E-luku = 78 (A+ raja) → A+
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 78))))
  ;; E-luku = 79 (A+ < x ≤ A, the former A0 range) → A
  (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 79))))
  ;; E-luku = 98 (A raja) → A
  (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 98))))
  ;; E-luku = 99 (> A raja) → B
  (t/is (= "B" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 99))))
  ;; E-luku = 500 (very large) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 500))))
  ;; RT: A+ raja = 57
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "RT" 100 57))))
  ;; RT: A raja = 71
  (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "RT" 100 71))))
  ;; AK3: A+ raja = 50
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "AK3" 100 50))))
  ;; AK3: > F raja (162) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2027 "AK3" 100 163))))
  ;; Sairaalat: A+ raja = 184
  (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "S" 100 184))))
  ;; Sairaalat: > F raja (546) → G
  (t/is (= "G" (:e-luokka (find-e-luokka-info 2027 "S" 100 547))))
  ;; Liikuntahallit: A raja = 71
  (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "LH" 100 71))))
  ;; Liikerakennukset: A raja = 97
  (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "N" 100 97))))

  ;; Workaround removal regression
  ;; 2027 raja-asteikko differs from 2018
  (t/is (not= (:raja-asteikko (find-e-luokka-info 2027 "YAT" 100 1))
              (:raja-asteikko (find-e-luokka-info 2018 "KAT" 100 1)))) )


(t/deftest luokittelut-2027-test
  (let [luokittelut (complete-service/luokittelut ts/*db*)]
    ;; 2027 kayttotarkoitukset should be present
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2027])))
    ;; 2027 alakayttotarkoitukset should be present
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2027])))
    ;; 2027 kayttotarkoitukset should contain classes 1-9
    (let [kt-2027 (get-in luokittelut [:kayttotarkoitukset 2027])
          ids (set (map :id kt-2027))]
      (t/is (every? ids (range 1 10))))
    ;; 2013 and 2018 are still present (regression)
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2013])))
    (t/is (some? (get-in luokittelut [:kayttotarkoitukset 2018])))
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2013])))
    (t/is (some? (get-in luokittelut [:alakayttotarkoitukset 2018])))))


(def ^:private ei service/a0-merkinta-ei)
(def ^:private a0 service/a0-merkinta-a0)
(def ^:private a0-korjattu service/a0-merkinta-a0-korjattu)
(def ^:private a0-mpr service/a0-merkinta-a0-mpr)
(t/deftest downgrade-e-luokka-test
  (t/testing "A+ requires the A+ flag and an A0 or A0_MPR marking"
    ;; Given: raw class is A+
    ;; When: both requirements are fulfilled
    ;; Then: result is A+
    (t/is (= "A+" (service/downgrade-e-luokka "A+" true a0)))
    (t/is (= "A+" (service/downgrade-e-luokka "A+" true a0-mpr))))
  (t/testing "A+ is downgraded to A when the A0 marking is not eligible"
    ;; When: A+ is set but the marking is not eligible
    ;; Then: result is A
    (t/is (= "A" (service/downgrade-e-luokka "A+" true a0-korjattu)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" true ei)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" true nil))))
  (t/testing "A+ is downgraded to A when the A+ flag is not set"
    ;; When: A+ is not set
    ;; Then: result is A
    (t/is (= "A" (service/downgrade-e-luokka "A+" false a0)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" false a0-mpr)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" false a0-korjattu)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" false ei)))
    (t/is (= "A" (service/downgrade-e-luokka "A+" nil a0))))
  ;; 1.3 Other classes: no-op regardless of the requirement flags.
  (t/testing "Other e-luokka values are never changed"
    (doseq [e-luokka ["A" "B" "C" "D" "E" "F" "G"]
            aplus [true false]
            a0-merkinta-id [ei a0 a0-korjattu a0-mpr]]
      (t/is (= e-luokka (service/downgrade-e-luokka e-luokka aplus a0-merkinta-id))))))
(t/deftest find-e-luokka-downgrade-test
  ;; 2.1 Version 2027 downgrade behavior
  (t/testing "2027 e-luokka integrates downgrade logic"
    ;; Given: YAT, nettoala 100, e-luku 1 (A+-alue, A+ raja = 78)
    ;; When: A+ is set and the marking is eligible
    ;; Then: result is A+
    (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1 true a0))))
    (t/is (= "A+" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1 true a0-mpr))))
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1 true a0-korjattu))))
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1 true ei))))
    ;; When: A+ is not set
    ;; Then: result is A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 1 false a0))))
    ;; Given: YAT, nettoala 100, e-luku 79 (former A0 range, A+ raja = 78, A raja = 98)
    ;; Then: A regardless of the A0 marking
    (doseq [a0-merkinta-id [ei a0 a0-korjattu a0-mpr]]
      (t/is (= "A" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 79 true a0-merkinta-id)))))
    ;; Given: YAT, nettoala 100, e-luku 99 (B-alue) → ei muutu
    ;; When: requirement flags vary
    ;; Then: result remains B
    (t/is (= "B" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 99 true a0))))
    ;; Given: YAT, nettoala 100, e-luku 500 (G-alue) → ei muutu
    ;; When: requirement flags vary
    ;; Then: result remains G
    (t/is (= "G" (:e-luokka (find-e-luokka-info 2027 "YAT" 100 500 true a0)))))
  ;; 2.2 Version 2013/2018 — no-op regression test
  (t/testing "2013/2018 are unaffected by downgrade params (regression)"
    ;; Given: version 2018, YAT, low E-value → A
    ;; When: A+ is false
    ;; Then: result remains A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2018 "KAT" 100 1 false ei))))
    ;; Given: version 2013, YAT, low E-value → A
    (t/is (= "A" (:e-luokka (find-e-luokka-info 2013 "YAT" 100 1 false ei))))))
