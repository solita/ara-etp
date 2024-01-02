(ns solita.etp.service.liite-test
  (:require [clojure.test :as t]
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
