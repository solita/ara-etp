(ns solita.etp.service.e-luokka
  (:require [clojure.core.match :as match]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.common.logic :as logic])
  (:import (java.math RoundingMode)))

(def default-luokka "G")

(defn raja-asteikko-without-kertoimet [raja-asteikko-with-kertoimet nettoala]
  (mapv (fn [[limit kerroin e-luokka]]
          [(->> nettoala (* kerroin) (- limit) int) e-luokka])
        raja-asteikko-with-kertoimet))

(defn pienet-asuinrakennukset-120-2013 [_]
  {:raja-asteikko [[94 "A"] [164 "B"] [204 "C"] [284 "D"] [414 "E"] [484 "F"]]})

(defn pienet-asuinrakennukset-120-150-2013 [nettoala]
  {:raja-asteikko (raja-asteikko-without-kertoimet
                   [[150 0.47 "A"] [320 1.3 "B"] [372 1.4 "C"] [452 1.4 "D"]
                    [582 1.4 "E"] [652 1.4 "F"]]
                   nettoala)})

(defn pienet-asuinrakennukset-150-600-2013-2018 [nettoala]
  {:raja-asteikko (raja-asteikko-without-kertoimet
                   [[83 0.02 "A"] [131 0.04 "B"] [173 0.07 "C"] [253 0.07 "D"]
                    [383 0.07 "E"] [453 0.07 "F"]]
                   nettoala)
   :raja-uusi-2018 (Math/round (- 116 (* 0.04 nettoala)))})

(defn pienet-asuinrakennukset-600-2013-2018 [_]
  {:raja-asteikko [[70 "A"] [106 "B"] [130 "C"] [210 "D"] [340 "E"] [410 "F"]]
   :raja-uusi-2018 92})

(defn pienet-asuinrakennukset-50-150-2018 [nettoala]
  {:raja-asteikko (raja-asteikko-without-kertoimet
                   [[110 0.2 "A"] [215 0.6 "B"] [252 0.6 "C"] [332 0.6 "D"]
                    [462 0.6 "E"] [532 0.6 "F"]]
                   nettoala)
   :raja-uusi-2018 (Math/round (- 200 (* 0.6 nettoala)))})

(defn rivitalot-2013-2018 [_]
  {:raja-asteikko [[80 "A"] [110 "B"] [150 "C"] [210 "D"] [340 "E"] [410 "F"]]
   :raja-uusi-2018 105})

(defn asuinkerrostalot-2013-2018 [_]
  {:raja-asteikko [[75 "A"] [100 "B"] [130 "C"] [160 "D"] [190 "E"] [240 "F"]]
   :raja-uusi-2018 90})

(defn toimistorakennukset-2013-2018 [_]
  {:raja-asteikko [[80 "A"] [120 "B"] [170 "C"] [200 "D"] [240 "E"] [300 "F"]]
   :raja-uusi-2018 100})

(defn liikerakennukset-2013-2018 [_]
  {:raja-asteikko [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"] [390 "F"]]
   :raja-uusi-2018 135})

(defn majoitusliikerakennukset-2013-2018 [_]
  {:raja-asteikko [[90 "A"] [170 "B"] [240 "C"] [280 "D"] [340 "E"] [450 "F"]]
   :raja-uusi-2018 160})

(defn opetusrakennukset-2013-2018 [_]
  {:raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [230 "D"] [300 "E"] [360 "F"]]
   :raja-uusi-2018 100})

(defn liikuntahallit-2013-2018 [_]
  {:raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"] [280 "F"]]
   :raja-uusi-2018 100})

(defn sairaalat-2013-2018 [_]
  {:raja-asteikko [[150 "A"] [350 "B"] [450 "C"] [550 "D"] [650 "E"] [800 "F"]]
   :raja-uusi-2018 320})

(defn varastorakennukset-ja-moottoriajoneuvosuojat-2013 [_]
  {:raja-asteikko [[75 "A"] [115 "B"] [155 "C"] [175 "D"] [225 "E"] [265 "F"]]})

(defn muut-2018 [_]
  {:raja-asteikko [[90 "A"] [130 "B"] [170 "C"] [190 "D"] [240 "E"] [280 "F"]]})

