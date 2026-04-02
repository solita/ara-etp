(ns solita.etp.service.complete-energiatodistus-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [solita.common.formats :as formats]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.complete-energiatodistus :as service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.e-luokka :as e-luokka-service]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistus-adds (concat (energiatodistus-test-data/generate-adds
                                      49
                                      2013
                                      true)
                                     (energiatodistus-test-data/generate-adds
                                      49
                                      2018
                                      true)
                                     (energiatodistus-test-data/generate-adds-with-zeros
                                      1
                                      2013)
                                     (energiatodistus-test-data/generate-adds-with-zeros
                                      1
                                      2018))
        energiatodistus-ids (energiatodistus-test-data/insert! energiatodistus-adds
                                                               laatija-id)]
    {:laatijat laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest safe-div-test
  (t/is (= 10 (service/safe-div 20 2)))
  (t/is (nil? (service/safe-div 20 nil)))
  (t/is (nil? (service/safe-div nil 10)))
  (t/is (nil? (service/safe-div 20 0))))

(defn assert-complete-energiatoditus [complete-energiatodistus]
  (let [{:keys [kaukolampo]
         :as kaytettavat-energiamuodot} (-> complete-energiatodistus
                                            :tulokset
                                            :kaytettavat-energiamuodot)
        ilmanvaihto (-> complete-energiatodistus :lahtotiedot :ilmanvaihto)
        paaiv (:paaiv ilmanvaihto)
        erillispoistot (:erillispoistot ilmanvaihto)]
    (and
     (or (nil? kaukolampo)
      (= (:kaukolampo-kertoimella kaytettavat-energiamuodot)
         (* (if (= (:versio complete-energiatodistus) 2013) 0.7M 0.5M)
            kaukolampo)))
     (= (:tulo-poisto paaiv)
        (str (formats/format-number (:tulo paaiv) 3 false)
             " / "
             (formats/format-number (:poisto paaiv) 3 false)))
     (= (:tulo-poisto erillispoistot)
        (str (formats/format-number (:tulo erillispoistot) 3 false)
             " / "
             (formats/format-number (:poisto erillispoistot) 3 false)))
     (-> complete-energiatodistus
         :perustiedot
         keys
         set
         (contains? :kayttotarkoitus))
     (-> complete-energiatodistus
         :perustiedot
         keys
         set
         (contains? :paakayttotarkoitus-id)))))

