(ns solita.etp.service.liite-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.liite :as liite-test-data]
            [solita.etp.service.liite :as service]
            [solita.etp.service.file :as file-service]
            [solita.etp.whoami :as test-whoami]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            1
                            2013
                            true
                            laatija-id)
        energiatodistus-id (-> energiatodistukset keys sort first)
        file-liitteet (liite-test-data/generate-and-insert-files! 2
                                                                  laatija-id
                                                                  energiatodistus-id)
        link-liitteet (liite-test-data/generate-and-insert-links! 2
                                                                  laatija-id
                                                                  energiatodistus-id)]
    {:laatijat laatijat
     :energiatodistukset energiatodistukset
     :file-liitteet file-liitteet
     :link-liitteet link-liitteet}))

(t/deftest add-liitteet-and-find-test
  (let [{:keys [laatijat energiatodistukset
                file-liitteet link-liitteet]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        energiatodistus-id (-> energiatodistukset keys sort first)
        liitteet (merge file-liitteet link-liitteet)
        found (service/find-energiatodistus-liitteet
               ts/*db*
               {:id laatija-id :rooli 0}
               energiatodistus-id)]
    (t/is (= (count liitteet) (count found)))
    (doseq [{:keys [id url nimi]} found
            :let [add (get liitteet id)]]
      (t/is (= (:nimi add) nimi))
      (t/is (= (:url add) url)))))

(t/deftest find-liite-content-test
  (let [{:keys [laatijat file-liitteet]} (test-data-set)
        laatija-id (-> laatijat keys sort first)]
    (doseq [id (keys file-liitteet)
            :let [add (get file-liitteet id)
                  {:keys [nimi content]} (service/find-energiatodistus-liite-content
                                           ts/*db*
                                           {:id laatija-id :rooli 0}
                                           ts/*aws-s3-client*
                                           id)]]
      (t/is (= nimi (:nimi add)))
      (t/is (= (-> content .readAllBytes vec)
               (-> add :tempfile file-service/file->byte-array vec))))))

(t/deftest delete-liite!-test
  (t/testing "Laatija can delete their liite"
    (let [{:keys [laatijat energiatodistukset
                  file-liitteet link-liitteet]} (test-data-set)
          laatija-id (-> laatijat keys sort first)
          whoami {:id laatija-id :rooli 0}
          energiatodistus-id (-> energiatodistukset keys sort first)
          liitteet (merge file-liitteet link-liitteet)]
      (doseq [id (keys liitteet)
              :let [found-before (service/find-energiatodistus-liitteet
                                   ts/*db*
                                   whoami
                                   energiatodistus-id)
                    _ (service/delete-liite! (ts/db-user laatija-id)
                                             whoami
                                             id)
                    found-after (service/find-energiatodistus-liitteet
                                  ts/*db*
                                  whoami
                                  energiatodistus-id)]]
        (t/is (= (-> found-before count dec) (count found-after)))
        (t/is (-> (map :id found-after) set (contains? id) not))
        (t/is (nil? (-> (service/find-energiatodistus-liite-content
                          ts/*db*
                          {:id laatija-id :rooli 0}
                          ts/*aws-s3-client*
                          id)))))))

  (t/testing "Laatija can't delete other laatija's liitteet"
    (let [{:keys [laatijat energiatodistukset
                  file-liitteet link-liitteet]} (test-data-set)
          another-laatija (laatija-test-data/generate-and-insert! 1)
          another-laatija-id (-> another-laatija keys sort first)
          another-laatija-whoami {:id another-laatija-id :rooli 0}
          original-laatija-id  (-> laatijat keys sort first)
          original-laatija-whoami {:id original-laatija-id :rooli 0}
          energiatodistus-id (-> energiatodistukset keys sort first)
          liitteet (merge file-liitteet link-liitteet)]
      (doseq [id (keys liitteet)
              :let [found-before (service/find-energiatodistus-liitteet
                                   ts/*db*
                                   original-laatija-whoami
                                   energiatodistus-id)]]

        ;; Trying to delete liite results in exception
        (t/is (thrown? Exception
                       (service/delete-liite! (ts/db-user another-laatija-id)
                                              another-laatija-whoami
                                              id)))

        ;; Liite is still there after failed deletion attempt
        (t/is (= found-before
                 (service/find-energiatodistus-liitteet
                   ts/*db*
                   original-laatija-whoami
                   energiatodistus-id)))))))

(t/deftest add-liitteet-with-unusual-filenames-test
  (t/testing "Attachments with an empty filename or a filename containing a quote are accepted as-is"
    (let [laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          energiatodistukset (energiatodistus-test-data/generate-and-insert!
                              1 2013 true laatija-id)
          energiatodistus-id (-> energiatodistukset keys sort first)
          whoami {:id laatija-id :rooli 0}
          file-adds [{:size        100
                      :tempfile    (io/file "deps.edn")
                      :contenttype "application/octet-stream"
                      :nimi        ""}
                     {:size        100
                      :tempfile    (io/file "Dockerfile")
                      :contenttype "application/octet-stream"
                      :nimi        "quote\"file.txt"}]
          ids (service/add-liitteet-from-files! (ts/db-user laatija-id)
                                                ts/*aws-s3-client*
                                                whoami
                                                energiatodistus-id
                                                file-adds)
          found (service/find-energiatodistus-liitteet
                 ts/*db* whoami energiatodistus-id)
          nimi-by-id (into {} (map (juxt :id :nimi) found))
          [empty-nimi-id quote-nimi-id] ids]
      (t/is (= "" (get nimi-by-id empty-nimi-id)))
      (t/is (= "quote\"file.txt" (get nimi-by-id quote-nimi-id))))))

(t/deftest add-liite-rejects-executable-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            1 2013 true laatija-id)
        energiatodistus-id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id :rooli 0}
        executable-file (doto (java.io.File/createTempFile "liite-test" ".exe")
                          .deleteOnExit)
        _ (io/copy (byte-array (map unchecked-byte [0x4D 0x5A 0x00 0x00]))
                    executable-file)]
    (t/is (= :liite-executable
             (:type
              (etp-test/catch-ex-data
               #(service/add-liitteet-from-files!
                 (ts/db-user laatija-id)
                 ts/*aws-s3-client*
                 whoami
                 energiatodistus-id
                 [{:size        4
                   :tempfile    executable-file
                   :contenttype "application/octet-stream"
                   :nimi        "virus.exe"}]))))
             "Executable attachment is rejected")
    (t/is (empty? (service/find-energiatodistus-liitteet
                    ts/*db* whoami energiatodistus-id))
          "No liite was persisted for the rejected executable")))

(t/deftest add-liite-rejects-content-type-mismatch-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            1 2013 true laatija-id)
        energiatodistus-id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id :rooli 0}
        pdf-file (doto (java.io.File/createTempFile "liite-test" ".pdf")
                   .deleteOnExit)
        _ (io/copy (byte-array (map unchecked-byte
                                     (concat [0x25 0x50 0x44 0x46] (repeat 20 0))))
                    pdf-file)]
    (t/is (= :liite-content-type-mismatch
             (:type
              (etp-test/catch-ex-data
               #(service/add-liitteet-from-files!
                 (ts/db-user laatija-id)
                 ts/*aws-s3-client*
                 whoami
                 energiatodistus-id
                 [{:size        24
                   :tempfile    pdf-file
                   :contenttype "image/png"
                   :nimi        "not-really-a-png.png"}]))))
             "Attachment whose declared content-type doesn't match its actual content is rejected")
    (t/is (empty? (service/find-energiatodistus-liitteet
                    ts/*db* whoami energiatodistus-id))
          "No liite was persisted for the rejected attachment")))

(t/deftest add-liite-stores-detected-content-type-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            1 2013 true laatija-id)
        energiatodistus-id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id :rooli 0}
        pdf-file (doto (java.io.File/createTempFile "liite-test" ".pdf")
                   .deleteOnExit)
        _ (io/copy (byte-array (map unchecked-byte
                                     (concat [0x25 0x50 0x44 0x46] (repeat 20 0))))
                    pdf-file)
        [id] (service/add-liitteet-from-files!
              (ts/db-user laatija-id)
              ts/*aws-s3-client*
              whoami
              energiatodistus-id
              [{:size        24
                :tempfile    pdf-file
                :contenttype "application/pdf"
                :nimi        "document.pdf"}])
        found (service/find-energiatodistus-liitteet ts/*db* whoami energiatodistus-id)]
    (t/is (= "application/pdf" (:contenttype (first (filter #(= id (:id %)) found))))
          "Recognized attachment is stored with its detected content-type")))

(t/deftest add-liite-forces-octet-stream-for-unrecognized-content-test
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (energiatodistus-test-data/generate-and-insert!
                            1 2013 true laatija-id)
        energiatodistus-id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id :rooli 0}
        [id] (service/add-liitteet-from-files!
              (ts/db-user laatija-id)
              ts/*aws-s3-client*
              whoami
              energiatodistus-id
              [{:size        100
                :tempfile    (io/file "deps.edn")
                :contenttype "image/png"
                :nimi        "not-actually-a-png.png"}])
        found (service/find-energiatodistus-liitteet ts/*db* whoami energiatodistus-id)]
    (t/is (= "application/octet-stream"
             (:contenttype (first (filter #(= id (:id %)) found))))
          "Unrecognized content is always accepted and stored as application/octet-stream")))

(t/deftest find-liite-other-user
  (let [{:keys [laatijat energiatodistukset
                file-liitteet link-liitteet]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        [energiatodistus-id _] (-> energiatodistukset first)
        liitteet (merge file-liitteet link-liitteet)
        [other-laatija-id _] (laatija-test-data/generate-and-insert!)]

    (t/is (= (etp-test/catch-ex-data
               #(service/find-energiatodistus-liitteet
                  (ts/db-user other-laatija-id) (test-whoami/laatija other-laatija-id)
                  energiatodistus-id))
             {:type :forbidden}))

    (doseq [id (keys liitteet)]
      (t/is (= (etp-test/catch-ex-data
                 #(service/find-energiatodistus-liite-content
                    (ts/db-user other-laatija-id) (test-whoami/laatija other-laatija-id)
                    ts/*aws-s3-client* id))
               {:type :forbidden})))))
