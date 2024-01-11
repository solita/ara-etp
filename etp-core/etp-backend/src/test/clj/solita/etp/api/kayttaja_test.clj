(ns solita.etp.api.kayttaja-test
  (:require [clojure.test :as t]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.test-data.kayttaja :as test-kayttaja]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest kayttaja-title-test
  (let [user-id (test-kayttaja/insert-virtu-paakayttaja!
                  {:etunimi  "Asian"
                   :sukunimi "Tuntija"
                   :email    "testi@ara.fi"
                   :puhelin  "0504363675457"})]
    (t/testing "User can be fetched from the api after being created"
      (let [response (ts/handler (-> (mock/request :get (format "/api/private/kayttajat/%s" user-id))
                                     (test-kayttaja/with-virtu-user)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response)
                 200))

        (t/is (= (-> response :body (j/read-value j/keyword-keys-object-mapper))
                 {:titteli-fi    nil
                  :titteli-sv    nil
                  :etunimi       "Asian"
                  :sukunimi      "Tuntija"
                  :email         "testi@ara.fi"
                  :puhelin       "0504363675457"
                  :cognitoid     nil
                  :henkilotunnus nil
                  :id            1
                  :login         nil
                  :organisaatio  ""
                  :passivoitu    false
                  :rooli         2
                  :valvoja       false
                  :verifytime    nil
                  :virtu         {:localid      "vvirkamies"
                                  :organisaatio "testivirasto.fi"}}))))

    (t/testing "Setting the title fields in user update request succeeds"
      (let [user-update {:titteli-fi    "Energia-asiantuntija"
                         :titteli-sv    "Energiexpert"
                         :valvoja       false
                         :email         "testi@ara.fi"
                         :puhelin       "0504363675457"
                         :sukunimi      "Tuntija"
                         :virtu         {:localid      "vvirkamies"
                                         :organisaatio "testivirasto.fi"}
                         :rooli         2
                         :henkilotunnus nil
                         :passivoitu    false
                         :etunimi       "Asian"
                         :organisaatio  ""
                         :api-key       nil}
            response (ts/handler (-> (mock/request :put (format "/api/private/kayttajat/%s" user-id))
                                     (mock/json-body user-update)
                                     (test-kayttaja/with-virtu-user)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response)
                 200))

        (t/testing "and the title and pre-existing fields are present in the returned user"
          (let [user (-> (mock/request :get (format "/api/private/kayttajat/%s" user-id))
                         (test-kayttaja/with-virtu-user)
                         (mock/header "Accept" "application/json")
                         ts/handler
                         :body
                         (j/read-value j/keyword-keys-object-mapper))]
            (t/is (= user
                     {:titteli-fi    "Energia-asiantuntija"
                      :titteli-sv    "Energiexpert"
                      :etunimi       "Asian"
                      :sukunimi      "Tuntija"
                      :email         "testi@ara.fi"
                      :puhelin       "0504363675457"
                      :cognitoid     nil
                      :henkilotunnus nil
                      :id            1
                      :login         nil
                      :organisaatio  ""
                      :passivoitu    false
                      :rooli         2
                      :valvoja       false
                      :verifytime    nil
                      :virtu         {:localid      "vvirkamies"
                                      :organisaatio "testivirasto.fi"}}))))))))

(t/deftest retrieve-users-test
  (test-kayttaja/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Users can be retrieved through the api"
    ;; Create users, together with the pääkäyttäjä there are 200 users
    (test-kayttaja/generate-and-insert! 199)
    (let [response (ts/handler (-> (mock/request :get "/api/private/kayttajat")
                          (test-kayttaja/with-virtu-user)
                          (mock/header "Accept" "application/json")))]
      (t/is (= (:status response) 200))
      (t/is (= (count (-> response :body (j/read-value j/keyword-keys-object-mapper)))
               200)))))