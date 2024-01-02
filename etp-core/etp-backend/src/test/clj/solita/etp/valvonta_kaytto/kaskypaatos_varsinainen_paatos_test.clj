(ns solita.etp.valvonta-kaytto.kaskypaatos-varsinainen-paatos-test
  (:require
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :refer [html->pdf-with-assertion]]
    [solita.etp.schema.valvonta-kaytto :as valvonta-schema]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.service.valvonta-kaytto.store :as file-store]
    [solita.etp.test-data.generators :as generators]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(def original-store-hallinto-oikeus-attachment file-store/store-hallinto-oikeus-attachment!)

(t/deftest kaskypaatos-varsinainen-paatos-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Käskypäätös / varsinainen päätös toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
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
      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 8 28)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-varsinainen-paatos-yksityishenkilo.html"
                                               html->pdf-called?)
                      #'file-store/store-hallinto-oikeus-attachment!
                      (fn [aws-s3-client valvonta-id toimenpide-id osapuoli document]
                        (reset! store-hallinto-oikeus-attachment-called? true)
                        (original-store-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id osapuoli document))}
        (let [new-toimenpide {:type-id            8
                              :deadline-date      (str (LocalDate/of 2023 10 4))
                              :template-id        6
                              :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                              :type-specific-data {:fine                     857
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
                                                   :department-head-name     "Yli Päällikkö"}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (true? @store-hallinto-oikeus-attachment-called?))
          (t/is (= (:status response) 201))))

      (t/testing "Created document can be downloaded through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kaskypaatos.pdf" valvonta-id 4 osapuoli-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
          (t/is (= (:status response) 200))))

      (t/testing "Created document is not available without authentication"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kaskypaatos.pdf" valvonta-id 4 osapuoli-id))
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (:status response) 403))
          (t/is (= (:body response) "Forbidden"))))

      (t/testing "hallinto-oikeus-liite can be downloaded through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/attachment/hallinto-oikeus.pdf" valvonta-id 4 osapuoli-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
          (t/is (= (:status response) 200))

          (t/testing "hallinto-oikeus-liite is the correct one"
            (t/is (= (slurp (io/input-stream (io/resource "pdf/hallinto-oikeudet/Valitusosoitus_30_pv_HAMEENLINNAN_HAO.pdf")))
                     (slurp (:body response)))))))

      (t/testing "hallinto-oikeus-liite is not available without authentication"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/attachment/hallinto-oikeus.pdf" valvonta-id 4 osapuoli-id))
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (:status response) 403))
          (t/is (= (:body response) "Forbidden"))))))

  (t/testing "Käskypäätös / varsinainen päätös toimenpide is created successfully for yritys and document is generated with correct information"
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
          html->pdf-called? (atom false)
          store-hallinto-oikeus-attachment-called? (atom false)
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
      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 8 28)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-varsinainen-paatos-yritys.html"
                                               html->pdf-called?)
                      #'file-store/store-hallinto-oikeus-attachment!
                      (fn [aws-s3-client valvonta-id toimenpide-id osapuoli document]
                        (reset! store-hallinto-oikeus-attachment-called? true)
                        (original-store-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id osapuoli document))}
        (let [new-toimenpide {:type-id            8
                              :deadline-date      (str (LocalDate/of 2023 10 4))
                              :template-id        6
                              :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                              :type-specific-data {:fine                     857
                                                   :osapuoli-specific-data   [{:osapuoli           {:id   osapuoli-id
                                                                                                    :type "yritys"}
                                                                               :hallinto-oikeus-id 2
                                                                               :document           true
                                                                               :recipient-answered false}]
                                                   :department-head-title-fi "Senior Vice President"
                                                   :department-head-title-sv "Kungen"
                                                   :department-head-name     "Jane Doe"}}
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
              (t/is (= (count response-body) 4))
              (t/is (contains? (->> response-body
                                    (map #(dissoc % :publish-time :create-time))
                                    set)
                               {:description
                                "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen",
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
                                :type-id       8,
                                :valvonta-id   2,
                                :author        {:rooli-id 2, :sukunimi "Tuntija", :id 1, :etunimi "Asian"},
                                :filename      "kaskypaatos.pdf",
                                :diaarinumero  "ARA-05.03.01-2023-132",
                                :id            8,
                                :deadline-date "2023-10-04",
                                :type-specific-data
                                {:department-head-title-fi "Senior Vice President",
                                 :department-head-name     "Jane Doe",
                                 :osapuoli-specific-data
                                 [{:hallinto-oikeus-id 2
                                   :osapuoli           {:id   1
                                                        :type "yritys"}
                                   :document           true
                                   :recipient-answered false}]
                                 :department-head-title-sv "Kungen"
                                 :fine                     857},
                                :template-id   6}))))

          (t/testing "Created document can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/yritykset/%s/document/kaskypaatos.pdf" valvonta-id 8 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))))

          (t/testing "Created document is not available without authentication"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/yritykset/%s/document/kaskypaatos.pdf" valvonta-id 8 osapuoli-id))
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (:status response) 403))
              (t/is (= (:body response) "Forbidden"))))

          (t/testing "hallinto-oikeus-liite can be downloaded through the api"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/yritykset/%s/attachment/hallinto-oikeus.pdf" valvonta-id 8 osapuoli-id))
                                           (test-kayttajat/with-virtu-user)
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
              (t/is (= (:status response) 200))

              (t/testing "hallinto-oikeus-liite is the correct one"
                (t/is (= (slurp (io/input-stream (io/resource "pdf/hallinto-oikeudet/Valitusosoitus_30_pv_ITA-SUOMEN_HAO.pdf")))
                         (slurp (:body response)))))))

          (t/testing "hallinto-oikeus-liite is not available without authentication"
            (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/yritykset/%s/attachment/hallinto-oikeus.pdf" valvonta-id 8 osapuoli-id))
                                           (mock/header "Accept" "application/pdf")))]
              (t/is (= (:status response) 403))
              (t/is (= (:body response) "Forbidden"))))))))

  (t/testing "Käskypäätös / varsinainen päätös toimenpide is created successfully when there are multiple osapuolis but one lives abroad and will not receive the document because of being outside court jurisdiction"
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
          html->pdf-called? (atom false)
          ;; Add osapuolis to the valvonta
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
                         :maa                      "FI"})
          osapuoli-id-2 (valvonta-service/add-henkilo!
                          ts/*db*
                          valvonta-id
                          {:toimitustapa-description nil
                           :toimitustapa-id          0
                           :email                    nil
                           :rooli-id                 0
                           :jakeluosoite             "Testikatu 13"
                           :postitoimipaikka         "Stockholm"
                           :puhelin                  nil
                           :sukunimi                 "Omistaja"
                           :postinumero              "00000"
                           :henkilotunnus            "000000-0001"
                           :rooli-description        ""
                           :etunimi                  "Toinen"
                           :vastaanottajan-tarkenne  nil
                           :maa                      "SV"})]
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
      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 8 28)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      ;; Assert that the created document is for the correct osapuoli
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-varsinainen-paatos-yksityishenkilo.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            8
                              :deadline-date      (str (LocalDate/of 2023 10 4))
                              :template-id        6
                              :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                              :type-specific-data {:fine                     857
                                                   :osapuoli-specific-data   [{:osapuoli             {:id   osapuoli-id
                                                                                                      :type "henkilo"}
                                                                               :hallinto-oikeus-id   1
                                                                               :document             true
                                                                               :recipient-answered   true
                                                                               :answer-commentary-fi "En tiennyt, että todistus tarvitaan :("
                                                                               :answer-commentary-sv "Jag visste inte att ett intyg behövs :("
                                                                               :statement-fi         "Tämän kerran annetaan anteeksi, kun hän ei tiennyt."
                                                                               :statement-sv         "Han vet inte. Vi förlotar."}
                                                                              {:osapuoli {:id   osapuoli-id-2
                                                                                          :type "henkilo"}
                                                                               :document false}]
                                                   :department-head-title-fi "Apulaisjohtaja"
                                                   :department-head-title-sv "Apulaisjohtaja på svenska"
                                                   :department-head-name     "Yli Päällikkö"}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (= (:status response) 201))))))


  (t/testing "Preview api call for käskypäätös / varsinainen päätös toimenpide succeeds"
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
            new-toimenpide {:type-id            8
                            :deadline-date      (str (LocalDate/of 2023 10 4))
                            :template-id        6
                            :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                            :type-specific-data {:fine                     857
                                                 :osapuoli-specific-data   [{:osapuoli           {:id   osapuoli-id
                                                                                                  :type "henkilo"}
                                                                             :hallinto-oikeus-id 3
                                                                             :document           true
                                                                             :recipient-answered false}]
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
            new-toimenpide {:type-id            8
                            :deadline-date      (str (LocalDate/of 2023 10 4))
                            :template-id        6
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

(t/deftest fetching-johtaja-test
  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!)]
    (t/testing "Kun käskypäätös / varsinainen päätös - toimenpidettä ei ole, osaston päällikön tietoja ei löydy"
      (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/kaytto/johtaja")
                                     (test-kayttajat/with-virtu-user)
                                     (mock/header "Accept" "application/json")))
            response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
        (t/is (= (:status response) 200))
        (t/is (= response-body {:department-head-name     nil
                                :department-head-title-fi nil
                                :department-head-title-sv nil}))))

    (t/testing "käskypäätös / varsinainen päätös -toimenpiteen olemassaollessa osaston päällikön tiedot täydentyvät responseen"
      (let [valvonta-id (valvonta-service/add-valvonta!
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
        ;; Add käskypäätös / varsinainen päätös toimenpide so department-head-name
        ;; and department-head-title are populated in the response
        (jdbc/insert! ts/*db*
                      :vk_toimenpide
                      {:valvonta_id        valvonta-id
                       :type_id            8
                       :create_time        (-> (LocalDate/of 2023 8 10)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :publish_time       (-> (LocalDate/of 2023 8 10)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :deadline_date      (LocalDate/of 2023 8 28)
                       :diaarinumero       "ARA-05.03.01-2023-235"
                       :type_specific_data {:fine                     6100
                                            :department-head-name     "Testi Testinen"
                                            :department-head-title-fi "Ylitarkastaja"
                                            :department-head-title-sv "Ylitarkastaja på svenska"}})
        (let [response (ts/handler (-> (mock/request :get "/api/private/valvonta/kaytto/johtaja")
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= response-body {:department-head-name     "Testi Testinen"
                                  :department-head-title-fi "Ylitarkastaja"
                                  :department-head-title-sv "Ylitarkastaja på svenska"})))))))

(t/deftest two-osapuolet-one-id-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Käskypäätös / varsinainen päätös toimenpide is created successfully for yksityishenkilö even though yritysosapuoli with same id exists"
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
                         :rooli-description        ""
                         :etunimi                  "Testi"
                         :vastaanottajan-tarkenne  nil
                         :maa                      "FI"})

          ;; Add yritysosapuoli to ensure that different types of osapuolis with the same id
          ;; do not affect each other
          yritysosapuoli-id (valvonta-service/add-yritys!
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

      (t/is (= osapuoli-id yritysosapuoli-id))
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
      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 8 28)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-varsinainen-paatos-yksityishenkilo.html"
                                               html->pdf-called?)
                      #'file-store/store-hallinto-oikeus-attachment!
                      (fn [aws-s3-client valvonta-id toimenpide-id osapuoli document]
                        (reset! store-hallinto-oikeus-attachment-called? true)
                        (original-store-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id osapuoli document))}
        (let [new-toimenpide {:type-id            8
                              :deadline-date      (str (LocalDate/of 2023 10 4))
                              :template-id        6
                              :description        "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                              :type-specific-data {:fine                     857
                                                   :osapuoli-specific-data   [{:osapuoli {:id   yritysosapuoli-id
                                                                                          :type "yritys"}
                                                                               :document false}
                                                                              {:osapuoli             {:id   osapuoli-id
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
                                                   :department-head-name     "Yli Päällikkö"}}
              response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (mock/json-body new-toimenpide)
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))]
          (t/is (true? @html->pdf-called?))
          (t/is (true? @store-hallinto-oikeus-attachment-called?))
          (t/is (= (:status response) 201))))

      (t/testing "Created document can be downloaded through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kaskypaatos.pdf" valvonta-id 4 osapuoli-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
          (t/is (= (:status response) 200))))

      (t/testing "Created document is not available without authentication"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/document/kaskypaatos.pdf" valvonta-id 4 osapuoli-id))
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (:status response) 403))
          (t/is (= (:body response) "Forbidden"))))

      (t/testing "hallinto-oikeus-liite can be downloaded through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/attachment/hallinto-oikeus.pdf" valvonta-id 4 osapuoli-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (-> response :headers (get "Content-Type")) "application/pdf"))
          (t/is (= (:status response) 200))

          (t/testing "hallinto-oikeus-liite is the correct one"
            (t/is (= (slurp (io/input-stream (io/resource "pdf/hallinto-oikeudet/Valitusosoitus_30_pv_HAMEENLINNAN_HAO.pdf")))
                     (slurp (:body response)))))))

      (t/testing "hallinto-oikeus-liite is not available without authentication"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet/%s/henkilot/%s/attachment/hallinto-oikeus.pdf" valvonta-id 4 osapuoli-id))
                                       (mock/header "Accept" "application/pdf")))]
          (t/is (= (:status response) 403))
          (t/is (= (:body response) "Forbidden")))))))