(ns solita.etp.service.statistics-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.statistics :as service]))

(t/use-fixtures :each ts/fixture)

(defn energiatodistus-adds [n]
  (->> (interleave
        (concat
         (energiatodistus-test-data/generate-adds
          (/ n 2)
          2013
          true)
         (energiatodistus-test-data/generate-adds
          (/ n 2)
          2018
          true))
        (cycle ["00100" "33100"])
        (cycle ["YAT" "KAT"])
        (cycle [2010 2020])
        (cycle [100 200])
        (cycle [0.5 1])
        (cycle [2 4])
        (cycle [0 nil 2 2])
        (cycle [nil 1 1 2])
        (cycle [nil 0 1.5 2.5])
        (cycle [nil 0 2.5 3.5])
        (cycle [nil 0 0.4 0.6])
        (cycle [nil 0 0.2 0.4]))
       (partition 13)
       (map (fn [[add
                  postinumero
                  alakayttotarkoitus-id
                  valmistumisvuosi
                  lammitetty-nettoala
                  numeric-value
                  luokittelu-id
                  takkojen-lkm
                  ilp-lkm
                  tilat-ja-iv-lampokerroin
                  lammin-kayttovesi-lampokerroin
                  iv-lto-vuosihyotysuhde
                  iv-sfp]]
              (-> add
                  (assoc-in [:perustiedot :postinumero] postinumero)
                  (assoc-in [:perustiedot :kayttotarkoitus] alakayttotarkoitus-id)
                  (assoc-in [:perustiedot :valmistumisvuosi] valmistumisvuosi)
                  (assoc-in [:lahtotiedot :lammitetty-nettoala] lammitetty-nettoala)
                  (assoc-in [:lahtotiedot :rakennusvaippa :alapohja :U] numeric-value)
                  (assoc-in [:lahtotiedot :rakennusvaippa :ikkunat :U] numeric-value)
                  (assoc-in [:lahtotiedot :ilmanvaihto :lto-vuosihyotysuhde] iv-lto-vuosihyotysuhde)
                  (assoc-in [:lahtotiedot :ilmanvaihto :ivjarjestelma :sfp] iv-sfp)
                  (assoc-in [:lahtotiedot :ilmanvaihto :tyyppi-id] luokittelu-id)
                  (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] luokittelu-id)
                  (assoc-in [:lahtotiedot :lammitys :takka :maara] takkojen-lkm)
                  (assoc-in [:lahtotiedot :lammitys :ilmalampopumppu :maara] ilp-lkm)
                  (assoc-in [:lahtotiedot :lammitys :tilat-ja-iv :lampokerroin] tilat-ja-iv-lampokerroin)
                  (assoc-in [:lahtotiedot :lammitys :lammin-kayttovesi :lampokerroin] lammin-kayttovesi-lampokerroin))))))

