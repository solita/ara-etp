(ns solita.etp.api.laatija-test
  (:require [clojure.test :as t]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.test-data.kayttaja :as test-data.kayttaja]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest retrieve-change-history-test
  (test-data.kayttaja/insert-virtu-paakayttaja! {:etunimi  "Asian"
                                                 :sukunimi "Tuntija"
                                                 :email    "testi@ara.fi"
                                                 :puhelin  "0504363675457"})
  (let [user (->> (test-data.laatija/generate-adds 1)
                  (first))
        user-id (first (test-data.laatija/insert! [user]))]
    (t/testing "Käyttäjä's change history can be fetched from the api after the user is created"
      (let [response (ts/handler (-> (mock/request :get (format "/api/private/kayttajat/%s/history" user-id))
                                     (test-data.kayttaja/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            body (-> response :body (j/read-value j/keyword-keys-object-mapper))]
        (t/is (= (:status response) 200))
        (t/is (= (count body)
                 1))
        (t/is (= (dissoc (first body) :modifytime)
                 (merge (select-keys user [:email :etunimi :henkilotunnus :puhelin :sukunimi :passivoitu])
                        {:cognitoid       nil
                         :id              2
                         :modifiedby-name "etp, database"
                         :organisaatio    ""
                         :rooli           0
                         :titteli-fi      nil
                         :titteli-sv      nil
                         :valvoja         false
                         :virtu           nil})))))
    (t/testing "Laatija's change history can be fetched from the api after the user is created"
      (let [response (ts/handler (-> (mock/request :get (format "/api/private/laatijat/%s/history" user-id))
                                     (test-data.kayttaja/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            body (-> response :body (j/read-value j/keyword-keys-object-mapper))]
        (t/is (= (:status response) 200))
        (t/is (= (count body)
                 1))
        (t/is (= (dissoc (first body) :modifytime :toteamispaivamaara)
                 (merge (select-keys user [:jakeluosoite :postinumero :postitoimipaikka :maa :patevyystaso :toteaja])
                        {:id                      2
                         :julkinenemail           false
                         :julkinenosoite          false
                         :julkinenpostinumero     false
                         :julkinenpuhelin         false
                         :julkinenwwwosoite       false
                         :laatimiskielto          false
                         :laskutuskieli           0
                         :modifiedby-name         "etp, database"
                         :muuttoimintaalueet      []
                         :partner                 false
                         :toimintaalue            nil
                         :vastaanottajan-tarkenne nil
                         :wwwosoite               nil})))))
    (t/testing "After changing user data"
      (let [update {:etunimi            "Uusi"
                    :sukunimi           "Nimi"
                    :toteamispaivamaara "2024-01-01"
                    :julkinenemail      true}
            update-response (ts/handler (-> (mock/request :put (format "/api/private/laatijat/%s" user-id))
                                            (mock/json-body (merge (dissoc user :toteamispaivamaara)
                                                                   update
                                                                   {:julkinenosoite          false
                                                                    :julkinenpostinumero     false
                                                                    :julkinenpuhelin         false
                                                                    :julkinenwwwosoite       false
                                                                    :laatimiskielto          false
                                                                    :laskutuskieli           0
                                                                    :muuttoimintaalueet      []
                                                                    :toimintaalue            nil
                                                                    :vastaanottajan-tarkenne nil
                                                                    :wwwosoite               nil
                                                                    :api-key                 nil}
                                                                   ))
                                            (test-data.kayttaja/with-virtu-user)
                                            (mock/header "Accept" "application/json")))]
        (t/is (= (:status update-response) 200))
        (t/testing "Changes in kayttaja data are reflected in the history"
          (let [response (ts/handler (-> (mock/request :get (format "/api/private/kayttajat/%s/history" user-id))
                                         (test-data.kayttaja/with-virtu-user)
                                         (mock/header "Accept" "application/json")))
                body (-> response :body (j/read-value j/keyword-keys-object-mapper))]
            (t/is (= (:status response) 200))
            (t/is (= (count body)
                     2))
            (t/is (= (dissoc (first body) :modifytime)
                     (merge (select-keys user [:email :etunimi :henkilotunnus :puhelin :sukunimi :passivoitu])
                            {:cognitoid       nil
                             :id              2
                             :modifiedby-name "etp, database"
                             :organisaatio    ""
                             :rooli           0
                             :titteli-fi      nil
                             :titteli-sv      nil
                             :valvoja         false
                             :virtu           nil})))
            (t/is (= (dissoc (last body) :modifytime)
                     (merge (select-keys user [:email :henkilotunnus :puhelin :passivoitu])
                            {:etunimi         "Uusi"
                             :sukunimi        "Nimi"
                             :cognitoid       nil
                             :id              2
                             :modifiedby-name "Tuntija, Asian"
                             :organisaatio    ""
                             :rooli           0
                             :titteli-fi      nil
                             :titteli-sv      nil
                             :valvoja         false
                             :virtu           nil})))))
        (t/testing "Changes in laatija data are reflected in the history"
          (let [response (ts/handler (-> (mock/request :get (format "/api/private/laatijat/%s/history" user-id))
                                         (test-data.kayttaja/with-virtu-user)
                                         (mock/header "Accept" "application/json")))
                body (-> response :body (j/read-value j/keyword-keys-object-mapper))]
            (t/is (= (:status response) 200))
            (t/is (= (count body)
                     2))
            (t/is (= (dissoc (first body) :modifytime :toteamispaivamaara)
                     (merge (select-keys user [:jakeluosoite :postinumero :postitoimipaikka :maa :patevyystaso :toteaja])
                            {:id                      2
                             :julkinenemail           false
                             :julkinenosoite          false
                             :julkinenpostinumero     false
                             :julkinenpuhelin         false
                             :julkinenwwwosoite       false
                             :laatimiskielto          false
                             :laskutuskieli           0
                             :modifiedby-name         "etp, database"
                             :muuttoimintaalueet      []
                             :partner                 false
                             :toimintaalue            nil
                             :vastaanottajan-tarkenne nil
                             :wwwosoite               nil})))
            (t/is (= (dissoc (last body) :modifytime :toteamispaivamaara)
                     (merge (select-keys user [:jakeluosoite :postinumero :postitoimipaikka :maa :patevyystaso :toteaja])
                            {:id                      2
                             :julkinenemail           true
                             :julkinenosoite          false
                             :julkinenpostinumero     false
                             :julkinenpuhelin         false
                             :julkinenwwwosoite       false
                             :laatimiskielto          false
                             :laskutuskieli           0
                             :modifiedby-name         "Tuntija, Asian"
                             :muuttoimintaalueet      []
                             :partner                 false
                             :toimintaalue            nil
                             :vastaanottajan-tarkenne nil
                             :wwwosoite               nil})))))))))
