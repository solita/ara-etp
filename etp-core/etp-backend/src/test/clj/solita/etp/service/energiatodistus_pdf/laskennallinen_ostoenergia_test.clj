(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as laskennallinen-ostoenergia]))

(defn- flatten-hiccup-strings
  "Recursively extract all string values from a hiccup data structure."
  [hiccup]
  (->> (tree-seq coll? seq hiccup)
       (filter string?)))

(defn- hiccup-contains-string?
  "Check if any string in the hiccup tree contains the given substring."
  [hiccup substring]
  (some #(.contains ^String % substring) (flatten-hiccup-strings hiccup)))

(t/deftest ostoenergia-tiedot-shows-dash-when-nil-test
  ;; Given: a 2026 energiatodistus where the calculation cannot be performed
  ;;        (e.g. E-luku = 0 and no utilized renewable → denominator = 0)
  (let [energiatodistus {:versio 2026
                         :lahtotiedot {:lammitetty-nettoala 200}
                         :tulokset {:e-luku 0
                                    :kaytettavat-energiamuodot {}
                                    :uusiutuvat-omavaraisenergiat
                                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                                     :muulampo 0 :muusahko 0 :lampopumppu 0}
                                    :uusiutuvat-omavaraisenergiat-kokonaistuotanto
                                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0
                                     :muulampo 0 :muusahko 0 :lampopumppu 0}}
                         :ilmastoselvitys nil}
        ;; When: ostoenergia-tiedot generates the hiccup structure
        result (laskennallinen-ostoenergia/ostoenergia-tiedot {:energiatodistus energiatodistus
                                        :kieli :fi})]
    ;; Then: a dash "-" should be displayed for the uusiutuva-energian-osuus field
    (t/is (hiccup-contains-string? result "-")
          "ostoenergia-tiedot should contain a dash when calculation returns nil")))

;; Helper to build a minimal energiatodistus data map for the calculation.
(defn- make-et
  "Build a minimal energiatodistus map for uusiutuvan-energian-osuus testing.
   nettoala:        lammitetty-nettoala value
   e-luku:          pre-computed E-luku value (or nil)
   ostoenergia-map: map of ostoenergia values for kaytettavat-energiamuodot (e.g. {:sahko 50000})
   omavarais-map:   map of uusiutuvat-omavaraisenergiat values (hyödynnetty osuus)
   kokonaistuotanto-map: map of kokonaistuotanto values (tuotto)"
  [nettoala e-luku ostoenergia-map omavarais-map kokonaistuotanto-map]
  {:lahtotiedot {:lammitetty-nettoala nettoala}
   :tulokset    {:e-luku                              e-luku
                 :kaytettavat-energiamuodot            ostoenergia-map
                 :uusiutuvat-omavaraisenergiat         omavarais-map
                 :uusiutuvat-omavaraisenergiat-kokonaistuotanto kokonaistuotanto-map}})

;; Test 1: Basic calculation — solar electricity only
(t/deftest uusiutuvan-energian-osuus-aurinkosahko-test
  (let [et (make-et 200 100
                    {:fossiilinen-polttoaine 20000}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 10000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= "33 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 2: Basic calculation — solar heat only
(t/deftest uusiutuvan-energian-osuus-aurinkolampo-test
  (let [et (make-et 100 80
                    {:fossiilinen-polttoaine 8000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 4000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 5000})]
    (t/is (= "20 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 3: Combined energy types — all three
(t/deftest uusiutuvan-energian-osuus-combined-test
  (let [et (make-et 250 90
                    {:fossiilinen-polttoaine 22500}
                    {:aurinkosahko 5000 :tuulisahko 2500 :aurinkolampo 1500}
                    {:aurinkosahko 6000 :tuulisahko 3000 :aurinkolampo 2000})]
    (t/is (= "30 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 4: Excluded energy types (lampopumppu, muulampo, muusahko) are not included
(t/deftest uusiutuvan-energian-osuus-excluded-types-test
  (let [et (make-et 200 100
                    {:fossiilinen-polttoaine 20000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                     :lampopumppu 40000 :muulampo 25000 :muusahko 15000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                     :lampopumppu 50000 :muulampo 30000 :muusahko 20000})]
    (t/is (= "0 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 5: All renewable production values are zero
(t/deftest uusiutuvan-energian-osuus-all-zero-test
  (let [et (make-et 150 120
                    {:fossiilinen-polttoaine 18000}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= "0 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 6: Edge case — nettoala is zero
(t/deftest uusiutuvan-energian-osuus-zero-nettoala-test
  (let [et (make-et 0 nil
                    {:fossiilinen-polttoaine 10000}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 7: Edge case — nettoala is nil
(t/deftest uusiutuvan-energian-osuus-nil-nettoala-test
  (let [et (make-et nil nil
                    {:fossiilinen-polttoaine 10000}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 8: Edge case — E-luku is nil (nil energiamuodot)
(t/deftest uusiutuvan-energian-osuus-nil-eluku-test
  (let [et (make-et 200 nil
                    nil
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 9: Edge case — denominator is zero (E-luku = 0 AND no utilized renewable)
(t/deftest uusiutuvan-energian-osuus-zero-denominator-test
  (let [et (make-et 200 0
                    {}
                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 10: Nil renewable energy values treated as zero
(t/deftest uusiutuvan-energian-osuus-nil-renewables-test
  (let [et (make-et 100 80
                    {:fossiilinen-polttoaine 8000}
                    {:aurinkosahko nil :tuulisahko nil :aurinkolampo 4000}
                    {:aurinkosahko nil :tuulisahko nil :aurinkolampo 5000})]
    (t/is (= "20 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 11: Rounding — verify whole percentage rounding
(t/deftest uusiutuvan-energian-osuus-rounding-test
  (let [et (make-et 100 80
                    {:fossiilinen-polttoaine 8000}
                    {:aurinkosahko 1000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 3333 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (= "34 %" (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2026 et)))))

;; Test 12: Versio 2018/2013 — returns nil (2026-only feature)
(t/deftest uusiutuvan-energian-osuus-non-2026-versio-test
  (let [et (make-et 200 100
                    {:fossiilinen-polttoaine 20000}
                    {:aurinkosahko 8000 :tuulisahko 0 :aurinkolampo 0}
                    {:aurinkosahko 10000 :tuulisahko 0 :aurinkolampo 0})]
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2018 et)))
    (t/is (nil? (laskennallinen-ostoenergia/uusiutuvan-energian-osuus 2013 et)))))
