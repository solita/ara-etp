(ns solita.etp.service.energiatodistus-pdf.ilmastoselvitys
  (:require
    [clojure.string :as str]
    [solita.common.formats :as formats]
    [solita.etp.service.localization :as loc]
    [solita.common.time :as time]
    [hiccup.core :refer [h]]))

(defn- fmt
  "Format number with specified decimal places. Returns empty string for nil."
  [value decimals] (or (formats/format-number value decimals false) ""))

(def ^:private hiilijalanjalki-fields
  [:rakennustuotteiden-valmistus
   :kuljetukset-tyomaavaihe
   :rakennustuotteiden-vaihdot
   :energiankaytto
   :purkuvaihe])

(defn hiilijalanjalki-rakennus-yhteensa
  "Calculate the sum of all hiilijalanjälki rakennus values. Nil values are ignored."
  [hiilijalanjalki-rakennus]
  (->> (select-keys hiilijalanjalki-rakennus hiilijalanjalki-fields)
       vals
       (remove nil?)
       (reduce + 0)))

(defn hiilijalanjalki-rakennuspaikka-yhteensa
  "Calculate the sum of all hiilijalanjälki rakennuspaikka values. Nil values are ignored."
  [hiilijalanjalki-rakennuspaikka]
  (->> (select-keys hiilijalanjalki-rakennuspaikka hiilijalanjalki-fields)
       vals
       (remove nil?)
       (reduce + 0)))

(defn has-ilmastoselvitys?
  "Returns true if the energiatodistus has a completed ilmastoselvitys."
  [energiatodistus]
  (boolean (some? (get-in energiatodistus [:ilmastoselvitys :laatimisajankohta]))))

(defn gwp-value-for-etusivu
  "Returns the GWP value string for the front page.
   If ilmastoselvitys exists: sum of rakennus hiilijalanjälki values.
   If not: returns \"-\"."
  [energiatodistus]
  (if (has-ilmastoselvitys? energiatodistus)
    (fmt (hiilijalanjalki-rakennus-yhteensa
           (get-in energiatodistus [:ilmastoselvitys :hiilijalanjalki :rakennus])) 1)
    "-"))

