(ns solita.etp.service.complete-perusparannuspassi
  (:require [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]))

(def ^:private co2-kertoimet
  {:kaukolampo         0.170
   :sahko              0.104
   :uusiutuvat-pat     0.0
   :fossiiliset-pat    0.264
   :kaukojaahdytys     0.120})

(def ^:private ppp-key->energiamuotokerroin-key
  {:ostoenergian-tarve-kaukolampo       :kaukolampo
   :ostoenergian-tarve-sahko            :sahko
   :ostoenergian-tarve-uusiutuvat-pat   :uusiutuva-polttoaine
   :ostoenergian-tarve-fossiiliset-pat  :fossiilinen-polttoaine
   :ostoenergian-tarve-kaukojaahdytys   :kaukojaahdytys})

(defn- painotettu-ostoenergia
  "Calculate weighted energy from PPP vaihe tulokset using e-luokka energiamuotokerroin"
  [tulokset]
  (when tulokset
    (let [kertoimet (e-luokka-service/energiamuotokerroin 2026)]
      (with-precision 20
        (reduce + 0M
                (map (fn [[ppp-key kerroin-key]]
                       (* (bigdec (or (get tulokset ppp-key) 0))
                          (bigdec (or (get kertoimet kerroin-key) 0))))
                     ppp-key->energiamuotokerroin-key))))))

(defn- energia-kustannukset
  "Calculate energy bill from PPP vaihe ostoenergian-tarve values.
   Returns cost in € per year."
  [vaihe-tulokset ppp-tulokset]
  (let [hinnat {:kaukolampo      (or (:kaukolampo-hinta ppp-tulokset) 0)
                :sahko           (or (:sahko-hinta ppp-tulokset) 0)
                :uusiutuvat-pat  (or (:uusiutuvat-pat-hinta ppp-tulokset) 0)
                :fossiiliset-pat (or (:fossiiliset-pat-hinta ppp-tulokset) 0)
                :kaukojaahdytys  (or (:kaukojaahdytys-hinta ppp-tulokset) 0)}]
    (/ (+ (* (or (:ostoenergian-tarve-kaukolampo vaihe-tulokset) 0) (:kaukolampo hinnat))
          (* (or (:ostoenergian-tarve-sahko vaihe-tulokset) 0) (:sahko hinnat))
          (* (or (:ostoenergian-tarve-uusiutuvat-pat vaihe-tulokset) 0) (:uusiutuvat-pat hinnat))
          (* (or (:ostoenergian-tarve-fossiiliset-pat vaihe-tulokset) 0) (:fossiiliset-pat hinnat))
          (* (or (:ostoenergian-tarve-kaukojaahdytys vaihe-tulokset) 0) (:kaukojaahdytys hinnat)))
       100.0))) ; Convert from snt to €

(defn- toteutunut-energia-kustannukset
  "Calculate measured energy bill from PPP vaihe toteutunut-ostoenergia values.
   Returns cost in € per year."
  [tulokset ppp-tulokset]
  (when (and tulokset ppp-tulokset)
    (let [hinnat {:kaukolampo        (or (:kaukolampo-hinta ppp-tulokset) 0)
                  :sahko              (or (:sahko-hinta ppp-tulokset) 0)
                  :uusiutuvat-pat     (or (:uusiutuvat-pat-hinta ppp-tulokset) 0)
                  :fossiiliset-pat    (or (:fossiiliset-pat-hinta ppp-tulokset) 0)
                  :kaukojaahdytys     (or (:kaukojaahdytys-hinta ppp-tulokset) 0)}]
      (/ (+ (* (or (:toteutunut-ostoenergia-kaukolampo tulokset) 0) (:kaukolampo hinnat))
            (* (or (:toteutunut-ostoenergia-sahko tulokset) 0) (:sahko hinnat))
            (* (or (:toteutunut-ostoenergia-uusiutuvat-pat tulokset) 0) (:uusiutuvat-pat hinnat))
            (* (or (:toteutunut-ostoenergia-fossiiliset-pat tulokset) 0) (:fossiiliset-pat hinnat))
            (* (or (:toteutunut-ostoenergia-kaukojaahdytys tulokset) 0) (:kaukojaahdytys hinnat)))
         100.0)))) ; Convert from snt to €

(defn- co2-paastot-ppp
  "Calculate CO2 emissions from PPP vaihe ostoenergian-tarve values.
   Returns emissions in tons CO2 per year."
  [tulokset]
  (when tulokset
    (/ (+ (* (or (:ostoenergian-tarve-kaukolampo tulokset) 0) (:kaukolampo co2-kertoimet))
          (* (or (:ostoenergian-tarve-sahko tulokset) 0) (:sahko co2-kertoimet))
          (* (or (:ostoenergian-tarve-uusiutuvat-pat tulokset) 0) (:uusiutuvat-pat co2-kertoimet))
          (* (or (:ostoenergian-tarve-fossiiliset-pat tulokset) 0) (:fossiiliset-pat co2-kertoimet))
          (* (or (:ostoenergian-tarve-kaukojaahdytys tulokset) 0) (:kaukojaahdytys co2-kertoimet)))
       1000.0))) ; Convert kg to tons

