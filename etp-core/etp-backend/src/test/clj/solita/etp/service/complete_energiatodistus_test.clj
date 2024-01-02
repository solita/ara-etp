(ns solita.etp.service.complete-energiatodistus-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [solita.common.formats :as formats]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.complete-energiatodistus :as service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistus-adds (concat (energiatodistus-test-data/generate-adds
                                      49
                                      2013
                                      true)
                                     (energiatodistus-test-data/generate-adds
                                      49
                                      2018
                                      true)
                                     (energiatodistus-test-data/generate-adds-with-zeros
                                      1
                                      2013)
                                     (energiatodistus-test-data/generate-adds-with-zeros
                                      1
                                      2018))
        energiatodistus-ids (energiatodistus-test-data/insert! energiatodistus-adds
                                                               laatija-id)]
    {:laatijat laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest safe-div-test
  (t/is (= 10 (service/safe-div 20 2)))
  (t/is (nil? (service/safe-div 20 nil)))
  (t/is (nil? (service/safe-div nil 10)))
  (t/is (nil? (service/safe-div 20 0))))

(defn assert-complete-energiatoditus [complete-energiatodistus]
  (let [{:keys [kaukolampo]
         :as kaytettavat-energiamuodot} (-> complete-energiatodistus
                                            :tulokset
                                            :kaytettavat-energiamuodot)
        ilmanvaihto (-> complete-energiatodistus :lahtotiedot :ilmanvaihto)
        paaiv (:paaiv ilmanvaihto)
        erillispoistot (:erillispoistot ilmanvaihto)]
    (and
     (or (nil? kaukolampo)
      (= (:kaukolampo-kertoimella kaytettavat-energiamuodot)
         (* (if (= (:versio complete-energiatodistus) 2013) 0.7M 0.5M)
            kaukolampo)))
     (= (:tulo-poisto paaiv)
        (str (formats/format-number (:tulo paaiv) 3 false)
             " / "
             (formats/format-number (:poisto paaiv) 3 false)))
     (= (:tulo-poisto erillispoistot)
        (str (formats/format-number (:tulo erillispoistot) 3 false)
             " / "
             (formats/format-number (:poisto erillispoistot) 3 false)))
     (-> complete-energiatodistus
         :perustiedot
         keys
         set
         (contains? :kayttotarkoitus))
     (-> complete-energiatodistus
         :perustiedot
         keys
         set
         (contains? :paakayttotarkoitus-id)))))

(t/deftest complete-energiatodistus-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        luokittelut (service/luokittelut ts/*db*)]
    (doseq [id (keys energiatodistukset)]
      (t/is (assert-complete-energiatoditus
             (service/complete-energiatodistus
              (energiatodistus-service/find-energiatodistus ts/*db* id)
              luokittelut))))))

(t/deftest complete-energiatodistus-with-null-nettoala-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        luokittelut (service/luokittelut ts/*db*)
        id (-> energiatodistukset keys sort first)]
    (jdbc/execute!
     ts/*db*
     ["UPDATE energiatodistus SET lt$lammitetty_nettoala = NULL where id = ?" id])
    (t/is (assert-complete-energiatoditus
           (service/complete-energiatodistus
            (energiatodistus-service/find-energiatodistus ts/*db* id)
            luokittelut)))))

(t/deftest find-complete-energiatodistus-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (assert-complete-energiatoditus
           (service/find-complete-energiatodistus ts/*db* id)))
    (t/is (assert-complete-energiatoditus
           (service/find-complete-energiatodistus
            ts/*db*
            {:id (-> laatijat keys sort first) :rooli 0}
            id)))
    (t/is (nil? (service/find-complete-energiatodistus ts/*db* -1)))))

(t/deftest find-complete-energiatodistus-no-permissions-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        id (-> energiatodistukset keys sort first)]
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:id -100 :rooli 0} id))
             {:type :forbidden}))
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:rooli 2} id))
             {:type :forbidden}))
    (t/is (= (etp-test/catch-ex-data
              #(service/find-complete-energiatodistus ts/*db* {:rooli 3} id))
             {:type :forbidden}))))
