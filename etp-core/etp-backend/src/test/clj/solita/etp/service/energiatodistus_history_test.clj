(ns solita.etp.service.energiatodistus-history-test
  (:require [clojure.test :as t]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-history :as service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts])
  (:import (clojure.lang ExceptionInfo)
           (java.time Instant)
           (java.time.temporal ChronoUnit)))

(t/use-fixtures :each ts/fixture)

(defn update-energiatodistus! [energiatodistus-id energiatodistus laatija-id]
  (energiatodistus-service/update-energiatodistus! (ts/db-user laatija-id)
                                                   {:id laatija-id :rooli 0}
                                                   energiatodistus-id
                                                   energiatodistus))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2] laatija-ids
        energiatodistus-adds (energiatodistus-test-data/generate-adds 3
                                                                      2018
                                                                      true)
        energiatodistus-ids (->> (interleave laatija-ids energiatodistus-adds)
                                 (partition 2)
                                 (mapcat #(energiatodistus-test-data/insert!
                                           [(second %)]
                                           (first %))))
        [energiatodistus-id-1 energiatodistus-id-2] energiatodistus-ids
        [energiatodistus-add-1 energiatodistus-add-2] energiatodistus-adds
        energiatodistus-update-2 (assoc energiatodistus-add-2
                                        :korvattu-energiatodistus-id
                                        energiatodistus-id-1)]

    ;; Update energiatodistus 1 nettoala
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       123.45)
                             laatija-id-1)

    ;; Sign energiatodistus 1
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    ;; Update energiatodistus 2 nettoala and replace energiatodistus 1 with 2
    (update-energiatodistus! energiatodistus-id-2
                             (assoc-in energiatodistus-update-2
                                       [:lahtotiedot :lammitetty-nettoala]
                                       678.91)
                             laatija-id-2)

    ;; Update energiatodistus 2 nettoala back to what it was originally
    (update-energiatodistus! energiatodistus-id-2
                             energiatodistus-update-2
                             laatija-id-2)

    ;; Sign energiatodistus 2
    (energiatodistus-test-data/sign! energiatodistus-id-2 laatija-id-2 true)

    ;; Update laskuriviviite of energiatodistus 2
    (update-energiatodistus! energiatodistus-id-2
                             (assoc energiatodistus-update-2
                                    :laskuriviviite
                                    "laskuriviviite")
                             laatija-id-2)
    {:laatijat laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest audit-row->flat-energiatodistus-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        id (-> energiatodistukset keys sort first)
        flats (->> (service/find-audit-rows ts/*db* id)
                                     (map service/audit-row->flat-energiatodistus))]
    (doseq [flat-energiatodistus flats]
      (t/is (contains? flat-energiatodistus :id))
      (t/is (contains? flat-energiatodistus :lahtotiedot.lammitetty-nettoala)))))

(t/deftest audit-event-test
  (let [now (Instant/now)
        yesterday (.minus now 1 ChronoUnit/DAYS)]
    (t/is (= {:modifiedby-fullname "Cooper, Dale"
              :modifytime now
              :k :foo
              :init-v "bar"
              :new-v "baz"
              :type :str
              :external-api false}
             (service/audit-event "Cooper, Dale" now :foo "bar" "baz" false)))
    (t/is (= {:modifiedby-fullname "Bob"
              :modifytime now
              :k :foo
              :init-v 100
              :new-v 123
              :type :number
              :external-api true}
             (service/audit-event "Bob" now :foo 100 123 true)))
    (t/is (= {:modifiedby-fullname "Mike"
              :modifytime now
              :k :foo
              :init-v now
              :new-v yesterday
              :type :date
              :external-api false}
             (service/audit-event "Mike" now :foo now yesterday false)))
    (t/is (= {:modifiedby-fullname "Judy"
              :modifytime yesterday
              :k :allekirjoitusaika
              :init-v now
              :new-v yesterday
              :type :date
              :external-api true}
             (service/audit-event "Judy"
                                  now
                                  :allekirjoitusaika
                                  now
                                  yesterday
                                  true)))
    (t/is (= {:modifiedby-fullname "Palmer, Laura"
              :modifytime now
              :k :foo
              :init-v []
              :new-v [1 2 3]
              :type :other
              :external-api false}
             (service/audit-event "Palmer, Laura" now :foo [] [1 2 3] false)))))

(t/deftest find-audit-rows-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2] ids
        audit-rows-1 (service/find-audit-rows ts/*db* id-1)
        audit-rows-2 (service/find-audit-rows ts/*db* id-2)]
    (t/is (= 5 (count audit-rows-1)))
    (t/is (= 6 (count audit-rows-2)))
    (t/is (= [0 0 1 2 4] (map :tila-id audit-rows-1)))
    (t/is (= [0 0 0 1 2 2] (map :tila-id audit-rows-2)))))

(defn fullname [laatijat laatija-id]
  (let [{:keys [etunimi sukunimi]} (get laatijat laatija-id)]
    (str sukunimi ", " etunimi)))

(t/deftest find-history-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2] laatija-ids
        [laatija-1-fullname
         laatija-2-fullname] (map (partial fullname laatijat) laatija-ids)
        [energiatodistus-id-1
         energiatodistus-id-2] (-> energiatodistukset keys sort)
        history-1 (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
        history-2 (service/find-history ts/*db*
                                        {:id laatija-id-2 :rooli 0}
                                        energiatodistus-id-2)]

    ;; Energiatodistus 1 state history
    (t/is (= 7 (-> history-1 :state-history count)))
    (t/is (= :allekirjoitusaika (-> history-1 :state-history first :k)))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v nil
              :new-v 0
              :type :number
              :external-api false}
             (-> history-1 :state-history second (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v 0
              :new-v 1
              :type :number
              :external-api false}
            (-> history-1 :state-history (nth 2) (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v 0
              :new-v 2
              :type :number
              :external-api false}
             (-> history-1 :state-history (nth 3) (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v 0
              :new-v 4
              :type :number
              :external-api false}
             (-> history-1 :state-history (nth 4) (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :korvaava-energiatodistus-id
              :init-v nil
              :new-v energiatodistus-id-2
              :type :number
              :external-api false}
             (-> history-1 :state-history (nth 5) (dissoc :modifytime))))
    (t/is (= :voimassaolo-paattymisaika
             (-> history-1 :state-history last :k)))

    ;; Energiatodistus 1 form history
    (t/is (= [{:modifiedby-fullname laatija-1-fullname
               :k :tulokset.e-luku
               :init-v 4
               :new-v 1
               :type :number
               :external-api false}
              {:modifiedby-fullname laatija-1-fullname
               :k :lahtotiedot.lammitetty-nettoala
               :init-v 1.0M
               :new-v 123.45M
               :type :number
               :external-api false}]
             (->> history-1 :form-history (map #(dissoc % :modifytime)))))

    ;; Energiatodistus 2 state history
    (t/is (= 5 (-> history-2 :state-history count)))
    (t/is (= :allekirjoitusaika (-> history-2 :state-history first :k)))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v nil
              :new-v 0
              :type :number
              :external-api false}
             (-> history-2 :state-history second (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v 0
              :new-v 1
              :type :number
              :external-api false}
             (-> history-2 :state-history (nth 2) (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v 0
              :new-v 2
              :type :number
              :external-api false}
             (-> history-2 :state-history (nth 3) (dissoc :modifytime))))
    (t/is (= :voimassaolo-paattymisaika
             (-> history-2 :state-history last :k)))

    ;; Energiatodistus 2 form history
    (t/is (= [{:modifiedby-fullname laatija-2-fullname
               :k :korvattu-energiatodistus-id
               :new-v energiatodistus-id-1
               :type :number
               :external-api false}
              {:modifiedby-fullname laatija-2-fullname
               :k :laskuriviviite
               :new-v "laskuriviviite"
               :type :str
               :external-api false}]
             (->> history-2
                  :form-history
                  (map #(dissoc % :modifytime :init-v)))))
    (t/is (string? (-> history-2 :form-history last :init-v)))))

(t/deftest find-history-no-permissions-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1] laatija-ids
        [_ _ energiatodistus-id-3] (-> energiatodistukset keys sort)]
    (doseq [whoami [{:id laatija-id-1 :rooli 0}
                    kayttaja-test-data/paakayttaja
                    kayttaja-test-data/laskuttaja
                    kayttaja-test-data/patevyyden-toteaja]]
      (t/is
       (thrown-with-msg?
         ExceptionInfo
         #"Forbidden"
         (service/find-history ts/*db*
                              whoami
                              energiatodistus-id-3))))))

;; ---- ET2026 test helpers ----

(defn test-data-set-2026 []
  (let [laatijat (laatija-test-data/generate-and-insert! 2)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2] laatija-ids
        energiatodistus-add-1 (energiatodistus-test-data/generate-add 2026 true)
        energiatodistus-add-2 (energiatodistus-test-data/generate-add 2026 true)
        [energiatodistus-id-1] (energiatodistus-test-data/insert!
                                 [energiatodistus-add-1]
                                 laatija-id-1)
        [energiatodistus-id-2] (energiatodistus-test-data/insert!
                                 [energiatodistus-add-2]
                                 laatija-id-2)]
    {:laatijat               laatijat
     :laatija-ids            laatija-ids
     :energiatodistus-id-1   energiatodistus-id-1
     :energiatodistus-id-2   energiatodistus-id-2
     :energiatodistus-add-1  energiatodistus-add-1
     :energiatodistus-add-2  energiatodistus-add-2}))

;; ---- ET2026 Tests: audit-row->flat-energiatodistus ----

(t/deftest audit-row->flat-energiatodistus-2026-test
  ;; Given: an ET2026 energiatodistus is created and updated
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update a shared field
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       999.99)
                             laatija-id-1)

    ;; Then: flattened audit rows contain shared and ET2026-specific keys
    (let [flats (->> (service/find-audit-rows ts/*db* energiatodistus-id-1)
                     (map service/audit-row->flat-energiatodistus))]
      (doseq [flat-et flats]
        ;; Shared fields
        (t/is (contains? flat-et :id))
        (t/is (contains? flat-et :lahtotiedot.lammitetty-nettoala))
        ;; ET2026-specific fields
        (t/is (contains? flat-et :perustiedot.havainnointikayntityyppi-id))
        (t/is (contains? flat-et :lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin))
        (t/is (contains? flat-et :lahtotiedot.lammitys.lammonjako-lampotilajousto))))))

(t/deftest audit-row->flat-energiatodistus-2026-ilmastoselvitys-test
  ;; Given: an ET2026 energiatodistus with ilmastoselvitys data
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update the energiatodistus
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-valmistus]
                                       42.0)
                             laatija-id-1)

    ;; Then: flattened audit rows contain ilmastoselvitys-related keys
    (let [flats (->> (service/find-audit-rows ts/*db* energiatodistus-id-1)
                     (map service/audit-row->flat-energiatodistus))]
      (doseq [flat-et flats]
        (t/is (contains? flat-et :ilmastoselvitys.laadintaperuste))
        (t/is (contains? flat-et :ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus))
        (t/is (contains? flat-et :ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto))))))

(t/deftest audit-row->flat-energiatodistus-2026-kokonaistuotanto-test
  ;; Given: an ET2026 energiatodistus
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update the energiatodistus
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       555.55)
                             laatija-id-1)

    ;; Then: flattened audit rows contain tulokset kokonaistuotanto keys
    (let [flats (->> (service/find-audit-rows ts/*db* energiatodistus-id-1)
                     (map service/audit-row->flat-energiatodistus))]
      (doseq [flat-et flats]
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko))
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo))
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko))
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu))
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo))
        (t/is (contains? flat-et :tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko))))))

(t/deftest audit-row->flat-energiatodistus-2026-toteutunut-test
  ;; Given: an ET2026 energiatodistus
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update the energiatodistus
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       333.33)
                             laatija-id-1)

    ;; Then: flattened audit rows contain toteutunut-ostoenergiankulutus 2026-specific keys
    (let [flats (->> (service/find-audit-rows ts/*db* energiatodistus-id-1)
                     (map service/audit-row->flat-energiatodistus))]
      (doseq [flat-et flats]
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi))
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.lisatietoja-fi))
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.lisatietoja-sv))
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa))
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.fossiiliset-polttoaineet-vuosikulutus-yhteensa))
        (t/is (contains? flat-et :toteutunut-ostoenergiankulutus.uusiutuva-energia-vuosituotto-yhteensa))))))

;; ---- ET2026 Tests: find-audit-rows ----

(t/deftest find-audit-rows-2026-test
  ;; Given: an ET2026 energiatodistus is created, updated, and signed
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update and sign
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       111.11)
                             laatija-id-1)
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    ;; Then: correct number of audit rows with expected tila-id progression
    (let [audit-rows (service/find-audit-rows ts/*db* energiatodistus-id-1)]
      ;; insert(0) + update(0) + start-signing(1) + end-signing(2)
      (t/is (= 4 (count audit-rows)))
      (t/is (= [0 0 1 2] (map :tila-id audit-rows))))))

;; ---- ET2026 Tests: find-history ----

(t/deftest find-history-2026-state-history-test
  ;; Given: an ET2026 energiatodistus is created, updated, and signed
  (let [{:keys [laatijat energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)
        laatija-1-fullname (fullname laatijat laatija-id-1)]

    ;; When: we update and sign
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       222.22)
                             laatija-id-1)
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    ;; Then: find-history returns non-nil with correct state history
    (let [history (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)]
      (t/is (some? history))
      (t/is (some? (:state-history history)))

      ;; State history contains initial draft
      (t/is (= {:modifiedby-fullname laatija-1-fullname
                :k :tila-id
                :init-v nil
                :new-v 0
                :type :number
                :external-api false}
               (-> history :state-history second (dissoc :modifytime))))

      ;; State history contains allekirjoitusaika
      (t/is (some #(= :allekirjoitusaika (:k %)) (:state-history history)))

      ;; State history contains signing started (tila-id 1)
      (t/is (some #(and (= :tila-id (:k %))
                        (= 1 (:new-v %)))
                  (:state-history history)))

      ;; State history contains signed (tila-id 2)
      (t/is (some #(and (= :tila-id (:k %))
                        (= 2 (:new-v %)))
                  (:state-history history))))))

(t/deftest find-history-2026-form-history-shared-fields-test
  ;; Given: an ET2026 energiatodistus
  (let [{:keys [laatijat energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)
        laatija-1-fullname (fullname laatijat laatija-id-1)]

    ;; When: we update a shared field (lammitetty-nettoala)
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       444.44)
                             laatija-id-1)

    ;; Then: form history contains the nettoala change
    (let [history (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
          nettoala-event (->> (:form-history history)
                              (filter #(= :lahtotiedot.lammitetty-nettoala (:k %)))
                              first)]
      (t/is (some? nettoala-event))
      (t/is (= :number (:type nettoala-event)))
      (t/is (= 444.44M (:new-v nettoala-event)))
      (t/is (= laatija-1-fullname (:modifiedby-fullname nettoala-event))))))

(t/deftest find-history-2026-form-history-et2026-specific-fields-test
  ;; Given: an ET2026 energiatodistus
  (let [{:keys [laatijat energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)
        original-value (get-in energiatodistus-add-1
                               [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin])]

    ;; When: we toggle the ET2026-specific boolean field
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin]
                                       (not original-value))
                             laatija-id-1)

    ;; Then: form history contains the ET2026-specific field change
    (let [history (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
          valmius-event (->> (:form-history history)
                             (filter #(= :lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin (:k %)))
                             first)]
      (t/is (some? valmius-event))
      (t/is (= :bool (:type valmius-event)))
      (t/is (= (not original-value) (:new-v valmius-event))))))

(t/deftest find-history-2026-form-history-ilmastoselvitys-test
  ;; Given: an ET2026 energiatodistus with ilmastoselvitys data
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update an ilmastoselvitys field
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-valmistus]
                                       99.9)
                             laatija-id-1)

    ;; Then: form history contains the ilmastoselvitys field change
    (let [history (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
          ilmasto-event (->> (:form-history history)
                              (filter #(= :ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus (:k %)))
                              first)]
      (t/is (some? ilmasto-event))
      (t/is (= 99.9M (:new-v ilmasto-event))))))

(t/deftest find-history-2026-reverted-field-test
  ;; Given: an ET2026 energiatodistus
  (let [{:keys [energiatodistus-id-1 energiatodistus-add-1 laatija-ids]}
        (test-data-set-2026)
        laatija-id-1 (first laatija-ids)]

    ;; When: we update a field and then revert it back
    (update-energiatodistus! energiatodistus-id-1
                             (assoc-in energiatodistus-add-1
                                       [:lahtotiedot :lammitetty-nettoala]
                                       777.77)
                             laatija-id-1)
    (update-energiatodistus! energiatodistus-id-1
                             energiatodistus-add-1
                             laatija-id-1)

    ;; Then: the reverted field should NOT be in form history
    (let [history (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
          nettoala-events (->> (:form-history history)
                               (filter #(= :lahtotiedot.lammitetty-nettoala (:k %))))]
      (t/is (empty? nettoala-events)))))

(t/deftest find-history-2026-no-permissions-test
  ;; Given: an ET2026 energiatodistus owned by laatija-id-2
  (let [{:keys [laatijat energiatodistus-id-2 laatija-ids]}
        (test-data-set-2026)
        [laatija-id-1] laatija-ids]

    ;; Then: a different laatija cannot access the history
    (doseq [whoami [{:id laatija-id-1 :rooli 0}
                    kayttaja-test-data/laskuttaja
                    kayttaja-test-data/patevyyden-toteaja]]
      (t/is
       (thrown-with-msg?
         ExceptionInfo
         #"Forbidden"
         (service/find-history ts/*db*
                              whoami
                              energiatodistus-id-2))))))

;; ---- AE-2759: yksinkertaistettu-paivitysmenettely is not a state-field ----

(t/deftest yksinkertaistettu-paivitysmenettely-not-in-state-fields-test
  ;; Given: the state-fields set from energiatodistus-history
  ;; When: checking if yksinkertaistettu-paivitysmenettely is in state-fields
  ;; Then: it should NOT be in state-fields (it is a form field)
  (t/is (not (contains? service/state-fields :yksinkertaistettu-paivitysmenettely))
        "yksinkertaistettu-paivitysmenettely should not be in state-fields"))

(t/deftest yksinkertaistettu-paivitysmenettely-appears-in-form-history-test
  ;; Given: an energiatodistus whose yksinkertaistettu-paivitysmenettely is changed from false to true
  ;; When: history is queried
  ;; Then: the change appears in form history
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        korvattava-add (energiatodistus-test-data/generate-add 2018 true)
        [korvattava-id] (energiatodistus-test-data/insert! [korvattava-add] laatija-id)
        _ (energiatodistus-test-data/sign! korvattava-id laatija-id true)
        et-add (energiatodistus-test-data/generate-add 2018 true)
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]

    ;; Update yksinkertaistettu-paivitysmenettely from false to true
    (update-energiatodistus! et-id
                             (assoc et-add
                                    :korvattu-energiatodistus-id korvattava-id
                                    :yksinkertaistettu-paivitysmenettely true)
                             laatija-id)

    (let [history (service/find-history ts/*db*
                                        {:id laatija-id :rooli 0}
                                        et-id)
          form-history (:form-history history)
          yksinkertaistettu-change (some #(when (= :yksinkertaistettu-paivitysmenettely (:k %)) %)
                                         form-history)]
      (t/is (some? yksinkertaistettu-change)
            "History should contain a change for yksinkertaistettu-paivitysmenettely")
      (t/is (= false (:init-v yksinkertaistettu-change))
            "Initial value should be false")
      (t/is (= true (:new-v yksinkertaistettu-change))
            "New value should be true")
      (t/is (= :bool (:type yksinkertaistettu-change))
            "Type should be :bool"))))