(defn- total-ostoenergia-ppp
  "Calculate total ostoenergia from PPP vaihe tulokset"
  [tulokset]
  (when tulokset
    (+ (or (:ostoenergian-tarve-kaukolampo tulokset) 0)
       (or (:ostoenergian-tarve-sahko tulokset) 0)
       (or (:ostoenergian-tarve-uusiutuvat-pat tulokset) 0)
       (or (:ostoenergian-tarve-fossiiliset-pat tulokset) 0)
       (or (:ostoenergian-tarve-kaukojaahdytys tulokset) 0))))

(defn- total-toteutunut-ostoenergia
  "Calculate total toteutunut ostoenergia from PPP vaihe tulokset"
  [tulokset]
  (when tulokset
    (+ (or (:toteutunut-ostoenergia-kaukolampo tulokset) 0)
       (or (:toteutunut-ostoenergia-sahko tulokset) 0)
       (or (:toteutunut-ostoenergia-uusiutuvat-pat tulokset) 0)
       (or (:toteutunut-ostoenergia-fossiiliset-pat tulokset) 0)
       (or (:toteutunut-ostoenergia-kaukojaahdytys tulokset) 0))))

(defn- calculate-year-range
  "Calculate the year range string for a vaihe.
   Takes the current vaihe and the next vaihe (if any).
   Returns a string like '2030–2035' or '2045–2050' for the last vaihe."
  [vaihe next-vaihe]
  (let [alku-pvm (get-in vaihe [:tulokset :vaiheen-alku-pvm])
        seuraava-alku-pvm (when next-vaihe
                            (get-in next-vaihe [:tulokset :vaiheen-alku-pvm]))
        start-year (cond
                     (number? alku-pvm) (int alku-pvm)
                     alku-pvm (.getYear alku-pvm)
                     :else nil)
        next-start-year (cond
                          (number? seuraava-alku-pvm) (int seuraava-alku-pvm)
                          seuraava-alku-pvm (.getYear seuraava-alku-pvm)
                          :else nil)
        end-year (if next-start-year
                   (dec next-start-year)
                   ;; For the last vaihe, use 2050 as the end year
                   (when start-year 2050))]
    (when (and start-year end-year)
      (str start-year "–" end-year))))

(defn- complete-vaihe
  "Enrich a PPP vaihe with calculated fields.
   Adds calculated fields both to :tulokset and at the top level for consistency.
   For invalid vaiheet, adds nil/0 placeholders."
  [vaihe energiatodistus ppp-tulokset {:keys [kayttotarkoitukset alakayttotarkoitukset]}]
  (if-not (:valid vaihe)
    ;; For invalid vaiheet, add nil placeholders to match structure
    (-> vaihe
        (assoc-in [:tulokset :e-luku] nil)
        (assoc-in [:tulokset :e-luokka] nil)
        (assoc-in [:tulokset :ostoenergia] nil)
        (assoc-in [:tulokset :painotettu-ostoenergia] nil)
        (assoc-in [:tulokset :energia-kustannukset] nil)
        (assoc-in [:tulokset :toteutunut-ostoenergia] nil)
        (assoc-in [:tulokset :toteutunut-energia-kustannukset] nil)
        (assoc-in [:tulokset :co2-paastot] nil))
    ;; For valid vaiheet, calculate real values
    (let [versio 2026
          tulokset (:tulokset vaihe)
          e-luku (e-luokka-service/e-luku-from-ppp-vaihe versio energiatodistus vaihe)
          e-luokka (when e-luku
                     (-> (e-luokka-service/e-luokka
                           (get kayttotarkoitukset versio)
                           (get alakayttotarkoitukset versio)
                           versio
                           (get-in energiatodistus [:perustiedot :kayttotarkoitus])
                           (get-in energiatodistus [:lahtotiedot :lammitetty-nettoala])
                           e-luku)
                         :e-luokka))
          ostoenergia (total-ostoenergia-ppp tulokset)
          painotettu (painotettu-ostoenergia tulokset)
          kustannukset (energia-kustannukset tulokset ppp-tulokset)
          toteutunut-ostoenergia (total-toteutunut-ostoenergia tulokset)
          toteutunut-kustannukset (toteutunut-energia-kustannukset tulokset ppp-tulokset)
          co2 (co2-paastot-ppp tulokset)]
      (-> vaihe
          (assoc-in [:tulokset :e-luku] e-luku)
          (assoc-in [:tulokset :e-luokka] e-luokka)
          (assoc-in [:tulokset :ostoenergia] ostoenergia)
          (assoc-in [:tulokset :painotettu-ostoenergia] painotettu)
          (assoc-in [:tulokset :energia-kustannukset] kustannukset)
          (assoc-in [:tulokset :toteutunut-ostoenergia] toteutunut-ostoenergia)
          (assoc-in [:tulokset :toteutunut-energia-kustannukset] toteutunut-kustannukset)
          (assoc-in [:tulokset :co2-paastot] co2)))))

