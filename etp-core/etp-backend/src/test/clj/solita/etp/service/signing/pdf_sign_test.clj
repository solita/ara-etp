(ns solita.etp.service.signing.pdf-sign-test
  (:require [clojure.test :as t]
            [solita.etp.config :as config]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-signing :as et-signing]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.service.signing.pdf-sign :as pdf-sign]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus]
            [solita.etp.test-system :as ts])
  (:import (java.nio.charset StandardCharsets)
           (java.util Base64)))

(t/use-fixtures :each ts/fixture)

(defn get-allekirjoitus-id [laatija]
  ;; TODO: Need to update test-system to actually add an allekirjoitus-id
  ;; For the tests it does not currently matter: The allekirjoitus-id will be wrong
  ;; in the tests but it does not matter.
  "1")

(t/deftest sign-with-system-testing
  (let [; Add laatija
        laatija-add (->> (test-data.laatija/generate-adds 1) (first))
        laatija-id (first (test-data.laatija/insert! [laatija-add]))
        laatija (laatija-service/find-laatija-by-id
                  ts/*db* {:id laatija-id :rooli 6} laatija-id)
        laatija-allekirjoitus-id (get-allekirjoitus-id laatija)
        certs {:root-cert config/system-signature-certificate-root
               :int-cert  config/system-signature-certificate-intermediate
               :leaf-cert config/system-signature-certificate-leaf}

        todistus-2018-fi (-> (test-data.energiatodistus/generate-add 2018 true) (assoc-in [:perustiedot :kieli] 0))

        [todistus-2018-fi-id]
        (test-data.energiatodistus/insert! [todistus-2018-fi] laatija-id)]

    (t/testing "aaa"
      (energiatodistus-service/start-energiatodistus-signing! ts/*db* {:rooli 0 :id laatija-id} todistus-2018-fi-id)
      (let [digest (et-signing/find-energiatodistus-digest ts/*db* ts/*aws-s3-client* todistus-2018-fi-id "fi" laatija-allekirjoitus-id certs)
            _ (println "D:" digest)
            data (-> digest
                     :digest
                     (.getBytes StandardCharsets/UTF_8)
                     (#(.decode (Base64/getDecoder) %)))
            signature (pdf-sign/get-signature-from-system-cms-service ts/*aws-kms-client* data)
            b-level-doc (et-signing/sign-energiatodistus-pdf ts/*db* ts/*aws-s3-client* todistus-2018-fi-id "fi" laatija-allekirjoitus-id certs signature)
            _ (.save b-level-doc "WASD.pdf")
            ]))))

(t/deftest signed-pdf-has-laatija-id

  )