(t/deftest complete-energiatodistus-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        luokittelut (service/luokittelut ts/*db*)]
    (doseq [id (keys energiatodistukset)]
      (t/is (assert-complete-energiatoditus
             (service/complete-energiatodistus
              (energiatodistus-service/find-energiatodistus ts/*db* id)
              luokittelut))))))

(t/deftest complete-energiatodistus-with-null-nettoala-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        luokittelut (service/luokittelut ts/*db*)
        id (-> energiatodistukset keys sort first)]
    (jdbc/execute!
     ts/*db*
     ["UPDATE energiatodistus SET lt$lammitetty_nettoala = NULL where id = ?" id])
    (t/is (assert-complete-energiatoditus
           (service/complete-energiatodistus
            (energiatodistus-service/find-energiatodistus ts/*db* id)
            luokittelut)))))

(t/deftest find-complete-energiatodistus-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (assert-complete-energiatoditus
           (service/find-complete-energiatodistus ts/*db* id)))
    (t/is (assert-complete-energiatoditus
           (service/find-complete-energiatodistus
            ts/*db*
            {:id (-> laatijat keys sort first) :rooli 0}
            id)))
    (t/is (nil? (service/find-complete-energiatodistus ts/*db* -1)))))

(t/deftest find-complete-energiatodistus-no-permissions-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:id -100 :rooli 0} id))
             {:type :forbidden}))
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:rooli 2} id))
             {:type :forbidden}))
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:rooli 3} id))
             {:type :forbidden}))))

;; Helper to build a minimal energiatodistus data map for the calculation.
;; ostoenergia is used to derive E-luku via e-luokka/e-luku for versio 2026.
(defn- make-et
  "Build a minimal energiatodistus map for uusiutuvan-energian-osuus testing.
   ostoenergia-map: map of ostoenergia values for kaytettavat-energiamuodot (e.g. {:sahko 50000})
   omavarais-map:   map of uusiutuvat-omavaraisenergiat values (hyödynnetty osuus)
   kokonaistuotanto-map: map of kokonaistuotanto values (tuotto)
   nettoala:        lammitetty-nettoala value"
  [nettoala ostoenergia-map omavarais-map kokonaistuotanto-map]
  {:lahtotiedot {:lammitetty-nettoala nettoala}
   :tulokset    {:kaytettavat-energiamuodot         ostoenergia-map
                 :uusiutuvat-omavaraisenergiat       omavarais-map
                 :uusiutuvat-omavaraisenergiat-kokonaistuotanto kokonaistuotanto-map}})

;; Test 1: Basic calculation — solar electricity only
(t/deftest uusiutuvan-energian-osuus-aurinkosahko-test
  ;; Given: aurinkosähkö kokonaistuotanto = 10000, hyödynnetty = 8000,
  ;;        nettoala = 200, ostoenergia that gives E-luku = 100
  ;; When: uusiutuvan-energian-osuus is calculated
  ;; Then: numerator = (10000 × 0.90) / 200 = 45
  ;;       denominator = 100 + (8000 × 0.90) / 200 = 100 + 36 = 136
  ;;       result = 45 / 136 × 100 = 33.09 → 33 %
  (let [;; E-luku for 2026: ceil(painotettu / nettoala)
        ;; We need E-luku = 100, nettoala = 200
        ;; So painotettu must be in [19801..20000] for ceil to give 100
        ;; sahko kerroin is 0.90, so sahko = 20000/0.90 = 22222.22...
        ;; Actually simpler: use kaukolampo (kerroin 0.38) — but let's compute precisely
        ;; We want ceil(painotettu/200) = 100, so painotettu ∈ [19801, 20000]
        ;; Use fossiilinen-polttoaine (kerroin 1.0) = 20000
        et (make-et 200
                    {:fossiilinen-polttoaine 20000} ;; E-luku = ceil(20000*1.0/200) = 100
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 10000 :tuulisahko 0 :aurinkolampo 0})]
    ;; Verify E-luku assumption
    (t/is (= 100M (e-luokka-service/e-luku 2026 et))
          "E-luku should be 100 for this test setup")
    (t/is (= "33 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 2: Basic calculation — solar heat only
(t/deftest uusiutuvan-energian-osuus-aurinkolampo-test
  ;; Given: aurinkolämpö kokonaistuotanto = 5000, hyödynnetty = 4000,
  ;;        nettoala = 100, E-luku = 80
  ;; When: calculated
  ;; Then: numerator = (5000 × 0.38) / 100 = 19
  ;;       denominator = 80 + (4000 × 0.38) / 100 = 80 + 15.2 = 95.2
  ;;       result = 19 / 95.2 × 100 = 19.96 → 20 %
  (let [et (make-et 100
                    {:fossiilinen-polttoaine 8000} ;; E-luku = ceil(8000*1.0/100) = 80
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 4000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 5000})]
    (t/is (= 80M (e-luokka-service/e-luku 2026 et))
          "E-luku should be 80 for this test setup")
    (t/is (= "20 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 3: Combined energy types — all three
(t/deftest uusiutuvan-energian-osuus-combined-test
  ;; Given: aurinkosähkö tuotto=6000 hyöd=5000, tuulisähkö tuotto=3000 hyöd=2500,
  ;;        aurinkolämpö tuotto=2000 hyöd=1500, nettoala=250, E-luku=90
  ;; When: calculated
  ;; Then: numerator = ((6000×0.90)+(3000×0.90)+(2000×0.38))/250 = (5400+2700+760)/250 = 35.44
  ;;       denominator = 90 + ((5000×0.90)+(2500×0.90)+(1500×0.38))/250 = 90 + (4500+2250+570)/250 = 90 + 29.28 = 119.28
  ;;       result = 35.44/119.28 × 100 = 29.71 → 30 %
  (let [et (make-et 250
                    {:fossiilinen-polttoaine 22500} ;; E-luku = ceil(22500*1.0/250) = 90
                    {:aurinkosahko 5000 :tuulisahko 2500 :aurinkolampo 1500}
                    {:aurinkosahko 6000 :tuulisahko 3000 :aurinkolampo 2000})]
    (t/is (= 90M (e-luokka-service/e-luku 2026 et))
          "E-luku should be 90 for this test setup")
    (t/is (= "30 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 4: Excluded energy types (lampopumppu, muulampo, muusahko) are not included
(t/deftest uusiutuvan-energian-osuus-excluded-types-test
  ;; Given: only excluded energy types have non-zero values;
  ;;        aurinkosähkö/tuulisähkö/aurinkolämpö are all 0
  ;; When: calculated
  ;; Then: numerator = 0, result = "0 %"
  (let [et (make-et 200
                    {:fossiilinen-polttoaine 20000} ;; E-luku = 100
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                     :lampopumppu 40000 :muulampo 25000 :muusahko 15000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                     :lampopumppu 50000 :muulampo 30000 :muusahko 20000})]
    (t/is (= 100M (e-luokka-service/e-luku 2026 et)))
    (t/is (= "0 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 5: All renewable production values are zero
(t/deftest uusiutuvan-energian-osuus-all-zero-test
  ;; Given: all kokonaistuotanto and omavaraisenergiat = 0, E-luku = 120
  ;; When: calculated
  ;; Then: numerator = 0, denominator = 120, result = "0 %"
  (let [et (make-et 150
                    {:fossiilinen-polttoaine 18000} ;; E-luku = ceil(18000/150) = 120
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= 120M (e-luokka-service/e-luku 2026 et)))
    (t/is (= "0 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 6: Edge case — nettoala is zero
(t/deftest uusiutuvan-energian-osuus-zero-nettoala-test
  ;; Given: nettoala = 0, non-zero renewable values
  ;; When: calculated
  ;; Then: nil (division by zero)
  (let [et (make-et 0
                    {:fossiilinen-polttoaine 10000}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 7: Edge case — nettoala is nil
(t/deftest uusiutuvan-energian-osuus-nil-nettoala-test
  ;; Given: nettoala = nil
  ;; When: calculated
  ;; Then: nil
  (let [et (make-et nil
                    {:fossiilinen-polttoaine 10000}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 8: Edge case — E-luku is nil (nil energiamuodot)
(t/deftest uusiutuvan-energian-osuus-nil-eluku-test
  ;; Given: kaytettavat-energiamuodot = nil, so e-luku returns nil
  ;; When: calculated
  ;; Then: nil
  (let [et (make-et 200
                    nil ;; e-luku returns nil when energiamuodot is nil
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (e-luokka-service/e-luku 2026 et))
          "E-luku should be nil when energiamuodot is nil")
    (t/is (nil? (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 9: Edge case — denominator is zero (E-luku = 0 AND no utilized renewable)
(t/deftest uusiutuvan-energian-osuus-zero-denominator-test
  ;; Given: E-luku = 0 (empty energiamuodot) and all hyödynnetty = 0
  ;; When: calculated
  ;; Then: denominator = 0 + 0 = 0 → nil (division by zero)
  (let [et (make-et 200
                    {} ;; e-luku = 0 (empty map)
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= 0M (e-luokka-service/e-luku 2026 et))
          "E-luku should be 0 with empty energiamuodot")
    (t/is (nil? (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 10: Nil renewable energy values treated as zero
(t/deftest uusiutuvan-energian-osuus-nil-renewables-test
  ;; Given: aurinkosähkö & tuulisähkö are nil, only aurinkolämpö has values
  ;; When: calculated
  ;; Then: nil values treated as 0, same result as test 2
  (let [et (make-et 100
                    {:fossiilinen-polttoaine 8000} ;; E-luku = 80
                    {:aurinkosahko nil :tuulisahko nil :aurinkolampo 4000}
                    {:aurinkosahko nil :tuulisahko nil :aurinkolampo 5000})]
    (t/is (= 80M (e-luokka-service/e-luku 2026 et)))
    (t/is (= "20 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 11: Rounding — verify whole percentage rounding
(t/deftest uusiutuvan-energian-osuus-rounding-test
  ;; Given: values chosen to produce a result requiring rounding
  ;; aurinkosähkö tuotto=3333, hyöd=1000, nettoala=100, E-luku=80
  ;; numerator = (3333 × 0.90) / 100 = 2999.7 / 100 = 29.997
  ;; denominator = 80 + (1000 × 0.90) / 100 = 80 + 9 = 89
  ;; result = 29.997 / 89 × 100 = 33.7044... → 34 %
  (let [et (make-et 100
                    {:fossiilinen-polttoaine 8000} ;; E-luku = 80
                    {:aurinkosahko 1000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 3333 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= 80M (e-luokka-service/e-luku 2026 et)))
    (t/is (= "34 %" (service/uusiutuvan-energian-osuus 2026 et)))))

;; Test 12: Versio 2018/2013 — returns nil (2026-only feature)
(t/deftest uusiutuvan-energian-osuus-non-2026-versio-test
  ;; Given: a valid energiatodistus but versio is 2018
  ;; When: calculated with versio 2018
  ;; Then: nil (feature is 2026-only)
  (let [et (make-et 200
                    {:fossiilinen-polttoaine 20000}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 10000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (service/uusiutuvan-energian-osuus 2018 et)))
    (t/is (nil? (service/uusiutuvan-energian-osuus 2013 et)))))
