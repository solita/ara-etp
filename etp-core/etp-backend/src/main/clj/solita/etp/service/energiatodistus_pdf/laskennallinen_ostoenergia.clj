(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia
  (:require
    [solita.common.formats :as formats]
    [solita.etp.service.localization :as loc]
    [solita.etp.service.energiatodistus-pdf.ilmastoselvitys :as ilmastoselvitys]))

(defn- fmt
  "Format number with specified decimal places. Returns empty string for nil."
  [value decimals] (or (formats/format-number value decimals false) ""))

(defn- table-ostoenergia [kieli items]
  (let [l (kieli loc/et-pdf-localization)]
    [:div {:class "etusivu-ostoenergia"}
     [:table
      [:thead
       [:tr
        [:th.empty]
        [:th.empty]
        [:th.oe-otsikko (l :kaukolampo)]
        [:th.oe-otsikko (l :sahko)]
        [:th.oe-otsikko (l :uusiutuva-polttoaine)]
        [:th.oe-otsikko (l :fossiilinen-polttoaine)]
        [:th.oe-otsikko (l :kaukojaahdytys)]]]
      [:tbody
       (for [{:keys [dt dd]} items]
         (into
           [:tr [:td dt]]
           (for [row dd]
             [:td (or row "")])))]]]))

(defn- description-list [key-vals]
  (into [:dl]
        (mapv #(vec [:div
                     [:dt (str (:dt %) ":")]
                     [:dd (:dd %)]]) key-vals)))

(defn- laskennallinen-helper [value nettoala]
  (if nettoala
    (^[double] Math/round (/ (double (or value 0)) (double nettoala)))
    0))

(defn- painotettu-helper [value]
  (^[double] Math/round (or value 0)))

(defn ostoenergia [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        nettoala (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])

        laskennallinen-kaukolampo (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukolampo]) (laskennallinen-helper nettoala))
        laskennallinen-sahko (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :sahko]) (laskennallinen-helper nettoala))
        laskennallinen-uusiutuva-polttoaine (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine]) (laskennallinen-helper nettoala))
        laskennallinen-fossiilinen-polttoaine (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine]) (laskennallinen-helper nettoala))
        laskennallinen-kaukojaahdytys (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys]) (laskennallinen-helper nettoala))

        painotettu-kaukolampo (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala-kertoimella]) (painotettu-helper))
        painotettu-sahko (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :sahko-nettoala-kertoimella]) (painotettu-helper))
        painotettu-uusutuva (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala-kertoimella]) (painotettu-helper))
        painotettu-fossiilinen (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala-kertoimella]) (painotettu-helper))
        painotettu-kaukojaahdytys (-> energiatodistus (get-in [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala-kertoimella]) (painotettu-helper))]

    (table-ostoenergia kieli
     [{:dt (l :laskennallinen-ostoenergia)
       :dd [(l :kwh-m2-vuosi)
            laskennallinen-kaukolampo
            laskennallinen-sahko
            laskennallinen-uusiutuva-polttoaine
            laskennallinen-fossiilinen-polttoaine
            laskennallinen-kaukojaahdytys]}
      {:dt (l :energimuodon-kerroin)
       :dd [(str "")
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :sahko-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin])
            (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin])]}
      {:dt (l :energiakulutus)
       :dd [(l :kwhE-m2-vuosi)
            painotettu-kaukolampo
            painotettu-sahko
            painotettu-uusutuva
            painotettu-fossiilinen
            painotettu-kaukojaahdytys]}])))

;; These coefficients are defined independently in the spec:
;; "tuotettu sähkö kertoimella 0,9 ja tuotettu aurinkolämpö kertoimella 0,38"
;; They happen to coincide with energiamuotokerroin 2026 values but are separate concepts.
(def ^:private uusiutuva-kerroin
  {:aurinkosahko 0.90
   :tuulisahko   0.90
   :aurinkolampo 0.38})

(defn- painotettu-uusiutuva-summa
  "Sum of (value × coefficient) for aurinkosähkö, tuulisähkö, aurinkolämpö."
  [energy-map]
  (reduce-kv (fn [acc k coeff]
               (+ acc (* coeff (double (or (get energy-map k) 0)))))
             0.0
             uusiutuva-kerroin))

(defn uusiutuvan-energian-osuus
  "Calculate the percentage of on-site renewable energy production relative to energy consumption.
   Returns a formatted string like '24 %' or nil if the calculation cannot be performed.

   Formula: (Σ(E_tuotto × k) / A_netto) / (E_luku + Σ(E_hyödynnetty × k) / A_netto) × 100"
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
            (str (Math/round (* (/ numerator denominator) 100.0)) " %")))))))


(defn ostoenergia-tiedot [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/et-pdf-localization)
        uusiutuvan-osuus (uusiutuvan-energian-osuus (:versio energiatodistus) energiatodistus)]

    [:div {:class "etusivu-ostoenergia"}
     (description-list
       [{:dt (l :energiakaytosta-syntyvat-kasvihuonepaastot)
         :dd (str (-> energiatodistus :tulokset :kasvihuonepaastot-nettoala (fmt 2)) " " (l :kgCO2ekv-m2/vuosi))}
        {:dt (l :uusiutuva-energian-osuus)
         :dd (or uusiutuvan-osuus "-")}
        {:dt (l :kasvihuonepaastot)
         :dd (ilmastoselvitys/gwp-value-for-etusivu energiatodistus)}])]))
