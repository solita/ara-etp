(ns solita.etp.service.enrgiatodistus-audit-test
  (:require [clojure.test :as t]
            [solita.etp.db :as db]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-test :as energiatodistus-service-test]
            [clojure.java.jdbc :as jdbc]))

(t/use-fixtures :each ts/fixture)

(defn find-energiatodistus-audit [db id laatija]
  (->
    (jdbc/query db ["select * from audit.energiatodistus where id = ? order by event_id desc" id]
                db/default-opts)
    first
    (assoc :laatija-fullname (str (:sukunimi laatija) ", " (:etunimi laatija)))
    (assoc :korvaava-energiatodistus-id nil)
    (assoc :perusparannuspassi-id nil)
    (assoc :perusparannuspassi-valid nil)))

(defn whoami-laatija [id]
  {:id id :rooli 0})

(t/deftest add-energiatodistus
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        energiatodistus (energiatodistus-test-data/generate-add 2018 false)
        db (ts/db-user laatija-id)
        whoami (whoami-laatija laatija-id)
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami 2018 energiatodistus))
        audit (find-energiatodistus-audit db energiatodistus-id laatija)]


    (t/is (energiatodistus-service-test/add-eq-found?
            energiatodistus
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (=
            (energiatodistus-service/find-energiatodistus db energiatodistus-id)
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (= laatija-id (:modifiedby-id audit)))
    (t/is (= "core.etp.test" (:service-uri audit)))))

(t/deftest update-energiatodistus
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        energiatodistus-add (energiatodistus-test-data/generate-add 2018 false)
        energiatodistus-update (energiatodistus-test-data/generate-add 2018 false)
        db (ts/db-user laatija-id)
        whoami (whoami-laatija laatija-id)
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami 2018 energiatodistus-add))
        _ (energiatodistus-service/update-energiatodistus! db whoami energiatodistus-id energiatodistus-update)
        audit (find-energiatodistus-audit db energiatodistus-id laatija)]


    (t/is (energiatodistus-service-test/add-eq-found?
            energiatodistus-update
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (=
            (energiatodistus-service/find-energiatodistus db energiatodistus-id)
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (= laatija-id (:modifiedby-id audit)))
    (t/is (= "core.etp.test" (:service-uri audit)))))

