(ns solita.etp.api.palveluvayla-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [jsonista.core :as j]
            [solita.etp.schema.energiatodistus :as schema.energiatodistus]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-system :as ts]
            [ring.mock.request :as mock]
            [solita.etp.service.file :as file])
  (:import (org.apache.commons.io IOUtils)))

(t/use-fixtures :each ts/fixture)

(def palveluvayla-basepath "/palveluvayla/v1/energiatodistukset")

(t/deftest test-palveluvayla-api
  (let [; Add laatija
        laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))

        ; Generate two different rakennustunnus
        rakennustunnus-1 (generators/generate-rakennustunnus)
        rakennustunnus-2 (generators/generate-rakennustunnus)

        ; Create six energiatodistus. Eech with rakennustunnus 1, all language options (fi, sv, multilingual) in both versions (2013, 2018)
        todistus-2013-fi (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 0))
        todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 0))
        todistus-2013-sv (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 1))
        todistus-2018-sv (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 1))
        todistus-2013-multilingual (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 2))
        todistus-2018-multilingual (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-1) (assoc-in [:perustiedot :kieli] 2))

        ; Create two energiatodistus with rakennustunnus 2
        todistus-2013-rakennustunnus-2 (-> (test-data.energiatodistus/generate-add 2013 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-2))
        todistus-2018-rakennustunnus-2 (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :rakennustunnus] rakennustunnus-2))

        ; Insert all energiatodistus
        [todistus-2013-fi-id todistus-2018-fi-id todistus-2013-sv-id todistus-2018-sv-id todistus-2013-multilingual-id todistus-2018-multilingual-id todistus-2013-rakennustunnus-2-id todistus-2018-rakennustunnus-2-id] (test-data.energiatodistus/insert! [todistus-2013-fi todistus-2018-fi todistus-2013-sv todistus-2018-sv todistus-2013-multilingual todistus-2018-multilingual todistus-2013-rakennustunnus-2 todistus-2018-rakennustunnus-2] laatija-id)]

    ; Sign all energiatodistus
    ; LibreOffice is quite slow so do signing in parallel
    (doall (pmap #(test-data.energiatodistus/sign! % laatija-id false) [todistus-2013-fi-id todistus-2018-fi-id todistus-2013-sv-id todistus-2018-sv-id todistus-2013-multilingual-id todistus-2018-multilingual-id todistus-2013-rakennustunnus-2-id todistus-2018-rakennustunnus-2-id]))

    (t/testing "Fetching energiatodistus basic information by id returns it"
      (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/any/%s" todistus-2013-fi-id)))))
            body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= 200 (:status response)))
        (t/is (= todistus-2013-fi-id (:id body)))
        (schema-tools.coerce/coerce body schema.energiatodistus/EnergiatodistusForAnyLaatija schema-tools.coerce/json-coercion-matcher)))

    (t/testing "Fetching energiatodistus by id returns it when fetching correct version"
      (t/testing "when fetching 2013"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2013/%s" todistus-2013-fi-id)))))
              body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= 200 (:status response)))
          (t/is (= todistus-2013-fi-id (:id body)))
          (schema-tools.coerce/coerce body schema.energiatodistus/Energiatodistus2013 schema-tools.coerce/json-coercion-matcher)))

      (t/testing "when fetching 2018"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2018/%s" todistus-2018-fi-id)))))
              body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= 200 (:status response)))
          (t/is (= todistus-2018-fi-id (:id body)))
          (schema-tools.coerce/coerce body schema.energiatodistus/Energiatodistus2018 schema-tools.coerce/json-coercion-matcher))))

    (t/testing "Fetching wrong version by id returns 404"
      (t/testing "for 2013"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2013/%s" todistus-2018-fi-id)))))]
          (t/is (= 404 (:status response)))))

      (t/testing "for 2018"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2018/%s" todistus-2013-fi-id)))))]
          (t/is (= 404 (:status response))))))

    (t/testing "Fetching pdf"
      (t/testing "for version 2013"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2013-fi-id)))))
              body (-> response :body io/input-stream)
              correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2013-fi-id "fi"))]
          (t/is (= 200 (:status response)))
          (t/is (IOUtils/contentEquals body correct-pdf))))

      (t/testing "for version 2018"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-fi-id)))))
              body (-> response :body io/input-stream)
              correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-fi-id "fi"))]
          (t/is (= 200 (:status response)))
          (t/is (IOUtils/contentEquals body correct-pdf))))

      (t/testing "Accept-Language is honored"
        (t/testing "when asking for existing Finnish pdf"
          (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-multilingual-id)))
                                         (mock/header "Accept-Language" "fi")))
                body (-> response :body io/input-stream)
                correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-multilingual-id "fi"))]
            (t/is (= 200 (:status response)))
            (t/is (IOUtils/contentEquals body correct-pdf))))

        (t/testing "when asking for existing Swedish pdf"
          (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-multilingual-id)))
                                         (mock/header "Accept-Language" "sv")))
                body (-> response :body io/input-stream)
                correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-multilingual-id "sv"))]
            (t/is (= 200 (:status response)))
            (t/is (IOUtils/contentEquals body correct-pdf))))

        (t/testing "when prioritising Swedish pdf"
          (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-multilingual-id)))
                                         (mock/header "Accept-Language" "fi;q=0.5, sv")))
                body (-> response :body io/input-stream)
                correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-multilingual-id "sv"))]
            (t/is (= 200 (:status response)))
            (t/is (IOUtils/contentEquals body correct-pdf))))

        (t/testing "when prioritising Finnish pdf"
          (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-multilingual-id)))
                                         (mock/header "Accept-Language" "fi, sv;q=0.5")))
                body (-> response :body io/input-stream)
                correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-multilingual-id "fi"))]
            (t/is (= 200 (:status response)))
            (t/is (IOUtils/contentEquals body correct-pdf)))))

      (t/testing "Asking for a multilingual pdf without Accept-Language returns Finnish pdf"
        (let [response (ts/handler (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-multilingual-id))))
              body (-> response :body io/input-stream)
              correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-multilingual-id "fi"))]
          (t/is (= 200 (:status response)))
          (t/is (IOUtils/contentEquals body correct-pdf))))

      (t/testing "Asking for a Swedish pdf without Accept-Language returns it"
        (let [response (ts/handler (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-sv-id))))
              body (-> response :body io/input-stream)
              correct-pdf (file/find-file ts/*aws-s3-client* (format "energiatodistukset/energiatodistus-%s-%s" todistus-2018-sv-id "sv"))]
          (t/is (= 200 (:status response)))
          (t/is (IOUtils/contentEquals body correct-pdf))))

      (t/testing "Asking for pdf in non-existing language returns 404"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/pdf/%s" todistus-2018-fi-id)))
                                       (mock/header "Accept-Language" "sv")))]
          (t/is (= 404 (:status response))))))

    (t/testing "Searching for basic information by rakennustunnus returns all versions"
      (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/any?rakennustunnus=%s" rakennustunnus-1)))))
            body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= 200 (:status response)))
        (t/is (= 6 (count body)))
        (t/is (= #{todistus-2013-fi-id
                   todistus-2018-fi-id
                   todistus-2013-sv-id
                   todistus-2018-sv-id
                   todistus-2013-multilingual-id
                   todistus-2018-multilingual-id}
                 (set (map :id body))))
        (schema-tools.coerce/coerce body [schema.energiatodistus/EnergiatodistusForAnyLaatija] schema-tools.coerce/json-coercion-matcher)))

    (t/testing "Searching for specific version by rakennustunnus returns only that version"
      (t/testing "when searching for 2013"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2013?rakennustunnus=%s" rakennustunnus-1)))))
              body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= 200 (:status response)))
          (t/is (= 3 (count body)))
          (t/is (= #{todistus-2013-fi-id
                     todistus-2013-sv-id
                     todistus-2013-multilingual-id} (set (map :id body))))
          (schema-tools.coerce/coerce body [schema.energiatodistus/Energiatodistus2013] schema-tools.coerce/json-coercion-matcher)))

      (t/testing "when searching for 2018"
        (let [response (ts/handler (-> (mock/request :get (str palveluvayla-basepath (format "/json/2018?rakennustunnus=%s" rakennustunnus-1)))))
              body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= 200 (:status response)))
          (t/is (= 3 (count body)))
          (t/is (= #{todistus-2018-fi-id
                     todistus-2018-sv-id
                     todistus-2018-multilingual-id}
                   (set (map :id body))))
          (schema-tools.coerce/coerce body [schema.energiatodistus/Energiatodistus2018] schema-tools.coerce/json-coercion-matcher))))))
