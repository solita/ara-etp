(ns solita.etp.security-test
  (:require [clojure.test :as t]
            [solita.etp.security :as security]))

(t/deftest log-safe-henkilotunnus-test
  (t/is (= "" (security/log-safe-henkilotunnus nil)))
  (t/is (= "0101" (security/log-safe-henkilotunnus "0101")))
  (t/is (= "010101A****" (security/log-safe-henkilotunnus "010101A000A"))))
