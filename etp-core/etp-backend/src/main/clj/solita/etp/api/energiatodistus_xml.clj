(ns solita.etp.api.energiatodistus-xml
  (:require [clojure.string :as str]
            [ring.util.response :as r]
            [clojure.tools.logging :as log]
            [schema.core :as schema]
            [schema-tools.coerce :as sc]
            [solita.common.maybe :as maybe]
            [solita.common.xml :as xml]
            [solita.etp.api.response :as response]
            [solita.common.map :as xmap]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as energiatodistus-service])
  (:import (clojure.lang ExceptionInfo)
           (javax.xml.validation Schema)))

(def coercer (sc/coercer energiatodistus-schema/EnergiatodistusSave2018External
                         sc/string-coercion-matcher))

(def xsd-path "legacy-api/energiatodistus-2018.xsd")
(def ^Schema xsd-schema (xml/load-schema xsd-path true))

(def types-ns "http://www.energiatodistusrekisteri.fi/ws/energiatodistustypes-2018")
(def xsi-url "http://www.w3.org/2001/XMLSchema-instance")

;; TODO should we host the schema ourselves?
(def schema-location "http://www.energiatodistusrekisteri.fi/ws/energiatodistustypes https://beta.energiatodistusrekisteri.fi/ws/etpservice?xsd=1")

