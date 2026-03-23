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

(defn- post-et! [versio et]
  (ts/handler (-> (mock/request :post (str "/api/private/energiatodistukset/" versio))
                  (mock/header "Accept" "application/json")
                  (mock/header "Content-Type" "application/json")
                  (mock/body (j/write-value-as-string et))
                  (laatija-test-data/with-suomifi-laatija))))

(defn- get-et [versio id]
  (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/" versio "/" id))
                  (mock/header "Accept" "application/json")
                  (laatija-test-data/with-suomifi-laatija))))

(defn- put-et! [versio id et]
  (ts/handler (-> (mock/request :put (str "/api/private/energiatodistukset/" versio "/" id))
                  (mock/header "Accept" "application/json")
                  (mock/header "Content-Type" "application/json")
                  (mock/body (j/write-value-as-string et))
                  (laatija-test-data/with-suomifi-laatija))))

(defn- response-body [response]
  (-> response :body (j/read-value object-mapper)))

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
          et2026-id (-> list-response :body (j/read-value object-mapper) first :id)
          response (-> (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/2026/" et2026-id))
                                       (mock/header "Accept" "application/json")
                                       (laatija-test-data/with-suomifi-laatija)))
                       :body
                       (j/read-value object-mapper))
          coerced-response (let [coercer (sc/coercer et-schema/Energiatodistus2026 stc/json-coercion-matcher)]
                             (coercer response))]
      (t/is (not (s-utils/error? coerced-response))))))

(t/deftest post-2026-e-luokka-downgrade-test
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  ;; Note: generated test data uses value 1.0M for all numeric fields,
  ;; so e-luku will be very small (well within A+ range for YAT).

  (t/testing "POST 2026: tayttaa-aplus/a0-vaatimukset both true → e-luokka A+"
    ;; Given: 2026-todistus with both tayttaa-*-vaatimukset true
    (let [et (-> (et-test-data/generate-add 2026 true)
                 (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] true)
                 (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] true))
          ;; When: POST and then GET
          post-res (post-et! 2026 et)
          _ (t/is (= 201 (:status post-res)))
          et-id (:id (response-body post-res))
          get-res (get-et 2026 et-id)
          body (response-body get-res)]
      ;; Then: e-luokka is "A+" (both rastit true, no downgrade)
      (t/is (= "A+" (get-in body [:tulokset :e-luokka]))
            "Both tayttaa-*-vaatimukset true → e-luokka should be A+")))

  (t/testing "POST 2026: tayttaa-aplus/a0-vaatimukset both false → e-luokka A (downgraded)"
    ;; Given: 2026-todistus with both tayttaa-*-vaatimukset false
    (let [et (-> (et-test-data/generate-add 2026 true)
                 (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] false)
                 (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] false))
          ;; When: POST and then GET
          post-res (post-et! 2026 et)
          _ (t/is (= 201 (:status post-res)))
          et-id (:id (response-body post-res))
          get-res (get-et 2026 et-id)
          body (response-body get-res)]
      ;; Then: e-luokka is "A" (downgraded from A+ because false, false)
      (t/is (= "A" (get-in body [:tulokset :e-luokka]))
            "Both tayttaa-*-vaatimukset false → e-luokka should be A (downgraded from A+)")))

  (t/testing "POST 2026: aplus=false, a0=true → e-luokka A0 (downgraded from A+)"
    ;; Given: 2026-todistus with aplus false, a0 true
    (let [et (-> (et-test-data/generate-add 2026 true)
                 (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] false)
                 (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] true))
          ;; When: POST and then GET
          post-res (post-et! 2026 et)
          _ (t/is (= 201 (:status post-res)))
          et-id (:id (response-body post-res))
          get-res (get-et 2026 et-id)
          body (response-body get-res)]
      ;; Then: e-luokka is "A0" (downgraded from A+ because aplus false, a0 stays)
      (t/is (= "A0" (get-in body [:tulokset :e-luokka]))
            "aplus=false, a0=true → e-luokka should be A0"))))


(t/deftest put-2026-e-luokka-downgrade-test
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  ;; Given: 2026-todistus created with both tayttaa-*-vaatimukset false
  (let [et (-> (et-test-data/generate-add 2026 true)
               (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] false)
               (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] false))
        post-res (post-et! 2026 et)
        _ (t/is (= 201 (:status post-res)))
        et-id (:id (response-body post-res))]

    (t/testing "PUT: set both rastit false→true → e-luokka changes A→A+"
      ;; When: update both tayttaa-*-vaatimukset to true
      (let [updated-et (-> et
                           (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] true)
                           (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] true))
            put-res (put-et! 2026 et-id updated-et)]
        ;; Then: e-luokka should now be "A+"
        (t/is (= 200 (:status put-res)))
        (let [verify-body (response-body (get-et 2026 et-id))]
          (t/is (= "A+" (get-in verify-body [:tulokset :e-luokka]))
                "After setting both rastit true, e-luokka should be A+"))))

    (t/testing "PUT: set aplus true→false (a0 still true) → e-luokka changes to A0"
      ;; When: set aplus false, a0 true
      (let [updated-et (-> et
                           (assoc-in [:perustiedot :tayttaa-aplus-vaatimukset] false)
                           (assoc-in [:perustiedot :tayttaa-a0-vaatimukset] true))
            put-res (put-et! 2026 et-id updated-et)]
        ;; Then: e-luokka should be "A0"
        (t/is (= 200 (:status put-res)))
        (let [verify-body (response-body (get-et 2026 et-id))]
          (t/is (= "A0" (get-in verify-body [:tulokset :e-luokka]))
                "aplus=false, a0=true → e-luokka should be A0"))))))
