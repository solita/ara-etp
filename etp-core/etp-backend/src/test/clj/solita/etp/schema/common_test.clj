(ns solita.etp.schema.common-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.common :as common]))

(t/deftest valid-henkilotunnus?-test
  (t/is (some? (schema/check common/Henkilotunnus "131052B308T")))
  (t/is (some? (schema/check common/Henkilotunnus "131053-308T")))
  (t/is (some? (schema/check common/Henkilotunnus "0131053-308T")))
  (t/is (nil? (schema/check common/Henkilotunnus "131052-308T")))
  (t/is (some? (schema/check common/Henkilotunnus "130200X892S")))
  (t/is (some? (schema/check common/Henkilotunnus "130200A891S")))
  (t/is (some? (schema/check common/Henkilotunnus "1A0200A892S")))
  (t/is (nil? (schema/check common/Henkilotunnus "130200A892S"))))

(t/deftest valid-ytunnus?-test
  (t/is (nil? (schema/check common/Ytunnus "1234567-1")))
  (t/is (some? (schema/check common/Ytunnus "1234567-2")))
  (t/is (nil? (schema/check common/Ytunnus "1060155-5")))
  (t/is (some? (schema/check common/Ytunnus "1060155-6")))
  (t/is (some? (schema/check common/Ytunnus "1060155-7")))
  (t/is (nil? (schema/check common/Ytunnus "0000001-9")))

  (t/is (some? (schema/check common/Ytunnus "a060155-7")))
  (t/is (some? (schema/check common/Ytunnus "aaaaaaa-b"))))

(t/deftest valid-ovt-tunnus?-test
  (t/is (nil? (schema/check common/OVTtunnus "003712345671")))
  (t/is (nil? (schema/check common/OVTtunnus "0037123456710")))
  (t/is (nil? (schema/check common/OVTtunnus "00371234567101")))
  (t/is (nil? (schema/check common/OVTtunnus "003712345671012")))
  (t/is (nil? (schema/check common/OVTtunnus "00371234567101234")))

  (t/is (some? (schema/check common/OVTtunnus "003712345671012345")))
  (t/is (some? (schema/check common/OVTtunnus "000012345671")))
  (t/is (some? (schema/check common/OVTtunnus nil)))
  (t/is (some? (schema/check common/OVTtunnus ""))))

(t/deftest valid-iban?-test
  (t/is (nil? (schema/check common/IBAN "FI1410093000123458")))
  (t/is (nil? (schema/check common/IBAN "BR1500000000000010932840814P2")))

  (t/is (some? (schema/check common/IBAN "FI1410093000123459")))
  (t/is (some? (schema/check common/IBAN "FI14")))
  (t/is (some? (schema/check common/IBAN "")))
  (t/is (some? (schema/check common/IBAN nil))))

(t/deftest valid-te-ovt-tunnus?-test
  (t/is (nil? (schema/check common/TEOVTtunnus "TE003712345671")))
  (t/is (nil? (schema/check common/TEOVTtunnus "TE0037123456710")))
  (t/is (nil? (schema/check common/TEOVTtunnus "TE00371234567101234")))

  (t/is (some? (schema/check common/TEOVTtunnus "003712345671")))
  (t/is (some? (schema/check common/TEOVTtunnus "TE003712345672" )))
  (t/is (some? (schema/check common/TEOVTtunnus nil)))
  (t/is (some? (schema/check common/TEOVTtunnus ""))))

(t/deftest valid-verkkolaskuosoite?-test
  (t/is (nil? (schema/check common/Verkkolaskuosoite "003712345671")))
  (t/is (nil? (schema/check common/Verkkolaskuosoite "FI1410093000123458")))
  (t/is (nil? (schema/check common/Verkkolaskuosoite "TE003712345671")))

  (t/is (some? (schema/check common/Verkkolaskuosoite "FI1410093000123459")))
  (t/is (some? (schema/check common/Verkkolaskuosoite "")))
  (t/is (some? (schema/check common/Verkkolaskuosoite nil))))

(t/deftest valid-rakennustunnus?-test
  (t/is (nil? (schema/check common/Rakennustunnus "1035150826")))
  (t/is (nil? (schema/check common/Rakennustunnus "103515074X")))

  (t/is (some? (schema/check common/Rakennustunnus "103515074x")))
  (t/is (some? (schema/check common/Rakennustunnus "100012345A")))
  (t/is (some? (schema/check common/Rakennustunnus nil))))
