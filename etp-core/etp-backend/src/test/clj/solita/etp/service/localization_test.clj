(ns solita.etp.service.localization-test
  (:require [clojure.test :as t]
            [solita.etp.service.localization :as localization]))

(t/deftest with-subscripts-test
  (t/is (= [:span "Täyttää A0" [:sub "korjattu"] " vaatimukset"]
           (localization/with-subscripts
             "Täyttää A0_korjattu vaatimukset")))
  (t/is (= [:span "A0" [:sub "korjattu"] ", A0" [:sub "MPR"] "."]
           (localization/with-subscripts "A0_korjattu, A0_MPR.")))
  (t/is (= "Ei alaindeksiä"
           (localization/with-subscripts "Ei alaindeksiä"))))
