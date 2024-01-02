(ns solita.etp.valvonta-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.schema.valvonta-kaytto :as valvonta-schema]
    [solita.etp.service.suomifi-viestit :as suomifi-viestit]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.generators :as generators]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time LocalDate)))

(t/use-fixtures :each ts/fixture)

(defn- non-nil-key->string [m k]
  (if (some? (k m))
    (assoc m k (str (k m))) m))

(defn- add-valvonta-and-map-id! [valvonta]
  (assoc valvonta :id (valvonta-service/add-valvonta! ts/*db* valvonta)))

(defn- add-toimenpide-and-map-id! [valvonta-id toimenpide]
  (merge toimenpide (valvonta-service/add-toimenpide! ts/*db* ts/*aws-s3-client* {} valvonta-id toimenpide)))

(t/deftest adding-toimenpide
  (t/testing "Kun uusi toimenpide lisätään, omistajille joilla on Suomi.fi-viestit käytössä lähtee viesti oikealla kuvauksella"
    (let [suomifi-message-sent (promise)
          assert-suomifi-message-sent (fn [sanoma kohde & _]
                                        (t/is (= {:tunniste "ARA-ETP-KV-1-1-PERSON-1"
                                                  :versio   "1.0"
                                                  }
                                                 sanoma
                                                 ))
                                        (t/is (= (str "Tämän viestin liitteenä on tietopyyntö koskien rakennustasi: 1035150826\n"
                                                      "katu, 65100 VAASA\n"
                                                      "Kehotamme vastaamaan tietopyyntöön 10.11.2024 mennessä.\n\n"
                                                      "Som bilaga till detta meddelande finns en begäran om information som gäller din byggnad: 1035150826\n"
                                                      "katu, 65100 VASA\n"
                                                      "Vi uppmanar dig att besvara begäran om information senast den 10.11.2024.")
                                                 (:kuvaus-teksti kohde))
                                              )
                                        (deliver suomifi-message-sent true))]
      (with-redefs [suomifi-viestit/send-message! assert-suomifi-message-sent]
        (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
              valvonta-id (valvonta-service/add-valvonta! ts/*db*
                                                          (-> {} (generators/complete valvonta-schema/ValvontaSave)
                                                              (merge {
                                                                      :rakennustunnus    "1035150826"
                                                                      :katuosoite        "katu"
                                                                      :postinumero       "65100"
                                                                      :valvoja-id        kayttaja-id
                                                                      :ilmoituspaikka-id nil
                                                                      :havaintopaiva     (LocalDate/of 2023 6 1)
                                                                      })))
              _ (valvonta-service/add-henkilo! ts/*db* valvonta-id (-> (generators/complete {} valvonta-schema/HenkiloSave)
                                                                       (merge {:henkilotunnus   (first (generators/unique-henkilotunnukset 1))
                                                                               :rooli-id        0 ; Omistaja
                                                                               :toimitustapa-id 0 ; Suomi-fi
                                                                               })
                                                                       ))
              body {:type-id       2
                    :deadline-date (str (LocalDate/of 2024 11 10))
                    :template-id   2
                    :description   "Tee jotain"
                    }
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body body)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]

          (t/is (= (:status response) 201))
          (t/is (= response-body {:id 1}))
          (t/is (deref suomifi-message-sent 5000 false))
          )))))

(t/deftest fetching-valvonta
  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
        valvonta-id (valvonta-service/add-valvonta!
                      ts/*db*
                      (-> {}
                          (generators/complete valvonta-schema/ValvontaSave)
                          (merge {
                                  :ilmoitustunnus             nil
                                  :rakennustunnus             "1035150826"
                                  :katuosoite                 "katu"
                                  :postinumero                "65100"
                                  :valvoja-id                 kayttaja-id
                                  :ilmoituspaikka-id          2
                                  :ilmoituspaikka-description "Netissä"
                                  :havaintopaiva              (LocalDate/of 2023 6 1)
                                  })))]
    (t/testing "Yksittäisen valvonnan hakeminen palauttaa vastauksen"
      (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s" valvonta-id))
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= response-body {:valvoja-id                 kayttaja-id
                                :katuosoite                 "katu"
                                :ilmoitustunnus             nil
                                :rakennustunnus             "1035150826"
                                :postinumero                "65100"
                                :id                         1
                                :ilmoituspaikka-description "Netissä"
                                :ilmoituspaikka-id          2
                                :havaintopaiva              "2023-06-01"}))))))

(t/deftest adding-and-fetching-valvonta
  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
        valvonta (-> {}
                     (generators/complete valvonta-schema/ValvontaSave)
                     (non-nil-key->string :havaintopaiva)   ; Jackson has problems encoding local date
                     (assoc :ilmoituspaikka-id 1)
                     (assoc :valvoja-id kayttaja-id))]
    (t/testing "Uuden valvonnan luominen"
      (let [response (ts/handler (-> (mock/request :post "/api/private/valvonta/kaytto")
                                     (mock/json-body valvonta)
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 201))
        (t/is (= response-body {:id 1}))))
    (t/testing "Luotu valvonta on tallennettu ja voidaan hakea"
      (let [fetch-response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/1"))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/json")))
            fetched-valvonta (j/read-value (:body fetch-response) j/keyword-keys-object-mapper)
            expected-valvonta (assoc valvonta :id 1)]
        (t/is (= (:status fetch-response) 200))
        (t/is (= fetched-valvonta expected-valvonta))))))

(t/deftest get-toimenpiteet-for-valvonta
  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
        ;; Create two valvontas
        valvonnat (repeatedly 2 #(-> {}
                                     (generators/complete valvonta-schema/ValvontaSave)
                                     (assoc :ilmoituspaikka-id 1
                                            :valvoja-id kayttaja-id)
                                     add-valvonta-and-map-id!))
        ;; Create 6 toimenpide for each valvonta
        toimenpiteet (->> (repeatedly 12 (fn [] (generators/complete {:type-id 1} valvonta-schema/ToimenpideAdd)))
                          (map vector (flatten (repeatedly (constantly [1 2]))))
                          (map (fn [[template-id toimenpide]] (assoc toimenpide :template-id template-id)))
                          (map vector (flatten (repeat (map :id valvonnat)))))]
    (doseq [[valvonta-id toimenpide] toimenpiteet]
      (add-toimenpide-and-map-id! valvonta-id toimenpide))
    (t/testing "Valvonnalle palautetaan 6 toimenpidettä"
      (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" (-> valvonnat first :id)))
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 6))))))


(t/deftest get-valvonnat-with-filters
  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)
        ;; Create two valvontas
        valvonnat (repeatedly 2 #(-> {}
                                     (generators/complete valvonta-schema/ValvontaSave)
                                     (assoc :ilmoituspaikka-id 1
                                            :valvoja-id kayttaja-id)
                                     add-valvonta-and-map-id!))
        ;; Create a toimenpide for each valvonta
        toimenpiteet (->> (repeatedly 2 (fn [] (generators/complete {:type-id 1} valvonta-schema/ToimenpideAdd)))
                          (map vector (flatten (repeatedly (constantly [1 2])))) ; Use two different templates for every other toimenpide
                          (map (fn [[template-id toimenpide]] (assoc toimenpide :template-id template-id)))
                          (map vector (flatten (repeat (map :id valvonnat))))
                          (mapv #(apply add-toimenpide-and-map-id! %)))]
    (t/testing "Hae valvontojen määrä joissa on käytetty asiakirjapohjaa 1"
      (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/kaytto/count")
                                     (mock/query-string {:asiakirjapohja-id 1})
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= response-body {:count 1}))))
    (t/testing "Hae valvonnat joissa on käytetty asiakirjapohjaa 1"
      (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/kaytto")
                                     (mock/query-string {:asiakirjapohja-id 1})
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)
            expected-valvonta (-> (first valvonnat)
                                  (assoc :henkilot []       ;Add synthetic fields
                                         :yritykset []
                                         :energiatodistus nil)
                                  (non-nil-key->string :havaintopaiva))
            expected-last-toimenpide (-> (first toimenpiteet)
                                         (dissoc :description) ; This is not sent here
                                         (assoc :diaarinumero nil) ; Would be generated by asha
                                         (non-nil-key->string :deadline-date))
            received-valvonta (first response-body)
            received-last-toimenpide (-> (:last-toimenpide received-valvonta)
                                         (dissoc :publish-time :create-time))]

        (t/is (= (:status response) 200))
        (t/is (= (count response-body) 1))
        (t/is (= (dissoc received-valvonta :last-toimenpide) expected-valvonta))
        (t/is (= received-last-toimenpide expected-last-toimenpide))))))
