(ns solita.etp.l10n-audit
  "Find likely missing Swedish translations in the source tree.

  This is deliberately a candidate finder, not a language detector. Equal
  Finnish and Swedish values can be valid for names, abbreviations, and
  numbers, so every result needs a human review."
  (:require
    [clojure.java.io :as io]
    [clojure.data.csv :as csv]
    [clojure.string :as str]
    [jsonista.core :as json]
    [solita.etp.service.localization :as localization]))

(def catalog-pairs
  [["etp-front/src/language/fi.json" "etp-front/src/language/sv.json"]
   ["etp-public/assets/fi.json" "etp-public/assets/sv.json"]])

(def backend-localization-objects
  [["solita.etp.service.localization/ppp-pdf-localization"
    localization/ppp-pdf-localization]
   ["solita.etp.service.localization/et-pdf-localization"
    localization/et-pdf-localization]])

(def sql-root
  "etp-core/etp-db/src/main/sql/migration/repeatable")

(def sql-location-prefix
  "etp-core/etp-db/src/main/sql/migration/")

(def csv-headings ["Location" "Key" "Finnish text" "Swedish status"])

(def obvious-identical-values
  #{"$unused" "U" "U*A" "A" "g-ks" "kg" ">" "<" "<=" ">=" "="
    "°C" "WWW" "W/(m² K)" "Cognito ID" "Virtu"})

