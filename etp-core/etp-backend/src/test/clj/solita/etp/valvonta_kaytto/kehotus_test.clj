(ns solita.etp.valvonta-kaytto.kehotus-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :as doc]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate)))

(t/use-fixtures :each ts/fixture)

(t/deftest kehotus-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi    "Asian"
     :sukunimi   "Tuntija"
     :email      "testi@ara.fi"
     :puhelin    "0504363675457"
     :titteli-fi "energia-asiantuntija"
     :titteli-sv "energiexpert"})
  (t/testing "Kehotus toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
    ;; Add the valvonta and previous toimenpides
    ;; so that käskypäätös / kuulemiskirje toimenpide can be created
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :havaintopaiva     (LocalDate/of 2023 7 22)
                                                               :ilmoituspaikka-id 0
                                                               :ilmoitustunnus    "ASF5"})
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
          osapuoli-id (valvonta-service/add-henkilo! ts/*db*
                                                     valvonta-id
                                                     {:toimitustapa-description nil
                                                      :toimitustapa-id          2
                                                      :email                    nil
                                                      :rooli-id                 0
                                                      :jakeluosoite             "Testikatu 12"
                                                      :postitoimipaikka         "Helsinki"
                                                      :puhelin                  nil
                                                      :sukunimi                 "Talonomistaja"
                                                      :postinumero              "00100"
                                                      :henkilotunnus            "000000-0000"
                                                      :rooli-description        "Omistaja"
                                                      :etunimi                  "Testi"
                                                      :vastaanottajan-tarkenne  nil
                                                      :maa                      "FI"})]

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial doc/html->pdf-with-assertion
                                               "documents/kehotus.html"
                                               html->pdf-called?)
                      }
        (let [new-toimenpide {:type-id       2
                              :deadline-date (str (LocalDate/of 2023 7 22))
                              :template-id   3
                              :description   "Lähetetään kehotus"}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (= (:status response) 201))

          (t/testing "Toimenpide is returned through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/json")))
                  response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
              (t/is (= (:status response) 200))
              (t/is (= (count response-body) 1))
              (t/is (= (-> response-body
                           last
                           (dissoc :publish-time :create-time))
                       {:author             {:etunimi  "Asian"
                                             :id       1
                                             :rooli-id 2
                                             :sukunimi "Tuntija"}
                        :deadline-date      "2023-07-22"
                        :description        "Lähetetään kehotus"
                        :diaarinumero       nil
                        :filename           "kehotus.pdf"
                        :henkilot           [{:email                    nil
                                              :etunimi                  "Testi"
                                              :henkilotunnus            "000000-0000"
                                              :id                       1
                                              :jakeluosoite             "Testikatu 12"
                                              :maa                      "FI"
                                              :postinumero              "00100"
                                              :postitoimipaikka         "Helsinki"
                                              :puhelin                  nil
                                              :rooli-description        "Omistaja"
                                              :rooli-id                 0
                                              :sukunimi                 "Talonomistaja"
                                              :toimitustapa-description nil
                                              :toimitustapa-id          2
                                              :valvonta-id              1
                                              :vastaanottajan-tarkenne  nil}]
                        :id                 1
                        :template-id        3
                        :type-id            2
                        :type-specific-data nil
                        :valvonta-id        1
                        :yritykset          []}))))

          (t/testing "Created document can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kehotus.pdf" valvonta-id 1 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))
                  pdf-document (doc/read-pdf (:body response))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))

              (t/testing "and document has two pages"
                (t/is (= (.getNumberOfPages pdf-document)
                         2)))

              (t/testing "and document looks as it should"
                (doc/assert-pdf-matches-visually pdf-document "documents/kehotus.pdf")))))))))