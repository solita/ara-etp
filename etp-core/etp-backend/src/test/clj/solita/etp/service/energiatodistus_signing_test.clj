(ns solita.etp.service.energiatodistus-signing-test
  (:require
    [solita.etp.service.energiatodistus-signing :as service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign-service]
    [clojure.test :as t]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (merge (energiatodistus-test-data/generate-and-insert!
                                    1
                                    2013
                                    true
                                    laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                    1
                                    2018
                                    true
                                    laatija-id))]
    {:laatijat           laatijat
     :energiatodistukset energiatodistukset}))

(t/deftest do-when-signing-test
  (let [f (constantly true)]
    (t/is (= (service/do-when-signing {:tila-id 0} f)
             :not-in-signing))
    (t/is (true? (service/do-when-signing {:tila-id 1} f)))
    (t/is (= (service/do-when-signing {:tila-id 2} f)
             :already-signed))))

(t/deftest create-stateful-signature-parameters-test
  (let [energiatodistus-id "123"
        language "fi"
        ;; A String is used as the object in this test.
        some-data "I am data"
        some-data-is (pdf-sign-service/object->input-stream some-data)
        params-key (service/stateful-signature-parameters-file-key energiatodistus-id language)
        wanted-tag {:Key "TemporarySignatureProcessFile" :Value "True"}]
    (t/testing "The key is in a place that is safe to allow for deletion via a lifecycle rule."
      (t/is (= "energiatodistus-signing/stateful-signature-parameters-123-fi" params-key)))
    (service/save-stateful-signature-parameters ts/*aws-s3-client* energiatodistus-id language some-data-is)
    (t/testing "The created file has a the correct tag."
      (t/is (= wanted-tag (file-service/get-file-tag ts/*aws-s3-client*
                                                     params-key
                                                     (:Key wanted-tag)))))
    (t/testing "Deserialization of the stored object works."
      (t/is (= some-data (-> (service/load-stateful-signature-parameters ts/*aws-s3-client* energiatodistus-id language)
                             (pdf-sign-service/input-stream->object)))))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} find-energiatodistus-digest-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id}]
    (t/is (= (service/find-energiatodistus-digest db ts/*aws-s3-client* id "fi" "allekirjoitus-id")
             :not-in-signing))
    (energiatodistus-service/start-energiatodistus-signing! db whoami id)
    (t/is (contains? (service/find-energiatodistus-digest db
                                                                  ts/*aws-s3-client*
                                                                  id
                                                                  "fi"
                                                                  "allekirjoitus-id")
                     :digest))
    (energiatodistus-service/end-energiatodistus-signing!
      db
      ts/*aws-s3-client*
      whoami
      id
      {:skip-pdf-signed-assert? true})
    (t/is (= (service/find-energiatodistus-digest db
                                                          ts/*aws-s3-client*
                                                          id
                                                          "fi"
                                                          "allekirjoitus-id")
             :already-signed))))

