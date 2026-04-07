(ns solita.etp.service.laatimisvaihe-test
  (:require [clojure.test :as t]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]
            [solita.etp.test :as etp-test]))

(defn et-2018-in-vaihe [vaihe]
  {:versio 2018 :perustiedot {:laatimisvaihe vaihe}})

(defn et-2026-in-vaihe [vaihe]
  {:versio 2026 :perustiedot {:laatimisvaihe vaihe}})

(defn et-2013 [uudisrakennus?]
  {:versio 2013 :perustiedot {:uudisrakennus uudisrakennus?}})

(t/deftest olemassa-oleva-rakennus
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 2)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 1)) false))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 0)) false))

  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 false)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 true)) false)))

(t/deftest vaihe-key-existing-ids
  (t/testing "given existing vaihe ids 0, 1, 2, when vaihe-key is called, then returns correct keys"
    (t/is (= (laatimisvaihe/vaihe-key 0) :rakennuslupa))
    (t/is (= (laatimisvaihe/vaihe-key 1) :kayttoonotto))
    (t/is (= (laatimisvaihe/vaihe-key 2) :olemassaolevarakennus))))

(t/deftest vaihe-key-new-perusparannus-ids
  (t/testing "given new vaihe id 3, when vaihe-key is called, then returns :rakennuslupa-perusparannus"
    (t/is (= (laatimisvaihe/vaihe-key 3) :rakennuslupa-perusparannus)))

  (t/testing "given new vaihe id 4, when vaihe-key is called, then returns :kayttoonotto-perusparannus"
    (t/is (= (laatimisvaihe/vaihe-key 4) :kayttoonotto-perusparannus))))

(t/deftest validate-laatimisvaihe-valid-2018
  (t/testing "given a 2018 energiatodistus with valid laatimisvaihe ids 0, 1, 2, when validating, then does not throw"
    (doseq [vaihe-id [0 1 2]]
      (t/is (nil? (laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2018-in-vaihe vaihe-id)))))))

(t/deftest validate-laatimisvaihe-invalid-2018-id-3
  (t/testing "given a 2018 energiatodistus with laatimisvaihe 3, when validating, then throws :invalid-laatimisvaihe"
    (let [result (etp-test/catch-ex-data
                   #(laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2018-in-vaihe 3)))]
      (t/is (= (:type result) :invalid-laatimisvaihe)))))

(t/deftest validate-laatimisvaihe-invalid-2018-id-4
  (t/testing "given a 2018 energiatodistus with laatimisvaihe 4, when validating, then throws :invalid-laatimisvaihe"
    (let [result (etp-test/catch-ex-data
                   #(laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2018-in-vaihe 4)))]
      (t/is (= (:type result) :invalid-laatimisvaihe)))))

(t/deftest validate-laatimisvaihe-valid-2026
  (t/testing "given a 2026 energiatodistus with laatimisvaihe ids 0-4, when validating, then does not throw for any"
    (doseq [vaihe-id [0 1 2 3 4]]
      (t/is (nil? (laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2026-in-vaihe vaihe-id)))))))

(t/deftest validate-laatimisvaihe-2013-not-validated
  (t/testing "given a 2013 energiatodistus, when validating, then does not throw (2013 doesn't use laatimisvaihe)"
    (t/is (nil? (laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2013 true))))))

(t/deftest validate-laatimisvaihe-invalid-unknown-id
  (t/testing "given a 2026 energiatodistus with laatimisvaihe 99, when validating, then throws :invalid-laatimisvaihe"
    (let [result (etp-test/catch-ex-data
                   #(laatimisvaihe/validate-laatimisvaihe-for-versio! (et-2026-in-vaihe 99)))]
      (t/is (= (:type result) :invalid-laatimisvaihe)))))

(t/deftest validate-laatimisvaihe-nil-does-not-throw
  (t/testing "given a 2018 energiatodistus with nil laatimisvaihe, when validating, then does not throw (nil handled by validate-required!)"
    (t/is (nil? (laatimisvaihe/validate-laatimisvaihe-for-versio!
                  {:versio 2018 :perustiedot {:laatimisvaihe nil}})))))
