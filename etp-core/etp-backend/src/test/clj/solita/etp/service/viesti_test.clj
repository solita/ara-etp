(ns solita.etp.service.viesti-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.service.viesti :as service]
            [solita.etp.schema.viesti :as viesti-schema]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test :as etp-test]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn laatija-whoami [id] {:id id :rooli 0})
(defn paakayttaja-whoami [id] {:id id :rooli 2})

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 4)
        paakayttaja-adds (->> (kayttaja-test-data/generate-adds 2)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)]
    {:laatijat laatijat
     :paakayttajat paakayttajat}))

(def empty-query {})

(defn find-and-count-ketjut [db whoami query]
  (let [ketjut (service/find-ketjut db whoami query)
        expected-count (min 100 (:count (service/count-ketjut db whoami query)))]
    (t/is (= expected-count (count ketjut)))
    ketjut))

(defn without-read-time [ketju]
  (assoc ketju :viestit (map #(assoc % :read-time nil)
                             (:viestit ketju))))

(defn complete-ketju-add [ketju-add]
  (generators/complete ketju-add viesti-schema/KetjuAdd))


(t/deftest laatija-ketju-visibility-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-id _]] (take 1 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ketju-id (service/add-ketju! (ts/db-user laatija-1-id)
                                     (laatija-whoami laatija-1-id)
                                     (complete-ketju-add
                                      {:vastaanottajat []
                                       :vastaanottajaryhma-id 0
                                       :energiatodistus-id nil}))]
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user laatija-1-id)
                                              (laatija-whoami laatija-1-id)
                                              ketju-id)))
          "Laatija must see their own ketju")
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-id)
                                              (paakayttaja-whoami paakayttaja-id)
                                              ketju-id)))
          "Paakayttaja must see the ketju")
    (t/is (= (-> (etp-test/catch-ex-data
                  #(service/find-ketju! (ts/db-user laatija-2-id)
                                        (laatija-whoami laatija-2-id)
                                        ketju-id))
                 (dissoc :reason))
             {:type :forbidden})
          "An other laatija must not see the ketju")))

(t/deftest paakayttaja-individual-visibility-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-1-id _] [paakayttaja-2-id _]] (take 2 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ketju-id (service/add-ketju! (ts/db-user paakayttaja-1-id)
                                     (paakayttaja-whoami paakayttaja-1-id)
                                     (complete-ketju-add
                                      {:vastaanottajat [laatija-1-id]
                                       :vastaanottajaryhma-id nil
                                       :energiatodistus-id nil}))]
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user laatija-1-id)
                                              (laatija-whoami laatija-1-id)
                                              ketju-id)))
          "Laatija must see the ketju where they are the recipient")
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-1-id)
                                              (paakayttaja-whoami paakayttaja-1-id)
                                              ketju-id)))
          "Sending paakayttaja must see the ketju")
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-2-id)
                                              (paakayttaja-whoami paakayttaja-2-id)
                                              ketju-id)))
          "Other paakayttaja must see the ketju")
    (t/is (= (-> (etp-test/catch-ex-data
                  #(service/find-ketju! (ts/db-user laatija-2-id)
                                        (laatija-whoami laatija-2-id)
                                        ketju-id))
                 (dissoc :reason))
             {:type :forbidden})
          "An other laatija must not see the ketju")))

(t/deftest paakayttaja-laatijat-group-visibility-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[sender-paakayttaja-id _]] (take 1 paakayttajat)
        ketju-id (service/add-ketju! (ts/db-user sender-paakayttaja-id)
                                     (paakayttaja-whoami sender-paakayttaja-id)
                                     (complete-ketju-add
                                      {:vastaanottajat []
                                       :vastaanottajaryhma-id 1
                                       :energiatodistus-id nil}))]
    (doseq [[laatija-id _] laatijat]
      (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user laatija-id)
                                              (laatija-whoami laatija-id)
                                              ketju-id)))
          "Laatija must see the ketju based on group membership"))

    (doseq [[paakayttaja-id _] paakayttajat]
      (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-id)
                                              (paakayttaja-whoami paakayttaja-id)
                                              ketju-id)))
          "Paakayttaja must see the ketju based on being paakayttaja"))))

