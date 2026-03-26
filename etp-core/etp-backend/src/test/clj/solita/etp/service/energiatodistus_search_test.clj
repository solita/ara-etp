(ns solita.etp.service.energiatodistus-search-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
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
           "select energiatodistus.*,\nfullname(kayttaja.*) laatija_fullname,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id,\nperusparannuspassi.id as perusparannuspassi_id,\nperusparannuspassi.valid as perusparannuspassi_valid"))
  (t/is (= (service/select energiatodistus-public-schema/Energiatodistus)
           "select energiatodistus.*,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id"))
  (t/is (= (service/select valvonta-schema/Energiatodistus+Valvonta)
           "select energiatodistus.*,\nfullname(kayttaja.*) laatija_fullname,\nkorvaava_energiatodistus.id as korvaava_energiatodistus_id,\nperusparannuspassi.id as perusparannuspassi_id,\nperusparannuspassi.valid as perusparannuspassi_valid,\ncoalesce(last_toimenpide.ongoing, false) valvonta$ongoing,\nlast_toimenpide.type_id valvonta$type_id")))

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
                {:alapohja       {:ala 0 :U 0}
                 :ikkunat        {:ala 0 :U 0}
                 :ylapohja       {:ala 0 :U 0}
                 :ulkoseinat     {:ala 1M :U 0}
                 :kylmasillat-UA 0
                 :ulkoovet       {:ala 0 :U 0}}))

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
        test-data-set {:laatijat           {laatija-id laatija}
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

;; === AE-2614: ET2026 painotettu kulutus search tests ===

(defn test-data-set-2026 []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        energiatodistus-adds (->> (energiatodistus-test-data/generate-adds
                                    2
                                    2026
                                    true)
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
    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

;; 2.1 - Verify painotettu-kulutus-sql works for version 2026 with sahko
(t/deftest search-by-sahko-painotettu-neliovuosikulutus-2026-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        sahko (-> energiatodistus :tulokset :kaytettavat-energiamuodot :sahko)
        ;; Given a 2026 energiatodistus with known sahko value
        ;; When we calculate painotettu using the new 2026 coefficient (0.90)
        sahko-kertoimella (* sahko (get-in e-luokka-service/energiamuotokerroin
                                           [2026 :sahko]))]
    ;; Then searching by painotettu and painotettu-neliovuosikulutus finds the correct ET
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

;; 2.2 - Verify painotettu-kulutus-sql works for version 2026 with kaukolampo
(t/deftest search-by-kaukolampo-painotettu-2026-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        kaukolampo (-> energiatodistus :tulokset :kaytettavat-energiamuodot :kaukolampo)
        ;; Given a 2026 energiatodistus with known kaukolampo value
        ;; When we calculate painotettu using the new 2026 coefficient (0.38)
        kaukolampo-kertoimella (* kaukolampo (get-in e-luokka-service/energiamuotokerroin
                                                     [2026 :kaukolampo]))]
    ;; Then searching by painotettu finds the correct ET
    (t/is (search-and-assert
            test-data-set
            id
            [[["="
               "energiatodistus.tulokset.kaytettavat-energiamuodot.kaukolampo-painotettu"
               kaukolampo-kertoimella]]]))))

;; 2.3 - Verify painotettu-kulutus-sql works for version 2026 with kaukojaahdytys
(t/deftest search-by-kaukojaahdytys-painotettu-2026-test
  (let [{:keys [energiatodistukset] :as test-data-set} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        kaukojaahdytys (-> energiatodistus :tulokset :kaytettavat-energiamuodot :kaukojaahdytys)
        ;; Given a 2026 energiatodistus with known kaukojaahdytys value
        ;; When we calculate painotettu using the new 2026 coefficient (0.21)
        kaukojaahdytys-kertoimella (* kaukojaahdytys (get-in e-luokka-service/energiamuotokerroin
                                                             [2026 :kaukojaahdytys]))]
    ;; Then searching by painotettu finds the correct ET
    (t/is (search-and-assert
            test-data-set
            id
            [[["="
               "energiatodistus.tulokset.kaytettavat-energiamuodot.kaukojaahdytys-painotettu"
               kaukojaahdytys-kertoimella]]]))))

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

(t/deftest public-search-2026-non-excluded-kayttotarkoitus-visible-test
  ;; given: a signed 2026 energiatodistus with non-excluded käyttötarkoitus 'RT'
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        et-add (-> (energiatodistus-test-data/generate-add 2026 true)
                   (assoc-in [:perustiedot :kayttotarkoitus] "RT"))
        et-id (first (energiatodistus-test-data/insert! [et-add] laatija-id))]
    (sign-energiatodistukset! [[laatija-id et-id]])
    ;; when: searching as public user (nil whoami)
    ;; then: the energiatodistus appears in results
    (t/is (= 1 (count (search nil nil nil nil nil)))
           "Signed 2026 ET with non-excluded käyttötarkoitus should be visible in public search")))

