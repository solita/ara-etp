(ns solita.etp.valvonta-kaytto.valvonta-csv-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :as t]
            [solita.common.time :as time]
            [solita.etp.schema.valvonta-kaytto :as valvonta-schema]
            [solita.etp.service.valvonta-kaytto :as valvonta-service]
            [solita.etp.test-data.generators :as generator]
            [solita.etp.test-data.kayttaja :as test-kayttajat]
            [solita.etp.test-system :as ts])
  (:import (java.time Clock LocalDate ZoneId)))

(t/use-fixtures :each ts/fixture)

(def csv-header-line "\"id\";\"rakennustunnus\";\"diaarinumero\";\"katuosoite\";\"postinumero\";\"postitoimipaikka\";\"toimenpide-id\";\"toimenpidetyyppi\";\"aika\";\"valvoja\"\n")

(defn handle-output [call-count output line]
  (swap! output conj line)
  (swap! call-count inc))

(t/deftest valvonta-csv-test
  (t/testing "Creating csv when there are no valvonnat returns only the header line"
    (let [output (atom [])
          call-count (atom 0)
          csv-producer (valvonta-service/csv ts/*db*)]
      (csv-producer (partial handle-output call-count output))

      (t/is (= 1 @call-count))
      (t/is (= @output
               [csv-header-line]))))

  (t/testing "Creating csv when there is one open valvonta contains one content line in csv with its data"
    (with-bindings {#'time/clock (Clock/fixed (-> (LocalDate/of 2024 3 18)
                                                  (.atStartOfDay time/timezone)
                                                  .toInstant)
                                              time/timezone)}
      (let [valvonta-id (valvonta-service/add-valvonta! ts/*db* {:katuosoite        "Testitie 5"
                                                                 :postinumero       "90100"
                                                                 :ilmoituspaikka-id 0
                                                                 :rakennustunnus    "3139000812"})
            start-timestamp (-> (LocalDate/of 2024 3 18)
                                (.atStartOfDay (ZoneId/systemDefault))
                                .toInstant)
            output (atom [])
            call-count (atom 0)
            csv-producer (valvonta-service/csv ts/*db*)]
        ;; Start the valvonta
        (jdbc/insert! ts/*db* :vk_toimenpide {:valvonta_id   valvonta-id
                                              :type_id       0
                                              :create_time   start-timestamp
                                              :publish_time  start-timestamp
                                              :deadline_date (LocalDate/of 2024 3 27)
                                              :diaarinumero  "ARA-05.03.01-2024-159"})
        (csv-producer (partial handle-output call-count output))

        (t/is (= 2 @call-count))
        (t/is (= @output
                 [csv-header-line
                  "1;\"3139000812\";\"ARA-05.03.01-2024-159\";\"Testitie 5\";\"90100\";\"OULU\";1;\"Valvonnan aloitus\";2024-03-18T02:00;\n"]))))))

(t/deftest valvonta.csv-generated-valvonnat-test
  (t/testing "Creating csv when there are 100 valvontas returns 101 unique rows with header row included"
    (test-kayttajat/insert-virtu-paakayttaja!
      {:etunimi  "Asian"
       :sukunimi "Tuntija"
       :email    "testi@ara.fi"
       :puhelin  "0504363675457"
       :titteli-fi "energia-asiantuntija"
       :titteli-sv "energiexpert"})
    (let [valvonnat (repeatedly 100 #(generator/complete {:ilmoituspaikka-id 1
                                                          :valvoja-id        1}
                                                         valvonta-schema/ValvontaSave))]
      (doseq [valvonta valvonnat]
        ;; Voiko tämän tehdä batch-insertinä, että olisi nopeampi? Onko mitään väliä?
        (valvonta-service/add-valvonta! ts/*db* valvonta))

      (let [output (atom [])
            call-count (atom 0)
            csv-producer (valvonta-service/csv ts/*db*)]
        (csv-producer (partial handle-output call-count output))

        (t/is (= 101 @call-count))

        (t/is (= (first @output)
                 csv-header-line))

        (t/is (= (count (set (rest @output)))
                 100))))))
