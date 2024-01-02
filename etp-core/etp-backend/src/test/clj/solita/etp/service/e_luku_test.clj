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
