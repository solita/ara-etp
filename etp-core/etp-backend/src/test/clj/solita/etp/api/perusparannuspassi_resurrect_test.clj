(ns solita.etp.api.perusparannuspassi-resurrect-test
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

(t/deftest ppp-resurrect-test
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (t/testing "Resurrect deleted PPP"
    (let [;; 1. Create ET
          et (-> (et-test-data/generate-add 2026 true))
          et-body (-> et j/write-value-as-string)
          post-res (ts/handler (-> (mock/request :post "/api/private/energiatodistukset/2026")
                                   (mock/header "Accept" "application/json")
                                   (mock/header "Content-Type" "application/json")
                                   (mock/body et-body)
                                   (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status post-res 201)
          et-id (-> post-res :body (j/read-value object-mapper) :id)

          ;; 2. Create PPP
          ppp (-> (ppp-test-data/generate-add et-id))
          ppp-body (-> ppp j/write-value-as-string)
          create-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body ppp-body)
                                     (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status create-res 201)
          created-ppp-response (-> create-res :body (j/read-value object-mapper))
          ppp-id (:id created-ppp-response)

          ;; 3. Fetch full PPP to modify it
          get-res (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                  (mock/header "Accept" "application/json")
                                  (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status get-res 200)
          full-ppp (j/read-value (:body get-res) object-mapper)
          ppp-to-modify (dissoc full-ppp :id :tila-id :laatija-id)

          ;; 4. Modify PPP (set havainnointikaynnin-pvm)
          modified-ppp (assoc-in ppp-to-modify
                                 [:passin-perustiedot :havainnointikaynti] "2025-01-01")
          put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                  (mock/header "Accept" "application/json")
                                  (mock/header "Content-Type" "application/json")
                                  (mock/body (j/write-value-as-string modified-ppp))
                                  (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status put-res 200)

          ;; 5. Delete PPP
          delete-res (ts/handler (-> (mock/request :delete (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                     (mock/header "Accept" "application/json")
                                     (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status delete-res 200)

          ;; 6. Create PPP again (should resurrect)
          new-ppp (-> (ppp-test-data/generate-add et-id)) ;; New empty PPP
          new-ppp-body (-> new-ppp j/write-value-as-string)
          resurrect-res (ts/handler (-> (mock/request :post "/api/private/perusparannuspassit/2026")
                                        (mock/header "Accept" "application/json")
                                        (mock/header "Content-Type" "application/json")
                                        (mock/body new-ppp-body)
                                        (laatija-test-data/with-suomifi-laatija)))
          _ (assert-status resurrect-res 201 "Resurrection POST should return 201")
          resurrected-ppp-id (-> resurrect-res :body (j/read-value object-mapper) :id)]

      ;; 7. Verify ID is the same
      (t/is (= ppp-id resurrected-ppp-id) "Resurrected PPP ID should match original ID")

      ;; 8. Verify data persists
      (let [get-res-2 (ts/handler (-> (mock/request :get (str "/api/private/perusparannuspassit/2026/" resurrected-ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            _ (j/read-value (:body get-res-2) object-mapper)]
        (t/is (= ppp-id resurrected-ppp-id))))))
