(ns solita.etp.service.energiatodistus-signing-test
  (:require
    [solita.etp.service.energiatodistus-signing :as service]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign-service]
    [clojure.test :as t]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

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
    (service/create-stateful-signature-parameters ts/*aws-s3-client* energiatodistus-id language some-data-is)
    (t/testing "The created file has a the correct tag."
      (t/is (= wanted-tag (file-service/get-file-tag ts/*aws-s3-client*
                                                     params-key
                                                     (:Key wanted-tag)))))
    (t/testing "Deserialization of the stored object works."
      (t/is (= some-data (-> (file-service/find-file ts/*aws-s3-client* params-key)
                             (pdf-sign-service/input-stream->object)))))))
