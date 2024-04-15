(ns solita.etp.service.energiatodistus-pdf-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [solita.common.certificates-test :as certificates-test]
            [solita.common.formats :as formats]
            [solita.common.xlsx :as xlsx]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as service]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts])
  (:import (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.xmpbox.xml DomXmpParser)))

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

(def sis-kuorma-data {:henkilot          {:kayttoaste 0.2 :lampokuorma 1}
                      :kuluttajalaitteet {:kayttoaste 0.3 :lampokuorma 1}
                      :valaistus         {:kayttoaste 0.3 :lampokuorma 2}})

(t/deftest sis-kuorma-test
  (let [sis-kuorma (service/sis-kuorma {:lahtotiedot {:sis-kuorma
                                                      sis-kuorma-data}})]
    (t/is (= sis-kuorma [[0.2 {:henkilot 1}]
                         [0.3 {:kuluttajalaitteet 1 :valaistus 2}]]))))

(t/deftest format-number-test
  (t/is (= "12,346" (formats/format-number 12.34567 3 false)))
  (t/is (= "0,84" (formats/format-number 0.8449 2 false)))
  (t/is (= "100 %" (formats/format-number 1 0 true)))
  (t/is (= "12,346 %" (formats/format-number 0.1234567 3 true))))

(t/deftest fill-xlsx-template-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        energiatodistus-ids (-> energiatodistukset keys sort)]
    (doseq [id (-> energiatodistukset keys sort)
            :let [energiatodistus (energiatodistus-service/find-energiatodistus
                                    ts/*db*
                                    id)
                  path (service/fill-xlsx-template energiatodistus "fi" false)
                  file (-> path io/input-stream)
                  loaded-xlsx (xlsx/load-xlsx file)
                  sheet-0 (xlsx/get-sheet loaded-xlsx 0)]]
      (t/is (str/ends-with? path ".xlsx"))
      (t/is (-> path io/as-file .exists true?))
      (t/is (= (str id)
               (xlsx/get-cell-value-at sheet-0 (case (:versio energiatodistus)
                                                 2013 "I17"
                                                 2018 "J16"))))
      (io/delete-file path))))

(t/deftest xlsx->pdf-test
  (let [file-path (service/xlsx->pdf (str "src/main/resources/"
                                          "energiatodistus-2018-fi.xlsx"))]
    (t/is (str/ends-with? file-path ".pdf"))
    (t/is (-> file-path io/as-file .exists true?))
    (io/delete-file file-path)))

(t/deftest generate-pdf-as-file-test
  (let [{:keys [energiatodistukset]} (test-data-set)
        xmp-parser (DomXmpParser.)]
    (doseq [id (-> energiatodistukset keys sort)
            :let [energiatodistus (energiatodistus-service/find-energiatodistus
                                    ts/*db*
                                    id)
                  file-path (service/generate-pdf-as-file energiatodistus
                                                          "sv"
                                                          true)]]
      (t/is (-> file-path io/as-file .exists))

      (t/testing "Test that the expected metadata is in place"
        (let [{:keys [etunimi sukunimi]} (kayttaja-service/find-kayttaja ts/*db* (:laatija-id energiatodistus))
              expected-author (str sukunimi ", " etunimi)
              expected-title (-> energiatodistus :perustiedot :nimi-sv)
              document (-> file-path io/as-file PDDocument/load)
              document-info (.getDocumentInformation document)
              xmp-metadata (->> document .getDocumentCatalog .getMetadata .exportXMPMetadata (.parse xmp-parser))]
          (t/testing "Test for author and title in the older-style metadata"
            (t/is (= expected-author (.getAuthor document-info)))
            (t/is (= expected-title (.getTitle document-info))))
          (t/testing "Test for author and title in the XMP metadata"
            (t/is (= expected-author (-> xmp-metadata .getDublinCoreSchema .getCreators first)))
            (t/is (= expected-title (-> xmp-metadata .getDublinCoreSchema .getTitle))))
          (t/testing "Test that the document declaries itself as PDF/A compliant"
            (t/is (= 2 (-> xmp-metadata .getPDFIdentificationSchema .getPart)))
            (t/is (= "B" (-> xmp-metadata .getPDFIdentificationSchema .getConformance))))
          (.close document)))

      (io/delete-file file-path))))
(t/deftest generate-pdf-without-building-name
  (let [{:keys [energiatodistukset]} (test-data-set)
        xmp-parser (DomXmpParser.)]
    (doseq [id (-> energiatodistukset keys sort)
            :let [energiatodistus (-> (energiatodistus-service/find-energiatodistus
                                        ts/*db*
                                        id)
                                      (assoc-in [:perustiedot :nimi-sv] nil))
                  file-path (service/generate-pdf-as-file energiatodistus
                                                          "sv"
                                                          true)]]
      (t/testing "Test that the generation works even when building name is not set"
        (let [expected-title "Energiatodistus"
              document (-> file-path io/as-file PDDocument/load)
              document-info (.getDocumentInformation document)
              xmp-metadata (->> document .getDocumentCatalog .getMetadata .exportXMPMetadata (.parse xmp-parser))]
          (t/testing "Test for title in the older-style metadata"
            (t/is (= expected-title (.getTitle document-info))))
          (t/testing "Test for title in the XMP metadata"
            (t/is (= expected-title (-> xmp-metadata .getDublinCoreSchema .getTitle))))
          (.close document)))

      (io/delete-file file-path))))

(t/deftest pdf-file-id-test
  (t/is (nil? (energiatodistus-service/file-key nil "fi")))
  (t/is (= (energiatodistus-service/file-key 12345 "fi")
           "energiatodistukset/energiatodistus-12345-fi")))

(t/deftest do-when-signing-test
  (let [f (constantly true)]
    (t/is (= (service/do-when-signing {:tila-id 0} f)
             :not-in-signing))
    (t/is (true? (service/do-when-signing {:tila-id 1} f)))
    (t/is (= (service/do-when-signing {:tila-id 2} f)
             :already-signed))))

(t/deftest find-energiatodistus-digest-test
  (let [{:keys [laatijat energiatodistukset]} (test-data-set)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        id (-> energiatodistukset keys sort first)
        whoami {:id laatija-id}]
    (t/is (= (service/find-energiatodistus-digest db ts/*aws-s3-client* id "fi")
             :not-in-signing))
    (energiatodistus-service/start-energiatodistus-signing! db whoami id)
    (t/is (contains? (service/find-energiatodistus-digest db
                                                          ts/*aws-s3-client*
                                                          id
                                                          "fi")
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
                                                  "fi")
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

(t/deftest sign-energiatodistus-test
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

(t/deftest sign-with-system-energiatodistus-test
  (t/testing "Signing a pdf using the system instead of mpollux"
    (let [{:keys [laatijat energiatodistukset]} (test-data-set)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          ;; The second ET is 2018 version
          id (-> energiatodistukset keys sort second)
          whoami {:id laatija-id}
          {:keys [versio] :as complete-energiatodistus} (complete-energiatodistus-service/find-complete-energiatodistus db id)
          language-code (-> complete-energiatodistus :perustiedot :kieli (energiatodistus-service/language-id->codes) first)]
      (t/testing "Testing assumptions about the energiatodistus in test"
        (t/is (= versio 2018))
        (t/is (or (= language-code "sv") (= language-code "fi")))
        (t/is (= id 2))
        (t/is (= versio 2018)))
      (t/testing "Signing a pdf should succeed"
        (t/is (= (service/sign-with-system {:db             db
                                            :aws-s3-client  ts/*aws-s3-client*
                                            :whoami         whoami
                                            :aws-kms-client ts/*aws-kms-client*
                                            :now            energiatodistus-test-data/time-when-test-cert-not-expired
                                            :id             id
                                            :language       language-code})
                 :ok)))
      #_(t/testing "Trying to sign again should result in :already-signed"
        (t/is (= (service/sign-with-system {:db             db
                                            :aws-s3-client  ts/*aws-s3-client*
                                            :whoami         whoami
                                            :aws-kms-client ts/*aws-kms-client*
                                            :now            energiatodistus-test-data/time-when-test-cert-not-expired
                                            :id             id
                                            :language       language-code})
                 :already-signed)))
      #_(t/testing "The state should result in :already-signed if trying to sign three times in a row"
        (t/is (= (service/sign-with-system {:db             db
                                            :aws-s3-client  ts/*aws-s3-client*
                                            :whoami         whoami
                                            :aws-kms-client ts/*aws-kms-client*
                                            :now            energiatodistus-test-data/time-when-test-cert-not-expired
                                            :id             id
                                            :language       language-code})
                 :already-signed)))))
  (t/testing "Trying to sign a pdf that is already in signing should fail and not change the state"
    (let [{:keys [laatijat energiatodistukset]} (test-data-set)
          laatija-id (-> laatijat keys sort first)
          db (ts/db-user laatija-id)
          id (-> energiatodistukset keys sort first)
          {:keys [kieli]} (complete-energiatodistus-service/find-complete-energiatodistus db id)
          language-code (energiatodistus-service/language-id->codes kieli)
          whoami {:id laatija-id}]
      ;; Put the energiatodistus into signing.
      (energiatodistus-service/start-energiatodistus-signing! db whoami id)
      (let [{:keys [tila-id]} (complete-energiatodistus-service/find-complete-energiatodistus db id)]
        (t/testing "Trying to sign a pdf that is already in signing should return :already-in-signing"
          (t/is (= (service/sign-with-system {:db             db
                                              :aws-s3-client  ts/*aws-s3-client*
                                              :whoami         whoami
                                              :aws-kms-client ts/*aws-kms-client*
                                              :now            energiatodistus-test-data/time-when-test-cert-not-expired
                                              :id             id
                                              :language       language-code})
                   :already-in-signing)))
        (t/testing "Trying to sign a pdf that is already in signing should not change the state"
          (t/is (= (-> (complete-energiatodistus-service/find-complete-energiatodistus db id) :tila-id)
                   tila-id)))))))

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

