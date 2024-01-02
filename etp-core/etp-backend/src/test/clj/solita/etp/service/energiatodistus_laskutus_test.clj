(ns solita.etp.service.energiatodistus-laskutus-test
  (:require [clojure.test :as t]
            [solita.etp.test :as etp-test]
            [solita.etp.whoami :as whoami]
            [solita.etp.service.energiatodistus-test :as energiatodistus-test]
            [solita.etp.service.energiatodistus :as service]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.yritys :as yritys-test-data]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest define-yritys-laskutus
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        yritys-id (-> (yritys-test-data/generate-and-insert! 1 laatija-id) keys first)
        energiatodistukset
        (map #(assoc % :laskutusosoite-id yritys-id)
             [(energiatodistus-test-data/generate-add 2013 true)
              (energiatodistus-test-data/generate-add 2018 true)
              (energiatodistus-test-data/generate-add 2013 false)
              (energiatodistus-test-data/generate-add 2018 false)])]

    (doseq [[energiatodistus id]
            (map vector energiatodistukset
                 (energiatodistus-test-data/insert! energiatodistukset laatija-id))]
      (t/is (energiatodistus-test/add-eq-found?
              (assoc energiatodistus :laskutettava-yritys-id yritys-id)
              (service/find-energiatodistus ts/*db* id))))))

(t/deftest define-laatija-laskutus
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistukset
        (map #(assoc % :laskutusosoite-id -1)
             [(energiatodistus-test-data/generate-add 2013 true)
              (energiatodistus-test-data/generate-add 2018 true)
              (energiatodistus-test-data/generate-add 2013 false)
              (energiatodistus-test-data/generate-add 2018 false)])]

    (doseq [[energiatodistus id]
            (map vector energiatodistukset
                 (energiatodistus-test-data/insert! energiatodistukset laatija-id))]
      (t/is (energiatodistus-test/add-eq-found?
              (assoc energiatodistus :laskutettava-yritys-id nil)
              (service/find-energiatodistus ts/*db* id))))))

(t/deftest undefined-laskutusosoite
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistukset
        (map #(assoc % :laskutusosoite-id nil :laskutettava-yritys-id nil)
             [(energiatodistus-test-data/generate-add 2013 true)
              (energiatodistus-test-data/generate-add 2018 true)
              (energiatodistus-test-data/generate-add 2013 false)
              (energiatodistus-test-data/generate-add 2018 false)])]

    (doseq [[energiatodistus id]
            (map vector energiatodistukset
                 (energiatodistus-test-data/insert! energiatodistukset laatija-id))]
      (t/is (energiatodistus-test/add-eq-found?
              energiatodistus
              (service/find-energiatodistus ts/*db* id))))))

(t/deftest add-invalid-yritys-laskutus
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistus (assoc (energiatodistus-test-data/generate-add 2018 false) :laskutusosoite-id 1)]
    (t/is
      (= (etp-test/catch-ex-data #(energiatodistus-test-data/insert! [energiatodistus] laatija-id))
         {:type :invalid-laskutusosoite
          :message "Laatija: 1 does not belong to yritys: 1 or yritys is deleted."}))))

(t/deftest update-invalid-yritys-laskutus
  (let [laatija-id (-> (laatija-test-data/generate-and-insert! 1) keys first)
        energiatodistus (assoc (energiatodistus-test-data/generate-add 2018 false) :laskutusosoite-id nil)
        id (first (energiatodistus-test-data/insert! [energiatodistus] laatija-id))]

    (t/is
      (= (etp-test/catch-ex-data #(service/update-energiatodistus!
                                    (ts/db-user laatija-id)
                                    (whoami/laatija laatija-id)
                                    id
                                    {:laskutusosoite-id 1}))
         {:type :invalid-laskutusosoite
          :message "Laatija: 1 does not belong to yritys: 1 or yritys is deleted."}))))