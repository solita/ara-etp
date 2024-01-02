(ns solita.etp.service.energiatodistus-search-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.schema.public-energiatodistus :as energiatodistus-public-schema]
            [solita.etp.schema.valvonta-oikeellisuus :as valvonta-schema]
            [solita.etp.service.energiatodistus-search :as service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.valvonta-oikeellisuus :as valvonta-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [solita.common.logic :as logic]
            [solita.etp.whoami :as test-whoami])
  (:import (java.time Instant LocalDate)))

(t/use-fixtures :each ts/fixture)

(t/deftest select
  (t/is (= (service/select {})
           "select energiatodistus.*"))
  (t/is (= (service/select energiatodistus-schema/Energiatodistus)
           "select energiatodistus.*,\nfullname(kayttaja.*) laatija_fullname,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id"))
  (t/is (= (service/select energiatodistus-public-schema/Energiatodistus)
           "select energiatodistus.*,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id"))
  (t/is (= (service/select valvonta-schema/Energiatodistus+Valvonta)
           "select energiatodistus.*,\nfullname(kayttaja.*) laatija_fullname,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id,\ncoalesce(last_toimenpide.ongoing, false) valvonta$ongoing,\nlast_toimenpide.type_id valvonta$type_id")))

(defn sign-energiatodistukset! [laatija-id-et-id-pairs]
  (doseq [[laatija-id energiatodistus-id] laatija-id-et-id-pairs]
    (energiatodistus-service/start-energiatodistus-signing!
      ts/*db*
      {:id laatija-id}
      energiatodistus-id)
    (energiatodistus-service/end-energiatodistus-signing!
      ts/*db*
      ts/*aws-s3-client*
      {:id laatija-id}
      energiatodistus-id
      {:skip-pdf-signed-assert? true})))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        energiatodistus-adds (->> (concat
                                    (energiatodistus-test-data/generate-adds
                                      2
                                      2018
                                      true)
                                    (energiatodistus-test-data/generate-adds-with-zeros
                                      2
                                      2018))
                                  (map #(assoc-in %
                                                  [:perustiedot :postinumero]
                                                  "33100"))
                                  (map #(assoc-in %
                                                  [:perustiedot :yritys :nimi]
                                                  nil)))
        energiatodistus-ids (->> (interleave (cycle laatija-ids)
                                             energiatodistus-adds)
                                 (partition 2)
                                 (mapcat #(energiatodistus-test-data/insert!
                                            [(second %)]
                                            (first %)))
                                 sort)]
    (sign-energiatodistukset! (->> (interleave
                                     (cycle laatija-ids)
                                     (take 2 energiatodistus-ids))
                                   (partition 2)))
    (energiatodistus-service/delete-energiatodistus-luonnos!
      ts/*db*
      {:id (last laatija-ids)}
      (last energiatodistus-ids))
    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(defn search [whoami where keyword sort order]
  (service/search ts/*db*
                  whoami
                  (cond-> {}
                          where (assoc :where where)
                          keyword (assoc :keyword keyword)
                          sort (assoc :sort sort)
                          order (assoc :order order))
                  energiatodistus-schema/Energiatodistus))

(defn search-and-assert
  ([test-data-set id where]
   (search-and-assert test-data-set id where nil nil nil))
  ([test-data-set id where keyword]
   (search-and-assert test-data-set id where keyword nil nil))
  ([test-data-set id where keyword sort order]
   (let [{:keys [laatijat energiatodistukset]} test-data-set
         laatija-id (-> laatijat keys clojure.core/sort first)
         add (-> energiatodistukset (get id) (assoc :id id))
         whoami {:rooli 0 :id laatija-id}
         found (search whoami where keyword sort order)]
     (xmap/submap? (-> found first :perustiedot) (:perustiedot add)))))

(t/deftest not-found-test
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (t/is (empty? (search {:rooli 0 :id laatija-id}
                          [[["=" "energiatodistus.id" -1]]]
                          nil
                          nil
                          nil)))))

(t/deftest search-by-id-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (not (search-and-assert test-data-set
                                  id
                                  [[["=" "energiatodistus.id" (inc id)]]])))
    (t/is (search-and-assert test-data-set
                             id
                             [[["=" "energiatodistus.id" id]]]))))

(t/deftest search-by-laatija-voimassaolo-paattymisaika-test
  (let [{:keys [energiatodistukset laatijat] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        laatija-id (-> laatijat keys clojure.core/sort first)
        {:keys [voimassaolo-paattymisaika] :as laatija} (laatija-service/find-laatija-by-id ts/*db* laatija-id)
        one-day (. java.time.Duration (ofDays 1))
        two-days (. java.time.Duration (ofDays 2))]
    (t/is (nil? (-> (service/search
                      ts/*db*
                      {:rooli 0 :id laatija-id}
                      {:where [[["=" "energiatodistus.id" id]
                                ["between" "laatija.voimassaolo-paattymisaika"
                                 (.minus voimassaolo-paattymisaika two-days)
                                 (.minus voimassaolo-paattymisaika one-day)]]]}
                      energiatodistus-schema/Energiatodistus)
                    first :id)))
    (t/is (= id (-> (service/search
                      ts/*db*
                      {:rooli 0 :id laatija-id}
                      {:where [[["=" "energiatodistus.id" id]
                                ["between" "laatija.voimassaolo-paattymisaika"
                                 (.minus voimassaolo-paattymisaika one-day)
                                 (.plus voimassaolo-paattymisaika one-day)]]]}
                      energiatodistus-schema/Energiatodistus)
                    first :id)))
    (t/is (nil? (-> (service/search
                      ts/*db*
                      {:rooli 0 :id laatija-id}
                      {:where [[["=" "energiatodistus.id" id]
                                ["between" "laatija.voimassaolo-paattymisaika"
                                 (.plus voimassaolo-paattymisaika one-day)
                                 (.plus voimassaolo-paattymisaika two-days)]]]}
                      energiatodistus-schema/Energiatodistus)
                    first :id)))))

(t/deftest search-by-id-null-nettoala-test
  (let [{:keys [energiatodistukset laatijat] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        laatija-id (-> laatijat keys clojure.core/sort first)]
    (jdbc/execute!
      ts/*db*
      ["UPDATE energiatodistus SET lt$lammitetty_nettoala = NULL where id = ?" id])
    (let [found-id (-> (service/search
                         ts/*db*
                         {:rooli 0 :id laatija-id}
                         {:where [[["=" "energiatodistus.id" id]
                                   ["nil?" "energiatodistus.tulokset.nettotarve.tilojen-lammitys-neliovuosikulutus"]]]}
                         energiatodistus-schema/Energiatodistus)
                       first :id)]

      (t/is (= id found-id)))))

(t/deftest search-by-id-zero-nettoala-test
  (let [{:keys [energiatodistukset laatijat] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        laatija-id (-> laatijat keys clojure.core/sort first)]

    (jdbc/execute!
      ts/*db*
      ["UPDATE energiatodistus SET lt$lammitetty_nettoala = 0 where id = ?" id])

    (let [found-id (-> (service/search
                         ts/*db*
                         {:rooli 0 :id laatija-id}
                         {:where [[["=" "energiatodistus.id" id]
                                   ["nil?" "energiatodistus.tulokset.nettotarve.tilojen-lammitys-neliovuosikulutus"]]]}
                         energiatodistus-schema/Energiatodistus)
                       first :id)]

      (t/is (= id found-id)))))

(t/deftest search-by-id-zero-ua-test
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        [laatija-id _] (laatija-test-data/generate-and-insert!)
        [energiatodistus-id energiatodistus] (energiatodistus-test-data/generate-and-insert! 2018 false laatija-id)
        whoami-laatija (test-whoami/laatija laatija-id)]

    (energiatodistus-service/update-energiatodistus!
      (ts/db-user paakayttaja-id) (test-whoami/paakayttaja paakayttaja-id)
      energiatodistus-id (assoc energiatodistus :bypass-validation-limits true))

    (energiatodistus-service/update-energiatodistus!
      (ts/db-user laatija-id) (test-whoami/laatija laatija-id)
      energiatodistus-id
      (assoc-in energiatodistus [:lahtotiedot :rakennusvaippa]
                {:alapohja          {:ala 0 :U 0}
                 :ikkunat           {:ala 0 :U 0}
                 :ylapohja          {:ala 0 :U 0}
                 :ulkoseinat        {:ala 1M :U 0}
                 :kylmasillat-UA    0
                 :ulkoovet          {:ala 0 :U 0}}))

    (t/is (empty? (search whoami-laatija
                          [[["=" "energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat-osuus-lampohaviosta" 123]]]
                          nil
                          nil
                          nil)))

    (t/is (-> (search whoami-laatija
                      [[["nil?" "energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat-osuus-lampohaviosta"]]]
                      nil
                      nil
                      nil)
              first
              :perustiedot
              (xmap/submap? (:perustiedot energiatodistus))))))


(t/deftest search-by-nimi-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        nimi (-> energiatodistukset (get id) :perustiedot :nimi-fi)]
    (t/is (not (search-and-assert
                test-data-set
                id
                [[["=" "energiatodistus.perustiedot.nimi-fi" (str "a" nimi)]]])))
    (t/is (search-and-assert
           test-data-set
           id
           [[["=" "energiatodistus.perustiedot.nimi-fi" nimi]]]))))

(t/deftest search-by-id-and-nimi-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        nimi (-> energiatodistukset (get id) :perustiedot :nimi-fi)]
    (t/is (search-and-assert
           test-data-set
           id
           [[["=" "energiatodistus.id" id]
             ["=" "energiatodistus.perustiedot.nimi-fi" nimi]]]))))

(t/deftest search-by-nimi-*-test
  (let [[laatija-id laatija] (laatija-test-data/generate-and-insert!)
        [id energiatodistus] (energiatodistus-test-data/generate-and-insert! 2018 true laatija-id)
        nimi-fi (-> energiatodistus :perustiedot :nimi-fi)
        nimi-sv (-> energiatodistus :perustiedot :nimi-fi)
        test-data-set {:laatijat {laatija-id laatija}
                       :energiatodistukset {id energiatodistus}}]

    (t/is (empty?
            (search (test-whoami/laatija laatija-id)
                    [[["like" "energiatodistus.perustiedot.nimi-*" (str nimi-fi nimi-sv)]]]
                    nil nil nil)))

    (t/is (search-and-assert
            test-data-set
            id
            [[["like" "energiatodistus.perustiedot.nimi-*" nimi-fi]]]))

    (t/is (search-and-assert
            test-data-set
            id
            [[["like" "energiatodistus.perustiedot.nimi-*" nimi-sv]]]))))

(t/deftest search-by-havainnointikaynti-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        havainnointikaynti (-> energiatodistukset
                               (get id)
                               :perustiedot
                               :havainnointikaynti)]
    (t/is (not (search-and-assert
                 test-data-set
                 id
                 [[["="
                    "energiatodistus.perustiedot.havainnointikaynti"
                    (.plusDays havainnointikaynti 1)]]])))
    (t/is (search-and-assert
            test-data-set
            id
            [[["="
               "energiatodistus.perustiedot.havainnointikaynti"
               havainnointikaynti]]]))))

(t/deftest search-by-toimintaalue-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (not (search-and-assert
                 test-data-set
                 id
                 [[["like" "toimintaalue.label-fi" "Kain%"]]])))
    (t/is (not (search-and-assert test-data-set id nil "Kain")))
    (t/is (search-and-assert
            test-data-set
            id
            [[["like" "toimintaalue.label-fi" "Pirkanma%"]]]))
    (t/is (search-and-assert test-data-set id nil "Pirkan"))))

(t/deftest search-by-postinumero-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (not (search-and-assert test-data-set id nil "3312")))
    (t/is (search-and-assert test-data-set id nil "33100"))))

(t/deftest search-by-katuosoite-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        {:keys [katuosoite-fi
                katuosoite-sv]} (:perustiedot (get energiatodistukset id))]
    (t/is (not (search-and-assert test-data-set id nil (str "a" katuosoite-fi))))
    (t/is (not (search-and-assert test-data-set id nil (str "a" katuosoite-sv))))
    (t/is (search-and-assert test-data-set id nil (str katuosoite-fi)))
    (t/is (search-and-assert test-data-set id nil (str katuosoite-sv)))))

(t/deftest search-by-nil-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (not (search-and-assert
                 test-data-set
                 id
                 [[["=" "energiatodistus.perustiedot.yritys.nimi" "a"]]])))
    (t/is (search-and-assert
            test-data-set
            id
            [[["nil?" "energiatodistus.perustiedot.yritys.nimi"]]]))))

(t/deftest search-by-ostettu-energia
  (let [nettoala 100M
        ostettu-kaukolampo 20000M
        expected-kwh-per-year-m2 (/ ostettu-kaukolampo nettoala)
        laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        other-ets (energiatodistus-test-data/generate-adds 5 2018 true)
        other-et-ids (energiatodistus-test-data/insert! other-ets laatija-id)
        target-et (-> (energiatodistus-test-data/generate-add 2018 true)
                      (assoc :draft-visible-to-paakayttaja true)
                      (assoc-in [:lahtotiedot
                                 :lammitetty-nettoala]
                                nettoala)
                      (assoc-in [:toteutunut-ostoenergiankulutus
                                 :ostettu-energia
                                 :kaukolampo-vuosikulutus]
                                ostettu-kaukolampo))
        target-et-id (-> (energiatodistus-test-data/insert! [target-et] laatija-id)
                         first)]
    (t/is (contains? (->> (service/search
                            ts/*db*
                            kayttaja-test-data/paakayttaja
                            {:where [[["=" "energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kaukolampo-neliovuosikulutus"
                                       expected-kwh-per-year-m2
                                       ]]]}
                            energiatodistus-schema/Energiatodistus)
                          (map :id)
                          set)
                     target-et-id))
    (t/is (not (contains? (->> (service/search
                                 ts/*db*
                                 kayttaja-test-data/paakayttaja
                                 {:where [[["<" "energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kaukolampo-neliovuosikulutus"
                                            (dec expected-kwh-per-year-m2)
                                            ]]]}
                                 energiatodistus-schema/Energiatodistus)
                               (map :id)
                               set)
                          target-et-id)))
    (t/is (not (contains? (->> (service/search
                                 ts/*db*
                                 kayttaja-test-data/paakayttaja
                                 {:where [[[">" "energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kaukolampo-neliovuosikulutus"
                                            (inc expected-kwh-per-year-m2)
                                            ]]]}
                                 energiatodistus-schema/Energiatodistus)
                               (map :id)
                               set)
                          target-et-id)))))

(t/deftest search-by-allekirjoitusaika-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (not (search-and-assert
                 test-data-set
                 id
                 [[[">" "energiatodistus.allekirjoitusaika" (Instant/now)]]])))
    (t/is (search-and-assert
            test-data-set
            id
            [[["<" "energiatodistus.allekirjoitusaika" (Instant/now)]]]))
    (t/is (= id (-> (search kayttaja-test-data/paakayttaja
                            [[["<" "energiatodistus.allekirjoitusaika" (Instant/now)]]]
                            nil
                            "energiatodistus.allekirjoitusaika"
                            "desc")
                    second
                    :id)))))

(t/deftest search-by-sahko-painotettu-neliovuosikulutus-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        sahko (-> energiatodistus :tulokset :kaytettavat-energiamuodot :sahko)
        sahko-kertoimella (* sahko (get-in e-luokka-service/energiamuotokerroin
                                           [2018 :sahko]))]
    (t/is (search-and-assert
            test-data-set
            id
            [[["="
               "energiatodistus.tulokset.kaytettavat-energiamuodot.sahko"
               sahko]
              ["="
               "energiatodistus.tulokset.kaytettavat-energiamuodot.sahko-painotettu"
               sahko-kertoimella]
              ["="
               "energiatodistus.tulokset.kaytettavat-energiamuodot.sahko-painotettu-neliovuosikulutus"
               (/ sahko-kertoimella nettoala)]]]))))

(t/deftest search-by-uusiutuvat-omavaraisenergiat-aurinkosahko-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        aurinkosahko (-> energiatodistus
                         :tulokset
                         :uusiutuvat-omavaraisenergiat
                         :aurinkosahko)]
    (t/is (search-and-assert
            test-data-set
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkosahko"
               aurinkosahko]
              ["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkosahko-neliovuosikulutus"
               (/ aurinkosahko nettoala)]]]))))

(t/deftest search-by-rakennusvaippa-ikkunat-osuus-lampohaviosta-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set)
        id (-> energiatodistukset keys sort first)
        {:keys [lahtotiedot]} (get energiatodistukset id)
        {:keys [rakennusvaippa]} lahtotiedot
        {:keys [ulkoseinat ylapohja alapohja ikkunat ulkoovet kylmasillat-UA]} rakennusvaippa
        ua-list (conj (->> [ikkunat ulkoseinat ylapohja alapohja ulkoovet]
                           (mapv #(* (:ala %) (:U %))))
                      kylmasillat-UA)
        ikkunat-ua (first ua-list)
        ua-summa (reduce + ua-list)]
    (t/is (search-and-assert
            test-data-set
            id
            [[["=" "energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.UA" ikkunat-ua]
              ["="
               "energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.osuus-lampohaviosta"
               (with-precision 20 (/ ikkunat-ua ua-summa))]]]))))

(t/deftest search-by-ostetut-polttoaineet
  (let [nettoala 100M
        kevyt-polttooljy-litres 2000M
        expected-kwh-per-year-m2 (/ (* kevyt-polttooljy-litres 10) nettoala)
        laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        other-ets (energiatodistus-test-data/generate-adds 5 2018 true)
        other-et-ids (energiatodistus-test-data/insert! other-ets laatija-id)
        target-et (-> (energiatodistus-test-data/generate-add 2018 true)
                      (assoc :draft-visible-to-paakayttaja true)
                      (assoc-in [:lahtotiedot
                                 :lammitetty-nettoala]
                                nettoala)
                      (assoc-in [:toteutunut-ostoenergiankulutus
                                 :ostetut-polttoaineet
                                 :kevyt-polttooljy]
                                kevyt-polttooljy-litres))
        target-et-id (-> (energiatodistus-test-data/insert! [target-et] laatija-id)
                         first)]
    (t/is (contains? (->> (service/search
                            ts/*db*
                            kayttaja-test-data/paakayttaja
                            {:where [[["=" "energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.kevyt-polttooljy-neliovuosikulutus"
                                       expected-kwh-per-year-m2
                                       ]]]}
                            energiatodistus-schema/Energiatodistus)
                          (map :id)
                          set)
                     target-et-id))
    (t/is (not (contains? (->> (service/search
                                 ts/*db*
                                 kayttaja-test-data/paakayttaja
                                 {:where [[["<" "energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.kevyt-polttooljy-neliovuosikulutus"
                                            (dec expected-kwh-per-year-m2)
                                            ]]]}
                                 energiatodistus-schema/Energiatodistus)
                               (map :id)
                               set)
                          target-et-id)))
    (t/is (not (contains? (->> (service/search
                                 ts/*db*
                                 kayttaja-test-data/paakayttaja
                                 {:where [[[">" "energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.kevyt-polttooljy-neliovuosikulutus"
                                            (inc expected-kwh-per-year-m2)
                                            ]]]}
                                 energiatodistus-schema/Energiatodistus)
                               (map :id)
                               set)
                          target-et-id)))))

(t/deftest laatija-cant-find-other-laatija-energiatodistukset-test
  (let [{:keys [laatijat energiatodistukset] :as test-data-set} (test-data-set)
        laatija-ids (-> laatijat keys sort)]
    (t/is (= 1 (count (search {:rooli 0 :id (first laatija-ids)}
                              nil
                              nil
                              nil
                              nil))))
    (t/is (= 1 (count (search {:rooli 0 :id (second laatija-ids)}
                              nil
                              nil
                              nil
                              nil))))))

(t/deftest public-paakayttaja-and-laskuttaja-cant-find-luonnokset-test
  (let [{:keys [laatijat energiatodistukset] :as test-data-set} (test-data-set)
        laatija-ids (-> laatijat keys sort)]
    (t/is (= 2 (count (search kayttaja-test-data/paakayttaja nil nil nil nil))))
    (t/is (= 2 (count (search kayttaja-test-data/laskuttaja nil nil nil nil))))
    (t/is (= 0 (count (search nil nil nil nil nil))))))

(t/deftest deleted-are-not-found-test
  (let [{:keys [laatijat energiatodistukset] :as test-data-set} (test-data-set)
        laatija-ids (-> laatijat keys sort)]
    (t/is (= 1 (count (search {:rooli 0 :id (first laatija-ids)}
                              nil
                              nil
                              nil
                              nil))))))

(t/deftest invalid-search-expression
  (let [pk kayttaja-test-data/paakayttaja
        search #(search %1 %2 nil nil nil)]
    (t/is (= :schema.core/error
             (:type (etp-test/catch-ex-data #(search pk [[[1]]])))))
    (t/is (= :schema.core/error
             (:type (etp-test/catch-ex-data #(search pk [[[]]])))))
    (t/is (= {:type      :invalid-arguments
              :predicate "="
              :message   "Wrong number of arguments: () for predicate: ="}
             (etp-test/catch-ex-data #(search pk [[["="]]]))))
    (t/is (= {:type      :invalid-arguments
              :predicate "="
              :message   "Wrong number of arguments: (\"id\") for predicate: ="}
             (etp-test/catch-ex-data #(search pk [[["=" "id"]]]))))
    (t/is (= {:type    :unknown-predicate :predicate "asdf"
              :message "Unknown predicate: asdf"}
             (etp-test/catch-ex-data #(search pk [[["asdf" "id" 1]]]))))
    (t/is (= {:type    :unknown-field
              :field   "energiatodistus.perustiedot.tilaaja"
              :message "Unknown field: energiatodistus.perustiedot.tilaaja"}
             (etp-test/catch-ex-data #(search nil [[["="
                                                     "energiatodistus.perustiedot.tilaaja"
                                                     "test"]]]))))
    (t/is (= {:type    :unknown-field
              :field   "asdf"
              :message "Unknown field: asdf"}
             (etp-test/catch-ex-data #(search pk [[["=" "asdf" "test"]]]))))))

(defn compare-energiatodistus [id versio laatija-id laatija
                               energiatodistus-add valvonta energiatodistus-search]
  (= (-> energiatodistus-add
         (assoc
           :id id :versio versio :tila-id 0
           :laatija-id laatija-id
           :laatija-fullname (str (:sukunimi laatija) ", " (:etunimi laatija))
           :valvonta valvonta
           :korvaava-energiatodistus-id nil)
         (update-in [:tulokset :kuukausierittely] (logic/when* nil? (constantly [])))
         (dissoc :kommentti))
     (-> energiatodistus-search
         (dissoc
           :kommentti :voimassaolo-paattymisaika
           :allekirjoitusaika :laskutusaika)
         (xmap/dissoc-in [:tulokset :e-luokka])
         (xmap/dissoc-in [:tulokset :e-luku]))))

(defn search-by-id [whoami id]
  (service/search ts/*db*
                  whoami
                  {:where [[["=" "energiatodistus.id" id]]]}
                  valvonta-schema/Energiatodistus+Valvonta))

(t/deftest energiatodistus+valvonta
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        [id energiatodistus] (first (energiatodistus-test-data/generate-and-insert!
                                      1
                                      2018
                                      true laatija-id))
        whoami {:rooli 0 :id laatija-id}]

    (t/is (= (count (search-by-id kayttaja-test-data/paakayttaja id)) 0))

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija energiatodistus
            {:pending false, :valvoja-id nil, :ongoing false, :type-id nil}
            (first (search-by-id whoami id))))

    (energiatodistus-service/update-energiatodistus!
      ts/*db* whoami id (assoc energiatodistus :draft-visible-to-paakayttaja true))

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija
            (assoc energiatodistus :draft-visible-to-paakayttaja true)
            {:pending false, :valvoja-id nil, :ongoing false, :type-id nil}
            (first (search-by-id kayttaja-test-data/paakayttaja id))))

    (valvonta-service/save-valvonta! ts/*db* kayttaja-test-data/paakayttaja id {:pending true})

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija
            (assoc energiatodistus :draft-visible-to-paakayttaja true)
            {:pending true, :valvoja-id nil, :ongoing false, :type-id nil}
            (first (search-by-id kayttaja-test-data/paakayttaja id))))

    (valvonta-service/add-toimenpide!
      ts/*db* ts/*aws-s3-client* kayttaja-test-data/paakayttaja id
      {:type-id     3   :deadline-date (LocalDate/now) :description nil
       :severity-id nil :template-id 1 :virheet [] :tiedoksi []})

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija
            (assoc energiatodistus :draft-visible-to-paakayttaja true)
            {:pending true, :valvoja-id nil, :ongoing true, :type-id 3}
            (first (search-by-id kayttaja-test-data/paakayttaja id))))

    (valvonta-service/add-toimenpide!
      ts/*db* ts/*aws-s3-client* kayttaja-test-data/paakayttaja id
      {:type-id     4   :deadline-date (LocalDate/now) :description nil
       :severity-id nil :template-id 1 :virheet [] :tiedoksi []})

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija
            (assoc energiatodistus :draft-visible-to-paakayttaja true)
            {:pending true, :valvoja-id nil, :ongoing true, :type-id 4}
            (first (search-by-id kayttaja-test-data/paakayttaja id))))

    ;; wait for emails to finish
    (Thread/sleep 100)))

(t/deftest search-by-postitoimipaikka-test
  (let [[laatija-id laatija] (-> (laatija-test-data/generate-and-insert! 1) first)
        energiatodistus-adds (concat
                               ;; Pyhtää / Pyttis
                               (map #(assoc-in % [:perustiedot :postinumero] "49270")
                                    (energiatodistus-test-data/generate-adds 2 2018 true))
                               ;; Purola / Svartbäck
                               (map #(assoc-in % [:perustiedot :postinumero] "49240")
                                    (energiatodistus-test-data/generate-adds 3 2018 true)))
        energiatodistus-ids (energiatodistus-test-data/insert!
                              energiatodistus-adds
                              laatija-id)]
    (sign-energiatodistukset! (map #(vec [laatija-id %]) energiatodistus-ids))

    (t/testing "Simple search in Finnish"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "postinumero.label" "purola"]]]
                            nil nil nil)]
        (t/is (= (count results) 3))
        (doseq [et results]
          (t/is (= (get-in et [:perustiedot :postinumero]) "49240")))))
    (t/testing "Search in Swedish, using caps"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "postinumero.label" "PYTTIS"]]]
                            nil nil nil)]
        (t/is (= (count results) 2))
        (doseq [et results]
          (t/is (= (get-in et [:perustiedot :postinumero]) "49270")))))
    (t/testing "Search with some other predicate"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["=" "postinumero.label" "PYTTIS"]]]
                            nil nil nil)]
        (t/is (= (count results) 2))
        (doseq [et results]
          (t/is (= (get-in et [:perustiedot :postinumero]) "49270")))))))

(t/deftest search-by-nimi-both-languages-test
  (let [[laatija-id _] (-> (laatija-test-data/generate-and-insert! 1) first)
        energiatodistus-adds (concat
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:perustiedot :nimi-fi] "Talo 12499")
                                          (assoc-in [:perustiedot :nimi-sv] "Hus 12499")))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (energiatodistus-test-data/generate-adds 3 2018 true))
        energiatodistus-ids (energiatodistus-test-data/insert!
                              energiatodistus-adds
                              laatija-id)]
    (sign-energiatodistukset! (map #(vec [laatija-id %]) energiatodistus-ids))

    (t/testing "Simple search in Finnish"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "energiatodistus.perustiedot.nimi" "%Talo 12499%"]]]
                            nil nil nil)]
        (t/is (= (count results) 1))
        (doseq [et results]
          (t/is (= (get-in et [:perustiedot :nimi-fi]) "Talo 12499"))
          (t/is (= (get-in et [:perustiedot :nimi-sv]) "Hus 12499")))))
    (t/testing "Search in Swedish, using caps"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "energiatodistus.perustiedot.nimi" "%Hus 12499%"]]]
                            nil nil nil)]
        (t/is (= (count results) 1))
        (doseq [et results]
          (t/is (= (get-in et [:perustiedot :nimi-fi]) "Talo 12499"))
          (t/is (= (get-in et [:perustiedot :nimi-sv]) "Hus 12499")))))
    (t/testing "Negation"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["not ilike" "energiatodistus.perustiedot.nimi" "%12499%"]]]
                            nil nil nil)]
        (t/is (= (count results) 3))))
    (t/testing "Negation so that it is hitting only one language"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["not ilike" "energiatodistus.perustiedot.nimi" "Hus 12499"]]]
                            nil nil nil)]
        (t/is (= (count results) 3))))))

(t/deftest search-by-lammitysmuoto-test
  (let [[laatija-id _] (-> (laatija-test-data/generate-and-insert! 1) first)
        energiatodistus-adds (concat
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 1)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 2)))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 9)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 9)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-fi] "Lämmitetään puulla")
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :kuvaus-sv] "Värms med ved")
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-fi] "Lämmitetään atomivoimalla")
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :kuvaus-sv] "Värms med atomkraft")))
                                    (energiatodistus-test-data/generate-adds 1 2018 true)))
        energiatodistus-ids (energiatodistus-test-data/insert!
                              energiatodistus-adds
                              laatija-id)]
    (sign-energiatodistukset! (map #(vec [laatija-id %]) energiatodistus-ids))

    (t/testing "Searching from lammitysmuoto-1"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["=" "energiatodistus.lahtotiedot.lammitys.lammitysmuoto.id" 1]]]
                            nil nil nil)]
        (t/is (= (count results) 1))))
    (t/testing "Searching from lammitysmuoto-2"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["=" "energiatodistus.lahtotiedot.lammitys.lammitysmuoto.id" 2]]]
                            nil nil nil)]
        (t/is (= (count results) 1))))
    (t/testing "Searching from Finnish description"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "energiatodistus.lahtotiedot.lammitys.lammitysmuoto.kuvaus-fi" "%atomivoima%"]]]
                            nil nil nil)]
        (t/is (= (count results) 1))))
    (t/testing "Searching from Swedish description"
      (let [results (search kayttaja-test-data/paakayttaja
                            [[["ilike" "energiatodistus.lahtotiedot.lammitys.lammitysmuoto.kuvaus-sv" "%med%"]]]
                            nil nil nil)]
        (t/is (= (count results) 1))))))
