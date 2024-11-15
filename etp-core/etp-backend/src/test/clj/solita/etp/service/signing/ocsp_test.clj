(ns solita.etp.service.signing.ocsp-test
  (:require
    [clojure.test :as t]
    [solita.etp.service.signing.ocsp :as ocsp-service]
    )
  (:import (java.io ByteArrayInputStream)
           (java.nio.charset StandardCharsets)
           (java.security.cert CertificateFactory)))

(defn string->input-stream [s]
  (ByteArrayInputStream. (.getBytes s StandardCharsets/UTF_8)))

(def cert-chain-with-ocsp
  {:leaf
   "-----BEGIN CERTIFICATE-----
    MIIDfTCCAmWgAwIBAgIQKpypOZKgm9xXGXh1OgGGizANBgkqhkiG9w0BAQsFADAa
    MRgwFgYDVQQDDA9FYXN5LVJTQSBTdWItQ0EwHhcNMjQxMTEzMTgxMDA3WhcNMjcw
    MjE2MTgxMDA3WjAbMRkwFwYDVQQDDBBzb21lX2NlcnRpZmljYXRlMIIBIjANBgkq
    hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkMFYjl5Sodr6oxjL27joA7BSt56k7PYJ
    W9lIQhj7mviio3yYdDF7UHPX+m4SJ/kvlhHpRhhVEHQ2xzfILLq9AijRXuytd8Pu
    TKJXm1r8Ljlv68PZ3iBSYhK+pAvzuBPu9uHl7isnFmEtlcpSG/MFBlrnn6sCf3b+
    fF2/6F+ckWPr7DPTqz1RQ9HjV0PBnSf9U2jKcwa7Q4bODwBjvKgpqXN5KvF6rMio
    9Qr8aaNyv4DRd8j2MMo7SuUFEw5L1KC9nTnXq455nO8ZVvk/1q4R5NEJq+A/QVZb
    XHF1heFLeCDFkewYvMrJYkWO9G1YoRaUfc8VPwiTrOV5tRpax8eLlwIDAQABo4G9
    MIG6MDIGCCsGAQUFBwEBBCYwJDAiBggrBgEFBQcwAYYWaHR0cDovL2xvY2FsaG9z
    dDoyMDYxLzAJBgNVHRMEAjAAMB0GA1UdDgQWBBRSDYXLhVWPUOxZANDgghPTsqsf
    aDBNBgNVHSMERjBEgBQ0mVClsFXUeKKN2LmdcbgnCNw4naEapBgwFjEUMBIGA1UE
    AwwLRWFzeS1SU0EgQ0GCEDpuK63HG/7CI8tqqS6dPm4wCwYDVR0PBAQDAgbAMA0G
    CSqGSIb3DQEBCwUAA4IBAQBvKs02M/Dg09Xfi3Th01H3wfH6jVV3yvqfYH5Eyc0r
    ASNx33Gfu9qC+BASNkhTjvlvNyKKHpCRuZ0Mzf8Ynba395yXL5CPE/41wYlVwz30
    hpHCJ1gPRtZMZAiGp4eqBsFEOFpgkQSjZBLjcmAkOShj1EbUzM9SgXtVQKR5llwu
    CqAYQIuNK+xnzX01NUc9lhg8kojfdw6x92PUYDu87xRF6JRVhUGlM1M8Qg30zbkB
    irE4qiPECNbq1RMtOCYlVanKSyY1gZH+KbHGjLD3Vt8xEo7sA5hGjW4z2YIt9a4F
    NZ815+qtiPXwzFnnKLYS6KoRhms+MKrL1GlkGSFAjjDx
    -----END CERTIFICATE-----"
   :intermediate
   "-----BEGIN CERTIFICATE-----
   MIIDfzCCAmegAwIBAgIQOm4rrccb/sIjy2qpLp0+bjANBgkqhkiG9w0BAQsFADAW
   MRQwEgYDVQQDDAtFYXN5LVJTQSBDQTAeFw0yNDExMTMxODEwMDFaFw0yNzAyMTYx
   ODEwMDFaMBoxGDAWBgNVBAMMD0Vhc3ktUlNBIFN1Yi1DQTCCASIwDQYJKoZIhvcN
   AQEBBQADggEPADCCAQoCggEBANCoczgc4Ksyt1O01TkcKIeA1NPK2UXQaaq/a6ds
   DJbqtQoFRBzcfGta1gUaTele3a/X9cxfCeiHWE7HymBYYgvlKmWqK4F5j5Yn5hkO
   cCAvdNEqmTGUKck4+4V86pXi7S8uQ352NzVqgTFVd9pnxtA53v9PZzC/d/6QkhDW
   pJCcZHFMHS2zZsdMLQ0tIo+oqhL3oIg5PtiPYhTu1pIFO8FhlfIEWnvXUNaMYwRq
   mqcyX1nwkBVbMfD21s8EFRaEqncDnVj4SpT07LWuNAlBKMQynT6xxXf1S1OW/QUv
   +FWS3VuBCFJOJNFVy/ln2X0OlTB3Q/rwUQixwIPS/ya3sjUCAwEAAaOBxDCBwTAy
   BggrBgEFBQcBAQQmMCQwIgYIKwYBBQUHMAGGFmh0dHA6Ly9sb2NhbGhvc3Q6MjA2
   MC8wDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUNJlQpbBV1Hiijdi5nXG4JwjcOJ0w
   UQYDVR0jBEowSIAU8CkC2Bdsv+xy+QAO03J0FS4Zni+hGqQYMBYxFDASBgNVBAMM
   C0Vhc3ktUlNBIENBghRNJ70P771kisZ6YcaopFEjOOoq/DALBgNVHQ8EBAMCAQYw
   DQYJKoZIhvcNAQELBQADggEBADHoCi8Xa9cnALtcR2GQ/5msMdjxkGN24hvUbPbN
   z4sr+kJ5Xb3LBIUb4aPU9o+RO9H4/3+ZKhtFxLWisKa6Mf/kA/reOcxzpZIMDbv4
   nn1GjhE9xhHkmtQ+nbH2K0y71qmTsLa5fPYTeei2i+RKAx6L/6XGX6UtZ6XWUnPh
   U22epo+E1q6m3qw19wFf0koWcZu3vj1h2B+bp+3a4saF7+49pk2MSyItYH7HuESd
   +vqnnxZRY4QIPSifaq9cI0kQjnnrGipsHQxSyDBKfEivOMtx8i54U4oHOJotjfuB
   n6UmLODg7UpE3mCAff4FhTEBJEnD2Ibdw1Vx5u8OoG/iWhA=
   -----END CERTIFICATE-----"
   })