(defn raja-asteikko-f [versio kayttotarkoitus-id alakayttotarkoitus-id nettoala]
  ((match/match [versio kayttotarkoitus-id alakayttotarkoitus-id nettoala]
                [2013 1 _ (_ :guard #(< % 120))] pienet-asuinrakennukset-120-2013
                [2013 1 _ (_ :guard #(<= % 150))] pienet-asuinrakennukset-120-150-2013
                [2013 1 _ (_ :guard #(<= % 600))] pienet-asuinrakennukset-150-600-2013-2018
                [2013 1 _ (_ :guard #(> % 600))] pienet-asuinrakennukset-600-2013-2018

                ;; For 2018, rivitalot is part of käyttötarkoitusluokka 1 for some reason
                ;; and for 2013 it is a separate käyttötarkoitusluokka
                [2013 2 _ _] rivitalot-2013-2018
                [2018 1 "RT" _] rivitalot-2013-2018
                [2018 1 "AK2" _] rivitalot-2013-2018

                ;; Even though the table is for building between 50-150 squares,
                ;; this table is used for even smaller buildings
                [2018 1 _ (_ :guard #(<= % 150))] pienet-asuinrakennukset-50-150-2018
                [2018 1 _ (_ :guard #(<= % 600))] pienet-asuinrakennukset-150-600-2013-2018
                [2018 1 _ (_ :guard #(> % 600))] pienet-asuinrakennukset-600-2013-2018

                ;; Luckily the rest of the tables are pretty much the same
                ;; for 2013 and 2018

                [2013 3 _ _] asuinkerrostalot-2013-2018
                [2018 2 _ _] asuinkerrostalot-2013-2018

                [2013 4 _ _] toimistorakennukset-2013-2018
                [2018 3 _ _] toimistorakennukset-2013-2018

                [2013 5 _ _] liikerakennukset-2013-2018
                [2018 4 _ _] liikerakennukset-2013-2018

                [2013 6 _ _] majoitusliikerakennukset-2013-2018
                [2018 5 _ _] majoitusliikerakennukset-2013-2018

                [2013 7 _ _] opetusrakennukset-2013-2018
                [2018 6 _ _] opetusrakennukset-2013-2018

                [2013 8 _ _] liikuntahallit-2013-2018
                [2018 7 _ _] liikuntahallit-2013-2018

                [2013 9 _ _] sairaalat-2013-2018
                [2018 8 _ _] sairaalat-2013-2018

                [2013 10 _ _] liikuntahallit-2013-2018

                [2013 11 _ _] varastorakennukset-ja-moottoriajoneuvosuojat-2013

                [2018 9 _ _] muut-2018

                :else (constantly nil))
   nettoala))

(defn e-luokka-from-raja-asteikko [raja-asteikko e-luku]
  (or (some (fn [[raja e-luokka]]
              (if (<= e-luku raja)
                e-luokka))
            raja-asteikko)
      default-luokka))

(defn- find-by-id [id collection]
  (->> collection (filter #(= (:id %) id)) first))

(defn e-luokka-rajat [kayttotarkoitukset alakayttotarkoitukset
                      versio alakayttotarkoitus-id nettoala]
  (logic/if-let*
    [alakayttotarkoitus (find-by-id alakayttotarkoitus-id alakayttotarkoitukset)
     kayttotarkoitus (find-by-id (:kayttotarkoitusluokka-id alakayttotarkoitus)
                                 kayttotarkoitukset)]

    (assoc (raja-asteikko-f versio
                            (:id kayttotarkoitus)
                            alakayttotarkoitus-id
                            nettoala)
      :kayttotarkoitus kayttotarkoitus)))

(defn e-luokka [kayttotarkoitukset alakayttotarkoitukset
                versio alakayttotarkoitus-id nettoala e-luku]
  (logic/if-let*
    [{:keys [raja-asteikko] :as rajat}
     (e-luokka-rajat kayttotarkoitukset alakayttotarkoitukset
                     versio alakayttotarkoitus-id nettoala)]

    (assoc rajat
      :e-luokka (e-luokka-from-raja-asteikko raja-asteikko e-luku))))

(defn find-e-luokka [db versio alakayttotarkoitus-id nettoala e-luku]
  (e-luokka (kayttotarkoitus-service/find-kayttotarkoitukset db versio)
            (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
            versio alakayttotarkoitus-id nettoala e-luku))

(def energiamuotokerroin
  {2018 {:fossiilinen-polttoaine 1M
         :sahko 1.2M
         :kaukojaahdytys 0.28M
         :kaukolampo 0.5M
         :uusiutuva-polttoaine 0.5M}
   2013 {:fossiilinen-polttoaine 1M
         :sahko 1.7M
         :kaukojaahdytys 0.4M
         :kaukolampo 0.7M
         :uusiutuva-polttoaine 0.5M}})

(defn painotettu-ostoenergiankulutus [versio energiatodistus]
  (logic/if-let*
    [energiamuodot (-> energiatodistus :tulokset :kaytettavat-energiamuodot)
     fixed-energiamuodot (dissoc energiamuodot :muu)
     muotokerroin (get energiamuotokerroin versio)]
    (with-precision 20
      (reduce + 0M (map #(* (bigdec (or %1 1M)) (bigdec (or %2 0M)))
                        (concat
                          (map muotokerroin (keys fixed-energiamuodot))
                          (map :muotokerroin (:muu energiamuodot)))
                        (concat
                          (vals fixed-energiamuodot)
                          (map :ostoenergia (:muu energiamuodot))))))))

(defn e-luku
  "E-luvun määritelmä:
  https://www.finlex.fi/fi/laki/alkup/2017/20171010#Pidp446079392"
  [versio energiatodistus]
  (logic/if-let*
    [^BigDecimal kulutus (painotettu-ostoenergiankulutus versio energiatodistus)
     ^BigDecimal nettoala (some-> energiatodistus :lahtotiedot :lammitetty-nettoala bigdec)]
    (when-not (zero? nettoala) (.divide kulutus nettoala 0 RoundingMode/CEILING))))
