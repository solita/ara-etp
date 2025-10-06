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
          created-ppp (-> get-res :body (j/read-value object-mapper) (dissoc :id :tila-id :laatija-id))]
      (t/is (= 200 (:status get-res)))

      (let [modified-ppp (assoc-in created-ppp [:passin-perustiedot :tayttaa-a0-vaatimukset] true)
            new-ppp-body (j/write-value-as-string modified-ppp)
            put-res (ts/handler (-> (mock/request :put (str "/api/private/perusparannuspassit/2026/" ppp-id))
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (mock/body new-ppp-body)
                                    (laatija-test-data/with-suomifi-laatija)))]
      (t/is (= 200 (:status put-res)))))))