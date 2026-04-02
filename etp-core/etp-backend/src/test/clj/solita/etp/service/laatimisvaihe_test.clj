(ns solita.etp.service.laatimisvaihe-test
  (:require [clojure.test :as t]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]))

(defn et-2018-in-vaihe [vaihe]
  {:versio 2018 :perustiedot {:laatimisvaihe vaihe}})

(defn et-2013 [uudisrakennus?]
  {:versio 2013 :perustiedot {:uudisrakennus uudisrakennus?}})

(t/deftest olemassa-oleva-rakennus
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 2)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 1)) false))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2018-in-vaihe 0)) false))

  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 false)) true))
  (t/is (= (laatimisvaihe/olemassaoleva-rakennus? (et-2013 true)) false)))