(defn update-energiatodistus-n [n versio [laatija-id laatija]]
  (let [energiatodistus-add (energiatodistus-test-data/generate-add versio false)
        service-uri (str "/" (rand-int 10000))
        db (ts/db-user ts/*db* laatija-id service-uri)
        whoami {:id laatija-id :rooli 0}
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami versio energiatodistus-add))]

    (doseq [energiatodistus-update (energiatodistus-test-data/generate-adds n versio false)]
      (energiatodistus-service/update-energiatodistus! db whoami energiatodistus-id energiatodistus-update)
      (let [audit (find-energiatodistus-audit db energiatodistus-id laatija)]
        (t/is (energiatodistus-service-test/add-eq-found?
                energiatodistus-update
                (energiatodistus-service/db-row->energiatodistus audit)))

        (t/is (=
                (energiatodistus-service/find-energiatodistus db energiatodistus-id)
                (energiatodistus-service/db-row->energiatodistus audit)))

        (t/is (= laatija-id (:modifiedby-id audit)))
        (t/is (= (str "core.etp.test" service-uri) (:service-uri audit)))))))

(t/deftest update-energiatodistus-1-2018
  (update-energiatodistus-n
    1 2018 (first (laatija-test-data/generate-and-insert! 1))))

(t/deftest update-energiatodistus-1-2013
  (update-energiatodistus-n
    1 2013 (first (laatija-test-data/generate-and-insert! 1))))

(t/deftest update-energiatodistus-2-2018
  (update-energiatodistus-n
    2 2018 (first (laatija-test-data/generate-and-insert! 1))))

;; ---- ET2026 Audit Tests ----

(t/deftest add-energiatodistus-2026
  ;; Given: a generated ET2026 energiatodistus
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        energiatodistus (energiatodistus-test-data/generate-add 2026 false)
        db (ts/db-user laatija-id)
        whoami (whoami-laatija laatija-id)

        ;; When: we add the energiatodistus
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami 2026 energiatodistus))
        audit (find-energiatodistus-audit db energiatodistus-id laatija)]

    ;; Then: audit row matches the inserted energiatodistus
    (t/is (energiatodistus-service-test/add-eq-found?
            energiatodistus
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (=
            (energiatodistus-service/find-energiatodistus db energiatodistus-id)
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (= laatija-id (:modifiedby-id audit)))
    (t/is (= "core.etp.test" (:service-uri audit)))))

(t/deftest update-energiatodistus-2026
  ;; Given: an ET2026 energiatodistus is created
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        energiatodistus-add (energiatodistus-test-data/generate-add 2026 false)
        energiatodistus-update (energiatodistus-test-data/generate-add 2026 false)
        db (ts/db-user laatija-id)
        whoami (whoami-laatija laatija-id)
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami 2026 energiatodistus-add))

        ;; When: we update the energiatodistus
        _ (energiatodistus-service/update-energiatodistus! db whoami energiatodistus-id energiatodistus-update)
        audit (find-energiatodistus-audit db energiatodistus-id laatija)]

    ;; Then: audit row matches the updated energiatodistus
    (t/is (energiatodistus-service-test/add-eq-found?
            energiatodistus-update
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (=
            (energiatodistus-service/find-energiatodistus db energiatodistus-id)
            (energiatodistus-service/db-row->energiatodistus audit)))

    (t/is (= laatija-id (:modifiedby-id audit)))
    (t/is (= "core.etp.test" (:service-uri audit)))))

(t/deftest update-energiatodistus-1-2026
  ;; Given/When/Then: multiple updates create correct audit rows for ET2026
  (update-energiatodistus-n
    1 2026 (first (laatija-test-data/generate-and-insert! 1))))

(t/deftest update-energiatodistus-2-2026
  ;; Given/When/Then: two updates create correct audit rows for ET2026
  (update-energiatodistus-n
    2 2026 (first (laatija-test-data/generate-and-insert! 1))))

(t/deftest audit-row-contains-2026-specific-columns
  ;; Given: an ET2026 energiatodistus with specific ET2026-only field values
  (let [[laatija-id laatija] (first (laatija-test-data/generate-and-insert! 1))
        energiatodistus (energiatodistus-test-data/generate-add 2026 false)
        db (ts/db-user laatija-id)
        whoami (whoami-laatija laatija-id)

        ;; When: we add the energiatodistus
        energiatodistus-id
        (:id (energiatodistus-service/add-energiatodistus!
               (ts/db-user laatija-id) whoami 2026 energiatodistus))

        ;; Then: audit row contains ET2026-specific database columns
        audit-row (first
                    (jdbc/query db
                                ["select * from audit.energiatodistus where id = ? order by event_id desc" energiatodistus-id]
                                db/default-opts))]
    ;; Verify ET2026-specific columns exist in the audit row (they should not be absent from the result set)
    (t/is (contains? audit-row :lt$energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin))
    (t/is (contains? audit-row :lt$lammitys$lammonjako-lampotilajousto))
    (t/is (contains? audit-row :pt$havainnointikayntityyppi-id))
    (t/is (contains? audit-row :is$laadintaperuste))))

(t/deftest update-energiatodistus-2-2013
  (update-energiatodistus-n
    2 2013 (first (laatija-test-data/generate-and-insert! 1))))

(t/deftest sequential-update-energiatodistus-2018
  (doall (map (partial update-energiatodistus-n 10 2018)
              (laatija-test-data/generate-and-insert! 10))))

(t/deftest parallel-update-energiatodistus-2018
  (doall (pmap (partial update-energiatodistus-n 10 2018)
               (laatija-test-data/generate-and-insert! 10))))

(t/deftest full-parallel-update-energiatodistus-2018
  (->> (laatija-test-data/generate-and-insert! 10)
       (mapv #(future (update-energiatodistus-n 10 2018 %)))
       (mapv deref)))
