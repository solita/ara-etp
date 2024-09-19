(ns solita.etp.service.energiatodistus-destruction-test
  (:require [clojure.test :as t]
            [solita.common.time :as time]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.valvonta-oikeellisuus :as valvonta-oikeellisuus-service]
            [solita.etp.service.energiatodistus-destruction :as service]
            [solita.etp.service.file :as file-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.whoami :as test-whoami]
            [solita.etp.test-system :as ts])
  (:import (java.time Instant LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(defn update-energiatodistus! [energiatodistus-id energiatodistus laatija-id]
  (energiatodistus-service/update-energiatodistus! (ts/db-user laatija-id)
                                                   {:id laatija-id :rooli 0}
                                                   energiatodistus-id
                                                   energiatodistus))

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 3)
        laatija-ids (-> laatijat keys sort)
        [laatija-id-1 laatija-id-2 laatija-id-3] laatija-ids
        energiatodistus-adds (energiatodistus-test-data/generate-adds 3
                                                                      2018
                                                                      true)
        energiatodistus-ids (mapcat #(energiatodistus-test-data/insert!
                                       [%1]
                                       %2)
                                    energiatodistus-adds
                                    laatija-ids)
        [energiatodistus-id-1 energiatodistus-id-2 energiatodistus-id-3] energiatodistus-ids
        [energiatodistus-add-1 energiatodistus-add-2 energiatodistus-add-3] energiatodistus-adds]

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

    {:laatijat           laatijat
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest get-currently-expired-todistus-ids-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        ids (-> energiatodistukset keys sort)
        [id-1 id-2 id-3] ids
        expired-ids (#'service/get-currently-expired-todistus-ids ts/*db*)]
    (t/testing "Todistus with expiration time at year 1970 should be expired."
      (t/is (some #{id-2} expired-ids)))
    (t/testing "Todistus with expiration date set by signing it today should not be expired."
      (t/is (nil? (some #{id-1} expired-ids))))
    (t/testing "Todistus whose expiration is today should not be expired yet."
      (t/is (nil? (some #{id-3} expired-ids))))))

(t/deftest destroy-energiatodistus-pdf-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-ids (-> laatijat keys sort)
        [laatija-id] laatija-ids
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

    (#'service/delete-energiatodistus-pdfs! ts/*db* ts/*aws-s3-client* energiatodistus-id-fi)

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

    (#'service/delete-energiatodistus-pdfs! ts/*db* ts/*aws-s3-client* energiatodistus-id-sv)

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

    (#'service/delete-energiatodistus-pdfs! ts/*db* ts/*aws-s3-client* energiatodistus-id-mu)

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
                                         :versio (= 2013 value)
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
    (#'service/anonymize-energiatodistus! ts/*db* id-1)
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
    (#'service/destroy-energiatodistus-audit-data! ts/*db* id-1)
    (t/testing "The audit data is destroyed."
      (t/is (empty? (get-et-1-audit-information))))
    (t/testing "The audit data for et-2 still exists."
      (t/is (not (empty? (get-et-2-audit-information)))))))

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
        rfi-reply {:type-id       4
                   :deadline-date nil
                   :template-id   nil
                   :description   "Test"
                   :virheet       [vo_virhe]
                   :severity-id   nil
                   :tiedoksi      [vo_tiedoksi vo_tiedoksi]}
        _ (valvonta-oikeellisuus-service/add-toimenpide! (ts/db-user laatija-id)
                                                         ts/*aws-s3-client*
                                                         (test-whoami/laatija laatija-id)
                                                         energiatodistus-id-1 rfi-reply)
        _ (valvonta-oikeellisuus-service/add-toimenpide! (ts/db-user laatija-id)
                                                         ts/*aws-s3-client*
                                                         (test-whoami/laatija laatija-id)
                                                         energiatodistus-id-2 rfi-reply)
        _ (valvonta-oikeellisuus-service/add-note! ts/*db* energiatodistus-id-1 (:description vo_note))
        _ (valvonta-oikeellisuus-service/add-note! ts/*db* energiatodistus-id-2 (:description vo_note))]
    (t/testing "There is some toimenpide before deletion."
      (t/is (not (empty? (get-vo-toimenpiteet energiatodistus-id-1)))))
    (t/testing "There is some note before deletion."
      (t/is (not (empty? (get-vo-notes energiatodistus-id-1)))))
    (t/testing "There is some tiedoksi before deletion."
      (t/is (not (empty? (get-vo-tiedoksi energiatodistus-id-1)))))
    (t/testing "There is some virheet before deletion."
      (t/is (not (empty? (get-vo-virheet energiatodistus-id-1)))))
    (#'service/destroy-energiatodistus-oikeellisuuden-valvonta! ts/*db* energiatodistus-id-1)
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

    ;; Audit data destruction
    (t/testing "There was some audit data on toimenpiteet before deletion."
      (t/is (not (empty? (select-toimenpiteet-audit energiatodistus-id-1)))))
    (#'service/destroy-energiatodistus-oikeellisuuden-valvonta-toimenpide-audit! ts/*db* energiatodistus-id-1)
    (t/testing "The audit data on toimenpiteet is destroyed."
      (t/is (empty? (select-toimenpiteet-audit energiatodistus-id-1))))
    (t/testing "The audit data for et-2 toimenpiteet still exists."
      (t/is (not (empty? (select-toimenpiteet-audit energiatodistus-id-2)))))

    (t/testing "There was some audit data on notes before deletion."
      (t/is (not (empty? (select-notes-audit energiatodistus-id-1)))))
    (#'service/destroy-energiatodistus-oikeellisuuden-valvonta-note-audit! ts/*db* energiatodistus-id-1)
    (t/testing "The audit data on notes is destroyed."
      (t/is (empty? (select-notes-audit energiatodistus-id-1))))
    (t/testing "The audit data for et-2 notes still exists."
      (t/is (not (empty? (select-notes-audit energiatodistus-id-2)))))))


