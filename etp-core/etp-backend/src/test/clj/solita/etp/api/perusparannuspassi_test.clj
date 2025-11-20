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

(defn assert-status
  "Assert that response has expected status. Print response body if status doesn't match."
  ([response expected-status]
   (assert-status response expected-status nil))
  ([response expected-status message]
   (when-not (= expected-status (:status response))
     (println "Expected status:" expected-status)
     (println "Actual status:" (:status response))
     (println "Response body:" (try
                                  (-> response :body (j/read-value object-mapper))
                                  (catch Exception _
                                    (str (:body response))))))
   (if message
     (t/is (= expected-status (:status response)) message)
     (t/is (= expected-status (:status response))))))

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
      (assert-status post-res 201 "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)]
        (t/is (= 1 et-id))
        (assert-status post-res 201))))

  (t/testing "create perusparannuspassi to energiatodistus with ppp-pätevyys and same laatija-id"
    (let [ppp (-> (ppp-test-data/generate-add 1))
          ppp-body (-> ppp j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body ppp-body)
                                   (laatija-test-data/with-suomifi-laatija)))
          ppp-id (-> post-res :body (j/read-value object-mapper) :id)]
      (assert-status post-res 201)

      (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            body-str (slurp (clojure.java.io/reader (:body get-res)))
            full-ppp (j/read-value body-str object-mapper)
            created-ppp (dissoc full-ppp :id :tila-id :laatija-id)]
        (assert-status get-res 200)

        (t/testing "create perusparannuspassi to energiatodistus with different laatija-id"
          (let [ppp (-> (ppp-test-data/generate-add 1))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (laatija-test-data/with-suomifi-laatija2)
                                         (mock/body ppp-body)))]
            (assert-status post-res 403)))

        (t/testing "Modify ppp with the same laatija-id"
          (let [modified-ppp (assoc-in created-ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
                new-ppp-body (j/write-value-as-string modified-ppp)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body new-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))]
            (assert-status put-res 200)))

        (t/testing "Can't modify ppp with the different laatija-id"
          (let [modified-ppp (assoc-in full-ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
                new-ppp-body (j/write-value-as-string modified-ppp)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (laatija-test-data/with-suomifi-laatija2)
                                        (mock/body new-ppp-body)))]
            (assert-status put-res 400)))

        (t/testing "owner can delete own perusparannuspassi"
          (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                           (mock/header "Accept" "application/json")
                                           (laatija-test-data/with-suomifi-laatija)))]
            (assert-status delete-res 200)))

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
            (assert-status post-res 403)))

        (t/testing "can't delete other laatija's perusparannuspassi"
          (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                           (mock/header "Accept" "application/json")
                                           (laatija-test-data/with-suomifi-laatija2)))]
            (assert-status delete-res 500)))

        (t/testing "Get requires authentication"
          (let [unauth-get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                               (mock/header "Accept" "application/json")))]
            (assert-status unauth-get-res 403)))))))

(t/deftest post-ppp-numeric-validation-test
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
      (assert-status post-res 201 "Expected status 201 from ET creation")
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
            (assert-status post-res 201 "Expected status 201 - value is outside warning range but inside error range")
            (t/is (some? (:warnings response-body)) "Expected warnings to be present")
            (t/is (seq (:warnings response-body)) "Expected warnings list to be non-empty")
            (let [warnings (:warnings response-body)]
              (t/is (some #(= "tulokset.kaukolampo-hinta" (:property %)) warnings)
                    "Expected warning for tulokset.kaukolampo-hinta"))))

        (t/testing "PPP with kaukolampo-hinta at 1 cent should fail with 400 (outside error range 2-100)"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:tulokset :kaukolampo-hinta] 1))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (mock/body ppp-body)
                                         (laatija-test-data/with-suomifi-laatija)))]
            (assert-status post-res 400 "Expected status 400 - value is outside error range")))))))

