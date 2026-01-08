(ns solita.etp.service.complete-perusparannuspassi-test
  (:require [clojure.test :as t]
            [clojure.set :as set]
            [solita.etp.service.complete-perusparannuspassi :as service]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]))

(t/use-fixtures :each ts/fixture)

(t/deftest find-complete-perusparannuspassi-test
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1)
                         first
                         (merge {:patevyystaso 4})))
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}
        [et-id _] (energiatodistus-test-data/generate-and-insert! 2026 true laatija-id)
        ;; Create minimal PPP manually instead of using the generator
        ppp-add {:energiatodistus-id et-id
                 :valid true
                 :passin-perustiedot {}
                 :rakennuksen-perustiedot {}
                 :tulokset {:kaukolampo-hinta 6.5
                           :sahko-hinta 15.0
                           :uusiutuvat-pat-hinta 5.0
                           :fossiiliset-pat-hinta 8.0
                           :kaukojaahdytys-hinta 4.0}
                 :vaiheet [{:vaihe-nro 1
                           :valid true
                           :toimenpiteet {:toimenpide-ehdotukset []}
                           :tulokset {:ostoenergian-tarve-kaukolampo 50000
                                     :ostoenergian-tarve-sahko 30000
                                     :ostoenergian-tarve-uusiutuvat-pat 0
                                     :ostoenergian-tarve-fossiiliset-pat 0
                                     :ostoenergian-tarve-kaukojaahdytys 0}}]}
        ppp-id (:id (perusparannuspassi-service/insert-perusparannuspassi!
                      (ts/db-user laatija-id)
                      whoami
                      ppp-add))
        completed (service/find-complete-perusparannuspassi
                    ts/*db*
                    whoami
                    ppp-id)]

    (t/is (some? completed))
    (t/is (= ppp-id (:id completed)))
    (t/is (vector? (:vaiheet completed)))

    ;; Check that lahtotilanne and vaiheet are present and have
    ;; the expected structure
    (doseq [{:keys [tulokset] :as vaihe} (cons (:lahtotilanne completed) (:vaiheet completed))]
      ;; No unexpected stuff on the top-level
      (t/is (empty? (dissoc vaihe :valid :vaihe-nro :tulokset :toimenpiteet)))
      (t/is (map? tulokset))
      (t/is (boolean? (:valid vaihe)))
      (t/is (or (nil? (:vaihe-nro vaihe))
                (integer? (:vaihe-nro vaihe))))
      (t/is (contains? tulokset :e-luku))
      (t/is (contains? tulokset :e-luokka))

      (t/is (contains? tulokset :ostoenergia))
      (t/is (contains? tulokset :painotettu-ostoenergia))
      (t/is (contains? tulokset :energia-kustannukset))

      (t/is (contains? tulokset :toteutunut-ostoenergia))
      (t/is (contains? tulokset :toteutunut-energia-kustannukset))

      (t/is (contains? tulokset :co2-paastot)))

    ;; Some specific checks for lahtotilanne
    (let [lahtotilanne (:lahtotilanne completed)
          first-vaihe (first (:vaiheet completed))]
      (t/is (:valid lahtotilanne))

      ;; No keys in lahtotilanne.tulokset that are not in first-vaihe.tulokset
      (t/is (empty? (set/difference (set (keys (:tulokset lahtotilanne)))
                                    (set (keys (:tulokset first-vaihe)))))))))

(t/deftest find-complete-perusparannuspassi-not-found-test
  (t/is (nil? (service/find-complete-perusparannuspassi
                ts/*db*
                {:id -1 :rooli 0 :patevyystaso 4}
                -1))))
