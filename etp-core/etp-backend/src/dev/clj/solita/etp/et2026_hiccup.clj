(ns solita.etp.et2026-hiccup
  (:require [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.service.toimenpide-ehdotus :as toimenpide-ehdotus-service]
            [solita.etp.service.energiatodistus-2026-pdf :as etp2026-pdf]
            [clojure.string :as str]))

(defn find-energiatodistus-2026-hiccup [db energiatodistus-id kieli]
  (let [complete-energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus db energiatodistus-id)
        luokittelut {:alakayttotarkoitukset              (kayttotarkoitus-service/find-alakayttotarkoitukset db 2026)
                     :kayttotarkoitukset                 (kayttotarkoitus-service/find-kayttotarkoitukset db 2026)
                     :laatimisvaiheet                    (luokittelu-service/find-laatimisvaiheet db)
                     :mahdollisuus-liittya               (luokittelu-service/find-mahdollisuus-liittya db)
                     :uusiutuva-energia                  (luokittelu-service/find-uusiutuva-energia db)
                     :lammitysmuodot                     (luokittelu-service/find-lammitysmuodot db)
                     :ilmanvaihtotyypit                  (luokittelu-service/find-ilmanvaihtotyypit db)
                     :toimenpide-ehdotukset              (toimenpide-ehdotus-service/find-all db)
                     :ilmastoselvitys-laadintaperusteet  (luokittelu-service/find-ilmastoselvitys-laadintaperusteet db)}
        kieli-keyword (keyword kieli)]
    (etp2026-pdf/generate-energiatodistus-hiccup
      {:energiatodistus                    complete-energiatodistus
       :alakayttotarkoitukset              (:alakayttotarkoitukset luokittelut)
       :laatimisvaiheet                    (:laatimisvaiheet luokittelut)
       :kieli                              kieli-keyword
       :kayttotarkoitukset                 (:kayttotarkoitukset luokittelut)
       :ilmastoselvitys-laadintaperusteet  (:ilmastoselvitys-laadintaperusteet luokittelut)})))

(defn search-hiccup [hiccup-data search-term]
  "Find paths (usable with clojure.core/get-in) into the hiccup data structure, pointing to elements that match the search term. Returns a list of paths.
   If search-term is a keyword, use an equality check. If search-term is a string, check if the string is contained in the element (case-insensitive)."
  (let [matches? (fn [element]
                   (cond
                     (keyword? search-term) (= element search-term)
                     (string? search-term)
                     (and (string? element)
                          (str/includes? (str/lower-case element) (str/lower-case search-term)))
                     :else false))]
    (letfn [(search-recursive [data path]
              (let [current-matches (if (matches? data) [path] [])]
                (cond
                  (or (vector? data) (list? data) (seq? data))
                  (concat current-matches
                          (mapcat (fn [idx] (search-recursive (nth data idx) (conj path idx)))
                                  (range (count data))))
                  (map? data)
                  (concat current-matches
                          (mapcat (fn [[k v]] (search-recursive v (conj path k)))
                                  (seq data)))
                  :else current-matches)))]
      (search-recursive hiccup-data []))))

(defn seq-get
  "Like get, but falls back to nth for sequential (non-map) types."
  [coll key]
  (cond
    (map? coll)        (get coll key)
    (sequential? coll) (nth coll key nil)
    :else              nil))

(defn seq-get-in
  "Like get-in, but treats all sequential types (lists, lazy seqs, etc.)
   uniformly by index, not just vectors."
  [coll path]
  (reduce seq-get coll path))

(defn pick [hiccup-data search-term]
  (let [paths (search-hiccup hiccup-data search-term)]
    (map #(seq-get-in hiccup-data (drop-last %)) paths)))