(defn- resolve-laadintaperuste
  "Look up laadintaperuste label from classification data."
  [laadintaperuste-id laadintaperusteet kieli]
  (let [label-key (case kieli :fi :label-fi :sv :label-sv)]
    (some #(when (= (:id %) laadintaperuste-id) (label-key %)) laadintaperusteet)))

(defn- description-list
  "Render a description list as Hiccup."
  [key-vals]
  (into [:dl]
        (mapv (fn [{:keys [dt dd]}]
                [:div [:dt (str dt ":")] [:dd dd]])
              key-vals)))

(defn- metatiedot-section
  "Render the metatiedot (metadata) section of the ilmastoselvitys page.
   Shows three labeled rows: laatimisajankohta, laatija (with company details), and laadintaperuste."
  [ilmastoselvitys l laadintaperusteet kieli]
  (let [{:keys [laatimisajankohta laatija yritys yritys-osoite
                 yritys-postinumero yritys-postitoimipaikka laadintaperuste]} ilmastoselvitys]
    [:div {:class "is-metatiedot"}
     (description-list
       [{:dt (l :is-laatimisajankohta)
         :dd (if laatimisajankohta (time/format-date laatimisajankohta) "-")}
        {:dt (l :is-laatija)
         :dd [:div
              (h (str laatija))
              [:br]
              (h (str yritys))
              [:br]
              (h (str yritys-osoite))
              [:br]
              (h (str yritys-postinumero " " yritys-postitoimipaikka))]}
        {:dt (l :is-laadintaperuste)
         :dd (or (resolve-laadintaperuste laadintaperuste laadintaperusteet kieli) "-")}])]))

(defn- gwp-data-row
  "Render one data row (rakennus or rakennuspaikka) with all field values as columns."
  ([label data field-keys]
   (into [:tr [:td label]]
         (mapv (fn [k] [:td (fmt (k data) 1)]) field-keys)))
  ([label data field-keys yhteensa]
   (into [:tr [:td label]]
         (conj (mapv (fn [k] [:td (fmt (k data) 1)]) field-keys)
               [:td (fmt yhteensa 1)]))))

(defn- th-header
  "Render a table header cell with label text. Line breaks in the label
   are converted to [:br] elements."
  [label]
  (let [lines (str/split label #"\n")]
    (if (= 1 (count lines))
      [:th.is-otsikko label]
      (into [:th.is-otsikko]
            (interpose [:br] lines)))))

(defn- row-label-with-unit
  "Render a row label with unit text on a second line."
  [label]
  [:div label [:br] [:span {:class "is-unit"} "kgCO" [:sub "2"] "ekv/m²"]])

(defn- hiilijalanjalki-table
  "Render the hiilijalanjälki table with rakennus and rakennuspaikka as rows
   and GWP fields as columns."
  [hiilijalanjalki l]
  (let [rakennus (:rakennus hiilijalanjalki)
        rakennuspaikka (:rakennuspaikka hiilijalanjalki)
        col-defs [[:is-rakennustuotteiden-valmistus :rakennustuotteiden-valmistus]
                  [:is-kuljetukset-tyomaavaihe :kuljetukset-tyomaavaihe]
                  [:is-rakennustuotteiden-vaihdot :rakennustuotteiden-vaihdot]
                  [:is-energiankaytto :energiankaytto]
                  [:is-purkuvaihe :purkuvaihe]]
        field-keys (mapv second col-defs)]
    [:table {:class "is-hiilijalanjalki"}
     [:thead
      (into [:tr [:th.empty]]
            (conj (mapv (fn [[label-key _]] (th-header (l label-key))) col-defs)
                  (th-header (l :is-yhteensa))))]
     [:tbody
      (gwp-data-row (row-label-with-unit (l :is-rakennus)) rakennus field-keys
                    (hiilijalanjalki-rakennus-yhteensa rakennus))
      (gwp-data-row (row-label-with-unit (l :is-rakennuspaikka)) rakennuspaikka field-keys
                    (hiilijalanjalki-rakennuspaikka-yhteensa rakennuspaikka))]]))

(defn- hiilikadenjalki-table
  "Render the hiilikädenjälki table with rakennus and rakennuspaikka as rows
   and GWP fields as columns."
  [hiilikadenjalki l]
  (let [rakennus (:rakennus hiilikadenjalki)
        rakennuspaikka (:rakennuspaikka hiilikadenjalki)
        col-defs [[:is-uudelleenkaytto :uudelleenkaytto]
                  [:is-kierratys :kierratys]
                  [:is-ylimaarainen-uusiutuvaenergia :ylimaarainen-uusiutuvaenergia]
                  [:is-hiilivarastovaikutus :hiilivarastovaikutus]
                  [:is-karbonatisoituminen :karbonatisoituminen]]
        field-keys (mapv second col-defs)]
    [:table {:class "is-hiilikadenjalki"}
     [:thead
      (into [:tr [:th.empty]]
            (mapv (fn [[label-key _]] (th-header (l label-key))) col-defs))]
     [:tbody
      (gwp-data-row (row-label-with-unit (l :is-rakennus)) rakennus field-keys)
      (gwp-data-row (row-label-with-unit (l :is-rakennuspaikka)) rakennuspaikka field-keys)]]))

(defn ilmastoselvitys-page-content
  "Generate the ilmastoselvitys page content as a Hiccup data structure."
  [{:keys [energiatodistus kieli ilmastoselvitys-laadintaperusteet]}]
  (let [l (kieli loc/et-pdf-localization)
        ilmastoselvitys (:ilmastoselvitys energiatodistus)]
    [:div {:class "ilmastoselvitys"}
     [:h2 (l :ilmastoselvitys-otsikko)]
     [:p {:class "is-intro"} (l :is-intro-teksti)]
     [:div {:class "is-separator"}]
     (metatiedot-section ilmastoselvitys l ilmastoselvitys-laadintaperusteet kieli)
     [:h3 (l :is-hiilijalanjalki-otsikko)]
     [:p {:class "is-description"} (l :is-hiilijalanjalki-teksti)]
     (hiilijalanjalki-table (:hiilijalanjalki ilmastoselvitys) l)
     [:h3 (l :is-hiilikadenjalki-otsikko)]
     [:p {:class "is-description"} (l :is-hiilikadenjalki-teksti)]
     (hiilikadenjalki-table (:hiilikadenjalki ilmastoselvitys) l)]))
