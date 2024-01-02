(ns solita.etp.service.valvonta-kaytto.hallinto-oikeus-attachment-test
  (:require [clojure.test :as t])
  (:require [solita.etp.service.valvonta-kaytto.hallinto-oikeus-attachment :refer [attachment-for-hallinto-oikeus-id]]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest attachment-for-hallinto-oikeus-id-test
  (t/testing "Unknown hallinto-oikeus-id results in exception"
    (t/is (thrown-with-msg? Exception
                            #"Attachment not found for hallinto-oikeus-id: 666"
                            (attachment-for-hallinto-oikeus-id ts/*db* 666)))))
