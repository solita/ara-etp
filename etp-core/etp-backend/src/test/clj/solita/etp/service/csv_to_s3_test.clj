(ns solita.etp.service.csv-to-s3-test
  (:require [solita.etp.service.csv-to-s3 :as csv-to-s3]
            [solita.etp.service.file :as file]
            [clojure.test :as t]
            [clojure.string :as str]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as test-data.laatija]
            [solita.etp.test-data.energiatodistus :as test-data.energiatodistus])
  (:import (java.io BufferedReader InputStreamReader)))

(t/use-fixtures :each ts/fixture)

(t/deftest test-public-csv-to-s3
  (t/testing "Public csv doesn't exist before generating"
    (t/is (false? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key))))

  (t/testing "Public csv exists after generating"
    (csv-to-s3/update-public-csv-in-s3! ts/*db* {:id -5 :rooli 2} ts/*aws-s3-client* {:where nil})
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key)))))

(defn get-first-two-lines-from-csv [key]
  (with-open [csv-file (file/find-file ts/*aws-s3-client* key)]
    (let [reader (BufferedReader. (InputStreamReader. csv-file))
          ;; CSV headers
          header (.readLine reader)
          first-line (.readLine reader)
          _ (prn "first-line:" first-line)
          second-line (.readLine reader)]
      [header first-line second-line])))

(t/deftest test-update-public-csv-in-s3!-handles-empty-query
  (t/testing "CSV contains expected columns when energiatodistus exists"
    (csv-to-s3/update-public-csv-in-s3! ts/*db* {:id -5 :rooli 2} ts/*aws-s3-client* {:where nil})
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key)))

    (let [[header & _] (get-first-two-lines-from-csv csv-to-s3/public-csv-key)]
      ;; Verify required columns exist in header with proper path format
      (t/is (str/includes? header "Perustiedot / Alakayttotarkoitus-fi"))
      (t/is (str/includes? header "Tulokset / E-luokka-rajat"))
      (t/is (str/includes? header "Tulokset / Kaytettavat-energiamuodot / Kaukolampo-kerroin"))

      ;; Verify hidden columns don't exist
      (t/is (not (str/includes? header "tila-id")))
      (t/is (not (str/includes? header "laatija-id")))
      (t/is (not (str/includes? header "yritys"))))))

(t/deftest test-update-public-csv-in-s3!-with-data
  (let [laatija-id (first (keys (test-data.laatija/generate-and-insert! 1)))
        energiatodistus (test-data.energiatodistus/generate-and-insert! 1 2018 true laatija-id)]

     ;; Debug prints to verify test data
    (prn "Debug - Created laatija-id:" laatija-id)
    (prn "Debug - Created energiatodistus:" energiatodistus)

    ;; First ensure CSV doesn't exist
    (t/is (false? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key)))

    ;; Generate CSV
    (csv-to-s3/update-public-csv-in-s3! ts/*db* {:id -5 :rooli 2} ts/*aws-s3-client* {:where nil})

    ;; Verify CSV was created
    (t/is (true? (file/file-exists? ts/*aws-s3-client* csv-to-s3/public-csv-key)))

    (t/testing "CSV contains expected data from energiatodistus"
      (let [[header first-line second-line :as lines] (get-first-two-lines-from-csv csv-to-s3/public-csv-key)]
        (prn "Debug - CSV content:" lines)
        (t/is (some? header) "Header should exist")
        (t/is (some? first-line) "First line should exist")
        (when first-line
          (t/is (str/includes? first-line (str (:id energiatodistus)))
                (str "Line should contain energiatodistus id: " (:id energiatodistus))))))))
