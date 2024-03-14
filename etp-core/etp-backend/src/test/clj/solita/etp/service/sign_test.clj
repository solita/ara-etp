(ns solita.etp.service.sign-test
  (:require [clojure.test :as t]
            [solita.etp.service.sign :as sign]
            [solita.etp.test-system :as ts])
  (:import (java.nio.charset StandardCharsets)))

(t/use-fixtures :each ts/fixture)

(t/deftest sign-test
  (let [message (.getBytes "hello world" StandardCharsets/UTF_8)
        signature (.readAllBytes (sign/sign ts/*aws-kms-client* message))]
    (t/testing "verify with valid signature returns true"
      (t/is (solita.etp.service.sign/verify ts/*aws-kms-client* signature message)))
    (t/testing "verify with wrong message throws exception"
      (let [wrong-message (.getBytes "hello world!" StandardCharsets/UTF_8)]
        (t/is (thrown? Exception (sign/verify ts/*aws-kms-client* signature wrong-message)))))))
