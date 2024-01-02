(ns solita.etp.schema.common
  (:require [clojure.string :as str]
            [schema-tools.core :as schema-tools]
            [schema-tools.walk :as walk]
            [solita.common.schema :as xschema]
            [schema.core :as schema]))

(defn not-contains-keys [object schema]
  (every? #(not (contains? object %)) (keys schema)))

(def Key schema/Int)
(def Id {:id Key})

(def IdAndWarnings (assoc Id :warnings [{:property schema/Str
                                         :value    schema/Num
                                         :min      schema/Num
                                         :max      schema/Num}]))

(defn StringBase [max]
  (schema/constrained schema/Str #(<= 1 (count %) max) (str "[1, " max "]")))

(def String8 (StringBase 8))
(def String12 (StringBase 12))
(def String30 (StringBase 30))
(def String50 (StringBase 75))
(def String60 (StringBase 60))
(def String75 (StringBase 75))
(def String100 (StringBase 100))
(def String150 (StringBase 150))
(def String200 (StringBase 200))
(def String500 (StringBase 500))
(def String1000 (StringBase 1000))
(def String1500 (StringBase 1500))
(def String2500 (StringBase 2500))
(def String6300 (StringBase 6300))

(def Year
  (schema/constrained schema/Int #(<= 0 % 9999) "Year"))

(defn- LimitedNum
  ([number-type mininclusive maxinclusive name]
   (schema/constrained number-type
                       #(<= mininclusive % maxinclusive) name)))

(def Num1
  (LimitedNum schema/Num 0 1 "[0, 1]"))

(def NonNegative
  (LimitedNum schema/Num 0 9999999999 "[0, max]"))

(def IntNonNegative
  (LimitedNum schema/Int 0 9999999999 "[0, max]"))

(defn LimitedInt [mininclusive maxinclusive]
  (LimitedNum schema/Int mininclusive maxinclusive
              (str "[" mininclusive ", " maxinclusive "]")))

(def Luokittelu (merge Id {:label-fi schema/Str
                           :label-sv schema/Str
                           :valid    schema/Bool}))

(def Date java.time.LocalDate)
(def Instant java.time.Instant)

(defn henkilotunnus-checksum [s]
  (try
    (->> (mod (. Integer parseInt s) 31)
         (nth [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9
               \A \B \C \D \E \F \H \J \K \L
               \M \N \P \R \S \T \U \V \W \X \Y]))
    (catch NumberFormatException _ false)))

(defn valid-henkilotunnus? [s]
  (try
    (let [date-part (subs s 0 6)
          century-sign (nth s 6)
          individual-number (subs s 7 10)
          checksum (last s)]
      (and (= 11 (count s))
           (contains? #{\+ \- \A} century-sign)
           (= checksum (henkilotunnus-checksum (str date-part individual-number)))))
    (catch StringIndexOutOfBoundsException _ false)))

(def Henkilotunnus (schema/constrained schema/Str valid-henkilotunnus?
                                       "henkilotunnus"))

(defn ytunnus-checksum [ytunnus]
  (let [digits (map #(-> % str Integer/parseInt) (subs ytunnus 0 7))
        sum (reduce + (map * digits [7 9 10 5 8 4 2]))
        remainder (rem sum 11)]
    (if (= remainder 0) 0 (- 11 remainder))))

(defn valid-ytunnus? [ytunnus]
  (let [checksum (ytunnus-checksum ytunnus)]
    (and (= 9 (count ytunnus))
         (not= 10 checksum)
         (= \- (get ytunnus 7))
         (= checksum (Integer/parseInt (str (get ytunnus 8)))))))

(def Ytunnus (schema/constrained schema/Str valid-ytunnus?
                                 "y-tunnus"))

(def ConstraintError
  {:type       schema/Keyword
   :constraint schema/Keyword})

(def GeneralError
  {:type    schema/Keyword
   :message schema/Str})

(defn valid-ovt-tunnus? [s]
  (if (re-find #"^0037\d{8,13}$" s)
    (let [ytunnus (str (subs s 4 11) "-" (subs s 11 12))]
      (valid-ytunnus? ytunnus))
    false))

(def OVTtunnus (schema/constrained schema/Str valid-ovt-tunnus?
                                   "ovt-tunnus"))

(def iban-char-map (zipmap (map char (range (int \a) (inc (int \z))))
                           (range 10 36)))

(defn valid-iban? [s]
  (try
    (let [country (subs s 0 2)
          checksum (subs s 2 4)
          bban (subs s 4)]
      (= (mod (->> (str bban country checksum)
                   str/lower-case
                   (map #(or (get iban-char-map %) %))
                   (apply str)
                   bigint)
              97)
         1))
    (catch Exception e
      false)))

(def IBAN (schema/constrained schema/Str valid-iban? "iban"))

(defn valid-te-ovt-tunnus? [s]
  (and (-> s str/lower-case (str/starts-with? "te"))
       (-> s (subs 2) valid-ovt-tunnus?)))

(def TEOVTtunnus (schema/constrained schema/Str
                                     valid-te-ovt-tunnus?
                                     "te-ovt-tunnus"))

(defn valid-verkkolaskuosoite? [s]
  (or (valid-ovt-tunnus? s)
      (valid-iban? s)
      (valid-te-ovt-tunnus? s)))

(def Verkkolaskuosoite (schema/constrained schema/Str
                                           valid-verkkolaskuosoite?
                                           "verkkolaskuosoite"))

(defn valid-url? [s]
  (re-matches #"^https?:\/\/(www\.)?[-\p{L}0-9@:%._\+~#=]{1,256}\.[-\p{L}0-9()]{1,6}\b([-\p{L}0-9()@:%_\+.~#?&//=]*$)"
              s))

(def Url (schema/constrained schema/Str valid-url?))

(def Kayttaja
  "Any kayttaja"
  {:id       Key
   :rooli-id Key
   :sukunimi schema/Str
   :etunimi  schema/Str})

(defn valid-rakennustunnus? [s]
  (try
    (let [number-part (subs s 0 9)
          checksum (last s)]
      (and (= 10 (count s))
           (= checksum (henkilotunnus-checksum number-part))))
    (catch StringIndexOutOfBoundsException _ false)))

(def Rakennustunnus
  (schema/constrained schema/Str valid-rakennustunnus?
                      "rakennustunnus"))

(defn QueryWindow [max-limit]
  {(schema/optional-key :limit)  (LimitedInt 1 max-limit)
   (schema/optional-key :offset) schema/Int})


(defn valid-email? [s]
  (re-matches #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
              s))

(def Email (schema/constrained schema/Str valid-email?))

(defn with-maybe-vals [schema]
  (walk/prewalk (fn [x]
                  (if (and (map-entry? x) (-> x second xschema/maybe? not))
                    (clojure.lang.MapEntry. (first x) (schema/maybe (second x)))
                    x))
                schema))

(def Language (schema/enum "fi" "sv"))

(schema/defschema AcceptLanguage
  "Define a schema for Accept-Language header. The header can contain multiple locales with preferred weights, for example Accept-Language: fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5"
  (schema-tools/schema
    ; Matches the docstring above. Case invariant (?i) list of languages (like fr or *), with optional region (like -CH) and optional weight (;q=0.9) separated by optional comma and optional whitespace.
    (schema/constrained schema/Str #(re-matches #"(?i)(([*]|([a-z]{2,3}(-[a-z]{2,3})?))(;q=\d.\d)?,?\s?)+" %))
    {:openapi/description "Standardin mukainen Accept-Language otsikko. Jos otsikkoa ei ole asetettu, palautetaan ensisijaisesti suomenkielinen todistus"
     :openapi/example     "Accept-Language: fi-FI,sv;q=0.5"}))

(schema/defschema EnergiatodistusId (schema-tools/schema Key {:openapi/description "Energiatodistuksen tunniste"}))
