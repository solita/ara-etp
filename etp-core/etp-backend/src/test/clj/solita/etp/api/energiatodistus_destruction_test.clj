(ns solita.etp.api.energiatodistus-destruction-test
  (:require
    [clojure.test :as t]
    [ring.mock.request :as mock]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest can-call-energiatodistus-expiration-endpoint
  (t/testing "Can call energiatodistus expiration endpoint"
    (let [response (ts/handler (-> (mock/request :post "/api/internal/energiatodistukset/anonymize-and-delete-expired")
                                   (mock/header "Accept" "application/json")))]
      (t/is (= (:status response) 200)))))