(t/deftest public-search-2026-excluded-kayttotarkoitus-not-visible-test
  ;; given: signed 2026 energiatodistukset with excluded käyttötarkoitus values
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        make-et (fn [kayttotarkoitus]
                  (-> (energiatodistus-test-data/generate-add 2026 true)
                      (assoc-in [:perustiedot :kayttotarkoitus] kayttotarkoitus)))
        yat-id (first (energiatodistus-test-data/insert! [(make-et "YAT")] laatija-id))
        kat-id (first (energiatodistus-test-data/insert! [(make-et "KAT")] laatija-id))
        krep-id (first (energiatodistus-test-data/insert! [(make-et "KREP")] laatija-id))]
    (sign-energiatodistukset! [[laatija-id yat-id]
                               [laatija-id kat-id]
                               [laatija-id krep-id]])
    ;; when: searching as public user
    ;; then: none of the excluded käyttötarkoitus values appear
    (t/is (= 0 (count (search nil nil nil nil nil)))
           "Signed 2026 ETs with excluded käyttötarkoitus (YAT, KAT, KREP) should not be visible in public search")))

(t/deftest public-search-2026-private-search-finds-excluded-kayttotarkoitus-test
  ;; given: a signed 2026 energiatodistus with excluded käyttötarkoitus 'YAT'
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        et-add (-> (energiatodistus-test-data/generate-add 2026 true)
                   (assoc-in [:perustiedot :kayttotarkoitus] "YAT"))
        et-id (first (energiatodistus-test-data/insert! [et-add] laatija-id))]
    (sign-energiatodistukset! [[laatija-id et-id]])
    ;; when: searching as pääkäyttäjä (private search)
    ;; then: the excluded käyttötarkoitus is still found (exclusion only applies to public)
    (t/is (= 1 (count (search kayttaja-test-data/paakayttaja nil nil nil nil)))
           "Pääkäyttäjä should find 2026 ET with excluded käyttötarkoitus in private search")))

(t/deftest public-search-2026-mixed-kayttotarkoitus-test
  ;; given: multiple signed 2026 energiatodistukset with different käyttötarkoitus values
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        make-et (fn [kayttotarkoitus luokka-id]
                  (-> (energiatodistus-test-data/generate-add 2026 true)
                      (assoc-in [:perustiedot :kayttotarkoitus] kayttotarkoitus)
                      (assoc-in [:lahtotiedot :sis-kuorma]
                                (energiatodistus-test-data/sisainen-kuorma 2026 luokka-id))))
        ;; Non-excluded: RT (Rivitalot, luokka 1), T (Toimistorakennukset, luokka 3)
        rt-id (first (energiatodistus-test-data/insert! [(make-et "RT" 1)] laatija-id))
        t-id (first (energiatodistus-test-data/insert! [(make-et "T" 3)] laatija-id))
        ;; Excluded: YAT (luokka 1), KAT (luokka 1)
        yat-id (first (energiatodistus-test-data/insert! [(make-et "YAT" 1)] laatija-id))
        kat-id (first (energiatodistus-test-data/insert! [(make-et "KAT" 1)] laatija-id))]
    (sign-energiatodistukset! [[laatija-id rt-id]
                               [laatija-id t-id]
                               [laatija-id yat-id]
                               [laatija-id kat-id]])
    ;; when: searching as public user
    ;; then: only non-excluded types are visible (RT and T)
    (t/is (= 2 (count (search nil nil nil nil nil)))
           "Public search should show only non-excluded 2026 käyttötarkoitus (RT, T but not YAT, KAT)")))

(t/deftest public-search-regression-2018-kayttotarkoitus-filtering-test
  ;; given: signed 2018 energiatodistukset with excluded and non-excluded käyttötarkoitus
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        make-et (fn [kayttotarkoitus luokka-id]
                  (-> (energiatodistus-test-data/generate-add 2018 true)
                      (assoc-in [:perustiedot :kayttotarkoitus] kayttotarkoitus)
                      (assoc-in [:lahtotiedot :sis-kuorma]
                                (energiatodistus-test-data/sisainen-kuorma 2018 luokka-id))))
        ;; Non-excluded for 2018: T (Toimistorakennukset, luokka 3)
        t-id (first (energiatodistus-test-data/insert! [(make-et "T" 3)] laatija-id))
        ;; Excluded for 2018: YAT (luokka 1), KAT (luokka 1), KREP (luokka 1)
        yat-id (first (energiatodistus-test-data/insert! [(make-et "YAT" 1)] laatija-id))
        kat-id (first (energiatodistus-test-data/insert! [(make-et "KAT" 1)] laatija-id))
        krep-id (first (energiatodistus-test-data/insert! [(make-et "KREP" 1)] laatija-id))]
    (sign-energiatodistukset! [[laatija-id t-id]
                               [laatija-id yat-id]
                               [laatija-id kat-id]
                               [laatija-id krep-id]])
    ;; when: searching as public user
    ;; then: only T is visible; YAT, KAT, KREP are excluded
    (t/is (= 1 (count (search nil nil nil nil nil)))
           "Regression: 2018 public search should still exclude YAT, KAT, KREP")))

