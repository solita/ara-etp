(ns solita.etp.valvonta-kaytto.kaskypaatos-kuulemiskirje-test
  (:require
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :as doc]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(def original-html->pdf pdf/html->pdf)

(t/deftest kaskypaatos-kuulemiskirje-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi    "Asian"
     :sukunimi   "Tuntija"
     :email      "testi@ara.fi"
     :puhelin    "0504363675457"
     :titteli-fi "energia-asiantuntija"
     :titteli-sv "energiexpert"})
  (t/testing "Käskypäätös / Kuulemiskirje toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
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
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
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

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial doc/html->pdf-with-assertion
                                               "documents/kaskypaatoskuulemiskirje-yksityishenkilo.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            7
                              :deadline-date      (str (LocalDate/of 2023 7 22))
                              :template-id        5
                              :description        "Lähetetään kuulemiskirje, kun myyjä ei ole hankkinut energiatodistusta eikä vastannut kehotukseen tai varoitukseen"
                              :type-specific-data {:fine 800}}
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
              (t/is (= (count response-body) 3))
              (t/is (= (-> response-body
                           last
                           (dissoc :publish-time :create-time))
                       {:author             {:etunimi  "Asian"
                                             :id       1
                                             :rooli-id 2
                                             :sukunimi "Tuntija"}
                        :deadline-date      "2023-07-22"
                        :description        "Lähetetään kuulemiskirje, kun myyjä ei ole hankkinut energiatodistusta eikä vastannut kehotukseen tai varoitukseen"
                        :diaarinumero       nil
                        :filename           "kuulemiskirje.pdf"
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
                                              :toimitustapa-id          0
                                              :valvonta-id              1
                                              :vastaanottajan-tarkenne  nil}]
                        :id                 3
                        :template-id        5
                        :type-id            7
                        :type-specific-data {:fine 800}
                        :valvonta-id        1
                        :yritykset          []}))))

          (t/testing "Created document can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kuulemiskirje.pdf" valvonta-id 3 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))
                  pdf-document (doc/read-pdf (:body response))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))

              (t/testing "and document has four pages"
                (t/is (= (.getNumberOfPages pdf-document)
                         4)))

              (t/testing "and document looks as it should"
                (doc/assert-pdf-matches-visually pdf-document "documents/kaskypaatos-kuulemiskirje-yksityishenkilo.pdf"))))))))

  (t/testing "Käskypäätös / Kuulemiskirje toimenpide is created successfully for two yksityishenkilö owners and both are mentioned in each document"
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
          html->pdf-called? (atom 0)]

      ;; Add osapuoli to the valvonta
      (valvonta-service/add-henkilo! ts/*db*
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

      ;; Add second owner, both need to be mentioned in the document(s)
      (valvonta-service/add-henkilo! ts/*db*
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
                                      :etunimi                  "Tessa"
                                      :vastaanottajan-tarkenne  nil
                                      :maa                      "FI"})

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

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (fn [html-doc output-stream]
                                        (when (= 0 @html->pdf-called?)
                                          (t/is
                                            (= html-doc
                                               (str
                                                 (slurp (io/resource "documents/base-template.html"))
                                                 (slurp
                                                   (io/resource "documents/kaskypaatoskuulemiskirje-yksityishenkilo-two-owners-1.html"))))))

                                        (when (= 1 @html->pdf-called?)
                                          (t/is
                                            (= html-doc
                                               (str
                                                 (slurp (io/resource "documents/base-template.html"))
                                                 (slurp
                                                   (io/resource "documents/kaskypaatoskuulemiskirje-yksityishenkilo-two-owners-2.html"))))))
                                        (swap! html->pdf-called? inc)
                                        (original-html->pdf html-doc output-stream))}
        (let [new-toimenpide {:type-id            7
                              :deadline-date      (str (LocalDate/of 2023 7 22))
                              :template-id        5
                              :description        "Lähetetään kuulemiskirje, kun myyjä ei ole hankkinut energiatodistusta eikä vastannut kehotukseen tai varoitukseen"
                              :type-specific-data {:fine 800}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (= 2 @html->pdf-called?) "Two documents are created")
          (t/is (= (:status response) 201))))))

  (t/testing "Käskypäätös / Kuulemiskirje toimenpide is created successfully for yritys and document is generated with correct information"
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
          html->pdf-called? (atom false)
          ;; Add osapuoli to the valvonta
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

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial doc/html->pdf-with-assertion
                                               "documents/kaskypaatoskuulemiskirje-yritys.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            7
                              :deadline-date      (str (LocalDate/of 2023 7 22))
                              :template-id        5
                              :description        "Lähetetään kuulemiskirje, kun myyjä ei ole hankkinut energiatodistusta eikä vastannut kehotukseen tai varoitukseen"
                              :type-specific-data {:fine 9000}}
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
              (t/is (= (count response-body) 3))
              (t/is (= (-> response-body
                           last
                           (dissoc :publish-time :create-time))
                       {:author             {:etunimi  "Asian"
                                             :id       1
                                             :rooli-id 2
                                             :sukunimi "Tuntija"}
                        :deadline-date      "2023-07-22"
                        :description        "Lähetetään kuulemiskirje, kun myyjä ei ole hankkinut energiatodistusta eikä vastannut kehotukseen tai varoitukseen"
                        :diaarinumero       nil
                        :filename           "kuulemiskirje.pdf"
                        :henkilot           []
                        :id                 9
                        :template-id        5
                        :type-id            7
                        :type-specific-data {:fine 9000}
                        :valvonta-id        3
                        :yritykset          [{:email                    nil
                                              :id                       1
                                              :jakeluosoite             "Testikatu 12"
                                              :maa                      "FI"
                                              :nimi                     "Yritysomistaja"
                                              :postinumero              "00100"
                                              :postitoimipaikka         "Helsinki"
                                              :puhelin                  nil
                                              :rooli-description        "Omistaja"
                                              :rooli-id                 0
                                              :toimitustapa-description nil
                                              :toimitustapa-id          0
                                              :valvonta-id              3
                                              :vastaanottajan-tarkenne  "Lisäselite C/O"
                                              :ytunnus                  nil}]}))

              (t/testing "Created document can be downloaded through the api"
                (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/yritykset/%s/document/kuulemiskirje.pdf" valvonta-id 9 osapuoli-id))
                                               (test-kayttajat/with-virtu-user)
                                               (mock/header "Accept" "application/pdf")))
                      pdf-document (doc/read-pdf (:body response))]
                  (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
                  (t/is (= (:status response) 200))

                  (t/testing "and document has four pages"
                    (t/is (= (.getNumberOfPages pdf-document)
                             4)))

                  (t/testing "and document looks as it should"
                    (doc/assert-pdf-matches-visually pdf-document "documents/kaskypaatos-kuulemiskirje-yritys.pdf")))))))))))
