(ns solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia :as laskennallinen-ostoenergia]))

(defn- flatten-hiccup-strings
  "Recursively extract all string values from a hiccup data structure."
  [hiccup]
  (->> (tree-seq coll? seq hiccup)
       (filter string?)))

(defn- hiccup-contains-string?
  "Check if any string in the hiccup tree contains the given substring."
  [hiccup substring]
  (some #(.contains ^String % substring) (flatten-hiccup-strings hiccup)))

(t/deftest ostoenergia-tiedot-shows-dash-when-nil-test
  ;; Given: a 2026 energiatodistus where the calculation cannot be performed
  ;;        (e.g. E-luku = 0 and no utilized renewable → denominator = 0)
  (let [energiatodistus {:versio 2026
                         :lahtotiedot {:lammitetty-nettoala 200}
                         :tulokset {:e-luku 0
                                    :kaytettavat-energiamuodot {}
                                    :uusiutuvat-omavaraisenergiat
                                    {:aurinkosahko 0 :tuulisahko 0 :aurinkolampo 0
                                     :muulampo 0 :muusahko 0 :lampopumppu 0}
                                    :uusiutuvat-omavaraisenergiat-kokonaistuotanto
                                    {:aurinkosahko 5000 :tuulisahko 0 :aurinkolampo 0
                                     :muulampo 0 :muusahko 0 :lampopumppu 0}}
                         :ilmastoselvitys nil}
        ;; When: ostoenergia-tiedot generates the hiccup structure
        result (laskennallinen-ostoenergia/ostoenergia-tiedot {:energiatodistus energiatodistus
                                        :kieli :fi})]
    ;; Then: a dash "-" should be displayed for the uusiutuva-energian-osuus field
    (t/is (hiccup-contains-string? result "-")
          "ostoenergia-tiedot should contain a dash when calculation returns nil")))
