(ns solita.etp.schema.energiatodistus
  (:require [solita.common.map :as m]
            [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [solita.common.schema :as xschema]
            [solita.etp.schema.geo :as geo-schema]
            [clojure.string :as str]
            [solita.etp.exception :as exception]
            [solita.common.map :as map])
  (:import (schema.core Predicate EnumSchema Constrained)))

(defn optional-properties [schema]
  (if (xschema/schema-record? schema)
    (cond
      (xschema/maybe? schema) schema
      (instance? Constrained schema)
        (let [constrained-schema (optional-properties (:schema schema))]
          (if (xschema/maybe? constrained-schema)
            (schema/maybe schema)
            (assoc schema :schema constrained-schema)))
      (instance? EnumSchema schema) (schema/maybe schema)
      (instance? Predicate schema) (schema/maybe schema)
      :else (exception/illegal-argument! (str "Unsupported schema record: " schema)))
    (cond
      (= schema schema/Bool) schema
      (class? schema) (schema/maybe schema)
      (map? schema) (m/map-values optional-properties schema)
      (vector? schema) (mapv optional-properties schema)
      (coll? schema) (map optional-properties schema)
      :else (exception/illegal-argument! (str "Unsupported schema: " schema)))))

(def YritysPostinumero common-schema/String8)

(def Yritys
  {:nimi             common-schema/String150
   :katuosoite       common-schema/String50
   :postinumero      YritysPostinumero
   :postitoimipaikka common-schema/String30})

(def Perustiedot
  {:katuosoite-fi            common-schema/String50
   :katuosoite-sv            common-schema/String50
   :valmistumisvuosi         common-schema/Year
   :julkinen-rakennus        schema/Bool
   :havainnointikaynti       common-schema/Date
   :rakennustunnus           common-schema/Rakennustunnus
   :postinumero              geo-schema/PostinumeroFI
   :keskeiset-suositukset-fi common-schema/String2500
   :keskeiset-suositukset-sv common-schema/String2500
   :laatimisvaihe            common-schema/Key
   :kiinteistotunnus         common-schema/String50
   :yritys                   Yritys
   :tilaaja                  common-schema/String200
   :rakennusosa              common-schema/String100
   :kieli                    common-schema/Key
   :nimi-fi                  common-schema/String50
   :nimi-sv                  common-schema/String50

   ;; This is alakäyttötarkoitusluokka
   ;; Käyttötarkoitusluokka can be inferred from this
   :kayttotarkoitus          schema/Str})

(def Rakennusvaippa
  {:ala common-schema/NonNegative
   :U   common-schema/NonNegative})

(def LahtotiedotRakennusvaippa
  {:ilmanvuotoluku    common-schema/NonNegative
   :lampokapasiteetti common-schema/NonNegative
   :ilmatilavuus      common-schema/NonNegative

   :ulkoseinat        Rakennusvaippa
   :ylapohja          Rakennusvaippa
   :alapohja          Rakennusvaippa
   :ikkunat           Rakennusvaippa
   :ulkoovet          Rakennusvaippa
   :kylmasillat-UA    common-schema/NonNegative})

(def LahtotiedotIkkuna
  {:ala  common-schema/NonNegative
   :U    common-schema/NonNegative
   :g-ks common-schema/NonNegative})

(def LahtotiedotIkkunat
  {:pohjoinen LahtotiedotIkkuna
   :koillinen LahtotiedotIkkuna
   :ita       LahtotiedotIkkuna
   :kaakko    LahtotiedotIkkuna
   :etela     LahtotiedotIkkuna
   :lounas    LahtotiedotIkkuna
   :lansi     LahtotiedotIkkuna
   :luode     LahtotiedotIkkuna
   :valokupu  LahtotiedotIkkuna
   :katto     LahtotiedotIkkuna})

(def PoistoTuloSfp
  {:poisto common-schema/NonNegative
   :tulo   common-schema/NonNegative
   :sfp    common-schema/NonNegative})

(def LahtotiedotIlmanvaihto
  {:erillispoistot      PoistoTuloSfp
   :ivjarjestelma       PoistoTuloSfp
   :tyyppi-id           common-schema/Key
   :kuvaus-fi           common-schema/String75
   :kuvaus-sv           common-schema/String75
   :lto-vuosihyotysuhde common-schema/Num1
   :tuloilma-lampotila  common-schema/NonNegative
   :paaiv               (merge PoistoTuloSfp
                               {:lampotilasuhde common-schema/Num1
                                :jaatymisenesto schema/Num})})

(def Hyotysuhde
  {:tuoton-hyotysuhde             common-schema/NonNegative,
   :jaon-hyotysuhde               common-schema/NonNegative,
   :lampokerroin                  common-schema/NonNegative,
   :apulaitteet                   common-schema/NonNegative,
   :lampohavio-lammittamaton-tila common-schema/NonNegative,
   :lampopumppu-tuotto-osuus      common-schema/Num1})

(def MaaraTuotto
  {:maara  common-schema/IntNonNegative,
   :tuotto common-schema/NonNegative})

(def FormalDescription
  {:id        common-schema/Key
   :kuvaus-fi common-schema/String75
   :kuvaus-sv common-schema/String75})

(def LahtotiedotLammitys
  {:lammitysmuoto-1   FormalDescription
   :lammitysmuoto-2   FormalDescription
   :lammonjako        FormalDescription
   :tilat-ja-iv       Hyotysuhde
   :lammin-kayttovesi Hyotysuhde
   :takka             MaaraTuotto
   :ilmalampopumppu   MaaraTuotto})

(def SisKuorma
  {:kayttoaste  common-schema/Num1
   :lampokuorma common-schema/NonNegative})

(def SisKuormat
  {:henkilot          SisKuorma
   :kuluttajalaitteet SisKuorma
   :valaistus         SisKuorma})

(def Lahtotiedot
  {:lammitetty-nettoala  common-schema/NonNegative
   :rakennusvaippa       LahtotiedotRakennusvaippa
   :ikkunat              LahtotiedotIkkunat
   :ilmanvaihto          LahtotiedotIlmanvaihto
   :lammitys             LahtotiedotLammitys
   :jaahdytysjarjestelma {:jaahdytyskauden-painotettu-kylmakerroin
                          common-schema/NonNegative}
   :lkvn-kaytto          {:ominaiskulutus              common-schema/NonNegative
                          :lammitysenergian-nettotarve common-schema/NonNegative}
   :sis-kuorma           SisKuormat})

(def UusiutuvatOmavaraisenergiat
  {:aurinkosahko common-schema/NonNegative
   :tuulisahko   common-schema/NonNegative
   :aurinkolampo common-schema/NonNegative
   :muulampo     common-schema/NonNegative
   :muusahko     common-schema/NonNegative
   :lampopumppu  common-schema/NonNegative})

(def SahkoLampo
  {:sahko common-schema/NonNegative
   :lampo common-schema/NonNegative})

(def Kuukausierittely (schema/maybe
                       {:tuotto (optional-properties UusiutuvatOmavaraisenergiat)
                        :kulutus (optional-properties SahkoLampo)}))

(def OptionalKuukausierittely (schema/constrained [Kuukausierittely]
                                                  #(contains? #{0 12} (count %))
                                                  "Kuukausierittelyt"))

(def Tulokset
  {:kaytettavat-energiamuodot
   {:fossiilinen-polttoaine common-schema/NonNegative
    :sahko                  common-schema/NonNegative
    :kaukojaahdytys         common-schema/NonNegative
    :kaukolampo             common-schema/NonNegative
    :uusiutuva-polttoaine   common-schema/NonNegative},

   :uusiutuvat-omavaraisenergiat
   UusiutuvatOmavaraisenergiat,

   :kuukausierittely OptionalKuukausierittely

   :tekniset-jarjestelmat
   {:tilojen-lammitys                     SahkoLampo,
    :tuloilman-lammitys                   SahkoLampo,
    :kayttoveden-valmistus                SahkoLampo
    :iv-sahko                             common-schema/NonNegative
    :jaahdytys                            (assoc SahkoLampo :kaukojaahdytys common-schema/NonNegative)
    :kuluttajalaitteet-ja-valaistus-sahko common-schema/NonNegative},

   :nettotarve
   {:tilojen-lammitys-vuosikulutus      common-schema/NonNegative
    :ilmanvaihdon-lammitys-vuosikulutus common-schema/NonNegative
    :kayttoveden-valmistus-vuosikulutus common-schema/NonNegative
    :jaahdytys-vuosikulutus             common-schema/NonNegative},

   :lampokuormat
   {:aurinko           common-schema/NonNegative
    :ihmiset           common-schema/NonNegative
    :kuluttajalaitteet common-schema/NonNegative
    :valaistus         common-schema/NonNegative,
    :kvesi             common-schema/NonNegative},

   :laskentatyokalu common-schema/String60})

(def ToteutunutOstoenergiankulutus
  {:ostettu-energia
   {:kaukolampo-vuosikulutus      common-schema/NonNegative,
    :kokonaissahko-vuosikulutus   common-schema/NonNegative,
    :kiinteistosahko-vuosikulutus common-schema/NonNegative,
    :kayttajasahko-vuosikulutus   common-schema/NonNegative,
    :kaukojaahdytys-vuosikulutus  common-schema/NonNegative},

   :ostetut-polttoaineet
     {:kevyt-polttooljy      common-schema/NonNegative,
      :pilkkeet-havu-sekapuu common-schema/NonNegative,
      :pilkkeet-koivu        common-schema/NonNegative,
      :puupelletit           common-schema/NonNegative,
      :muu
       [{:nimi           common-schema/String30,
         :yksikko        common-schema/String12,
         :muunnoskerroin common-schema/NonNegative,
         :maara-vuodessa common-schema/NonNegative}]},

   :sahko-vuosikulutus-yhteensa          common-schema/NonNegative,
   :kaukolampo-vuosikulutus-yhteensa     common-schema/NonNegative,
   :polttoaineet-vuosikulutus-yhteensa   common-schema/NonNegative,
   :kaukojaahdytys-vuosikulutus-yhteensa common-schema/NonNegative})

(def Huomio
  {:teksti-fi  common-schema/String1000
   :teksti-sv  common-schema/String1000,
   :toimenpide [{:nimi-fi       common-schema/String100
                 :nimi-sv       common-schema/String100
                 :lampo         schema/Num
                 :sahko         schema/Num
                 :jaahdytys     schema/Num
                 :eluvun-muutos schema/Num}]})

(def Huomiot
  {:suositukset-fi    common-schema/String1500
   :suositukset-sv    common-schema/String1500
   :lisatietoja-fi    common-schema/String500
   :lisatietoja-sv    common-schema/String500
   :iv-ilmastointi    Huomio
   :valaistus-muut    Huomio
   :lammitys          Huomio
   :ymparys           Huomio
   :alapohja-ylapohja Huomio})

(def Laskutus
  "Laskutusosoite can be defined either using:
  - laskutusosoite-id - both laatija and yrityslaskutus or
  - laskutettava-yritys-id - only for yrityslaskutus.
  If both are defined then laskutusosoite-id is decisive.
  Laskutettava-yritys-id is for backwards compatibility."
  {:laskutettava-yritys-id common-schema/Key
   :laskutusosoite-id      common-schema/Key
   :laskuriviviite         common-schema/String6300})

(def EnergiatodistusSave2018
  "This schema is used in
  add-energiatodistus and update-energiatodistus
  services for 2018 version"
  (optional-properties
    (merge
      Laskutus
      {:korvattu-energiatodistus-id     common-schema/Key
       :kommentti                       common-schema/String6300
       :draft-visible-to-paakayttaja    schema/Bool
       :bypass-validation-limits        schema/Bool
       :bypass-validation-limits-reason schema/Str
       :perustiedot                     Perustiedot
       :lahtotiedot                     Lahtotiedot
       :tulokset                        Tulokset
       :toteutunut-ostoenergiankulutus  ToteutunutOstoenergiankulutus
       :huomiot                         Huomiot
       :lisamerkintoja-fi               common-schema/String6300
       :lisamerkintoja-sv               common-schema/String6300})))

(defn dissoc-not-in-2013 [schema2018]
  (-> schema2018
      (map/dissoc-in [:perustiedot :laatimisvaihe])))

(def UserDefinedEnergiamuoto
  {:nimi         common-schema/String50
   :muotokerroin common-schema/NonNegative
   :ostoenergia  common-schema/NonNegative})

(def UserDefinedEnergia
  {:nimi-fi      common-schema/String50
   :nimi-sv      common-schema/String50
   :vuosikulutus common-schema/NonNegative})

(def EnergiatodistusSave2013
  "This schema is used in
  add-energiatodistus and update-energiatodistus
  services for 2013 version"
  (-> (dissoc-not-in-2013 EnergiatodistusSave2018)
      (assoc-in [:perustiedot :uudisrakennus] schema/Bool)
      (assoc-in [:tulokset :kaytettavat-energiamuodot :muu]
                [(optional-properties UserDefinedEnergiamuoto)])
      (assoc-in [:tulokset :uusiutuvat-omavaraisenergiat]
                [(optional-properties UserDefinedEnergia)])
      (assoc-in [:toteutunut-ostoenergiankulutus :ostettu-energia :muu]
                [(optional-properties UserDefinedEnergia)])))

(defn ->EnergiatodistusSaveExternal [schema]
  (-> schema
      (dissoc :kommentti
              :draft-visible-to-paakayttaja
              :bypass-validation-limits
              :bypass-validation-limits-reason)
      (assoc-in [:perustiedot :nimi] (schema/maybe common-schema/String50))
      xschema/optional-key-for-maybe))

(def EnergiatodistusSave2013External
  (->EnergiatodistusSaveExternal EnergiatodistusSave2013))
(def EnergiatodistusSave2018External
  (->EnergiatodistusSaveExternal EnergiatodistusSave2018))

(def Energiatehokkuus
  {:e-luku (schema/maybe schema/Num)
   :e-luokka (schema/maybe schema/Str)})

(def Status
  {:tila-id common-schema/Key
   :allekirjoitusaika (schema/maybe common-schema/Instant)
   :voimassaolo-paattymisaika (schema/maybe common-schema/Instant)
   :laskutusaika (schema/maybe common-schema/Instant)
   :korvaava-energiatodistus-id (schema/maybe common-schema/Key)})

(def Laatija
  {:laatija-id common-schema/Key
   :laatija-fullname schema/Str})

(defn energiatodistus-versio [versio save-schema]
  "Energiatodistus schema contains basic information about persistent energiatodistus"
  (-> save-schema
      (merge common-schema/Id
             {:versio (schema/eq versio)}
             Status
             Laatija)
      (update :tulokset (partial merge Energiatehokkuus))))

(def Energiatodistus2018
  "Energiatodistus 2018"
  (energiatodistus-versio 2018 EnergiatodistusSave2018))

(def Energiatodistus2013
  "Energiatodistus 2013"
  (energiatodistus-versio 2013 EnergiatodistusSave2013))

(defn versio? [versio et] (-> et :versio (= versio)))

(def Energiatodistus
  (schema/conditional
    (partial versio? 2018) Energiatodistus2018
    (partial versio? 2013) Energiatodistus2013))

(defn assoc-energiatodistus
  "Extend energiatodistus schema by associating new key value pairs."
  [& kvs]
  (schema/->ConditionalSchema
    (map #(update % 1 (fn [schema] (apply assoc schema kvs)))
         (:preds-and-schemas Energiatodistus))
    (:error-symbol Energiatodistus)))

(def Signature {:signature schema/Str :chain [schema/Str]})

(def Range
  {:min schema/Num
   :max schema/Num})

(def NumericValidation
  {:property schema/Str,
   :warning Range,
   :error Range})

(def EnergiatodistusSearch
  {(schema/optional-key :sort)    schema/Str
   (schema/optional-key :order)   (schema/enum "asc" "desc")
   (schema/optional-key :limit)   (common-schema/LimitedInt 1 100)
   (schema/optional-key :offset)  schema/Int
   (schema/optional-key :where)   schema/Str
   (schema/optional-key :keyword) schema/Str})

(def EnergiatodistusForAnyLaatija
  (assoc
    (merge common-schema/Id Status Laatija)
    :versio schema/Int
    :perustiedot
    (optional-properties
      (select-keys Perustiedot
                   [:rakennustunnus
                    :kayttotarkoitus
                    :nimi-fi :nimi-sv
                    :katuosoite-fi :katuosoite-sv
                    :postinumero]))))