(defn map-values-from-xml [xml m]
  (xmap/map-values #(xml/get-content xml [%]) m))

(defn schema->identity-map [schema]
  (->> schema
       keys
       (reduce (fn [acc k]
                 (assoc acc k k))
               {})))

(defn yritys [xml]
  (map-values-from-xml xml (schema->identity-map energiatodistus-schema/Yritys)))

(defn perustiedot [xml]
  (-> xml
      (map-values-from-xml (-> energiatodistus-schema/Perustiedot
                               schema->identity-map
                               (assoc :nimi :nimi)))
      (assoc :rakennustunnus (maybe/map* str/upper-case (xml/get-content xml [:rakennustunnus])))
      (assoc :julkinen-rakennus (xml/get-content xml [:onko-julkinen-rakennus]))
      (assoc :yritys (yritys (xml/get-in-xml xml [:yritys])))))

(defn rakennusvaippa [xml]
  {:ala (xml/get-content xml [:rv-a])
   :U   (xml/get-content xml [:rv-u])})

(defn lahtotiedot-rakennusvaippa [xml]
  (let [f #(rakennusvaippa (xml/get-in-xml xml [%]))]
    {:ilmanvuotoluku    (xml/get-content xml [:ilmanvuotoluku])
     :lampokapasiteetti nil
     :ilmatilavuus      nil
     :ulkoseinat        (f :rv-ulkoseinat)
     :ylapohja          (f :rv-ylapohja)
     :alapohja          (f :rv-alapohja)
     :ikkunat           (f :rv-ikkunat)
     :ulkoovet          (f :rv-ulkoovet)
     :kylmasillat-UA    (xml/get-content xml [:rv-kylmasillat-ua])}))

(defn ikkuna [xml]
  (let [f #(xml/get-content xml [%])]
    {:ala  (f :ikk-a)
     :U    (f :ikk-u)
     :g-ks (f :ikk-g)}))

(defn lahtotiedot-ikkunat [xml]
  (->> (schema->identity-map energiatodistus-schema/LahtotiedotIkkunat)
       (xmap/map-values #(ikkuna (xml/get-in-xml xml [% 0])))))

(defn lahtotiedot-ilmanvaihto [xml]
  (let [f #(xml/get-content xml [%])]
    {:erillispoistot      {:poisto (f :iv-erillispoistot-poisto)
                           :tulo   (f :iv-erillispoistot-tulo)
                           :sfp    (f :iv-erillispoistot-sfp)}
     :ivjarjestelma       {:poisto (f :iv-ivjarjestelma-poisto)
                           :tulo   (f :iv-ivjarjestelma-tulo)
                           :sfp    (f :iv-ivjarjestelma-sfp)}
     :tyyppi-id           6 ;; Muu, mikä
     :kuvaus-fi           (f :iv-kuvaus-fi)
     :kuvaus-sv           (f :iv-kuvaus-sv)
     :lto-vuosihyotysuhde (f :iv-lto-vuosihyotysuhde)
     :tuloilma-lampotila  nil
     :paaiv               {:poisto         (f :iv-paaiv-poisto)
                           :tulo           (f :iv-paaiv-tulo)
                           :sfp            (f :iv-paaiv-sfp)
                           :lampotilasuhde (f :iv-paaiv-lampotilasuhde)
                           :jaatymisenesto (f :iv-paaiv-jaatymisenesto)}}))

(defn hyotysuhde [xml]
  (map-values-from-xml xml (schema->identity-map energiatodistus-schema/Hyotysuhde)))

(defn maara-tuotto [xml]
  (map-values-from-xml xml (schema->identity-map energiatodistus-schema/MaaraTuotto)))

(defn lahtotiedot-lammitys [xml]
  {:lammitysmuoto-1   {:id        9 ;; Muu, mikä
                       :kuvaus-fi (xml/get-content xml [:lammitys-kuvaus-fi])
                       :kuvaus-sv (xml/get-content xml [:lammitys-kuvaus-sv])}
   :lammitysmuoto-2   {:id        nil
                       :kuvaus-fi nil
                       :kuvaus-sv nil}
   :lammonjako        {:id        nil
                       :kuvaus-fi nil
                       :kuvaus-sv nil}
   :tilat-ja-iv       (hyotysuhde (xml/get-in-xml xml [:lammitys-tilat-ja-iv]))
   :lammin-kayttovesi (hyotysuhde (xml/get-in-xml xml [:lammitys-lammin-kayttovesi]))
   :takka             (maara-tuotto (xml/get-in-xml xml [:lammitys-takka]))
   :ilmalampopumppu   (maara-tuotto (xml/get-in-xml xml [:lammitys-ilmanlampopumppu]))})

(defn sis-kuorma [xml]
  (reduce (fn [acc k]
            (let [xml-for-k (->> xml
                                 (filter #(xml/get-content % [k]))
                                 first)]
              (assoc acc k {:kayttoaste  (xml/get-content xml-for-k [:kayttoaste])
                            :lampokuorma (xml/get-content xml-for-k [k])})))
          {}
          [:henkilot :kuluttajalaitteet :valaistus]))

(defn lahtotiedot [xml]
  {:lammitetty-nettoala  (xml/get-content xml [:lammitetty-nettoala])
   :rakennusvaippa       (lahtotiedot-rakennusvaippa (xml/get-in-xml xml [:lahtotiedot-rakennusvaippa]))
   :ikkunat              (lahtotiedot-ikkunat (xml/get-in-xml xml [:lahtotiedot-ikkunat]))
   :ilmanvaihto          (lahtotiedot-ilmanvaihto (xml/get-in-xml xml [:lahtotiedot-ilmanvaihto]))
   :lammitys             (lahtotiedot-lammitys (xml/get-in-xml xml [:lahtotiedot-lammitys]))
   :jaahdytysjarjestelma {:jaahdytyskauden-painotettu-kylmakerroin
                          (xml/get-content xml [:lahtotiedot-jaahdytysjarjestelma
                                                :jaahdytysjarjestelma-jaahdytyskauden-painotettu-kylmakerroin])}
   :lkvn-kaytto          {:ominaiskulutus              (xml/get-content xml [:lahtotiedot-lkvn-kaytto
                                                                             :lkvn-kaytto-kulutus-per-nelio])
                          :lammitysenergian-nettotarve (xml/get-content xml [:lahtotiedot-lkvn-kaytto
                                                                             :lkvn-kaytto-vuosikulutus])}
   :sis-kuorma           (sis-kuorma (xml/get-in-xml xml [:lahtotiedot-sis-kuormat :sis-kuorma]))})

(defn find-by-vakio [xml vakio-k vakio-v]
  (->> xml
       (filter #(= vakio-v (str/lower-case (xml/get-content % [vakio-k]))))
       first))

(defn kaytettavat-energiamuodot [xml energiamuotovakio]
  (xml/get-content (find-by-vakio xml :energiamuoto-vakio energiamuotovakio) [:laskettu-ostoenergia]))

(defn uusiutuvat-omavaraisenergiat [xml nimivakio]
  (xml/get-content (find-by-vakio xml :nimi-vakio nimivakio) [:vuosikulutus]))

(defn sahko-lampo [xml]
  (map-values-from-xml xml (schema->identity-map energiatodistus-schema/SahkoLampo)))

(defn tulokset [xml]
  (let [kaytettavat-energiamuodot-xml    (xml/get-in-xml xml [:tulokset-kaytettavat-energiamuodot
                                                              :kaytettava-energiamuoto])
        uusiutuvat-omavaraisenergiat-xml (xml/get-in-xml xml [:tulokset-uusiutuvat-omavaraisenergiat
                                                              :uusiutuva-omavaraisenergia])
        tekniset-jarjestelmat-xml        (xml/get-in-xml xml [:tulokset-tekniset-jarjestelmat])
        nettotarve-xml                   (xml/get-in-xml xml [:tulokset-nettotarve])
        lampokuormat-xml                 (xml/get-in-xml xml [:tulokset-lampokuormat])]
    {:kaytettavat-energiamuodot
     {:fossiilinen-polttoaine (kaytettavat-energiamuodot kaytettavat-energiamuodot-xml "fossiilinen polttoaine")
      :sahko                  (kaytettavat-energiamuodot kaytettavat-energiamuodot-xml "sähkö")
      :kaukojaahdytys         (kaytettavat-energiamuodot kaytettavat-energiamuodot-xml "kaukojäähdytys")
      :kaukolampo             (kaytettavat-energiamuodot kaytettavat-energiamuodot-xml "kaukolämpö")
      :uusiutuva-polttoaine   (kaytettavat-energiamuodot kaytettavat-energiamuodot-xml "uusiutuva polttoaine")}

     :uusiutuvat-omavaraisenergiat
     {:aurinkosahko (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "aurinkosahko")
      :tuulisahko   (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "tuulisahko")
      :aurinkolampo (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "aurinkolampo")
      :muulampo     (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "muulampo")
      :muusahko     (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "muusahko")
      :lampopumppu  (uusiutuvat-omavaraisenergiat uusiutuvat-omavaraisenergiat-xml "lampopumppu")}

     :kuukausierittely []

     :tekniset-jarjestelmat
     {:tilojen-lammitys                     (sahko-lampo (xml/get-in-xml tekniset-jarjestelmat-xml
                                                                         [:tj-tilojen-lammitys]))
      :tuloilman-lammitys                   (sahko-lampo (xml/get-in-xml tekniset-jarjestelmat-xml
                                                                         [:tj-tuloilman-lammitys]))
      :kayttoveden-valmistus                (sahko-lampo (xml/get-in-xml tekniset-jarjestelmat-xml
                                                                         [:tj-kayttoveden-valmistus]))
      :iv-sahko                             (xml/get-content tekniset-jarjestelmat-xml [:tj-iv-sahko])
      :jaahdytys                            (assoc (sahko-lampo (xml/get-in-xml tekniset-jarjestelmat-xml
                                                                                [:tj-jaahdytys]))
                                                   :kaukojaahdytys
                                                   (xml/get-content tekniset-jarjestelmat-xml
                                                                    [:tj-jaahdytys
                                                                     :kaukojaahdytys]))
      :kuluttajalaitteet-ja-valaistus-sahko (xml/get-content tekniset-jarjestelmat-xml
                                                             [:tj-kuluttajalaitteet-ja-valaistus-sahko])}

     :nettotarve
     {:tilojen-lammitys-vuosikulutus      (xml/get-content nettotarve-xml [:nt-tilojen-lammitys-vuosikulutus])
      :ilmanvaihdon-lammitys-vuosikulutus (xml/get-content nettotarve-xml [:nt-ilmanvaihdon-lammitys-vuosikulutus])
      :kayttoveden-valmistus-vuosikulutus (xml/get-content nettotarve-xml [:nt-kayttoveden-valmistus-vuosikulutus])
      :jaahdytys-vuosikulutus             (xml/get-content nettotarve-xml [:nt-jaahdytys-vuosikulutus])}

     :lampokuormat
     {:aurinko           (xml/get-content lampokuormat-xml [:lk-aurinko])
      :ihmiset           (xml/get-content lampokuormat-xml [:lk-ihmiset])
      :kuluttajalaitteet (xml/get-content lampokuormat-xml [:lk-kuluttajalaitteet])
      :valaistus         (xml/get-content lampokuormat-xml [:lk-valaistus])
      :kvesi             (xml/get-content lampokuormat-xml [:lk-kvesi])}

     :laskentatyokalu (xml/get-content xml [:tulokset-laskentatyokalu])}))

(defn muut-ostetut-polttoaineet [xml]
  (map (fn [op-vapaa-xml]
         (let [f #(xml/get-content op-vapaa-xml [%])]
           {:nimi           (f :op-nimi)
            :yksikko        (f :op-yksikko)
            :muunnoskerroin (f :op-muunnoskerroin)
            :maara-vuodessa (f :op-maara-vuodessa)}))
       xml))

(defn toteutunut-ostoenergiankulutus [xml]
  (let [ostettu-energia      #(xml/get-content xml [:ostettu-energia %])
        ostetut-polttoaineet #(xml/get-content xml [:ostetut-polttoaineet %])]
    {:ostettu-energia                      {:kaukolampo-vuosikulutus      (ostettu-energia :to-kaukolampo-vuosikulutus)
                                            :kokonaissahko-vuosikulutus   (ostettu-energia :to-kokonaissahko-vuosikulutus)
                                            :kiinteistosahko-vuosikulutus (ostettu-energia :to-kiinteistosahko-vuosikulutus)
                                            :kayttajasahko-vuosikulutus   (ostettu-energia :to-kayttajasahko-vuosikulutus)
                                            :kaukojaahdytys-vuosikulutus  (ostettu-energia :to-kaukojaahdytys-vuosikulutus)}
     :ostetut-polttoaineet                 {:kevyt-polttooljy      (ostetut-polttoaineet :op-kevyt-polttooljy)
                                            :pilkkeet-havu-sekapuu (ostetut-polttoaineet :op-pilkkeet-havu-sekapuu)
                                            :pilkkeet-koivu        (ostetut-polttoaineet :op-pilkkeet-koivu)
                                            :puupelletit           (ostetut-polttoaineet :op-puupelletit)
                                            :muu                   (muut-ostetut-polttoaineet
                                                                    (xml/get-in-xml xml [:ostetut-polttoaineet
                                                                                         :op-vapaa]))}
     :sahko-vuosikulutus-yhteensa          (xml/get-content xml [:to-sahko-vuosikulutus-yhteensa])
     :kaukolampo-vuosikulutus-yhteensa     (xml/get-content xml [:to-kaukolampo-vuosikulutus-yhteensa])
     :polttoaineet-vuosikulutus-yhteensa   (xml/get-content xml [:to-polttoaineet-vuosikulutus-yhteensa])
     :kaukojaahdytys-vuosikulutus-yhteensa (xml/get-content xml [:to-kaukojaahdytys-vuosikulutus-yhteensa])}))

(defn huomio [xml]
  {:teksti-fi  (xml/get-content xml [:huomiot-teksti-fi])
   :teksti-sv  (xml/get-content xml [:huomiot-teksti-sv])
   :toimenpide (map (fn [toimenpide-xml]
                      (let [f #(xml/get-content toimenpide-xml [%])]
                        {:nimi-fi       (f :nimi-fi)
                         :nimi-sv       (f :nimi-sv)
                         :lampo         (f :lampo)
                         :sahko         (f :sahko)
                         :jaahdytys     (f :jaahdytys)
                         :eluvun-muutos (f :eluvun-muutos)}))
                    (xml/get-in-xml xml [:toimenpide]))})

(defn huomiot [xml]
  (let [f #(xml/get-content xml [%])
        huomio #(huomio (xml/get-in-xml xml [%]))]
    {:suositukset-fi    (f :huomiot-suositukset-fi)
     :suositukset-sv    (f :huomiot-suositukset-sv)
     :lisatietoja-fi    (f :huomiot-lisatietoja-fi)
     :lisatietoja-sv    (f :huomiot-lisatietoja-sv)
     :iv-ilmastointi    (huomio :huomiot-iv-ilmastointi)
     :valaistus-muut    (huomio :huomiot-valaistus-muut)
     :lammitys          (huomio :huomiot-lammitys)
     :ymparys           (huomio :huomiot-ymparys)
     :alapohja-ylapohja (huomio :huomiot-alapohja-ylapohja)}))

(defn xml->energiatodistus [xml]
  (let [xml (xml/with-kebab-case-tags xml)
        f #(xml/get-in-xml xml [:energiatodistus %])]
    (-> {:korvattu-energiatodistus-id     nil
         :laskutettava-yritys-id          nil
         :laskutusosoite-id               nil
         :laskuriviviite                  nil
         :perustiedot                     (perustiedot (f :perustiedot))
         :lahtotiedot                     (lahtotiedot (f :lahtotiedot))
         :tulokset                        (tulokset (f :tulokset))
         :toteutunut-ostoenergiankulutus  (toteutunut-ostoenergiankulutus (f :toteutunut-ostoenergiankulutus))
         :huomiot                         (huomiot (f :huomiot))
         :lisamerkintoja-fi               (xml/get-content
                                           xml
                                           [:energiatodistus :lisamerkintoja-fi])
         :lisamerkintoja-sv               (xml/get-content
                                           xml
                                           [:energiatodistus :lisamerkintoja-sv])}
        coercer)))

(defn soap-body [content]
  (->
   (xml/element (xml/qname types-ns "EnergiatodistusVastaus")
                {(xml/qname xsi-url "schemaLocation") schema-location}
                content)
   xml/with-soap-envelope
   xml/emit-str))

(defn success-body [id warnings]
  (-> (map (fn [{:keys [property value]}]
             (xml/element (xml/qname types-ns "Huomautus")
                          {}
                          (str "Property "
                               property
                               " has an invalid value "
                               value)))
           warnings)
      (conj (xml/element (xml/qname types-ns "TodistusTunnus") {} id))
      soap-body))

(defn error-body [errors]
  (->> errors
       (map (fn [error]
              (xml/element (xml/qname types-ns "Virhe") {} error)))
       soap-body))

(def error-response {:foreign-key-violation response/bad-request
                     :invalid-replace response/bad-request
                     :invalid-sisainen-kuorma response/bad-request
                     :invalid-value response/bad-request
                     :xml-not-well-formed response/bad-request
                     :xml-xsd-validation-failed response/bad-request
                     :schema-tools.coerce/error response/bad-request})

(def error-label {:xml-xsd-validation-failed "XML XSD validation did not pass."
                  :xml-not-well-formed "XML is not well formed."})

(defn- error->xml-response [error]
  (let [{:keys [type]} (ex-data error)
        msg (.getMessage error)
        response-fn (get error-response type)
        label (get error-label type)]
    (if response-fn
      (do
        (log/warn "Failed to load ET in legacy API:" msg)
        (-> (filter not-empty [label msg])
            error-body
            response-fn))
      (throw error))))

(defn handle-post [versio]
  (fn [{:keys [db whoami body]}]
    (response/->xml-response
      (try
        (let [xml (-> body xml/input-stream->xml xml/without-soap-envelope)]
          (xml/validate-xsd! xml xsd-schema)
          (let [energiatodistus (xml->energiatodistus xml)
                {:keys [id warnings]} (energiatodistus-service/add-energiatodistus! db
                                                                                    whoami
                                                                                    versio
                                                                                    energiatodistus)]
            (r/response (success-body id warnings))))
        (catch ExceptionInfo e
          (error->xml-response e))))))

;; TODO 2013 versio
(defn post [versio]
  {:post {:summary    "Lisää luonnostilaisen energiatodistuksen"
          :parameters {:body schema/Any}
          :swagger    {:consumes ["application/xml"]
                       :produces ["application/xml"]}
          :handler    (handle-post versio)}})
