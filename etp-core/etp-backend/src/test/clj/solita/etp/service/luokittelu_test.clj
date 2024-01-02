(ns solita.etp.service.luokittelu-test
  (:require [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.service.luokittelu :as service]))

(t/use-fixtures :once ts/fixture)

(t/deftest find-ilmanvaihtotyypit-test
  (let [ilmanvaihtotyypit (service/find-ilmanvaihtotyypit ts/*db*)]
    (t/is (= (-> ilmanvaihtotyypit last :label-fi) "Muu ilmanvaihtojärjestelmä"))
    (t/is (= 9 (count ilmanvaihtotyypit)))))

(t/deftest find-lammitysmuodot-test
  (let [lammitysmuodot (service/find-lammitysmuodot ts/*db*)]
    (t/is (= (-> lammitysmuodot last :label-fi) "Muu lämmitysjärjestelmä"))
    (t/is (= 10 (count lammitysmuodot)))))

(t/deftest find-lammonjaot-test
  (let [lammonjaot (service/find-lammonjaot ts/*db*)]
    (t/is (= (-> lammonjaot last :label-fi) "Muu lämmönjako"))
    (t/is (= 13 (count lammonjaot)))))

