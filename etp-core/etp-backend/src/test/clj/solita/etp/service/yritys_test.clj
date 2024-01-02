(ns solita.etp.service.yritys-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.yritys :as yritys-test-data]
            [solita.etp.service.yritys :as service]
            [solita.etp.schema.yritys :as yritys-schema]
            [solita.etp.schema.common :as common-schema]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [paakayttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        laskuttaja-adds (->> (kayttaja-test-data/generate-adds 1)
                             (map #(assoc % :rooli 3)))
        laskuttaja-ids (kayttaja-test-data/insert! laskuttaja-adds)
        laatijat (laatija-test-data/generate-and-insert! 10)
        yritykset (->> laatijat
                       keys
                       sort
                       (map #(yritys-test-data/generate-and-insert! 1 %))
                       (apply merge))]
    {:paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)
     :laskuttajat (zipmap laskuttaja-ids laskuttaja-adds)
     :laatijat laatijat
     :yritykset yritykset}))

(t/deftest add-and-find-yritys-test
  (let [{:keys [yritykset]} (test-data-set)]
    (doseq [[id yritys-add] yritykset]
      (t/is (= (assoc yritys-add :id id :deleted false) (service/find-yritys ts/*db* id))))))

(t/deftest add-yritys-with-duplicate-ytunnus-nimi-and-vastaanottajan-tarkenne-test
  (let [{:keys [laatijat]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        [yritys-1 yritys-2] (->> (yritys-test-data/generate-adds 2)
                                 (map #(assoc % :ytunnus "0000001-9"))
                                 (map #(assoc % :nimi "test"))
                                 (map #(assoc % :vastaanottajan-tarkenne "test")))]
    (service/add-yritys! ts/*db* {:id laatija-id} yritys-1)
    (t/is (= (etp-test/catch-ex-data
              #(service/add-yritys! ts/*db* {:id laatija-id} yritys-2))
             {:type       :unique-violation
              :constraint :yritys-ytunnus-nimi-vastaanottajan-tarkenne-key,
              :value      "0000001-9, test, test"}))))

(t/deftest update-yritys-test
  (let [{:keys [paakayttajat laatijat yritykset]} (test-data-set)
        paakayttaja-id (-> paakayttajat keys sort first)
        laatija-id (-> laatijat keys sort first)
        yritys-id (-> yritykset keys sort first)]
    (doseq [update (yritys-test-data/generate-updates 50)
            :let [whoami (rand-nth [{:rooli 2 :id paakayttaja-id}
                                    {:rooli 0 :id laatija-id}])]]
      (service/update-yritys! (ts/db-user (:id whoami))
                              whoami
                              yritys-id
                              update)
      (t/is (= (assoc update :id yritys-id :deleted false)
               (service/find-yritys ts/*db* yritys-id))))))

(t/deftest update-yritys-no-permissions-test
  (let [{:keys [laskuttajat laatijat yritykset]} (test-data-set)
        laskuttaja-id (-> laskuttajat keys sort first)
        laatija-id (-> laatijat keys sort last)
        yritys-id (-> yritykset keys sort first)
        whoami {:rooli 0 :id laatija-id}]
    (doseq [update (yritys-test-data/generate-updates 50)]
      (t/is (= (-> (etp-test/catch-ex-data #(service/update-yritys!
                                             ts/*db*
                                             whoami
                                             yritys-id
                                             update))
                   (dissoc :reason))
               {:type :forbidden})))))

(t/deftest find-all-laskutuskielet-test
  (let [laskutuskielet (service/find-all-laskutuskielet ts/*db*)]
    (t/is (= 3 (count laskutuskielet)))
    (t/is (schema/validate [common-schema/Luokittelu]
                           laskutuskielet))))

(t/deftest find-all-verkkolaskuoperaattorit-test
  (let [verkkolaskuoperaattorit (service/find-all-verkkolaskuoperaattorit ts/*db*)]
    (t/is (= 32 (count verkkolaskuoperaattorit)))
    (t/is (schema/validate [yritys-schema/Verkkolaskuoperaattori]
                           verkkolaskuoperaattorit))))
