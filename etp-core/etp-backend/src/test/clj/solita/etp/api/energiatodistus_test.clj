(ns solita.etp.api.energiatodistus-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [schema.utils :as s-utils]
    [schema.coerce :as sc]
    [schema-tools.coerce :as stc]
    [ring.mock.request :as mock]
    [solita.etp.schema.energiatodistus :as et-schema]
    [solita.etp.test-data.energiatodistus :as et-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(t/deftest energiatodistukset-empty
  (laatija-test-data/insert-suomifi-laatija!)
  (t/testing "Energiatodistukset endpoint returns 200"
    (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset")
                                   (mock/header "Accept" "application/json")
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 200 (:status response)))
      (t/is (= [] (-> response :body j/read-value))))))

(t/deftest energiatodistukset-some
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (t/testing "Preparation"
    (let [et (-> (et-test-data/generate-add 2013 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2013")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation"))
    (let [et (-> (et-test-data/generate-add 2018 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2018")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation"))
    (let [et (-> (et-test-data/generate-add 2026 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation")))

  (t/testing "Energiatodistukset endpoint returns 200"
    (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset")
                                   (mock/header "Accept" "application/json")
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 200 (:status response)))
      (t/is (= 3 (-> response :body j/read-value count)))))

  (t/testing "Fetching of ET 2013 produces an ET 2013"
    (let [list-response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.versio%22%2C2013%5D%5D%5D")
                                        (mock/header "Accept" "application/json")
                                        (laatija-test-data/with-suomifi-laatija)))
          et2013-id (-> list-response :body (j/read-value object-mapper) first :id)
          response (-> (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/2013/" et2013-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija)))
                       :body
                       (j/read-value object-mapper))
          coerced-response (let [coercer (sc/coercer et-schema/Energiatodistus2013 stc/json-coercion-matcher)]
                             (coercer response))]
      (t/is (not (s-utils/error? coerced-response)))))

  (t/testing "Fetching of ET 2018 produces an ET 2018"
    (let [list-response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.versio%22%2C2018%5D%5D%5D")
                                        (mock/header "Accept" "application/json")
                                        (laatija-test-data/with-suomifi-laatija)))
          et2018-id (-> list-response :body (j/read-value object-mapper) first :id)
          response (-> (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/2018/" et2018-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija)))
                       :body
                       (j/read-value object-mapper))
          coerced-response (let [coercer (sc/coercer et-schema/Energiatodistus2018 stc/json-coercion-matcher)]
                             (coercer response))]
      (t/is (not (s-utils/error? coerced-response)))))

  (t/testing "Fetching of ET 2026 produces an ET 2026"
    (let [list-response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.versio%22%2C2026%5D%5D%5D")
                                        (mock/header "Accept" "application/json")
                                        (laatija-test-data/with-suomifi-laatija)))
          et2013-id (-> list-response :body (j/read-value object-mapper) first :id)
          response (-> (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/2026/" et2013-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija)))
                       :body
                       (j/read-value object-mapper))
          coerced-response (let [coercer (sc/coercer et-schema/Energiatodistus2026 stc/json-coercion-matcher)]
                             (coercer response))]
      (t/is (not (s-utils/error? coerced-response))))))
