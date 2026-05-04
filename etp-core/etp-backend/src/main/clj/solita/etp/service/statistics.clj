(ns solita.etp.service.statistics
  (:require [solita.etp.db :as db]
            [solita.etp.service.luokittelu :as luokittelu-service]))

;; *** Require sql functions ***
(db/require-queries 'statistics)

(def find-kayttotarkoitukset luokittelu-service/find-statistics-kayttotarkoitukset)

(def default-query {:keyword nil
                    :kayttotarkoitus-id nil
                    :valmistumisvuosi-min nil
                    :valmistumisvuosi-max nil
                    :lammitetty-nettoala-min nil
                    :lammitetty-nettoala-max nil})

(def min-sample-size 4)

(defn sufficient-sample-size? [versio-counts]
  (->> versio-counts :e-luokka vals (reduce +) (<= min-sample-size)))

(defn find-e-luokka-counts
  "Returns e-luokka counts grouped by versio."
  [db query]
  (reduce (fn [acc {:keys [versio e-luokka count]}]
            (cond-> acc
              e-luokka
              (assoc-in [versio :e-luokka e-luokka] count)))
          {}
          (statistics-db/select-e-luokka-counts db query)))

(defn find-lammitys-ilmanvaihto-counts
  "Returns lammitysmuoto and ilmanvaihto counts grouped by versio."
  [db query]
  (reduce (fn [acc {:keys [versio lammitysmuoto-id ilmanvaihtotyyppi-id count]}]
            (cond-> acc
              lammitysmuoto-id
              (assoc-in [versio :lammitysmuoto lammitysmuoto-id] count)

              ilmanvaihtotyyppi-id
              (assoc-in [versio :ilmanvaihto ilmanvaihtotyyppi-id] count)))
          {}
          (statistics-db/select-lammitys-ilmanvaihto-counts db query)))

(defn find-counts
  "Returns combined e-luokka, lammitysmuoto, and ilmanvaihto counts per versio."
  [db query]
  (merge-with merge
              (find-e-luokka-counts db query)
              (find-lammitys-ilmanvaihto-counts db query)))

(defn find-e-luku-statistics [db query versio]
  (first (statistics-db/select-e-luku-statistics db (assoc query
                                                           :versio
                                                           versio))))

(defn find-common-averages [db query]
  (first (statistics-db/select-common-averages db query)))

(defn find-uusiutuvat-omavaraisenergiat-counts [db query versio]
  (first (statistics-db/select-uusiutuvat-omavaraisenergiat-counts
          db
          (assoc query :versio versio))))

(defn future-when [f pred]
  (future (when pred (f))))

(defn find-statistics [db query]
  (let [query (merge default-query query)
        e-luokka-counts (future-when #(find-e-luokka-counts db query) true)
        lammitys-iv-counts (future-when #(find-lammitys-ilmanvaihto-counts db query) true)
        return-2013? (sufficient-sample-size? (get @e-luokka-counts 2013))
        return-2018? (sufficient-sample-size? (get @e-luokka-counts 2018))
        return-2026? (sufficient-sample-size? (get @e-luokka-counts 2026))
        e-luku-statistics-2013 (future-when
                                #(find-e-luku-statistics db query 2013)
                                return-2013?)
        e-luku-statistics-2018 (future-when
                                #(find-e-luku-statistics db query 2018)
                                return-2018?)
        e-luku-statistics-2026 (future-when
                                #(find-e-luku-statistics db query 2026)
                                return-2026?)
        common-averages (future-when
                         #(find-common-averages db query)
                         (or return-2013? return-2018? return-2026?))
        uusiutuvat-omavaraisenergiat-counts-2018
        (future-when
         #(find-uusiutuvat-omavaraisenergiat-counts db query 2018)
         return-2018?)
        uusiutuvat-omavaraisenergiat-counts-2026
        (future-when
         #(find-uusiutuvat-omavaraisenergiat-counts db query 2026)
         return-2026?)]
    (let [result {:counts {2013 (when return-2013?
                                (merge (get @e-luokka-counts 2013)
                                       (get @lammitys-iv-counts 2013)))
                           2018 (when return-2018?
                                (merge (get @e-luokka-counts 2018)
                                       (get @lammitys-iv-counts 2018)))
                           2026 (when return-2026?
                                (merge (get @e-luokka-counts 2026)
                                       (get @lammitys-iv-counts 2026)))}
                  :e-luku-statistics {2013 @e-luku-statistics-2013
                                      2018 @e-luku-statistics-2018
                                      2026 @e-luku-statistics-2026}
                  :common-averages @common-averages
                  :uusiutuvat-omavaraisenergiat-counts
                  {2018 @uusiutuvat-omavaraisenergiat-counts-2018
                   2026 @uusiutuvat-omavaraisenergiat-counts-2026}}]
      result)))

(defn find-count [db]
  (first (statistics-db/select-count db)))
