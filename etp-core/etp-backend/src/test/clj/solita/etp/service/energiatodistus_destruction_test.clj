(ns solita.etp.service.energiatodistus-destruction-test
  (:require [clojure.test :as t]
            [solita.common.time :as time]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.valvonta-oikeellisuus :as valvonta-oikeellisuus-service]
            [solita.etp.service.energiatodistus-destruction :as service]
            [solita.etp.service.viesti-test :as viesti-test]
            [solita.etp.service.valvonta-oikeellisuus.asha :as vo-asha-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.liite :as liite-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.whoami :as test-whoami]
            [solita.etp.test-system :as ts])
  (:import (java.time Instant LocalDate ZoneId Duration)))

(t/use-fixtures :each ts/fixture)

(def system-expiration-user {:id (kayttaja-service/system-kayttaja :expiration) :rooli -1})

(defn file-exists? [file-key] (file-service/file-exists? ts/*aws-s3-client* file-key))

(defn update-energiatodistus! [energiatodistus-id energiatodistus laatija-id]
  (energiatodistus-service/update-energiatodistus! (ts/db-user laatija-id)
                                                   {:id laatija-id :rooli 0}
                                                   energiatodistus-id
                                                   energiatodistus))

(defn expire-energiatodistus!
  "Sets voimassaolo-paattymisaika to two days ago"
  [energiatodistus-id]
  (jdbc/execute! ts/*db* ["update energiatodistus set voimassaolo_paattymisaika = CURRENT_DATE - INTERVAL '2 days' where id = ?" energiatodistus-id])
  (jdbc/execute! ts/*db* ["update vo_toimenpide set create_time = CURRENT_DATE - INTERVAL '3 years' where energiatodistus_id = ?" energiatodistus-id]))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 4)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2 laatija-id-3 laatija-id-4] laatija-ids
        energiatodistus-adds (energiatodistus-test-data/generate-adds 4
                                                                      2018
                                                                      true)
        energiatodistus-ids (mapcat #(energiatodistus-test-data/insert!
                                       [%1]
                                       %2)
                                    energiatodistus-adds
                                    laatija-ids)
        [energiatodistus-id-1 energiatodistus-id-2 energiatodistus-id-3 energiatodistus-id-4] energiatodistus-ids
        [energiatodistus-add-1 energiatodistus-add-2 energiatodistus-add-3 energiatodistus-add-4] energiatodistus-adds]

    ;; Sign energiatodistus 1
    (energiatodistus-test-data/sign! energiatodistus-id-1 laatija-id-1 true)

    (update-energiatodistus! energiatodistus-id-2
                             (assoc energiatodistus-add-2
                               ;; Korvaa energiatodistus 1 with energiatodistus 2
                               :korvattu-energiatodistus-id
                               energiatodistus-id-1
                               ;; Expire energiatodistus 2
                               :voimassaolo-paattymisaika
                               (LocalDate/ofInstant (Instant/ofEpochSecond 0) (ZoneId/of "Europe/Helsinki")))
                             laatija-id-2)

    (update-energiatodistus! energiatodistus-id-3
                             (assoc energiatodistus-add-3
                               ;; Set expiration of energiatodistus 3 to today
                               :voimassaolo-paattymisaika
                               (time/now))
                             laatija-id-3)

    (expire-energiatodistus! energiatodistus-id-4)


    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest get-currently-expired-todistus-ids-without-valvonta-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2 id-3 id-4] ids
        expired-ids (service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "Todistus with expiration date set by signing it today should not be expired."
      (t/is (nil? (some #{id-1} expired-ids))))
    (t/testing "Todistus with expiration time at year 1970 should be expired."
      (t/is (some #{id-2} expired-ids)))
    (t/testing "Todistus whose expiration is today should not be expired yet."
      (t/is (nil? (some #{id-3} expired-ids))))
    (t/testing "Todistus with expiration date at yesterday should be expired."
      (t/is (some #{id-4} expired-ids)))))


(defn- add-valvonta-and-modify-create-time [paakayttaja-id energiatodistus-id create-time]
  (let [rfi-reply {:type-id       4
                   :deadline-date nil
                   :template-id   nil
                   :description   "Test"
                   :virheet       []
                   :severity-id   nil
                   :tiedoksi      []}
        {valvonta-id :id} (valvonta-oikeellisuus-service/add-toimenpide! (ts/db-user paakayttaja-id)
                                                                         ts/*aws-s3-client*
                                                                         (test-whoami/paakayttaja paakayttaja-id)
                                                                         energiatodistus-id rfi-reply)]
    ;; Set the create-time
    (valvonta-oikeellisuus-service/update-toimenpide! ts/*db*
                                                      (test-whoami/paakayttaja paakayttaja-id)
                                                      energiatodistus-id
                                                      valvonta-id
                                                      {:create-time create-time})))

(t/deftest get-currently-expired-todistus-ids-with-recent-valvonta-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        add-valvonta #(partial add-valvonta-and-modify-create-time paakayttaja-id % (time/now))
        _ (doall (map #(%) (map add-valvonta ids)))
        expired-ids (service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "None of the energiatodistukset should be expired as they have a recent valvonta"
      (t/is (empty? expired-ids)))))

(t/deftest get-currently-expired-todistus-ids-with-old-valvonta-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        [id-1 id-2 id-3 id-4] ids
        add-valvonta #(partial add-valvonta-and-modify-create-time paakayttaja-id % (.minus (time/now) (Duration/ofDays 735)))
        _ (doall (map #(%) (map add-valvonta ids)))
        expired-ids (service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "Valvonta should not affect the expiration as it is older than two years"
      (t/is (nil? (some #{id-1} expired-ids)))
      (t/is (some #{id-2} expired-ids))
      (t/is (nil? (some #{id-3} expired-ids)))
      (t/is (some #{id-4} expired-ids)))))

(t/deftest get-currently-expired-todistus-ids-with-almost-old-valvonta-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        add-valvonta #(partial add-valvonta-and-modify-create-time paakayttaja-id % (.minus (time/now) (Duration/ofDays 720)))
        _ (doall (map #(%) (map add-valvonta ids)))
        expired-ids (service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "None of the energiatodistukset should be expired as they have a recent valvonta"
      (t/is (empty? expired-ids)))))

(t/deftest destroy-energiatodistus-pdf-test
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistus-add-fi (-> (energiatodistus-test-data/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))
        energiatodistus-add-sv (-> (energiatodistus-test-data/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 1))
        energiatodistus-add-multilingual (-> (energiatodistus-test-data/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))
        energiatodistus-add-control (-> (energiatodistus-test-data/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 2))
        energiatodistus-ids (energiatodistus-test-data/insert!
                              [energiatodistus-add-fi energiatodistus-add-sv energiatodistus-add-multilingual energiatodistus-add-control]
                              laatija-id)
        [energiatodistus-id-fi energiatodistus-id-sv energiatodistus-id-mu energiatodistus-id-control] energiatodistus-ids
        lang-fi-pdf-fi-key (energiatodistus-service/file-key energiatodistus-id-fi "fi")
        lang-fi-pdf-sv-key (energiatodistus-service/file-key energiatodistus-id-fi "sv")
        lang-sv-pdf-fi-key (energiatodistus-service/file-key energiatodistus-id-sv "fi")
        lang-sv-pdf-sv-key (energiatodistus-service/file-key energiatodistus-id-sv "sv")
        lang-mu-pdf-fi-key (energiatodistus-service/file-key energiatodistus-id-mu "fi")
        lang-mu-pdf-sv-key (energiatodistus-service/file-key energiatodistus-id-mu "sv")
        control-pdf-fi-key (energiatodistus-service/file-key energiatodistus-id-control "fi")
        control-pdf-sv-key (energiatodistus-service/file-key energiatodistus-id-control "sv")]

    ;; Sign the control pdf (it should then always exist)
    (energiatodistus-test-data/sign! energiatodistus-id-control laatija-id false)

    (t/testing "PDFs should not exist in the bucket before signing."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    ;; Sign Finnish
    (energiatodistus-test-data/sign! energiatodistus-id-fi laatija-id false)

    (t/testing "Finnish version PDF should exist after signing it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    (expire-energiatodistus! energiatodistus-id-fi)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "Finnish version PDF should not exist after deleting it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    ;; Sign Swedish
    (energiatodistus-test-data/sign! energiatodistus-id-sv laatija-id false)

    (t/testing "Swedish version PDF should exist after signing it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    (expire-energiatodistus! energiatodistus-id-sv)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "Swedish version PDF should not exist after deleting it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    ;; Sign Multilingual
    (energiatodistus-test-data/sign! energiatodistus-id-mu laatija-id false)

    (t/testing "Multilingual version PDFs should exist after signing it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))

    (expire-energiatodistus! energiatodistus-id-mu)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "Multilingual version PDFs should not exist after deleting it."
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-fi-key)))
      (t/is (true? (file-service/file-exists? ts/*aws-s3-client* control-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-fi-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-sv-pdf-sv-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-fi-key)))
      (t/is (false? (file-service/file-exists? ts/*aws-s3-client* lang-mu-pdf-sv-key))))))

(defn- collect-invalid-keys-for-destroyed-energiatodistus
  "Helper function for deducing which keys have an incorrect value. Also
  contains the information on what the correct value should be."
  [energiatodistus-map]
  (map (fn [[key value]]
         {:key key :value value :valid (case key
                                         ;; Here are what anonymized values should be.
                                         :id (number? value)
                                         ;; Versio is kept
                                         :versio (number? value)
                                         :tila_id (= 6 value)
                                         :laatija_id (number? value)
                                         :korvattu_energiatodistus_id (or (nil? value) (number? value))
                                         :bypass_validation_limits (false? value)
                                         :pt$julkinen_rakennus (false? value)
                                         :pt$uudisrakennus (false? value)
                                         :laskutettava_yritys_defined (false? value)
                                         :valvonta$pending (false? value)
                                         :valvonta (false? value)
                                         :draft_visible_to_paakayttaja (false? value)
                                         ;; Other values are null
                                         (nil? value))})
       energiatodistus-map))

(t/deftest anonymize-energiatodistus-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        select-energiatodistus #(jdbc/query ts/*db*
                                            ["select * from energiatodistus where id = ?" %])
        ids (-> energiatodistukset keys sort)
        [id-1] ids
        get-et-1 #(first (select-energiatodistus id-1))]
    (expire-energiatodistus! id-1)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)
    (t/testing "The values are anonymized."
      (t/is (empty? (->> (get-et-1)
                         (collect-invalid-keys-for-destroyed-energiatodistus)
                         (filter #(-> %
                                      :valid
                                      (false?)))
                         (map #(select-keys % [:key :value]))))))))

(t/deftest destroy-energiatodistus-audit-information-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        select-audit-information #(jdbc/query ts/*db*
                                              ["select * from audit.energiatodistus where id = ?" %])
        ids (-> energiatodistukset keys sort)
        [id-1 id-2] ids
        get-et-1-audit-information #(select-audit-information id-1)
        get-et-2-audit-information #(select-audit-information id-2)]
    (t/testing "There was some audit information before deletion."
      (t/is (not (empty? (get-et-1-audit-information)))))
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)
    (t/testing "The audit data for et-1 still exists as it is not expired."
      (t/is (not (empty? (get-et-1-audit-information)))))
    (t/testing "The audit data for et-2 is destroyed as it is expired."
      (t/is (empty? (get-et-2-audit-information))))))

(t/deftest destroy-energiatodistus-oikeellisuuden-valvonta-test
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistus-add (energiatodistus-test-data/generate-add 2018 true)
        [energiatodistus-id-1
         energiatodistus-id-2] (energiatodistus-test-data/insert! [energiatodistus-add energiatodistus-add] laatija-id)
        select-toimenpiteet #(jdbc/query ts/*db* ["select * from vo_toimenpide where energiatodistus_id = ?" %])
        select-notes #(jdbc/query ts/*db* ["select * from vo_note where energiatodistus_id = ?" %])
        select-tiedoksi #(jdbc/query ts/*db* ["select * from vo_tiedoksi where toimenpide_id in (select id from vo_toimenpide where energiatodistus_id = ?)" %])
        select-virheet #(jdbc/query ts/*db* ["select * from vo_virhe where toimenpide_id in (select id from vo_toimenpide where energiatodistus_id = ?)" %])

        select-toimenpiteet-audit #(jdbc/query ts/*db* ["select * from audit.vo_toimenpide where energiatodistus_id = ?" %])
        select-notes-audit #(jdbc/query ts/*db* ["select * from audit.vo_note where energiatodistus_id = ?" %])
        ;; tiedoksi and virheet do not have audit tables.

        get-vo-toimenpiteet #(select-toimenpiteet %)
        get-vo-notes #(select-notes %)
        get-vo-tiedoksi #(select-tiedoksi %)
        get-vo-virheet #(select-virheet %)

        vo_note {:description "Tämä on testi"}
        vo_tiedoksi {:name "Testihenkilö" :email "test@etp.fi"}
        {virhetype-id :id} (valvonta-oikeellisuus-service/add-virhetype! (ts/db-user paakayttaja-id)
                                                                         {:label-fi       "test"
                                                                          :label-sv       "test"
                                                                          :ordinal        0 :valid true
                                                                          :description-fi "test"
                                                                          :description-sv "test"})
        vo_virhe {:description "Test"
                  :type-id     virhetype-id}

        audit-report {:type-id       7
                      :deadline-date nil
                      :template-id   nil
                      :description   "Test"
                      :virheet       [vo_virhe]
                      :severity-id   nil
                      :tiedoksi      [vo_tiedoksi vo_tiedoksi]}
        vo-toimenpide-1-id (:id (valvonta-oikeellisuus-service/add-toimenpide! (ts/db-user paakayttaja-id)
                                                                               ts/*aws-s3-client*
                                                                               (test-whoami/paakayttaja paakayttaja-id)
                                                                               energiatodistus-id-1 audit-report))
        vo-toimenpide-2-id (:id (valvonta-oikeellisuus-service/add-toimenpide! (ts/db-user paakayttaja-id)
                                                                               ts/*aws-s3-client*
                                                                               (test-whoami/paakayttaja paakayttaja-id)
                                                                               energiatodistus-id-2 audit-report))]

    (valvonta-oikeellisuus-service/add-note! ts/*db* energiatodistus-id-1 (:description vo_note))

    (valvonta-oikeellisuus-service/update-toimenpide! (ts/db-user paakayttaja-id)
                                                      (test-whoami/paakayttaja paakayttaja-id)
                                                      energiatodistus-id-1 vo-toimenpide-1-id {:template-id 1})
    (valvonta-oikeellisuus-service/publish-toimenpide! ts/*db*
                                                       ts/*aws-s3-client*
                                                       (test-whoami/paakayttaja paakayttaja-id)
                                                       energiatodistus-id-1
                                                       vo-toimenpide-1-id)
    (valvonta-oikeellisuus-service/add-note! ts/*db* energiatodistus-id-2 (:description vo_note))
    (valvonta-oikeellisuus-service/update-toimenpide! (ts/db-user paakayttaja-id)
                                                      (test-whoami/paakayttaja paakayttaja-id)
                                                      energiatodistus-id-2 vo-toimenpide-2-id {:template-id 1})
    (valvonta-oikeellisuus-service/publish-toimenpide! ts/*db*
                                                       ts/*aws-s3-client*
                                                       (test-whoami/paakayttaja paakayttaja-id)
                                                       energiatodistus-id-2
                                                       vo-toimenpide-2-id)

    (t/testing "There is some toimenpide before deletion."
      (t/is (not (empty? (get-vo-toimenpiteet energiatodistus-id-1)))))
    (t/testing "There is some note before deletion."
      (t/is (not (empty? (get-vo-notes energiatodistus-id-1)))))
    (t/testing "There is some tiedoksi before deletion."
      (t/is (not (empty? (get-vo-tiedoksi energiatodistus-id-1)))))
    (t/testing "There is some virheet before deletion."
      (t/is (not (empty? (get-vo-virheet energiatodistus-id-1)))))


    (t/testing "There was some audit data on toimenpiteet before deletion."
      (t/is (not (empty? (select-toimenpiteet-audit energiatodistus-id-1)))))
    (t/testing "There was some audit data on notes before deletion."
      (t/is (not (empty? (select-notes-audit energiatodistus-id-1)))))

    (t/testing "There are valvonta liite documents"
      (let [file-key-et-1 (vo-asha-service/file-path energiatodistus-id-1 vo-toimenpide-1-id)
            file-key-et-2 (vo-asha-service/file-path energiatodistus-id-2 vo-toimenpide-2-id)]
        (t/is (true? (file-service/file-exists? ts/*aws-s3-client* file-key-et-1)))
        (t/is (true? (file-service/file-exists? ts/*aws-s3-client* file-key-et-2)))))

    ;; wait for emails to finish
    (Thread/sleep 5000)

    (expire-energiatodistus! energiatodistus-id-1)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "Energiatodistus 1 valvonta documents are destroyed and energiatodistus 2 are not"
      (let [file-key-et-1 (vo-asha-service/file-path energiatodistus-id-1 vo-toimenpide-1-id)
            file-key-et-2 (vo-asha-service/file-path energiatodistus-id-2 vo-toimenpide-2-id)]
        (t/is (false? (file-service/file-exists? ts/*aws-s3-client* file-key-et-1)))
        (t/is (true? (file-service/file-exists? ts/*aws-s3-client* file-key-et-2)))))

    (t/testing "There are no more toimenpiteet after deletion."
      (t/is (empty? (get-vo-toimenpiteet energiatodistus-id-1))))
    (t/testing "There are no more notes after deletion."
      (t/is (empty? (get-vo-notes energiatodistus-id-1))))
    (t/testing "There are no more tiedoksi after deletion."
      (t/is (empty? (get-vo-tiedoksi energiatodistus-id-1))))
    (t/testing "There are no more virheet after deletion."
      (t/is (empty? (get-vo-virheet energiatodistus-id-1))))
    (t/testing "Didn't affect the other energiatodistus"
      (t/is (not (empty? (get-vo-toimenpiteet energiatodistus-id-2))))
      (t/is (not (empty? (get-vo-notes energiatodistus-id-2))))
      (t/is (not (empty? (get-vo-tiedoksi energiatodistus-id-2))))
      (t/is (not (empty? (get-vo-virheet energiatodistus-id-2)))))

    (t/testing "The audit data on toimenpiteet is destroyed."
      (t/is (empty? (select-toimenpiteet-audit energiatodistus-id-1))))
    (t/testing "The audit data for et-2 toimenpiteet still exists."
      (t/is (not (empty? (select-toimenpiteet-audit energiatodistus-id-2)))))
    (t/testing "The audit data on notes is destroyed."
      (t/is (empty? (select-notes-audit energiatodistus-id-1))))
    (t/testing "The audit data for et-2 notes still exists."
      (t/is (not (empty? (select-notes-audit energiatodistus-id-2)))))))

(t/deftest destroy-liite-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        select-liitteet #(jdbc/query ts/*db* ["select * from liite where energiatodistus_id = ?" %])
        select-liitteet-audit #(jdbc/query ts/*db* ["select * from audit.liite where energiatodistus_id = ?" %])
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                             2
                             2013
                             true
                             laatija-id)
        [energiatodistus-id-1 energiatodistus-id-2] (-> energiatodistukset keys sort)
        file-liitteet-1 (liite-test-data/generate-and-insert-files! 2
                                                                    laatija-id
                                                                    energiatodistus-id-1)
        link-liitteet-1 (liite-test-data/generate-and-insert-links! 2
                                                                    laatija-id
                                                                    energiatodistus-id-1)
        file-liitteet-2 (liite-test-data/generate-and-insert-files! 2
                                                                    laatija-id
                                                                    energiatodistus-id-2)
        link-liitteet-2 (liite-test-data/generate-and-insert-links! 2
                                                                    laatija-id
                                                                    energiatodistus-id-2)
        file-liitteet-keys-1 (->> file-liitteet-1
                                  keys
                                  (map #(liite-service/file-key %)))
        link-liitteet-keys-1 (->> link-liitteet-1
                                  keys
                                  (map #(liite-service/file-key %)))
        file-liitteet-keys-2 (->> file-liitteet-2
                                  keys
                                  (map #(liite-service/file-key %)))
        link-liitteet-keys-2 (->> link-liitteet-2
                                  keys
                                  (map #(liite-service/file-key %)))]

    (t/testing "The liitteet exist before deletion"
      (let [liitteet-1-in-db (select-liitteet energiatodistus-id-1)
            liitteet-2-in-db (select-liitteet energiatodistus-id-2)]
        (t/is (every? file-exists? file-liitteet-keys-1))
        (t/is (every? file-exists? file-liitteet-keys-2))

        (t/is (not-any? file-exists? link-liitteet-keys-1))
        (t/is (not-any? file-exists? link-liitteet-keys-2))

        (t/is (= 4 (count liitteet-1-in-db)))
        (t/is (= 4 (count liitteet-2-in-db)))))

    (t/testing "Audit for liitteet exist before deletion"
      (t/is (not-empty (select-liitteet-audit energiatodistus-id-1)))
      (t/is (not-empty (select-liitteet-audit energiatodistus-id-2))))

    ;; Destroy et-1 liiteet
    (expire-energiatodistus! energiatodistus-id-1)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "The liitteet for energiatodistus-1 are deleted but exist for energiatodistus-2"
      (let [liitteet-1-in-db (select-liitteet energiatodistus-id-1)
            liitteet-2-in-db (select-liitteet energiatodistus-id-2)]

        (t/is (not-any? file-exists? file-liitteet-keys-1))
        (t/is (every? file-exists? file-liitteet-keys-2))

        (t/is (not-any? file-exists? link-liitteet-keys-1))
        (t/is (not-any? file-exists? link-liitteet-keys-2))

        (t/is (= 0 (count liitteet-1-in-db)))
        (t/is (= 4 (count liitteet-2-in-db)))))

    (t/testing "Audit for energiatodistus-1's liitteet do not exist after deletion"
      (t/is (empty? (select-liitteet-audit energiatodistus-id-1)))
      (t/is (not-empty (select-liitteet-audit energiatodistus-id-2))))))

(t/deftest destroy-viestiketju-test
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)

        select-vastaanottajat #(jdbc/query ts/*db* ["select * from vastaanottaja where viestiketju_id = ?" %])
        select-viestit #(jdbc/query ts/*db* ["select * from viesti where viestiketju_id = ?" %])
        select-viesti-readers #(jdbc/query ts/*db* ["select * from viesti v right outer join viesti_reader vr on v.id = vr.viesti_id where v.viestiketju_id = ?" %])
        select-viesti-liitteet #(jdbc/query ts/*db* ["select * from viesti_liite where viestiketju_id = ?" %])
        select-viestiketju #(jdbc/query ts/*db* ["select id from viestiketju where id = ?" %])


        select-viestiketju-audit #(jdbc/query ts/*db* ["select id from audit.viestiketju where id = ?" %])
        select-viesti-liite-audit #(jdbc/query ts/*db* ["select * from audit.viesti_liite where viestiketju_id = ?" %])

        energiatodistus-add (energiatodistus-test-data/generate-add 2018 true)
        [energiatodistus-id-1
         energiatodistus-id-2] (energiatodistus-test-data/insert! [energiatodistus-add energiatodistus-add] laatija-id)

        viestiketju-1-id (viesti-service/add-ketju! (ts/db-user paakayttaja-id)
                                                    (test-whoami/paakayttaja paakayttaja-id)
                                                    (viesti-test/complete-ketju-add
                                                      {:vastaanottajat        [laatija-id]
                                                       :vastaanottajaryhma-id nil
                                                       :energiatodistus-id    energiatodistus-id-1}))
        viestiketju-2-id (viesti-service/add-ketju! (ts/db-user paakayttaja-id)
                                                    (test-whoami/paakayttaja paakayttaja-id)
                                                    (viesti-test/complete-ketju-add
                                                      {:vastaanottajat        [laatija-id]
                                                       :vastaanottajaryhma-id nil
                                                       :energiatodistus-id    energiatodistus-id-2}))
        _ (liite-test-data/generate-and-insert-files-to-viestiketju! 2

                                                                     viestiketju-1-id)
        file-liitteet-1 (mapv :id (viesti-service/find-liitteet ts/*db*
                                                                (test-whoami/paakayttaja paakayttaja-id)
                                                                viestiketju-1-id))
        link-liitteet-1 (liite-test-data/generate-and-insert-links-to-viestiketju! 2 viestiketju-1-id)
        _ (liite-test-data/generate-and-insert-files-to-viestiketju! 2 viestiketju-2-id)
        file-liitteet-2 (mapv :id (viesti-service/find-liitteet ts/*db*
                                                                (test-whoami/paakayttaja paakayttaja-id)
                                                                viestiketju-2-id))
        link-liitteet-2 (liite-test-data/generate-and-insert-links-to-viestiketju! 2 viestiketju-2-id)
        file-liitteet-keys-1 (->> file-liitteet-1
                                  (map #(viesti-service/file-path viestiketju-1-id %)))
        link-liitteet-keys-1 (->> link-liitteet-1
                                  keys
                                  (map #(viesti-service/file-path viestiketju-1-id %)))
        file-liitteet-keys-2 (->> file-liitteet-2
                                  (map #(viesti-service/file-path viestiketju-2-id %)))
        link-liitteet-keys-2 (->> link-liitteet-2
                                  keys
                                  (map #(viesti-service/file-path viestiketju-2-id %)))]
    (t/testing "Viestiketjut exists before deletion"
      (t/is (not (empty? (select-viestiketju viestiketju-1-id))))
      (t/is (not (empty? (select-viestiketju viestiketju-2-id)))))
    (t/testing "The liitteet exist before deletion"
      (let [liitteet-1-in-db (select-viesti-liitteet viestiketju-1-id)
            liitteet-2-in-db (select-viesti-liitteet viestiketju-2-id)]
        (t/is (every? file-exists? file-liitteet-keys-1))
        (t/is (every? file-exists? file-liitteet-keys-2))

        (t/is (not-any? file-exists? link-liitteet-keys-1))
        (t/is (not-any? file-exists? link-liitteet-keys-2))

        (t/is (= 4 (count liitteet-1-in-db)))
        (t/is (= 4 (count liitteet-2-in-db)))))
    (t/testing "Vastaanottaja for both ketjus exists before deletion"
      (t/is (not (empty? (select-vastaanottajat viestiketju-1-id))))
      (t/is (not (empty? (select-vastaanottajat viestiketju-2-id)))))
    (t/testing "Viesti for both ketjus exists before deletion"
      (t/is (not (empty? (select-viestit viestiketju-1-id))))
      (t/is (not (empty? (select-viestit viestiketju-2-id)))))
    (t/testing "Viesti readers for both ketjus exists before deletion"
      (t/is (not (empty? (select-viesti-readers viestiketju-1-id))))
      (t/is (not (empty? (select-viesti-readers viestiketju-2-id)))))
    (t/testing "Viesti liite for both ketjus exists before deletion"
      (t/is (not (empty? (select-viesti-liitteet viestiketju-1-id))))
      (t/is (not (empty? (select-viesti-liitteet viestiketju-2-id)))))

    (t/testing "Viestiketju audit for both ketjus exists before deletion"
      (t/is (not (empty? (select-viestiketju-audit viestiketju-1-id))))
      (t/is (not (empty? (select-viestiketju-audit viestiketju-2-id)))))
    (t/testing "Viesti liite audit for both ketjus exists before deletion"
      (t/is (not (empty? (select-viesti-liite-audit viestiketju-1-id))))
      (t/is (not (empty? (select-viesti-liite-audit viestiketju-2-id)))))

    (expire-energiatodistus! energiatodistus-id-1)
    (service/destroy-expired-energiatodistukset! ts/*db* ts/*aws-s3-client* system-expiration-user)

    (t/testing "Only viestiketju 2 exists after deletion"
      (t/is (empty? (select-viestiketju viestiketju-1-id)))
      (t/is (not (empty? (select-viestiketju viestiketju-2-id)))))
    (t/testing "Liitteet is removed only from viestiketju-1 after deletion"
      (let [liitteet-1-in-db (select-viesti-liitteet viestiketju-1-id)
            liitteet-2-in-db (select-viesti-liitteet viestiketju-2-id)]
        (t/is (not-any? file-exists? file-liitteet-keys-1))
        (t/is (every? file-exists? file-liitteet-keys-2))

        (t/is (not-any? file-exists? link-liitteet-keys-1))
        (t/is (not-any? file-exists? link-liitteet-keys-2))

        (t/is (= 0 (count liitteet-1-in-db)))
        (t/is (= 4 (count liitteet-2-in-db)))))
    (t/testing "Vastaanottaja is removed only from viestiketju-1 after deletion"
      (t/is (empty? (select-vastaanottajat viestiketju-1-id)))
      (t/is (not (empty? (select-vastaanottajat viestiketju-2-id)))))
    (t/testing "Viesti is removed only for viestiketju-1-id afted deletion"
      (t/is (empty? (select-viestit viestiketju-1-id)))
      (t/is (not (empty? (select-viestit viestiketju-2-id)))))
    (t/testing "Viesti readers is removed only for viestiketju-1-id after deletion"
      (t/is (empty? (select-viesti-readers viestiketju-1-id)))
      (t/is (not (empty? (select-viesti-readers viestiketju-2-id)))))

    (t/testing "Viestiketju audit for viestiketju-1-id does not exist after deletion"
      (t/is (empty? (select-viestiketju-audit viestiketju-1-id)))
      (t/is (not (empty? (select-viestiketju-audit viestiketju-2-id)))))
    (t/testing "Viesti liite audit for viestiketju-1-id does not exist after deletion"
      (t/is (empty? (select-viesti-liite-audit viestiketju-1-id)))
      (t/is (not (empty? (select-viesti-liite-audit viestiketju-2-id)))))))
