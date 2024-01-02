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

(def empty-results {:counts {2013 nil 2018 nil}
                    :e-luku-statistics {2013 nil 2018 nil}
                    :common-averages nil
                    :uusiutuvat-omavaraisenergiat-counts {2018 nil}})

(t/deftest find-statistics-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (t/is (= {:counts {2013 {:e-luokka {"A" 3 "B" 3}
                             :lammitysmuoto {4 3 2 3}
                             :ilmanvaihto {4 3 2 3}}
                       2018 {:e-luokka {"A" 3 "B" 3}
                             :lammitysmuoto {4 3 2 3}
                             :ilmanvaihto {4 3 2 3}}}
              :e-luku-statistics {2013 {:avg 350.00M
                                        :percentile-15 175.0
                                        :percentile-85 525.0}
                                  2018 {:avg 950.00M
                                        :percentile-15 775.0
                                        :percentile-85 1125.0}}
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
                                                          :muulampo 6}}}
             (service/find-statistics ts/*db* query-all)))
    (t/is (= empty-results (service/find-statistics ts/*db* query-exact)))))

(t/deftest find-statistics-not-signed-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 false)]
    (t/is (= empty-results (service/find-statistics ts/*db* query-all)))))

(t/deftest find-statistics-expired-test
  (let [{:keys [energiatodistukset]} (test-data-set 12 true)]
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET voimassaolo_paattymisaika = now()"])
    (t/is (= empty-results (service/find-statistics ts/*db* query-all)))))
