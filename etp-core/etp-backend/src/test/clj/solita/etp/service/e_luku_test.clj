(ns solita.etp.service.e-luku-test
  (:require [clojure.test :as t]
            [solita.etp.service.e-luokka :as e-luokka-service])
  (:import (java.math RoundingMode)))

(defn et [nettoala energiamuodot]
  {:lahtotiedot
    {:lammitetty-nettoala nettoala}
   :tulokset
    {:kaytettavat-energiamuodot energiamuodot}})

(defn et-all-same [nettoala ostoenergia]
  (et nettoala {:fossiilinen-polttoaine ostoenergia
                :sahko                  ostoenergia
                :kaukojaahdytys         ostoenergia
                :kaukolampo             ostoenergia
                :uusiutuva-polttoaine   ostoenergia}))

(defn user-defined-energiamuoto [kerroin ostoenergia]
  {:muotokerroin kerroin
   :ostoenergia  ostoenergia})

(t/deftest e-luku-nil
  (t/is (= (e-luokka-service/e-luku 2018 (et 0 {})) nil))
  (t/is (= (e-luokka-service/e-luku 2018 (et 1 nil)) nil))
  (t/is (= (e-luokka-service/e-luku 2000 (et 1 {})) nil))
  (t/is (= (e-luokka-service/e-luku nil (et 1 {})) nil)))

(t/deftest e-luku-0
  (t/is (= (e-luokka-service/e-luku 2018 (et 1 {})) 0M))
  (t/is (= (e-luokka-service/e-luku 2018 (et-all-same 1 nil)) 0M))
  (t/is (= (e-luokka-service/e-luku 2018 (et 1 {:muu nil})) 0M))
  (t/is (= (e-luokka-service/e-luku 2018 (et 1 {:muu []})) 0M)))


(defn test-energiamuoto [versio energiamuoto]
  (t/is (= (e-luokka-service/e-luku versio (et 1 {energiamuoto 1}))
           (.setScale (energiamuoto (e-luokka-service/energiamuotokerroin versio))
                      0 RoundingMode/CEILING))))

(t/deftest e-luku-kerroin-2018
  (test-energiamuoto 2018 :fossiilinen-polttoaine)
  (test-energiamuoto 2018 :sahko)
  (test-energiamuoto 2018 :kaukojaahdytys)
  (test-energiamuoto 2018 :kaukolampo)
  (test-energiamuoto 2018 :uusiutuva-polttoaine))

(t/deftest e-luku-kerroin-2013
  (test-energiamuoto 2013 :fossiilinen-polttoaine)
  (test-energiamuoto 2013 :sahko)
  (test-energiamuoto 2013 :kaukojaahdytys)
  (test-energiamuoto 2013 :kaukolampo)
  (test-energiamuoto 2013 :uusiutuva-polttoaine))

(t/deftest e-luku-muu
  (t/is (= (e-luokka-service/e-luku
             2018 (et 1 {:muu [(user-defined-energiamuoto 1 1)]}))
           1M)))

;; === AE-2614: ET2027 energiamuotokertoimet ===

;; 1.1 - Individual coefficient verification using existing test-energiamuoto helper
(t/deftest e-luku-kerroin-2027
  (test-energiamuoto 2027 :fossiilinen-polttoaine)
  (test-energiamuoto 2027 :sahko)
  (test-energiamuoto 2027 :kaukojaahdytys)
  (test-energiamuoto 2027 :kaukolampo)
  (test-energiamuoto 2027 :uusiutuva-polttoaine))

;; 1.2 - Exact coefficient values for 2027
(t/deftest energiamuotokerroin-2027-values
  (let [kertoimet (get e-luokka-service/energiamuotokerroin 2027)]
    (t/is (= (:sahko kertoimet) 0.90M))
    (t/is (= (:kaukolampo kertoimet) 0.38M))
    (t/is (= (:kaukojaahdytys kertoimet) 0.21M))
    (t/is (= (:fossiilinen-polttoaine kertoimet) 1M))
    (t/is (= (:uusiutuva-polttoaine kertoimet) 0.38M))))

