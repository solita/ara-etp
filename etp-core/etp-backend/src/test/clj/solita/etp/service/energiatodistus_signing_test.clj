(ns solita.etp.service.energiatodistus-signing-test
  (:require
    [clojure.java.jdbc :as jdbc]
    [solita.common.certificates-test :as certificates-test]
    [solita.common.time :as time]
    [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
    [solita.etp.service.energiatodistus-signing :as service]
    [solita.etp.service.energiatodistus-pdf :as pdf-service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign-service]
    [solita.etp.test :as etp-test]
    [clojure.test :as t]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-system :as ts])
  (:import (java.io InputStream)
           (java.time Instant ZoneId)
           (org.apache.pdfbox.pdmodel PDDocument)))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        energiatodistukset (merge (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2013
                                   true
                                   laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2018
                                   true
                                   laatija-id)
                                  (energiatodistus-test-data/generate-and-insert!
                                   1
                                   2026
                                   true
                                   laatija-id))]
    {:laatijat           laatijat
     :energiatodistukset energiatodistukset}))

(t/deftest do-when-signing-test
  (let [f (constantly true)]
    (t/is (= (service/do-when-signing {:tila-id 0} f)
             :not-in-signing))
    (t/is (true? (service/do-when-signing {:tila-id 1} f)))
    (t/is (= (service/do-when-signing {:tila-id 2} f)
             :already-signed))))

