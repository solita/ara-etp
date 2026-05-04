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
    (t/is (= {:avg 350.00M :percentile-15 175.0 :percentile-85 525.0}
             (service/find-e-luku-statistics ts/*db* query-all 2013)))
    (t/is (= {:avg 300.00M :percentile-15 160.0 :percentile-85 440.0}
             (service/find-e-luku-statistics ts/*db* query-exact 2013)))
    (t/is (= {:avg 950.00M :percentile-15 775.0 :percentile-85 1125.0}
             (service/find-e-luku-statistics ts/*db* query-all 2018)))
    (t/is (= {:avg 900.00M :percentile-15 760.0 :percentile-85 1040.0}
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
                    :uusiutuvat-omavaraisenergiat-counts {2018 nil 2026 nil}})

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
                                        :percentile-15 175.0
                                        :percentile-85 525.0}
                                  2018 {:avg 950.00M
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
                                                    2026 nil}}
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

;; --- find-statistics with mixed allekirjoitusaika test ---

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
