(ns solita.etp.valvonta-kaytto.sakkopaatos-varsinainen-paatos-test
  (:require
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :refer [html->pdf-with-assertion]]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.service.valvonta-kaytto.store :as file-store]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(def original-store-hallinto-oikeus-attachment file-store/store-hallinto-oikeus-attachment!)

(t/deftest sakkopaatos-varsinainen-paatos-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Sakkopäätös / varsinainen päätös toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
    ;; Add the valvonta and previous toimenpides
    ;; so that käskypäätös / kuulemiskirje toimenpide can be created
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)
          kuulemiskirje-timestamp (-> (LocalDate/of 2023 7 13)
                                      (.atStartOfDay (ZoneId/systemDefault))
                                      .toInstant)
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 10 13)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)
          sakkopaatos-kuulemiskirje-timestamp (-> (LocalDate/of 2023 11 1)
                                                  (.atStartOfDay (ZoneId/systemDefault))
                                                  .toInstant)
          html->pdf-called? (atom false)
          store-hallinto-oikeus-attachment-called? (atom false)
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
                                            :deadline_date      (LocalDate/of 2023 10 24)
                                            :template_id        6
                                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                                            :diaarinumero       "ARA-05.03.01-2023-159"
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

      ;; Add sakkopäätös / kuulemiskirje toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            14
                                            :create_time        sakkopaatos-kuulemiskirje-timestamp
                                            :publish_time       sakkopaatos-kuulemiskirje-timestamp
                                            :deadline_date      (LocalDate/of 2023 11 4)
                                            :template_id        7
                                            :description        "Tehdään sakkopäätöksen kuulemiskirje"
                                            :diaarinumero       "ARA-05.03.01-2023-159"
                                            :type_specific_data {:fine                   9000
                                                                 :osapuoli-specific-data [{:osapuoli {:id   osapuoli-id
                                                                                                      :type "henkilo"}
                                                                                           :document true}]}})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 11 25)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/sakkopaatos-varsinainen-paatos-yksityishenkilo.html"
                                               html->pdf-called?)
                      #'file-store/store-hallinto-oikeus-attachment!
                      (fn [aws-s3-client valvonta-id toimenpide-id osapuoli document]
                        (reset! store-hallinto-oikeus-attachment-called? true)
                        (original-store-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id osapuoli document))}
        (let [new-toimenpide {:type-id            15
                              :deadline-date      (str (LocalDate/of 2023 12 10))
                              :template-id        9
                              :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                              :type-specific-data {:fine                     8572
                                                   :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                      :type "henkilo"}
                                                                               :hallinto-oikeus-id   3
                                                                               :document             true
                                                                               :recipient-answered   true
                                                                               :answer-commentary-fi "En tiennyt, että todistus tarvitaan :("
                                                                               :answer-commentary-sv "Jag visste inte att ett intyg behövs :("
                                                                               :statement-fi         "ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä."
                                                                               :statement-sv         "Han vet inte. Vi förlotar."}]
                                                   :department-head-title-fi "Apulaisjohtaja"
                                                   :department-head-title-sv "Apulaisjohtaja på svenska"
                                                   :department-head-name     "Yli Päällikkö"}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (true? @store-hallinto-oikeus-attachment-called?))
          (t/is (= (:status response) 201))

          (t/testing "Toimenpide is returned through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/json")))
                  response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
              (t/is (= (:status response) 200))
              (t/is (= (count response-body) 6))
              (t/is (contains? (->> response-body
                                    (map #(dissoc % :publish-time :create-time))
                                    set)
                               {:description
                                "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                                :henkilot
                                [{:toimitustapa-description nil
                                  :toimitustapa-id          0
                                  :email                    nil
                                  :rooli-id                 0
                                  :jakeluosoite             "Testikatu 12"
                                  :valvonta-id              1
                                  :postitoimipaikka         "Helsinki"
                                  :puhelin                  nil
                                  :sukunimi                 "Talonomistaja"
                                  :postinumero              "00100"
                                  :id                       1
                                  :henkilotunnus            "000000-0000"
                                  :rooli-description        "Omistaja"
                                  :etunimi                  "Testi"
                                  :vastaanottajan-tarkenne  nil
                                  :maa                      "FI"}]
                                :yritykset     []
                                :type-id       15
                                :valvonta-id   1
                                :author        {:rooli-id 2 :sukunimi "Tuntija", :id 1 :etunimi "Asian"}
                                :filename      "sakkopaatos.pdf"
                                :diaarinumero  "ARA-05.03.01-2023-159"
                                :id            6
                                :deadline-date "2023-12-10"
                                :type-specific-data
                                {:department-head-title-fi "Apulaisjohtaja"
                                 :department-head-name     "Yli Päällikkö"
                                 :osapuoli-specific-data
                                 [{:hallinto-oikeus-id   3
                                   :osapuoli             {:id   1
                                                          :type "henkilo"}
                                   :recipient-answered   true
                                   :document             true
                                   :statement-sv         "Han vet inte. Vi förlotar."
                                   :statement-fi
                                   "ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä."
                                   :answer-commentary-sv "Jag visste inte att ett intyg behövs :("
                                   :answer-commentary-fi "En tiennyt, että todistus tarvitaan :("}]
                                 :department-head-title-sv "Apulaisjohtaja på svenska"
                                 :fine                     8572}
                                :template-id   9}))))

          (t/testing "Created document can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kaskypaatos.pdf" valvonta-id 6 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))))

          (t/testing "hallinto-oikeus-liite can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/attachment/hallinto-oikeus.pdf" valvonta-id 6 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))

              (t/testing "hallinto-oikeus-liite is the correct one"
                (t/is (= (slurp (io/input-stream (io/resource "pdf/hallinto-oikeudet/Valitusosoitus_30_pv_POHJOIS-SUOMEN_HAO.pdf")))
                         (slurp (:body response)))))))))))

  (t/testing "Sakkopäätös / varsinainen päätös toimenpide is created successfully for yritys and document is generated with correct information"
    ;; Add the valvonta and previous toimenpides
    ;; so that käskypäätös / kuulemiskirje toimenpide can be created
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)
          kuulemiskirje-timestamp (-> (LocalDate/of 2023 7 13)
                                      (.atStartOfDay (ZoneId/systemDefault))
                                      .toInstant)
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 10 15)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)
          sakkopaatos-kuulemiskirje-timestamp (-> (LocalDate/of 2023 11 6)
                                                  (.atStartOfDay (ZoneId/systemDefault))
                                                  .toInstant)
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
          osapuoli-id (valvonta-service/add-yritys!
                        ts/*db*
                        valvonta-id
                        {:nimi                     "Yritysomistaja"
                         :toimitustapa-description nil
                         :toimitustapa-id          0
                         :email                    nil
                         :rooli-id                 0
                         :jakeluosoite             "Testikatu 12"
                         :vastaanottajan-tarkenne  "Lisäselite C/O"
                         :postitoimipaikka         "Helsinki"
                         :puhelin                  nil
                         :postinumero              "00100"
                         :rooli-description        "Omistaja"
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
                                            :diaarinumero       "ARA-05.03.01-2023-132"
                                            :type_specific_data {:fine 9000}})
      ;; Add käskypäätös / varsinainen päätös toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            8
                                            :create_time        varsinainen-paatos-timestamp
                                            :publish_time       varsinainen-paatos-timestamp
                                            :deadline_date      (LocalDate/of 2023 10 24)
                                            :template_id        6
                                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                                            :diaarinumero       "ARA-05.03.01-2023-159"
                                            :type_specific_data {:fine                     857
                                                                 :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                                    :type "yritys"}
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

      ;; Add sakkopäätös / kuulemiskirje toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            14
                                            :create_time        sakkopaatos-kuulemiskirje-timestamp
                                            :publish_time       sakkopaatos-kuulemiskirje-timestamp
                                            :deadline_date      (LocalDate/of 2023 11 4)
                                            :template_id        7
                                            :description        "Tehdään sakkopäätöksen kuulemiskirje"
                                            :diaarinumero       "ARA-05.03.01-2023-159"
                                            :type_specific_data {:fine 932
                                                                 :osapuoli-specific-data [{:osapuoli {:id   osapuoli-id
                                                                                                      :type "yritys"}
                                                                                           :document true}]}})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 11 28)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/sakkopaatos-varsinainen-paatos-yritys.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            15
                              :deadline-date      (str (LocalDate/of 2023 10 4))
                              :template-id        9
                              :description        "Tehdään varsinainen päätös, omistaja ei vastannut kuulemiskirjeeseen"
                              :type-specific-data {:fine                     857
                                                   :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                      :type "yritys"}
                                                                               :hallinto-oikeus-id   4
                                                                               :statement-fi         "ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä."
                                                                               :statement-sv         "Placeholder"
                                                                               :answer-commentary-fi "Asianosainen ei vastannut."
                                                                               :answer-commentary-sv "Nej ansverat han."
                                                                               :document             true
                                                                               :recipient-answered   false}]
                                                   :department-head-title-fi "Senior Vice President"
                                                   :department-head-title-sv "Kungen"
                                                   :department-head-name     "Jane Doe"}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (println (j/read-value (:body response) j/keyword-keys-object-mapper))
          (t/is (true? @html->pdf-called?))
          (t/is (= (:status response) 201))

          (t/testing "Toimenpide is returned through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/json")))
                  response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
              (t/is (= (:status response) 200))
              (t/is (= (count response-body) 6))

              (t/is (= (->> response-body
                            (map #(dissoc % :publish-time :create-time))
                            last)
                       {:description
                        "Tehdään varsinainen päätös, omistaja ei vastannut kuulemiskirjeeseen",
                        :henkilot      [],
                        :yritykset
                        [{:toimitustapa-description nil,
                          :toimitustapa-id          0,
                          :email                    nil,
                          :rooli-id                 0,
                          :jakeluosoite             "Testikatu 12",
                          :valvonta-id              2,
                          :postitoimipaikka         "Helsinki",
                          :ytunnus                  nil,
                          :puhelin                  nil,
                          :nimi                     "Yritysomistaja",
                          :postinumero              "00100",
                          :id                       1,
                          :rooli-description        "Omistaja",
                          :vastaanottajan-tarkenne  "Lisäselite C/O",
                          :maa                      "FI"}],
                        :type-id       15,
                        :valvonta-id   2,
                        :author        {:rooli-id 2, :sukunimi "Tuntija", :id 1, :etunimi "Asian"},
                        :filename      "sakkopaatos.pdf",
                        :diaarinumero  "ARA-05.03.01-2023-159",
                        :id            12,
                        :deadline-date "2023-10-04",
                        :type-specific-data
                        {:department-head-title-fi "Senior Vice President",
                         :department-head-name     "Jane Doe",
                         :osapuoli-specific-data
                         [{:hallinto-oikeus-id   4,
                           :osapuoli             {:id   1,
                                                  :type "yritys"},
                           :recipient-answered   false,
                           :document             true,
                           :statement-sv         "Placeholder",
                           :statement-fi
                           "ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä."
                           :answer-commentary-fi "Asianosainen ei vastannut."
                           :answer-commentary-sv "Nej ansverat han."}],
                         :department-head-title-sv "Kungen",
                         :fine                     857},
                        :template-id   9}))))))))

  (t/testing "Preview api call for Sakkopäätös / varsinainen päätös toimenpide succeeds"
    (t/testing "for yksityishenkilö"
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
                                                        :rooli-description        ""
                                                        :etunimi                  "Testi"
                                                        :vastaanottajan-tarkenne  nil
                                                        :maa                      "FI"})
            new-toimenpide {:type-id            15
                            :deadline-date      (str (LocalDate/of 2023 10 4))
                            :template-id        9
                            :description        "Tehdään varsinainen päätös, omistaja ei vastannut kuulemiskirjeeseen"
                            :type-specific-data {:fine                     857
                                                 :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                    :type "henkilo"}
                                                                             :hallinto-oikeus-id   3
                                                                             :document             true
                                                                             :recipient-answered   false
                                                                             :statement-fi         "Päätöstä ei muuteta."
                                                                             :statement-sv         "Beslutet kommer inte att ändras."
                                                                             :answer-commentary-fi "Vastausta ei annettu"
                                                                             :answer-commentary-sv "Svar gavs inte"}]
                                                 :department-head-title-fi "Johtaja"
                                                 :department-head-title-sv "Ledar"
                                                 :department-head-name     "Nimi Muutettu"}}
            response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet/henkilot/%s/preview" valvonta-id osapuoli-id))
                                     (mock/json-body new-toimenpide)
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))

    (t/testing "for yritysomistaja"
      (let [valvonta-id (valvonta-service/add-valvonta! ts/*db*
                                                        {:katuosoite        "Testitie 5"
                                                         :postinumero       "90100"
                                                         :ilmoituspaikka-id 0})
            osapuoli-id (valvonta-service/add-yritys! ts/*db*
                                                      valvonta-id
                                                      {:nimi                     "Yritysomistaja"
                                                       :toimitustapa-description nil
                                                       :toimitustapa-id          0
                                                       :email                    nil
                                                       :rooli-id                 0
                                                       :jakeluosoite             "Testikatu 12"
                                                       :vastaanottajan-tarkenne  "Lisäselite C/O"
                                                       :postitoimipaikka         "Helsinki"
                                                       :puhelin                  nil
                                                       :postinumero              "00100"
                                                       :rooli-description        ""
                                                       :maa                      "FI"})
            new-toimenpide {:type-id            15
                            :deadline-date      (str (LocalDate/of 2023 10 4))
                            :template-id        9
                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                            :type-specific-data {:fine                     857
                                                 :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                    :type "yritys"}
                                                                             :hallinto-oikeus-id   5
                                                                             :document             true
                                                                             :recipient-answered   true
                                                                             :answer-commentary-fi "Yritykseni on niin iso, ettei minun tarvitse välittää tällaisista asioista"
                                                                             :answer-commentary-sv "Mitt företag är så stort att jag inte behöver bry mig om sådana saker"
                                                                             :statement-fi         "Vastaus oli väärä, joten saat isot sakot."
                                                                             :statement-sv         "Svaret var fel, så du får stora böter."}]
                                                 :department-head-title-fi "Titteli"
                                                 :department-head-title-sv "Tittel"
                                                 :department-head-name     "Nimi"}}
            response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet/yritykset/%s/preview" valvonta-id osapuoli-id))
                                     (mock/json-body new-toimenpide)
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))]
        (t/is (= (:status response) 200))))))
