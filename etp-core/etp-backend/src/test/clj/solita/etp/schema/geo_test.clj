(ns solita.etp.schema.geo-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.geo :as geo]))

(t/deftest valid-postinumero?-test
  (t/is (some? (schema/check geo/PostinumeroFI nil)))
  (t/is (some? (schema/check geo/PostinumeroFI 00100)))
  (t/is (some? (schema/check geo/PostinumeroFI "00100, Helsinki")))
  (t/is (some? (schema/check geo/PostinumeroFI "0")))

  (t/is (nil? (schema/check geo/PostinumeroFI "00100"))))