(ns solita.etp.service.energiatodistus-signing-test
  (:require
    [solita.common.certificates-test :as certificates-test]
    [solita.common.time :as time]
    [solita.etp.service.energiatodistus-signing :as service]
    [solita.etp.service.energiatodistus :as energiatodistus-service]
    [solita.etp.service.file :as file-service]
    [solita.etp.service.signing.pdf-sign :as pdf-sign-service]
    [clojure.test :as t]
    [solita.etp.test-data.laatija :as laatija-test-data]
    [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
    [solita.etp.test-system :as ts]))

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
        id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id}]
    (t/is (= (service/find-energiatodistus-digest db ts/*aws-s3-client* id "fi" "allekirjoitus-id")
             :not-in-signing))
    (energiatodistus-service/start-energiatodistus-signing! db whoami id)
    (t/is (contains? (service/find-energiatodistus-digest db
                                                                  ts/*aws-s3-client*
                                                                  id
                                                                  "fi"
                                                                  "allekirjoitus-id")
                     :digest))
    (energiatodistus-service/end-energiatodistus-signing!
      db
      ts/*aws-s3-client*
      whoami
      id
      {:skip-pdf-signed-assert? true})
    (t/is (= (service/find-energiatodistus-digest db
                                                          ts/*aws-s3-client*
                                                          id
                                                          "fi"
                                                          "allekirjoitus-id")
             :already-signed))))

(t/deftest comparable-name-test
  (t/is (= "abc" (service/comparable-name "abc")))
  (t/is (= "aeiouao" (service/comparable-name "á, é, í, ó, ú. ä ö"))))

(t/deftest validate-surname!-test
  (t/is (thrown? clojure.lang.ExceptionInfo
                 (service/validate-surname! "Meikäläinen"
                                                    certificates-test/test-cert)))
  (t/is (nil? (service/validate-surname! "Specimen-POtex"
                                                 certificates-test/test-cert))))

(t/deftest validate-certificate!-test
  (t/testing "Last name of laatija has to match the signing certificate"
    (let [ex (try
               (service/validate-certificate! "Meikäläinen"
                                                      energiatodistus-test-data/time-when-test-cert-not-expired
                                                      certificates-test/test-cert-str)
               (catch clojure.lang.ExceptionInfo ex ex))
          {:keys [type]} (ex-data ex)]
      (t/is (instance? clojure.lang.ExceptionInfo ex))
      (t/is (= :name-does-not-match type))))

  (t/testing "Signing certificate must not have expired"
    (let [ex (try
               (service/validate-certificate! "Specimen-POtex"
                                                      energiatodistus-test-data/time-when-test-cert-expired
                                                      certificates-test/test-cert-str)
               (catch clojure.lang.ExceptionInfo ex ex))
          {:keys [type]} (ex-data ex)]
      (t/is (instance? clojure.lang.ExceptionInfo ex))
      (t/is (= :expired-signing-certificate type))))

  (t/testing "With the expected name and within the validity period of the certificate, signing succeeds"
    (service/validate-certificate! "Specimen-POtex"
                                           energiatodistus-test-data/time-when-test-cert-not-expired
                                           certificates-test/test-cert-str)))

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} sign-energiatodistus-pdf-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id}]
    (t/is (= (service/sign-energiatodistus-pdf db
                                                       ts/*aws-s3-client*
                                                       whoami
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
                                                       whoami
                                                       energiatodistus-test-data/time-when-test-cert-not-expired
                                                       id
                                                       "fi"
                                                       nil)
             :already-signed))))

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
    (t/testing "Signing a pdf using the system instead of mpollux"
      (let [{:keys [laatijat energiatodistukset]} (test-data-set)
            laatija-id (-> laatijat keys sort first)
            db (ts/db-user laatija-id)
            ;; The second ET is 2018 version
            id (-> energiatodistukset keys sort second)
            whoami {:id laatija-id}]
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
                   :already-signed)))))))

