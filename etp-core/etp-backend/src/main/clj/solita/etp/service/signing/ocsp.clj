(ns solita.etp.service.signing.ocsp
  "Online Certificate Status Protocol"
  (:import (java.io ByteArrayInputStream)
           (java.nio.charset StandardCharsets)
           (java.security.cert X509Certificate CertificateFactory)
           (org.bouncycastle.asn1.x509 AccessDescription AuthorityInformationAccess Extension)
           (org.bouncycastle.cert.jcajce JcaX509CertificateHolder)))

(defn string->input-stream [s]
  (ByteArrayInputStream. (.getBytes s StandardCharsets/UTF_8)))

(def cert-with-ocsp-pem
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
   -----END CERTIFICATE-----")

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

(def cert-with-ocsp (some-> (CertificateFactory/getInstance "X.509")
                            (.generateCertificate (string->input-stream cert-with-ocsp-pem))))

(def cert-without-ocsp (some-> (CertificateFactory/getInstance "X.509")
                               (.generateCertificate (string->input-stream cert-without-ocsp-pem))))


(defn get-ocsp-uri
  "Gets the OCSP URI from the certificate's Authority Information Access"
  [^X509Certificate certificate]
  (some-> certificate
          JcaX509CertificateHolder.
          (.getExtension Extension/authorityInfoAccess)
          .getParsedValue
          AuthorityInformationAccess/getInstance
          .getAccessDescriptions
          (#(some (fn [access-desc]
                    (when (some-> access-desc .getAccessMethod (= AccessDescription/id_ad_ocsp))
                      (some-> access-desc .getAccessLocation .getName str)))
                  %))))

#_(defn make-ocsp-request [^X509Certificate certificate]
    (let [ocspUrl ""
          issuerUrl ""
          ]
      )
    CertificateSigna
    )
