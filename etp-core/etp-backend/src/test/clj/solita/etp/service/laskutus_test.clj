(ns solita.etp.service.laskutus-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [solita.common.map :as xmap]
            [solita.common.xml :as xml]
            [solita.common.sftp :as sftp]
            [solita.etp.config :as config]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.yritys :as yritys-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.laskutus :as laskutus-service]
            [solita.etp.service.file :as file-service]
            [solita.common.smtp-test :as smtp-test])
  (:import (java.time Instant OffsetDateTime ZoneOffset)))

(t/use-fixtures :each ts/fixture)

;; There are 2 yritys.
;; There are 5 laatijat.
;; Laatija 1 belongs to yritys 1.
;; Laatija 2 belongs to yritys 2.
;; Laatijat 3, 4 and 5 have no yritys.

;; Energiatodistus 1 is signed by laatija 1 during last month => laskutettava.
;; Energiatodistus 2 is signed by laatija 1 during last month and has replaced
;; energiatodistus 1 in more than 7 days => laskutettava.
;; Energiatodistus 3 is signed by laatija 1 during last month => laskutettava.
;; Energiatodistus 4 is signed by laatija 1 during last month and has replaced
;; energiatodistus 3 in less than 7 days => not laskutettava.
;; Energiatodistus 5 is signed by laatija 1 during last month => laskutettava.
;; Energiatodistus 6 is signed by laatija 1 three months ago but has already
;; been laskutettu => not laskutettava.
;; Energiatodistus 7 is signed by laatija 1 three months ago => not
;; laskuttettava.
;; Energiatodistus 8 is signed by laatija 2 during last month and has replaced
;; energiatodistus 5 (by laatija 1) in less than 7 days => laskutettava.
;; Energiatodistus 9 is signed by laatija 3 during last month => laskutettava.
;; Energiatodistus 10 is signed by laatija 4 during last month => laskutettava.
;; Energiatodistus 11 is not signed. => not laskutettava.

;; Laatija 5 gets all extra energiatodistukset, which are signed during last
;; month => laskutettava.

;; All of these tests expect asiakastunnus to match id, which is true in this
;; test data set but not in other environments.

