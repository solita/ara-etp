(ns solita.etp.api.perusparannuspassi-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.energiatodistus :as et-test-data]
    [solita.etp.test-data.kayttaja :as kayttaja-test-data]
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

        (t/testing "PPP with kaukolampo-hinta at 21 cents should succeed with warnings (outside warning range 0-20)"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:tulokset :kaukolampo-hinta] 21))
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

        (t/testing "PPP with kaukolampo-hinta at 201 cent should fail with 400 (outside error range 0-200)"
          (let [ppp (-> (ppp-test-data/generate-add et-id)
                        (assoc-in [:tulokset :kaukolampo-hinta] 201))
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

        (t/testing "PUT: Update PPP with kaukolampo-hinta at 21 cents should succeed with warnings (outside warning range 0-20)"
          (let [warning-ppp (assoc-in initial-ppp [:tulokset :kaukolampo-hinta] 21)
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

        (t/testing "PUT: Update PPP with kaukolampo-hinta at 201 cent should fail with 400 (outside error range 0-200)"
          (let [invalid-ppp (assoc-in initial-ppp [:tulokset :kaukolampo-hinta] 201)
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

(t/deftest ppp-paakayttaja-access-test
  ;; Given: a laatija with ppp-pätevyys creates an energiatodistus and perusparannuspassi
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (kayttaja-test-data/insert-virtu-paakayttaja!)

  (let [;; Given: laatija creates an energiatodistus (2026)
        et (et-test-data/generate-add 2026 true)
        et-body (j/write-value-as-string et)
        et-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (mock/body et-body)
                                    (laatija-test-data/with-suomifi-laatija)))
        et-id (-> et-post-res :body (j/read-value object-mapper) :id)

        ;; Given: laatija creates a perusparannuspassi for that energiatodistus
        ppp (ppp-test-data/generate-add et-id)
        ppp-body (j/write-value-as-string ppp)
        ppp-post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body ppp-body)
                                     (laatija-test-data/with-suomifi-laatija)))
        ppp-id (-> ppp-post-res :body (j/read-value object-mapper) :id)]

    ;; Verify setup succeeded
    (assert-status et-post-res 201 "Setup: ET creation should succeed")
    (assert-status ppp-post-res 201 "Setup: PPP creation should succeed")

    (t/testing "Pääkäyttäjä can GET perusparannuspassi when draft-visible-to-paakayttaja is true"
      ;; Given: laatija updates the ET to be visible to pääkäyttäjä
      (let [updated-et (assoc et :draft-visible-to-paakayttaja true)
            updated-et-body (j/write-value-as-string updated-et)
            put-et-res (ts/handler (-> (mock/request :put (str "/api/private/energiatodistukset/2026/" et-id))
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body updated-et-body)
                                       (laatija-test-data/with-suomifi-laatija)))]
        (assert-status put-et-res 200 "Setup: ET update with draft-visible-to-paakayttaja should succeed")
        ;; When: pääkäyttäjä sends GET request for the PPP
        (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (kayttaja-test-data/with-virtu-user)))]
          ;; Then: response status is 200 with PPP data
          (assert-status get-res 200 "Pääkäyttäjä should be able to GET perusparannuspassi"))))

    (t/testing "Pääkäyttäjä cannot POST new perusparannuspassi"
      ;; When: pääkäyttäjä tries to create a new PPP
      (let [new-ppp (ppp-test-data/generate-add et-id)
            new-ppp-body (j/write-value-as-string new-ppp)
            post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body new-ppp-body)
                                     (kayttaja-test-data/with-virtu-user)))]
        ;; Then: access is denied
        (assert-status post-res 403 "Pääkäyttäjä should not be able to POST perusparannuspassi")))

    (t/testing "Pääkäyttäjä cannot PUT perusparannuspassi"
      ;; When: pääkäyttäjä tries to modify the PPP
      (let [modified-ppp (assoc-in ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
            put-body (j/write-value-as-string modified-ppp)
            put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (mock/body put-body)
                                    (kayttaja-test-data/with-virtu-user)))]
        ;; Then: access is denied
        (assert-status put-res 403 "Pääkäyttäjä should not be able to PUT perusparannuspassi")))

    (t/testing "Pääkäyttäjä cannot DELETE perusparannuspassi"
      ;; When: pääkäyttäjä tries to delete the PPP
      (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                       (mock/header "Accept" "application/json")
                                       (kayttaja-test-data/with-virtu-user)))]
        ;; Then: access is denied
        (assert-status delete-res 403 "Pääkäyttäjä should not be able to DELETE perusparannuspassi")))))