(def cert-with-ocsp-pem-leaf (:leaf cert-chain-with-ocsp))
(def cert-with-ocsp-pem-int (:intermediate cert-chain-with-ocsp))

(def cert-without-ocsp-pem "-----BEGIN CERTIFICATE-----
  MIIErDCCApSgAwIBAgIBAjANBgkqhkiG9w0BAQsFADBrMQswCQYDVQQGEwJVUzET
  MBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNjbzETMBEG
  A1UECgwKTXkgQ29tcGFueTEaMBgGA1UEAwwRRVRQIExPQ0FMIERFViBJTlQwHhcN
  MjQwNDI5MDcwNDMzWhcNMjUwNTA5MDcwNDMzWjBsMQswCQYDVQQGEwJVUzETMBEG
  A1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNjbzETMBEGA1UE
  CgwKTXkgQ29tcGFueTEbMBkGA1UEAwwSRVRQIExPQ0FMIERFViBMRUFGMIIBIjAN
  BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJ9HVkn7sN0GqUXbMwwR+IFyLQ+s
  y5dpDhhNqonGHHwlN8Qlzsx/p1+EhBQBWF0LVsv5fvdP1CXZDvMrJOJaojk0hTpA
  dUSd4siwOgcjsgjTLwOTUK16/Bhwe4Q/Se5qAmKP7qLfWYxJajsMj4GypQzmT7HL
  vADoG68t6e1n5VXbo8jBmbNJDXKD+WssmwgcHs6v4pQzshpBhMWGpt5I/V3pFJnf
  fzNURao+reGBunaTkNOL91RFqqPTn3NPsIG7hWBtPfjCEq+HqNoXIIyeRi2ql0iI
  cQvDFt1tFt+hrxMe8e/VDrqWHNoVZDzHcYU4K5UtfhgMTbo7178Pgn8tmwIDAQAB
  o1owWDAJBgNVHRMEAjAAMAsGA1UdDwQEAwIGwDAdBgNVHQ4EFgQU8N0s+L4izOTP
  S+p6tef/agSxe2swHwYDVR0jBBgwFoAUG9vVSBOnUi+3YwPFTmPlq8/XmqkwDQYJ
  KoZIhvcNAQELBQADggIBAHxSXqoBrIRzYxBZZJ4HNLS4k0jxJlFdyH0vum4V50/l
  3vFaWNBtvtrjE/zAdQxXbKf/LHgrzzM4rizUyhnFyyK2sWOsIsD8RxHuEkHUOXus
  wVxbgTDpwblMm8ogCawZ6OV9OyGL+HdJq+n554RKrMEjQ1e/nNPtcTe9LS13twjb
  4iaRaP8xpOqSYYC0o47EA3QCOcLLSy9I+daDatUxnKx8rl3P1n51hLNmA/oXsQnb
  EwDlnTZdy8ORvQDom6lx4XQi1sLNeIRxLYleL18UDmMzRhlqJg/fRpmQPPqWsGkx
  y4WnwiBbwOMELHlVnkNyIwd+E9M2UrUkUXL8WKExlB1cm747PAjuYLUEq+3mhw+C
  774oQRLPyAZFQO2pDtUTA6IOWTx0uqpJ3NTSV9knKpW1wiuTxVpKGIAUMUUUe5uZ
  cf7nC9osvn9TBJ1Gyb0Owz4YOXBu5lj74AavHJPGiqEwrqBC8FfClGG7xDq665p+
  ZDJHWHX3bNXJXshO2gF1oHWyGTpDsZhwhVePOW1r0R5FqH/Kt57bisSJPWLlCsQo
  DYXkSw9kHMMPJ688tE7Tt66vQT+YqNF+xiUd2gixbiLK4ywQJWGrbms8YeIJn1bX
  FNgIAmCQa6O8MM/6eGBsirivrMJTY2fB/CCrhZyFsvaXxI0HA6M4ontwbFR0LhO5
  -----END CERTIFICATE-----"
  )

