(ns solita.etp.valvonta-kaytto.kaskypaatos-tiedoksianto-haastemies-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.common.time :as time]
    [solita.etp.document-assertion :refer [html->pdf-with-assertion]]
    [solita.etp.service.pdf :as pdf]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate)))

(t/use-fixtures :each ts/fixture)

(t/deftest kaskypaatos-tiedoksianto-haastemies-test
  ;; Add the main user for the following tests
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Käskypäätös / Haastemies toimenpide is created successfully for yksityishenkilö and document is generated with correct information"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          html->pdf-called? (atom false)]

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
                                      :rooli-description        "Omistaja"
                                      :etunimi                  "Testi"
                                      :vastaanottajan-tarkenne  nil
                                      :maa                      "FI"})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-haastemies-yksityishenkilo.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            11
                              :deadline-date      (str (LocalDate/of 2023 7 22))
                              :template-id        8
                              :description        "Kuvaus"
                              :type-specific-data {:osapuoli-specific-data [{:osapuoli         {:id   1
                                                                                                :type "henkilo"}
                                                                             :karajaoikeus-id  1
                                                                             :haastemies-email "haaste@mie.het"
                                                                             :document         true}
                                                                            {:osapuoli {:id   2
                                                                                        :type "yritys"}
                                                                             :document false}]}}
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
          (t/is (= (count response-body) 1))
          (t/is (= (-> response-body
                       first
                       (dissoc :publish-time :create-time))
                   {:author             {:etunimi  "Asian"
                                         :id       1
                                         :rooli-id 2
                                         :sukunimi "Tuntija"}
                    :deadline-date      "2023-07-22"
                    :description        "Kuvaus"
                    :diaarinumero       nil
                    :filename           "haastemies-tiedoksianto.pdf"
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
                    :id                 1
                    :template-id        8
                    :type-id            11
                    :type-specific-data {:osapuoli-specific-data [{:document         true
                                                                   :haastemies-email "haaste@mie.het"
                                                                   :karajaoikeus-id  1
                                                                   :osapuoli         {:id   1
                                                                                      :type "henkilo"}}
                                                                  {:document false
                                                                   :osapuoli {:id   2
                                                                              :type "yritys"}}]}
                    :valvonta-id        valvonta-id
                    :yritykset          []}))))))

  (t/testing "Käskypäätös / Tiedoksianto (Haastemies) toimenpide is created successfully for yritys and document is generated with correct information"
    ;; Add the valvonta
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})
          html->pdf-called? (atom false)]

      ;; Add osapuoli to the valvonta
      (valvonta-service/add-yritys! ts/*db*
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
                                     :maa                      "FI"})

      ;; Mock the current time to ensure that the document has a fixed date
      (with-bindings {#'time/clock    (Clock/fixed (-> (LocalDate/of 2023 6 26)
                                                       (.atStartOfDay time/timezone)
                                                       .toInstant)
                                                   time/timezone)
                      #'pdf/html->pdf (partial html->pdf-with-assertion
                                               "documents/kaskypaatos-haastemies-yritys.html"
                                               html->pdf-called?)}
        (let [new-toimenpide {:type-id            11
                              :deadline-date      (str (LocalDate/of 2023 7 22))
                              :template-id        8
                              :description        "Kuvaus"
                              :type-specific-data {:osapuoli-specific-data [{:osapuoli         {:id   1
                                                                                                :type "yritys"}
                                                                             :karajaoikeus-id  1
                                                                             :haastemies-email "haaste@mie.het"
                                                                             :document         true}]}}
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
          (t/is (= (count response-body) 1))
          (t/is (= (-> response-body
                       first
                       (dissoc :publish-time :create-time))
                   {:author             {:etunimi  "Asian"
                                         :id       1
                                         :rooli-id 2
                                         :sukunimi "Tuntija"}
                    :deadline-date      "2023-07-22"
                    :description        "Kuvaus"
                    :diaarinumero       nil
                    :filename           "haastemies-tiedoksianto.pdf"
                    :henkilot           []
                    :id                 2
                    :template-id        8
                    :type-id            11
                    :type-specific-data {:osapuoli-specific-data [{:document         true
                                                                   :haastemies-email "haaste@mie.het"
                                                                   :karajaoikeus-id  1
                                                                   :osapuoli         {:id   1
                                                                                      :type "yritys"}}]}
                    :valvonta-id        valvonta-id
                    :yritykset          [{:toimitustapa-description nil,
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
                                          :maa                      "FI"}]})))))))