(t/deftest public-search-regression-2013-kayttotarkoitus-filtering-test
  ;; given: signed 2013 energiatodistukset with excluded and non-excluded käyttötarkoitus
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        make-et (fn [kayttotarkoitus luokka-id]
                  (-> (energiatodistus-test-data/generate-add 2013 true)
                      (assoc-in [:perustiedot :kayttotarkoitus] kayttotarkoitus)
                      (assoc-in [:lahtotiedot :sis-kuorma]
                                (energiatodistus-test-data/sisainen-kuorma 2013 luokka-id))))
        ;; Non-excluded for 2013: T (Toimistorakennukset, luokka 4 in 2013)
        t-id (first (energiatodistus-test-data/insert! [(make-et "T" 4)] laatija-id))
        ;; Excluded for 2013: YAT (luokka 1), KAT (luokka 1), MEP (luokka 1), MAEP (luokka 1)
        yat-id (first (energiatodistus-test-data/insert! [(make-et "YAT" 1)] laatija-id))
        kat-id (first (energiatodistus-test-data/insert! [(make-et "KAT" 1)] laatija-id))
        mep-id (first (energiatodistus-test-data/insert! [(make-et "MEP" 1)] laatija-id))
        maep-id (first (energiatodistus-test-data/insert! [(make-et "MAEP" 1)] laatija-id))]
    (sign-energiatodistukset! [[laatija-id t-id]
                               [laatija-id yat-id]
                               [laatija-id kat-id]
                               [laatija-id mep-id]
                               [laatija-id maep-id]])
    ;; when: searching as public user
    ;; then: only T is visible; YAT, KAT, MEP, MAEP are excluded
    (t/is (= 1 (count (search nil nil nil nil nil)))
           "Regression: 2013 public search should still exclude YAT, KAT, MEP, MAEP")))

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
           :allekirjoitusaika :laskutusaika :perusparannuspassi-id)
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
      {:type-id     3 :deadline-date (LocalDate/now) :description nil
       :severity-id nil :template-id 1 :virheet [] :tiedoksi []})

    (t/is (compare-energiatodistus
            id 2018 laatija-id laatija
            (assoc energiatodistus :draft-visible-to-paakayttaja true)
            {:pending true, :valvoja-id nil, :ongoing true, :type-id 3}
            (first (search-by-id kayttaja-test-data/paakayttaja id))))

    (valvonta-service/add-toimenpide!
      ts/*db* ts/*aws-s3-client* kayttaja-test-data/paakayttaja id
      {:type-id     4 :deadline-date (LocalDate/now) :description nil
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

(t/deftest search-perusparannuspassi-test
  (let [laatija-id (->> (laatija-test-data/generate-adds 1)
                        (map #(assoc-in % [:patevyystaso] 4))
                        (laatija-test-data/insert!)
                        first)
        laatija-whoami {:id laatija-id :patevyystaso 4 :rooli 0}

        energiatodistus-ids-2018 (keys (energiatodistus-test-data/generate-and-insert! 2 2018 true laatija-id))
        energiatodistus-ids-2026-without-ppp (keys (energiatodistus-test-data/generate-and-insert! 2 2026 true laatija-id))
        never-created-ppp-et-ids (concat energiatodistus-ids-2018 energiatodistus-ids-2026-without-ppp)

        energiatodistus-ids-2026-with-ppp (keys (energiatodistus-test-data/generate-and-insert! 2 2026 true laatija-id))
        perusparannuspassi-ids-2026 (ppp-test-data/generate-and-insert! energiatodistus-ids-2026-with-ppp laatija-whoami)

        energiatodistus-id-2026-ppp-removed (first (keys (energiatodistus-test-data/generate-and-insert! 1 2026 true laatija-id)))
        perusparannuspassi-removed-id-2026 (first (ppp-test-data/generate-and-insert! [energiatodistus-id-2026-ppp-removed] laatija-whoami))

        energiatodistus->ppp (merge (zipmap energiatodistus-ids-2026-with-ppp perusparannuspassi-ids-2026)
                                    (zipmap [energiatodistus-id-2026-ppp-removed] [perusparannuspassi-removed-id-2026]))]
    ;; Remove the ppp from energiatodistus
    (-> (ppp-test-data/generate-add energiatodistus-id-2026-ppp-removed)
        (assoc-in [:valid] false)
        (ppp-test-data/update! perusparannuspassi-removed-id-2026 laatija-whoami))
    (t/testing "Perusparannuspassi-id shows up (created and exists in db)"
      (let [results (search laatija-whoami nil nil nil nil)
            filtered-results (filter #(->> %
                                           :id
                                           (contains? (set energiatodistus-ids-2026-with-ppp))) results)]
        (t/is (= 2 (count filtered-results)))
        (t/is (every? #(= (get energiatodistus->ppp (:id %)) (:perusparannuspassi-id %))
                      filtered-results))))
    (t/testing "Perusparannuspassi-id does not show up (deleted but exists in db)"
      (let [results (search laatija-whoami nil nil nil nil)
            filtered-results (filter #(->> %
                                           :id
                                           (contains? (set [energiatodistus-id-2026-ppp-removed]))) results)]
        (t/is (= 1 (count filtered-results)))
        (t/is (every? #(= nil (:perusparannuspassi-id %))
                      filtered-results))))
    (t/testing "Perusparannuspassi-id shows does not show up (never created)"
      (let [results (search laatija-whoami nil nil nil nil)
            filtered-results (filter #(->> %
                                           :id
                                           (contains? (set never-created-ppp-et-ids))) results)]
        (t/is (= 4 (count filtered-results)))
        (t/is (every? #(= nil (:perusparannuspassi-id %))
                      filtered-results))))))

;; === AE-2618: ET2026 haku uusilla tietokentillä ===

(defn test-data-set-mixed
  "Creates both ET2018 and ET2026 certificates for mixed-version tests.
   Returns {:laatijat, :energiatodistukset-2018, :energiatodistukset-2026}"
  []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        first-laatija-id (first laatija-ids)
        et2018-adds (->> (energiatodistus-test-data/generate-adds 2 2018 true)
                         (map #(assoc-in % [:perustiedot :postinumero] "33100"))
                         (map #(assoc-in % [:perustiedot :yritys :nimi] nil)))
        et2026-adds (->> (energiatodistus-test-data/generate-adds 2 2026 true)
                         (map #(assoc-in % [:perustiedot :postinumero] "33100"))
                         (map #(assoc-in % [:perustiedot :yritys :nimi] nil)))
        et2018-ids (energiatodistus-test-data/insert! et2018-adds first-laatija-id)
        et2026-ids (energiatodistus-test-data/insert! et2026-adds first-laatija-id)]
    (sign-energiatodistukset! (map #(vector first-laatija-id %) (concat et2018-ids et2026-ids)))
    {:laatijat                laatijat
     :energiatodistukset-2018 (zipmap et2018-ids et2018-adds)
     :energiatodistukset-2026 (zipmap et2026-ids et2026-adds)}))

;; --- 1.1 ET2026-spesifisellä kentällä haku löytää ET2026-todistuksen ---
(t/deftest search-by-havainnointikayntityyppi-id-2026-test
  ;; Given: ET2026 certificate with known havainnointikayntityyppi-id
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        havainnointikayntityyppi-id (-> energiatodistus :perustiedot :havainnointikayntityyppi-id)]
    ;; When: searching by energiatodistus.perustiedot.havainnointikayntityyppi-id
    ;; Then: the correct certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.perustiedot.havainnointikayntityyppi-id"
               havainnointikayntityyppi-id]]]))))

;; --- 1.2 ET2026-spesifisellä kentällä haku ei löydä ET2018-todistuksia ---
(t/deftest search-by-havainnointikayntityyppi-id-excludes-et2018-test
  ;; Given: both ET2018 and ET2026 certificates
  (let [{:keys [energiatodistukset-2018 energiatodistukset-2026]} (test-data-set-mixed)
        et2026-id (-> energiatodistukset-2026 keys sort first)
        et2026 (get energiatodistukset-2026 et2026-id)
        havainnointikayntityyppi-id (-> et2026 :perustiedot :havainnointikayntityyppi-id)
        ;; When: searching by havainnointikayntityyppi-id
        results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.perustiedot.havainnointikayntityyppi-id"
                           havainnointikayntityyppi-id]]]
                        nil nil nil)
        result-ids (set (map :id results))
        et2018-ids (set (keys energiatodistukset-2018))]
    ;; Then: only ET2026 certificates are in results
    (t/is (not-empty results)
           "Should find at least one ET2026 certificate")
    (t/is (empty? (set/intersection result-ids et2018-ids))
           "ET2018 certificates should not appear in results")))

;; --- 1.3 Regression: ET2018-kentillä haku toimii edelleen ---
;; NOTE: This is a regression test - MAY pass in red phase.
;; Covered by existing tests: search-by-id-test, search-by-nimi-test, etc.
;; The key verification is that adding Energiatodistus2026 to private-search-schema
;; does not break existing ET2018 field searches.

;; --- 1.4 ET2026-todistus löytyy yhteisillä kentillä haettaessa ---
(t/deftest search-et2026-by-shared-field-lammitetty-nettoala-test
  ;; Given: ET2026 certificate with known lammitetty-nettoala
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)]
    ;; When: searching by energiatodistus.lahtotiedot.lammitetty-nettoala
    ;; Then: the ET2026 certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.lahtotiedot.lammitetty-nettoala"
               nettoala]]]))))

;; --- 1.5 ET2026-todistus rajautuu pois polttoaineet-vuosikulutus-yhteensa-hausta ---
(t/deftest search-polttoaineet-vuosikulutus-yhteensa-excludes-et2026-test
  ;; Given: ET2018 with known polttoaineet-vuosikulutus-yhteensa and ET2026 certificates
  (let [{:keys [energiatodistukset-2018 energiatodistukset-2026]} (test-data-set-mixed)
        et2018-id (-> energiatodistukset-2018 keys sort first)
        et2018 (get energiatodistukset-2018 et2018-id)
        polttoaineet-yht (-> et2018 :toteutunut-ostoenergiankulutus :polttoaineet-vuosikulutus-yhteensa)
        ;; When: searching by polttoaineet-vuosikulutus-yhteensa
        results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.toteutunut-ostoenergiankulutus.polttoaineet-vuosikulutus-yhteensa"
                           polttoaineet-yht]]]
                        nil nil nil)
        result-ids (set (map :id results))
        et2026-ids (set (keys energiatodistukset-2026))]
    ;; Then: only ET2018 is in results (ET2026 doesn't have this field)
    (t/is (contains? result-ids et2018-id)
           "ET2018 certificate should be found")
    (t/is (empty? (set/intersection result-ids et2026-ids))
           "ET2026 certificates should not appear in polttoaineet-vuosikulutus-yhteensa search")))

;; --- 2.1-2.6 Kokonaistuotanto neliövuosikulutus ---
(t/deftest search-by-kokonaistuotanto-aurinkosahko-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto aurinkosahko
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        aurinkosahko (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkosahko)]
    ;; When: searching by aurinkosahko-neliovuosikulutus (= aurinkosahko / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko-neliovuosikulutus"
               (/ aurinkosahko nettoala)]]]))))

(t/deftest search-by-kokonaistuotanto-aurinkolampo-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto aurinkolampo
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        aurinkolampo (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkolampo)]
    ;; When: searching by aurinkolampo-neliovuosikulutus (= aurinkolampo / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo-neliovuosikulutus"
               (/ aurinkolampo nettoala)]]]))))

(t/deftest search-by-kokonaistuotanto-tuulisahko-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto tuulisahko
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        tuulisahko (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :tuulisahko)]
    ;; When: searching by tuulisahko-neliovuosikulutus (= tuulisahko / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko-neliovuosikulutus"
               (/ tuulisahko nettoala)]]]))))

(t/deftest search-by-kokonaistuotanto-lampopumppu-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto lampopumppu
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        lampopumppu (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :lampopumppu)]
    ;; When: searching by lampopumppu-neliovuosikulutus (= lampopumppu / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu-neliovuosikulutus"
               (/ lampopumppu nettoala)]]]))))

(t/deftest search-by-kokonaistuotanto-muulampo-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto muulampo
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        muulampo (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muulampo)]
    ;; When: searching by muulampo-neliovuosikulutus (= muulampo / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo-neliovuosikulutus"
               (/ muulampo nettoala)]]]))))

(t/deftest search-by-kokonaistuotanto-muusahko-neliovuosikulutus-test
  ;; Given: ET2026 certificate with known kokonaistuotanto muusahko
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        muusahko (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muusahko)]
    ;; When: searching by muusahko-neliovuosikulutus (= muusahko / nettoala)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko-neliovuosikulutus"
               (/ muusahko nettoala)]]]))))

;; --- 2.7 Kokonaistuotanto raakakentällä haku ---
(t/deftest search-by-kokonaistuotanto-aurinkosahko-raw-test
  ;; Given: ET2026 certificate with known kokonaistuotanto aurinkosahko value
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        aurinkosahko (-> energiatodistus :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkosahko)]
    ;; When: searching by the raw field (not per-neliö)
    ;; Then: the certificate is found
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko"
               aurinkosahko]]]))))

;; --- 2.8 Kokonaistuotanto-kentällä haku ei löydä ET2018-todistuksia ---
(t/deftest search-by-kokonaistuotanto-excludes-et2018-test
  ;; Given: both ET2018 and ET2026 certificates
  (let [{:keys [energiatodistukset-2018 energiatodistukset-2026]} (test-data-set-mixed)
        et2026-id (-> energiatodistukset-2026 keys sort first)
        et2026 (get energiatodistukset-2026 et2026-id)
        aurinkosahko (-> et2026 :tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkosahko)
        ;; When: searching by kokonaistuotanto field
        results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko"
                           aurinkosahko]]]
                        nil nil nil)
        result-ids (set (map :id results))
        et2018-ids (set (keys energiatodistukset-2018))]
    ;; Then: only ET2026 certificates appear
    (t/is (not-empty results)
           "Should find at least one ET2026 certificate")
    (t/is (empty? (set/intersection result-ids et2018-ids))
           "ET2018 certificates should not appear in kokonaistuotanto search")))

;; --- 3.1 Kasvihuonepäästöt per neliö – ET2026 ---
(t/deftest search-by-kasvihuonepaastot-per-nelio-2026-test
  ;; CO2-kertoimet: kaukolampo=0.059, sahko=0.05, uusiutuva-polttoaine=0.027,
  ;;               fossiilinen-polttoaine=0.306, kaukojaahdytys=0.014
  ;; Given: ET2026 certificate with known kaytettavat-energiamuodot and nettoala
  (let [{:keys [energiatodistukset] :as tds} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        energiamuodot (-> energiatodistus :tulokset :kaytettavat-energiamuodot)
        ;; When: calculating kasvihuonepaastot-per-nelio
        kasvihuonepaastot (/ (+ (* (:kaukolampo energiamuodot) 0.059M)
                               (* (:sahko energiamuodot) 0.05M)
                               (* (:uusiutuva-polttoaine energiamuodot) 0.027M)
                               (* (:fossiilinen-polttoaine energiamuodot) 0.306M)
                               (* (:kaukojaahdytys energiamuodot) 0.014M))
                            nettoala)]
    ;; Then: searching by the calculated value finds the certificate
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
               kasvihuonepaastot]]]))))

;; --- 3.2 Kasvihuonepäästöt per neliö – ET2018 ---
(t/deftest search-by-kasvihuonepaastot-per-nelio-2018-test
  ;; Given: ET2018 certificate with known kaytettavat-energiamuodot and nettoala
  (let [{:keys [energiatodistukset] :as tds} (test-data-set)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        energiamuodot (-> energiatodistus :tulokset :kaytettavat-energiamuodot)
        ;; When: calculating kasvihuonepaastot-per-nelio (same CO2 coefficients)
        kasvihuonepaastot (/ (+ (* (:kaukolampo energiamuodot) 0.059M)
                               (* (:sahko energiamuodot) 0.05M)
                               (* (:uusiutuva-polttoaine energiamuodot) 0.027M)
                               (* (:fossiilinen-polttoaine energiamuodot) 0.306M)
                               (* (:kaukojaahdytys energiamuodot) 0.014M))
                            nettoala)]
    ;; Then: searching by the calculated value finds the certificate
    (t/is (search-and-assert
            tds
            id
            [[["="
               "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
               kasvihuonepaastot]]]))))

;; --- 3.3 Kasvihuonepäästöt per neliö – nolla nettoala ---
(t/deftest search-by-kasvihuonepaastot-per-nelio-zero-nettoala-test
  ;; Given: certificate with nettoala set to 0 after insertion (validation prevents 0 on insert)
  ;; Note: follows same pattern as existing search-by-id-zero-nettoala-test (line 177)
  (let [{:keys [energiatodistukset laatijat]} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        laatija-id (-> laatijat keys sort first)]
    (jdbc/execute!
      ts/*db*
      ["UPDATE energiatodistus SET lt$lammitetty_nettoala = 0 where id = ?" id])
    ;; When: searching by kasvihuonepaastot-per-nelio with any value
    ;; Then: no error occurs and no certificate is found (nullif returns NULL)
    (let [results (search kayttaja-test-data/paakayttaja
                          [[["="
                             "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
                             0]]]
                          nil nil nil)]
      (t/is (not (contains? (set (map :id results)) id))
             "Certificate with zero nettoala should not match kasvihuonepaastot search"))))

;; --- 3.4 Kasvihuonepäästöt per neliö – NULL energiamuotoarvot ---
(t/deftest search-by-kasvihuonepaastot-per-nelio-null-values-test
  ;; Given: certificate where some kaytettavat-energiamuodot values are NULL
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        et-add (-> (energiatodistus-test-data/generate-add 2026 true)
                   (assoc-in [:lahtotiedot :lammitetty-nettoala] 100M)
                   (assoc-in [:tulokset :kaytettavat-energiamuodot :sahko] 50M)
                   (assoc-in [:tulokset :kaytettavat-energiamuodot :kaukolampo] nil)
                   (assoc-in [:tulokset :kaytettavat-energiamuodot :uusiutuva-polttoaine] nil)
                   (assoc-in [:tulokset :kaytettavat-energiamuodot :fossiilinen-polttoaine] nil)
                   (assoc-in [:tulokset :kaytettavat-energiamuodot :kaukojaahdytys] nil))
        et-id (first (energiatodistus-test-data/insert! [et-add] laatija-id))]
    (sign-energiatodistukset! [[laatija-id et-id]])
    ;; When: searching with coalesced calculation (NULL -> 0)
    ;; Then: only sahko contributes: (50 * 0.05) / 100 = 0.025
    (let [expected-value (/ (* 50M 0.05M) 100M)
          results (search kayttaja-test-data/paakayttaja
                          [[["="
                             "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
                             expected-value]]]
                          nil nil nil)]
      (t/is (contains? (set (map :id results)) et-id)
             "Certificate with NULL energiamuoto values should be found with coalesced calculation"))))

;; --- 3.5 Kasvihuonepäästöt per neliö – vertailuoperaattorit ---
(t/deftest search-by-kasvihuonepaastot-per-nelio-comparison-operators-test
  ;; Given: ET2026 certificate with known kasvihuonepaastot-per-nelio value
  (let [{:keys [energiatodistukset]} (test-data-set-2026)
        id (-> energiatodistukset keys sort first)
        energiatodistus (get energiatodistukset id)
        nettoala (-> energiatodistus :lahtotiedot :lammitetty-nettoala)
        energiamuodot (-> energiatodistus :tulokset :kaytettavat-energiamuodot)
        kasvihuonepaastot (/ (+ (* (:kaukolampo energiamuodot) 0.059M)
                               (* (:sahko energiamuodot) 0.05M)
                               (* (:uusiutuva-polttoaine energiamuodot) 0.027M)
                               (* (:fossiilinen-polttoaine energiamuodot) 0.306M)
                               (* (:kaukojaahdytys energiamuodot) 0.014M))
                            nettoala)]
    ;; When/Then: < with smaller value does not find the certificate
    (t/is (not (contains?
                 (->> (search kayttaja-test-data/paakayttaja
                              [[["<"
                                 "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
                                 (- kasvihuonepaastot 1M)]]]
                              nil nil nil)
                      (map :id) set)
                 id))
           "< with smaller value should not find the certificate")
    ;; When/Then: > with larger value does not find the certificate
    (t/is (not (contains?
                 (->> (search kayttaja-test-data/paakayttaja
                              [[[">"
                                 "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
                                 (+ kasvihuonepaastot 1M)]]]
                              nil nil nil)
                      (map :id) set)
                 id))
           "> with larger value should not find the certificate")
    ;; When/Then: > with smaller value finds the certificate
    (t/is (contains?
            (->> (search kayttaja-test-data/paakayttaja
                         [[[">"
                            "energiatodistus.tulokset.kasvihuonepaastot-per-nelio"
                            (- kasvihuonepaastot 1M)]]]
                         nil nil nil)
                 (map :id) set)
            id)
           "> with smaller value should find the certificate")))

;; --- 4.1 Uusiutuvan energian osuus stub palauttaa 0 ---
(t/deftest search-by-uusiutuvan-energian-osuus-stub-zero-test
  ;; Given: signed certificates exist
  (let [{:keys [energiatodistukset]} (test-data-set-2026)
        signed-ids (->> energiatodistukset keys sort (take 2) set)]
    ;; When: searching by uusiutuvan-energian-osuus = 0
    ;; Then: all signed certificates are found (stub returns 0 for all)
    (let [results (search kayttaja-test-data/paakayttaja
                          [[["="
                             "energiatodistus.tulokset.uusiutuvan-energian-osuus"
                             0]]]
                          nil nil nil)
          result-ids (set (map :id results))]
      (t/is (every? #(contains? result-ids %) signed-ids)
             "All signed certificates should match uusiutuvan-energian-osuus = 0"))))

;; --- 4.2 Uusiutuvan energian osuus stub – ei-nolla ei matchaa ---
(t/deftest search-by-uusiutuvan-energian-osuus-stub-nonzero-test
  ;; Given: signed certificates exist
  (test-data-set-2026)
  ;; When: searching by uusiutuvan-energian-osuus = 1
  ;; Then: no certificates are found (stub returns 0, not 1)
  (let [results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.tulokset.uusiutuvan-energian-osuus"
                           1]]]
                        nil nil nil)]
    (t/is (empty? results)
           "No certificates should match uusiutuvan-energian-osuus = 1")))

;; --- 5.1 Boolean: energiankulutuksen-valmius = true ---
(t/deftest search-by-energiankulutuksen-valmius-true-test
  ;; Given: ET2026 with energiankulutuksen-valmius = true, another with false
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        et-true (-> (energiatodistus-test-data/generate-add 2026 true)
                    (assoc-in [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin] true))
        et-false (-> (energiatodistus-test-data/generate-add 2026 true)
                     (assoc-in [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin] false))
        [id-true] (energiatodistus-test-data/insert! [et-true] laatija-id)
        [id-false] (energiatodistus-test-data/insert! [et-false] laatija-id)]
    (sign-energiatodistukset! [[laatija-id id-true] [laatija-id id-false]])
    ;; When: searching = true
    ;; Then: only true-valued certificate found
    (let [results (search kayttaja-test-data/paakayttaja
                          [[["="
                             "energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin"
                             true]]]
                          nil nil nil)
          result-ids (set (map :id results))]
      (t/is (contains? result-ids id-true)
             "Certificate with true should be found")
      (t/is (not (contains? result-ids id-false))
             "Certificate with false should not be found"))))

;; --- 5.2 Boolean: energiankulutuksen-valmius = false includes ET2018 ---
(t/deftest search-by-energiankulutuksen-valmius-false-includes-et2018-test
  ;; Given: ET2018 and ET2026 certificates (ET2018 defaults to false for this field)
  (let [{:keys [energiatodistukset-2018 energiatodistukset-2026]} (test-data-set-mixed)
        et2018-ids (set (keys energiatodistukset-2018))
        ;; When: searching = false
        results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin"
                           false]]]
                        nil nil nil)
        result-ids (set (map :id results))]
    ;; Then: ET2018 certificates appear (default false) plus ET2026 with false
    (t/is (every? #(contains? result-ids %) et2018-ids)
           "ET2018 certificates should appear with default false value")))

;; --- 5.3 Boolean: lammonjako-lampotilajousto = true ---
(t/deftest search-by-lammonjako-lampotilajousto-true-test
  ;; Given: ET2026 with lammonjako-lampotilajousto = true, another with false
  (let [laatija-id (first (keys (laatija-test-data/generate-and-insert! 1)))
        et-true (-> (energiatodistus-test-data/generate-add 2026 true)
                    (assoc-in [:lahtotiedot :lammitys :lammonjako-lampotilajousto] true))
        et-false (-> (energiatodistus-test-data/generate-add 2026 true)
                     (assoc-in [:lahtotiedot :lammitys :lammonjako-lampotilajousto] false))
        [id-true] (energiatodistus-test-data/insert! [et-true] laatija-id)
        [id-false] (energiatodistus-test-data/insert! [et-false] laatija-id)]
    (sign-energiatodistukset! [[laatija-id id-true] [laatija-id id-false]])
    ;; When: searching = true
    ;; Then: only true-valued certificate found
    (let [results (search kayttaja-test-data/paakayttaja
                          [[["="
                             "energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto"
                             true]]]
                          nil nil nil)
          result-ids (set (map :id results))]
      (t/is (contains? result-ids id-true)
             "Certificate with lammonjako-lampotilajousto true should be found")
      (t/is (not (contains? result-ids id-false))
             "Certificate with lammonjako-lampotilajousto false should not be found"))))

;; --- 5.4 Boolean: lammonjako-lampotilajousto = false includes ET2018 ---
(t/deftest search-by-lammonjako-lampotilajousto-false-includes-et2018-test
  ;; Given: ET2018 and ET2026 certificates
  (let [{:keys [energiatodistukset-2018 energiatodistukset-2026]} (test-data-set-mixed)
        et2018-ids (set (keys energiatodistukset-2018))
        ;; When: searching = false
        results (search kayttaja-test-data/paakayttaja
                        [[["="
                           "energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto"
                           false]]]
                        nil nil nil)
        result-ids (set (map :id results))]
    ;; Then: ET2018 certificates appear (default false)
    (t/is (every? #(contains? result-ids %) et2018-ids)
           "ET2018 certificates should appear with default false for lammonjako-lampotilajousto")))
