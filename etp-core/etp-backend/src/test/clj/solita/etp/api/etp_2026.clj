(ns solita.etp.api.etp-2026
  "Tests related to the development phase of ETP 2026"
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.test-data.energiatodistus :as et-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-system :as ts]))


(t/use-fixtures :each ts/fixture)

(t/deftest perustaso-et-post-access
  (laatija-test-data/insert-suomifi-laatija!)

  (t/testing "An usual laatija may POST energiatodistus versions 2013 and 2018, but not 2026"
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
      (t/is (= 403 (:status post-res)) "Expected status 403 from ET creation"))))

(t/deftest ppp-taso-et-post-access
  (laatija-test-data/insert-suomifi-laatija!
    (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4})))

  (t/testing "A laatija with a '+ppp' pätevyystaso is permitted to POST all the existing kinds of ET"
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
      (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation"))))

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(t/deftest kayttotarkoitusluokat-endpoint-test
  (let [endpoint "/api/public/energiatodistukset/kayttotarkoitusluokat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "1. Erilliset pientalot" (:label-fi (some #(when (= 1 (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/kayttotarkoitusluokat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "1. Pienet asuinrakennukset" (:label-fi (some #(when (= 1 (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/kayttotarkoitusluokat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "1. Pienet asuinrakennukset" (:label-fi (some #(when (= 1 (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res)))))))

(t/deftest alakayttotarkoitusluokat-endpoint-test
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res)))))))

(t/deftest alakayttotarkoitusluokat-endpoint-test
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/alakayttotarkoitusluokat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "Yhden asunnon talot" (:label-fi (some #(when (= "YAT" (:id %)) %) response-body))))
        (t/is (= 200 (:status get-res)))))))

(t/deftest e-luokka-endpoint-test
  (let [endpoint "/api/public/energiatodistukset/e-luokka/2013/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/e-luokka/2018/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/public/energiatodistukset/e-luokka/2026/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res)))))))

(t/deftest validation-numeric-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/validation/numeric/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= {:min 50 :max 999999} (:warning (some #(when (= "lahtotiedot.lammitetty-nettoala" (:property %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/numeric/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= {:min 50 :max 999999} (:warning (some #(when (= "lahtotiedot.lammitetty-nettoala" (:property %)) %) response-body))))
        (t/is (= 200 (:status get-res))))))

  (let [endpoint "/api/private/validation/numeric/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= {:min 50 :max 999999} (:warning (some #(when (= "lahtotiedot.lammitetty-nettoala" (:property %)) %) response-body))))
        (t/is (= 200 (:status get-res)))))))

(t/deftest validation-required-bypass-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/validation/required/2013/bypass"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2018/bypass"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2026/bypass"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res)))))))

(t/deftest validation-required-all-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/validation/required/2013/all"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2018/all"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2026/all"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res)))))))

(t/deftest deprecated-validation-required-all-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/validation/required/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/required/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some? (some #{"laskutusosoite-id"} response-body)))
        (t/is (= 200 (:status get-res)))))))

(t/deftest sisaiset-kuormat-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/validation/sisaiset-kuormat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some #(contains? % :henkilot) response-body))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/sisaiset-kuormat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some #(contains? % :henkilot) response-body))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/validation/sisaiset-kuormat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some #(contains? % :henkilot) response-body))
        (t/is (= 200 (:status get-res)))))))

(t/deftest private-kayttotarkoitusluokat-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/kayttotarkoitusluokat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "1. Fristående småhus", :id 1, :label-fi "1. Erilliset pientalot"}) response-body))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/kayttotarkoitusluokat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "1. Små bostadsbyggnader", :id 1, :label-fi "1. Pienet asuinrakennukset"}) response-body))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/kayttotarkoitusluokat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "1. Små bostadsbyggnader", :id 1, :label-fi "1. Pienet asuinrakennukset"}) response-body))
        (t/is (= 200 (:status get-res)))))))

(t/deftest private-alakayttotarkoitusluokat-endpoint-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/alakayttotarkoitusluokat/2013"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "Hus med en bostad", :kayttotarkoitusluokka-id 1, :id "YAT", :label-fi "Yhden asunnon talot"}) response-body))
        (t/is (= 200 (:status get-res))))))

  (let [endpoint "/api/private/alakayttotarkoitusluokat/2018"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "Hus med en bostad", :kayttotarkoitusluokka-id 1, :id "YAT", :label-fi "Yhden asunnon talot"}) response-body))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/alakayttotarkoitusluokat/2026"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (some (partial = {:valid true, :label-sv "Hus med en bostad", :kayttotarkoitusluokka-id 1, :id "YAT", :label-fi "Yhden asunnon talot"}) response-body))
        (t/is (= 200 (:status get-res)))))))

(t/deftest private-e-luokka-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [endpoint "/api/private/e-luokka/2013/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/e-luokka/2018/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res))))))
  (let [endpoint "/api/private/e-luokka/2026/YAT/100/25"]
    (t/testing endpoint
      (let [get-res (ts/handler (-> (mock/request :get endpoint)
                                    (mock/header "Accept" "application/json")
                                    (mock/header "Content-Type" "application/json")
                                    (laatija-test-data/with-suomifi-laatija)))
            response-body (-> get-res :body (j/read-value object-mapper))]
        (t/is (= "A" (:e-luokka response-body)))
        (t/is (= 200 (:status get-res)))))))
