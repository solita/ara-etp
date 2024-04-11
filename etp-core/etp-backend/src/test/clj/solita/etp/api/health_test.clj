(ns solita.etp.api.health-test
  (:require [clojure.test :as t]
            [ring.mock.request :as mock]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest health-test
  (let [response (ts/handler (-> (mock/request :get (format "/api/health"))))]
    (t/is (= (:status response) 200))))
