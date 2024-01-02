(ns solita.etp.energiatodistus-search-api-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.service.energiatodistus-search-test :as energiatodistus-search-test]))

(t/use-fixtures :each ts/fixture)

(t/deftest search-energiatodistus
  (laatija-test-data/insert! (laatija-test-data/generate-adds 1))
  (test-kayttajat/insert-virtu-paakayttaja!)
  (let [energiatodistus-adds (concat
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 1)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 2)))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 2)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 1)))
                                    (energiatodistus-test-data/generate-adds 1 2018 true))
                               (map (fn [et]
                                      (-> et
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-1 :id] 3)
                                          (assoc-in [:lahtotiedot :lammitys :lammitysmuoto-2 :id] 4)))
                                    (energiatodistus-test-data/generate-adds 2 2018 true)))
        ids (energiatodistus-test-data/insert! energiatodistus-adds 1)]
    (energiatodistus-search-test/sign-energiatodistukset! (map vector (repeatedly (constantly 1)) ids))
    (t/testing "Haku ilman parametrejä palauttaa kaikki energiatodisukset"
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset")
                                              (test-kayttajat/with-virtu-user)
                                              (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 4))))
    (t/testing "Haku löytää energiatodistukset, joissa kumpi tahansa lämmitysmuoto on haettu"
      (let [response (ts/handler (-> (mock/request :get "/api/private/energiatodistukset?where=%5B%5B%5B%22%3D%22%2C%22energiatodistus.lahtotiedot.lammitys.lammitysmuoto.id%22%2C1%5D%5D%5D")
                                  (test-kayttajat/with-virtu-user)
                                  (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 2))
        (t/is (every? (fn [et] (or (= (->> et :lahtotiedot :lammitys :lammitysmuoto-1 :id) 1)
                                   (= (->> et :lahtotiedot :lammitys :lammitysmuoto-2 :id) 1)))
                      response-body))))))
