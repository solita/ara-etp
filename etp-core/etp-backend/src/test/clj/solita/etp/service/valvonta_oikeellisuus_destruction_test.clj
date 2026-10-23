(ns solita.etp.service.valvonta-oikeellisuus-destruction-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.service.valvonta-oikeellisuus :as valvonta-oikeellisuus-service]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.valvonta-oikeellisuus-destruction :as service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts]
            [solita.etp.whoami :as test-whoami]))

(t/use-fixtures :each ts/fixture)

(defn- publish-valvonta! [paakayttaja-id energiatodistus-id valvonta-id]
  (valvonta-oikeellisuus-service/publish-toimenpide!
    ts/*db*
    ts/*aws-s3-client*
    (test-whoami/paakayttaja paakayttaja-id)
    energiatodistus-id
    valvonta-id
    {:inhibit-email? true}))

(def system-vo-expiration-user {:id (kayttaja-service/system-kayttaja :expiration-vo-valvonnat) :rooli -1})
(def destruction-tag {:Key "ValvontaOikeellisuusDestruction" :Value "True"})

(def test-report {:type-id       7
                  :deadline-date nil
                  :template-id   1
                  :description   "Test"
                  :virheet       []
                  :severity-id   nil
                  :tiedoksi      [{:name "Testihenkilö" :email "test@example.com"}]})

(defn- add-child-data! [paakayttaja-id laatija-id energiatodistus-id valvonta-id]
  (valvonta-oikeellisuus-service/add-note! ts/*db* energiatodistus-id (str "Testimuistio for " energiatodistus-id))
  (valvonta-oikeellisuus-service/add-liitteet-from-files!
    ts/*db*
    ts/*aws-s3-client*
    (test-whoami/paakayttaja paakayttaja-id)
    energiatodistus-id
    valvonta-id
    [{:nimi        (str "Testitiedosto-" valvonta-id ".txt")
      :tempfile    (io/file "deps.edn")
      :contenttype "application/octet-stream"}])
  (valvonta-oikeellisuus-service/add-liite-from-link!
    ts/*db*
    (test-whoami/paakayttaja paakayttaja-id)
    energiatodistus-id
    valvonta-id
    {:nimi (str "Testiliite-" valvonta-id ".pdf") :url (str "https://example.com/testiliite-" valvonta-id ".pdf")})
  (viesti-service/add-ketju!
    ts/*db*
    (test-whoami/paakayttaja paakayttaja-id)
    {:vastaanottajat        [laatija-id]
     :vastaanottajaryhma-id nil
     :energiatodistus-id    energiatodistus-id
     :vo-toimenpide-id      valvonta-id
     :subject               (str "Testiviestiketju-" valvonta-id)
     :body                  (str "Testiviesti-" valvonta-id)}))

(defn test-data-set []
  (let [;; Add some relevant test data to the database
        paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        {virhetype-1-id :id} (valvonta-oikeellisuus-service/add-virhetype!
                             (ts/db-user paakayttaja-id)
                             {:label-fi       "Testivirhe"
                              :label-sv       "Testfel"
                              :ordinal        0
                              :valid          true
                              :description-fi "Testivirhe"
                              :description-sv "Testfel"})
        {virhetype-2-id :id} (valvonta-oikeellisuus-service/add-virhetype!
                               (ts/db-user paakayttaja-id)
                               {:label-fi       "Testivirhe2"
                                :label-sv       "Testfel2"
                                :ordinal        0
                                :valid          true
                                :description-fi "Testivirhe2"
                                :description-sv "Testfel2"})
        test-report (assoc test-report
                      :virheet [{:type-id virhetype-1-id
                                 :description "Testivirhe"}
                                {:type-id virhetype-2-id
                                 :description "Testivirhe2"}
                                ])
        laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistus-add (energiatodistus-test-data/generate-add 2026 true)
        energiatodistus-ids (energiatodistus-test-data/insert!
                              (vec (repeat 3 energiatodistus-add))
                              laatija-id)

        ;; Add toimenpide to energiatodistukset
        valvonta-ids (mapv (fn [energiatodistus-id]
                             (:id (valvonta-oikeellisuus-service/add-toimenpide!
                                    (ts/db-user paakayttaja-id)
                                    ts/*aws-s3-client*
                                    (test-whoami/paakayttaja paakayttaja-id)
                                    energiatodistus-id
                                    test-report)))
                           energiatodistus-ids)]
    (doseq [[energiatodistus-id valvonta-id]
            (map vector energiatodistus-ids valvonta-ids)]
      (publish-valvonta! paakayttaja-id energiatodistus-id valvonta-id))
    (doseq [[energiatodistus-id valvonta-id]
            (map vector energiatodistus-ids valvonta-ids)]
      (add-child-data! paakayttaja-id laatija-id energiatodistus-id valvonta-id))
    {:energiatodistus-ids energiatodistus-ids
     :valvonta-ids        valvonta-ids
     :paakayttaja-id      paakayttaja-id
     :laatija-id          laatija-id}))

(defn- make-valvonta-expired! [valvonta-id]
  (jdbc/execute!
    ts/*db*
    ["UPDATE vo_toimenpide
      SET publish_time = now() - interval '2 years' - interval '2 days'
      WHERE id = ?"
     valvonta-id]))

(defn- audit-table-exists? [table-name]
  (boolean
    (seq
      (jdbc/query
        ts/*db*
        ["SELECT 1
          FROM information_schema.tables
          WHERE table_schema = 'audit'
            AND table_name = ?"
         table-name]))))

(t/deftest find-expired-oikeellisuuden-valvonta-ids-test
  (let [{:keys [valvonta-ids]} (test-data-set)
        expired-valvonta-id (first valvonta-ids)]
    (make-valvonta-expired! expired-valvonta-id)
    (let [expired-valvonta-ids (service/find-expired-oikeellisuuden-valvonta-ids ts/*db*)]
      (t/is (= #{{:vo-toimenpide-id expired-valvonta-id}} (set expired-valvonta-ids))
            "find-expired-oikeellisuuden-valvonta-ids returns the correct expired valvonta ids"))))

(defn- find-linked-vo-virhe [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT toimenpide_id as \"vo-toimenpide-id\"
      FROM vo_virhe
      WHERE toimenpide_id = ?"
     vo-toimenpide-id]))

(defn- ->result-count [query-results]
  (reduce (fn [acc res] (+ acc (count res))) 0 query-results))

(defn tap [f x] (f x) x)

(t/deftest handle-vo-virhe-table!-test
  (let [{:keys [valvonta-ids]} (test-data-set)
        expired-valvonta-ids (take 2 valvonta-ids)
        non-expired-valvonta-id (nth valvonta-ids 2)]
    (run! make-valvonta-expired! expired-valvonta-ids)

    (t/testing "There are linked vo_virhe before running the destruction"
      (t/is (> (->> expired-valvonta-ids (mapv find-linked-vo-virhe) ->result-count) 0))
      (t/is (> (->> non-expired-valvonta-id (find-linked-vo-virhe) count) 0)))

    (service/anonymize-expired-vo-valvontas-and-destroy-linked-data ts/*db* ts/*aws-s3-client*)

    (t/testing "There are no linked vo_virhe after running the destruction in the expired valvontas"
      (t/is (= (->> expired-valvonta-ids (mapv find-linked-vo-virhe) ->result-count) 0)))
    (t/testing "There are linked vo_virhe in the valvonta that is not expired"
      (t/is (> (->> non-expired-valvonta-id (find-linked-vo-virhe) count) 0)))))

(defn- find-liite-audit [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT id as \"liite-id\"
      FROM liite
      WHERE vo_toimenpide_id = ?"
     vo-toimenpide-id]))

(t/deftest handle-liite-table!-test
  (let [{:keys [valvonta-ids]} (test-data-set)
        expired-valvonta-ids (take 2 valvonta-ids)
        non-expired-valvonta-ids [(nth valvonta-ids 2)]]
    (run! make-valvonta-expired! expired-valvonta-ids)
    (let [expired-linked-liitteet (->> expired-valvonta-ids
                                      (mapcat (partial service/find-linked-liite-ids ts/*db*)))
          non-expired-linked-liitteet (->> non-expired-valvonta-ids
                                           (mapcat (partial service/find-linked-liite-ids ts/*db*)))
          expired-audit (->> expired-valvonta-ids
                                      (mapcat find-liite-audit))
          non-expired-audit (->> non-expired-valvonta-ids
                             (mapcat find-liite-audit))]

      (t/testing "There are liitteet in database"
        (t/is (> (count expired-linked-liitteet) 0))
        (t/is (> (count expired-audit) 0))
        (t/is (> (count non-expired-linked-liitteet) 0))
        (t/is (> (count non-expired-audit) 0)))

      (t/testing "Expired liitteet are in object store before destruction"
        (doseq [liite-result expired-linked-liitteet]
          (when (= (:contenttype liite-result) "application/octet-stream")
            (t/is (true? (file-service/file-exists? ts/*aws-s3-client* (liite-service/file-key (:liite-id liite-result))))))))

      (t/testing "Non-expired liitteet are in object store before destruction"
        (doseq [liite-result non-expired-linked-liitteet]
          (when (= (:contenttype liite-result) "application/octet-stream")
            (t/is (true? (file-service/file-exists? ts/*aws-s3-client* (liite-service/file-key (:liite-id liite-result))))))))

      (service/anonymize-expired-vo-valvontas-and-destroy-linked-data ts/*db* ts/*aws-s3-client*)

      (t/testing "All the version of the expired files have the destruction tag"
        (let [
              all-the-deleted-keys (mapv #(liite-service/file-key (:liite-id %)) expired-linked-liitteet)
              key-and-version-ids (mapv
                                    (fn [file-key] {:file-key file-key
                                                    :version-ids (file-service/key->version-ids ts/*aws-s3-client* file-key)})
                                    all-the-deleted-keys)]
          (doseq [key-and-version-id key-and-version-ids]
            (println key-and-version-id)
            (doseq [version-id (:version-ids key-and-version-id)]
              (t/is
                (= destruction-tag
                   (file-service/get-file-tag ts/*aws-s3-client* (:file-key key-and-version-id) (:Key destruction-tag) version-id)))))))

      (t/testing "All the version of the non-expired files should not have the destruction tag"
        (let [all-the-deleted-keys (mapv #(liite-service/file-key (:liite-id %)) non-expired-linked-liitteet)
              key-and-version-ids (mapv
                                    (fn [file-key] {:file-key file-key
                                                    :version-ids (file-service/key->version-ids ts/*aws-s3-client* file-key)})
                                    all-the-deleted-keys)]
          (doseq [key-and-version-id key-and-version-ids]
            (println key-and-version-id)
            (doseq [version-id (:version-ids key-and-version-id)]
              (t/is
                (= nil
                   (file-service/get-file-tag ts/*aws-s3-client* (:file-key key-and-version-id) (:Key destruction-tag) version-id))))))))

    (let [expired-linked-liitteet (->> expired-valvonta-ids
                                      (mapcat (partial service/find-linked-liite-ids ts/*db*)))
          non-expired-linked-liitteet (->> non-expired-valvonta-ids
                                           (mapcat (partial service/find-linked-liite-ids ts/*db*)))
          expired-audit (->> expired-valvonta-ids
                             (mapcat find-liite-audit))
          non-expired-audit (->> non-expired-valvonta-ids
                                 (mapcat find-liite-audit))]

      (t/testing "Expired liitteet should not exist"
        (t/is (= (count expired-linked-liitteet) 0))
        (t/is (= (count expired-audit) 0))
        (t/is (> (count non-expired-linked-liitteet) 0))
        (t/is (> (count non-expired-audit) 0)))

      (t/testing "Expired liitteet do not exist in object store after destruction"
        (doseq [liite-result expired-linked-liitteet]
            (t/is (false? (file-service/file-exists? ts/*aws-s3-client* (liite-service/file-key (:liite-id liite-result)))))))

      (t/testing "Non-expired liitteet are still in object store after destruction"
        (doseq [liite-result non-expired-linked-liitteet]
          (when (= (:contenttype liite-result) "application/octet-stream")
            (t/is (true? (file-service/file-exists? ts/*aws-s3-client* (liite-service/file-key (:liite-id liite-result)))))))))))

(defn- find-linked-vo-tiedoksi [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT toimenpide_id as \"vo-toimenpide-id\"
      FROM vo_tiedoksi
      WHERE toimenpide_id = ?"
     vo-toimenpide-id]))

(t/deftest handle-vo-tiedoksi-table!-test
  (let [{:keys [valvonta-ids]} (test-data-set)
        expired-valvonta-ids (take 2 valvonta-ids)
        non-expired-valvonta-id (nth valvonta-ids 2)]
    (run! make-valvonta-expired! expired-valvonta-ids)

    (t/testing "There are linked vo_tiedoksi before running the destruction"
      (t/is (> (->> expired-valvonta-ids (mapv find-linked-vo-tiedoksi) ->result-count ) 0))
      (t/is (> (->> non-expired-valvonta-id (find-linked-vo-tiedoksi) count) 0)))

    (service/anonymize-expired-vo-valvontas-and-destroy-linked-data ts/*db* ts/*aws-s3-client*)

    (t/testing "There are no linked vo_tiedoksi after running the destruction in the expired valvontas"
      (t/is (= (->> expired-valvonta-ids (mapv find-linked-vo-tiedoksi ) ->result-count) 0)))
    (t/testing "There are linked vo_tiedoksi in the valvonta that is not expired"
      (t/is (> (->> non-expired-valvonta-id (find-linked-vo-tiedoksi) count) 0)))))

(defn- find-linked-vastaanottaja [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT vastaanottaja_id
      FROM vastaanottaja INNER JOIN viestiketju ON vastaanottaja.viestiketju_id = viestiketju.id
      WHERE viestiketju.vo_toimenpide_id = ?"
     vo-toimenpide-id]))

(t/deftest handle-vastaanottaja-table!-test
  (let [{:keys [valvonta-ids]} (test-data-set)
        expired-valvonta-ids (take 2 valvonta-ids)
        non-expired-valvonta-id (nth valvonta-ids 2)]
    (run! make-valvonta-expired! expired-valvonta-ids)

    (t/testing "There are linked vastaanottaja before running the destruction"
      (t/is (> (->> expired-valvonta-ids (mapv find-linked-vastaanottaja) ->result-count ) 0))
      (t/is (> (->> non-expired-valvonta-id (find-linked-vastaanottaja) count) 0)))

    (service/anonymize-expired-vo-valvontas-and-destroy-linked-data ts/*db* ts/*aws-s3-client*)

    (t/testing "There are no linked vastaanottaja after running the destruction in the expired valvontas"
      (t/is (= (->> expired-valvonta-ids (mapv find-linked-vastaanottaja ) ->result-count) 0)))
    (t/testing "There are linked vastaanottaja in the valvonta that is not expired"
      (t/is (> (->> non-expired-valvonta-id (find-linked-vastaanottaja) count) 0)))))

(defn- find-linked-viesti-reader [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT viesti_id, reader_id
      FROM viesti_reader INNER JOIN viesti ON viesti_reader.viesti_id = viesti.id
                         INNER JOIN viestiketju ON viesti.viestiketju_id = viestiketju.id
      WHERE viestiketju.vo_toimenpide_id = ?"
     vo-toimenpide-id]))

(defn- find-linked-viestiketju [vo-toimenpide-id]
  (jdbc/query
    ts/*db*
    ["SELECT id
      FROM viestiketju
      WHERE vo_toimenpide_id = ?"
     vo-toimenpide-id]))

(t/deftest handle-viesti-reader-table!-test
  (let [{:keys [valvonta-ids laatija-id]} (test-data-set)
        expired-valvonta-ids (take 2 valvonta-ids)
        non-expired-valvonta-id (nth valvonta-ids 2)]
    (run! make-valvonta-expired! expired-valvonta-ids)

    ;; Laatija reads all ketjus
    (run! (partial viesti-service/read-ketju! (ts/db-user laatija-id) (test-whoami/laatija laatija-id))
          (->> (cons non-expired-valvonta-id expired-valvonta-ids)
               (mapcat find-linked-viestiketju)
               (mapv :id)
               (tap println)))

    (t/testing "There are linked viesti-reader before running the destruction"
      (t/is (> (->> expired-valvonta-ids (mapv find-linked-viesti-reader) ->result-count ) 0))
      (t/is (> (->> non-expired-valvonta-id (find-linked-viesti-reader) count) 0)))

    (service/anonymize-expired-vo-valvontas-and-destroy-linked-data ts/*db* ts/*aws-s3-client*)

    (t/testing "There are no linked viesti-reader after running the destruction in the expired valvontas"
      (t/is (= (->> expired-valvonta-ids (mapv find-linked-viesti-reader ) ->result-count) 0)))
    (t/testing "There are linked viesti-reader in the valvonta that is not expired"
      (t/is (> (->> non-expired-valvonta-id (find-linked-viesti-reader) count) 0)))

    (t/testing "Audit table does not exist for viesti-reader"
      (t/is (not (audit-table-exists? "viesti_reader"))))))
