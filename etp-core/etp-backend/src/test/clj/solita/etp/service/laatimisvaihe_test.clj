(ns solita.etp.service.laatimisvaihe-test
  (:require [clojure.test :as t]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]))

(defn et-2018-in-vaihe [vaihe]
  {:versio 2018 :perustiedot {:laatimisvaihe vaihe}})

(defn et-2013 [uudisrakennus?]
  {:versio 2013 :perustiedot {:uudisrakennus uudisrakennus?}})

;; --- Regression tests (existing functionality) ---

(t/deftest olemassa-oleva-rakennus
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 2)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 1)) false))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 0)) false))

  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 false)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 true)) false)))

;; --- Regression: vaihe-key returns correct key for original IDs ---

(t/deftest vaihe-key-regression
  ;; given vaihe-id 0, when calling vaihe-key, then :rakennuslupa is returned
  (t/is (= :rakennuslupa (laatimisvaihe/vaihe-key 0)))
  ;; given vaihe-id 1, when calling vaihe-key, then :kayttoonotto is returned
  (t/is (= :kayttoonotto (laatimisvaihe/vaihe-key 1)))
  ;; given vaihe-id 2, when calling vaihe-key, then :olemassaolevarakennus is returned
  (t/is (= :olemassaolevarakennus (laatimisvaihe/vaihe-key 2))))

;; --- Regression: olemassaoleva-rakennus? returns false for new IDs ---

(t/deftest olemassa-oleva-rakennus-new-ids-regression
  ;; given a 2018 energiatodistus with laatimisvaihe 3,
  ;; when checking olemassaoleva-rakennus?, then false is returned
  (t/is (= false (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 3))))
  ;; given a 2018 energiatodistus with laatimisvaihe 4,
  ;; when checking olemassaoleva-rakennus?, then false is returned
  (t/is (= false (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 4)))))

;; --- NEW: vaihe-key returns correct key for new IDs 3 and 4 ---

(t/deftest vaihe-key-new-ids
  ;; given vaihe-id 3, when calling vaihe-key,
  ;; then :rakennuslupa-perusparannus is returned
  (t/is (= :rakennuslupa-perusparannus (laatimisvaihe/vaihe-key 3)))
  ;; given vaihe-id 4, when calling vaihe-key,
  ;; then :kayttoonotto-perusparannus is returned
  (t/is (= :kayttoonotto-perusparannus (laatimisvaihe/vaihe-key 4))))
