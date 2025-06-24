(ns solita.etp.schema.aineisto-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.aineisto :as aineisto]))

(t/deftest valid-ipv4-test
  (t/testing "Valid IPv4 addresses"
    (t/is (nil? (schema/check aineisto/IPv4AddressOrCIDR "10.0.0.1")))))

(t/deftest valid-cidr-test
  (t/testing "Valid IPv4 addresses"
    (t/is (nil? (schema/check aineisto/IPv4AddressOrCIDR "10.0.0.0/24")))))

(t/deftest invalid-ipv4-test
  (t/testing "Invalid IPv4 addresses"
    (t/is (some? (schema/check aineisto/IPv4AddressOrCIDR "10.0.0.256")))
    (t/is (some? (schema/check aineisto/IPv4AddressOrCIDR "10.0.0")))))
