(ns solita.etp.api.perusparannuspassi-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.energiatodistus :as et-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(t/deftest ppp-post-test
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (laatija-test-data/insert-suomifi-laatija2!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (t/testing "Create energiatodistus to test perusparannuspassi"
    (let [et (-> (et-test-data/generate-add 2026 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)]
        (t/is ( = 1 et-id))
        (t/is (= 201 (:status post-res)))[])))

  (t/testing "create perusparannuspassi to energiatodistus with ppp-pätevyys and same laatija-id"
    (let [ppp (-> (ppp-test-data/generate-add 1))
          ppp-body (-> ppp j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body ppp-body)
                                   (laatija-test-data/with-suomifi-laatija)))
          ppp-id (-> post-res :body (j/read-value object-mapper) :id)]
      (t/is (= 201 (:status post-res)))

    (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                  (mock/header "Accept" "application/json")
                                  (laatija-test-data/with-suomifi-laatija)))
          body-str (slurp (clojure.java.io/reader (:body get-res)))
          full-ppp (j/read-value body-str object-mapper)
          created-ppp (dissoc full-ppp :id :tila-id :laatija-id)]
      (t/is (= 200 (:status get-res)))

      (t/testing "create perusparannuspassi to energiatodistus with different laatija-id"
        (let [ppp (-> (ppp-test-data/generate-add 1))
              ppp-body (-> ppp j/write-value-as-string)
              post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (laatija-test-data/with-suomifi-laatija2)
                                       (mock/body ppp-body)))]
          (t/is (= 403 (:status post-res)))))

      (t/testing "Modify ppp with the same laatija-id"
      (let [modified-ppp (assoc-in created-ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
            new-ppp-body (j/write-value-as-string modified-ppp)
            put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (mock/body new-ppp-body)
                                    (laatija-test-data/with-suomifi-laatija)))]
        (t/is (= 200 (:status put-res)))))

      (t/testing "Can't modify ppp with the different laatija-id"
        (let [modified-ppp (assoc-in full-ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
              new-ppp-body (j/write-value-as-string modified-ppp)
              put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (laatija-test-data/with-suomifi-laatija2)
                                      (mock/body new-ppp-body)))]
          (t/is (= 400 (:status put-res)))))

      (t/testing "owner can delete own perusparannuspassi"
        (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                         (mock/header "Accept" "application/json")
                                         (laatija-test-data/with-suomifi-laatija)))]
          (t/is (= 200 (:status delete-res)))))

      (t/testing "can't create perusparannuspassi to energiatodistus without ppp-pätevyys"
        (laatija-test-data/insert-suomifi-laatija!
          (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 2})))
        (let [ppp (-> (ppp-test-data/generate-add 1))
              ppp-body (-> ppp j/write-value-as-string)
              post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))]
          (t/is (= 403 (:status post-res)))))

      (t/testing "can't delete other laatija's perusparannuspassi"
        (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                         (mock/header "Accept" "application/json")
                                         (laatija-test-data/with-suomifi-laatija2)))]
          (t/is (= 500 (:status delete-res)))))

      (t/testing "Get requires authentication"
        (let [unauth-get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                   (mock/header "Accept" "application/json")))]
          (t/is (= 403 (:status unauth-get-res)))))))))

(t/deftest ppp-numeric-validation-test
  (t/testing "PPP numeric validation for kaukolampo-hinta"
    (laatija-test-data/insert-suomifi-laatija!
      (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

    ;; Create energiatodistus first
    (let [et (-> (et-test-data/generate-add 2026 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)]

        (t/testing "PPP with kaukolampo-hinta at 3 cents should succeed with warnings (outside warning range 5-20)"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:tulokset :kaukolampo-hinta] 3))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (mock/body ppp-body)
                                         (laatija-test-data/with-suomifi-laatija)))
                response-body (-> post-res :body (j/read-value object-mapper))]
            (when-not (= 201 (:status post-res))
              (println "Response body:" response-body))
            (t/is (= 201 (:status post-res)) "Expected status 201 - value is outside warning range but inside error range")
            (t/is (some? (:warnings response-body)) "Expected warnings to be present")
            (t/is (seq (:warnings response-body)) "Expected warnings list to be non-empty")
            (let [warnings (:warnings response-body)]
              (t/is (some #(= "tulokset.kaukolampo-hinta" (:property %)) warnings)
                    "Expected warning for tulokset.kaukolampo-hinta"))))

        (t/testing "PPP with kaukolampo-hinta at 1 cent should fail with 400 (outside error range 1-100)"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:tulokset :kaukolampo-hinta] 1))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (mock/body ppp-body)
                                         (laatija-test-data/with-suomifi-laatija)))]
            (t/is (= 400 (:status post-res)) "Expected status 400 - value is outside error range")))))))
