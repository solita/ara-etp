(ns solita.etp.service.energiatodistus-anonymized-csv-test
  (:require [clojure.test :as t]
            [flathead.deep :as deep]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.energiatodistus :as service]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [paakayttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        laatijat (laatija-test-data/generate-and-insert! 1)]
    {:paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)
     :laatijat laatijat}))

(defn generate-add [versio kayttotarkoitus postinumero]
  (deep/deep-merge (energiatodistus-test-data/generate-add versio true)
                   {:perustiedot {:kayttotarkoitus kayttotarkoitus
                                  :postinumero postinumero}}))

(defn add-et! [et laatija-id]
  (let [et-id (first (energiatodistus-test-data/insert! [et] laatija-id))]
    (energiatodistus-test-data/sign! et-id laatija-id true)
    et-id))

(t/deftest test-protected-single-group
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat first first)]
    ;; Initially nothing protected
    (t/is (= #{} (service/find-protected-postinumerot ts/*db* 5)))

    ;; After adding one ET of the protected käyttötarkoitus, it should show up
    (add-et! (generate-add 2018 "YAT" "90500") laatija-id)
    (t/is (= #{{:postinumero "90500", :versio 2018, :kayttotarkoitus "YAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))

    ;; Add three more. Still should be marked as hidden
    (dotimes [_ 3] (add-et! (generate-add 2018 "YAT" "90500") laatija-id))
    (t/is (= #{{:postinumero "90500", :versio 2018, :kayttotarkoitus "YAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))

    ;; After inserting the fifth one, there group should disappear
    (add-et! (generate-add 2018 "YAT" "90500") laatija-id)
    (t/is (= #{} (service/find-protected-postinumerot ts/*db* 5)))))

(t/deftest test-protected-two-postinumeros
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat first first)]
    ;; Add close to the limit for a couple of groups
    (doseq [postinumero ["00130" "33100"]]
      (dotimes [_ 4] (add-et! (generate-add 2018 "YAT" postinumero) laatija-id)))

    ;; Check that these form protected groups
    (t/is (= #{{:postinumero "00130", :versio 2018, :kayttotarkoitus "YAT"}
               {:postinumero "33100", :versio 2018, :kayttotarkoitus "YAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))

    ;; Expand one group more to make it safe
    (add-et! (generate-add 2018 "YAT" "00130") laatija-id)
    (t/is (= #{{:postinumero "33100", :versio 2018, :kayttotarkoitus "YAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))))

(t/deftest test-protected-two-ktls
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat first first)]
    ;; Add close to the limit for a couple of groups
    (doseq [kayttotarkoitus ["YAT" "KAT"]]
      (dotimes [_ 4] (add-et! (generate-add 2018 kayttotarkoitus "33100") laatija-id)))

    ;; Check that these form protected groups
    (t/is (= #{{:postinumero "33100", :versio 2018, :kayttotarkoitus "KAT"}
               {:postinumero "33100", :versio 2018, :kayttotarkoitus "YAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))

    ;; Expand one group more to make it safe
    (add-et! (generate-add 2018 "YAT" "33100") laatija-id)
    (t/is (= #{{:postinumero "33100", :versio 2018, :kayttotarkoitus "KAT"}}
             (service/find-protected-postinumerot ts/*db* 5)))))
