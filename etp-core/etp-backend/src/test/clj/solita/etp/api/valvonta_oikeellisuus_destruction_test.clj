(ns solita.etp.api.valvonta-oikeellisuus-destruction-test
  (:require
    [clojure.test :as t]
    [ring.mock.request :as mock]
    [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest can-call-valvonta-oikeellisuus-expiration-endpoint
  (t/testing "Can call valvonta oikeellisuus expiration endpoint"
    (let [response (ts/handler (-> (mock/request :post "/api/internal/valvonta/oikeellisuus/anonymize-and-delete-expired")
                                   (mock/header "Accept" "application/json")))]
      (t/is (= 200 (:status response))))))

