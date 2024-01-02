(ns solita.etp.service.energiatodistus-validation
  (:require [clojure.string :as str]
            [solita.etp.exception :as exception]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]
            [solita.etp.service.kielisyys :as kielisyys]
            [solita.etp.service.luokittelu :as luokittelu]
            [solita.common.logic :as logic]
            [flathead.deep :as deep]))

(def required-condition
  {"perustiedot.rakennustunnus" (logic/if* (logic/pred = :versio 2013)
                                           (constantly true)
                                           (complement laatimisvaihe/rakennuslupa?))

   "perustiedot.havainnointikaynti" laatimisvaihe/olemassaoleva-rakennus?
   "perustiedot.keskeiset-suositukset-fi" laatimisvaihe/olemassaoleva-rakennus?
   "perustiedot.keskeiset-suositukset-sv" laatimisvaihe/olemassaoleva-rakennus?

   "lahtotiedot.ilmanvaihto.kuvaus-fi" luokittelu/ilmanvaihto-kuvaus-required?
   "lahtotiedot.ilmanvaihto.kuvaus-sv" luokittelu/ilmanvaihto-kuvaus-required?

   "lahtotiedot.lammitys.lammitysmuoto-1.kuvaus-fi" luokittelu/lammitysmuoto-1-kuvaus-required?
   "lahtotiedot.lammitys.lammitysmuoto-1.kuvaus-sv" luokittelu/lammitysmuoto-1-kuvaus-required?

   "lahtotiedot.lammitys.lammitysmuoto-2.kuvaus-fi" luokittelu/lammitysmuoto-2-kuvaus-required?
   "lahtotiedot.lammitys.lammitysmuoto-2.kuvaus-sv" luokittelu/lammitysmuoto-2-kuvaus-required?

   "lahtotiedot.lammitys.lammonjako.kuvaus-fi" luokittelu/lammonjako-kuvaus-required?
   "lahtotiedot.lammitys.lammonjako.kuvaus-sv" luokittelu/lammonjako-kuvaus-required?

   "huomiot.ymparys.teksti-fi" laatimisvaihe/olemassaoleva-rakennus?
   "huomiot.ymparys.teksti-sv" laatimisvaihe/olemassaoleva-rakennus?})

(defn localized-property-condition [property]
  (cond
    (str/ends-with? property "-fi") kielisyys/fi?
    (str/ends-with? property "-sv") kielisyys/sv?
    :else nil))

(def path
  (comp
    (partial map keyword)
    #(str/split % #"\.")))

(defn assoc-last [v new-value]
  (assoc v (dec (count v)) new-value))

(defn u-property-condition [property]
  (when (or (str/ends-with? (str/lower-case property) ".u")
            (str/ends-with? (str/lower-case property) ".g-ks"))
    (let [u-path (vec (path property))
          ala-path (assoc-last u-path :ala)]
      #(> (or (get-in % ala-path) 0) 0))))

(defn required-constraints
  "Required constraint is a pair of predicate and a required property.
  The property is required if predicate is true for particular energiatodistus."
  [required-properties]
  (map (juxt
         (comp
           #(if (empty? %)
              (constantly true)
              (apply every-pred %))
           (partial filter (complement nil?))
           (juxt required-condition
                 localized-property-condition
                 u-property-condition))
         identity)
       required-properties))

(defn missing-properties [required-constraints energiatodistus]
  (->> required-constraints
       (filter (fn [[predicate property]]
                 (and (predicate energiatodistus)
                      (nil? (get-in energiatodistus (path property))))))
       (map second)))

(defn validate-required!
  ([required-constraints current update]
    (let [missing-current (set (missing-properties required-constraints current))
          constraints (filter #(not (contains? missing-current (second %)))
                              required-constraints)]
      (validate-required! constraints update)))

  ([required-constraints energiatodistus]
    (let [missing (missing-properties required-constraints energiatodistus)]
      (when-not (empty? missing)
        (exception/throw-ex-info!
          {:type :missing-value
           :missing missing
           :message (str "Required value is missing in properties: "
                         (str/join ", " missing))})))))

(defn- find-constant-kuorma [constant-kuormat kayttotarkoitusluokka-id]
  (->> constant-kuormat
      (filter (comp (partial = kayttotarkoitusluokka-id) :kayttotarkoitusluokka-id))
       first))

(defn- find-alakayttotarkoitusluokka [alakayttotarkoitusluokat energiatodistus]
  (some-> energiatodistus
          :perustiedot :kayttotarkoitus
          (luokittelu/find-luokka alakayttotarkoitusluokat)))

(defn- constant-kuorma-properties [kuorma]
  (-> kuorma
      (dissoc :kayttotarkoitusluokka-id)
      (update :valaistus #(dissoc % :lampokuorma))))

(defn validate-sisainen-kuorma! [constant-kuormat
                                  alakayttotarkoitusluokat
                                  energiatodistus]
  (logic/if-let*
    [alakayttotarkoitusluokka
     (find-alakayttotarkoitusluokka alakayttotarkoitusluokat energiatodistus)
     constant-kuorma
     (some-> constant-kuormat
         (find-constant-kuorma (:kayttotarkoitusluokka-id alakayttotarkoitusluokka))
         constant-kuorma-properties)]

    (when-not (= constant-kuorma
                 (->> energiatodistus
                      :lahtotiedot :sis-kuorma constant-kuorma-properties
                      (deep/map-values (logic/when* number? bigdec))))
      (exception/throw-ex-info!
        {:type         :invalid-sisainen-kuorma
         :valid-kuorma constant-kuorma
         :message      (str "Values of sisainen kuorma are fixed and must be: " constant-kuorma)}))))