(defn test-data-set [n sign?]
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        postinumerot [00100 33100]
        energiatodistus-adds (energiatodistus-adds n)
        energiatodistus-ids (energiatodistus-test-data/insert!
                             energiatodistus-adds
                             laatija-id)]
    (doseq [[energiatodistus-id e-luokka] (->> (interleave energiatodistus-ids
                                                           (cycle ["A" "B"]))
                                               (partition 2))]
      (when sign?
        (energiatodistus-test-data/sign! energiatodistus-id laatija-id true))
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET t$e_luku = 100 * id, t$e_luokka = ? WHERE id = ?"
                              e-luokka
                              energiatodistus-id]))
    {:energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(def query-all service/default-query)
(def query-exact (assoc service/default-query
                        :keyword
                        "Uusimaa"
                        :alakayttotarkoitus-ids
                        ["YAT"]
                        :valmistumisvuosi-max
                        2019
                        :lammitetty-nettoala-max
                        199))

(t/deftest sufficient-sample-size?-test
  (t/is (false? (service/sufficient-sample-size? {})))
  (t/is (false? (service/sufficient-sample-size? {:e-luokka {"A" 1}})))
  (t/is (false? (service/sufficient-sample-size? {:e-luokka {"A" 1 "C" 2}})))
  (t/is (true? (service/sufficient-sample-size? {:e-luokka {"A" 1 "C" 3}})))
  (t/is (true? (service/sufficient-sample-size? {:e-luokka {"A" 1
                                                            "B" 2
                                                            "F" 2}}))))

(t/deftest find-counts-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (t/is (= {2013 {:e-luokka {"A" 3 "B" 3}
                    :lammitysmuoto {2 3 4 3}
                    :ilmanvaihto {2 3 4 3}}
              2018 {:e-luokka {"A" 3 "B" 3}
                    :lammitysmuoto {2 3 4 3}
                    :ilmanvaihto {2 3 4 3}}}
             (service/find-counts ts/*db* query-all)))
    (t/is (= {2013 {:e-luokka {"A" 3} :lammitysmuoto {2 3} :ilmanvaihto {2 3}}
              2018 {:e-luokka {"A" 3} :lammitysmuoto {2 3} :ilmanvaihto {2 3}}}
             (service/find-counts ts/*db* query-exact)))))

(t/deftest find-e-luku-statistics-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]

    ;; TODO are percentiles meaninful? They are interpolated values.
    (t/is (= {:avg 350.00M :median 350.0 :percentile-15 175.0 :percentile-85 525.0}
             (service/find-e-luku-statistics ts/*db* query-all 2013)))
    (t/is (= {:avg 300.00M :median 300.0 :percentile-15 160.0 :percentile-85 440.0}
             (service/find-e-luku-statistics ts/*db* query-exact 2013)))
    (t/is (= {:avg 950.00M :median 950.0 :percentile-15 775.0 :percentile-85 1125.0}
             (service/find-e-luku-statistics ts/*db* query-all 2018)))
    (t/is (= {:avg 900.00M :median 900.0 :percentile-15 760.0 :percentile-85 1040.0}
             (service/find-e-luku-statistics ts/*db* query-exact 2018)))))

(t/deftest find-common-averages-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)
        common-averages-for-all (service/find-common-averages ts/*db* query-all)
        common-averages-for-exact (service/find-common-averages ts/*db*
                                                                query-exact)]
    (t/is (= 0.75M (:alapohja-u common-averages-for-all)))
    (t/is (= 0.50M (:alapohja-u common-averages-for-exact)))
    (t/is (= 1.0M (:ylapohja-u common-averages-for-all)))
    (t/is (= 1.0M (:ylapohja-u common-averages-for-exact)))
    (t/is (= 0.75M (:ikkunat-u common-averages-for-all)))
    (t/is (= 0.50M (:ikkunat-u common-averages-for-exact)))
    (t/is (= 0.5M (:lto-vuosihyotysuhde common-averages-for-all)))
    (t/is (= 0.4M (:lto-vuosihyotysuhde common-averages-for-exact)))))

(defn uusiutuvat-omavaraisenergiat-counts-result [n]
  {:aurinkolampo n
   :aurinkosahko n
   :tuulisahko n
   :lampopumppu n
   :muusahko n
   :muulampo n})

(t/deftest find-uusiutuvat-omavaraisenergiat-counts
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (t/is (= (uusiutuvat-omavaraisenergiat-counts-result 6)
             (service/find-uusiutuvat-omavaraisenergiat-counts ts/*db*
                                                               query-all
                                                               2018)))
    (t/is (= (uusiutuvat-omavaraisenergiat-counts-result 3)
             (service/find-uusiutuvat-omavaraisenergiat-counts ts/*db*
                                                               query-exact
                                                               2018)))))

(def empty-results {:counts {2013 nil 2018 nil 2026 nil}
                    :e-luku-statistics {2013 nil 2018 nil 2026 nil}
                    :common-averages nil
                    :uusiutuvat-omavaraisenergiat-counts {2018 nil 2026 nil}
                    :elinkaaren-aikaiset-paastot         nil})

(t/deftest find-statistics-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (t/is (= {:counts {2013 {:e-luokka {"A" 3 "B" 3}
                             :lammitysmuoto {4 3 2 3}
                             :ilmanvaihto {4 3 2 3}}
                       2018 {:e-luokka {"A" 3 "B" 3}
                             :lammitysmuoto {4 3 2 3}
                             :ilmanvaihto {4 3 2 3}}
                       2026 nil}
              :e-luku-statistics {2013 {:avg 350.00M
                                        :median 350.0
                                        :percentile-15 175.0
                                        :percentile-85 525.0}
                                  2018 {:avg 950.00M
                                        :median 950.0
                                        :percentile-15 775.0
                                        :percentile-85 1125.0}
                                  2026 nil}
              :common-averages {:alapohja-u 0.75M
                                :ulkoovet-u 1.00M
                                :ylapohja-u 1.00M
                                :ulkoseinat-u 1.00M
                                :ilmalampopumppu 1.0M
                                :ikkunat-u 0.75M
                                :tilat-ja-iv-lampokerroin 2.0M
                                :ilmanvuotoluku 1.0M
                                :ivjarjestelma-sfp 0.3M
                                :takka 1.0M
                                :lammin-kayttovesi-lampokerroin 3.0M
                                :lto-vuosihyotysuhde 0.5M}
              :uusiutuvat-omavaraisenergiat-counts {2018 {:aurinkolampo 6
                                                          :aurinkosahko 6
                                                          :tuulisahko 6
                                                          :lampopumppu 6
                                                          :muusahko 6
                                                          :muulampo 6}
                                                    2026 nil}
              :elinkaaren-aikaiset-paastot         nil}
             (service/find-statistics ts/*db* query-all)))
    (t/is (= empty-results (service/find-statistics ts/*db* query-exact)))))

(t/deftest find-statistics-not-signed-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 false)]
    (t/is (= empty-results (service/find-statistics ts/*db* query-all)))))

(t/deftest find-statistics-expired-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET voimassaolo_paattymisaika = now()"])
    (t/is (= empty-results (service/find-statistics ts/*db* query-all)))))


(t/deftest find-e-luokka-counts-returns-only-e-luokka-test
  ;; Given: signed certificates exist for versio 2013 and 2018
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    ;; When: we call find-e-luokka-counts
    (let [result (service/find-e-luokka-counts ts/*db* query-all)]
      ;; Then: result contains e-luokka counts per versio
      (t/is (= {"A" 3 "B" 3} (get-in result [2013 :e-luokka])))
      (t/is (= {"A" 3 "B" 3} (get-in result [2018 :e-luokka])))
      ;; Then: result does NOT contain lammitysmuoto or ilmanvaihto
      (t/is (nil? (get-in result [2013 :lammitysmuoto])))
      (t/is (nil? (get-in result [2013 :ilmanvaihto])))
      (t/is (nil? (get-in result [2018 :lammitysmuoto])))
      (t/is (nil? (get-in result [2018 :ilmanvaihto]))))))

(t/deftest find-e-luokka-counts-not-affected-by-allekirjoitusaika-test
  ;; Given: signed certificates exist, some with allekirjoitusaika before 2021
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2020-06-15'::timestamp WHERE versio = 2018"])
    ;; When: we call find-e-luokka-counts
    (let [result (service/find-e-luokka-counts ts/*db* query-all)]
      ;; Then: ALL certificates are counted regardless of allekirjoitusaika
      (t/is (= {"A" 3 "B" 3} (get-in result [2013 :e-luokka])))
      (t/is (= {"A" 3 "B" 3} (get-in result [2018 :e-luokka]))))))

;; --- find-lammitys-ilmanvaihto-counts tests ---

(t/deftest find-lammitys-ilmanvaihto-counts-returns-only-lammitys-and-ilmanvaihto-test
  ;; Given: signed certificates exist (allekirjoitusaika = now(), i.e. 2026, which is >= 2021)
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    ;; When: we call find-lammitys-ilmanvaihto-counts
    (let [result (service/find-lammitys-ilmanvaihto-counts ts/*db* query-all)]
      ;; Then: result contains lammitysmuoto and ilmanvaihto counts per versio
      (t/is (= {2 3 4 3} (get-in result [2013 :lammitysmuoto])))
      (t/is (= {2 3 4 3} (get-in result [2013 :ilmanvaihto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :lammitysmuoto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :ilmanvaihto])))
      ;; Then: result does NOT contain e-luokka
      (t/is (nil? (get-in result [2013 :e-luokka])))
      (t/is (nil? (get-in result [2018 :e-luokka]))))))

(t/deftest find-lammitys-ilmanvaihto-counts-excludes-pre-2021-test
  ;; Given: signed certificates exist, ALL with allekirjoitusaika before 2021
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2020-06-15'::timestamp"])
    ;; When: we call find-lammitys-ilmanvaihto-counts
    (let [result (service/find-lammitys-ilmanvaihto-counts ts/*db* query-all)]
      ;; Then: no lammitysmuoto or ilmanvaihto counts are returned
      (t/is (= {} result)))))

(t/deftest find-lammitys-ilmanvaihto-counts-includes-post-2021-only-test
  ;; Given: signed certificates exist, half with allekirjoitusaika before 2021
  ;; and half after (the default now() = 2026)
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    ;; Set versio 2013 certificates to pre-2021 allekirjoitusaika
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2020-06-15'::timestamp WHERE versio = 2013"])
    ;; When: we call find-lammitys-ilmanvaihto-counts
    (let [result (service/find-lammitys-ilmanvaihto-counts ts/*db* query-all)]
      ;; Then: only versio 2018 lammitysmuoto/ilmanvaihto are returned
      ;; (versio 2013 certificates were signed pre-2021, so excluded)
      (t/is (nil? (get-in result [2013 :lammitysmuoto])))
      (t/is (nil? (get-in result [2013 :ilmanvaihto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :lammitysmuoto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :ilmanvaihto]))))))

(t/deftest find-lammitys-ilmanvaihto-counts-boundary-2021-01-01-test
  ;; Given: signed certificates exist, all with allekirjoitusaika exactly on 2021-01-01
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2021-01-01 00:00:00'::timestamp"])
    ;; When: we call find-lammitys-ilmanvaihto-counts
    (let [result (service/find-lammitys-ilmanvaihto-counts ts/*db* query-all)]
      ;; Then: all certificates are included (boundary is inclusive >=)
      (t/is (= {2 3 4 3} (get-in result [2013 :lammitysmuoto])))
      (t/is (= {2 3 4 3} (get-in result [2013 :ilmanvaihto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :lammitysmuoto])))
      (t/is (= {2 3 4 3} (get-in result [2018 :ilmanvaihto]))))))

(t/deftest find-lammitys-ilmanvaihto-counts-boundary-2020-12-31-test
  ;; Given: signed certificates exist, all with allekirjoitusaika just before 2021
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2020-12-31 23:59:59'::timestamp"])
    ;; When: we call find-lammitys-ilmanvaihto-counts
    (let [result (service/find-lammitys-ilmanvaihto-counts ts/*db* query-all)]
      ;; Then: no certificates are included (boundary is exclusive for < 2021)
      (t/is (= {} result)))))

;; --- Elinkaarenaikaiset kasvihuonepaastot averages tests ---

(def hiilijalanjalki-fields
  [:rakennustuotteiden-valmistus
   :kuljetukset-tyomaavaihe
   :rakennustuotteiden-vaihdot
   :energiankaytto
   :purkuvaihe])

(defn make-2026-adds
  "Generate n versio-2026 energiatodistus adds with specified hiilijalanjälki values.
   Each entry in `hj-values` is a map with :rakennus and :rakennuspaikka field maps."
  [n hj-values]
  (let [adds (energiatodistus-test-data/generate-adds n 2026 true)]
    (map (fn [add hj]
           (-> add
               (assoc-in [:perustiedot :postinumero] "00100")
               (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
               (assoc-in [:perustiedot :valmistumisvuosi] 2020)
               (assoc-in [:lahtotiedot :lammitetty-nettoala] 100)
               (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennus]
                         (:rakennus hj))
               (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka]
                         (:rakennuspaikka hj))))
         adds (cycle hj-values))))

(defn carbonfootprint-test-data-set
  "Create a test data set with versio-2026 energiatodistus records.
   Options:
   - :n - number of records (default 4)
   - :hj-values - seq of hiilijalanjälki value maps
   - :sign? - whether to sign (default true)
   - :set-laatimisajankohta? - whether to set laatimisajankohta (default true)
   - :expire? - whether to expire records (default false)"
  [{:keys [n hj-values sign? set-laatimisajankohta? expire?]
    :or   {n 4 sign? true set-laatimisajankohta? true expire? false}}]
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        adds (make-2026-adds n hj-values)
        ids (energiatodistus-test-data/insert! adds laatija-id)]
    ;; Set e_luku and e_luokka so sample size check passes
    (doseq [[id e-luokka] (map vector ids (cycle ["A" "B"]))]
      (when sign?
        (energiatodistus-test-data/sign! id laatija-id true))
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET t$e_luku = 100, t$e_luokka = ? WHERE id = ?"
                              e-luokka id]))
    (when set-laatimisajankohta?
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET is$laatimisajankohta = now() WHERE versio = 2026"]))
    (when-not set-laatimisajankohta?
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET is$laatimisajankohta = NULL WHERE versio = 2026"]))
    (when expire?
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET voimassaolo_paattymisaika = now() WHERE versio = 2026"]))
    {:ids ids :adds adds :laatija-id laatija-id}))

(def known-hj-values
  "4 known hiilijalanjälki value sets for deterministic testing."
  [{:rakennus       {:rakennustuotteiden-valmistus 10.0M
                     :kuljetukset-tyomaavaihe      20.0M
                     :rakennustuotteiden-vaihdot   30.0M
                     :energiankaytto               40.0M
                     :purkuvaihe                   50.0M}
    :rakennuspaikka {:rakennustuotteiden-valmistus 1.0M
                     :kuljetukset-tyomaavaihe      2.0M
                     :rakennustuotteiden-vaihdot   3.0M
                     :energiankaytto               4.0M
                     :purkuvaihe                   5.0M}}
   {:rakennus       {:rakennustuotteiden-valmistus 20.0M
                     :kuljetukset-tyomaavaihe      30.0M
                     :rakennustuotteiden-vaihdot   40.0M
                     :energiankaytto               50.0M
                     :purkuvaihe                   60.0M}
    :rakennuspaikka {:rakennustuotteiden-valmistus 2.0M
                     :kuljetukset-tyomaavaihe      3.0M
                     :rakennustuotteiden-vaihdot   4.0M
                     :energiankaytto               5.0M
                     :purkuvaihe                   6.0M}}
   {:rakennus       {:rakennustuotteiden-valmistus 30.0M
                     :kuljetukset-tyomaavaihe      40.0M
                     :rakennustuotteiden-vaihdot   50.0M
                     :energiankaytto               60.0M
                     :purkuvaihe                   70.0M}
    :rakennuspaikka {:rakennustuotteiden-valmistus 3.0M
                     :kuljetukset-tyomaavaihe      4.0M
                     :rakennustuotteiden-vaihdot   5.0M
                     :energiankaytto               6.0M
                     :purkuvaihe                   7.0M}}
   {:rakennus       {:rakennustuotteiden-valmistus 40.0M
                     :kuljetukset-tyomaavaihe      50.0M
                     :rakennustuotteiden-vaihdot   60.0M
                     :energiankaytto               70.0M
                     :purkuvaihe                   80.0M}
    :rakennuspaikka {:rakennustuotteiden-valmistus 4.0M
                     :kuljetukset-tyomaavaihe      5.0M
                     :rakennustuotteiden-vaihdot   6.0M
                     :energiankaytto               7.0M
                     :purkuvaihe                   8.0M}}])

;; Row sums:
;; rakennus: 150, 200, 250, 300 => avg = 225
;; rakennuspaikka: 15, 20, 25, 30 => avg = 22.5

;; Test 1: find-carbon-footprint returns correct averages with sufficient 2026 data
(t/deftest find-carbon-footprint-correct-averages-test
  ;; Given: 4 signed versio-2026 records with known hiilijalanjälki values
  (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values})
  ;; When: find-carbon-footprint is called with query-all
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns correct averages
    (t/is (some? result))
    (t/is (= 225.0M (:rakennus-avg result)))
    (t/is (= 22.5M (:rakennuspaikka-avg result)))))

;; Test 2: find-carbon-footprint returns nil when no ilmastoselvitys data exists
(t/deftest find-carbon-footprint-no-ilmastoselvitys-test
  ;; Given: only versio 2013/2018 records (the standard test data set)
  (test-data-set 12 true)
  ;; When: find-carbon-footprint is called with query-all
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns nil
    (t/is (nil? result))))

;; Test 3: find-carbon-footprint returns nil when sample size < 4
(t/deftest find-carbon-footprint-insufficient-sample-size-test
  ;; Given: 3 signed versio-2026 records (below minimum sample size of 4)
  (carbonfootprint-test-data-set {:n 3 :hj-values known-hj-values})
  ;; When: find-carbon-footprint is called with query-all
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns nil
    (t/is (nil? result))))

;; Test 4: find-carbon-footprint returns nil for records without laatimisajankohta
(t/deftest find-carbon-footprint-no-laatimisajankohta-test
  ;; Given: 4 versio-2026 records but without laatimisajankohta set
  (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values :set-laatimisajankohta? false})
  ;; When: find-carbon-footprint is called with query-all
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns nil (records without laatimisajankohta should be excluded)
    (t/is (nil? result))))

;; Test 5: find-carbon-footprint handles nil hiilijalanjälki subfields
(t/deftest find-carbon-footprint-nil-subfields-test
  ;; Given: 4 versio-2026 records, some with nil purkuvaihe
  (let [hj-with-nils [{:rakennus       {:rakennustuotteiden-valmistus 10.0M
                                         :kuljetukset-tyomaavaihe      20.0M
                                         :rakennustuotteiden-vaihdot   30.0M
                                         :energiankaytto               40.0M
                                         :purkuvaihe                   nil}
                        :rakennuspaikka {:rakennustuotteiden-valmistus 1.0M
                                         :kuljetukset-tyomaavaihe      2.0M
                                         :rakennustuotteiden-vaihdot   3.0M
                                         :energiankaytto               4.0M
                                         :purkuvaihe                   nil}}
                       {:rakennus       {:rakennustuotteiden-valmistus 20.0M
                                         :kuljetukset-tyomaavaihe      30.0M
                                         :rakennustuotteiden-vaihdot   40.0M
                                         :energiankaytto               50.0M
                                         :purkuvaihe                   60.0M}
                        :rakennuspaikka {:rakennustuotteiden-valmistus 2.0M
                                         :kuljetukset-tyomaavaihe      3.0M
                                         :rakennustuotteiden-vaihdot   4.0M
                                         :energiankaytto               5.0M
                                         :purkuvaihe                   6.0M}}
                       {:rakennus       {:rakennustuotteiden-valmistus 30.0M
                                         :kuljetukset-tyomaavaihe      40.0M
                                         :rakennustuotteiden-vaihdot   50.0M
                                         :energiankaytto               60.0M
                                         :purkuvaihe                   nil}
                        :rakennuspaikka {:rakennustuotteiden-valmistus 3.0M
                                         :kuljetukset-tyomaavaihe      4.0M
                                         :rakennustuotteiden-vaihdot   5.0M
                                         :energiankaytto               6.0M
                                         :purkuvaihe                   nil}}
                       {:rakennus       {:rakennustuotteiden-valmistus 40.0M
                                         :kuljetukset-tyomaavaihe      50.0M
                                         :rakennustuotteiden-vaihdot   60.0M
                                         :energiankaytto               70.0M
                                         :purkuvaihe                   80.0M}
                        :rakennuspaikka {:rakennustuotteiden-valmistus 4.0M
                                         :kuljetukset-tyomaavaihe      5.0M
                                         :rakennustuotteiden-vaihdot   6.0M
                                         :energiankaytto               7.0M
                                         :purkuvaihe                   8.0M}}]]
    (carbonfootprint-test-data-set {:n 4 :hj-values hj-with-nils})
    ;; When: find-carbon-footprint is called
    (let [result (service/find-carbon-footprint ts/*db* query-all)]
      ;; Then: nil fields treated as 0 in sum
      ;; rakennus sums: 100, 200, 180, 300 => avg = 195
      ;; rakennuspaikka sums: 10, 20, 18, 30 => avg = 19.5
      (t/is (some? result))
      (t/is (= 195.0M (:rakennus-avg result)))
      (t/is (= 19.5M (:rakennuspaikka-avg result))))))

;; Test 6: find-carbon-footprint respects query filters
(t/deftest find-carbon-footprint-respects-filters-test
  ;; Given: 8 versio-2026 records split across different postinumero/kayttotarkoitus
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        ;; 4 records with YAT postinumero 00100
        adds-yat (map (fn [add]
                        (-> add
                            (assoc-in [:perustiedot :postinumero] "00100")
                            (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                            (assoc-in [:perustiedot :valmistumisvuosi] 2020)
                            (assoc-in [:lahtotiedot :lammitetty-nettoala] 100)
                            (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennus]
                                      {:rakennustuotteiden-valmistus 10.0M
                                       :kuljetukset-tyomaavaihe      10.0M
                                       :rakennustuotteiden-vaihdot   10.0M
                                       :energiankaytto               10.0M
                                       :purkuvaihe                   10.0M})
                            (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka]
                                      {:rakennustuotteiden-valmistus 1.0M
                                       :kuljetukset-tyomaavaihe      1.0M
                                       :rakennustuotteiden-vaihdot   1.0M
                                       :energiankaytto               1.0M
                                       :purkuvaihe                   1.0M})))
                      (energiatodistus-test-data/generate-adds 4 2026 true))
        ;; 4 records with KAT postinumero 33100
        adds-kat (map (fn [add]
                        (-> add
                            (assoc-in [:perustiedot :postinumero] "33100")
                            (assoc-in [:perustiedot :kayttotarkoitus] "KAT")
                            (assoc-in [:perustiedot :valmistumisvuosi] 2022)
                            (assoc-in [:lahtotiedot :lammitetty-nettoala] 200)
                            (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennus]
                                      {:rakennustuotteiden-valmistus 100.0M
                                       :kuljetukset-tyomaavaihe      100.0M
                                       :rakennustuotteiden-vaihdot   100.0M
                                       :energiankaytto               100.0M
                                       :purkuvaihe                   100.0M})
                            (assoc-in [:ilmastoselvitys :hiilijalanjalki :rakennuspaikka]
                                      {:rakennustuotteiden-valmistus 10.0M
                                       :kuljetukset-tyomaavaihe      10.0M
                                       :rakennustuotteiden-vaihdot   10.0M
                                       :energiankaytto               10.0M
                                       :purkuvaihe                   10.0M})))
                      (energiatodistus-test-data/generate-adds 4 2026 true))
        all-adds (concat adds-yat adds-kat)
        ids (energiatodistus-test-data/insert! all-adds laatija-id)]
    (doseq [[id e-luokka] (map vector ids (cycle ["A" "B"]))]
      (energiatodistus-test-data/sign! id laatija-id true)
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET t$e_luku = 100, t$e_luokka = ? WHERE id = ?"
                              e-luokka id]))
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET is$laatimisajankohta = now() WHERE versio = 2026"])
    ;; When: find-carbon-footprint with filter for YAT and valmistumisvuosi <= 2021
    (let [filtered-query (assoc service/default-query
                                :keyword "Uusimaa"
                                :alakayttotarkoitus-ids ["YAT"]
                                :valmistumisvuosi-max 2021)
          result (service/find-carbon-footprint ts/*db* filtered-query)]
      ;; Then: only YAT records contribute: rakennus sum=50 per row, avg=50; rakennuspaikka sum=5, avg=5
      (t/is (some? result))
      (t/is (= 50.0M (:rakennus-avg result)))
      (t/is (= 5.0M (:rakennuspaikka-avg result))))))

;; Test 7: find-carbon-footprint excludes unsigned records
(t/deftest find-carbon-footprint-excludes-unsigned-test
  ;; Given: 4 versio-2026 records with ilmastoselvitys but NOT signed
  (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values :sign? false})
  ;; When: find-carbon-footprint is called
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns nil (unsigned records excluded)
    (t/is (nil? result))))

;; Test 8: find-carbon-footprint excludes expired records
(t/deftest find-carbon-footprint-excludes-expired-test
  ;; Given: 4 signed versio-2026 records with ilmastoselvitys but all expired
  (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values :expire? true})
  ;; When: find-carbon-footprint is called
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: returns nil
    (t/is (nil? result))))

;; Test 9: find-carbon-footprint only includes versio 2026
(t/deftest find-carbon-footprint-only-versio-2026-test
  ;; Given: mixed dataset with 2013/2018 and 2026 records
  (test-data-set 12 true) ;; creates 6x2013 + 6x2018
  (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values})
  ;; When: find-carbon-footprint is called
  (let [result (service/find-carbon-footprint ts/*db* query-all)]
    ;; Then: only versio-2026 records contribute
    (t/is (some? result))
    (t/is (= 225.0M (:rakennus-avg result)))
    (t/is (= 22.5M (:rakennuspaikka-avg result)))))

;; Test 10: find-statistics includes elinkaaren-aikaiset-paastot in response
(t/deftest find-statistics-includes-elinkaaren-aikaiset-paastot-test
           ;; Given: 4 signed versio-2026 records with ilmastoselvitys data
           (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values})
           ;; When: find-statistics is called
           (let [result (service/find-statistics ts/*db* query-all)]
                ;; Then: result contains :elinkaaren-aikaiset-paastot key with correct values
                (t/is (some? (:elinkaaren-aikaiset-paastot result)))
                (t/is (= 225.0M (get-in result [:elinkaaren-aikaiset-paastot :rakennus-avg])))
                (t/is (= 22.5M (get-in result [:elinkaaren-aikaiset-paastot :rakennuspaikka-avg])))))

;; Test 11: find-statistics returns nil elinkaaren-aikaiset-paastot when insufficient data (regression)
(t/deftest find-statistics-nil-elinkaaren-aikaiset-paastot-test
  ;; Given: only versio-2013/2018 records
  (test-data-set 12 true)
  ;; When: find-statistics is called
  (let [result (service/find-statistics ts/*db* query-all)]
    ;; Then: elinkaaren-aikaiset-paastot is nil and existing assertions still hold
    (t/is (nil? (:elinkaaren-aikaiset-paastot result)))
    ;; Backward compatibility: other keys still present
    (t/is (some? (:counts result)))
    (t/is (some? (:e-luku-statistics result)))))

;; Test 12: find-statistics with not-signed 2026 records returns nil elinkaaren-aikaiset-paastot
(t/deftest find-statistics-unsigned-2026-nil-elinkaaren-aikaiset-paastot-test
           ;; Given: 4 versio-2026 records with ilmastoselvitys but not signed
           (carbonfootprint-test-data-set {:n 4 :hj-values known-hj-values :sign? false})
           ;; When: find-statistics is called
           (let [result (service/find-statistics ts/*db* query-all)]
                ;; Then: elinkaaren-aikaiset-paastot is nil
                (t/is (nil? (:elinkaaren-aikaiset-paastot result)))))

(t/deftest find-statistics-mixed-allekirjoitusaika-test
  ;; Given: signed certificates exist, versio 2013 signed pre-2021, versio 2018 signed post-2021
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = '2020-06-15'::timestamp WHERE versio = 2013"])
    ;; When: we call find-statistics
    (let [result (service/find-statistics ts/*db* query-all)]
      ;; Then: e-luokka counts include ALL certificates (both 2013 and 2018)
      (t/is (= {"A" 3 "B" 3} (get-in result [:counts 2013 :e-luokka])))
      (t/is (= {"A" 3 "B" 3} (get-in result [:counts 2018 :e-luokka])))
      ;; Then: lammitysmuoto/ilmanvaihto counts exclude pre-2021 certificates
      ;; versio 2013 certificates were signed pre-2021, so no lammitysmuoto/ilmanvaihto
      (t/is (nil? (get-in result [:counts 2013 :lammitysmuoto])))
      (t/is (nil? (get-in result [:counts 2013 :ilmanvaihto])))
      ;; versio 2018 certificates were signed post-2021 (now() = 2026), so included
      (t/is (= {4 3 2 3} (get-in result [:counts 2018 :lammitysmuoto])))
      (t/is (= {4 3 2 3} (get-in result [:counts 2018 :ilmanvaihto])))
      ;; Then: e-luku-statistics, common-averages, and uusiutuvat-omavaraisenergiat are unaffected
      (t/is (some? (get-in result [:e-luku-statistics 2013])))
      (t/is (some? (get-in result [:e-luku-statistics 2018])))
      (t/is (some? (:common-averages result)))
      (t/is (some? (get-in result [:uusiutuvat-omavaraisenergiat-counts 2018]))))))


;; Helper: create test data with explicit laatimisvaihe/uudisrakennus values
;; across all three versio values (2013, 2018, 2026).
;; Creates 12 records total:
;;   2013: 4 records — 2 with uudisrakennus=true, 2 with uudisrakennus=false
;;   2018: 4 records — 2 with laatimisvaihe=0 (uudis), 1 with laatimisvaihe=1 (uudis), 1 with laatimisvaihe=2 (oleva)
;;   2026: 4 records — 1 with laatimisvaihe=0 (uudis), 1 with laatimisvaihe=2 (oleva), 1 with laatimisvaihe=3 (oleva), 1 with laatimisvaihe=4 (oleva)
(defn laatimisvaihe-test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        ;; Generate adds for each versio
        adds-2013 (energiatodistus-test-data/generate-adds 4 2013 true)
        adds-2018 (energiatodistus-test-data/generate-adds 4 2018 true)
        adds-2026 (energiatodistus-test-data/generate-adds 4 2026 true)
        ;; Set common fields for all
        set-common (fn [add]
                     (-> add
                         (assoc-in [:perustiedot :postinumero] "00100")
                         (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                         (assoc-in [:perustiedot :valmistumisvuosi] 2020)
                         (assoc-in [:lahtotiedot :lammitetty-nettoala] 100)))
        adds-2013 (map set-common adds-2013)
        adds-2018 (map set-common adds-2018)
        adds-2026 (map set-common adds-2026)
        all-adds (concat adds-2013 adds-2018 adds-2026)
        ids (energiatodistus-test-data/insert! all-adds laatija-id)
        ids-2013 (take 4 ids)
        ids-2018 (take 4 (drop 4 ids))
        ids-2026 (take 4 (drop 8 ids))]
    ;; Sign all and set e_luku/e_luokka
    (doseq [[id e-luokka] (map vector ids (cycle ["A" "B"]))]
      (energiatodistus-test-data/sign! id laatija-id true)
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET t$e_luku = 100 * id, t$e_luokka = ? WHERE id = ?"
                              e-luokka id]))
    ;; Set laatimisvaihe/uudisrakennus values:
    ;; 2013: first 2 uudisrakennus=true, last 2 uudisrakennus=false
    (doseq [id (take 2 ids-2013)]
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$uudisrakennus = true WHERE id = ?" id]))
    (doseq [id (drop 2 ids-2013)]
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$uudisrakennus = false WHERE id = ?" id]))
    ;; 2018: ids 0,1 -> laatimisvaihe 0 (uudis), id 2 -> laatimisvaihe 1 (uudis), id 3 -> laatimisvaihe 2 (oleva)
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" (nth ids-2018 0)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" (nth ids-2018 1)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 1 WHERE id = ?" (nth ids-2018 2)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" (nth ids-2018 3)])
    ;; 2026: id 0 -> laatimisvaihe 0 (uudis), id 1 -> laatimisvaihe 2 (oleva), id 2 -> laatimisvaihe 3 (oleva), id 3 -> laatimisvaihe 4 (oleva)
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" (nth ids-2026 0)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" (nth ids-2026 1)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 3 WHERE id = ?" (nth ids-2026 2)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 4 WHERE id = ?" (nth ids-2026 3)])
    {:ids-2013 ids-2013
     :ids-2018 ids-2018
     :ids-2026 ids-2026
     :laatija-id laatija-id}))

(t/deftest find-counts-laatimisvaihe-uudisrakennus-test
  ;; Given: mixed test data across 2013/2018/2026 with known laatimisvaihe values
  (laatimisvaihe-test-data-set)
  ;; When: find-counts is called with laatimisvaihe=1 (uudisrakennus)
  (let [result (service/find-counts ts/*db* (assoc query-all :laatimisvaihe 1))]
    ;; Then: only uudisrakennus records are counted per version
    ;; 2013: 2 records with uudisrakennus=true (1 A, 1 B)
    (t/is (= 2 (reduce + (vals (get-in result [2013 :e-luokka])))))
    ;; 2018: 3 records with laatimisvaihe IN (0, 1) — ids 0,1 (laatimisvaihe=0) + id 2 (laatimisvaihe=1)
    (t/is (= 3 (reduce + (vals (get-in result [2018 :e-luokka])))))
    ;; 2026: 1 record with laatimisvaihe=0
    (t/is (= 1 (reduce + (vals (get-in result [2026 :e-luokka])))))))

(t/deftest find-counts-laatimisvaihe-olemassa-oleva-test
  ;; Given: mixed test data across 2013/2018/2026 with known laatimisvaihe values
  (laatimisvaihe-test-data-set)
  ;; When: find-counts is called with laatimisvaihe=2 (olemassa oleva rakennus)
  (let [result (service/find-counts ts/*db* (assoc query-all :laatimisvaihe 2))]
    ;; Then: only olemassa oleva rakennus records are counted per version
    ;; 2013: 2 records with uudisrakennus=false
    (t/is (= 2 (reduce + (vals (get-in result [2013 :e-luokka])))))
    ;; 2018: 1 record with laatimisvaihe=2
    (t/is (= 1 (reduce + (vals (get-in result [2018 :e-luokka])))))
    ;; 2026: 3 records with laatimisvaihe IN (2, 3, 4)
    (t/is (= 3 (reduce + (vals (get-in result [2026 :e-luokka])))))))

(t/deftest find-counts-laatimisvaihe-nil-returns-all-test
  ;; Given: mixed test data
  (laatimisvaihe-test-data-set)
  ;; When: find-counts is called with laatimisvaihe=nil (kaikki)
  (let [result (service/find-counts ts/*db* (assoc query-all :laatimisvaihe nil))]
    ;; Then: all records are counted — same as no filter
    (t/is (= 4 (reduce + (vals (get-in result [2013 :e-luokka])))))
    (t/is (= 4 (reduce + (vals (get-in result [2018 :e-luokka])))))
    (t/is (= 4 (reduce + (vals (get-in result [2026 :e-luokka])))))))

(t/deftest find-e-luku-statistics-laatimisvaihe-uudisrakennus-2013-test
  ;; Given: versio 2013 records with 2 uudisrakennus=true and 2 uudisrakennus=false
  (let [{:keys [ids-2013]} (laatimisvaihe-test-data-set)
        uudis-ids (take 2 ids-2013)]
    ;; When: find-e-luku-statistics called with laatimisvaihe=1 for versio 2013
    (let [result (service/find-e-luku-statistics ts/*db* (assoc query-all :laatimisvaihe 1) 2013)]
      ;; Then: statistics computed only from uudisrakennus=true records
      ;; e_luku = 100 * id for each, so avg should reflect only the uudis ids
      (t/is (some? result))
      (let [expected-avg (/ (reduce + (map #(* 100 %) uudis-ids)) 2)]
        (t/is (= (bigdec expected-avg) (:avg result)))))))

(t/deftest find-e-luku-statistics-laatimisvaihe-olemassa-oleva-2018-test
  ;; Given: versio 2018 records — 1 with laatimisvaihe=2 (olemassa oleva)
  (let [{:keys [ids-2018]} (laatimisvaihe-test-data-set)
        oleva-id (nth ids-2018 3)]
    ;; When: find-e-luku-statistics called with laatimisvaihe=2 for versio 2018
    (let [result (service/find-e-luku-statistics ts/*db* (assoc query-all :laatimisvaihe 2) 2018)]
      ;; Then: statistics computed only from laatimisvaihe=2 record
      (t/is (some? result))
      (t/is (= (bigdec (* 100 oleva-id)) (:avg result))))))

(t/deftest find-e-luku-statistics-laatimisvaihe-uudisrakennus-2026-test
  ;; Given: versio 2026 records — 1 with laatimisvaihe=0 (uudisrakennus)
  (let [{:keys [ids-2026]} (laatimisvaihe-test-data-set)
        uudis-id (nth ids-2026 0)]
    ;; When: find-e-luku-statistics called with laatimisvaihe=1 for versio 2026
    (let [result (service/find-e-luku-statistics ts/*db* (assoc query-all :laatimisvaihe 1) 2026)]
      ;; Then: statistics computed only from laatimisvaihe IN (0,1) records
      (t/is (some? result))
      (t/is (= (bigdec (* 100 uudis-id)) (:avg result))))))

(t/deftest find-e-luku-statistics-laatimisvaihe-olemassa-oleva-2026-includes-perusparannus-test
  ;; Given: versio 2026 records — laatimisvaihe 2, 3, 4 are all "olemassa oleva"
  (let [{:keys [ids-2026]} (laatimisvaihe-test-data-set)
        oleva-ids (rest ids-2026)] ;; ids at positions 1,2,3 have laatimisvaihe 2,3,4
    ;; When: find-e-luku-statistics called with laatimisvaihe=2 for versio 2026
    (let [result (service/find-e-luku-statistics ts/*db* (assoc query-all :laatimisvaihe 2) 2026)]
      ;; Then: statistics computed from laatimisvaihe IN (2, 3, 4) — all 3 records
      (t/is (some? result))
      (let [expected-avg (/ (reduce + (map #(* 100 %) oleva-ids)) 3)]
        (t/is (= (bigdec expected-avg) (:avg result)))))))

(t/deftest find-statistics-laatimisvaihe-uudisrakennus-full-test
  ;; Given: mixed test data with known laatimisvaihe values
  (laatimisvaihe-test-data-set)
  ;; When: find-statistics called with laatimisvaihe=1 (uudisrakennus)
  (let [result (service/find-statistics ts/*db* (assoc query-all :laatimisvaihe 1))]
    ;; Then: counts reflect only uudisrakennus records
    ;; 2013: 2 records — below min sample size (4), so nil
    (t/is (nil? (get-in result [:counts 2013])))
    ;; 2018: 3 records (laatimisvaihe 0, 0, 1) — below min sample size, so nil
    (t/is (nil? (get-in result [:counts 2018])))
    ;; 2026: 1 record (laatimisvaihe 0) — below min sample size, so nil
    (t/is (nil? (get-in result [:counts 2026])))))

(t/deftest find-statistics-laatimisvaihe-olemassa-oleva-full-test
  ;; Given: mixed test data with known laatimisvaihe values
  (laatimisvaihe-test-data-set)
  ;; When: find-statistics called with laatimisvaihe=2 (olemassa oleva rakennus)
  (let [result (service/find-statistics ts/*db* (assoc query-all :laatimisvaihe 2))]
    ;; Then: counts reflect only olemassa oleva records
    ;; 2013: 2 records — below min sample size, so nil
    (t/is (nil? (get-in result [:counts 2013])))
    ;; 2018: 1 record — below min sample size, so nil
    (t/is (nil? (get-in result [:counts 2018])))
    ;; 2026: 3 records — below min sample size, so nil
    (t/is (nil? (get-in result [:counts 2026])))))

(t/deftest find-statistics-laatimisvaihe-combined-with-versio-test
  ;; Given: mixed test data
  (laatimisvaihe-test-data-set)
  ;; When: find-statistics called with both laatimisvaihe=1 and versio=2018
  (let [result (service/find-statistics ts/*db* (assoc query-all :laatimisvaihe 1 :versio 2018))]
    ;; Then: only versio 2018 uudisrakennus records contribute
    ;; 2018 has 3 uudisrakennus records (laatimisvaihe 0, 0, 1) — below min sample size so nil
    (t/is (nil? (get-in result [:counts 2018])))
    ;; Other versions should be nil (versio filter excludes them)
    (t/is (nil? (get-in result [:counts 2013])))
    (t/is (nil? (get-in result [:counts 2026])))))

(t/deftest find-carbon-footprint-laatimisvaihe-uudisrakennus-test
  ;; Given: versio 2026 records with ilmastoselvitys and known laatimisvaihe values
  (let [hj-values [{:rakennus       {:rakennustuotteiden-valmistus 10.0M
                                      :kuljetukset-tyomaavaihe      10.0M
                                      :rakennustuotteiden-vaihdot   10.0M
                                      :energiankaytto               10.0M
                                      :purkuvaihe                   10.0M}
                     :rakennuspaikka {:rakennustuotteiden-valmistus 1.0M
                                      :kuljetukset-tyomaavaihe      1.0M
                                      :rakennustuotteiden-vaihdot   1.0M
                                      :energiankaytto               1.0M
                                      :purkuvaihe                   1.0M}}]
        ;; Create 5 records: 4 uudis (laatimisvaihe=0) + 1 oleva (laatimisvaihe=2)
        {:keys [ids]} (carbonfootprint-test-data-set {:n 5 :hj-values hj-values})]
    ;; Set laatimisvaihe: first 4 -> uudis(0), last 1 -> oleva(2)
    (doseq [id (take 4 ids)]
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" id]))
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" (last ids)])
    ;; When: find-carbon-footprint called with laatimisvaihe=1 (uudisrakennus)
    (let [result (service/find-carbon-footprint ts/*db* (assoc query-all :laatimisvaihe 1))]
      ;; Then: only the 4 uudis records are included
      ;; Each rakennus sum = 50, avg = 50; each rakennuspaikka sum = 5, avg = 5
      (t/is (some? result))
      (t/is (= 50.0M (:rakennus-avg result)))
      (t/is (= 5.0M (:rakennuspaikka-avg result))))))

(t/deftest find-carbon-footprint-laatimisvaihe-olemassa-oleva-includes-perusparannus-test
  ;; Given: versio 2026 records with laatimisvaihe 2, 3, 4 (all olemassa oleva)
  (let [hj-uudis {:rakennus       {:rakennustuotteiden-valmistus 100.0M
                                    :kuljetukset-tyomaavaihe      100.0M
                                    :rakennustuotteiden-vaihdot   100.0M
                                    :energiankaytto               100.0M
                                    :purkuvaihe                   100.0M}
                   :rakennuspaikka {:rakennustuotteiden-valmistus 10.0M
                                    :kuljetukset-tyomaavaihe      10.0M
                                    :rakennustuotteiden-vaihdot   10.0M
                                    :energiankaytto               10.0M
                                    :purkuvaihe                   10.0M}}
        hj-oleva {:rakennus       {:rakennustuotteiden-valmistus 10.0M
                                    :kuljetukset-tyomaavaihe      10.0M
                                    :rakennustuotteiden-vaihdot   10.0M
                                    :energiankaytto               10.0M
                                    :purkuvaihe                   10.0M}
                   :rakennuspaikka {:rakennustuotteiden-valmistus 1.0M
                                    :kuljetukset-tyomaavaihe      1.0M
                                    :rakennustuotteiden-vaihdot   1.0M
                                    :energiankaytto               1.0M
                                    :purkuvaihe                   1.0M}}
        ;; 5 records: 1 uudis(0) + 4 oleva(2,3,4,2)
        {:keys [ids]} (carbonfootprint-test-data-set {:n 5 :hj-values [hj-uudis hj-oleva hj-oleva hj-oleva hj-oleva]})]
    ;; Set laatimisvaihe
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" (nth ids 0)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" (nth ids 1)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 3 WHERE id = ?" (nth ids 2)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 4 WHERE id = ?" (nth ids 3)])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" (nth ids 4)])
    ;; When: find-carbon-footprint called with laatimisvaihe=2 (olemassa oleva)
    (let [result (service/find-carbon-footprint ts/*db* (assoc query-all :laatimisvaihe 2))]
      ;; Then: 4 oleva records are included (laatimisvaihe 2, 3, 4, 2)
      ;; Each rakennus sum = 50, avg = 50; each rakennuspaikka sum = 5, avg = 5
      (t/is (some? result))
      (t/is (= 50.0M (:rakennus-avg result)))
      (t/is (= 5.0M (:rakennuspaikka-avg result))))))

(t/deftest find-carbon-footprint-laatimisvaihe-insufficient-sample-test
  ;; Given: 5 versio 2026 records, but only 3 have laatimisvaihe=0 (uudisrakennus)
  (let [hj-values [{:rakennus       {:rakennustuotteiden-valmistus 10.0M
                                      :kuljetukset-tyomaavaihe      10.0M
                                      :rakennustuotteiden-vaihdot   10.0M
                                      :energiankaytto               10.0M
                                      :purkuvaihe                   10.0M}
                     :rakennuspaikka {:rakennustuotteiden-valmistus 1.0M
                                      :kuljetukset-tyomaavaihe      1.0M
                                      :rakennustuotteiden-vaihdot   1.0M
                                      :energiankaytto               1.0M
                                      :purkuvaihe                   1.0M}}]
        {:keys [ids]} (carbonfootprint-test-data-set {:n 5 :hj-values hj-values})]
    ;; 3 uudis + 2 oleva
    (doseq [id (take 3 ids)]
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 0 WHERE id = ?" id]))
    (doseq [id (drop 3 ids)]
      (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET pt$laatimisvaihe = 2 WHERE id = ?" id]))
    ;; When: find-carbon-footprint called with laatimisvaihe=1 (uudisrakennus)
    (let [result (service/find-carbon-footprint ts/*db* (assoc query-all :laatimisvaihe 1))]
      ;; Then: only 3 records match, below min-sample-size of 4 -> nil
      (t/is (nil? result)))))


(t/deftest find-counts-laatimisvaihe-nil-regression-test
  ;; Given: existing test data (no explicit laatimisvaihe set)
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    ;; When: find-counts called with laatimisvaihe=nil (default, no filter)
    (let [result (service/find-counts ts/*db* (assoc query-all :laatimisvaihe nil))]
      ;; Then: results identical to existing test — all records included
      (t/is (= {2013 {:e-luokka {"A" 3 "B" 3}
                      :lammitysmuoto {2 3 4 3}
                      :ilmanvaihto {2 3 4 3}}
                2018 {:e-luokka {"A" 3 "B" 3}
                      :lammitysmuoto {2 3 4 3}
                      :ilmanvaihto {2 3 4 3}}}
               result)))))

(t/deftest find-statistics-laatimisvaihe-nil-regression-test
  ;; Given: existing test data (no explicit laatimisvaihe set)
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    ;; When: find-statistics called with laatimisvaihe=nil
    (let [result-with-nil (service/find-statistics ts/*db* (assoc query-all :laatimisvaihe nil))
          result-without  (service/find-statistics ts/*db* query-all)]
      ;; Then: results identical to calling without the parameter
      (t/is (= result-with-nil result-without)))))