(t/deftest put-ppp-numeric-validation-test
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
      (assert-status post-res 201 "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)
            ;; Create initial PPP with valid kaukolampo-hinta
            initial-ppp (-> (ppp-test-data/generate-add et-id)
                            (assoc-in [:tulokset :kaukolampo-hinta] 10))
            initial-ppp-body (-> initial-ppp j/write-value-as-string)
            create-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body initial-ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))
            ppp-id (-> create-res :body (j/read-value object-mapper) :id)]
        (assert-status create-res 201 "Expected status 201 from initial PPP creation")

        (t/testing "PUT: Update PPP with kaukolampo-hinta at 3 cents should succeed with warnings (outside warning range 5-20)"
          (let [warning-ppp (assoc-in initial-ppp [:tulokset :kaukolampo-hinta] 3)
                warning-ppp-body (-> warning-ppp j/write-value-as-string)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body warning-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))
                response-body (-> put-res :body (j/read-value object-mapper))]
            (assert-status put-res 200 "Expected status 200 - value is outside warning range but inside error range")
            (t/is (some? (:warnings response-body)) "Expected warnings to be present")
            (t/is (seq (:warnings response-body)) "Expected warnings list to be non-empty")
            (let [warnings (:warnings response-body)]
              (t/is (some #(= "tulokset.kaukolampo-hinta" (:property %)) warnings)
                    "Expected warning for tulokset.kaukolampo-hinta"))))

        (t/testing "PUT: Update PPP with kaukolampo-hinta at 1 cent should fail with 400 (outside error range 2-100)"
          (let [invalid-ppp (assoc-in initial-ppp [:tulokset :kaukolampo-hinta] 1)
                invalid-ppp-body (-> invalid-ppp j/write-value-as-string)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body invalid-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))]
            (assert-status put-res 400 "Expected status 400 - value is outside error range on PUT")))))))

(t/deftest post-ppp-vaihe-numeric-validation-test
  (t/testing "PPP vaihe numeric validation for uusiutuvan-energian-hyodynnetty-osuus"
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
      (assert-status post-res 201 "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)]

        (t/testing "PPP with vaihe uusiutuvan-energian-hyodynnetty-osuus above 100 should fail with 400"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:vaiheet 0 :tulokset :uusiutuvan-energian-hyodynnetty-osuus] 101))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (mock/body ppp-body)
                                         (laatija-test-data/with-suomifi-laatija)))]
            (assert-status post-res 400 "Expected status 400 - value is outside error range 0-100")))

        (t/testing "PPP with vaihe uusiutuvan-energian-hyodynnetty-osuus at 50 should succeed"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:vaiheet 0 :tulokset :uusiutuvan-energian-hyodynnetty-osuus] 50))
                ppp-body (-> ppp j/write-value-as-string)
                post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                         (mock/header "Accept" "application/json")
                                         (mock/header "Content-Type" "application/json")
                                         (mock/body ppp-body)
                                         (laatija-test-data/with-suomifi-laatija)))]
            (assert-status post-res 201 "Expected status 201 - value is within valid range 0-100")))))))



(t/deftest put-ppp-vaihe-numeric-validation-test
  (t/testing "PPP vaihe numeric validation for uusiutuvan-energian-hyodynnetty-osuus"
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
      (assert-status post-res 201 "Expected status 201 from ET creation")
      (let [et-id (-> post-res :body (j/read-value object-mapper) :id)
            ;; Create initial PPP with valid vaihe value
            initial-ppp (-> (ppp-test-data/generate-add et-id)
                            (assoc-in [:vaiheet 0 :tulokset :uusiutuvan-energian-hyodynnetty-osuus] 50))
            initial-ppp-body (-> initial-ppp j/write-value-as-string)
            create-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body initial-ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))
            ppp-id (-> create-res :body (j/read-value object-mapper) :id)]
        (assert-status create-res 201 "Expected status 201 from initial PPP creation")

        (t/testing "PUT: Update PPP vaihe with uusiutuvan-energian-hyodynnetty-osuus above 100 should fail with 400"
          (let [invalid-ppp (assoc-in initial-ppp [:vaiheet 0 :tulokset :uusiutuvan-energian-hyodynnetty-osuus] 101)
                invalid-ppp-body (-> invalid-ppp j/write-value-as-string)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body invalid-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))]
            (assert-status put-res 400 "Expected status 400 - value is outside error range 0-100 on PUT")))

        (t/testing "PUT: Update PPP vaihe with uusiutuvan-energian-hyodynnetty-osuus at 75 should succeed"
          (let [updated-ppp (assoc-in initial-ppp [:vaiheet 0 :tulokset :uusiutuvan-energian-hyodynnetty-osuus] 75)
                updated-ppp-body (-> updated-ppp j/write-value-as-string)
                put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body updated-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))]
            (assert-status put-res 200 "Expected status 200 - value is within valid range 0-100 on PUT")))))))
