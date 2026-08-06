(ns solita.etp.postinumero-update
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.string :as str]
    [solita.etp.service.geo :as geo-service]))

(def ahvenanmaa-toiminta-alue "FI200")

(defn format-kunta-id [kunta-id]
  (let [kunta-id (str kunta-id)]
    (case (count kunta-id)
      1 (str "00" kunta-id)
      2 (str "0" kunta-id)
      kunta-id)))

(defn line->postinumero [line]
  {:id       (-> line (subs 13 18) Integer/parseInt)
   :label-fi (-> line (subs 18 48) str/trim)
   :label-sv (-> line (subs 48 78) str/trim)
   :valid    true
   :kunta-id (-> line (subs 176 179) Integer/parseInt)
   :type-id  (-> line (subs 110 111) Integer/parseInt)
   :toimintaalue-id (-> line (subs 111 116) str/trim)})

(defn ->id-pair [thing]
  [(:id thing) thing])

(defn with-invalidations [absent-ids postinumerot]
  (reduce (fn [postinumerot absent-id] (assoc-in postinumerot [absent-id :valid] false))
          postinumerot absent-ids))

(defn ->sorted-by-id [postinumerot]
  (->> postinumerot
       (map second)
       (sort-by :id)
       vec))

(defn ->migration-line [{:keys [id label-fi label-sv kunta-id type-id valid]}]
  (str "(" id ", '" label-fi "', '" label-sv "', '" (format-kunta-id kunta-id) "', " (if type-id type-id "null") ", " valid ")"))

(defn wrap-ends [mid-sql]
  (str
    "\n-- conversion\n-- select '(' || postinumero::int || ', ''' || nimi || ''', ''' || nimisv || ''', ''' || kunta || '''),'\n-- from etp.postinumero order by postinumero;\n\ninsert into postinumero (id, label_fi, label_sv, kunta_id, type_id, valid)\nvalues\n"
    mid-sql
    "\non conflict (id) do update set\n  label_fi = excluded.label_fi,\n  label_sv = excluded.label_sv,\n  kunta_id = excluded.kunta_id,\n  type_id = excluded.type_id,\n  valid = excluded.valid;"))

(defn load-new-postinumerot [input]
  (with-open [reader (io/reader input :encoding "ISO-8859-1")]
    (->> reader
         line-seq
         (map line->postinumero)
         (filter #(not (= ahvenanmaa-toiminta-alue (:toimintaalue-id %))))
         (map ->id-pair)
         (into {}))))

(defn merge-new-postinumerot [db input dest]
  (let [existing-postinumerot (->> (geo-service/find-all-postinumerot db)
                                   (map ->id-pair)
                                   (into {}))
        new-postinumerot (load-new-postinumerot input)
        existing-ids (->> existing-postinumerot (map first) set)
        new-ids (->> new-postinumerot (map first) set)
        newly-absent-postinumerot (set/difference existing-ids new-ids)]
    (->> (merge existing-postinumerot new-postinumerot)
         (with-invalidations newly-absent-postinumerot)
         ->sorted-by-id
         (map ->migration-line)
         (str/join ",\n")
         wrap-ends
         (spit dest))))