(t/deftest create-stateful-signature-parameters-test
  (let [energiatodistus-id "123"
        language "fi"
        ;; A String is used as the object in this test.
        some-data "I am data"
        some-data-is (pdf-sign-service/object->input-stream some-data)
        params-key (service/stateful-signature-parameters-file-key energiatodistus-id language)
        wanted-tag {:Key "TemporarySignatureProcessFile" :Value "True"}]
    (t/testing "The key is in a place that is safe to allow for deletion via a lifecycle rule."
      (t/is (= "energiatodistus-signing/stateful-signature-parameters-123-fi" params-key)))
    (service/save-stateful-signature-parameters ts/*aws-s3-client* energiatodistus-id language some-data-is)
    (t/testing "The created file has a the correct tag."
      (t/is (= wanted-tag (file-service/get-file-tag ts/*aws-s3-client*
                                                     params-key
                                                     (:Key wanted-tag)))))
    (t/testing "Deserialization of the stored object works."
      (t/is (= some-data (-> (service/load-stateful-signature-parameters ts/*aws-s3-client* energiatodistus-id language)
                             (pdf-sign-service/input-stream->object)))))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} find-energiatodistus-digest-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        ids (keys energiatodistukset)
        whoami {:id laatija-id}
        now (time/now)]
    (doseq [id ids]
      (t/is (= (service/find-energiatodistus-digest db whoami ts/*aws-s3-client* id "fi" "allekirjoitus-id" now)
               :not-in-signing))
      (energiatodistus-service/start-energiatodistus-signing! db whoami id)
      (t/is (contains? (service/find-energiatodistus-digest db
                                                            whoami
                                                            ts/*aws-s3-client*
                                                            id
                                                            "fi"
                                                            "allekirjoitus-id"
                                                            now)
                       :digest))
      (energiatodistus-service/end-energiatodistus-signing!
       db
       ts/*aws-s3-client*
       whoami
       id
       {:skip-pdf-signed-assert? true})
      (t/is (= (service/find-energiatodistus-digest db
                                                    whoami
                                                    ts/*aws-s3-client*
                                                    id
                                                    "fi"
                                                    "allekirjoitus-id"
                                                    now)
               :already-signed)))))

(t/deftest comparable-name-test
  (t/is (= "abc" (service/comparable-name "abc")))
  (t/is (= "aeiouao" (service/comparable-name "á, é, í, ó, ú. ä ö"))))

(t/deftest validate-certificate!-test
  (t/testing "Signing certificate must not have expired"
    (let [ex (try
               (service/validate-certificate! energiatodistus-test-data/time-when-test-cert-expired
                                              certificates-test/test-cert-str)
               (catch clojure.lang.ExceptionInfo ex ex))
          {:keys [type]} (ex-data ex)]
      (t/is (instance? clojure.lang.ExceptionInfo ex))
      (t/is (= :expired-signing-certificate type))))

  (t/testing "With the expected name and within the validity period of the certificate, signing succeeds"
    (service/validate-certificate! energiatodistus-test-data/time-when-test-cert-not-expired
                                   certificates-test/test-cert-str)))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} sign-energiatodistus-pdf-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        ids (keys energiatodistukset)]
    (doseq [id ids]
      (t/is (= (service/sign-energiatodistus-pdf db
                                                 ts/*aws-s3-client*
                                                 energiatodistus-test-data/time-when-test-cert-not-expired
                                                 id
                                                 "fi"
                                                 nil)
               :not-in-signing))
      (energiatodistus-test-data/sign-at-time! id
                                               laatija-id
                                               false
                                               energiatodistus-test-data/time-when-test-cert-not-expired)
      (t/is (= (service/sign-energiatodistus-pdf db
                                                 ts/*aws-s3-client*
                                                 energiatodistus-test-data/time-when-test-cert-not-expired
                                                 id
                                                 "fi"
                                                 nil)
               :already-signed)))))

(t/deftest cert-pem->one-liner-without-headers-test
  (let [given "-----BEGIN CERTIFICATE-----
MIIDqjCCAZICFDasnTgEcpPh0nwI7KDvEkoIrRCgMA0GCSqGSIb3DQEBCwUAMBEx
DzANBgNVBAMMBklOVCBDQTAeFw0yNDA0MTAxMDEyMDNaFw0yNTA0MjAxMDEyMDNa
MBIxEDAOBgNVBAMMB0xFQUYgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
AoIBAQDZ7LojEvUzN4GuvLwlDH/Ex6rV5mcopSbJ8eHMfTEs2bq8Czll82mUmy08
agj2g6zsll7sMpOQLDjI0Im8HuWGuBcq8q7ArkbDjj9582ul21kWcEERMCWZWwFC
jlqF5xWT/iz8d62hvP++hmFJulG3U/grCKyBu/PpoQRYBUgkbqmyz8dMLMCF4y7V
ojcKdFe7tQEIw0fvdoT8dTXURXemhk5bzmejHYSU2+h727ALG3WoNkgwaBWW10nQ
SCQhP/mmrq5Z3QhIrpAboPddpSpAukpUM7Bss7q153oC+QIvZaMMFlXVdBisS6lS
tiX+upnlEIx9BtBxvTxUgm9rQFZdAgMBAAEwDQYJKoZIhvcNAQELBQADggIBAMR/
nFJAI58MtpAa9Yr6vTI0RSWv30/Tlv/DGrg0ujXx/3lmPygdA2LW5KTt9MLB3kZ/
wMl2RMGv66AssdHFlnUHUAkvm28UTzaJ4pKatiRuHODyvn9cbjodD+MHJjksdWO/
JLJ4yk3tsvhK0zUC6nnJGvwyodQvhBgPLfB9NRWbOWvDzsAIRYEWfAJUz0nqnN9q
MkQLibMRkEmt8JrtlUk+o8DUxZi7lpEJ6s1DilDDacQmOW+2bOoXdn8LFz2qVq6+
hoIpHiFEvge0wWYID6XcGUlV5X3Su+LnXFYMdqEwRYcpNWwWrAPNleMV9QLIHsBB
j7131sa4axikbvnEOrCEmeLL2KOLkbWONh6HayYXmonQGeYapIaA9CB59Moxgeeo
F9+dWH9dK9lfItKyPJzQYO2ZJXa1EbE21yY4JoRluh89dnPvvXcwF7MS0JN9+Re5
Q9zyOEVkZ4YEc8aTYeEua4byUOJ43USk4+A82Y0gLTsDN/exo09c1pR8dFHy9l9D
wpvr3ZEb1iocWqBjzWN+GsW7+pf42qS0xKvf1gdFxW1wq+M+lO5LYnA6n9da1azD
ZtIhpinH/MfQG6HI6EfgQU9zxTHEwEaJog95wBV1HV0g7eXo3quTqltN/nzERzLA
qv9qLQ9UDTgHkSPRn65MhpmqlfSqI1sdQmPUnOJX
-----END CERTIFICATE-----"
        expected "MIIDqjCCAZICFDasnTgEcpPh0nwI7KDvEkoIrRCgMA0GCSqGSIb3DQEBCwUAMBExDzANBgNVBAMMBklOVCBDQTAeFw0yNDA0MTAxMDEyMDNaFw0yNTA0MjAxMDEyMDNaMBIxEDAOBgNVBAMMB0xFQUYgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZ7LojEvUzN4GuvLwlDH/Ex6rV5mcopSbJ8eHMfTEs2bq8Czll82mUmy08agj2g6zsll7sMpOQLDjI0Im8HuWGuBcq8q7ArkbDjj9582ul21kWcEERMCWZWwFCjlqF5xWT/iz8d62hvP++hmFJulG3U/grCKyBu/PpoQRYBUgkbqmyz8dMLMCF4y7VojcKdFe7tQEIw0fvdoT8dTXURXemhk5bzmejHYSU2+h727ALG3WoNkgwaBWW10nQSCQhP/mmrq5Z3QhIrpAboPddpSpAukpUM7Bss7q153oC+QIvZaMMFlXVdBisS6lStiX+upnlEIx9BtBxvTxUgm9rQFZdAgMBAAEwDQYJKoZIhvcNAQELBQADggIBAMR/nFJAI58MtpAa9Yr6vTI0RSWv30/Tlv/DGrg0ujXx/3lmPygdA2LW5KTt9MLB3kZ/wMl2RMGv66AssdHFlnUHUAkvm28UTzaJ4pKatiRuHODyvn9cbjodD+MHJjksdWO/JLJ4yk3tsvhK0zUC6nnJGvwyodQvhBgPLfB9NRWbOWvDzsAIRYEWfAJUz0nqnN9qMkQLibMRkEmt8JrtlUk+o8DUxZi7lpEJ6s1DilDDacQmOW+2bOoXdn8LFz2qVq6+hoIpHiFEvge0wWYID6XcGUlV5X3Su+LnXFYMdqEwRYcpNWwWrAPNleMV9QLIHsBBj7131sa4axikbvnEOrCEmeLL2KOLkbWONh6HayYXmonQGeYapIaA9CB59MoxgeeoF9+dWH9dK9lfItKyPJzQYO2ZJXa1EbE21yY4JoRluh89dnPvvXcwF7MS0JN9+Re5Q9zyOEVkZ4YEc8aTYeEua4byUOJ43USk4+A82Y0gLTsDN/exo09c1pR8dFHy9l9Dwpvr3ZEb1iocWqBjzWN+GsW7+pf42qS0xKvf1gdFxW1wq+M+lO5LYnA6n9da1azDZtIhpinH/MfQG6HI6EfgQU9zxTHEwEaJog95wBV1HV0g7eXo3quTqltN/nzERzLAqv9qLQ9UDTgHkSPRn65MhpmqlfSqI1sdQmPUnOJX"]
    (t/testing "Certificate should be on one line without headers"
      (t/is (= (service/cert-pem->one-liner-without-headers given) expected)))))

(t/deftest sign-with-system-states-test
  (with-bindings {#'solita.etp.service.signing.pdf-sign/get-tsp-source solita.etp.test-timeserver/get-tsp-source-in-test}
    (t/testing "Signing a pdf using the system"
      (let [{:keys [laatijat energiatodistukset]} (test-data-set)
            laatija-id (-> laatijat keys sort first)
            db (ts/db-user laatija-id)
            ids (keys energiatodistukset)
            whoami {:id laatija-id}]
        (doseq [id ids]
          (t/testing "Signing a pdf should succeed"
            (t/is (= (service/sign-with-system {:db             db
                                                :aws-s3-client  ts/*aws-s3-client*
                                                :whoami         whoami
                                                :aws-kms-client ts/*aws-kms-client*
                                                :now            (time/now)
                                                :id             id})
                     :ok)))
          (t/testing "Trying to sign again should result in :already-signed"
            (t/is (= (service/sign-with-system {:db             db
                                                :aws-s3-client  ts/*aws-s3-client*
                                                :whoami         whoami
                                                :aws-kms-client ts/*aws-kms-client*
                                                :now            (time/now)
                                                :id             id})
                     :already-signed)))
          (t/testing "The state should result in :already-signed if trying to sign three times in a row"
            (t/is (= (service/sign-with-system {:db             db
                                                :aws-s3-client  ts/*aws-s3-client*
                                                :whoami         whoami
                                                :aws-kms-client ts/*aws-kms-client*
                                                :now            (time/now)
                                                :id             id})
                     :already-signed))))))))

(t/deftest sign-with-system-already-in-signing-test
  (t/testing "Trying to sign a pdf that is already in signing should fail and not change the state"
    (let [{:keys [laatijat energiatodistukset]} (test-data-set)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          ids (keys energiatodistukset)
          whoami {:id laatija-id}]
      (doseq [id ids]
        ;; Put the energiatodistus into signing.
        (energiatodistus-service/start-energiatodistus-signing! db whoami id)
        (let [{:keys [tila-id]} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
          (t/testing "Trying to sign a pdf that is already in signing should return :already-in-signing"
            (t/is (= (service/sign-with-system {:db             db
                                                :aws-s3-client  ts/*aws-s3-client*
                                                :whoami         whoami
                                                :aws-kms-client ts/*aws-kms-client*
                                                :now            (time/now)
                                                :id             id})
                     :already-in-signing)))
          (t/testing "Trying to sign a pdf that is already in signing should not change the state"
            (t/is (= (-> (complete-energiatodistus-service/find-complete-energiatodistus db id) :tila-id)
                     tila-id))))))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} sign-with-system-signature-test
  (t/testing "Signing a pdf using the system"
    (with-bindings {#'solita.etp.service.signing.pdf-sign/get-tsp-source solita.etp.test-timeserver/get-tsp-source-in-test}
      (let [{:keys [laatijat energiatodistukset]} (test-data-set)
            laatija-id (-> laatijat keys sort first)
            db (ts/db-user laatija-id)
            ids (keys energiatodistukset)
            whoami {:id laatija-id :rooli 0}]
        (doseq [id ids
                :let [complete-energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus db id)
                      language-code (-> complete-energiatodistus :perustiedot :kieli (energiatodistus-service/language-id->codes) first)]]
          (t/testing "The signed document's signature should exist."
            (service/sign-with-system {:db             db
                                       :aws-s3-client  ts/*aws-s3-client*
                                       :whoami         whoami
                                       :aws-kms-client ts/*aws-kms-client*
                                       :now            (time/now)
                                       :id             id})
            (with-open [^InputStream pdf-is (pdf-service/find-energiatodistus-pdf db ts/*aws-s3-client* whoami id language-code)
                        pdf (PDDocument/load pdf-is)]
              (let [signatures (.getSignatureDictionaries pdf)]
                (t/is (not (nil? signatures)))))))))))

;; Laatimisvaihe validation during signing

(defn- generate-and-insert-with-laatimisvaihe!
  "Generates a ready-for-signing energiatodistus for the given version,
   then sets the laatimisvaihe to the specified value via direct DB update
   (bypassing service-level validation to simulate API bypass scenarios)."
  [versio laatimisvaihe-id laatija-id]
  (let [et-entry (energiatodistus-test-data/generate-and-insert!
                   1 versio true laatija-id)
        et-id (first (keys et-entry))]
    (jdbc/execute! ts/*db*
                   ["UPDATE energiatodistus SET pt$laatimisvaihe = ? WHERE id = ?"
                    laatimisvaihe-id et-id])
    et-id))

(t/deftest signing-rejects-invalid-laatimisvaihe-2018-id-3
  (t/testing "given a 2018 energiatodistus with laatimisvaihe 3, when signing, then throws :invalid-laatimisvaihe"
    (let [laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-id (generate-and-insert-with-laatimisvaihe! 2018 3 laatija-id)
          result (etp-test/catch-ex-data
                   #(energiatodistus-service/start-energiatodistus-signing! db whoami et-id))]
      (t/is (= (:type result) :invalid-laatimisvaihe)))))

(t/deftest signing-rejects-invalid-laatimisvaihe-2018-id-4
  (t/testing "given a 2018 energiatodistus with laatimisvaihe 4, when signing, then throws :invalid-laatimisvaihe"
    (let [laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-id (generate-and-insert-with-laatimisvaihe! 2018 4 laatija-id)
          result (etp-test/catch-ex-data
                   #(energiatodistus-service/start-energiatodistus-signing! db whoami et-id))]
      (t/is (= (:type result) :invalid-laatimisvaihe)))))

(t/deftest signing-allows-valid-laatimisvaihe-2026-id-3
  (t/testing "given a 2026 energiatodistus with laatimisvaihe 3, when signing, then succeeds"
    (let [laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-id (generate-and-insert-with-laatimisvaihe! 2026 3 laatija-id)]
      (t/is (= (energiatodistus-service/start-energiatodistus-signing! db whoami et-id)
               :ok)))))

(t/deftest signing-allows-valid-laatimisvaihe-2026-id-4
  (t/testing "given a 2026 energiatodistus with laatimisvaihe 4, when signing, then succeeds"
    (let [laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-id (generate-and-insert-with-laatimisvaihe! 2026 4 laatija-id)]
      (t/is (= (energiatodistus-service/start-energiatodistus-signing! db whoami et-id)
               :ok)))))

;; ============================================================================
;; AE-2782: Validity period PDF generation bug — new tests
;; ============================================================================

(defn- create-signed-energiatodistus!
  "Creates and signs an energiatodistus, returns its id."
  [versio laatija-id]
  (let [et-add (energiatodistus-test-data/generate-add versio true)
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    (energiatodistus-test-data/sign! et-id laatija-id true)
    et-id))

(defn- create-simplified-update-draft!
  "Creates a draft energiatodistus that replaces the given korvattava-id
   with yksinkertaistettu-paivitysmenettely = true."
  [versio laatija-id korvattava-id]
  (let [et-add (-> (energiatodistus-test-data/generate-add versio true)
                   (assoc :korvattu-energiatodistus-id korvattava-id
                          :yksinkertaistettu-paivitysmenettely true))
        [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)]
    et-id))

(defn- set-custom-validity!
  "Sets a custom voimassaolo-paattymisaika on an energiatodistus via direct DB update."
  [et-id custom-validity]
  (jdbc/execute! ts/*db*
                 ["UPDATE energiatodistus SET voimassaolo_paattymisaika = ? WHERE id = ?"
                  (java.sql.Timestamp/from custom-validity) et-id]))

(defn- five-years-from-now []
  (-> (Instant/now)
      (.atZone (ZoneId/of "Europe/Helsinki"))
      (.plusYears 5)
      .toInstant))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} pdf-digest-uses-inherited-validity-for-simplified-update-test
  (t/testing "given a simplified update energiatodistus,
              when find-energiatodistus-digest is called,
              then the voimassaolo-paattymisaika injected into the PDF equals the replaced ET's validity"
    (let [;; Given: a signed ET A with a custom 5-year validity
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          korvattava-id (create-signed-energiatodistus! 2018 laatija-id)
          custom-validity (five-years-from-now)
          _ (set-custom-validity! korvattava-id custom-validity)
          korvattava-validity (:voimassaolo-paattymisaika
                                (energiatodistus-service/find-energiatodistus ts/*db* korvattava-id))
          ;; And: a draft ET B replacing A with yksinkertaistettu=true
          korvaava-id (create-simplified-update-draft! 2018 laatija-id korvattava-id)
          ;; Capture the voimassaolo-paattymisaika passed to PDF generation
          captured-voimassaolo (atom nil)
          original-mock energiatodistus-test-data/generate-pdf-as-file-mock
          capturing-mock (fn [energiatodistus & args]
                           (reset! captured-voimassaolo (:voimassaolo-paattymisaika energiatodistus))
                           (apply original-mock energiatodistus args))
          now (time/now)]
      ;; When: start signing and generate the digest
      (energiatodistus-service/start-energiatodistus-signing! db whoami korvaava-id)
      (with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file capturing-mock}
        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                             korvaava-id "fi" "allekirjoitus-id" now))
      ;; Then: the validity injected into the PDF should be the inherited one
      (t/is (some? @captured-voimassaolo)
            "voimassaolo-paattymisaika should have been captured from PDF generation")
      (t/is (= korvattava-validity @captured-voimassaolo)
            "PDF should use inherited validity from replaced ET, not fresh 10-year"))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} pdf-digest-uses-fresh-validity-for-normal-procedure-test
  (t/testing "given a normal procedure energiatodistus (yksinkertaistettu=false),
              when find-energiatodistus-digest is called,
              then the voimassaolo-paattymisaika injected is approximately now + 1 day + 10 years"
    (let [;; Given: a normal draft ET
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-add (energiatodistus-test-data/generate-add 2018 true)
          [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)
          captured-voimassaolo (atom nil)
          original-mock energiatodistus-test-data/generate-pdf-as-file-mock
          capturing-mock (fn [energiatodistus & args]
                           (reset! captured-voimassaolo (:voimassaolo-paattymisaika energiatodistus))
                           (apply original-mock energiatodistus args))
          now (time/now)
          expected-min (-> now
                           (.atZone (ZoneId/of "Europe/Helsinki"))
                           .toLocalDate
                           (.plusDays 1)
                           (.atStartOfDay (ZoneId/of "Europe/Helsinki"))
                           (.plusYears 10)
                           .toInstant)]
      ;; When: start signing and generate the digest
      (energiatodistus-service/start-energiatodistus-signing! db whoami et-id)
      (with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file capturing-mock}
        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                             et-id "fi" "allekirjoitus-id" now))
      ;; Then: validity should be approximately now + 10 years
      (t/is (some? @captured-voimassaolo)
            "voimassaolo-paattymisaika should have been captured")
      (t/is (= expected-min @captured-voimassaolo)
            "PDF should use fresh 10-year validity for normal procedure"))))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} full-signing-pdf-matches-db-simplified-update-test
  (t/testing "given a simplified update energiatodistus with custom 5-year replaced validity,
              when the full signing flow completes,
              then the voimassaolo-paattymisaika in the PDF matches the one stored in the database"
    (let [;; Given: a signed ET A with custom 5-year validity
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          korvattava-id (create-signed-energiatodistus! 2018 laatija-id)
          custom-validity (five-years-from-now)
          _ (set-custom-validity! korvattava-id custom-validity)
          korvattava-validity (:voimassaolo-paattymisaika
                                (energiatodistus-service/find-energiatodistus ts/*db* korvattava-id))
          ;; And: a draft ET B replacing A with yksinkertaistettu=true
          korvaava-id (create-simplified-update-draft! 2018 laatija-id korvattava-id)
          ;; Capture the voimassaolo-paattymisaika passed to PDF generation
          captured-pdf-voimassaolo (atom nil)
          original-mock energiatodistus-test-data/generate-pdf-as-file-mock
          capturing-mock (fn [energiatodistus & args]
                           (reset! captured-pdf-voimassaolo (:voimassaolo-paattymisaika energiatodistus))
                           (apply original-mock energiatodistus args))
          now (time/now)]
      ;; When: perform the full signing flow step by step
      (energiatodistus-service/start-energiatodistus-signing! db whoami korvaava-id)
      ;; Digest step — capture what voimassaolo is injected into the PDF
      (with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file capturing-mock}
        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                             korvaava-id "fi" "allekirjoitus-id" now))
      ;; End signing — finalize the DB record
      (energiatodistus-service/end-energiatodistus-signing! db ts/*aws-s3-client* whoami korvaava-id
                                                             {:skip-pdf-signed-assert? true
                                                              :allekirjoitusaika       now})
      ;; Then: the value injected into the PDF must equal the value stored in the DB
      (let [korvaava-et (energiatodistus-service/find-energiatodistus ts/*db* korvaava-id)
            db-voimassaolo (:voimassaolo-paattymisaika korvaava-et)]
        (t/is (some? @captured-pdf-voimassaolo)
              "voimassaolo-paattymisaika should have been captured from PDF generation")
        (t/is (some? db-voimassaolo)
              "voimassaolo-paattymisaika should be stored in database after signing")
        (t/is (= @captured-pdf-voimassaolo db-voimassaolo)
              "PDF voimassaolo-paattymisaika must equal database voimassaolo-paattymisaika (core invariant)")))))

(t/deftest early-validation-expired-replaced-at-digest-time-test
  (t/testing "given a simplified update where the replaced ET has expired,
              when find-energiatodistus-digest is called,
              then an error is thrown at digest time (not only at finalization)"
    (let [;; Given: a signed ET A with expired validity
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          korvattava-id (create-signed-energiatodistus! 2018 laatija-id)
          ;; Set validity to past
          _ (jdbc/execute! (ts/db-user ts/*admin-db* -1)
                           ["UPDATE energiatodistus SET voimassaolo_paattymisaika = now() - interval '1 day' WHERE id = ?"
                            korvattava-id])
          ;; And: a draft B replacing A with yksinkertaistettu=true
          korvaava-id (create-simplified-update-draft! 2018 laatija-id korvattava-id)
          now (time/now)]
      ;; When: start signing and attempt digest generation
      (energiatodistus-service/start-energiatodistus-signing! db whoami korvaava-id)
      ;; Then: an error should be thrown at digest time
      (let [result (etp-test/catch-ex-data
                     #(with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file
                                      energiatodistus-test-data/generate-pdf-as-file-mock}
                        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                                             korvaava-id "fi" "allekirjoitus-id" now)))]
        (t/is (= :invalid-replace (:type result))
              "Expired replaced ET should cause error at digest time, not only at finalization")))))

(t/deftest early-validation-nil-korvattu-id-at-digest-time-test
  (t/testing "given yksinkertaistettu-paivitysmenettely=true but korvattu-energiatodistus-id=nil,
              when find-energiatodistus-digest is called,
              then an error is thrown at digest time"
    (let [;; Given: a draft ET with yksinkertaistettu=true but no korvattu-id
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          et-add (-> (energiatodistus-test-data/generate-add 2018 true)
                     (assoc :yksinkertaistettu-paivitysmenettely true
                            :korvattu-energiatodistus-id nil))
          [et-id] (energiatodistus-test-data/insert! [et-add] laatija-id)
          now (time/now)]
      ;; When: start signing and attempt digest generation
      (energiatodistus-service/start-energiatodistus-signing! db whoami et-id)
      ;; Then: error at digest time
      (let [result (etp-test/catch-ex-data
                     #(with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file
                                      energiatodistus-test-data/generate-pdf-as-file-mock}
                        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                                             et-id "fi" "allekirjoitus-id" now)))]
        (t/is (= :invalid-replace (:type result))
              "Missing korvattu-energiatodistus-id with yksinkertaistettu=true should fail at digest time")))))

(t/deftest early-validation-never-signed-replaced-at-digest-time-test
  (t/testing "given a simplified update where the replaced ET was never signed (no voimassaolo-paattymisaika),
              when find-energiatodistus-digest is called,
              then an error is thrown at digest time"
    (let [;; Given: an unsigned (draft) ET A — manually set up to bypass normal constraints
          laatijat (laatija-test-data/generate-and-insert! 1)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          whoami {:id laatija-id}
          ;; Create and sign A, then clear its voimassaolo to simulate "never signed" scenario
          korvattava-id (create-signed-energiatodistus! 2018 laatija-id)
          _ (jdbc/execute! (ts/db-user ts/*admin-db* -1)
                           ["UPDATE energiatodistus SET voimassaolo_paattymisaika = NULL WHERE id = ?"
                            korvattava-id])
          ;; And: a draft B replacing A with yksinkertaistettu=true
          korvaava-id (create-simplified-update-draft! 2018 laatija-id korvattava-id)
          now (time/now)]
      ;; When: start signing and attempt digest generation
      (energiatodistus-service/start-energiatodistus-signing! db whoami korvaava-id)
      ;; Then: error at digest time
      (let [result (etp-test/catch-ex-data
                     #(with-bindings {#'pdf-service/generate-et-2013-2018-pdf-as-file
                                      energiatodistus-test-data/generate-pdf-as-file-mock}
                        (service/find-energiatodistus-digest db whoami ts/*aws-s3-client*
                                                             korvaava-id "fi" "allekirjoitus-id" now)))]
        (t/is (= :invalid-replace (:type result))
              "Replaced ET without voimassaolo-paattymisaika should fail at digest time")))))
