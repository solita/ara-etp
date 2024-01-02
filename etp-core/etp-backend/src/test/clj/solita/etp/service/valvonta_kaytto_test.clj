(ns solita.etp.service.valvonta-kaytto-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.valvonta-kaytto :as valvonta-kaytto-schema]
            [solita.etp.service.valvonta-kaytto :as valvonta-kaytto]
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
                 :allow-comments       false}
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
                 :allow-comments       false}
                {:id                   5
                 :label-fi             "Valvonnan lopetus"
                 :label-sv             "Valvonnan lopetus (sv)"
                 :valid                true
                 :manually-deliverable false
                 :allow-comments       false}
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
                 :allow-comments       true}])))

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
