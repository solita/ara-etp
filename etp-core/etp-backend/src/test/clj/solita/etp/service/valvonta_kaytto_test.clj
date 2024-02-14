(ns solita.etp.service.valvonta-kaytto-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.valvonta-kaytto :as valvonta-kaytto-schema]
            [solita.etp.service.valvonta-kaytto :as valvonta-kaytto]
            [solita.etp.test-data.kayttaja :as test-kayttajat]
            [solita.etp.test-system :as ts])
  (:import (java.time LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(t/deftest find-toimenpidetyypit-test
  (let [toimenpidetyypit (valvonta-kaytto/find-toimenpidetyypit ts/*db*)]
    (t/testing "find-toimenpidetyypit returns correct toimenpidetypes"
      (t/is (= toimenpidetyypit
               [{:id                   0
                 :label-fi             "Valvonnan aloitus"
                 :label-sv             "Valvonnan aloitus (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   1
                 :label-fi             "Tietopyyntö 2021"
                 :label-sv             "Begäran om uppgifter 2021"
                 :valid                false
                 :manually-deliverable false
                 :allow-comments       false}
                {:id                   2
                 :label-fi             "Kehotus"
                 :label-sv             " Uppmaning"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       false}
                {:id                   3
                 :label-fi             "Varoitus"
                 :label-sv             "Varning"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       false}
                {:id                   4
                 :label-fi             "Käskypäätös"
                 :label-sv             "Käskypäätös (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   5
                 :label-fi             "Valvonnan lopetus"
                 :label-sv             "Valvonnan lopetus (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   6
                 :label-fi             "HaO-käsittely"
                 :label-sv             "HaO-käsittely (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   7
                 :label-fi             "Käskypäätös / kuulemiskirje"
                 :label-sv             "Käskypäätös / kuulemiskirje (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   8
                 :label-fi             "Käskypäätös / varsinainen päätös"
                 :label-sv             "Käskypäätös / varsinainen päätös (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   9
                 :label-fi             "Käskypäätös / tiedoksianto (ensimmäinen postitus)"
                 :label-sv             "Käskypäätös / tiedoksianto (ensimmäinen postitus) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   10
                 :label-fi             "Käskypäätös / tiedoksianto (toinen postitus)"
                 :label-sv             "Käskypäätös / tiedoksianto (toinen postitus) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   11
                 :label-fi             "Käskypäätös / tiedoksianto (Haastemies)"
                 :label-sv             "Käskypäätös / tiedoksianto (Haastemies) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   12
                 :label-fi             "Käskypäätös / valitusajan odotus ja umpeutuminen"
                 :label-sv             "Käskypäätös / valitusajan odotus ja umpeutuminen (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   14
                 :label-fi             "Sakkopäätös / kuulemiskirje"
                 :label-sv             "Sakkopäätös / kuulemiskirje (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   15
                 :label-fi             "Sakkopäätös / varsinainen päätös"
                 :label-sv             "Sakkopäätös / varsinainen päätös (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   16
                 :label-fi             "Sakkopäätös / tiedoksianto (ensimmäinen postitus)"
                 :label-sv             "Sakkopäätös / tiedoksianto (ensimmäinen postitus) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   17
                 :label-fi             "Sakkopäätös / tiedoksianto (toinen postitus)"
                 :label-sv             "Sakkopäätös / tiedoksianto (toinen postitus) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   18
                 :label-fi             "Sakkopäätös / tiedoksianto (Haastemies)"
                 :label-sv             "Sakkopäätös / tiedoksianto (Haastemies) (sv)"
                 :valid                true
                 :manually-deliverable true
                 :allow-comments       true}
                {:id                   19
                 :label-fi             "Sakkopäätös / valitusajan odotus ja umpeutuminen"
                 :label-sv             "Sakkopäätös / valitusajan odotus ja umpeutuminen (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:id                   21
                 :label-fi             "Sakkoluettelon lähetys menossa"
                 :label-sv             "Sakkoluettelon lähetys menossa (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       true}
                {:allow-comments       true
                 :id                   22
                 :label-fi             "Suljetun valvonnan uudelleenavaus"
                 :label-sv             "Suljetun valvonnan uudelleenavaus (sv)"
                 :manually-deliverable false
                 :valid                false}])))

    (t/testing "Toimenpidetyypit matches the schema"
      (t/is (nil? (schema/check [valvonta-kaytto-schema/Toimenpidetyypit]
                                toimenpidetyypit))))))

(t/deftest hallinto-oikeudet-id-schema-test
  (t/testing "Ids for existing hallinto-oikeudet matches the values allowed in the schema"
    (let [hallinto-oikeudet (valvonta-kaytto/find-hallinto-oikeudet ts/*db*)
          ids (map :id hallinto-oikeudet)]
      (t/is (= (count ids)
               6))

      (t/testing "Existing ids are valid according to the schema"
        (doseq [id ids]
          (t/is (nil? (schema/check valvonta-kaytto-schema/HallintoOikeusId id)))))

      (t/testing "-1 is not valid according to the schema"
        (t/is (not (nil? (schema/check valvonta-kaytto-schema/HallintoOikeusId -1)))))

      (t/testing "bigger ids than the existing ones are not allowed"
        (let [biggest-valid-id (apply max ids)
              invalid-ids (range (inc biggest-valid-id) (+ biggest-valid-id 101))]
          (t/is (= (count invalid-ids)
                   100))

          (doseq [id invalid-ids]
            (t/is (not (nil? (schema/check valvonta-kaytto-schema/HallintoOikeusId id)))
                  (str "Id " id " should not be valid"))))))))

(t/deftest department-head-data-test
  (t/testing "When there is no previous käskypäätös / varsinainen päätös toimenpide, map without values is returned"
    (t/is (= (valvonta-kaytto/department-head-data ts/*db*)
             {:department-head-title-fi nil
              :department-head-title-sv nil
              :department-head-name     nil})))

  (t/testing "When there is previous käskypäätös / varsinainen päätös toimenpide, the title and name used in it is returned"
    (let [valvonta-id (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})]
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
                                          :department-head-title-sv "Övertillsyningsman"}})

      (t/is (= (valvonta-kaytto/department-head-data ts/*db*)
               {:department-head-title-fi "Ylitarkastaja"
                :department-head-title-sv "Övertillsyningsman"
                :department-head-name     "Testi Testinen"}))))

  (t/testing "When there are multiple previous käskypäätös / varsinainen päätös toimenpide, the title and name used in the latest one is returned"
    (let [valvonta-id (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})]
      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id        valvonta-id
                     :type_id            8
                     :create_time        (-> (LocalDate/of 2022 8 10)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :publish_time       (-> (LocalDate/of 2022 8 10)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :deadline_date      (LocalDate/of 2022 8 28)
                     :diaarinumero       "ARA-05.03.01-2022-235"
                     :type_specific_data {:fine                     6100
                                          :department-head-name     "Keskivanhan Tarkastaja"
                                          :department-head-title-fi "Keskitason tarkastaja"
                                          :department-head-title-sv "Keskitason tarkastaja ruotsiksi"}})

      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id        valvonta-id
                     :type_id            8
                     :create_time        (-> (LocalDate/of 2021 8 10)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :publish_time       (-> (LocalDate/of 2021 8 10)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :deadline_date      (LocalDate/of 2021 8 28)
                     :diaarinumero       "ARA-05.03.01-2021-235"
                     :type_specific_data {:fine                     6100
                                          :department-head-name     "Vanhin Tarkastaja"
                                          :department-head-title-fi "Alimman tason tarkastaja"
                                          :department-head-title-sv "Alimman tason tarkastaja ruotsiksi"}})

      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id        valvonta-id
                     :type_id            8
                     :create_time        (-> (LocalDate/of 2023 8 11)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :publish_time       (-> (LocalDate/of 2023 8 11)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :deadline_date      (LocalDate/of 2023 8 28)
                     :diaarinumero       "ARA-05.03.01-2023-235"
                     :type_specific_data {:fine                     6100
                                          :department-head-name     "Uusin Tarkastaja"
                                          :department-head-title-fi "Yliylitarkastaja"
                                          :department-head-title-sv "Yliylitarkastaja på svenska"}})

      (t/is (= (valvonta-kaytto/department-head-data ts/*db*)
               {:department-head-title-fi "Yliylitarkastaja"
                :department-head-title-sv "Yliylitarkastaja på svenska"
                :department-head-name     "Uusin Tarkastaja"}))

      (t/testing "related valvonta does not affect that the newest of them all is returned"
        (let [valvonta-id-2 (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})]
          (jdbc/insert! ts/*db*
                        :vk_toimenpide
                        {:valvonta_id        valvonta-id-2
                         :type_id            8
                         :create_time        (-> (LocalDate/of 2023 8 12)
                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                 .toInstant)
                         :publish_time       (-> (LocalDate/of 2023 8 12)
                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                 .toInstant)
                         :deadline_date      (LocalDate/of 2023 8 28)
                         :diaarinumero       "ARA-05.03.01-2023-235"
                         :type_specific_data {:fine                     6100
                                              :department-head-name     "Vielä Uudempi Tarkastaja"
                                              :department-head-title-fi "Yliyliylitarkastaja"
                                              :department-head-title-sv "Yliyliylitarkastaja på svenska"}})
          (t/is (= (valvonta-kaytto/department-head-data ts/*db*)
                   {:department-head-title-fi "Yliyliylitarkastaja"
                    :department-head-title-sv "Yliyliylitarkastaja på svenska"
                    :department-head-name     "Vielä Uudempi Tarkastaja"})))))))

(t/deftest find-valvonnat-test
  (t/testing "find-valvonnat returns"
    (t/testing "an empty list when there are no valvonnat"
      (let [query {}]
        (t/is (empty? (valvonta-kaytto/find-valvonnat ts/*db* query)))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   0)))))

    ;; Create a valvonta
    (let [valvonta-not-in-uhkasakkoprosessi (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})]
      (t/testing "a valvonta when valvonta exists"
        (let [query {}]
          (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                   1))

          (t/testing "count-valvonnat matches the actual count"
            (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                     1)))))

      (t/testing "a valvonta when searching by address"
        (let [query {:keyword "Testitie 5"}]
          (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                   1))

          (t/testing "count-valvonnat matches the actual count"
            (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                     1)))))

      (t/testing "an empty list when valvonta exists but is not in uhkasakkoprosessi and we want only valvonnat in uhkasakkoprosessi"
        (let [query {:only-uhkasakkoprosessi true}]
          (t/is (empty? (valvonta-kaytto/find-valvonnat ts/*db* query)))

          (t/testing "count-valvonnat matches the actual count"
            (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                     0)))))

      (t/testing "returns a valvonta that is in uhkasakkoprosessi when only-uhkasakkoprosessi is true"
        (let [valvonta-in-uhkasakkoprosessi (valvonta-kaytto/add-valvonta! ts/*db* {:katuosoite "Testitie 6"})
              query {:only-uhkasakkoprosessi true}]
          ;; Create a toimenpide that is part of uhkasakkoprosessi
          (jdbc/insert! ts/*db*
                        :vk_toimenpide
                        {:valvonta_id        valvonta-in-uhkasakkoprosessi
                         :type_id            7
                         :create_time        (-> (LocalDate/of 2023 8 12)
                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                 .toInstant)
                         :publish_time       (-> (LocalDate/of 2023 8 12)
                                                 (.atStartOfDay (ZoneId/systemDefault))
                                                 .toInstant)
                         :deadline_date      (LocalDate/of 2023 8 28)
                         :diaarinumero       "ARA-05.03.01-2023-235"
                         :type_specific_data {:fine 6100}})

          (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                   1))
          (t/is (= (-> (valvonta-kaytto/find-valvonnat ts/*db* query)
                       first
                       :id)
                   valvonta-in-uhkasakkoprosessi))

          (t/testing "count-valvonnat matches the actual count"
            (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                     1)))

          (t/testing "and both valvonnat when only-uhkasakkoprosessi is false"
            (let [query {:only-uhkasakkoprosessi false}
                  valvonnat (valvonta-kaytto/find-valvonnat ts/*db* query)]
              (t/is (= (count valvonnat)
                       2))

              (t/is (= (->> valvonnat
                            (map :id))
                       [valvonta-not-in-uhkasakkoprosessi valvonta-in-uhkasakkoprosessi]))

              (t/testing "count-valvonnat matches the actual count"
                (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                         2))))))))))


(def toimenpide-types-not-part-of-uhkasakkoprosessi
  "toimenpide-types 0 - 5 are not part of uhkasakkoprosessi"
  (range 0 6))
(def uhkasakkoprosessi-toimenpide-types [6 7 8 9 10 11 12 14 15 16 17 18 19 21])

(t/deftest find-valvonnat-in-every-toimenpide-type-test
  (t/testing "There are valvonnat in every possible toimenpide"
    (doseq [toimenpide-type-id (concat
                                 toimenpide-types-not-part-of-uhkasakkoprosessi
                                 uhkasakkoprosessi-toimenpide-types)
            :let [valvonta-id (valvonta-kaytto/add-valvonta!
                                ts/*db*
                                {:katuosoite (str "Testikatu " toimenpide-type-id)})]]
      ;; Create a toimenpide for valvonta completely disregarding what kind of toimenpide-speficic-data
      ;; the toimenpidetype might have
      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id        valvonta-id
                     :type_id            toimenpide-type-id
                     :create_time        (-> (LocalDate/of 2024 1 7)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :publish_time       (-> (LocalDate/of 2024 1 7)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :deadline_date      (LocalDate/of 2024 2 7)
                     :diaarinumero       (str "ARA-05.03.01-2024-235-" toimenpide-type-id)
                     :type_specific_data {:fine 6100}}))

    (t/testing "and find-valvonnat returns all of them when searching for all, including closed ones"
      (let [query {:limit          100
                   :include-closed true}]
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                 20
                 (count (concat
                          toimenpide-types-not-part-of-uhkasakkoprosessi
                          uhkasakkoprosessi-toimenpide-types))))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   20)))))

    (t/testing "and find-valvonnat returns all of them except the one that is closed (toimenpide-type 5) when closed ones are not included"
      (let [query {:limit 100}]
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                 19))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   19)))))

    (t/testing "and find-valvonnat returns only those that are part of uhkasakkoprosessi when :only-uhkasakkoprosessi is true"
      (let [query {:limit                  100
                   :only-uhkasakkoprosessi true}]
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                 14
                 (count uhkasakkoprosessi-toimenpide-types)))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   14)))))

    (t/testing "and find-valvonnat returns only one when searching by each individual toimenpide-type"
      (doseq [toimenpide-type-id (concat
                                   toimenpide-types-not-part-of-uhkasakkoprosessi
                                   uhkasakkoprosessi-toimenpide-types)
              :let [query {:limit             100
                           :include-closed    true
                           :toimenpidetype-id toimenpide-type-id}
                    results (valvonta-kaytto/find-valvonnat ts/*db* query)]]

        (t/testing (str "for toimenpide-type " toimenpide-type-id)
          (t/is (= (count results)
                   1))
          (t/is (= (->> results first :last-toimenpide :type-id)
                   toimenpide-type-id))

          (t/testing "count-valvonnat matches the actual count"
            (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                     1))))))

    (t/testing "and find-valvonnat returns only the amount of valvonnat that is given as a limit"
      (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* {:limit 2}))
               2)))

    (t/testing "count-valvonnat does not respect limit or offset as it's used to determine the pagination"
      (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* {:include-closed true
                                                                 :limit          2
                                                                 :offset         15}))
               20)))

    (t/testing "and find-valvonnat returns the same results when retrieving 4 items and 2 with offset of 2 twice"
      (let [one-search (valvonta-kaytto/find-valvonnat ts/*db* {:limit 4})
            first-half-of-offset-search (valvonta-kaytto/find-valvonnat ts/*db* {:limit 2})
            second-half-of-offset-search (valvonta-kaytto/find-valvonnat ts/*db* {:limit  2
                                                                                  :offset 2})]
        (t/is (= one-search
                 (concat first-half-of-offset-search second-half-of-offset-search)))))

    (t/testing "and find-valvonnat returns empty list when searching for valvonnat with document template that none has"
      (let [query {:asiakirjapohja-id 4}]
        (t/is (empty? (valvonta-kaytto/find-valvonnat ts/*db* query)))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   0)))))

    (t/testing "and find-valvonnat returns empty list when searching for valvonnat with valvoja as none have one"
      (let [query {:has-valvoja true}]
        (t/is (empty? (valvonta-kaytto/find-valvonnat ts/*db* query)))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   0))))))

  (t/testing "find-valvonnat returns a valvonta that has a toimenpide with a given document template"
    (let [valvonta-id (valvonta-kaytto/add-valvonta!
                        ts/*db*
                        {:katuosoite "Asiakirjapohjakatu"})
          query {:asiakirjapohja-id 3}]
      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id   valvonta-id
                     :type_id       2
                     :template_id   3
                     :create_time   (-> (LocalDate/of 2024 1 7)
                                        (.atStartOfDay (ZoneId/systemDefault))
                                        .toInstant)
                     :publish_time  (-> (LocalDate/of 2024 1 7)
                                        (.atStartOfDay (ZoneId/systemDefault))
                                        .toInstant)
                     :deadline_date (LocalDate/of 2024 2 7)
                     :diaarinumero  "ARA-05.03.01-2024-238"})
      (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
               1))

      (t/testing "count-valvonnat matches the actual count"
        (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                 1)))))

  (let [kayttaja-id (test-kayttajat/insert-virtu-paakayttaja!
                      {:etunimi  "Asian"
                       :sukunimi "Tuntija"
                       :email    "testi@ara.fi"
                       :puhelin  "0504363675457"})]
    (valvonta-kaytto/add-valvonta!
      ts/*db*
      {:katuosoite "Asiakirjapohjakatu"
       :valvoja-id kayttaja-id})

    (t/testing "and find-valvonnat returns a valvonta that has a valvoja when has-valvoja is true"
      (let [query {:has-valvoja true}]
        (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                 1))

        (t/testing "count-valvonnat matches the actual count"
          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   1))))

      (t/testing "and find-valvonnat returns a valvonta when a correct valvoja-id is given"
        (let [query {:valvoja-id kayttaja-id}]
          (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
                   1))

          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   1))))

      (t/testing "and find-valvonnat returns empty list when incorrect valvova-id is given"
        (let [query {:valvoja-id 666}]
          (t/is (empty? (valvonta-kaytto/find-valvonnat ts/*db* query)))

          (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                   0)))))))

(t/deftest only-uhkasakkoprosessi-and-include-closed-test
  (t/testing "Toimenpide that has been in uhkasakkoprosessi but is now closed should be returned when in-uhkasakkoprosessi and include-closed are true"
    (let [valvonta-id (valvonta-kaytto/add-valvonta!
                        ts/*db*
                        {:katuosoite (str "Testikatu")})
          query {:include-closed         true
                 :only-uhkasakkoprosessi true}]
      ;; Create a toimenpide that is part of uhkasakkoprosessi
      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id        valvonta-id
                     :type_id            7
                     :create_time        (-> (LocalDate/of 2023 8 12)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :publish_time       (-> (LocalDate/of 2023 8 12)
                                             (.atStartOfDay (ZoneId/systemDefault))
                                             .toInstant)
                     :deadline_date      (LocalDate/of 2023 8 28)
                     :diaarinumero       "ARA-05.03.01-2023-235"
                     :type_specific_data {:fine 6100}})

      ;; Create valvonnan lopetus toimenpide
      (jdbc/insert! ts/*db*
                    :vk_toimenpide
                    {:valvonta_id  valvonta-id
                     :type_id      5
                     :create_time  (-> (LocalDate/of 2023 8 13)
                                       (.atStartOfDay (ZoneId/systemDefault))
                                       .toInstant)
                     :publish_time (-> (LocalDate/of 2023 8 13)
                                       (.atStartOfDay (ZoneId/systemDefault))
                                       .toInstant)
                     :diaarinumero "ARA-05.03.01-2023-235"})

      (t/is (= (count (valvonta-kaytto/find-valvonnat ts/*db* query))
               1))
      (t/testing "count-valvonnat matches the actual count"
        (t/is (= (:count (valvonta-kaytto/count-valvonnat ts/*db* query))
                 1))))))
