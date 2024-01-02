(ns solita.etp.service.energiatodistus-pdf-test
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.common.xlsx :as xlsx]
            [solita.etp.service.energiatodistus-pdf :as service]
            [solita.common.formats :as formats]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.common.certificates-test :as certificates-test]))

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
    {:laatijat laatijat
     :energiatodistukset energiatodistukset}))

(def sis-kuorma-data {:henkilot {:kayttoaste 0.2 :lampokuorma 1}
                      :kuluttajalaitteet {:kayttoaste 0.3 :lampokuorma 1}
                      :valaistus {:kayttoaste 0.3 :lampokuorma 2}})

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
  (let [{:keys [energiatodistukset]} (test-data-set)]
    (doseq [id (-> energiatodistukset keys sort)
            :let [energiatodistus (energiatodistus-service/find-energiatodistus
                                   ts/*db*
                                   id)
                  file-path (service/generate-pdf-as-file energiatodistus
                                                          "sv"
                                                          true)]]
      (t/is (-> file-path io/as-file .exists))
      (io/delete-file file-path))))

(t/deftest pdf-file-id-test
  (t/is (nil? (energiatodistus-service/file-key nil "fi")))
  (t/is (= (energiatodistus-service/file-key 12345 "fi")
           "energiatodistukset/energiatodistus-12345-fi")))

(t/deftest do-when-signing-test
  (let [f (constantly true)]
    (t/is (= (service/do-when-signing {:tila-id 0 } f)
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
                                             energiatodistus-test-data/time-when-test-cert-not-expired
                                             false)
    (t/is (= (service/sign-energiatodistus-pdf db
                                               ts/*aws-s3-client*
                                               whoami
                                               energiatodistus-test-data/time-when-test-cert-not-expired
                                               id
                                               "fi"
                                               nil)
             :already-signed))))
