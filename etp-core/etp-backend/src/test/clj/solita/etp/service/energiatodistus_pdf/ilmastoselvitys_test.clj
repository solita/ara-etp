(ns solita.etp.service.energiatodistus-pdf.ilmastoselvitys-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus-pdf.ilmastoselvitys :as is])
  (:import [java.time LocalDate]))

(def test-date (LocalDate/of 2026 1 1))

;; Test has-ilmastoselvitys?
(t/deftest has-ilmastoselvitys-true
  (t/is (true? (is/has-ilmastoselvitys?
                 {:ilmastoselvitys {:laatimisajankohta test-date}}))))

(t/deftest has-ilmastoselvitys-false-nil
  (t/is (false? (is/has-ilmastoselvitys?
                  {:ilmastoselvitys {:laatimisajankohta nil}}))))

(t/deftest has-ilmastoselvitys-false-missing
  (t/is (false? (is/has-ilmastoselvitys? {}))))

;; Test gwp-value-for-etusivu
;; The function should return "-" when no ilmastoselvitys,
;; and a formatted GWP sum when present.
(t/deftest gwp-value-for-etusivu-without-ilmastoselvitys
  (t/is (= "-" (is/gwp-value-for-etusivu {}))))

(t/deftest gwp-value-for-etusivu-nil-laatimisajankohta
  (t/is (= "-"
           (is/gwp-value-for-etusivu
             {:ilmastoselvitys {:laatimisajankohta nil}}))))

(t/deftest gwp-value-for-etusivu-with-data
  ;; Provide data in a form that works both before and after the refactor:
  ;; - Original code computes sum from individual fields
  ;; - Refactored code reads :yhteensa
  ;; So we provide both the individual fields AND :yhteensa with consistent values
  (t/is (= "12,5"
           (is/gwp-value-for-etusivu
             {:ilmastoselvitys
              {:laatimisajankohta test-date
               :hiilijalanjalki
               {:rakennus {:rakennustuotteiden-valmistus 1.5
                           :kuljetukset-tyomaavaihe 2.3
                           :rakennustuotteiden-vaihdot 3.7
                           :energiankaytto 4.1
                           :purkuvaihe 0.9
                           :yhteensa 12.5}}}}))))

(t/deftest gwp-value-for-etusivu-with-nils-in-fields
  (t/is (= "6,0"
           (is/gwp-value-for-etusivu
             {:ilmastoselvitys
              {:laatimisajankohta test-date
               :hiilijalanjalki
               {:rakennus {:rakennustuotteiden-valmistus 1
                           :kuljetukset-tyomaavaihe nil
                           :rakennustuotteiden-vaihdot 2
                           :energiankaytto nil
                           :purkuvaihe 3
                           :yhteensa 6}}}}))))

(t/deftest gwp-value-for-etusivu-all-zeros
  (t/is (= "0,0"
           (is/gwp-value-for-etusivu
             {:ilmastoselvitys
              {:laatimisajankohta test-date
               :hiilijalanjalki
               {:rakennus {:rakennustuotteiden-valmistus 0
                           :kuljetukset-tyomaavaihe 0
                           :rakennustuotteiden-vaihdot 0
                           :energiankaytto 0
                           :purkuvaihe 0
                           :yhteensa 0}}}}))))
