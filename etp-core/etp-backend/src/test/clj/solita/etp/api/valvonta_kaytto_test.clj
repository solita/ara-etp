(ns solita.etp.api.valvonta-kaytto-test
  (:require [clojure.test :as t]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.test-data.kayttaja :as test-kayttaja]
            [solita.etp.test-system :as ts]))

(defn user-fixture [f]
  (test-kayttaja/insert-virtu-paakayttaja!)
  (f))

(t/use-fixtures :each ts/fixture user-fixture)

(t/deftest hallinto-oikeudet-api-test
  (t/testing "/hallinto-oikeudet returns status 403 when not authenticated"
    (let [{:keys [body status]} (ts/handler (mock/request :get "/api/private/valvonta/kaytto/hallinto-oikeudet"))]
      (t/is (= status 403))
      (t/is (= body "Forbidden"))))

  (t/testing "/hallinto-oikeudet returns status 200 and hallinto-oikeus data when properly authenticated"
    (let [{:keys [body status]} (->> "/api/private/valvonta/kaytto/hallinto-oikeudet"
                                     (mock/request :get)
                                     test-kayttaja/with-virtu-user
                                     ts/handler)]
      (t/is (= status 200))
      (t/is (= (j/read-value body j/keyword-keys-object-mapper)
               [{:id       0
                 :label-fi "Helsingin hallinto-oikeus"
                 :label-sv "Helsingfors förvaltningsdomstol"
                 :valid    true}
                {:id       1
                 :label-fi "Hämeenlinnan hallinto-oikeus"
                 :label-sv "Tavastehus förvaltningsdomstol"
                 :valid    true}
                {:id       2
                 :label-fi "Itä-Suomen hallinto-oikeus"
                 :label-sv "Östra Finlands förvaltningsdomstol"
                 :valid    true}
                {:id       3
                 :label-fi "Pohjois-Suomen hallinto-oikeus"
                 :label-sv "Norra Finlands förvaltningsdomstol"
                 :valid    true}
                {:id       4
                 :label-fi "Turun hallinto-oikeus"
                 :label-sv "Åbo förvaltningsdomstol"
                 :valid    true}
                {:id       5
                 :label-fi "Vaasan hallinto-oikeus"
                 :label-sv "Vasa förvaltningsdomstol"
                 :valid    true}])))))