(def obvious-identical-keys
  #{"TILASTOT_U_ARVOT_UNIT"})

(defn catalog-same-value-excluded? [key value]
  (or (contains? obvious-identical-values value)
      (contains? obvious-identical-keys key)
      (str/ends-with? key "-fi")
      (str/ends-with? key "-sv")))

(defn shorten [value]
  (let [value (str/trim (str (or value "")))]
    (if (> (count value) 100)
      (str (subs value 0 97) "...")
      value)))

(defn result [location key finnish status]
  [location (or key "") (shorten finnish) status])

(defn relative-path [root file]
  (str (.relativize (.toPath (io/file root))
                    (.toPath (io/file file)))))

(defn flatten-map [prefix value]
  (if (map? value)
    (mapcat (fn [[key child]]
              (flatten-map (conj prefix (name key)) child))
            value)
    [[(str/join "." prefix) value]]))

(defn audit-catalog-pair [root [fi-path sv-path]]
  (let [fi-file (io/file root fi-path)
        sv-file (io/file root sv-path)]
    (if (and (.isFile fi-file) (.isFile sv-file))
      (let [fi (json/read-value (slurp fi-file) (json/object-mapper))
            sv (json/read-value (slurp sv-file) (json/object-mapper))
            fi-values (into {} (flatten-map [] fi))
            sv-values (into {} (flatten-map [] sv))]
        (reduce
          (fn [results [key fi-value]]
            (let [sv-value (get sv-values key ::missing)]
              (cond
                (= ::missing sv-value)
                (conj results (result sv-path key fi-value "absent"))

                (or (nil? sv-value) (and (string? sv-value) (str/blank? sv-value)))
                (conj results (result sv-path key fi-value "absent"))

                (and (string? sv-value) (re-find #"(?i)\(sv\)\s*$" sv-value))
                (conj results (result sv-path key fi-value "SV suffix"))

                (and (string? fi-value)
                     (= fi-value sv-value)
                     (not (str/blank? fi-value))
                     (not (catalog-same-value-excluded? key fi-value)))
                (conj results (result sv-path key fi-value "same as Finnish"))

                :else results)))
          []
          fi-values))
      [(result sv-path nil nil "absent")])))

(defn audit-localization-object [location object]
  (let [fi-values (into {} (flatten-map [] (:fi object)))
        sv-values (into {} (flatten-map [] (:sv object)))]
    (reduce
      (fn [results [key fi-value]]
        (let [sv-value (get sv-values key ::missing)]
          (cond
            (= ::missing sv-value)
            (conj results (result location key fi-value "absent"))

            (or (nil? sv-value) (and (string? sv-value) (str/blank? sv-value)))
            (conj results (result location key fi-value "absent"))

            (and (string? sv-value) (re-find #"(?i)\(sv\)\s*$" sv-value))
            (conj results (result location key fi-value "SV suffix"))

            (and (string? fi-value)
                 (= fi-value sv-value)
                 (not (str/blank? fi-value)))
            (conj results (result location key fi-value "same as Finnish"))

            :else results)))
      []
      fi-values)))

(defn audit-backend-localization []
  (mapcat (fn [[location object]]
            (audit-localization-object location object))
          backend-localization-objects))

(defn split-sql-values [text]
  (loop [remaining (seq text)
         quoted? false
         dollar-quoted? false
         depth 0
         current []
         values []]
    (if-let [character (first remaining)]
      (let [next-character (second remaining)]
        (cond
          (and (= character \$) (= next-character \$))
          (recur (nnext remaining) quoted? (not dollar-quoted?) depth current values)

          dollar-quoted?
          (recur (next remaining) quoted? dollar-quoted? depth (conj current character) values)

          (and (= character \') quoted? (= next-character \'))
          (recur (nnext remaining) quoted? dollar-quoted? depth (conj current character character) values)

          (and (= character \') (not quoted?))
          (recur (next remaining) true dollar-quoted? depth (conj current character) values)

          (and (= character \') quoted?)
          (recur (next remaining) false dollar-quoted? depth (conj current character) values)

          (and (= character \,) (not quoted?) (zero? depth))
          (recur (next remaining) quoted? dollar-quoted? depth []
                 (conj values (str/trim (apply str current))))

          (and (= character \() (not quoted?))
          (recur (next remaining) quoted? dollar-quoted? (inc depth)
                 (conj current character) values)

          (and (= character \)) (not quoted?))
          (recur (next remaining) quoted? dollar-quoted? (dec depth)
                 (conj current character) values)

          :else
          (recur (next remaining) quoted? dollar-quoted? depth
                 (conj current character) values)))
      (conj values (str/trim (apply str current))))))

(defn sql-tuples [text]
  (loop [remaining (seq text)
         quoted? false
         dollar-quoted? false
         depth 0
         current []
         tuples []]
    (if-let [character (first remaining)]
      (let [next-character (second remaining)]
        (cond
          (and (= character \$) (= next-character \$))
          (recur (nnext remaining) quoted? (not dollar-quoted?) depth current tuples)

          dollar-quoted?
          (recur (next remaining) quoted? dollar-quoted? depth
                 (conj current character) tuples)

          (and (= character \') quoted? (= next-character \'))
          (recur (nnext remaining) quoted? dollar-quoted? depth
                 (conj current character character) tuples)

          (and (= character \') (not quoted?))
          (recur (next remaining) true dollar-quoted? depth
                 (conj current character) tuples)

          (and (= character \') quoted?)
          (recur (next remaining) false dollar-quoted? depth
                 (conj current character) tuples)

          (and (= character \() (not quoted?) (zero? depth))
          (recur (next remaining) quoted? dollar-quoted? 1 [] tuples)

          (and (= character \)) (not quoted?) (= depth 1))
          (recur (next remaining) quoted? dollar-quoted? 0 []
                 (conj tuples (apply str current)))

          (pos? depth)
          (recur (next remaining) quoted? dollar-quoted? depth
                 (conj current character) tuples)

          :else
          (recur (next remaining) quoted? dollar-quoted? depth current tuples)))
      tuples)))

(defn sql-text [value]
  (let [value (str/trim value)]
    (if (and (<= 2 (count value))
             (= \' (first value))
             (= \' (last value)))
      (-> value (subs 1 (dec (count value))) (str/replace "''" "'"))
      value)))

(defn compare-sql-values? [path value]
  (and (not (str/includes? path "/geo/"))
       (not (str/ends-with? path "/r-countries.sql"))
       (not (contains? #{"Etuovi" "Oikotie" "Suomi.fi"} value))))

(defn audit-sql-file [root file]
  (let [path (-> (relative-path root file)
                 (str/replace sql-location-prefix ""))
        header-pattern #"(?i)^\s*insert\s+into\s+\S+\s*\(([^)]*)\)\s*(.*)$"
        parsed-statements (->> (line-seq (io/reader file))
                               (map-indexed vector)
                               (reduce
                                 (fn [{:keys [statements current]} [index line]]
                                   (cond
                                     (and (nil? current) (re-find header-pattern line))
                                     (let [[_ columns rest] (re-find header-pattern line)]
                                       {:statements statements
                                        :current {:line (inc index)
                                                  :columns columns
                                                  :values [rest]}})

                                     (and current (re-find #"(?i)^\s*on\s+conflict\b" line))
                                     {:statements (conj statements current) :current nil}

                                     current
                                     {:statements statements
                                      :current (update current :values conj line)}

                                     :else
                                     {:statements statements :current nil}))
                                 {:statements [] :current nil}))
        statements (cond-> (:statements parsed-statements)
                     (:current parsed-statements)
                     (conj (:current parsed-statements)))]
    (mapcat
      (fn [{:keys [columns values]}]
        (let [columns (mapv #(keyword (str/trim %)) (str/split columns #","))
              fi-index (.indexOf columns :label_fi)
              sv-index (.indexOf columns :label_sv)
              id-index (.indexOf columns :id)]
          (when (and (not= -1 fi-index)
                     (not= -1 sv-index)
                     (re-find #"(?is)^\s*values\b" (str/join "\n" values)))
            (for [tuple (sql-tuples (str/join "\n" values))
                  :let [fields (mapv sql-text (split-sql-values tuple))
                        fi (get fields fi-index)
                        sv (get fields sv-index)]
                  :when (or (str/blank? sv)
                            (and (compare-sql-values? path fi) (= fi sv))
                            (re-find #"(?i)\(sv\)\s*$" sv))]
              (result path
                      (when (not= -1 id-index)
                        (str "id=" (get fields id-index)))
                      fi
                      (cond
                        (str/blank? sv) "absent"
                        (re-find #"(?i)\(sv\)\s*$" sv) "SV suffix"
                        :else "same as Finnish"))))))
      statements)))

(defn audit-sql [root]
  (let [directory (io/file root sql-root)]
    (if (.isDirectory directory)
      (->> (file-seq directory)
           (filter #(and (.isFile %) (str/ends-with? (.getName %) ".sql")))
           (mapcat #(audit-sql-file root %))
           vec)
      [(result sql-root nil nil "absent")])))

(defn print-results [results]
  (csv/write-csv *out* (cons csv-headings results)))

(defn -main [& args]
  (let [root (io/file (or (first args) "."))
        results (concat
                 (mapcat #(audit-catalog-pair root %) catalog-pairs)
                 (audit-backend-localization)
                 (audit-sql root))]
    (print-results results)))