;; 1.3 - Combined E-luku with all energy forms, nettoala=100
(t/deftest e-luku-2027-combined
  ;; Given all energy forms at 10000 and nettoala=100
  ;; When E-luku is calculated with 2027 coefficients
  ;; Then result = ceil((10000*1.00 + 10000*0.90 + 10000*0.21 + 10000*0.38 + 10000*0.38) / 100)
  ;;             = ceil(28700 / 100) = 287
  (t/is (= (e-luokka-service/e-luku 2027 (et-all-same 100 10000))
           287M)))

;; 1.4 - Realistic building sizes with non-trivial nettoala
(t/deftest e-luku-2027-realistic-nettoala
  ;; Given a pientalo with nettoala=150, sähkö=5000, kaukolämpö=12000
  ;; When E-luku is calculated
  ;; Then painotettu = 5000*0.90 + 12000*0.38 = 4500 + 4560 = 9060
  ;;      E-luku = ceil(9060 / 150) = ceil(60.4) = 61
  (t/is (= (e-luokka-service/e-luku
             2027
             (et 150 {:sahko 5000 :kaukolampo 12000}))
           61M))

  ;; Given a kerrostalo with nettoala=3500, sähkö=80000, kaukolämpö=250000, kaukojäähdytys=15000
  ;; When E-luku is calculated
  ;; Then painotettu = 80000*0.90 + 250000*0.38 + 15000*0.21 = 72000 + 95000 + 3150 = 170150
  ;;      E-luku = ceil(170150 / 3500) = ceil(48.614...) = 49
  (t/is (= (e-luokka-service/e-luku
             2027
             (et 3500 {:sahko 80000
                       :kaukolampo 250000
                       :kaukojaahdytys 15000}))
           49M))

  ;; Given a toimisto with nettoala=800, all energy forms=20000
  ;; When E-luku is calculated
  ;; Then painotettu = 20000*(1.00+0.90+0.21+0.38+0.38) = 20000*2.87 = 57400
  ;;      E-luku = ceil(57400 / 800) = ceil(71.75) = 72
  (t/is (= (e-luokka-service/e-luku 2027 (et-all-same 800 20000))
           72M)))

;; 1.5 - Nil/edge cases for 2027
(t/deftest e-luku-2027-nil-cases
  ;; Given zero nettoala, When E-luku calculated, Then nil
  (t/is (= (e-luokka-service/e-luku 2027 (et 0 {})) nil))
  ;; Given nil energiamuodot, When E-luku calculated, Then nil
  (t/is (= (e-luokka-service/e-luku 2027 (et 1 nil)) nil))
  ;; Given empty energiamuodot, When E-luku calculated, Then 0
  (t/is (= (e-luokka-service/e-luku 2027 (et 1 {})) 0M))
  ;; Given all nil ostoenergias, When E-luku calculated, Then 0
  (t/is (= (e-luokka-service/e-luku 2027 (et-all-same 1 nil)) 0M)))

;; 1.6 - User-defined ("muu") energy form with 2027
(t/deftest e-luku-2027-muu
  ;; Given user-defined kerroin=1, ostoenergia=1, nettoala=1
  ;; When E-luku calculated, Then 1
  (t/is (= (e-luokka-service/e-luku
             2027 (et 1 {:muu [(user-defined-energiamuoto 1 1)]}))
           1M))
  ;; Given user-defined kerroin=1.5, ostoenergia=10000, nettoala=200
  ;; When E-luku calculated
  ;; Then ceil(1.5 * 10000 / 200) = ceil(75) = 75
  (t/is (= (e-luokka-service/e-luku
             2027 (et 200 {:muu [(user-defined-energiamuoto 1.5 10000)]}))
           75M)))

;; 1.7 - Regression: 2018 coefficients unchanged
(t/deftest energiamuotokerroin-2018-unchanged
  (let [kertoimet (get e-luokka-service/energiamuotokerroin 2018)]
    (t/is (= (:sahko kertoimet) 1.2M))
    (t/is (= (:kaukolampo kertoimet) 0.5M))
    (t/is (= (:kaukojaahdytys kertoimet) 0.28M))
    (t/is (= (:fossiilinen-polttoaine kertoimet) 1M))
    (t/is (= (:uusiutuva-polttoaine kertoimet) 0.5M))))