(t/deftest paakayttaja-query-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-1-id _] [paakayttaja-2-id _]] (take 2 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ;; Two ketjus from one laatija and another from a second laatija
        ketju-0-id (service/add-ketju! (ts/db-user laatija-1-id)
                                       (laatija-whoami laatija-1-id)
                                       (complete-ketju-add
                                        {:vastaanottajat []
                                         :vastaanottajaryhma-id 0
                                         :energiatodistus-id nil}))
        ketju-1-id (service/add-ketju! (ts/db-user laatija-1-id)
                                       (laatija-whoami laatija-1-id)
                                       (complete-ketju-add
                                        {:vastaanottajat []
                                         :vastaanottajaryhma-id 0
                                         :energiatodistus-id nil}))
        ketju-2-id (service/add-ketju! (ts/db-user laatija-2-id)
                                       (laatija-whoami laatija-2-id)
                                       (complete-ketju-add
                                        {:vastaanottajat []
                                         :vastaanottajaryhma-id 0
                                         :energiatodistus-id nil}))
        ;; PK1 assingns ketju 0 and ketju 2 to themselves
        ketju-0-assign-res (service/update-ketju! (ts/db-user paakayttaja-1-id)
                                                  ketju-0-id
                                                  {:kasittelija-id paakayttaja-1-id})
        ketju-2-assign-res (service/update-ketju! (ts/db-user paakayttaja-1-id)
                                                  ketju-2-id
                                                  {:kasittelija-id paakayttaja-1-id})
        ;; PK1 responds in ketju 0
        ketju-0-add-viesti-res (service/add-viesti! (ts/db-user paakayttaja-1-id)
                                                    (paakayttaja-whoami paakayttaja-1-id)
                                                    ketju-0-id
                                                    "Vastaus ekaan ketjuun")
        ;; PK1 marks ketju 0 as done
        ketju-0-done-res (service/update-ketju! (ts/db-user paakayttaja-1-id)
                                               ketju-0-id
                                               {:kasitelty true})
        ]
    ;; Check that the above requests succeeded
    (t/is (int? ketju-0-id) "Must have gotten an int from add-ketju!")
    (t/is (int? ketju-0-add-viesti-res))
    (t/is (= 1 ketju-0-assign-res))
    (t/is (= 1 ketju-2-assign-res))
    (t/is (= 1 ketju-0-done-res))

    ;; All ketjus
    (t/is (= #{ketju-0-id ketju-1-id ketju-2-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-2-id)
                                          (paakayttaja-whoami paakayttaja-2-id)
                                          {:include-kasitelty true})))
          "Paakayttaja 2 should see all ketjut")
    (t/is (= #{ketju-0-id ketju-1-id ketju-2-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-1-id)
                                          (paakayttaja-whoami paakayttaja-1-id)
                                          {:include-kasitelty true})))
          "Paakayttaja 1 should see all ketjut")
    ;; Organizational overview of ongoing and upcoming work
    (t/is (= #{ketju-1-id ketju-2-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-1-id)
                                          (paakayttaja-whoami paakayttaja-1-id)
                                          {:include-kasitelty false})))
          "Expected to get every ketju that is not done")

    ;; Personal ongoing work
    (t/is (= #{ketju-2-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-1-id)
                                          (paakayttaja-whoami paakayttaja-1-id)
                                          {:include-kasitelty false
                                           :kasittelija-id paakayttaja-1-id})))
          "Expected to get every unfinished ketju of Paakayttaja 1")
    ;; Personal everything
    (t/is (= #{ketju-0-id ketju-2-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-1-id)
                                          (paakayttaja-whoami paakayttaja-1-id)
                                          {:include-kasitelty true
                                           :kasittelija-id paakayttaja-1-id})))
          "Expected to get everything for Paakayttaja 1")
    ;; Query for someone who is looking to take on more work
    (t/is (= #{ketju-1-id}
             (into #{} (map :id)
                   (find-and-count-ketjut (ts/db-user paakayttaja-2-id)
                                          (paakayttaja-whoami paakayttaja-2-id)
                                          {:include-kasitelty false
                                           :has-kasittelija false})))
          "Expected that Paakayttaja 2 would get ketju 1")))

