(ns solita.etp.service.uusiutuva-energia)

;; These coefficients are defined independently in the spec:
;; "tuotettu sähkö kertoimella 0,9 ja tuotettu aurinkolämpö kertoimella 0,38"
;; They happen to coincide with energiamuotokerroin 2026 values but are separate concepts.
(def uusiutuva-kerroin
  {:aurinkosahko 0.90
   :tuulisahko   0.90
   :aurinkolampo 0.38})

(defn painotettu-uusiutuva-summa
  "Sum of (value × coefficient) for aurinkosähkö, tuulisähkö, aurinkolämpö."
  [energy-map]
  (reduce-kv (fn [acc k coeff]
               (+ acc (* coeff (double (or (get energy-map k) 0)))))
             0.0
             uusiutuva-kerroin))

(defn uusiutuvan-energian-osuus
  "Calculate the percentage of on-site renewable energy production relative to energy consumption.
   Returns a rounded long (integer percentage) or nil if the calculation cannot be performed.

   Formula: round((Σ(E_tuotto × k) / A_netto) / (E_luku + Σ(E_hyödynnetty × k) / A_netto) × 100)"
  [versio energiatodistus]
  (when (= versio 2026)
    (let [nettoala  (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
          e-luku    (get-in energiatodistus [:tulokset :e-luku])]
      (when (and nettoala (pos? nettoala) (some? e-luku))
        (let [tuotto      (get-in energiatodistus [:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto])
              hyodynnetty (get-in energiatodistus [:tulokset :uusiutuvat-omavaraisenergiat])
              numerator   (/ (painotettu-uusiutuva-summa tuotto) (double nettoala))
              denominator (+ (double e-luku)
                             (/ (painotettu-uusiutuva-summa hyodynnetty) (double nettoala)))]
          (when (pos? denominator)
            (Math/round (* (/ numerator denominator) 100.0))))))))
