(ns solita.etp.api.energiatodistus-copy-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.service.energiatodistus :as et-service]
    [solita.etp.service.energiatodistus-tila :as et-tila-service]
    [solita.etp.test-data.energiatodistus :as et-test-data]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-system :as ts])
  (:import
    (java.time Instant)))

(t/use-fixtures :each ts/fixture)

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(defn cleanup-et
  "Simulate the cleanup normally done in the front-end"

  ;; NOTE: if you need to change the cleanup here to match changes in the schema in the API,
  ;; you probably also need to change the `serialize` function in
  ;; etp-front/src/pages/energiatodistus/energiatodistus-api.js
  [et]
  (-> et
      (assoc :korvattu-energiatodistus-id nil
             :laskutettava-yritys-id nil
             :laskutusosoite-id nil
             :laskuriviviite nil
             :kommentti nil)
      (dissoc :id
              :laatija-fullname
              :laatija-id
              :perusparannuspassi-id
              :korvaava-energiatodistus-id
              :versio
              :tila-id
              :allekirjoitusaika
              :voimassaolo-paattymisaika
              :laskutusaika)
      (update :tulokset #(dissoc % :e-luku :e-luokka))))

(t/deftest energiatodistus-copy
  (let [laatija-id (-> (laatija-test-data/insert-suomifi-laatija!
                         (-> (laatija-test-data/generate-adds 1) first (merge {:patevyystaso 4}))))]

    (doseq [versio [2013 2018 2026]]
      (let [et (-> (et-test-data/generate-add versio true))
            et-body (-> et j/write-value-as-string)
            post-res (ts/handler (-> (mock/request :post (str "/api/private/energiatodistukset/" versio))
                                     (mock/header "Accept" "application/json")
                                     (mock/header "Content-Type" "application/json")
                                     (mock/body et-body)
                                     (laatija-test-data/with-suomifi-laatija)))]
        (t/is (= 201 (:status post-res)) "Expected status 201 from ET creation")
        (let [et-id (-> post-res :body (j/read-value object-mapper) :id)]
          (t/is (integer? et-id))
          (t/is (et-tila-service/draft? (et-service/find-energiatodistus ts/*db* et-id)))
          (et-test-data/sign-at-time! et-id laatija-id true (Instant/now))
          (t/is (et-tila-service/signed? (et-service/find-energiatodistus ts/*db* et-id)))

          (t/testing (str "Test that copying as-is over the API works for ET" versio)
            (let [et (-> (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/" versio "/" et-id))
                                         (mock/header "Accept" "application/json")
                                         (laatija-test-data/with-suomifi-laatija)))
                         :body
                         (j/read-value object-mapper)
                         cleanup-et)
                  post-res (ts/handler (-> (mock/request :post (str "/api/private/energiatodistukset/" versio))
                                           (mock/header "Accept" "application/json")
                                           (mock/header "Content-Type" "application/json")
                                           (mock/body (j/write-value-as-string et))
                                           (laatija-test-data/with-suomifi-laatija)))]
              (t/is (= 201 (:status post-res))))))))))
