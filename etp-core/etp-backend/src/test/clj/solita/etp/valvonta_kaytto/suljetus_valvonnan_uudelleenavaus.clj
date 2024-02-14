(ns solita.etp.valvonta-kaytto.suljetus-valvonnan-uudelleenavaus
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.service.valvonta-kaytto :as valvonta-kaytto]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(t/deftest suljetun-valvonnan-uudelleenavaus-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi    "Asian"
     :sukunimi   "Tuntija"
     :email      "testi@ara.fi"
     :puhelin    "0504363675457"
     :titteli-fi "energia-asiantuntija"
     :titteli-sv "energiexpert"})

  (t/testing "Creating Suljetun valvonnan uudelleenavaus toimenpide marks the valvonta as open"
    (let [valvonta-id (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                              :postinumero       "90100"
                                                              :ilmoituspaikka-id 0})]

      (t/testing "Newly created valvonta is open"
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* {:include-closed false}))
                 1)))

      (t/testing "Creating valvonnan lopetus toimenpide marks valvonta as closed"
        (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                              :type_id       5
                                              :create_time   (-> (LocalDate/of 2024 02 14)
                                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                                 .toInstant)
                                              :publish_time  (-> (LocalDate/of 2024 02 14)
                                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                                 .toInstant)
                                              :deadline_date (LocalDate/of 2024 02 14)})

        ;; Valvonta is found as closed
        (t/is (zero? (count (valvonta-kaytto/find-valvonnat ts/*db* {:include-closed false}))))
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* {:include-closed true}))
                 1)))

      (t/testing "Creating Suljetun valvonnan uudelleenavaus toimenpide succeeds"
        (with-bindings {#'time/clock (Clock/fixed (-> (LocalDate/of 2024 02 15)
                                                      (.atStartOfDay time/timezone)
                                                      .toInstant)
                                                  time/timezone)}
          (let [new-toimenpide {:type-id       22
                                :deadline-date (str (LocalDate/of 2024 02 15))
                                :template-id   nil
                                :description   "Suljettiin valvonta vahingossa, avataan uudelleen"}
                response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                         (mock/json-body new-toimenpide)
                                         (test-kayttajat/with-virtu-user)
                                         (mock/header "Accept" "application/json")))]
            (t/is (= (:status response) 201))

            (t/testing "Valvonta is open after suljetun valvonnan uudelleenavaus is created"
              (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* {:include-closed false}))
                       1))
              (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* {:include-closed true}))
                       1))))

          (t/testing "Toimenpide is returned through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/json")))
                  response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
              (t/is (= (:status response) 200))
              (t/is (= (count response-body) 2))
              (t/is (= (->> response-body
                            (map #(dissoc % :publish-time :create-time))
                            last)
                       {:author             {:etunimi  "Asian"
                                             :id       1
                                             :rooli-id 2
                                             :sukunimi "Tuntija"}
                        :deadline-date      "2024-02-15"
                        :description        "Suljettiin valvonta vahingossa, avataan uudelleen"
                        :diaarinumero       nil
                        :filename           nil
                        :henkilot           []
                        :id                 2
                        :template-id        nil
                        :type-id            22
                        :type-specific-data nil
                        :valvonta-id        1
                        :yritykset          []})))))))))
