(ns solita.etp.service.energiatodistus-pdf-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [solita.common.formats :as formats]
            [solita.common.xlsx :as xlsx]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as service]
            [solita.etp.service.energiatodistus-signing :as signing-service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-system :as ts])
  (:import (java.io InputStream)
           (java.time Instant)
           (org.apache.pdfbox.pdmodel PDDocument)
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
                                                          true
                                                          "allekirjoitus-id")]]
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
          (t/testing "Test for laatija-allekirjoitus-id in the XMP metadata"
            (t/is (= "allekirjoitus-id" (.getCustomMetadataValue document-info "laatija-allekirjoitus-id"))))
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
                                                          true
                                                          "allekirjoitus-id")]]
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

(t/deftest ^{:broken-on-windows-test "Couldn't delete .. signable.pdf"} sign-with-system-signature-test
  (t/testing "Signing a pdf using the system instead of mpollux"
    (with-bindings {#'solita.etp.service.signing.pdf-sign/get-tsp-source solita.etp.test-timeserver/get-tsp-source-in-test}
      (let [{:keys [laatijat energiatodistukset]} (test-data-set)
            laatija-id (-> laatijat keys sort first)
            db (ts/db-user laatija-id)
            ;; The second ET is 2018 version
            id (-> energiatodistukset keys sort second)
            whoami {:id laatija-id :rooli 0}
            complete-energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus db id)
            language-code (-> complete-energiatodistus :perustiedot :kieli (energiatodistus-service/language-id->codes) first)]

        (t/testing "The signed document's signature should be exist."
          (signing-service/sign-with-system {:db             db
                                             :aws-s3-client  ts/*aws-s3-client*
                                             :whoami         whoami
                                             :aws-kms-client ts/*aws-kms-client*
                                             :now            (Instant/now)
                                             :id             id})
          (with-open [^InputStream pdf-bytes (service/find-energiatodistus-pdf db ts/*aws-s3-client* whoami id language-code)
                      xout (java.io.ByteArrayOutputStream.)]
            (io/copy pdf-bytes xout)))))))

