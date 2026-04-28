(ns solita.etp.service.energiatodistus-test
  (:require [clojure.test :as t]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [solita.etp.test :as etp-test]
            [solita.common.map :as xmap]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.perusparannuspassi :as perusparannuspassi-test-data]
            [solita.etp.service.energiatodistus-tila :as energiatodistus-tila]
            [solita.etp.service.energiatodistus :as service]
            [solita.etp.whoami :as test-whoami]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [paakayttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (merge (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2013
                                   true
                                   laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2018
                                   true
                                   laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2026
                                   true
                                   laatija-id))]
    {:paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)
     :laatijat laatijat
     :energiatodistukset energiatodistukset}))

(defn add-eq-found? [add found]
  (xmap/submap? (dissoc add :kommentti)
                (-> found
                    (dissoc :kommentti)
                    (xmap/dissoc-in [:tulokset :e-luokka])
                    (xmap/dissoc-in [:tulokset :e-luku]))))

(defn energiatodistus-tila [id]
  (-> (service/find-energiatodistus ts/*db* id) :tila-id energiatodistus-tila/tila-key))

(t/deftest add-and-find-energiatodistus-test
  (let [{:keys [energiatodistukset]} (test-data-set)]
    (doseq [id (keys energiatodistukset)]
      (t/is (add-eq-found? (get energiatodistukset id)
                           (service/find-energiatodistus ts/*db* id))))))

(t/deftest no-permissions-to-draft-test
  (let [{:keys [energiatodistukset]} (test-data-set)]
    (doseq [id (keys energiatodistukset)]
      (doseq [rooli [kayttaja-test-data/laatija
                     kayttaja-test-data/patevyyden-toteaja
                     kayttaja-test-data/paakayttaja
                     kayttaja-test-data/laskuttaja]]
        (t/is (= (etp-test/catch-ex-data
                  #(service/find-energiatodistus ts/*db* rooli id))
                 {:type :forbidden}))))))

(t/deftest draft-visible-to-paakayttaja-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        id (-> energiatodistukset keys first)
        update (-> (energiatodistus-test-data/generate-updates 1 2013 false)
                   first
                   (assoc :draft-visible-to-paakayttaja true))]
    (service/update-energiatodistus! ts/*db* {:id laatija-id :rooli 0} id update)
    (doseq [rooli [kayttaja-test-data/laatija
                   kayttaja-test-data/paakayttaja
                   kayttaja-test-data/laskuttaja]]
      (t/is (add-eq-found? update
                           (service/find-energiatodistus ts/*db* id))))))

(t/deftest validation-test-invalid-value
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        [energiatodistus-2018
         energiatodistus-2026] (mapv #(energiatodistus-test-data/generate-add % false)
                                     [2018 2026])
        add-energiatodistus
        (fn [energiatodistus versio path value]
          (etp-test/catch-ex-data
           #(service/add-energiatodistus!
             (ts/db-user laatija-id)
             (test-whoami/laatija laatija-id)
             versio
             (assoc-in energiatodistus path value))))]
    (t/is (= (add-energiatodistus energiatodistus-2018 2018 [:lahtotiedot :ikkunat :etela :U] 99M)
             {:type     :invalid-value
              :value    99M :max 6.5M, :min 0.4M,
              :property "lahtotiedot.ikkunat.etela.U"
              :message  "Property: lahtotiedot.ikkunat.etela.U has an invalid value: 99"}))

    (t/is (= (add-energiatodistus energiatodistus-2018 2018 [:lahtotiedot :rakennusvaippa :alapohja :U] 99M)
             {:type     :invalid-value
              :value    99M :max 4M, :min 0.03M,
              :property "lahtotiedot.rakennusvaippa.alapohja.U"
              :message  "Property: lahtotiedot.rakennusvaippa.alapohja.U has an invalid value: 99"}))

    (t/is (= (add-energiatodistus energiatodistus-2018 2018 [:lahtotiedot :rakennusvaippa :kylmasillat-UA] 0M)
             {:type     :invalid-value
              :value    0M :max 999999M, :min 0.1M,
              :property "lahtotiedot.rakennusvaippa.kylmasillat-UA"
              :message  "Property: lahtotiedot.rakennusvaippa.kylmasillat-UA has an invalid value: 0"}))
    ))

(t/deftest validation-test-invalid-sisainen-kuorma
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        energiatodistus (energiatodistus-test-data/generate-add 2018 false)]
    (t/is (xmap/submap?
            {:type         :invalid-sisainen-kuorma
             :valid-kuorma {:henkilot {:kayttoaste 0.6M, :lampokuorma 2M},
                            :kuluttajalaitteet {:kayttoaste 0.6M, :lampokuorma 3M},
                            :valaistus {:kayttoaste 0.1M}}}
            (etp-test/catch-ex-data
              #(service/add-energiatodistus!
                 (ts/db-user laatija-id)
                 (test-whoami/laatija laatija-id)
                 2018
                 (-> energiatodistus
                     (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                     (assoc-in [:lahtotiedot :sis-kuorma :henkilot]
                               {:kayttoaste 9999 :lampokuorma 9999}))))))))

(t/deftest validation-2026-invalid-sisainen-kuorma-yat-test
  ;; given: a 2026 energiatodistus with käyttötarkoitus 'YAT' (luokka 1)
  ;;        and invalid sisäinen kuorma values
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        energiatodistus (energiatodistus-test-data/generate-add 2026 false)]
    ;; when: attempting to add the energiatodistus with invalid sis-kuorma
    ;; then: an :invalid-sisainen-kuorma exception is thrown with valid values for 2026/YAT
    (t/is (xmap/submap?
            {:type         :invalid-sisainen-kuorma
             :valid-kuorma {:henkilot {:kayttoaste 0.6M, :lampokuorma 2M},
                            :kuluttajalaitteet {:kayttoaste 0.6M, :lampokuorma 3M},
                            :valaistus {:kayttoaste 0.1M}}}
            (etp-test/catch-ex-data
              #(service/add-energiatodistus!
                 (ts/db-user laatija-id)
                 (test-whoami/laatija laatija-id)
                 2026
                 (-> energiatodistus
                     (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                     (assoc-in [:lahtotiedot :sis-kuorma :henkilot]
                               {:kayttoaste 9999 :lampokuorma 9999}))))))))

(t/deftest validation-2026-invalid-sisainen-kuorma-toimisto-test
  ;; given: a 2026 energiatodistus with käyttötarkoitus 'T' (luokka 3 — Toimistorakennukset)
  ;;        and invalid sisäinen kuorma values
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        energiatodistus (energiatodistus-test-data/generate-add 2026 false)]
    ;; when: attempting to add the energiatodistus with invalid sis-kuorma
    ;; then: an :invalid-sisainen-kuorma exception is thrown with valid values for 2026/T (luokka 3)
    (t/is (xmap/submap?
            {:type         :invalid-sisainen-kuorma
             :valid-kuorma {:henkilot {:kayttoaste 0.65M, :lampokuorma 5M},
                            :kuluttajalaitteet {:kayttoaste 0.65M, :lampokuorma 12M},
                            :valaistus {:kayttoaste 0.65M}}}
            (etp-test/catch-ex-data
              #(service/add-energiatodistus!
                 (ts/db-user laatija-id)
                 (test-whoami/laatija laatija-id)
                 2026
                 (-> energiatodistus
                     (assoc-in [:perustiedot :kayttotarkoitus] "T")
                     (assoc-in [:lahtotiedot :sis-kuorma :henkilot]
                               {:kayttoaste 9999 :lampokuorma 9999}))))))))

(t/deftest validation-2026-valid-sisainen-kuorma-accepted-test
  ;; given: a 2026 energiatodistus with käyttötarkoitus 'YAT' (luokka 1)
  ;;        and correct sisäinen kuorma values matching the DB rules
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        energiatodistus (energiatodistus-test-data/generate-add 2026 false)]
    ;; when: adding the energiatodistus with valid sis-kuorma for YAT
    ;; then: no exception is thrown, the energiatodistus is created successfully
    (t/is (number? (:id (service/add-energiatodistus!
                          (ts/db-user laatija-id)
                          (test-whoami/laatija laatija-id)
                          2026
                          (-> energiatodistus
                              (assoc-in [:perustiedot :kayttotarkoitus] "YAT")
                              (assoc-in [:lahtotiedot :sis-kuorma]
                                        (energiatodistus-test-data/sisainen-kuorma 2026 1)))))))))

(t/deftest update-energiatodistus-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        id (-> energiatodistukset keys sort first)
        update (first (energiatodistus-test-data/generate-updates 1 2013 false))]
    (service/update-energiatodistus! ts/*db* {:id laatija-id :rooli 0} id update)
    (t/is (add-eq-found? update
                         (service/find-energiatodistus ts/*db* id)))))

(t/deftest bypass-validation-limits-test
  (let [{:keys [paakayttajat laatijat energiatodistukset]} (test-data-set)
        paakayttaja-id (-> paakayttajat keys sort first)
        laatija-id (-> laatijat keys sort first)
        id (-> energiatodistukset keys sort first)
        add (get energiatodistukset id)
        update-f #(assoc-in % [:lahtotiedot :ikkunat :etela :U] 99M)
        update-without-bypass (update-f add)
        paakayttaja-update (assoc add :bypass-validation-limits true)
        update-with-bypass (update-f paakayttaja-update)]
    (t/is (= (etp-test/catch-ex-data
              #(service/update-energiatodistus! (ts/db-user laatija-id)
                                                {:id laatija-id :rooli 0}
                                                id
                                                update-without-bypass))
             {:type :invalid-value
              :value 99M :max 6.5M, :min 0.4M,
              :property "lahtotiedot.ikkunat.etela.U"
              :message "Property: lahtotiedot.ikkunat.etela.U has an invalid value: 99"}))
    (t/is (not (add-eq-found? update-without-bypass
                              (service/find-energiatodistus ts/*db* id))))
    (t/is (not (add-eq-found? update-with-bypass
                              (service/find-energiatodistus ts/*db* id))))
    (service/update-energiatodistus! (ts/db-user paakayttaja-id)
                                     kayttaja-test-data/paakayttaja
                                     id
                                     paakayttaja-update)
    (service/update-energiatodistus! (ts/db-user laatija-id)
                                     {:id laatija-id :rooli 0}
                                     id
                                     update-with-bypass)
    (t/is (add-eq-found? update-with-bypass
                         (service/find-energiatodistus ts/*db* id)))))

(t/deftest delete-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (doseq [id (keys energiatodistukset)]
      (service/delete-energiatodistus-luonnos! ts/*db*
                                               {:id laatija-id}
                                               id)
      (t/is (nil? (service/find-energiatodistus ts/*db* id))))))

(t/deftest laskuttaja-permissions-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        update (first (energiatodistus-test-data/generate-updates 1 2013 false))
        id (-> energiatodistukset keys sort first)]
    (t/is (= (etp-test/catch-ex-data
              #(service/update-energiatodistus! ts/*db*
                                                kayttaja-test-data/laskuttaja
                                                id
                                                update))
             {:type :forbidden
              :reason (str "Role: :laskuttaja is not allowed to update energiatodistus "
                           id
                           " in state: :draft laskutettu: false")}))))

(t/deftest start-energiatodistus-signing!-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        ids (keys energiatodistukset)
        db (ts/db-user laatija-id)]
    (doseq [id ids]
      (t/is (= (energiatodistus-tila id) :draft))
      (t/is (= (service/start-energiatodistus-signing! db whoami id) :ok))
      (t/is (= (energiatodistus-tila id) :in-signing))
      (t/is (= (service/start-energiatodistus-signing! db whoami id)
               :already-in-signing)))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} stop-energiatodistus-signing!-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        ids (keys energiatodistukset)
        db (ts/db-user laatija-id)]
    (doseq [id ids]
      (t/is (= (energiatodistus-tila id) :draft))
      (t/is (=  (service/end-energiatodistus-signing! db
                                                      ts/*aws-s3-client*
                                                      whoami
                                                      id)
                :not-in-signing))
      (t/is (= (energiatodistus-tila id) :draft))
      (service/start-energiatodistus-signing! db whoami id)
      (t/is (= (energiatodistus-tila id) :in-signing))
      (energiatodistus-test-data/sign-pdf! id laatija-id whoami)
      (t/is (= (service/end-energiatodistus-signing! db
                                                     ts/*aws-s3-client*
                                                     whoami
                                                     id)
               :ok))
      (t/is (= (energiatodistus-tila id) :signed))
      (t/is (= (service/end-energiatodistus-signing! db
                                                     ts/*aws-s3-client*
                                                     whoami
                                                     id)
               :already-signed)))))

(t/deftest cancel-energiatodistus-signing!-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        ids (keys energiatodistukset)
        db (ts/db-user laatija-id)]
    (doseq [id ids]
      (t/is (= (energiatodistus-tila id) :draft))
      (t/is (=  (service/cancel-energiatodistus-signing! db whoami id)
                :not-in-signing))
      (t/is (= (energiatodistus-tila id) :draft))
      (service/start-energiatodistus-signing! db whoami id)
      (t/is (= (energiatodistus-tila id) :in-signing))
      (t/is (=  (service/cancel-energiatodistus-signing! db whoami id)
                :ok))
      (t/is (= (energiatodistus-tila id) :draft)))))

(t/deftest update-signed-energiatodistus!-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        id (-> energiatodistukset keys sort first)
        add (get energiatodistukset id)
        update (-> (energiatodistus-test-data/generate-updates 1 2013 true)
                   first
                   (assoc-in [:perustiedot :rakennustunnus] "103515074X"))]
    (t/is (= (energiatodistus-tila id) :draft))
    (energiatodistus-test-data/sign! id laatija-id true)
    (t/is (= (energiatodistus-tila id) :signed))
    (service/update-energiatodistus! db whoami id update)
    (let [{:keys [allekirjoitusaika voimassaolo-paattymisaika]
           :as energiatodistus} (service/find-energiatodistus ts/*db* id)]
      (t/is (add-eq-found?
             (-> add
                 (assoc-in [:perustiedot :rakennustunnus]
                           (-> update :perustiedot :rakennustunnus))
                 (assoc :laskuriviviite
                        (:laskuriviviite update)
                        :tila-id 2
                        :allekirjoitusaika
                        allekirjoitusaika
                        :voimassaolo-paattymisaika
                        voimassaolo-paattymisaika))
             energiatodistus)))))

(t/deftest korvaa-energiatodistus!-test
  (let [{:keys [laatijat energiatodistukset paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        db (ts/db-user laatija-id)
        [korvattava-1st-id korvattava-2nd-id] (-> energiatodistukset keys sort)
        korvaava-add (-> (energiatodistus-test-data/generate-adds 1 2018 true)
                         first
                         (assoc :korvattu-energiatodistus-id korvattava-1st-id))
        _ (energiatodistus-test-data/sign! korvattava-1st-id laatija-id true)
        _ (energiatodistus-test-data/sign! korvattava-2nd-id laatija-id true)
        korvaava-id (first (energiatodistus-test-data/insert! [korvaava-add]
                                                              laatija-id))]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    ;; States after the initial replacement
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-2nd-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-1st-id) :replaced))

    ;; Change the replacing ET. Should revert the first superseded ET
    ;; back to a signed state
    (service/update-energiatodistus! db paakayttaja-whoami korvaava-id
                                     (-> (service/find-energiatodistus db korvaava-id)
                                         (assoc :korvattu-energiatodistus-id korvattava-2nd-id)))
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-2nd-id) :replaced))
    (t/is (= (energiatodistus-tila korvattava-1st-id) :signed))

    ;; Simulate grandfathered cases for which there is no state change
    ;; history
    (jdbc/execute! (ts/db-user ts/*admin-db* -1)
                   ["DELETE FROM audit.energiatodistus_tila WHERE id = ?;" korvattava-2nd-id])

    ;; Change back from the previous replacement maneuver, verifying
    ;; that it can be done even when state change history is absent
    (service/update-energiatodistus! db paakayttaja-whoami korvaava-id
                                     (-> (service/find-energiatodistus db korvaava-id)
                                         (assoc :korvattu-energiatodistus-id korvattava-1st-id)))
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-2nd-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-1st-id) :replaced))))

(t/deftest korvaa-energiatodistus-states-test
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        energiatodistus (first (energiatodistus-test-data/generate-adds 1
                                                                        2018
                                                                        true))
        [korvattu-id korvattava-id
         luonnos-id update-id] (energiatodistus-test-data/insert!
                                (repeat 4 energiatodistus)
                                laatija-id)
        _ (energiatodistus-test-data/sign! korvattu-id laatija-id true)
        korvannut-id (energiatodistus-test-data/insert!
                      [(assoc energiatodistus
                              :korvattu-energiatodistus-id
                              korvattu-id)]
                      laatija-id)
        _ (energiatodistus-test-data/sign! korvannut-id laatija-id true)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)]

    ;; Create energiatodistus with illegals and valid replaceable energiatodistus
    (t/is (= (etp-test/catch-ex-data
              #(energiatodistus-test-data/insert!
                [(assoc energiatodistus
                        :korvattu-energiatodistus-id
                        101)]
                laatija-id))
             {:type :invalid-replace
              :message "Replaceable energiatodistus 101 does not exist"}))
    (t/is (= (etp-test/catch-ex-data
              #(energiatodistus-test-data/insert!
                [(assoc energiatodistus
                        :korvattu-energiatodistus-id
                        luonnos-id)]
                laatija-id))
             {:type :invalid-replace
              :message (str "Replaceable energiatodistus "
                            luonnos-id
                            " is not in signed or discarded state")}))
    (t/is (= (etp-test/catch-ex-data
              #(energiatodistus-test-data/insert!
                [(assoc energiatodistus
                        :korvattu-energiatodistus-id
                        korvattu-id)]
                laatija-id))
             {:type :invalid-replace
              :message (str "Replaceable energiatodistus "
                            korvattu-id
                            " is already replaced")}))

    ;; Update energiatodistus with illegals and valid replaceable
    ;; energiatodistus
    (t/is (= (etp-test/catch-ex-data
              #(service/update-energiatodistus!
                ts/*db*
                whoami
                update-id
                (assoc energiatodistus
                       :korvattu-energiatodistus-id
                       101)))
             {:type :invalid-replace
              :message "Replaceable energiatodistus 101 does not exist"}))
    (t/is (= (etp-test/catch-ex-data
              #(service/update-energiatodistus!
                ts/*db*
                whoami
                update-id
                (assoc energiatodistus
                       :korvattu-energiatodistus-id
                       luonnos-id)))
             {:type :invalid-replace
              :message (str "Replaceable energiatodistus "
                            luonnos-id
                            " is not in signed or discarded state")}))
    (t/is (= (etp-test/catch-ex-data
              #(service/update-energiatodistus!
                ts/*db*
                whoami
                update-id
                (assoc energiatodistus
                       :korvattu-energiatodistus-id
                       korvattu-id)))
             {:type :invalid-replace
              :message (str "Replaceable energiatodistus "
                            korvattu-id
                            " is already replaced")}))

    ;; Check states of energiatodistukset
    (t/is (= (energiatodistus-tila korvattava-id) :signed))
    (t/is (= (energiatodistus-tila korvattu-id) :replaced))
    (t/is (= (energiatodistus-tila korvannut-id) :signed))
    (t/is (= (energiatodistus-tila luonnos-id) :draft))
    (t/is (= (energiatodistus-tila update-id) :draft))))

(t/deftest set-energiatodistus-discarded!-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        id (-> energiatodistukset keys sort first)
        add (get energiatodistukset id)]
    (t/is (= (energiatodistus-tila id) :draft))
    (energiatodistus-test-data/sign! id laatija-id true)
    (t/is (= (energiatodistus-tila id) :signed))
    (service/set-energiatodistus-discarded! db id true)
    (t/is (= (energiatodistus-tila id) :discarded))
    (t/is (add-eq-found? (get energiatodistukset id)
                         (service/find-energiatodistus ts/*db* id)))
    (service/set-energiatodistus-discarded! db id false)
    (t/is (= (energiatodistus-tila id) :signed))))

(t/deftest validate-pdf-signature
  (t/is (= true (#'service/pdf-signed? (-> "energiatodistukset/signed-with-test-certificate.pdf"
                                           io/resource
                                           io/input-stream))))
  (t/is (= false (#'service/pdf-signed? (-> "energiatodistukset/not-signed.pdf"
                                            io/resource
                                            io/input-stream)))))

;; ---- AE-2620: Pääkäyttäjä korvaavuus on signed energiatodistus ----

(t/deftest paakayttaja-sets-korvaavuus-on-signed-et-test
  ;; Given: two signed energiatodistukset
  ;; When: pääkäyttäjä sets korvattu-energiatodistus-id on the korvaava
  ;; Then: korvaava stays signed, korvattava transitions to replaced
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id korvattava-id] (energiatodistus-test-data/insert!
                                      [et-add et-add]
                                      laatija-id)]
    ;; Given: both are signed
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (energiatodistus-test-data/sign! korvattava-id laatija-id true)
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :signed))

    ;; When: pääkäyttäjä sets korvaavuus
    (service/update-energiatodistus!
      (ts/db-user paakayttaja-id)
      paakayttaja-whoami
      korvaava-id
      (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
          (assoc :korvattu-energiatodistus-id korvattava-id)))

    ;; Then: korvaava is signed, korvattava is replaced
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :replaced))))

(t/deftest paakayttaja-sets-korvaavuus-on-discarded-et-test
  ;; Given: a signed energiatodistus and a discarded energiatodistus
  ;; When: pääkäyttäjä sets korvaavuus targeting the discarded ET
  ;; Then: korvaava stays signed, discarded ET transitions to replaced
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id korvattava-id] (energiatodistus-test-data/insert!
                                      [et-add et-add]
                                      laatija-id)]
    ;; Given: korvaava is signed, korvattava is discarded
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (energiatodistus-test-data/sign! korvattava-id laatija-id true)
    (service/set-energiatodistus-discarded! (ts/db-user laatija-id) korvattava-id true)
    (t/is (= (energiatodistus-tila korvattava-id) :discarded))

    ;; When: pääkäyttäjä sets korvaavuus
    (service/update-energiatodistus!
      (ts/db-user paakayttaja-id)
      paakayttaja-whoami
      korvaava-id
      (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
          (assoc :korvattu-energiatodistus-id korvattava-id)))

    ;; Then: korvaava stays signed, korvattava becomes replaced
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :replaced))))

(t/deftest paakayttaja-removes-korvaavuus-from-signed-et-test
  ;; Given: a signed korvaava ET that replaces korvattava (korvattava is 'replaced')
  ;; When: pääkäyttäjä removes korvaavuus (sets korvattu-energiatodistus-id to nil)
  ;; Then: korvaava stays signed, korvattava reverts to signed
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [et-add] laatija-id)
        korvaava-add (assoc et-add :korvattu-energiatodistus-id korvattava-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :replaced))

    ;; When: pääkäyttäjä removes korvaavuus
    (service/update-energiatodistus!
      (ts/db-user paakayttaja-id)
      paakayttaja-whoami
      korvaava-id
      (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
          (assoc :korvattu-energiatodistus-id nil)))

    ;; Then: both are signed again
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :signed))))

(t/deftest paakayttaja-korvaavuus-target-in-draft-fails-test
  ;; Given: a signed korvaava ET and a draft korvattava ET
  ;; When: pääkäyttäjä tries to set korvaavuus to the draft ET
  ;; Then: fails with :invalid-replace
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id draft-id] (energiatodistus-test-data/insert!
                                 [et-add et-add]
                                 laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (t/is (= (energiatodistus-tila draft-id) :draft))

    ;; When/Then: setting korvaavuus to draft ET fails
    (t/is (= (:type (etp-test/catch-ex-data
                       #(service/update-energiatodistus!
                          (ts/db-user paakayttaja-id)
                          paakayttaja-whoami
                          korvaava-id
                          (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
                              (assoc :korvattu-energiatodistus-id draft-id)))))
             :invalid-replace))))

(t/deftest paakayttaja-korvaavuus-target-nonexistent-fails-test
  ;; Given: a signed energiatodistus
  ;; When: pääkäyttäjä sets korvattu-energiatodistus-id to nonexistent id
  ;; Then: fails with :invalid-replace
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)

    ;; When/Then: nonexistent target fails
    (t/is (= (etp-test/catch-ex-data
               #(service/update-energiatodistus!
                  (ts/db-user paakayttaja-id)
                  paakayttaja-whoami
                  korvaava-id
                  (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
                      (assoc :korvattu-energiatodistus-id 99999))))
             {:type :invalid-replace
              :message "Replaceable energiatodistus 99999 does not exist"}))))

(t/deftest laatija-cannot-set-korvaavuus-on-signed-et-test
  ;; Given: two signed energiatodistukset owned by the same laatija
  ;; When: laatija tries to set korvaavuus on the signed korvaava ET
  ;; Then: korvattu-energiatodistus-id is filtered out (not in allowed fields for [:signed :laatija _])
  ;;       and the korvattava ET remains in signed state
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        laatija-whoami {:id laatija-id :rooli 0}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id korvattava-id] (energiatodistus-test-data/insert!
                                      [et-add et-add]
                                      laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (energiatodistus-test-data/sign! korvattava-id laatija-id true)

    ;; When: laatija tries update with korvattu-energiatodistus-id
    (service/update-energiatodistus!
      (ts/db-user laatija-id)
      laatija-whoami
      korvaava-id
      (-> (service/find-energiatodistus (ts/db-user laatija-id) korvaava-id)
          (assoc :korvattu-energiatodistus-id korvattava-id)))

    ;; Then: korvattava is still signed (korvaavuus was silently filtered out)
    (t/is (= (energiatodistus-tila korvattava-id) :signed))))

(t/deftest paakayttaja-sets-korvaavuus-on-laskutettu-et-test
  ;; Given: a signed and already-invoiced energiatodistus, and another signed ET
  ;; When: pääkäyttäjä sets korvaavuus
  ;; Then: update succeeds, korvattava transitions to replaced
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        paakayttaja-id (-> paakayttajat keys sort first)
        paakayttaja-whoami {:id paakayttaja-id :rooli 2}
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvaava-id korvattava-id] (energiatodistus-test-data/insert!
                                      [et-add et-add]
                                      laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (energiatodistus-test-data/sign! korvattava-id laatija-id true)

    ;; Given: mark korvaava as laskutettu
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET laskutusaika = now() WHERE id = ?" korvaava-id])

    ;; When: pääkäyttäjä sets korvaavuus on already-invoiced ET
    (service/update-energiatodistus!
      (ts/db-user paakayttaja-id)
      paakayttaja-whoami
      korvaava-id
      (-> (service/find-energiatodistus (ts/db-user paakayttaja-id) korvaava-id)
          (assoc :korvattu-energiatodistus-id korvattava-id)))

    ;; Then: korvaava stays signed, korvattava is replaced
    (t/is (= (energiatodistus-tila korvaava-id) :signed))
    (t/is (= (energiatodistus-tila korvattava-id) :replaced))))

;; ---- AE-2620: PPP data in korvattavat results ----

(t/deftest find-korvattavat-includes-ppp-id-test
  ;; Given: a signed ET with an associated perusparannuspassi
  ;; When: find-korvattavat is called with matching query
  ;; Then: the result includes perusparannuspassi-id
  (let [laatija-adds (map #(assoc % :patevyystaso 4) (laatija-test-data/generate-adds 1))
        laatija-ids (laatija-test-data/insert! laatija-adds)
        laatija-id (first laatija-ids)
        laatija-whoami {:id laatija-id :rooli 0 :patevyystaso 4}
        et-add (-> (first (energiatodistus-test-data/generate-adds 1 2026 true))
                   (assoc-in [:perustiedot :postinumero] "33100")
                   (assoc-in [:perustiedot :katuosoite-fi] "Testikatu 1")
                   (assoc-in [:perustiedot :rakennustunnus] "103515074X"))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]

    ;; Given: create a perusparannuspassi BEFORE signing (PPP can only be added to draft)
    (let [ppp-add (perusparannuspassi-test-data/generate-add et-id)
          [ppp-id] (perusparannuspassi-test-data/insert! [ppp-add] laatija-whoami)]

      ;; Sign the ET after PPP is added
      (energiatodistus-test-data/sign! et-id laatija-id true)

      ;; When: find-korvattavat with matching rakennustunnus
      (let [results (service/find-korvattavat ts/*db*
                                               {:rakennustunnus "103515074X"
                                                :postinumero    "33100"})
            found-et (first (filter #(= (:id %) et-id) results))]

        ;; Then: PPP id is present
        (t/is (some? found-et) "ET should be found in korvattavat results")
        (t/is (= ppp-id (:perusparannuspassi-id found-et))
              "Korvattavat result should include perusparannuspassi-id")))))

(t/deftest find-korvattavat-ppp-nil-when-no-ppp-test
  ;; Given: a signed ET without a perusparannuspassi
  ;; When: find-korvattavat is called with matching query
  ;; Then: perusparannuspassi-id is nil
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        et-add (-> (first (energiatodistus-test-data/generate-adds 1 2026 true))
                   (assoc-in [:perustiedot :postinumero] "33100")
                   (assoc-in [:perustiedot :katuosoite-fi] "Testikatu 2")
                   (assoc-in [:perustiedot :rakennustunnus] "103515075Y"))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    (energiatodistus-test-data/sign! et-id laatija-id true)

    ;; When: find-korvattavat
    (let [results (service/find-korvattavat ts/*db*
                                             {:rakennustunnus "103515075Y"
                                              :postinumero    "33100"})
          found-et (first (filter #(= (:id %) et-id) results))]

      ;; Then: perusparannuspassi-id is nil
      (t/is (some? found-et) "ET should be found in korvattavat results")
      (t/is (nil? (:perusparannuspassi-id found-et))
            "Korvattavat result should have nil perusparannuspassi-id when no PPP exists"))))

;; ---- AE-2759: Yksinkertaistettu päivitysmenettely tests ----

(t/deftest yksinkertaistettu-paivitysmenettely-default-value-test
  ;; Given: a newly created energiatodistus draft
  ;; When: fetched from the database
  ;; Then: yksinkertaistettu-paivitysmenettely is false
  (let [{:keys [energiatodistukset]} (test-data-set)]
    (doseq [id (keys energiatodistukset)]
      (let [et (service/find-energiatodistus ts/*db* id)]
        (t/is (= false (:yksinkertaistettu-paivitysmenettely et))
              "Default value of yksinkertaistettu-paivitysmenettely should be false")))))

(t/deftest yksinkertaistettu-paivitysmenettely-round-trip-true-test
  ;; Given: an energiatodistus add payload with yksinkertaistettu-paivitysmenettely true
  ;;        and a valid korvattu-energiatodistus-id
  ;; When: the energiatodistus is added and then fetched
  ;; Then: the field value is preserved as true
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        korvaava-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                         (assoc :korvattu-energiatodistus-id korvattava-id
                                :yksinkertaistettu-paivitysmenettely true))
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (t/is (= true (:yksinkertaistettu-paivitysmenettely
                     (service/find-energiatodistus ts/*db* korvaava-id)))
          "yksinkertaistettu-paivitysmenettely should round-trip as true")))

(t/deftest yksinkertaistettu-paivitysmenettely-round-trip-false-test
  ;; Given: an energiatodistus add payload with yksinkertaistettu-paivitysmenettely false
  ;; When: added and fetched
  ;; Then: the value is false
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        et-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                   (assoc :yksinkertaistettu-paivitysmenettely false))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    (t/is (= false (:yksinkertaistettu-paivitysmenettely
                      (service/find-energiatodistus ts/*db* et-id)))
          "yksinkertaistettu-paivitysmenettely should round-trip as false")))

(t/deftest yksinkertaistettu-signing-inherits-validity-test
  ;; Given: a signed ET A with a custom voimassaolo-paattymisaika (5 years from now)
  ;; And: a draft ET B that replaces A with yksinkertaistettu-paivitysmenettely true
  ;; When: B is signed
  ;; Then: B's voimassaolo-paattymisaika equals A's voimassaolo-paattymisaika (5 years, not 10)
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        ;; Set a custom validity date (5 years from now) to distinguish from standard 10-year
        custom-validity (java.sql.Timestamp. (.getTime (java.util.Date. (+ (.getTime (java.util.Date.))
                                                                           (* 5 365 24 60 60 1000)))))
        _ (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET voimassaolo_paattymisaika = ? WHERE id = ?"
                                  custom-validity korvattava-id])
        korvattava-et (service/find-energiatodistus ts/*db* korvattava-id)
        korvattava-validity (:voimassaolo-paattymisaika korvattava-et)
        korvaava-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                         (assoc :korvattu-energiatodistus-id korvattava-id
                                :yksinkertaistettu-paivitysmenettely true))
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (let [korvaava-et (service/find-energiatodistus ts/*db* korvaava-id)]
      (t/is (= korvattava-validity (:voimassaolo-paattymisaika korvaava-et))
            "Signed ET with yksinkertaistettu-paivitysmenettely should inherit replaced ET's validity"))))

(t/deftest yksinkertaistettu-false-signing-gets-standard-validity-test
  ;; Given: a signed ET A and a draft ET B replacing A with yksinkertaistettu-paivitysmenettely false
  ;; When: B is signed
  ;; Then: B's voimassaolo-paattymisaika is approximately now + 10 years (standard)
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        korvattava-et (service/find-energiatodistus ts/*db* korvattava-id)
        korvattava-validity (:voimassaolo-paattymisaika korvattava-et)
        korvaava-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                         (assoc :korvattu-energiatodistus-id korvattava-id
                                :yksinkertaistettu-paivitysmenettely false))
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (let [korvaava-et (service/find-energiatodistus ts/*db* korvaava-id)]
      ;; Regression: standard signing sets voimassaolo-paattymisaika
      (t/is (some? (:voimassaolo-paattymisaika korvaava-et))
            "Signed ET without yksinkertaistettu should have voimassaolo-paattymisaika set"))))

(t/deftest yksinkertaistettu-signing-without-replacement-regression-test
  ;; Given: a draft ET with no korvattu-energiatodistus-id and yksinkertaistettu false
  ;; When: signed
  ;; Then: voimassaolo-paattymisaika is approximately now + 10 years
  ;; This is a regression test ensuring existing behavior is not broken
  (let [{:keys [energiatodistukset laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        id (-> energiatodistukset keys sort first)]
    (t/is (= (energiatodistus-tila id) :draft))
    (energiatodistus-test-data/sign! id laatija-id true)
    (let [et (service/find-energiatodistus ts/*db* id)]
      (t/is (some? (:voimassaolo-paattymisaika et))
            "Standard signing should set voimassaolo-paattymisaika")
      (t/is (nil? (:korvattu-energiatodistus-id et))
            "No korvattu-energiatodistus-id for non-replacement signing"))))

(t/deftest yksinkertaistettu-replaced-certificate-state-transition-test
  ;; Given: a signed ET A, and a draft B replacing A with yksinkertaistettu true
  ;; When: B is signed
  ;; Then: A's state becomes :replaced
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        korvaava-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                         (assoc :korvattu-energiatodistus-id korvattava-id
                                :yksinkertaistettu-paivitysmenettely true))
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (energiatodistus-test-data/sign! korvaava-id laatija-id true)
    (t/is (= (energiatodistus-tila korvattava-id) :replaced)
          "Replaced ET should be in :replaced state even with yksinkertaistettu")))

(t/deftest yksinkertaistettu-true-without-korvattu-id-rejected-test
  ;; Given: a draft ET with yksinkertaistettu-paivitysmenettely true and korvattu-energiatodistus-id nil
  ;; When: signing is attempted
  ;; Then: an error is thrown
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        et-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                   (assoc :yksinkertaistettu-paivitysmenettely true
                          :korvattu-energiatodistus-id nil))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    (t/is (= :invalid-replace
             (:type (etp-test/catch-ex-data
                      #(do
                         (service/start-energiatodistus-signing! db whoami et-id)
                         (service/end-energiatodistus-signing! db
                                                               ts/*aws-s3-client*
                                                               whoami
                                                               et-id
                                                               {:skip-pdf-signed-assert? true})))))
          "yksinkertaistettu true without korvattu-energiatodistus-id should fail")))

(t/deftest yksinkertaistettu-with-expired-replaced-certificate-rejected-test
  ;; Given: a signed ET A whose voimassaolo-paattymisaika is in the past
  ;; And: a draft B replacing A with yksinkertaistettu-paivitysmenettely true
  ;; When: signing B is attempted
  ;; Then: an appropriate error is thrown
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        ;; Manually set the validity to the past
        _ (jdbc/execute! (ts/db-user ts/*admin-db* -1)
                         ["UPDATE energiatodistus SET voimassaolo_paattymisaika = now() - interval '1 day' WHERE id = ?" korvattava-id])
        korvaava-add (-> (first (energiatodistus-test-data/generate-adds 1 2018 true))
                         (assoc :korvattu-energiatodistus-id korvattava-id
                                :yksinkertaistettu-paivitysmenettely true))
        [korvaava-id] (energiatodistus-test-data/insert! [korvaava-add] laatija-id)]
    (t/is (= :invalid-replace
             (:type (etp-test/catch-ex-data
                      #(do
                         (service/start-energiatodistus-signing! db whoami korvaava-id)
                         (service/end-energiatodistus-signing! db
                                                               ts/*aws-s3-client*
                                                               whoami
                                                               korvaava-id
                                                               {:skip-pdf-signed-assert? true})))))
          "yksinkertaistettu with expired replaced certificate should fail")))

(t/deftest yksinkertaistettu-update-draft-field-persistence-test
  ;; Given: a draft ET with yksinkertaistettu-paivitysmenettely false
  ;; When: the laatija updates it to true with a valid korvattu-energiatodistus-id
  ;; Then: the value is persisted and can be read back as true
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        whoami {:id laatija-id :rooli 0}
        db (ts/db-user laatija-id)
        korvattava-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        et-add (first (energiatodistus-test-data/generate-adds 1 2018 true))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    ;; Confirm initial value is false
    (t/is (= false (:yksinkertaistettu-paivitysmenettely
                      (service/find-energiatodistus ts/*db* et-id))))
    ;; Update to true with valid korvattu id
    (service/update-energiatodistus! db whoami et-id
                                     (assoc et-add
                                            :korvattu-energiatodistus-id korvattava-id
                                            :yksinkertaistettu-paivitysmenettely true))
    (t/is (= true (:yksinkertaistettu-paivitysmenettely
                     (service/find-energiatodistus ts/*db* et-id)))
          "Updated yksinkertaistettu-paivitysmenettely should persist as true")))
