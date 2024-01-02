(ns solita.etp.service.valvonta-kaytto.asha-test
  (:require [clojure.test :as t]
            [solita.etp.service.valvonta-kaytto.asha :as asha]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest remove-osapuolet-with-no-document-if-varsinainen-paatos-test
  (t/testing "Two osapuolis, only the osapuoli with document set to true should be returned"
    (let [osapuolet [{:toimitustapa-description nil
                      :toimitustapa-id          0
                      :email                    nil
                      :rooli-id                 0
                      :jakeluosoite             "Testikatu 12"
                      :valvonta-id              3
                      :postitoimipaikka         "Helsinki"
                      :puhelin                  nil
                      :sukunimi                 "Talonomistaja"
                      :postinumero              "00100"
                      :id                       2
                      :henkilotunnus            "000000-0000"
                      :rooli-description        ""
                      :etunimi                  "Testi"
                      :vastaanottajan-tarkenne  nil
                      :maa                      "FI"}
                     {:toimitustapa-description nil
                      :toimitustapa-id          0
                      :email                    nil
                      :rooli-id                 0
                      :jakeluosoite             "Testikatu 13"
                      :valvonta-id              3
                      :postitoimipaikka         "Stockholm"
                      :puhelin                  nil
                      :sukunimi                 "Omistaja"
                      :postinumero              "00000"
                      :id                       3
                      :henkilotunnus            "000000-0001"
                      :rooli-description        ""
                      :etunimi                  "Toinen"
                      :vastaanottajan-tarkenne  nil
                      :maa                      "SV"}]
          toimenpide {:description
                      "Tehdään varsinainen päätös, omistaja vastasi kuulemiskirjeeseen"
                      :type-id      8
                      :valvonta-id  3
                      :filename     nil
                      :diaarinumero "ARA-05.03.01-2023-159"
                      :id           12
                      :author-id    1
                      :type-specific-data
                      {:department-head-title-fi "Apulaisjohtaja"
                       :department-head-name     "Yli Päällikkö"
                       :osapuoli-specific-data   [{:hallinto-oikeus-id 1
                                                   :osapuoli           {:id   2
                                                                        :type "henkilo"}
                                                   :document           true}
                                                  {:osapuoli {:id   3
                                                              :type "henkilo"}
                                                   :document false}]
                       :recipient-answered       true
                       :statement-sv             "Han vet inte. Vi förlotar."
                       :statement-fi             "Tämän kerran annetaan anteeksi kun hän ei tiennyt."
                       :department-head-title-sv "Apulaisjohtaja på svenska"
                       :fine                     857
                       :answer-commentary-sv     "Jag visste inte att ett intyg behövs :("
                       :answer-commentary-fi     "En tiennyt, että todistus tarvitaan :("}
                      :template-id  6}]
      (t/is (= (asha/remove-osapuolet-with-no-document toimenpide osapuolet)
               [{:toimitustapa-description nil
                 :toimitustapa-id          0
                 :email                    nil
                 :rooli-id                 0
                 :jakeluosoite             "Testikatu 12"
                 :valvonta-id              3
                 :postitoimipaikka         "Helsinki"
                 :puhelin                  nil
                 :sukunimi                 "Talonomistaja"
                 :postinumero              "00100"
                 :id                       2
                 :henkilotunnus            "000000-0000"
                 :rooli-description        ""
                 :etunimi                  "Testi"
                 :vastaanottajan-tarkenne  nil
                 :maa                      "FI"}]))))

  (t/testing "Henkilo-osapuoli and yritysosapuoli with the same id"
    (let [osapuolet [{:toimitustapa-description nil,
                      :toimitustapa-id          0,
                      :email                    nil,
                      :rooli-id                 0,
                      :jakeluosoite             "Testikatu 12",
                      :valvonta-id              1,
                      :postitoimipaikka         "Helsinki",
                      :puhelin                  nil,
                      :sukunimi                 "Talonomistaja",
                      :postinumero              "00100",
                      :id                       1,
                      :henkilotunnus            "000000-0000",
                      :rooli-description        "",
                      :etunimi                  "Testi",
                      :vastaanottajan-tarkenne  nil,
                      :maa                      "FI"}
                     {:toimitustapa-description nil,
                      :toimitustapa-id          0,
                      :email                    nil,
                      :rooli-id                 0,
                      :jakeluosoite             "Testikatu 12",
                      :valvonta-id              1,
                      :postitoimipaikka         "Helsinki",
                      :ytunnus                  nil,
                      :puhelin                  nil,
                      :nimi                     "Yritysomistaja",
                      :postinumero              "00100",
                      :id                       1,
                      :rooli-description        "Omistaja",
                      :vastaanottajan-tarkenne  "Lisäselite C/O",
                      :maa                      "FI"}]]
      (t/testing "only the henkilo-osapuoli has a document so it should be returned"
        (t/is (= (asha/remove-osapuolet-with-no-document
                   {:type-id            8
                    :type-specific-data {:osapuoli-specific-data [{:hallinto-oikeus-id 1
                                                                   :osapuoli           {:id   1
                                                                                        :type "henkilo"}
                                                                   :document           true}
                                                                  {:hallinto-oikeus-id 3
                                                                   :osapuoli           {:id   1
                                                                                        :type "yritys"}
                                                                   :document           false}]}}
                   osapuolet)
                 [{:toimitustapa-description nil,
                   :toimitustapa-id          0,
                   :email                    nil,
                   :rooli-id                 0,
                   :jakeluosoite             "Testikatu 12",
                   :valvonta-id              1,
                   :postitoimipaikka         "Helsinki",
                   :puhelin                  nil,
                   :sukunimi                 "Talonomistaja",
                   :postinumero              "00100",
                   :id                       1,
                   :henkilotunnus            "000000-0000",
                   :rooli-description        "",
                   :etunimi                  "Testi",
                   :vastaanottajan-tarkenne  nil,
                   :maa                      "FI"}])))

      (t/testing "only the yritys-osapuoli has a document so it should be returned"
        (t/is (= (asha/remove-osapuolet-with-no-document
                   {:type-id            8
                    :type-specific-data {:osapuoli-specific-data [{:hallinto-oikeus-id 1
                                                                   :osapuoli           {:id   1
                                                                                        :type "henkilo"}
                                                                   :document           false}
                                                                  {:hallinto-oikeus-id 3
                                                                   :osapuoli           {:id   1
                                                                                        :type "yritys"}
                                                                   :document           true}]}}
                   osapuolet)
                 [{:toimitustapa-description nil,
                   :toimitustapa-id          0,
                   :email                    nil,
                   :rooli-id                 0,
                   :jakeluosoite             "Testikatu 12",
                   :valvonta-id              1,
                   :postitoimipaikka         "Helsinki",
                   :ytunnus                  nil,
                   :puhelin                  nil,
                   :nimi                     "Yritysomistaja",
                   :postinumero              "00100",
                   :id                       1,
                   :rooli-description        "Omistaja",
                   :vastaanottajan-tarkenne  "Lisäselite C/O",
                   :maa                      "FI"}])))

      (t/testing "both have a document so both should be returned"
        (t/is (= (asha/remove-osapuolet-with-no-document
                   {:type-id            8
                    :type-specific-data {:osapuoli-specific-data [{:hallinto-oikeus-id 1
                                                                   :osapuoli           {:id   1
                                                                                        :type "henkilo"}
                                                                   :document           true}
                                                                  {:hallinto-oikeus-id 3
                                                                   :osapuoli           {:id   1
                                                                                        :type "yritys"}
                                                                   :document           true}]}}
                   osapuolet)
                 osapuolet)))))

  (t/testing "Two osapuolis and two documents, both osapuolet are returned"
    (t/is (= (asha/remove-osapuolet-with-no-document
               {:type-id            8
                :type-specific-data {:osapuoli-specific-data [{:hallinto-oikeus-id 1
                                                               :osapuoli           {:id   2
                                                                                    :type "henkilo"}
                                                               :document           true}
                                                              {:hallinto-oikeus-id 3
                                                               :osapuoli           {:id   3
                                                                                    :type "yritys"}
                                                               :document           true}]}}
               [{:id       2
                 :etunimi  "Testi"
                 :sukunimi "Testerson"}
                {:id   3
                 :nimi "Testiyritys"}])
             [{:id       2
               :etunimi  "Testi"
               :sukunimi "Testerson"}
              {:id   3
               :nimi "Testiyritys"}])))

  (t/testing "Two osapuolis and toimenpide is not varsinainen päätös, so both should be returned"
    (t/is (= (asha/remove-osapuolet-with-no-document
               {:type-id 7}
               [{:id       2
                 :etunimi  "Testi"
                 :sukunimi "Testerson"}
                {:id   3
                 :nimi "Testiyritys"}])
             [{:id       2
               :etunimi  "Testi"
               :sukunimi "Testerson"}
              {:id   3
               :nimi "Testiyritys"}]))))
