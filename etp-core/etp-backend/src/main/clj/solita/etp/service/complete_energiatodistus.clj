(ns solita.etp.service.complete-energiatodistus
  (:require [clojure.string :as str]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.geo :as geo]
            [solita.etp.service.laatimisvaihe :as laatimisvaihe]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.etp.service.kielisyys :as kielisyys]
            [solita.etp.service.luokittelu :as luokittelu]
            [solita.etp.service.polttoaine :as polttoaine]
            [solita.common.map :as map]
            [solita.common.formats :as formats]))

(defn safe-div [x y]
  (when (and x y (> y 0))
    (/ x y)))

(defn combine-keys [m f nil-replacement path-new & paths]
  (let [vals (map #(or (get-in m %) nil-replacement) paths)]
    (if (not-any? nil? vals)
      (assoc-in m path-new (apply f vals))
      m)))

(defn assoc-div-nettoala [energiatodistus path]
  (let [new-k (-> path last name (str "-nettoala") keyword)
        new-path (-> path pop (conj new-k))]
    (combine-keys energiatodistus
                  safe-div
                  nil
                  new-path
                  path
                  [:lahtotiedot :lammitetty-nettoala])))

(defn copy-field [m from-path to-path]
  (let [v (get-in m from-path)]
    (assoc-in m to-path v)))

(defn kuukausierittely-hyodynnetty [kuukausierittely]
  (mapv (fn [{:keys [tuotto kulutus] :as month}]
          (let [{:keys [aurinkosahko tuulisahko muusahko
                        aurinkolampo lampopumppu muulampo]}
                (map/map-values #(or % 0) tuotto)
               {:keys [sahko lampo]} (map/map-values #(or % 0) kulutus)]
            (-> month
                (assoc-in [:hyoty :sahko]
                          (min (+ aurinkosahko tuulisahko muusahko)
                               sahko))
                (assoc-in [:hyoty :lampo]
                          (min (+ aurinkolampo lampopumppu muulampo)
                               lampo)))))
        kuukausierittely))

(defn assoc-kuukausierittely-summat [energiatodistus]
  (assoc-in energiatodistus
            [:tulokset :kuukausierittely-summat]
            (reduce (fn [sums month]
                      (reduce (fn [sums k]
                                (update-in sums
                                           k
                                           (fnil + 0)
                                           (or (get-in month k) 0)))
                              sums
                              [[:tuotto :aurinkosahko]
                               [:tuotto :tuulisahko]
                               [:tuotto :muusahko]
                               [:tuotto :aurinkolampo]
                               [:tuotto :lampopumppu]
                               [:tuotto :muulampo]
                               [:kulutus :sahko]
                               [:kulutus :lampo]
                               [:hyoty :sahko]
                               [:hyoty :lampo]]))
                    {}
                    (-> energiatodistus :tulokset :kuukausierittely))))

(defn format-tulo-or-poisto [x]
  (formats/format-number x 3 false))

(defn assoc-tulo-poisto [energiatodistus path]
  (assoc-in energiatodistus
            (conj path :tulo-poisto)
            (str (format-tulo-or-poisto (get-in energiatodistus
                                                (conj path :tulo)))
                 " / "
                 (format-tulo-or-poisto (get-in energiatodistus
                                                (conj path :poisto))))))

(defn find-by-id [coll id]
  (->> coll (filter #(= (:id %) id)) first))

(defn join-strings [& strs]
  (->> strs (remove str/blank?) (str/join ", ")))

(def ^:private energiamuotokertoimet
  (map/map-values
    (partial map/map-keys #(-> % name (str "-kerroin") keyword))
    e-luokka-service/energiamuotokerroin))

(defn complete-energiatodistus
  [energiatodistus {:keys [postinumerot kielisyydet laatimisvaiheet
                           kayttotarkoitukset alakayttotarkoitukset
                           ilmanvaihtotyypit lammitysmuodot lammonjaot]}]
  (with-precision 20
    (let [{:keys [versio]} energiatodistus
          postinumero (find-by-id postinumerot (-> energiatodistus
                                                   :perustiedot
                                                   :postinumero
                                                   formats/string->int))
          kielisyys (find-by-id kielisyydet (-> energiatodistus
                                                :perustiedot
                                                :kieli))
          laatimisvaihe (find-by-id laatimisvaiheet (-> energiatodistus
                                                        :perustiedot
                                                        :laatimisvaihe))
          ilmanvaihtotyyppi-id (-> energiatodistus
                                   :lahtotiedot
                                   :ilmanvaihto
                                   :tyyppi-id)
          use-ilmanvaihto-kuvaus? (luokittelu/ilmanvaihto-kuvaus-required?
                                   energiatodistus)
          ilmanvaihtotyyppi (find-by-id ilmanvaihtotyypit ilmanvaihtotyyppi-id)
          lammitysmuoto-1-id (-> energiatodistus
                                 :lahtotiedot
                                 :lammitys
                                 :lammitysmuoto-1
                                 :id)
          use-lammitysmuoto-1-kuvaus? (luokittelu/lammitysmuoto-1-kuvaus-required?
                                       energiatodistus)
          lammitysmuoto-1 (find-by-id lammitysmuodot lammitysmuoto-1-id)
          lammitysmuoto-2-id (-> energiatodistus
                                 :lahtotiedot
                                 :lammitys
                                 :lammitysmuoto-2
                                 :id)
          use-lammitysmuoto-2-kuvaus? (luokittelu/lammitysmuoto-2-kuvaus-required?
                                       energiatodistus)
          lammitysmuoto-2 (find-by-id lammitysmuodot lammitysmuoto-2-id)
          lammonjako-id (-> energiatodistus
                            :lahtotiedot
                            :lammitys
                            :lammonjako
                            :id)
          use-lammonjako-kuvaus? (luokittelu/lammonjako-kuvaus-required?
                                  energiatodistus)
          lammonjako (find-by-id lammonjaot lammonjako-id)
          ;; Käyttötarkoitus is actually alakäyttötarkoitus in database
          alakayttotarkoitus-id (-> energiatodistus
                                    :perustiedot
                                    :kayttotarkoitus)
          alakayttotarkoitus (-> (get alakayttotarkoitukset versio)
                                 (find-by-id alakayttotarkoitus-id))
          paakayttotarkoitus-id (->> alakayttotarkoitus-id
                                     (find-by-id (alakayttotarkoitukset versio))
                                     :kayttotarkoitusluokka-id)
          paakayttotarkoitus (-> (get kayttotarkoitukset versio)
                                 (find-by-id paakayttotarkoitus-id))]
      (-> energiatodistus
          (assoc-in [:perustiedot :postitoimipaikka-fi] (:label-fi postinumero))
          (assoc-in [:perustiedot :postitoimipaikka-sv] (:label-sv postinumero))
          (assoc-in [:perustiedot :kieli-fi] (:label-fi kielisyys))
          (assoc-in [:perustiedot :kieli-sv] (:label-sv kielisyys))
          (assoc-in [:perustiedot :laatimisvaihe-fi] (:label-fi laatimisvaihe))
          (assoc-in [:perustiedot :laatimisvaihe-sv] (:label-sv laatimisvaihe))
          (assoc-in [:perustiedot :alakayttotarkoitus-fi] (:label-fi alakayttotarkoitus))
          (assoc-in [:perustiedot :alakayttotarkoitus-sv] (:label-sv alakayttotarkoitus))
          (assoc-in [:perustiedot :paakayttotarkoitus-id] paakayttotarkoitus-id)
          (assoc-in [:perustiedot :paakayttotarkoitus-fi] (:label-fi paakayttotarkoitus))
          (assoc-in [:perustiedot :paakayttotarkoitus-sv] (:label-sv paakayttotarkoitus))
          (update-in [:tulokset :kaytettavat-energiamuodot] (partial merge (energiamuotokertoimet versio)))
          (update-in [:tulokset :kuukausierittely] kuukausierittely-hyodynnetty)
          (assoc-kuukausierittely-summat)
          (combine-keys #(when (= versio 2013) (+ %1 %2))
                        nil
                        [:tulokset
                         :kaytettavat-energiamuodot
                         :valaistus-kuluttaja-sahko]
                        [:tulokset :lampokuormat :kuluttajalaitteet]
                        [:tulokset :lampokuormat :valaistus])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :valaistus-kuluttaja-sahko])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :kaukolampo])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :sahko])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys])
          (assoc-div-nettoala [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :sahko-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :sahko]
                        [:tulokset :kaytettavat-energiamuodot :sahko-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :sahko-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :sahko-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :sahko-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :muotokerroin])
          (combine-keys *
                        nil
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-nettoala-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-nettoala]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :muotokerroin])
          (combine-keys +
                        0
                        [:tulokset :kaytettavat-energiamuodot :summa]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo]
                        [:tulokset :kaytettavat-energiamuodot :sahko]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia])
          (combine-keys +
                        0
                        [:tulokset :kaytettavat-energiamuodot :kertoimella-summa]
                        [:tulokset :kaytettavat-energiamuodot :kaukolampo-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :sahko-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine-kertoimella]
                        [:tulokset :kaytettavat-energiamuodot :muu 0 :ostoenergia-kertoimella])
          (combine-keys (partial e-luokka-service/e-luokka-rajat
                                 (kayttotarkoitukset versio)
                                 (alakayttotarkoitukset versio)
                                 versio
                                 alakayttotarkoitus-id)
                        nil
                        [:tulokset :e-luokka-rajat]
                        [:lahtotiedot :lammitetty-nettoala])
          (combine-keys *
                        nil
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :UA]
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :ala]
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :U])
          (combine-keys *
                        nil
                        [:lahtotiedot :rakennusvaippa :ylapohja :UA]
                        [:lahtotiedot :rakennusvaippa :ylapohja :ala]
                        [:lahtotiedot :rakennusvaippa :ylapohja :U])
          (combine-keys *
                        nil
                        [:lahtotiedot :rakennusvaippa :alapohja :UA]
                        [:lahtotiedot :rakennusvaippa :alapohja :ala]
                        [:lahtotiedot :rakennusvaippa :alapohja :U])
          (combine-keys *
                        nil
                        [:lahtotiedot :rakennusvaippa :ikkunat :UA]
                        [:lahtotiedot :rakennusvaippa :ikkunat :ala]
                        [:lahtotiedot :rakennusvaippa :ikkunat :U])
          (combine-keys *
                        nil
                        [:lahtotiedot :rakennusvaippa :ulkoovet :UA]
                        [:lahtotiedot :rakennusvaippa :ulkoovet :ala]
                        [:lahtotiedot :rakennusvaippa :ulkoovet :U])
          (combine-keys +
                        0
                        [:lahtotiedot :rakennusvaippa :UA-summa]
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :UA]
                        [:lahtotiedot :rakennusvaippa :ylapohja :UA]
                        [:lahtotiedot :rakennusvaippa :alapohja :UA]
                        [:lahtotiedot :rakennusvaippa :ikkunat :UA]
                        [:lahtotiedot :rakennusvaippa :ulkoovet :UA]
                        [:lahtotiedot :rakennusvaippa :kylmasillat-UA])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :ulkoseinat :UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :ylapohja :osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :ylapohja :UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :alapohja :osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :alapohja :UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :ikkunat :osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :ikkunat :UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :ulkoovet :osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :ulkoovet :UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (combine-keys safe-div
                        nil
                        [:lahtotiedot :rakennusvaippa :kylmasillat-osuus-lampohaviosta]
                        [:lahtotiedot :rakennusvaippa :kylmasillat-UA]
                        [:lahtotiedot :rakennusvaippa :UA-summa])
          (assoc-tulo-poisto [:lahtotiedot :ilmanvaihto :paaiv])
          (assoc-tulo-poisto [:lahtotiedot :ilmanvaihto :erillispoistot])
          (assoc-tulo-poisto [:lahtotiedot :ilmanvaihto :ivjarjestelma])
          (assoc-in [:lahtotiedot :ilmanvaihto :label-fi]
                    (if use-ilmanvaihto-kuvaus?
                      (-> energiatodistus :lahtotiedot :ilmanvaihto :kuvaus-fi)
                      (-> ilmanvaihtotyyppi :label-fi)))
          (assoc-in [:lahtotiedot :ilmanvaihto :label-sv]
                    (if use-ilmanvaihto-kuvaus?
                      (-> energiatodistus :lahtotiedot :ilmanvaihto :kuvaus-sv)
                      (-> ilmanvaihtotyyppi :label-sv)))
          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-label-fi]
                    (join-strings (if use-lammitysmuoto-1-kuvaus?
                                    (-> energiatodistus
                                        :lahtotiedot
                                        :lammitys
                                        :lammitysmuoto-1
                                        :kuvaus-fi)
                                    (:label-fi lammitysmuoto-1))
                                  (if use-lammitysmuoto-2-kuvaus?
                                    (-> energiatodistus
                                        :lahtotiedot
                                        :lammitys
                                        :lammitysmuoto-2
                                        :kuvaus-fi)
                                    (:label-fi lammitysmuoto-2))))
          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-label-sv]
                    (join-strings (if use-lammitysmuoto-1-kuvaus?
                                    (-> energiatodistus
                                        :lahtotiedot
                                        :lammitys
                                        :lammitysmuoto-1
                                        :kuvaus-sv)
                                    (-> lammitysmuoto-1 :label-sv))
                                  (if use-lammitysmuoto-2-kuvaus?
                                    (-> energiatodistus
                                        :lahtotiedot
                                        :lammitys
                                        :lammitysmuoto-2
                                        :kuvaus-sv)
                                    (-> lammitysmuoto-2 :label-sv))))
          (assoc-in [:lahtotiedot :lammitys :lammonjako-label-fi]
                    (if use-lammonjako-kuvaus?
                      (-> energiatodistus
                          :lahtotiedot
                          :lammitys
                          :lammonjako
                          :kuvaus-fi)
                      (:label-fi lammonjako)))
          (assoc-in [:lahtotiedot :lammitys :lammonjako-label-sv]
                    (if use-lammonjako-kuvaus?
                      (-> energiatodistus
                          :lahtotiedot
                          :lammitys
                          :lammonjako
                          :kuvaus-sv)
                      (:label-sv lammonjako)))
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :aurinkosahko])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :tuulisahko])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :aurinkolampo])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :muulampo])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :muusahko])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat :lampopumppu])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 0 :vuosikulutus])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 1 :vuosikulutus])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 2 :vuosikulutus])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 3 :vuosikulutus])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 4 :vuosikulutus])
          (assoc-div-nettoala [:tulokset :uusiutuvat-omavaraisenergiat 5 :vuosikulutus])
          (combine-keys +
                        0
                        [:tulokset :tekniset-jarjestelmat :sahko-summa]
                        [:tulokset :tekniset-jarjestelmat :tilojen-lammitys :sahko]
                        [:tulokset :tekniset-jarjestelmat :tuloilman-lammitys :sahko]
                        [:tulokset :tekniset-jarjestelmat :kayttoveden-valmistus :sahko]
                        [:tulokset :tekniset-jarjestelmat :iv-sahko]
                        [:tulokset :tekniset-jarjestelmat :jaahdytys :sahko]
                        [:tulokset :tekniset-jarjestelmat :kuluttajalaitteet-ja-valaistus-sahko])
          (combine-keys +
                        0
                        [:tulokset :tekniset-jarjestelmat :lampo-summa]
                        [:tulokset :tekniset-jarjestelmat :tilojen-lammitys :lampo]
                        [:tulokset :tekniset-jarjestelmat :tuloilman-lammitys :lampo]
                        [:tulokset :tekniset-jarjestelmat :kayttoveden-valmistus :lampo]
                        [:tulokset :tekniset-jarjestelmat :jaahdytys :lampo])

          (combine-keys +
                        0
                        [:tulokset :tekniset-jarjestelmat :kaukojaahdytys-summa]
                        [:tulokset :tekniset-jarjestelmat :jaahdytys :kaukojaahdytys])


          (assoc-div-nettoala [:tulokset :nettotarve :tilojen-lammitys-vuosikulutus])
          (assoc-div-nettoala [:tulokset :nettotarve :ilmanvaihdon-lammitys-vuosikulutus])
          (assoc-div-nettoala [:tulokset :nettotarve :kayttoveden-valmistus-vuosikulutus])
          (assoc-div-nettoala [:tulokset :nettotarve :jaahdytys-vuosikulutus])
          (assoc-div-nettoala [:tulokset :lampokuormat :aurinko])
          (assoc-div-nettoala [:tulokset :lampokuormat :ihmiset])
          (assoc-div-nettoala [:tulokset :lampokuormat :kuluttajalaitteet])
          (assoc-div-nettoala [:tulokset :lampokuormat :valaistus])
          (assoc-div-nettoala [:tulokset :lampokuormat :kvesi])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukolampo-vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :kokonaissahko-vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :kiinteistosahko-vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :kayttajasahko-vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :kaukojaahdytys-vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 0 :vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 1 :vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 2 :vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 3 :vuosikulutus])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostettu-energia :muu 4 :vuosikulutus])
          (update-in [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet]
                     (partial merge (map/map-keys #(keyword (str (name %) "-kerroin"))
                                                  polttoaine/muunnoskertoimet)))
          (combine-keys *
                        nil
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy-kwh]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy-kerroin])
          (combine-keys *
                        nil
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu-kwh]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu-kerroin])
          (combine-keys *
                        nil
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu-kwh]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu-kerroin])
          (combine-keys *
                        nil
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit-kwh]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit]
                        [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit-kerroin])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :kevyt-polttooljy-kwh])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-havu-sekapuu-kwh])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :pilkkeet-koivu-kwh])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :puupelletit-kwh])
          (update-in [:toteutunut-ostoenergiankulutus :ostetut-polttoaineet :muu]
                     (fn [vapaat]
                       (if-let [nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)]
                         (mapv (fn [{:keys [maara-vuodessa muunnoskerroin] :as vapaa}]
                                 (if (and maara-vuodessa muunnoskerroin)
                                   (let [kwh (* maara-vuodessa muunnoskerroin)]
                                     (assoc vapaa :kwh kwh :kwh-nettoala (safe-div kwh nettoala)))
                                   vapaa))
                               vapaat)
                         vapaat)))
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa])
          (assoc-div-nettoala [:toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa])
          (combine-keys +
                        0
                        [:toteutunut-ostoenergiankulutus :summa]
                        [:toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa]
                        [:toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa]
                        [:toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa]
                        [:toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa])
          (combine-keys +
                        0
                        [:toteutunut-ostoenergiankulutus :summa-nettoala]
                        [:toteutunut-ostoenergiankulutus :sahko-vuosikulutus-yhteensa-nettoala]
                        [:toteutunut-ostoenergiankulutus :kaukolampo-vuosikulutus-yhteensa-nettoala]
                        [:toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa-nettoala]
                        [:toteutunut-ostoenergiankulutus :kaukojaahdytys-vuosikulutus-yhteensa-nettoala])
          (combine-keys +
                        0
                        [:lahtotiedot :lammitys :lampohavio-lammittamaton-tila]
                        [:lahtotiedot :lammitys :tilat-ja-iv :lampohavio-lammittamaton-tila]
                        [:lahtotiedot :lammitys :lammin-kayttovesi :lampohavio-lammittamaton-tila])))))

(defn luokittelut [db]
  {:postinumerot          (geo/find-all-postinumerot db)
   :kielisyydet           (kielisyys/find-kielisyys db)
   :laatimisvaiheet       (laatimisvaihe/find-laatimisvaiheet db)
   :kayttotarkoitukset    (into {} (map #(vector % (kayttotarkoitus-service/find-kayttotarkoitukset db %)))
                                [2013 2018])
   :alakayttotarkoitukset (into {} (map #(vector % (kayttotarkoitus-service/find-alakayttotarkoitukset db %)))
                                [2013 2018])
   :ilmanvaihtotyypit     (luokittelu/find-ilmanvaihtotyypit db)
   :lammitysmuodot        (luokittelu/find-lammitysmuodot db)
   :lammonjaot            (luokittelu/find-lammonjaot db)})

(defn find-complete-energiatodistus
  ([db id]
   (some->
     (energiatodistus-service/find-energiatodistus db id)
     (complete-energiatodistus (luokittelut db))))
  ([db whoami id]
   (some->
     (energiatodistus-service/find-energiatodistus db whoami id)
     (complete-energiatodistus (luokittelut db)))))