(t/deftest add-ketju!-by-laatija-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-id _]] (take 1 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ketju-id (service/add-ketju! (ts/db-user laatija-1-id)
                                     (laatija-whoami laatija-1-id)
                                     (complete-ketju-add
                                      {:vastaanottajat []
                                       :vastaanottajaryhma-id 0
                                       :energiatodistus-id nil}))
        ketju (service/find-ketju! (ts/db-user laatija-1-id)
                                   (laatija-whoami laatija-1-id)
                                   ketju-id)]
    (t/is (nil? (schema/check viesti-schema/Ketju ketju)))
    (t/is (empty? (:vastaanottajat ketju)))

    (t/is (nil? (:kasittelija-id ketju)))
    (t/is (false? (:kasitelty ketju)))
    (t/is (= laatija-1-id (->> ketju :viestit first :from :id)))
    (t/is (= (without-read-time ketju)
             (without-read-time (service/find-ketju! (ts/db-user paakayttaja-id)
                                                     (paakayttaja-whoami paakayttaja-id)
                                                     ketju-id)))
          "Paakayttaja must be able to get the same ketju")
    (t/is (= (-> (etp-test/catch-ex-data
                  #(service/find-ketju! (ts/db-user laatija-2-id)
                                        (laatija-whoami laatija-2-id)
                                        ketju-id))
                 (dissoc :reason))
             {:type :forbidden})
          "An other laatija must not see the ketju")))

(t/deftest add-ketju!-by-paakayttaja-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-id _]] (take 1 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ketju-id (service/add-ketju! (ts/db-user paakayttaja-id)
                                     (paakayttaja-whoami paakayttaja-id)
                                     (complete-ketju-add
                                      {:vastaanottajat [laatija-1-id]
                                       :vastaanottajaryhma-id nil
                                       :energiatodistus-id nil}))
        ketju (service/find-ketju! (ts/db-user paakayttaja-id)
                                   (paakayttaja-whoami paakayttaja-id)
                                   ketju-id)]
    (t/is (nil? (schema/check viesti-schema/Ketju ketju)))
    (t/is (= laatija-1-id (->> ketju :vastaanottajat first :id)))

    ;; default kasittelija is paakayttaja
    (t/is (= (:kasittelija-id ketju) paakayttaja-id))

    ;; TODO jos ei pidä asettaa käsittelijää, pitäisikö asettaa
    ;; käsitellyksi?
    (t/is (false? (:kasitelty ketju)))
    (t/is (= paakayttaja-id (->> ketju :viestit first :from :id)))
    (t/is (= (without-read-time ketju)
             (without-read-time (service/find-ketju! (ts/db-user laatija-1-id)
                                                     (laatija-whoami laatija-1-id)
                                                     ketju-id)))
          "The recipient laatija must be able to get the same ketju")
    (t/is (= (-> (etp-test/catch-ex-data
                  #(service/find-ketju! (ts/db-user laatija-2-id)
                                        (laatija-whoami laatija-2-id)
                                        ketju-id))
                 (dissoc :reason))
             {:type :forbidden})
          "An other laatija must not see the ketju")))

