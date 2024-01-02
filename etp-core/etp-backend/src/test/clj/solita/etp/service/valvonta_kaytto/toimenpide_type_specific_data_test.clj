(ns solita.etp.service.valvonta-kaytto.toimenpide-type-specific-data-test
  (:require [clojure.test :as t]
            [solita.etp.service.luokittelu :as luokittelu]
            [solita.etp.service.valvonta-kaytto :as valvonta-service]
            [solita.etp.service.valvonta-kaytto.toimenpide-type-specific-data :as type-specific-data]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest format-type-specific-data-test
  (t/testing "For käskypäätös / varsinainen päätös toimenpide a new key vastaus is added and its value is based on values of :recipient-answered and :answer-commentary"
    (t/is (= (type-specific-data/format-type-specific-data
               ts/*db*
               {:type-id            8
                :type-specific-data {:fine                     129
                                     :osapuoli-specific-data   [{:osapuoli             {:id   1
                                                                                        :type "henkilo"}
                                                                 :hallinto-oikeus-id   0
                                                                 :document             true
                                                                 :recipient-answered   true
                                                                 :answer-commentary-fi "Voi anteeksi, en tiennyt."
                                                                 :answer-commentary-sv "Jag vet inte, förlåt."
                                                                 :statement-fi         "Olisi pitänyt tietää."
                                                                 :statement-sv         "Du måste ha visst."}]
                                     :department-head-name     "Jorma Jormanen"
                                     :department-head-title-fi "Hallinto-oikeuden presidentti"
                                     :department-head-title-sv "Hallinto-oikeuden kuningas"}}
               {:id   1
                :type "henkilo"})
             {:fine                     129
              :vastaus-fi               "Asianosainen antoi vastineen kuulemiskirjeeseen. Voi anteeksi, en tiennyt."
              :oikeus-fi                "Helsingin hallinto-oikeudelta"
              :vastaus-sv               "gav ett bemötande till brevet om hörande. Jag vet inte, förlåt."
              :statement-fi             "Olisi pitänyt tietää."
              :statement-sv             "Du måste ha visst."
              :oikeus-sv                "Helsingfors"
              :department-head-name     "Jorma Jormanen"
              :department-head-title-fi "Hallinto-oikeuden presidentti"
              :department-head-title-sv "Hallinto-oikeuden kuningas"
              :recipient-answered       true})))

  (t/testing "For käskypäätös / kuulemiskirje toimenpide fine and all owner names are returned"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0})]
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
                                     :rooli-description        ""
                                     :maa                      "FI"})
      (t/is (= (type-specific-data/format-type-specific-data
                 ts/*db*
                 {:type-id            7
                  :valvonta-id        valvonta-id
                  :type-specific-data {:fine 800}}
                 nil)
               {:fine      800
                :omistajat "Talonomistaja Testi, Yritysomistaja"})))))

(t/deftest hallinto-oikeus-id->formatted-string-test
  (t/testing "id 0 results in Helsingin hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 0)
             {:fi "Helsingin hallinto-oikeudelta"
              :sv "Helsingfors"})))

  (t/testing "id 1 results in Hämeenlinnan hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 1)
             {:fi "Hämeenlinnan hallinto-oikeudelta"
              :sv "Tavastehus"})))

  (t/testing "id 2 results in Itä-Suomen hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 2)
             {:fi "Itä-Suomen hallinto-oikeudelta"
              :sv "Östra Finland"})))

  (t/testing "id 3 results in Pohjois-Suomen hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 3)
             {:fi "Pohjois-Suomen hallinto-oikeudelta"
              :sv "Norra Finland"})))

  (t/testing "id 4 results in Turun hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 4)
             {:fi "Turun hallinto-oikeudelta"
              :sv "Åbo"})))

  (t/testing "id 5 results in Vaasan hallinto-oikeudelta"
    (t/is (= (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 5)
             {:fi "Vaasan hallinto-oikeudelta"
              :sv "Vasa"})))

  (t/testing "Unknown id results in exception"
    (t/is (thrown-with-msg?
            Exception
            #"Unknown hallinto-oikeus-id: 6" (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* 6))))

  (t/testing "All hallinto-oikeudet in database have a formatted string"
    (let [hallinto-oikeudet (luokittelu/find-hallinto-oikeudet ts/*db*)]
      (t/is (= (count hallinto-oikeudet)
               6))

      (doseq [hallinto-oikeus hallinto-oikeudet]
        (t/is (not (nil? (:fi (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* (:id hallinto-oikeus))))))
        (t/is (not (nil? (:sv (type-specific-data/hallinto-oikeus-id->formatted-strings ts/*db* (:id hallinto-oikeus))))))))))

(t/deftest find-court-id-from-osapuoli-specific-data-test
  (t/testing "Correct court id is found for the osapuoli"
    (t/is (= (type-specific-data/find-administrative-court-id-from-osapuoli-specific-data
               [{:osapuoli           {:id   1
                                      :type "henkilo"}
                 :hallinto-oikeus-id 0}
                {:osapuoli           {:id   3
                                      :type "henkilo"}
                 :hallinto-oikeus-id 5}
                {:osapuoli           {:id   643
                                      :type "yritys"}
                 :hallinto-oikeus-id 2}]
               {:id   3
                :type "henkilo"})
             5))))