(def cert-with-ocsp-leaf (some-> (CertificateFactory/getInstance "X.509")
                                 (.generateCertificate (string->input-stream cert-with-ocsp-pem-leaf))))

(def cert-with-ocsp-int (some-> (CertificateFactory/getInstance "X.509")
                                 (.generateCertificate (string->input-stream cert-with-ocsp-pem-int))))

(def cert-without-ocsp (some-> (CertificateFactory/getInstance "X.509")
                               (.generateCertificate (string->input-stream cert-without-ocsp-pem))))

(t/deftest get-ocsp-uri-test
  (t/testing "Can get OCSP URI from a certificate that has it"
    (let [ocsp-uri (ocsp-service/get-ocsp-uri cert-with-ocsp-leaf)]
      (t/is (= "http://localhost:2061/" ocsp-uri))))
  (t/testing "Can get OCSP URI from a certificate that has a different one"
    (let [ocsp-uri (ocsp-service/get-ocsp-uri cert-with-ocsp-int)]
      (t/is (= "http://localhost:2060/" ocsp-uri))))
  (t/testing "Return nil when get OCSP URI from a certificate that does not have it"
    (let [ocsp-uri (ocsp-service/get-ocsp-uri cert-without-ocsp)]
      (t/is (nil? ocsp-uri)))))

(t/deftest hmm200
  (t/testing "hmm"
    (let [hmm1 (ocsp-service/make-ocsp-request cert-with-ocsp-leaf cert-with-ocsp-int)]
      (println hmm1))))


#_(t/deftest make-ocsp-request
  (t/testing ""
    (let [ocsp-uri (ocsp-service/make-ocsp-request cert-with-ocsp-leaf cert-with-ocsp-int)]
      (t/is (nil? ocsp-uri)))))