(t/deftest update-ketju-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-1-id _] [paakayttaja-2-id _]] (take 2 paakayttajat)
        [[laatija-id _]] (take 1 laatijat)
        ketju-id (service/add-ketju! (ts/db-user paakayttaja-1-id)
                                     (paakayttaja-whoami paakayttaja-1-id)
                                     (complete-ketju-add
                                      {:vastaanottajat [laatija-id]
                                       :vastaanottajaryhma-id nil
                                       :energiatodistus-id nil}))]
    (t/is (not= paakayttaja-2-id
                (:kasittelija-id (service/find-ketju! (ts/db-user paakayttaja-1-id)
                                                      (paakayttaja-whoami paakayttaja-1-id)
                                                      ketju-id))))
    (service/update-ketju! (ts/db-user paakayttaja-1-id) ketju-id {:kasittelija-id paakayttaja-2-id})
    (t/is (= paakayttaja-2-id
             (:kasittelija-id (service/find-ketju! (ts/db-user paakayttaja-1-id)
                                                   (paakayttaja-whoami paakayttaja-1-id)
                                                   ketju-id))))))

(t/deftest ketju-unreads-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-1-id _] [paakayttaja-2-id _]] (take 2 paakayttajat)
        [[laatija-id _]] (take 1 laatijat)
        paakayttaja-count-unread (fn [paakayttaja-id]
                                   (:count
                                    (service/count-unread-ketjut
                                     (ts/db-user paakayttaja-id)
                                     (paakayttaja-whoami paakayttaja-id))))
        unreads-before (paakayttaja-count-unread paakayttaja-1-id)
        ketju-add (complete-ketju-add
                   {:vastaanottajat []
                    :vastaanottajaryhma-id 0
                    :energiatodistus-id nil})
        ketju-id (service/add-ketju! (ts/db-user laatija-id)
                                     (laatija-whoami laatija-id)
                                     ketju-add)]
    (t/is (= 0 unreads-before))
    (t/is (= 1 (paakayttaja-count-unread paakayttaja-1-id)))
    (t/is (= 1 (paakayttaja-count-unread paakayttaja-2-id)))
    (t/is (= ketju-id
             (:id (service/read-ketju! (ts/db-user paakayttaja-1-id)
                                       (paakayttaja-whoami paakayttaja-1-id)
                                       ketju-id))))
    (t/is (= 0 (paakayttaja-count-unread paakayttaja-1-id)))
    (t/is (= 1 (paakayttaja-count-unread paakayttaja-2-id)))))