(defn test-data-set [extra-count]
  (let [laatijat (laatija-test-data/generate-and-insert! 5)
        laatija-ids (-> laatijat keys sort)
        yritys-adds (yritys-test-data/generate-adds 2)
        yritys-ids (->> (interleave laatija-ids yritys-adds)
                        (partition 2)
                        (mapcat #(yritys-test-data/insert!
                                  [(second %)]
                                  (first %))))
        energiatodistus-adds (->> (interleave
                                   (energiatodistus-test-data/generate-adds
                                    5
                                    2013
                                    true)
                                   (energiatodistus-test-data/generate-adds
                                    (+ 6 extra-count)
                                    2018
                                    true))
                                  (interleave (concat (repeat 7 (first yritys-ids))
                                                      [(second yritys-ids)]
                                                      (repeat nil)))
                                  (partition 2)
                                  (map #(assoc (second %)
                                               :laskutusosoite-id
                                               (or (first %) -1))))
        energiatodistus-laatija-ids (concat (repeat 7 (first laatija-ids))
                                            [(second laatija-ids)
                                             (nth laatija-ids 2)
                                             (nth laatija-ids 3)]
                                            (repeat (nth laatija-ids 4)))
        energiatodistus-ids (->> (interleave energiatodistus-laatija-ids
                                             energiatodistus-adds)
                                 (partition 2)
                                 (mapcat #(energiatodistus-test-data/insert!
                                           [(second %)]
                                           (first %)))
                                 doall)]
    (doseq [[laatija-id energiatodistus-id] (->> (interleave energiatodistus-laatija-ids
                                                             energiatodistus-ids)
                                                 (partition 2)
                                                 (take 10))]
      (energiatodistus-service/start-energiatodistus-signing! ts/*db*
                                                              {:id laatija-id}
                                                              energiatodistus-id)
      (energiatodistus-service/end-energiatodistus-signing! ts/*db*
                                                            ts/*aws-s3-client*
                                                            {:id laatija-id}
                                                            energiatodistus-id
                                                            {:skip-pdf-signed-assert? true}))
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = date_trunc('month', now()) - interval '1 month' WHERE id IN (1, 3, 4, 5, 6, 8, 9, 10)"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = date_trunc('month', now()) - interval '10 days' WHERE id = 2"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = now() - interval '3 month' WHERE id = 7"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET allekirjoitusaika = now() - interval '1 month' WHERE id >= 12"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET laskutusaika = now() WHERE id = 6"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET korvattu_energiatodistus_id = 1 WHERE id = 2"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET korvattu_energiatodistus_id = 3 WHERE id = 4"])
    (jdbc/execute! ts/*db* ["UPDATE energiatodistus SET korvattu_energiatodistus_id = 5 WHERE id = 8"])

    {:laatijat laatijat
     :yritykset (zipmap yritys-ids yritys-adds)
     :energiatodistukset (zipmap energiatodistus-ids energiatodistus-adds)}))

(t/deftest safe-subs-test
  (t/is (= nil (laskutus-service/safe-subs nil 1 2)))
  (t/is (= "e" (laskutus-service/safe-subs "hello" 1 2)))
  (t/is (= "ello" (laskutus-service/safe-subs "hello" 1 100)))
  (t/is (= "hello" (laskutus-service/safe-subs "hello" -5 100)))
  (t/is (= "" (laskutus-service/safe-subs "hello" -5 -2)))
  (t/is (= "" (laskutus-service/safe-subs "hello" 1 0))))

(t/deftest find-kuukauden-laskutus-test
  (let [_ (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)]
    (t/is (= #{1 2 3 5 8 9 10} (->> laskutus (map :energiatodistus-id) set)))))

(t/deftest asiakastiedot-test
  (let [{:keys [yritykset laatijat]} (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        asiakastiedot (laskutus-service/asiakastiedot laskutus)]
    (t/is (= 4 (count asiakastiedot)))
    (t/is (= (set (concat
                   (->> (select-keys laatijat [3 4])
                        (map (fn [[id {:keys [etunimi sukunimi henkilotunnus]}]]
                               {:laskutus-asiakastunnus (format "L0%08d" id)
                                :nimi (str etunimi " " sukunimi)
                                :henkilotunnus henkilotunnus})))
                   (map (fn [[id {:keys [nimi henkilotunnus]}]]
                          {:laskutus-asiakastunnus (format "L1%08d" id)
                           :nimi nimi
                           :henkilotunnus (-> laatijat (get id) :henkilotunnus)})
                        yritykset)))
             (->> asiakastiedot
                  (map #(select-keys % [:laskutus-asiakastunnus :nimi :henkilotunnus]))
                  set)))))

(t/deftest asiakastiedot-xml-test
  (let [{:keys [yritykset]} (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        asiakastiedot (laskutus-service/asiakastiedot laskutus)
        yritys-id (-> yritykset keys sort first)
        yritys (get yritykset yritys-id)
        laskutus-asiakastunnus (format "L1%08d" yritys-id)
        asiakastieto (->> asiakastiedot
                          (filter #(= (:laskutus-asiakastunnus %)
                                      (format "L1%08d" yritys-id)))
                          first)
        xml-str (-> asiakastieto
                    laskutus-service/asiakastieto-xml
                    xml/emit-str)]
    (t/is (str/includes? xml-str (str "<AsiakasTunnus>"
                                      laskutus-asiakastunnus
                                      "</AsiakasTunnus")))
    (t/is (str/includes? xml-str (str "<YritysTunnus>"
                                      (:ytunnus yritys)
                                      "</YritysTunnus")))
    (t/is (str/includes? xml-str "<KumppaniNro>ETP</KumppaniNro>"))))

(t/deftest energiatodistus-tilausrivi-text-test
  (let [instant (Instant/parse "2021-01-01T10:10:10.Z")]
    (t/is (= "Energicertifikat 100, datum: 01.01.2021"
             (laskutus-service/energiatodistus-tilausrivi-text 100 instant nil 1)))
    (t/is (= "Energicertifikat 10, datum: 01.01.2021, referens: hello"
             (laskutus-service/energiatodistus-tilausrivi-text 10 instant "hello" 1)))
    (t/is (= "EPC 1, date: 01.01.2021"
             (laskutus-service/energiatodistus-tilausrivi-text 1 instant nil 2)))
    (t/is (= "EPC 123, date: 01.01.2021, reference: ref123"
             (laskutus-service/energiatodistus-tilausrivi-text 123 instant "ref123" 2)))
    (t/is (= "Energiatodistus 99, pvm: 01.01.2021"
             (laskutus-service/energiatodistus-tilausrivi-text 99 instant nil 0)))
    (t/is (= "Energiatodistus 99, pvm: 01.01.2021, viite: 1234"
             (laskutus-service/energiatodistus-tilausrivi-text 99 instant "1234" 0)))))

(t/deftest laskutustiedot-test
  (let [{:keys [yritykset laatijat energiatodistukset]} (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        laskutustiedot (laskutus-service/laskutustiedot laskutus)
        yritys-id (-> yritykset keys sort first)
        yritys-laatija-id (-> laatijat keys sort first)
        yritys-laatija (get laatijat yritys-laatija-id)
        yritys-laskutus-asiakastunnus (format "L1%08d" yritys-id)
        yritys-laskutustieto (->> laskutustiedot
                                  (filter #(= (:laskutus-asiakastunnus %)
                                              yritys-laskutus-asiakastunnus))
                                  first)
        yritys-laatija-energiatodistukset (get-in yritys-laskutustieto
                                                  [:laatijat
                                                   yritys-laatija-id
                                                   :energiatodistukset])
        laatija-id (-> laatijat keys sort (nth 3))
        laatija (get laatijat laatija-id)
        laatija-laskutus-asiakastunnus (format "L0%08d" laatija-id)
        laatija-laskutustieto (->> laskutustiedot
                                   (filter #(= (:laskutus-asiakastunnus %)
                                               laatija-laskutus-asiakastunnus))
                                   first)
        laatija-energiatodistukset (get-in laatija-laskutustieto
                                           [:laatijat
                                            laatija-id
                                            :energiatodistukset])
        energiatodistus-ids (-> energiatodistukset keys sort)]
    (t/is (= 4 (count laskutustiedot)))
    (t/is (= {:laskutus-asiakastunnus yritys-laskutus-asiakastunnus
              :laatijat {yritys-laatija-id
                         {:nimi (str (:etunimi yritys-laatija)
                                     " "
                                     (:sukunimi yritys-laatija))}}}
             (-> yritys-laskutustieto
                 (dissoc :laskutuskieli)
                 (xmap/dissoc-in [:laatijat
                                  yritys-laatija-id
                                  :energiatodistukset]))))
    (t/is (= #{1 2 3 5}
             (set (map :id yritys-laatija-energiatodistukset))))
    (t/is (= {:laskutus-asiakastunnus laatija-laskutus-asiakastunnus
              :laatijat {laatija-id {:nimi (str (:etunimi laatija)
                                                " "
                                                (:sukunimi laatija))}}}
             (-> laatija-laskutustieto
                 (dissoc :laskutuskieli)
                 (xmap/dissoc-in [:laatijat
                                  laatija-id
                                  :energiatodistukset]))))
    (t/is (= #{10}
             (set (map :id laatija-energiatodistukset))))))

(defn tilausrivi-pattern [id allekirjoitusaika]
  (re-pattern (str "<TilausriviTekstiTyyppi><Teksti>"
                   "(Energiatodistus|Energicertifikat|EPC) "
                   id
                   ".*"
                   allekirjoitusaika)))


(def laskupvm-cases
  [{:now                (Instant/from (OffsetDateTime/of 2023 12 1 4 0 0 0 (ZoneOffset/ofTotalSeconds 0)))
    :laskupvm-expected? false}
   {:now                (Instant/from (OffsetDateTime/of 2024 1 1 4 0 0 0 (ZoneOffset/ofTotalSeconds 0)))
    :laskupvm-expected? true}])

(t/deftest laskutustiedot-xml-test
  (let [{:keys [yritykset]} (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        laskutustiedot (laskutus-service/laskutustiedot laskutus)
        yritys-id (-> yritykset keys sort first)
        laskutus-asiakastunnus (format "L1%08d" yritys-id)
        laskutustieto (->> laskutustiedot
                           (filter #(= (:laskutus-asiakastunnus %)
                                       (format "L1%08d" yritys-id)))
                           first)
        laskutustieto-energiatodistukset (-> laskutustieto
                                             :laatijat
                                             vals
                                             first
                                             :energiatodistukset)]
    (run! (fn [{:keys [now laskupvm-expected?]}]
            (let [xml-str (->> laskutustieto
                               (laskutus-service/laskutustieto-xml now)
                               xml/emit-str)]
              (t/is (str/includes? xml-str (str "<AsiakasNro>"
                                                laskutus-asiakastunnus
                                                "</AsiakasNro")))
              (t/is (str/includes? xml-str "<TilausMaaraArvo>4</TilausMaaraArvo>"))
              (t/is (re-find (tilausrivi-pattern (-> laskutustieto-energiatodistukset
                                                     first
                                                     :id)
                                                 (->> laskutustieto-energiatodistukset
                                                      first
                                                      :allekirjoitusaika
                                                      (.format laskutus-service/date-formatter-fi)))
                             xml-str))
              (t/is (re-find (tilausrivi-pattern (-> laskutustieto-energiatodistukset
                                                     second
                                                     :id)
                                                 (->> laskutustieto-energiatodistukset
                                                      second
                                                      :allekirjoitusaika
                                                      (.format laskutus-service/date-formatter-fi)))
                             xml-str))
              (if laskupvm-expected?
                (t/testing (str "Test that LaskuPvm is present, as expected on " now)
                  (t/is (str/includes? xml-str "<LaskuPvm>")))
                (t/testing (str "Test that LaskuPvm is absent, as expected on " now)
                  (t/is (not (str/includes? xml-str "<LaskuPvm>")))))

              (t/is (str/includes? xml-str "<KumppaniNro>ETP</KumppaniNro>"))))
          laskupvm-cases)))

(t/deftest tasmaytysraportti-test
  (let [{:keys [yritykset laatijat]} (test-data-set 0)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        tasmaytysraportti (laskutus-service/tasmaytysraportti laskutus (Instant/now))
        yritys-id (-> yritykset keys sort first)
        yritys (get yritykset yritys-id)
        yritys-laskutus-asiakastunnus (format "L1%08d" yritys-id)
        laatija-id (-> laatijat keys sort (nth 3))
        laatija (get laatijat laatija-id)
        laatija-laskutus-asiakastunnus (format "L0%08d" laatija-id)]
    (t/is (= 16 (count tasmaytysraportti)))
    (t/is (= "ETP" (ffirst tasmaytysraportti)))
    (t/is (= "ARA" (-> tasmaytysraportti second first)))
    (t/is (= [["Asiakkaiden lukumäärä yhteensä" nil {:v 4 :align :left}]
              ["Myyntitilausten lukumäärä yhteensä" nil {:v 4 :align :left}]
              ["Velotusmyyntitilausten lukumäärä yhteensä" nil {:v 4 :align :left}]
              ["Energiatodistusten lukumäärä yhteensä" nil {:v 7 :align :left}]
              ["Hyvitystilausten lukumäärä yhteensä" nil {:v 0 :align :left}]
              ["Siirrettyjen liitetiedostojen lukumäärä" nil {:v 0 :align :left}]]
             (->> tasmaytysraportti
                  (drop 3)
                  (take 6))))
    (t/is (= [{:v "Tilauslaji" :align :center}
              {:v "Asiakkaan numero" :align :center}
              {:v "Asiakkaan nimi" :align :center}
              {:v "Laskutettava nimike" :align :center}
              {:v "KPL" :align :center}]
             (nth tasmaytysraportti 11)))
    (t/is (->> tasmaytysraportti (drop 12) (map #(-> % first :v)) (every? #(= % "Z001"))))
    (t/is (->> tasmaytysraportti (drop 12) (map #(-> % (nth 3) :v)) (every? #(= % "RA0001"))))

    (t/is (= laatija-laskutus-asiakastunnus
             (-> tasmaytysraportti (nth 13) second :v)))
    (t/is (= (str (:etunimi laatija) " " (:sukunimi laatija))
             (-> tasmaytysraportti (nth 13) (nth 2) :v)))
    (t/is (= 1 (-> tasmaytysraportti (nth 13) last :v)))

    (t/is (= yritys-laskutus-asiakastunnus (-> tasmaytysraportti (nth 14) second :v)))
    (t/is (= (:nimi yritys) (-> tasmaytysraportti (nth 14) (nth 2) :v)))
    (t/is (= 1 (-> tasmaytysraportti (nth 15) last :v)))))

(t/deftest write-tasmaytysraportti-file!-test
  (test-data-set 0)
  (let [now (Instant/now)
        laskutus (laskutus-service/find-kuukauden-laskutus ts/*db*)
        tasmaytysraportti-file (-> laskutus
                                   (laskutus-service/tasmaytysraportti now)
                                   (laskutus-service/write-tasmaytysraportti-file! now))]
    (t/is (-> tasmaytysraportti-file .exists true?))
    (io/delete-file tasmaytysraportti-file)))

(t/deftest xml-filename-test
  (t/is (= "some-prefix20210114040101123.xml"
           (laskutus-service/xml-filename (Instant/parse "2021-01-14T02:01:01.00Z")
                                          "some-prefix"
                                          123))))

(t/deftest file-key-prefix-test
  (t/is (= "laskutus/2021/20211101121530/"
           (laskutus-service/file-key-prefix (Instant/parse "2021-11-01T10:15:30.00Z") false))))

(t/deftest ^:eftest/synchronized do-kuukauden-laskutus-test
  (test-data-set 0)
  (smtp-test/empty-email-directory!)
  (let [{:keys [started-at]} (with-redefs [laskutus-service/sleep-between-asiakastiedot-and-laskutustiedot 500]
                               (laskutus-service/do-kuukauden-laskutus
                                ts/*db*
                                ts/*aws-s3-client*
                                false))
        file-key-prefix (laskutus-service/file-key-prefix started-at false)]
    (t/is (zero? (count (laskutus-service/find-kuukauden-laskutus ts/*db*))))
    (with-open [sftp-connection (sftp/connect! config/laskutus-sftp-host
                                               config/laskutus-sftp-port
                                               config/laskutus-sftp-username
                                               config/laskutus-sftp-password
                                               config/known-hosts-path)]
      (try
        (let [asiakastieto-filenames (sftp/files-in-dir
                                      sftp-connection
                                      laskutus-service/asiakastieto-destination-dir)
              laskutustieto-filenames (sftp/files-in-dir
                                       sftp-connection
                                       laskutus-service/laskutustieto-destination-dir)
              tasmaytysraportti-filename (str "tasmaytysraportti-"
                                              (.format laskutus-service/time-formatter-file
                                                       started-at)
                                              ".pdf")
              tasmaytysraportti-email (->> (smtp-test/email-directory-files)
                                           (map slurp)
                                           (filter #(str/includes? % "tasmaytysraportti"))
                                           first)]
          (t/is (some? tasmaytysraportti-email))
          (t/is (= 4 (count asiakastieto-filenames)))
          (t/is (= 4 (count laskutustieto-filenames)))
          (t/is (every? #(re-matches #"asiakastieto_etp_ara_.+\.xml" %)
                        asiakastieto-filenames))
          (t/is (every? #(re-matches #"laskutustieto_etp_ara_.+\.xml" %)
                        laskutustieto-filenames))
          (t/is (every? #(file-service/find-file ts/*aws-s3-client* %)
                        (->> (concat laskutustieto-filenames)
                             (map #(str file-key-prefix %)))))
          (t/is (every? #(file-service/find-file ts/*aws-s3-client* %)
                        (->> (concat asiakastieto-filenames)
                             (map #(str file-key-prefix %)))))
          (t/is (file-service/find-file
                 ts/*aws-s3-client*
                 (str file-key-prefix tasmaytysraportti-filename)))
          (t/is (str/includes? tasmaytysraportti-email
                               (str "filename=" tasmaytysraportti-filename))))
        (finally (sftp/delete! sftp-connection
                               (str laskutus-service/asiakastieto-destination-dir "*"))
                 (sftp/delete! sftp-connection
                               (str laskutus-service/laskutustieto-destination-dir "*")))))))

;; Uncomment to test performance and robustness locally
#_(t/deftest ^:eftest/synchronized performance-test
  (smtp-test/empty-email-directory!)
  (let [extra-count 100000
        {:keys [laatijat energiatodistukset]} (test-data-set extra-count)
        start-time (System/currentTimeMillis)
        _ (with-redefs [laskutus-service/sleep-between-asiakastiedot-and-laskutustiedot 0]
            (laskutus-service/do-kuukauden-laskutus
             ts/*db*
             ts/*aws-s3-client*
             false))
        end-time (System/currentTimeMillis)]
    (println (format "Laskutusajo took %.3f seconds" (-> (- end-time start-time) (/ 1000.0))))
    (with-open [sftp-connection (sftp/connect! config/laskutus-sftp-host
                                               config/laskutus-sftp-port
                                               config/laskutus-sftp-username
                                               config/laskutus-sftp-password
                                               config/known-hosts-path)]
      (try
        (t/is (= 4
                 (count (sftp/files-in-dir
                         sftp-connection
                         laskutus-service/asiakastieto-destination-dir))))
        (t/is (zero? (count (laskutus-service/find-kuukauden-laskutus ts/*db*))))

        (finally
          (sftp/delete! sftp-connection
                        (str laskutus-service/asiakastieto-destination-dir "*")))))))
