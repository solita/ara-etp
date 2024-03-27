(ns solita.etp.valvonta-kaytto.valvonta-csv-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t]
            [solita.etp.schema.valvonta-kaytto :as valvonta-schema]
            [solita.etp.service.valvonta-kaytto :as valvonta-service]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-data.generators :as generator]
            [solita.etp.test-data.kayttaja :as test-kayttajat]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-system :as ts])
  (:import (java.time LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(def csv-header-line "\"id\";\"rakennustunnus\";\"diaarinumero\";\"katuosoite\";\"postinumero\";\"postitoimipaikka\";\"toimenpide-id\";\"toimenpidetyyppi\";\"aika\";\"valvoja\";\"energiatodistus hankittu\"\n")

(defn handle-output [call-count output line]
  (swap! output conj line)
  (swap! call-count inc))

(t/deftest valvonta-csv-test
  (t/testing "Creating csv when there are no valvonnat returns only the header line"
    (let [output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      (create-csv (partial handle-output call-count output))

      (t/is (= 1 @call-count))
      (t/is (= @output
               [csv-header-line]))))

  (t/testing "Creating csv when there is one open valvonta results in one content line in csv with its data"

    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    "3139000812"
                                                               :valvoja-id        valvoja-id})
          start-timestamp (-> (LocalDate/of 2024 3 18)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      ;; Start the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       0
                                            :create_time   start-timestamp
                                            :publish_time  start-timestamp
                                            :deadline_date (LocalDate/of 2024 3 27)
                                            :diaarinumero  "ARA-05.03.01-2024-159"})
      (create-csv (partial handle-output call-count output))

      (t/is (= 2 @call-count))
      (t/is (= @output
               [csv-header-line
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"])))))

(t/deftest valvonta-csv-toimenpidetype-test
  (t/testing "Every toimenpide created on valvonta results in a row in the csv"
    (let [toimenpidetype-ids [0 1 2 3 4 5 6 7 8 9 10 11 12 14 15 16 17 18 19 21]
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    "3139000812"})]
      (doseq [toimenpidetype-id toimenpidetype-ids]
        (jdbc/insert! ts/*db*
                      :vk_toimenpide
                      {:valvonta_id   valvonta-id
                       :type_id       toimenpidetype-id
                       :create_time   (-> (LocalDate/of 2024 3 18)
                                          (.atStartOfDay (ZoneId/systemDefault))
                                          .toInstant)
                       :publish_time  (-> (LocalDate/of 2024 3 18)
                                          (.atStartOfDay (ZoneId/systemDefault))
                                          .toInstant)
                       :deadline_date (LocalDate/of 2024 2 7)
                       :diaarinumero  "ARA-05.03.01-2024-734"}))

      (let [output (atom [])
            call-count (atom 0)
            create-csv (valvonta-service/csv ts/*db*)]
        (create-csv (partial handle-output call-count output))

        (t/is (= (inc (count toimenpidetype-ids))
                 @call-count)
              "Call count is how many toimenpidetypes there are plus the header row")

        (t/is (= (inc (count toimenpidetype-ids))
                 (count @output))
              "Number of rows in the output is how many toimenpidetypes there are plus the header row")))))

(t/deftest valvonta-csv-generated-valvonnat-test
  (t/testing "Creating csv when there are 100 valvontas returns 101 unique rows with header row included"
    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          valvonnat (repeatedly 100 #(generator/complete {:ilmoituspaikka-id 1
                                                          :valvoja-id        valvoja-id}
                                                         valvonta-schema/ValvontaSave))]
      (doseq [valvonta valvonnat]
        (valvonta-service/add-valvonta! ts/*db* valvonta))

      (let [output (atom [])
            call-count (atom 0)
            create-csv (valvonta-service/csv ts/*db*)]
        (create-csv (partial handle-output call-count output))

        (t/is (= 101 @call-count))

        (t/is (= (first @output)
                 csv-header-line))

        (t/is (= (count (set (rest @output)))
                 100))))))

(t/deftest valvonta-csv-no-energiatodistus-test
  (t/testing "Energiatodistus hankittu column in csv is empty for all rows, when valvonta has been closed without created energiatodistus"
    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    "3139000812"
                                                               :valvoja-id        valvoja-id})
          start-timestamp (-> (LocalDate/of 2024 3 18)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      ;; Start the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       0
                                            :create_time   start-timestamp
                                            :publish_time  start-timestamp
                                            :deadline_date (LocalDate/of 2024 3 27)
                                            :diaarinumero  "ARA-05.03.01-2024-159"})

      ;; Close the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       5
                                            :create_time   (-> (LocalDate/of 2024 3 18)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :publish_time  (-> (LocalDate/of 2024 3 18)
                                                               (.atStartOfDay (ZoneId/systemDefault))
                                                               .toInstant)
                                            :deadline_date (LocalDate/of 2024 02 14)
                                            :diaarinumero  "ARA-05.03.01-2024-159"})

      (create-csv (partial handle-output call-count output))

      (t/is (= 3 @call-count))
      (t/is (= @output
               [csv-header-line
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";2;\"Valvonnan lopetus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"])))))

(t/deftest energiatodistus-hankittu-test
  (t/testing "Energiatodistus hankittu column is set for kehotus because energiatodistus was acquired during it"
    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          rakennustunnus "3139000812"
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    rakennustunnus
                                                               :valvoja-id        valvoja-id})
          start-timestamp (-> (LocalDate/of 2024 3 18)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          kehotus-timestamp (-> (LocalDate/of 2024 3 20)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          close-timestamp (-> (LocalDate/of 2024 3 22)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      ;; Start the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                            :type_id      0
                                            :create_time  start-timestamp
                                            :publish_time start-timestamp
                                            :diaarinumero "ARA-05.03.01-2024-159"})

      ;; Add kehotus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   kehotus-timestamp
                                            :publish_time  kehotus-timestamp
                                            :deadline_date (LocalDate/of 2024 3 27)
                                            :diaarinumero  "ARA-05.03.01-2024-159"})

      ;; Create energiatodistus and sign it
      (let [laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))
            todistus (-> (test-data.energiatodistus/generate-add 2018 true)
                         (assoc-in [:perustiedot :rakennustunnus] rakennustunnus))
            todistus-id (test-data.energiatodistus/insert! [todistus] laatija-id)]
        (test-data.energiatodistus/sign-at-time! todistus-id
                                                 laatija-id
                                                 true
                                                 (-> (LocalDate/of 2024 3 21)
                                                     (.atStartOfDay (ZoneId/systemDefault))
                                                     .toInstant)))

      ;; Close the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                            :type_id      5
                                            :create_time  close-timestamp
                                            :publish_time close-timestamp
                                            :diaarinumero "ARA-05.03.01-2024-159"})

      (create-csv (partial handle-output call-count output))

      (t/is (= 4 @call-count))
      (t/is (= @output
               [csv-header-line
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";2;\"Kehotus\";2024-03-20T02:00;\"Tuntija, Asian\";\"x\"\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";3;\"Valvonnan lopetus\";2024-03-22T02:00;\"Tuntija, Asian\";\n"])
            "All toimenpiteet for the valvonta are present, energiatodistus is marked being created during kehotus toimenpide"))))


(t/deftest energiatodistus-hankittu-on-the-last-day-test
  (t/testing "Energiatodistus hankittu column is set for kehotus when the todistus was acquired on the deadline day"
    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))
          rakennustunnus "3139000812"
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    rakennustunnus
                                                               :valvoja-id        valvoja-id})
          start-timestamp (-> (LocalDate/of 2024 3 18)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          kehotus-timestamp (-> (LocalDate/of 2024 3 20)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          kehotus-deadline (LocalDate/of 2024 3 27)
          close-timestamp (-> (LocalDate/of 2024 3 30)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      ;; Start the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                            :type_id      0
                                            :create_time  start-timestamp
                                            :publish_time start-timestamp
                                            :diaarinumero "ARA-05.03.01-2024-159"})

      ;; Add kehotus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   kehotus-timestamp
                                            :publish_time  kehotus-timestamp
                                            :deadline_date kehotus-deadline
                                            :diaarinumero  "ARA-05.03.01-2024-159"})

      ;; Create energiatodistus and sign it, the signing time is on the same day as the kehotus deadline
      (let [todistus (-> (test-data.energiatodistus/generate-add 2018 true)
                         (assoc-in [:perustiedot :rakennustunnus] rakennustunnus))
            todistus-id (test-data.energiatodistus/insert! [todistus] laatija-id)]
        (test-data.energiatodistus/sign-at-time! todistus-id
                                                 laatija-id
                                                 true
                                                 (-> kehotus-deadline
                                                     (.atStartOfDay (ZoneId/systemDefault))
                                                     .toInstant)))

      ;; Close the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                            :type_id      5
                                            :create_time  close-timestamp
                                            :publish_time close-timestamp
                                            :diaarinumero "ARA-05.03.01-2024-159"})

      (create-csv (partial handle-output call-count output))

      (t/is (= 4 @call-count))
      (t/is (= @output
               [csv-header-line
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";2;\"Kehotus\";2024-03-20T02:00;\"Tuntija, Asian\";\"x\"\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";3;\"Valvonnan lopetus\";2024-03-30T02:00;\"Tuntija, Asian\";\n"])
            "All toimenpiteet for the valvonta are present, energiatodistus is marked being created during kehotus toimenpide"))))

(t/deftest energiatodistus-hankittu-on-the-day-after-deadline-test
  (t/testing "Energiatodistus hankkittu is not set for kehotus when the todistus was acquired the day after the deadline"
    ;; This test exists to check that the exclusivity and the deadline date to timestamp conversion in the implementation
    ;; work as they should
    (let [valvoja-id (test-kayttajat/insert-virtu-paakayttaja!
                       {:etunimi    "Asian"
                        :sukunimi   "Tuntija"
                        :email      "testi@ara.fi"
                        :puhelin    "0504363675457"
                        :titteli-fi "energia-asiantuntija"
                        :titteli-sv "energiexpert"})
          laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))
          rakennustunnus "3139000812"
          valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                               :postinumero       "90100"
                                                               :ilmoituspaikka-id 0
                                                               :rakennustunnus    rakennustunnus
                                                               :valvoja-id        valvoja-id})
          start-timestamp (-> (LocalDate/of 2024 3 18)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          kehotus-timestamp (-> (LocalDate/of 2024 3 20)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
          kehotus-deadline (LocalDate/of 2024 3 27)
          close-timestamp (-> (.plusDays kehotus-deadline 2)
                              (.atStartOfDay (ZoneId/systemDefault))
                              .toInstant)
          output (atom [])
          call-count (atom 0)
          create-csv (valvonta-service/csv ts/*db*)]
      ;; Start the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                            :type_id      0
                                            :create_time  start-timestamp
                                            :publish_time start-timestamp
                                            :diaarinumero "ARA-05.03.01-2024-159"})

      ;; Add kehotus-toimenpide to the valvonta
      (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                            :type_id       2
                                            :create_time   kehotus-timestamp
                                            :publish_time  kehotus-timestamp
                                            :deadline_date kehotus-deadline
                                            :diaarinumero  "ARA-05.03.01-2024-159"})

      ;; Create energiatodistus and sign it, the signing time is the next day from the deadline
      (let [todistus (-> (test-data.energiatodistus/generate-add 2018 true)
                         (assoc-in [:perustiedot :rakennustunnus] rakennustunnus))
            todistus-id (test-data.energiatodistus/insert! [todistus] laatija-id)]
        (test-data.energiatodistus/sign-at-time! todistus-id
                                                 laatija-id
                                                 true
                                                 (-> (.plusDays kehotus-deadline 1)
                                                     (.atStartOfDay (ZoneId/systemDefault))
                                                     .toInstant)))

      (create-csv (partial handle-output call-count output))

      ;; Csv doesn't show the energiatodistus as created
      (t/is (= 3 @call-count))
      (t/is (= @output
               [csv-header-line
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"
                "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";2;\"Kehotus\";2024-03-20T02:00;\"Tuntija, Asian\";\n"])
            "All toimenpiteet for the valvonta are present, energiatodistus is not marked as being created. Exclusive end of the range works correctly with the deadline.")

      (t/testing "after the valvonta has been closed, the energiatodistus shows as having been acquired during kehotus"
        ;; Close the valvonta after the energiatodistus has been acquired
        (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id  valvonta-id
                                              :type_id      5
                                              :create_time  close-timestamp
                                              :publish_time close-timestamp
                                              :diaarinumero "ARA-05.03.01-2024-159"})

        ;; Reset the test state
        (reset! call-count 0)
        (reset! output [])

        ;; Create the csv again
        (create-csv (partial handle-output call-count output))

        ;; Csv now shows the energiatodistus as having been created during kehotus, as it was acquired after kehotus creation
        ;; and before the next toimenpide, even if it was after kehotus deadline
        (t/is (= 4 @call-count))
        (t/is (= @output
                 [csv-header-line
                  "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\"Tuntija, Asian\";\n"
                  "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";2;\"Kehotus\";2024-03-20T02:00;\"Tuntija, Asian\";\"x\"\n"
                  "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";3;\"Valvonnan lopetus\";2024-03-29T02:00;\"Tuntija, Asian\";\n"])
              "All toimenpiteet for the valvonta are present, energiatodistus is marked being created during kehotus as it's created before the next toimenpide")))))