(t/deftest laatija-question-sequence-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-id _]] (take 1 paakayttajat)
        [[laatija-id _]] (take 1 laatijat)
        make-unread-checker (fn [whoami-fn kayttaja-id]
                              (fn []
                                (:count
                                 (service/count-unread-ketjut
                                  (ts/db-user kayttaja-id)
                                  (whoami-fn kayttaja-id)))))
        laatija-count-unread (make-unread-checker laatija-whoami laatija-id)
        paakayttaja-count-unread (make-unread-checker paakayttaja-whoami paakayttaja-id)
        paakayttaja-count-not-kasitelty (fn [] (count
                                                (find-and-count-ketjut (ts/db-user paakayttaja-id)
                                                                       (paakayttaja-whoami paakayttaja-id)
                                                                       {:include-kasitelty false})))
        paakayttaja-count-omat (fn [] (count
                                       (find-and-count-ketjut (ts/db-user paakayttaja-id)
                                                              (paakayttaja-whoami paakayttaja-id)
                                                              {:include-kasitelty false
                                                               :kasittelija-id paakayttaja-id})))
        ketju-count-before (paakayttaja-count-not-kasitelty)
        ketju-id (service/add-ketju! (ts/db-user laatija-id)
                                     (laatija-whoami laatija-id)
                                     (complete-ketju-add
                                      {:body "Hello"
                                       :vastaanottajat []
                                       :vastaanottajaryhma-id 0
                                       :energiatodistus-id nil}))]
    (t/is (= 0 ketju-count-before))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 0 (paakayttaja-count-omat)))
    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 1 (paakayttaja-count-unread)))
    (t/is (= ketju-id
             (:id (service/read-ketju! (ts/db-user paakayttaja-id)
                                       (paakayttaja-whoami paakayttaja-id)
                                       ketju-id))))
    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 0 (paakayttaja-count-omat)))

    (t/is (= 1 (service/update-ketju! (ts/db-user paakayttaja-id)
                                      ketju-id {:kasittelija-id paakayttaja-id})))
    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 1 (paakayttaja-count-omat)))

    (t/is (= 1 (service/add-viesti! (ts/db-user paakayttaja-id)
                                    (paakayttaja-whoami paakayttaja-id)
                                    ketju-id "Heippa vaan")))
    (t/is (= 1 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 1 (paakayttaja-count-omat)))

    (t/is (= #{"Hello" "Heippa vaan"}
             (->> (service/read-ketju! (ts/db-user laatija-id)
                                       (laatija-whoami laatija-id)
                                       ketju-id)
                  :viestit
                  (map #(:body %))
                  set)))

    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 1 (paakayttaja-count-omat)))

    (t/is (= 1 (service/update-ketju! (ts/db-user paakayttaja-id)
                                      ketju-id {:kasitelty true})))

    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 0 (paakayttaja-count-not-kasitelty)))
    (t/is (= 0 (paakayttaja-count-omat)))

    ;; End of typical sequence
    ;;
    ;; Also check the case from AE-1321 where an added question is
    ;; posted. It is expected that the kasitelty flag gets cleared.
    (t/is (= 1 (service/add-viesti! (ts/db-user laatija-id)
                                    (laatija-whoami laatija-id)
                                    ketju-id "Vielä yksi juttu")))
    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 1 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 1 (paakayttaja-count-omat)))

    (t/is (= 2 (service/add-viesti! (ts/db-user paakayttaja-id)
                                    (paakayttaja-whoami paakayttaja-id)
                                    ketju-id "Juu")))
    (t/is (= 1 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 1 (paakayttaja-count-not-kasitelty)))
    (t/is (= 1 (paakayttaja-count-omat)))

    (t/is (= #{"Hello" "Heippa vaan" "Vielä yksi juttu" "Juu"}
             (->> (service/read-ketju! (ts/db-user laatija-id)
                                       (laatija-whoami laatija-id)
                                       ketju-id)
                  :viestit
                  (map #(:body %))
                  set)))

    (t/is (= 1 (service/update-ketju! (ts/db-user paakayttaja-id)
                                      ketju-id {:kasitelty true})))

    (t/is (= 0 (laatija-count-unread)))
    (t/is (= 0 (paakayttaja-count-unread)))
    (t/is (= 0 (paakayttaja-count-not-kasitelty)))
    (t/is (= 0 (paakayttaja-count-omat)))))