;; 1.8 - Regression: 2013 coefficients unchanged
(t/deftest energiamuotokerroin-2013-unchanged
  (let [kertoimet (get e-luokka-service/energiamuotokerroin 2013)]
    (t/is (= (:sahko kertoimet) 1.7M))
    (t/is (= (:kaukolampo kertoimet) 0.7M))
    (t/is (= (:kaukojaahdytys kertoimet) 0.4M))
    (t/is (= (:fossiilinen-polttoaine kertoimet) 1M))
    (t/is (= (:uusiutuva-polttoaine kertoimet) 0.5M))))

;; === AE-2777: ET2027 laatijan asettama kaukolämmön energiamuotokerroin ===

(defn et-with-kaukolampo-kerroin [nettoala energiamuodot laatimisvaihe kerroin]
  (-> (et nettoala energiamuodot)
      (assoc-in [:perustiedot :laatimisvaihe] laatimisvaihe)
      (assoc-in [:tulokset :kaytettavat-energiamuodot-kertoimet :kaukolampo] kerroin)))

(t/deftest energiamuotokertoimet-2027-olemassaoleva-rakennus
  ;; Given olemassa oleva rakennus with kerroin 0.2, Then kaukolampo kerroin is 0.2
  (t/is (= (:kaukolampo (e-luokka-service/energiamuotokertoimet
                          2027 (et-with-kaukolampo-kerroin 1 {} 2 0.2M)))
           0.2M))
  ;; Other kertoimet stay constant
  (t/is (= (dissoc (e-luokka-service/energiamuotokertoimet
                     2027 (et-with-kaukolampo-kerroin 1 {} 2 0.2M))
                   :kaukolampo)
           (dissoc (get e-luokka-service/energiamuotokerroin 2027) :kaukolampo)))
  ;; Given olemassa oleva rakennus without kerroin, Then default kerroin is used
  (t/is (= (e-luokka-service/energiamuotokertoimet
             2027 (et-with-kaukolampo-kerroin 1 {} 2 nil))
           (get e-luokka-service/energiamuotokerroin 2027))))

(t/deftest energiamuotokertoimet-2027-other-laatimisvaihe
  ;; Given another laatimisvaihe with kerroin, Then default kerroin is used
  (doseq [laatimisvaihe [0 1 3 4 nil]]
    (t/is (= (e-luokka-service/energiamuotokertoimet
               2027 (et-with-kaukolampo-kerroin 1 {} laatimisvaihe 0.2M))
             (get e-luokka-service/energiamuotokerroin 2027)))))

(t/deftest energiamuotokertoimet-2018-ignores-kerroin
  (t/is (= (e-luokka-service/energiamuotokertoimet
             2018 (et-with-kaukolampo-kerroin 1 {} 2 0.2M))
           (get e-luokka-service/energiamuotokerroin 2018))))

(t/deftest e-luku-2027-kaukolampo-kerroin
  ;; Given nettoala=100, kaukolampo=10000 and kerroin=0.2 for olemassa oleva rakennus
  ;; Then E-luku = ceil(10000 * 0.2 / 100) = 20
  (t/is (= (e-luokka-service/e-luku
             2027 (et-with-kaukolampo-kerroin 100 {:kaukolampo 10000} 2 0.2M))
           20M))
  ;; Given the same for rakennuslupa, Then E-luku = ceil(10000 * 0.38 / 100) = 38
  (t/is (= (e-luokka-service/e-luku
             2027 (et-with-kaukolampo-kerroin 100 {:kaukolampo 10000} 0 0.2M))
           38M)))

(t/deftest e-luku-from-ppp-vaihe-kaukolampo-kerroin
  ;; Given olemassa oleva rakennus with kerroin 0.2 and vaihe kaukolampo=10000, nettoala=100
  ;; Then E-luku = ceil(10000 * 0.2 / 100) = 20
  (t/is (= (e-luokka-service/e-luku-from-ppp-vaihe
             2027
             (et-with-kaukolampo-kerroin 100 {} 2 0.2M)
             {:tulokset {:ostoenergian-tarve-kaukolampo 10000}})
           20M)))
