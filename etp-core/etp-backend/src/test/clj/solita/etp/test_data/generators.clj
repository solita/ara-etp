(ns solita.etp.test-data.generators
  (:require [clojure.test.check.generators :as test-generators]
            [schema-generators.generators :as g]
            [schema-generators.complete :as c]
            [solita.etp.schema.common :as common]
            [solita.etp.schema.geo :as geo]
            [solita.etp.schema.laatija :as laatija]
            [solita.etp.schema.energiatodistus :as energiatodistus]
            [solita.etp.schema.kayttaja]))

(defn unique-henkilotunnukset-f []
  (let [state (atom 0)]
    (fn [n]
      (let [[start end] (swap-vals! state + n)]
        (->> (range start end)
             (map (partial format "%09d"))
             (map #(str % (common/henkilotunnus-checksum %)))
             (map #(str (subs % 0 6) "-" (subs % 6 10))))))))

(def unique-henkilotunnukset (unique-henkilotunnukset-f))

(defn unique-emails [n]
  (take n (repeatedly #(str (java.util.UUID/randomUUID) "@example.com"))))

(def unique-ytunnukset
  (->> (range)
       (map (partial format "%07d"))
       (filter #(not= (common/ytunnus-checksum %) 10))
       (map #(str % "-" (common/ytunnus-checksum %)))))

(defn random-printable-ascii-char []
  (char (+ 33 (rand-int 94))))

(defn generate-password [_]
  (apply str (take (+ 8 (rand-int 192)) (repeatedly random-printable-ascii-char))))

(def kuukausierittely {:tuotto  {:aurinkosahko 1M
                                 :tuulisahko   2M
                                 :aurinkolampo nil
                                 :muulampo     3.5M
                                 :muusahko     4M
                                 :lampopumppu  5.6789M}
                       :kulutus {:sahko nil
                                 :lampo 6.789M}})

(defn- rakennustunnus-generator [_]
  (let [number-part (apply str (repeatedly 9 #(rand-nth "0123456789")))
        checksum (common/henkilotunnus-checksum number-part)]
    (str number-part checksum)))

(defn generate-rakennustunnus []
  (rakennustunnus-generator nil))

(def generators
  {common/Num1                              (test-generators/choose 0 1)
   common/NonNegative                       test-generators/nat
   common/IntNonNegative                    test-generators/nat
   common/Year                              (test-generators/choose 2018 2025)
   common/Henkilotunnus                     (g/always "130200A892S")
   common/Ytunnus                           (g/always "0000000-0")
   common/Verkkolaskuosoite                 (g/always "003712345671")
   common/Date                              (g/always (java.time.LocalDate/now))
   common/Instant                           (g/always (java.time.Instant/now))
   common/Url                               (g/always "https://example.com")

   geo/Maa                                  (test-generators/elements ["FI" "SV"])
   geo/PostinumeroFI                        (test-generators/elements ["00100" "33100"])

   laatija/MuutToimintaalueet               (test-generators/list-distinct
                                              (test-generators/elements (range 18))
                                              {:min-elements 0
                                               :max-elements 5})

   common/Rakennustunnus                    (test-generators/fmap rakennustunnus-generator test-generators/boolean)
   energiatodistus/YritysPostinumero        (g/always "33100")
   energiatodistus/OptionalKuukausierittely (test-generators/one-of
                                              [(g/always (vec (repeat 12 kuukausierittely)))
                                               (g/always [])])
   solita.etp.schema.kayttaja/Password      (test-generators/fmap generate-password test-generators/boolean)})

(defn complete
  ([x schema]
   (complete x schema {}))
  ([x schema custom-generators]
   (c/complete x schema {} (merge generators custom-generators))))