(t/deftest test-viesti-many-users-test
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-1-id _] [paakayttaja-2-id _]] (take 2 paakayttajat)
        [[laatija-1-id _] [laatija-2-id _]] (take 2 laatijat)
        ketju-count-before (-> (service/count-ketjut (ts/db-user laatija-1-id)
                                                     (laatija-whoami laatija-1-id)
                                                     empty-query)
                               :count)
        ketju-id (service/add-ketju! (ts/db-user paakayttaja-1-id)
                                     (paakayttaja-whoami paakayttaja-1-id)
                                     (complete-ketju-add
                                      {:vastaanottajat [laatija-1-id]
                                       :vastaanottajaryhma-id nil
                                       :energiatodistus-id nil}))
        ketju-count-after (-> (service/count-ketjut (ts/db-user laatija-1-id)
                                                    (laatija-whoami laatija-1-id)
                                                    empty-query)
                              :count)
        ketju-count-laatija-2 (-> (service/count-ketjut (ts/db-user laatija-2-id)
                                                        (laatija-whoami laatija-2-id)
                                                        empty-query)
                                  :count)
        ketju-count-pk (-> (service/count-ketjut (ts/db-user paakayttaja-1-id)
                                                 (paakayttaja-whoami paakayttaja-1-id)
                                                 empty-query)
                                  :count)
        ketju-count-pk2 (-> (service/count-ketjut (ts/db-user paakayttaja-2-id)
                                                  (paakayttaja-whoami paakayttaja-2-id)
                                                  empty-query)
                                  :count)]
    (t/is (= 0 ketju-count-before) "Must have no visible ketjus before one is added")
    (t/is (int? ketju-id) "Must have gotten an int from add-ketju!")
    (t/is (= 1 ketju-count-after) "Must have one ketju after the add")

    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user laatija-1-id)
                                              (laatija-whoami laatija-1-id)
                                              ketju-id)))
          "The recipient laatija must see the ketju")

    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-1-id)
                                              (paakayttaja-whoami paakayttaja-1-id)
                                              ketju-id)))
          "The sending paakayttaja must see the ketju")
    (t/is (nil?
           (schema/check viesti-schema/Ketju
                         (service/find-ketju! (ts/db-user paakayttaja-2-id)
                                              (paakayttaja-whoami paakayttaja-2-id)
                                              ketju-id)))
          "Other paakayttaja must see the ketju")
    (t/is (= (-> (etp-test/catch-ex-data
                  #(service/find-ketju! (ts/db-user laatija-2-id)
                                        (laatija-whoami laatija-2-id)
                                        ketju-id))
                 (dissoc :reason))
             {:type :forbidden})
          "An other laatija must not see the ketju")
    (t/is (nil? (service/find-ketju! (ts/db-user laatija-1-id)
                                     (laatija-whoami laatija-1-id)
                                     (inc ketju-id)))
          "Nil is expected from find of a non-existent ketju")))

(t/deftest remove-kasitelty-on-new-viesti-test
  "Check that we meet requirements in AE-1321"
  (let [{:keys [laatijat paakayttajat]} (test-data-set)
        [[paakayttaja-id _]] (take 1 paakayttajat)
        [[laatija-id _]] (take 1 laatijat)
        ketju-id (service/add-ketju! (ts/db-user laatija-id)
                                     (laatija-whoami laatija-id)
                                     (complete-ketju-add
                                      {:vastaanottajat []
                                       :vastaanottajaryhma-id 0
                                       :energiatodistus-id nil}))]
    (t/is (not (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                                (paakayttaja-whoami paakayttaja-id)
                                                ketju-id))))
    (service/add-viesti! (ts/db-user paakayttaja-id)
                         (paakayttaja-whoami paakayttaja-id)
                         ketju-id
                         "Pääkäyttäjän vastaus")
    (service/add-viesti! (ts/db-user laatija-id)
                         (laatija-whoami laatija-id)
                         ketju-id
                         "Ok")
    (t/is (not (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                                (paakayttaja-whoami paakayttaja-id)
                                                ketju-id))))
    (t/is (= 1 (service/update-ketju! (ts/db-user paakayttaja-id)
                                      ketju-id {:kasitelty true})))
    (t/is (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                           (paakayttaja-whoami paakayttaja-id)
                                           ketju-id)))
    (service/add-viesti! (ts/db-user laatija-id)
                                    (laatija-whoami laatija-id)
                                    ketju-id "Niin vielä yks juttu")
    (t/is (not (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                                (paakayttaja-whoami paakayttaja-id)
                                                ketju-id))))
    (service/add-viesti! (ts/db-user paakayttaja-id)
                                    (paakayttaja-whoami paakayttaja-id)
                                    ketju-id "Toinen vastaus")
    (t/is (not (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                                (paakayttaja-whoami paakayttaja-id)
                                                ketju-id))))
    (t/is (= 1 (service/update-ketju! (ts/db-user paakayttaja-id)
                                      ketju-id {:kasitelty true})))
    (t/is (:kasitelty (service/find-ketju! (ts/db-user paakayttaja-id)
                                           (paakayttaja-whoami paakayttaja-id)
                                           ketju-id)))))