(t/deftest ppp-paakayttaja-access-signed-et-test
  ;; Given: a laatija with ppp-pätevyys creates an energiatodistus, a PPP, then signs the ET
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))]

    (kayttaja-test-data/insert-virtu-paakayttaja!)

    (let [;; Given: laatija creates an energiatodistus (2026)
          et (et-test-data/generate-add 2026 true)
          et-body (j/write-value-as-string et)
          et-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body et-body)
                                      (laatija-test-data/with-suomifi-laatija)))
          et-id (-> et-post-res :body (j/read-value object-mapper) :id)

          ;; Given: laatija creates a perusparannuspassi while ET is still in draft
          ppp (ppp-test-data/generate-add et-id)
          ppp-body (j/write-value-as-string ppp)
          ppp-post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))
          ppp-id (-> ppp-post-res :body (j/read-value object-mapper) :id)]

      (assert-status et-post-res 201 "Setup: ET creation should succeed")
      (assert-status ppp-post-res 201 "Setup: PPP creation should succeed")

      ;; Sign the energiatodistus to set tila-id = 2
      (et-test-data/sign! et-id laatija-id true)

      (t/testing "Pääkäyttäjä can GET perusparannuspassi when ET is signed (tila-id = 2)"
        ;; When: pääkäyttäjä sends GET request for the PPP
        (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (kayttaja-test-data/with-virtu-user)))]
          ;; Then: response status is 200 with PPP data
          (assert-status get-res 200 "Pääkäyttäjä should be able to GET perusparannuspassi for signed ET"))))))

(t/deftest ppp-laatija-access-regression-test
  ;; Given: two laatijas with ppp-pätevyys
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (laatija-test-data/insert-suomifi-laatija2!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (let [;; Given: laatija 1 creates an energiatodistus and perusparannuspassi
        et (et-test-data/generate-add 2026 true)
        et-body (j/write-value-as-string et)
        et-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (mock/body et-body)
                                    (laatija-test-data/with-suomifi-laatija)))
        et-id (-> et-post-res :body (j/read-value object-mapper) :id)

        ppp (ppp-test-data/generate-add et-id)
        ppp-body (j/write-value-as-string ppp)
        ppp-post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body ppp-body)
                                     (laatija-test-data/with-suomifi-laatija)))
        ppp-id (-> ppp-post-res :body (j/read-value object-mapper) :id)]

    ;; Verify setup succeeded
    (assert-status et-post-res 201 "Setup: ET creation should succeed")
    (assert-status ppp-post-res 201 "Setup: PPP creation should succeed")

    (t/testing "Owner laatija can still GET own perusparannuspassi"
      ;; When: the owning laatija sends GET request
      (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))]
        ;; Then: response status is 200
        (assert-status get-res 200 "Owner laatija should be able to GET own perusparannuspassi")))

    (t/testing "Owner laatija can GET own perusparannuspassi PDF"
      ;; When: the owning laatija requests the PDF version
      (let [pdf-filename (format "perusparannuspassi-%s-fi.pdf" ppp-id)
            pdf-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id
                                                           "/pdf/fi/" pdf-filename))
                                    (mock/header "Accept" "application/pdf")
                                    (laatija-test-data/with-suomifi-laatija)))]
        ;; Then: response status is 200
        (assert-status pdf-res 200 "Owner laatija should be able to GET own perusparannuspassi PDF")))

    (t/testing "Owner laatija can GET own perusparannuspassi HTML"
      ;; When: the owning laatija requests the HTML version
      (let [html-filename (format "perusparannuspassi-%s-fi.html" ppp-id)
            html-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id
                                                            "/html/fi/" html-filename))
                                     (mock/header "Accept" "text/html")
                                     (laatija-test-data/with-suomifi-laatija)))]
        ;; Then: response status is 200
        (assert-status html-res 200 "Owner laatija should be able to GET own perusparannuspassi HTML")))

    (t/testing "Different laatija cannot GET another laatija's perusparannuspassi"
      ;; When: a different laatija (laatija 2) sends GET request
      (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (laatija-test-data/with-suomifi-laatija2)))]
        ;; Then: PPP not found for this user (SQL filters by laatija-id)
        (assert-status get-res 404 "Different laatija should not be able to GET another's perusparannuspassi")))

    (t/testing "Owner laatija can still DELETE own perusparannuspassi"
      ;; When: the owning laatija sends DELETE request
      (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija)))]
        ;; Then: response status is 200
        (assert-status delete-res 200 "Owner laatija should be able to DELETE own perusparannuspassi"))))

  ;; Given: fresh setup for cross-laatija delete test (separate from above since PPP was deleted)
  (let [et2 (et-test-data/generate-add 2026 true)
        et2-body (j/write-value-as-string et2)
        et2-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body et2-body)
                                     (laatija-test-data/with-suomifi-laatija)))
        et2-id (-> et2-post-res :body (j/read-value object-mapper) :id)

        ppp2 (ppp-test-data/generate-add et2-id)
        ppp2-body (j/write-value-as-string ppp2)
        ppp2-post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body ppp2-body)
                                      (laatija-test-data/with-suomifi-laatija)))
        ppp2-id (-> ppp2-post-res :body (j/read-value object-mapper) :id)]

    (assert-status et2-post-res 201 "Setup: ET2 creation should succeed")
    (assert-status ppp2-post-res 201 "Setup: PPP2 creation should succeed")

    (t/testing "Different laatija cannot DELETE another laatija's perusparannuspassi"
      ;; When: laatija 2 tries to delete laatija 1's PPP
      (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp2-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija2)))]
        (t/is (not= 200 (:status delete-res))
              "Different laatija should not be able to DELETE another's perusparannuspassi")))))

