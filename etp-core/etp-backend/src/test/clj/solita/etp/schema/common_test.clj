(ns solita.etp.schema.common-test
  (:require [clojure.test :as t]
            [schema.core :as schema]
            [solita.etp.schema.common :as common]))

(t/deftest valid-henkilotunnus?-test

  ;; Henkilotunnus is valid if `schema/check` dosen't return
  ;; a validation error.
  (t/testing "Valid henkilotunnus"
    (t/is (nil? (schema/check common/Henkilotunnus "030516+9037")))
    (t/is (nil? (schema/check common/Henkilotunnus "030516+9026")))

    (t/is (nil? (schema/check common/Henkilotunnus "131052-308T")))

    (t/is (nil? (schema/check common/Henkilotunnus "130200A892S")))
    (t/is (nil? (schema/check common/Henkilotunnus "020504A902E")))
    (t/is (nil? (schema/check common/Henkilotunnus "130200A892S")))

    (t/is (nil? (schema/check common/Henkilotunnus "020504B904H")))
    (t/is (nil? (schema/check common/Henkilotunnus "010516B903X")))
    (t/is (nil? (schema/check common/Henkilotunnus "010516B902W")))
    (t/is (nil? (schema/check common/Henkilotunnus "131052B308T")))

    (t/is (nil? (schema/check common/Henkilotunnus "020516C903K")))
    (t/is (nil? (schema/check common/Henkilotunnus "020516C902J")))

    (t/is (nil? (schema/check common/Henkilotunnus "030516D9037")))
    (t/is (nil? (schema/check common/Henkilotunnus "030516D9026")))

    (t/is (nil? (schema/check common/Henkilotunnus "010501E9032")))
    (t/is (nil? (schema/check common/Henkilotunnus "020502E902X")))

    (t/is (nil? (schema/check common/Henkilotunnus "020503F9037")))

    (t/is (nil? (schema/check common/Henkilotunnus "010594Y9032")))
    (t/is (nil? (schema/check common/Henkilotunnus "010594Y9021")))

    (t/is (nil? (schema/check common/Henkilotunnus "020594X903P")))
    (t/is (nil? (schema/check common/Henkilotunnus "020594X902N")))
    (t/is (nil? (schema/check common/Henkilotunnus "130200X892S")))

    (t/is (nil? (schema/check common/Henkilotunnus "030594W903B")))
    (t/is (nil? (schema/check common/Henkilotunnus "030694W9024")))

    (t/is (nil? (schema/check common/Henkilotunnus "040594V9030")))
    (t/is (nil? (schema/check common/Henkilotunnus "040594V902Y")))

    (t/is (nil? (schema/check common/Henkilotunnus "050594U903M")))
    (t/is (nil? (schema/check common/Henkilotunnus "050594U902L"))))

  ;; Henkilotunnus is invalid if `schema/check` returns
  ;; some validation error.
  (t/testing "Inalid henkilotunnus"
    (t/testing "nil"
      (t/is (some? (schema/check common/Henkilotunnus nil))))
    (t/testing "Invalid century signs"
      (t/is (some? (schema/check common/Henkilotunnus "030516G9037")))
      (t/is (some? (schema/check common/Henkilotunnus "030516Z9037")))
      (t/is (some? (schema/check common/Henkilotunnus "030516T9037"))))
    (t/testing "Invalid checksums"
      (t/is (some? (schema/check common/Henkilotunnus "131053-308T")))
      (t/is (some? (schema/check common/Henkilotunnus "0131053-308T")))
      (t/is (some? (schema/check common/Henkilotunnus "130200A891S")))
      (t/is (some? (schema/check common/Henkilotunnus "040594V901Y")))
      (t/is (some? (schema/check common/Henkilotunnus "020594X904P")))
      (t/is (some? (schema/check common/Henkilotunnus "131052B300T"))))
    (t/testing "Extra characters"
      (t/is (some? (schema/check common/Henkilotunnus "030594W903Basd")))
      (t/is (some? (schema/check common/Henkilotunnus "030694W9024123")))
      (t/is (some? (schema/check common/Henkilotunnus "040594V9030Y")))
      (t/is (some? (schema/check common/Henkilotunnus "A040594V9030")))
      (t/is (some? (schema/check common/Henkilotunnus "0A40594V9030")))
      (t/is (some? (schema/check common/Henkilotunnus "040A594V9030")))
      (t/is (some? (schema/check common/Henkilotunnus "04059A4V9030")))
      (t/is (some? (schema/check common/Henkilotunnus "1A0200A892S")))
      (t/is (some? (schema/check common/Henkilotunnus "040594V902YA"))))))

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
  (t/is (some? (schema/check common/TEOVTtunnus "TE003712345672")))
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

(t/deftest valid-email?-test
  ;; Invalid cases
  (t/is (some? (schema/check common/Email "")))
  (t/is (some? (schema/check common/Email nil)))
  (t/is (some? (schema/check common/Email "plainaddress")))
  (t/is (some? (schema/check common/Email "@missingusername.com")))
  (t/is (some? (schema/check common/Email "username@.com")))
  (t/is (some? (schema/check common/Email "username@com")))
  (t/is (some? (schema/check common/Email "username@.com.")))
  (t/is (some? (schema/check common/Email "username@-example.com")))
  (t/is (some? (schema/check common/Email "username@example..com")))
  (t/is (some? (schema/check common/Email "username@example.com (Joe Smith)")))
  (t/is (some? (schema/check common/Email "username@example,com")))
  (t/is (some? (schema/check common/Email "username@.example.com")))
  (t/is (some? (schema/check common/Email "username@ex_ample.com")))
  (t/is (some? (schema/check common/Email "username@exam!ple.com")))
  (t/is (some? (schema/check common/Email "username@.com.com")))
  (t/is (some? (schema/check common/Email "username@%*.com")))
  (t/is (some? (schema/check common/Email "username..2002@example.com")))
  (t/is (some? (schema/check common/Email "username.@example.com")))
  (t/is (some? (schema/check common/Email ".username@example.com")))

  ;; Valid cases
  (t/is (nil? (schema/check common/Email "bob@example.com")))
  (t/is (nil? (schema/check common/Email "Bob@example.com")))
  (t/is (nil? (schema/check common/Email "alice.smith@example.co.uk")))
  (t/is (nil? (schema/check common/Email "user+mailbox/department=shipping@example.com")))
  (t/is (nil? (schema/check common/Email "customer/department=shipping@example.com")))
  (t/is (nil? (schema/check common/Email "user.name+tag+sorting@example.com")))
  (t/is (nil? (schema/check common/Email "x@example.com")))
  (t/is (nil? (schema/check common/Email "example-indeed@strange-example.com")))
  (t/is (nil? (schema/check common/Email "user_name@example.com")))
  (t/is (nil? (schema/check common/Email "user-name@example.org")))
  (t/is (nil? (schema/check common/Email "user.name@sub.example.com"))))