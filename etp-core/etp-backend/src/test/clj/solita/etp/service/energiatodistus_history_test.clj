(ns solita.etp.service.energiatodistus-history-test
  (:require [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-history :as service])
  (:import (java.time Instant)
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
        whoami-1 {:id laatija-id-1 :rooli 0}
        whoami-2 {:id laatija-id-2 :rooli 0}
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

(defn without-modifytimes [coll]
  (map #(dissoc % :modifytime) coll))

(defn fullname [laatijat laatija-id]
  (let [{:keys [etunimi sukunimi]} (get laatijat laatija-id)]
    (str sukunimi ", " etunimi)))

(t/deftest find-history-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2 laatija-id-3] laatija-ids
        [laatija-1-fullname
         laatija-2-fullname
         laatija-3-fullname] (map (partial fullname laatijat) laatija-ids)
        [energiatodistus-id-1
         energiatodistus-id-2
         energiatodistus-id-3] (-> energiatodistukset keys sort)
        history-1 (service/find-history ts/*db*
                                        {:id laatija-id-1 :rooli 0}
                                        energiatodistus-id-1)
        history-2 (service/find-history ts/*db*
                                        {:id laatija-id-2 :rooli 0}
                                        energiatodistus-id-2)
        history-3 (service/find-history ts/*db*
                                        {:id laatija-id-3 :rooli 0}
                                        energiatodistus-id-3)]

    ;; Energiatodistus 1 state history
    (t/is (= 7 (-> history-1 :state-history count)))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v nil
              :new-v 0
              :type :number
              :external-api false}
             (-> history-1 :state-history first (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v 0
              :new-v 1
              :type :number
              :external-api false}
            (-> history-1 :state-history second (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-1-fullname
              :k :tila-id
              :init-v 0
              :new-v 2
              :type :number
              :external-api false}
             (-> history-1 :state-history (nth 2) (dissoc :modifytime))))
    (t/is (= :allekirjoitusaika (-> history-1 :state-history (nth 3) :k)))
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
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v nil
              :new-v 0
              :type :number
              :external-api false}
             (-> history-2 :state-history first (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v 0
              :new-v 1
              :type :number
              :external-api false}
             (-> history-2 :state-history second (dissoc :modifytime))))
    (t/is (= {:modifiedby-fullname laatija-2-fullname
              :k :tila-id
              :init-v 0
              :new-v 2
              :type :number
              :external-api false}
             (-> history-2 :state-history (nth 2) (dissoc :modifytime))))
    (t/is (= :allekirjoitusaika (-> history-2 :state-history (nth 3) :k)))
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
        clojure.lang.ExceptionInfo
        #"Forbidden"
        (service/find-history ts/*db*
                              whoami
                              energiatodistus-id-3))))))
