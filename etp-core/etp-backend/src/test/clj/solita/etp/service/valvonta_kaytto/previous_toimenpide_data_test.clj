(ns solita.etp.service.valvonta-kaytto.previous-toimenpide-data-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t]
            [solita.etp.service.valvonta-kaytto :as valvonta-service]
            [solita.etp.service.valvonta-kaytto.previous-toimenpide-data :as previous-toimenpide]
            [solita.etp.test-system :as ts])
  (:import (java.time LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(t/deftest previous-toimenpide-data-for-decision-order-hearing-letter-test
  (t/testing "Käskypäätös / kuulemiskirje needs kehotus pvm, deadline and varoitus deadline"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)]
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

      (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 7} valvonta-id)
               {:kehotus-pvm        "12.06.2023"
                :kehotus-maarapaiva "12.07.2023"
                :varoitus-maarapaiva "13.08.2023"}))))

  (t/testing "When there are multiple kehotus and varoitus toimenpide, the newest dates are found"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)]

      ;; Add old kehotus-toimenpide
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   (-> (LocalDate/of 2022 6 11)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :publish_time  (-> (LocalDate/of 2022 6 11)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :deadline_date (LocalDate/of 2022 7 11)})

      ;; Add old varoitus-toimenpide
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       3
                                            :create_time   (-> (LocalDate/of 2022 7 12)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :publish_time  (-> (LocalDate/of 2022 7 12)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :deadline_date (LocalDate/of 2022 8 12)})

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

      ;; The newest kehotus creation date and deadline and varoitus creation date
      ;; and deadline are found and formatted correctly
      ;; There is no kuulemiskirje toimenpide yet
      (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 7} valvonta-id)
               {:kehotus-pvm                   "12.06.2023"
                :kehotus-maarapaiva            "12.07.2023"
                :varoitus-maarapaiva           "13.08.2023"})))))

(t/deftest previous-toimenpide-data-for-decision-order-actual-decision-test
  (t/testing "Käskypäätös / varsinainen päätös needs kehotus deadline, varoitus deadline, käskypäätös / kuulemiskirje pvm, diaarinumero and fine"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})
          kehotus-timestamp (-> (LocalDate/of 2023 6 12)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          varoitus-timestamp (-> (LocalDate/of 2023 7 13)
                                 (.atStartOfDay (ZoneId/systemDefault))
                                 .toInstant)
          kuulemiskirje-timestamp (-> (LocalDate/of 2023 8 14)
                                      (.atStartOfDay (ZoneId/systemDefault))
                                      .toInstant)]
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

      ;; Add kuulemiskirje-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       7
                                            :create_time   kuulemiskirje-timestamp
                                            :publish_time  kuulemiskirje-timestamp
                                            :deadline_date (LocalDate/of 2023 8 28)
                                            :diaarinumero "ARA-XKC-24232"
                                            :type_specific_data {:fine 800}})

      (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 8} valvonta-id)
               {:kuulemiskirje-diaarinumero "ARA-XKC-24232"
                :kuulemiskirje-pvm "14.08.2023"
                :kuulemiskirje-fine 800
                :kehotus-maarapaiva "12.07.2023"
                :varoitus-maarapaiva "13.08.2023"}))

      (t/testing "When multiple kuulemiskirje exists for the given valvonta, the details for the newest one are returned"
        (jdbc/insert! ts/*db*
                      :vk_toimenpide
                      {:valvonta_id        valvonta-id
                       :type_id            7
                       :create_time        (-> (LocalDate/of 2023 8 10)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :publish_time       (-> (LocalDate/of 2023 8 10)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :deadline_date      (LocalDate/of 2023 8 28)
                       :diaarinumero       "ARA-05.03.01-2023-235"
                       :type_specific_data {:fine 6100}})

        (jdbc/insert! ts/*db*
                      :vk_toimenpide
                      {:valvonta_id        valvonta-id
                       :type_id            7
                       :create_time        (-> (LocalDate/of 2023 8 19)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :publish_time       (-> (LocalDate/of 2023 8 19)
                                               (.atStartOfDay (ZoneId/systemDefault))
                                               .toInstant)
                       :deadline_date      (LocalDate/of 2023 8 28)
                       :diaarinumero       "ARA-05.03.01-2023-245"
                       :type_specific_data {:fine 3200}})
        (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 8} valvonta-id)
                 {:kuulemiskirje-diaarinumero  "ARA-05.03.01-2023-245"
                  :kuulemiskirje-pvm "19.08.2023"
                  :kuulemiskirje-fine 3200
                  :kehotus-maarapaiva "12.07.2023"
                  :varoitus-maarapaiva "13.08.2023"}))))))

(t/deftest previous-toimenpide-data-for-penalty-decision-actual-decision-test
  (t/testing "Sakkopäätös / varsinainen päätös needs käskypäätös / varsinainen päätös pvm, deadline, diaarinumero and fine and sakkopäätös / kuulemiskirje pvm and diaarinumero"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 9 14)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)
          sakkopaatos-kuulemiskirje-timestamp (-> (LocalDate/of 2023 11 1)
                                                  (.atStartOfDay (ZoneId/systemDefault))
                                                  .toInstant)]
      ;; Add varsinainen päätös -toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            8
                                            :create_time        varsinainen-paatos-timestamp
                                            :publish_time       varsinainen-paatos-timestamp
                                            :deadline_date      (LocalDate/of 2023 10 28)
                                            :diaarinumero       "ARA-2132-X"
                                            :type_specific_data {:fine 902}})

      ;; Add sakkopäätös / kuulemiskirje -toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       14
                                            :create_time   sakkopaatos-kuulemiskirje-timestamp
                                            :publish_time  sakkopaatos-kuulemiskirje-timestamp
                                            :diaarinumero  "ARA 21345-XSW"
                                            :deadline_date (LocalDate/of 2023 11 12)})

      (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 15} valvonta-id)
               {:varsinainen-paatos-pvm                 "14.09.2023"
                :varsinainen-paatos-maarapaiva          "28.10.2023"
                :varsinainen-paatos-diaarinumero        "ARA-2132-X"
                :varsinainen-paatos-fine                902
                :sakkopaatos-kuulemiskirje-pvm          "01.11.2023"
                :sakkopaatos-kuulemiskirje-diaarinumero "ARA 21345-XSW"})))))

(t/deftest previous-toimenpide-data-for-penalty-decision-hearing-letter-test
  (t/testing "Sakkopäätös / kuulemiskirje needs käskypäätös / varsinainen päätös pvm and deadline"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite "Testitie 5"})
          varsinainen-paatos-timestamp (-> (LocalDate/of 2023 9 14)
                                           (.atStartOfDay (ZoneId/systemDefault))
                                           .toInstant)]
      ;; Add varsinainen päätös -toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id        valvonta-id
                                            :type_id            8
                                            :create_time        varsinainen-paatos-timestamp
                                            :publish_time       varsinainen-paatos-timestamp
                                            :deadline_date      (LocalDate/of 2023 10 28)
                                            :diaarinumero       "ARA-2132-X"
                                            :type_specific_data {:fine 902}})

      (t/is (= (previous-toimenpide/formatted-previous-toimenpide-data ts/*db* {:type-id 14} valvonta-id)
               {:varsinainen-paatos-pvm        "14.09.2023"
                :varsinainen-paatos-maarapaiva "28.10.2023"})))))