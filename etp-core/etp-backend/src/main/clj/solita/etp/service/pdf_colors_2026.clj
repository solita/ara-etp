(ns solita.etp.service.pdf-colors-2026
  (:require [clojure.string :as str]))

(def e-luokat
  ["A+" "A0" "A" "B" "C" "D" "E" "F" "G"])

(def e-luokka-colors
  {"A+" "#009641"
   "A0" "#52ae32"
   "A"  "#c8d302"
   "B"  "#ffed00"
   "C"  "#fbb900"
   "D"  "#ec6608"
   "E"  "#e50104"
   "F"  "#e40202"
   "G"  "#e40202"})

(def e-luokka-text-colors
  {"A+" "#000000"
   "A0" "#000000"
   "A"  "#000000"
   "B"  "#000000"
   "C"  "#000000"
   "D"  "#000000"
   "E"  "#ffffff"
   "F"  "#ffffff"
   "G"  "#ffffff"})

(defn e-luokka-color [e-luokka]
  (get e-luokka-colors e-luokka (e-luokka-colors "G")))

(defn e-luokka-text-color [e-luokka]
  (get e-luokka-text-colors e-luokka "#000000"))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn text-color-for-background-color [background-color]
  (or (some (fn [[e-luokka color]]
              (when (= color background-color)
                (e-luokka-text-color e-luokka)))
            e-luokka-colors)
      "#000000"))

(defn e-luokka-css-class [e-luokka]
  (when e-luokka
    (str "energialuokka-"
         (-> (str e-luokka)
             str/lower-case
             (str/replace "+" "plus")))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn e-luokka-css []
  (str/join
    "\n\n"
    (for [e-luokka e-luokat]
      (str "." (e-luokka-css-class e-luokka) " {\n"
           "    background-color: " (e-luokka-color e-luokka) ";\n"
           "    color: " (e-luokka-text-color e-luokka) ";\n"
           "}"))))


