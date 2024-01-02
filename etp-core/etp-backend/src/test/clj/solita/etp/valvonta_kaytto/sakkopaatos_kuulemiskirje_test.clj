(ns solita.etp.valvonta-kaytto.sakkopaatos-kuulemiskirje-test
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :refer [html->pdf-with-assertion]]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(t/deftest sakkopaatos-kuulemiskirje-test
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Preview api call for sakkopäätös / kuulemiskirje toimenpide succeeds"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db*
                                                      {:katuosoite        "Testitie 5"
                                                       :postinumero       "90100"
                                                       :ilmoituspaikka-id 0})
          osapuoli-id (valvonta-service/add-henkilo! ts/*db*
                                                     valvonta-id
                                                     {:toimitustapa-description nil
                                                      :toimitustapa-id          0
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
                                                      :maa                      "FI"})
          new-toimenpide {:type-id            14
                          :deadline-date      (str (LocalDate/of 2023 11 4))
                          :template-id        7
                          :description        "Tehdään sakkopäätöksen kuulemiskirje"
                          :type-specific-data {:fine                   9000
                                               :osapuoli-specific-data [{:osapuoli {:id   osapuoli-id
                                                                                    :type "henkilo"}
                                                                         :document true}]}}
          response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet/henkilot/%s/preview" valvonta-id osapuoli-id))
                                   (mock/json-body new-toimenpide)
                                   (test-kayttajat/with-virtu-user)
                                   (mock/header "Accept" "application/json")))]
      (t/is (= (:status response) 200))))

  (t/testing "Sakkopäätös / kuulemiskirje toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
    ;; Add the valvonta and previous toimenpides
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)
          kuulemiskirje-timestamp (-> (LocalDate/of 2023 8 13)
                                      (.atStartOfDay (ZoneId/systemDefault))
                                      .toInstant)
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 9 13)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
          osapuoli-id (valvonta-service/add-henkilo!
                        ts/*db*
                        valvonta-id
                        {:toimitustapa-description nil
                         :toimitustapa-id          0
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
      ;; Add kehotus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   kehotus-timestamp
                                            :publish_time  kehotus-timestamp
                                            :deadline_date (LocalDate/of 2023 7 12)})
      ;; Add varoitus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       3
                                            :create_time   varoitus-timestamp
                                            :publish_time  varoitus-timestamp
                                            :deadline_date (LocalDate/of 2023 8 13)})

      ;; Add käskypäätös / kuulemiskirje toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            7
                                            :create_time        kuulemiskirje-timestamp
                                            :publish_time       kuulemiskirje-timestamp
                                            :deadline_date      (LocalDate/of 2023 8 27)
                                            :type_specific_data {:fine 9000}
                                            :diaarinumero       "ARA-05.03.01-2023-159"})
      ;; Add käskypäätös / varsinainen päätös toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            8
                                            :create_time        varsinainen-paatos-timestamp
                                            :publish_time       varsinainen-paatos-timestamp
                                            :deadline_date      (LocalDate/of 2023 10 4)
                                            :template_id        6
                                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                                            :type_specific_data {:fine                     857
                                                                 :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                                    :type "henkilo"}
                                                                                             :hallinto-oikeus-id   1
                                                                                             :document             true
                                                                                             :recipient-answered   true
                                                                                             :answer-commentary-fi "En tiennyt, että todistus tarvitaan :("
                                                                                             :answer-commentary-sv "Jag visste inte att ett intyg behövs :("
                                                                                             :statement-fi         "Tämän kerran annetaan anteeksi, kun hän ei tiennyt."
                                                                                             :statement-sv         "Han vet inte. Vi förlotar."}]
                                                                 :department-head-title-fi "Apulaisjohtaja"
                                                                 :department-head-title-sv "Apulaisjohtaja på svenska"
                                                                 :department-head-name     "Yli Päällikkö"}})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 10 5)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/sakkopaatos-kuulemiskirje-yksityishenkilo.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            14
                              :deadline-date      (str (LocalDate/of 2023 11 4))
                              :template-id        7
                              :description        "Tehdään sakkopäätöksen kuulemiskirje"
                              :type-specific-data {:fine                   9000
                                                   :osapuoli-specific-data [{:osapuoli {:id   osapuoli-id
                                                                                        :type "henkilo"}
                                                                             :document true}]}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (= (:status response) 201))))

      (t/testing "Toimenpide is returned through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= (count response-body) 5))

          (t/is (= (->> response-body
                        (map #(dissoc % :publish-time :create-time))
                        last)
                   {:author             {:etunimi  "Asian"
                                         :id       1
                                         :rooli-id 2
                                         :sukunimi "Tuntija"}
                    :deadline-date      "2023-11-04"
                    :description        "Tehdään sakkopäätöksen kuulemiskirje"
                    :diaarinumero       "ARA-05.03.01-2023-159"
                    :filename           "sakkopaatos-kuulemiskirje.pdf"
                    :henkilot           [{:email                    nil
                                          :etunimi                  "Testi"
                                          :henkilotunnus            "000000-0000"
                                          :id                       2
                                          :jakeluosoite             "Testikatu 12"
                                          :maa                      "FI"
                                          :postinumero              "00100"
                                          :postitoimipaikka         "Helsinki"
                                          :puhelin                  nil
                                          :rooli-description        "Omistaja"
                                          :rooli-id                 0
                                          :sukunimi                 "Talonomistaja"
                                          :toimitustapa-description nil
                                          :toimitustapa-id          0
                                          :valvonta-id              2
                                          :vastaanottajan-tarkenne  nil}]
                    :id                 5
                    :template-id        7
                    :type-id            14
                    :type-specific-data {:fine                   9000
                                         :osapuoli-specific-data [{:document true
                                                                   :osapuoli {:id   2
                                                                              :type "henkilo"}}]}
                    :valvonta-id        2
                    :yritykset          []}))))))

  (t/testing "Sakkopäätös / kuulemiskirje toimenpide is created successfully but no document is generated as it's marked false for the osapuoli"
    ;; Add the valvonta and previous toimenpides
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)
          kuulemiskirje-timestamp (-> (LocalDate/of 2023 8 13)
                                      (.atStartOfDay (ZoneId/systemDefault))
                                      .toInstant)
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 9 13)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
          osapuoli-id (valvonta-service/add-henkilo!
                        ts/*db*
                        valvonta-id
                        {:toimitustapa-description nil
                         :toimitustapa-id          0
                         :email                    nil
                         :rooli-id                 0
                         :jakeluosoite             "Testikatu 12"
                         :postitoimipaikka         "Helsinki"
                         :puhelin                  nil
                         :sukunimi                 "Talonomistaja"
                         :postinumero              "00100"
                         :henkilotunnus            "000000-0000"
                         :rooli-description        ""
                         :etunimi                  "Testi"
                         :vastaanottajan-tarkenne  nil
                         :maa                      "FI"})]
      ;; Add kehotus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   kehotus-timestamp
                                            :publish_time  kehotus-timestamp
                                            :deadline_date (LocalDate/of 2023 7 12)})
      ;; Add varoitus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       3
                                            :create_time   varoitus-timestamp
                                            :publish_time  varoitus-timestamp
                                            :deadline_date (LocalDate/of 2023 8 13)})

      ;; Add käskypäätös / kuulemiskirje toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            7
                                            :create_time        kuulemiskirje-timestamp
                                            :publish_time       kuulemiskirje-timestamp
                                            :deadline_date      (LocalDate/of 2023 8 27)
                                            :type_specific_data {:fine 9000}
                                            :diaarinumero       "ARA-05.03.01-2023-159"})
      ;; Add käskypäätös / varsinainen päätös toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            8
                                            :create_time        varsinainen-paatos-timestamp
                                            :publish_time       varsinainen-paatos-timestamp
                                            :deadline_date      (LocalDate/of 2023 10 4)
                                            :template_id        6
                                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                                            :type_specific_data {:fine                     857
                                                                 :recipient-answered       true
                                                                 :answer-commentary-fi     "En tiennyt, että todistus tarvitaan :("
                                                                 :answer-commentary-sv     "Jag visste inte att ett intyg behövs :("
                                                                 :statement-fi             "Tämän kerran annetaan anteeksi, kun hän ei tiennyt."
                                                                 :statement-sv             "Han vet inte. Vi förlotar."
                                                                 :osapuoli-specific-data   [{:osapuoli           {:id   osapuoli-id
                                                                                                                  :type "henkilo"}
                                                                                             :hallinto-oikeus-id 1
                                                                                             :document           true}]
                                                                 :department-head-title-fi "Apulaisjohtaja"
                                                                 :department-head-title-sv "Apulaisjohtaja på svenska"
                                                                 :department-head-name     "Yli Päällikkö"}})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 10 5)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (fn [_ _]
                                        (reset! html->pdf-called? true))}
        (let [new-toimenpide {:type-id            14
                              :deadline-date      (str (LocalDate/of 2023 11 4))
                              :template-id        7
                              :description        "Tehdään sakkopäätöksen kuulemiskirje"
                              :type-specific-data {:fine                   9000
                                                   :osapuoli-specific-data [{:osapuoli {:id   osapuoli-id
                                                                                        :type "henkilo"}
                                                                             :document false}]}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (false? @html->pdf-called?))
          (t/is (= (:status response) 201)))))))
