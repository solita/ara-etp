(ns solita.etp.service.csv-to-s3-test
  (:require [solita.etp.service.csv-to-s3 :as csv-to-s3]
            [solita.etp.service.file :as file]
            [clojure.test :as t]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest test-public-csv-to-s3
  (t/testing "Public csv doesn't exist before generating"
    (t/is (false? (file/file-exists? ts/*aws-s3-client* "/api/csv/public/energiatodistukset.csv"))))

  (t/testing "Public csv exists after generating"
    (csv-to-s3/update-public-csv-in-s3! ts/*db* {:id -5 :rooli 2} ts/*aws-s3-client* {:where nil})
    (t/is (true? (file/file-exists? ts/*aws-s3-client* "/api/csv/public/energiatodistukset.csv")))))