(t/deftest ppp-locked-after-signing-test
  ;; Given: a laatija with ppp-pätevyys creates an energiatodistus (2026) and a PPP, then signs the ET
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))]

    (kayttaja-test-data/insert-virtu-paakayttaja!)

    (let [;; Given: laatija creates an energiatodistus (2026)
          et (et-test-data/generate-add 2026 true)
          et-body (j/write-value-as-string et)
          et-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body et-body)
                                      (laatija-test-data/with-suomifi-laatija)))
          et-id (-> et-post-res :body (j/read-value object-mapper) :id)

          ;; Given: laatija creates a perusparannuspassi while ET is still in draft
          ppp (ppp-test-data/generate-add et-id)
          ppp-body (j/write-value-as-string ppp)
          ppp-post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))
          ppp-id (-> ppp-post-res :body (j/read-value object-mapper) :id)]

      ;; Verify setup
      (assert-status et-post-res 201 "Setup: ET creation should succeed")
      (assert-status ppp-post-res 201 "Setup: PPP creation should succeed")

      ;; Given: energiatodistus is signed (tila-id = 2)
      (et-test-data/sign! et-id laatija-id true)

      ;; Test 1.1 — Laatija cannot PUT (update) PPP after ET is signed
      (t/testing "Laatija cannot PUT perusparannuspassi after ET is signed"
        ;; When: owner laatija sends PUT request to update PPP
        (let [modified-ppp (assoc-in ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
              put-body (j/write-value-as-string modified-ppp)
              put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body put-body)
                                      (laatija-test-data/with-suomifi-laatija)))]
          ;; Then: response is 403 Forbidden
          (assert-status put-res 403 "Owner laatija should not be able to PUT PPP on signed ET")))

      ;; Test 1.3 — Laatija cannot DELETE PPP after ET is signed
      (t/testing "Laatija cannot DELETE perusparannuspassi after ET is signed"
        ;; When: owner laatija sends DELETE request for the PPP
        (let [delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                         (mock/header "Accept" "application/json")
                                         (laatija-test-data/with-suomifi-laatija)))]
          ;; Then: response is 403 Forbidden
          (assert-status delete-res 403 "Owner laatija should not be able to DELETE PPP on signed ET")))

      ;; Test 1.4 — Pääkäyttäjä cannot PUT PPP after ET is signed
      (t/testing "Pääkäyttäjä cannot PUT perusparannuspassi after ET is signed"
        ;; When: pääkäyttäjä sends PUT request to update PPP
        (let [modified-ppp (assoc-in ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
              put-body (j/write-value-as-string modified-ppp)
              put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body put-body)
                                      (kayttaja-test-data/with-virtu-user)))]
          ;; Then: response is 403 Forbidden
          (assert-status put-res 403 "Pääkäyttäjä should not be able to PUT PPP on signed ET")))

      ;; Test 1.5 — Laatija can still GET PPP after ET is signed
      (t/testing "Laatija can still GET perusparannuspassi after ET is signed"
        ;; When: owner laatija sends GET request for the PPP
        (let [get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                      (mock/header "Accept" "application/json")
                                      (laatija-test-data/with-suomifi-laatija)))]
          ;; Then: response is 200 OK
          (assert-status get-res 200 "Owner laatija should still be able to GET PPP on signed ET"))))))

(t/deftest ppp-post-locked-after-signing-test
  ;; Test 1.2 — Laatija cannot POST (create) a new PPP after ET is signed
  ;; Given: a laatija with ppp-pätevyys creates an ET (2026) without PPP, then signs it
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))]

    (let [;; Given: laatija creates an energiatodistus (2026) without PPP
          et (et-test-data/generate-add 2026 true)
          et-body (j/write-value-as-string et)
          et-post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                      (mock/header "Accept" "application/json")
                                      (mock/header "Content-Type" "application/json")
                                      (mock/body et-body)
                                      (laatija-test-data/with-suomifi-laatija)))
          et-id (-> et-post-res :body (j/read-value object-mapper) :id)]

      ;; Verify setup
      (assert-status et-post-res 201 "Setup: ET creation should succeed")

      ;; Given: energiatodistus is signed (tila-id = 2)
      (et-test-data/sign! et-id laatija-id true)

      (t/testing "Laatija cannot POST perusparannuspassi after ET is signed"
        ;; When: owner laatija sends POST request to create a new PPP for the signed ET
        (let [ppp (ppp-test-data/generate-add et-id)
              ppp-body (j/write-value-as-string ppp)
              post-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                       (mock/header "Accept" "application/json")
                                       (mock/header "Content-Type" "application/json")
                                       (mock/body ppp-body)
                                       (laatija-test-data/with-suomifi-laatija)))]
          ;; Then: response is 403 Forbidden
          (assert-status post-res 403 "Owner laatija should not be able to POST PPP on signed ET"))))))