(defn- sum-uusiutuvat-omavaraisenergiat
  "Sum all non-nil values from uusiutuvat-omavaraisenergiat (or whatever map).
   Returns 0 if all values are nil."
  [omavaraisenergiat]
  (->> omavaraisenergiat vals (filter some?) (reduce + 0)))

(defn- energiatodistus->lahtotilanne
  "Transform a complete energiatodistus into a vaihe-shaped lähtötilanne structure.
   Creates a basic vaihe structure and uses complete-vaihe to add calculated fields."
  [energiatodistus ppp-tulokset luokittelut]
  (let [energiamuodot (get-in energiatodistus [:tulokset :kaytettavat-energiamuodot])
        toteutunut-ostoenergiankulutus (:toteutunut-ostoenergiankulutus energiatodistus)
        ;; Create a basic vaihe structure with PPP-style field names
        basic-vaihe {:valid true
                     :tulokset {:ostoenergian-tarve-kaukolampo (or (:kaukolampo energiamuodot) 0)
                                :ostoenergian-tarve-sahko (or (:sahko energiamuodot) 0)
                                :ostoenergian-tarve-uusiutuvat-pat (or (:uusiutuva-polttoaine energiamuodot) 0)
                                :ostoenergian-tarve-fossiiliset-pat (or (:fossiilinen-polttoaine energiamuodot) 0)
                                :ostoenergian-tarve-kaukojaahdytys (or (:kaukojaahdytys energiamuodot) 0)
                                ;; Toteutunut values are nil for lähtötilanne
                                :toteutunut-ostoenergia-kaukolampo (get-in toteutunut-ostoenergiankulutus [:ostettu-energia :kaukojaahdytys-vuosikulutus])
                                :toteutunut-ostoenergia-sahko (get-in toteutunut-ostoenergiankulutus [:ostettu-energia :kokonaissahko-vuosikulutus])
                                :toteutunut-ostoenergia-uusiutuvat-pat nil
                                :toteutunut-ostoenergia-fossiiliset-pat nil
                                :toteutunut-ostoenergia-kaukojaahdytys (get-in toteutunut-ostoenergiankulutus [:ostettu-energia :kaukojaahdytys-vuosikulutus])
                                ;; Date fields are nil for lähtötilanne
                                :vaiheen-alku-pvm nil
                                :vaiheen-loppu-pvm nil
                                ;; Renewable energy fields from energiatodistus
                                :uusiutuvan-energian-kokonaistuotto (sum-uusiutuvat-omavaraisenergiat
                                                                      (-> energiatodistus
                                                                          (get-in [:tulokset :uusiutuvat-omavaraisenergiat])))
                                :uusiutuvan-energian-hyodynnetty-osuus nil}}]
    ;; Use complete-vaihe to add all calculated fields
    (complete-vaihe basic-vaihe energiatodistus ppp-tulokset luokittelut)))

(defn- add-year-ranges
  "Add year-range to each vaihe based on consecutive vaihe start dates.
   Last vaihe gets 2050 as end year."
  [vaiheet]
  (->> (partition 2 1 [nil] vaiheet)
       (mapv (fn [[vaihe next-vaihe]]
               (if-let [year-range (calculate-year-range vaihe next-vaihe)]
                 (assoc vaihe :year-range year-range)
                 vaihe)))))

(defn complete-perusparannuspassi
  "Enrich a perusparannuspassi by augmenting all vaiheet with calculated fields.
   Takes a perusparannuspassi, its associated energiatodistus, and luokittelut map.
   Returns the perusparannuspassi with completed vaiheet and :lahtotilanne.
   Valid vaiheet get real calculated values, invalid vaiheet get nil/0 placeholders."
  [perusparannuspassi energiatodistus luokittelut]
  (let [ppp-tulokset (:tulokset perusparannuspassi)
        lahtotilanne (energiatodistus->lahtotilanne energiatodistus ppp-tulokset luokittelut)]
    (-> perusparannuspassi
        (update :vaiheet
                (fn [vaiheet]
                  (->> vaiheet
                       (mapv #(complete-vaihe % energiatodistus ppp-tulokset luokittelut))
                       add-year-ranges)))
        (assoc :lahtotilanne lahtotilanne))))

(defn luokittelut
  "Fetch all classification data needed for completing PPP vaiheet.
   Returns a map with :kayttotarkoitukset and :alakayttotarkoitukset for versio 2026."
  [db]
  {:kayttotarkoitukset    {2026 (kayttotarkoitus-service/find-kayttotarkoitukset db 2026)}
   :alakayttotarkoitukset {2026 (kayttotarkoitus-service/find-alakayttotarkoitukset db 2026)}})

(defn find-complete-perusparannuspassi
  "Fetch a perusparannuspassi and enrich it with calculated fields.
   Returns the complete perusparannuspassi with augmented vaiheet, or nil if not found."
  [db whoami id]
  (when-let [ppp (perusparannuspassi-service/find-perusparannuspassi db whoami id)]
    (when-let [energiatodistus (energiatodistus-service/find-energiatodistus
                                 db whoami (:energiatodistus-id ppp))]
      (let [luokittelut-data (luokittelut db)]
        (complete-perusparannuspassi ppp energiatodistus luokittelut-data)))))
