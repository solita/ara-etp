(ns solita.etp.test-data.energiatodistus
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test.check.generators :as test-generators]
            [flathead.deep :as deep]
            [schema-generators.generators :as g]
            [schema.core :as schema]
            [solita.common.logic :as logic]
            [solita.common.schema :as xschema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.energiatodistus-signing :as energiatodistus-signing-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]
            [solita.etp.test-timeserver :as test-timeserver])
  (:import (eu.europa.esig.dss.pades PAdESSignatureParameters)
           (java.io FileInputStream ObjectInputStream)
           (java.time Instant)))

(defn not-escaped-backslash? [generated-string]
  (not (str/includes? generated-string "\\")))

(def string-generator
  "Pure string-ascii generator can generate \\ which breaks test
   when the generated string goes to PostgreSQL like search"
  (test-generators/such-that not-escaped-backslash?
                             test-generators/string-ascii))

(def generators {schema/Str                   string-generator
                 schema/Num                   (g/always 1.0M)
                 common-schema/Num1           (g/always 1.0M)
                 common-schema/NonNegative    (g/always 1.0M)
                 common-schema/IntNonNegative (g/always 1)})

(defn schema-by-version-and-ready-for-signing [versio ready-for-signing?]
  (cond->> (if (= versio 2013)
             energiatodistus-schema/EnergiatodistusSave2013
             energiatodistus-schema/EnergiatodistusSave2018)
           ready-for-signing? (deep/map-values record?
                                               (logic/when* xschema/maybe? :schema))))

(defn sisainen-kuorma [versio id]
  (-> (energiatodistus-service/find-sisaiset-kuormat ts/*db* versio)
      (->> (filter (comp (partial = id) :kayttotarkoitusluokka-id)))
      first
      (dissoc :kayttotarkoitusluokka-id)))

(defn generate-add [versio ready-for-signing?]
  (generators/complete
    {:perustiedot                     (merge
                                        {:kieli           (rand-int 2)
                                         :kayttotarkoitus "YAT"}
                                        (when (= versio 2018)
                                          {:laatimisvaihe (rand-int 2)}))
     :lahtotiedot                     {:ilmanvaihto {:tyyppi-id (rand-int 7)}
                                       :lammitys    {:lammitysmuoto-1 {:id (rand-int 10)}
                                                     :lammitysmuoto-2 {:id (rand-int 10)}
                                                     :lammonjako      {:id (rand-int 13)}}
                                       :sis-kuorma  (sisainen-kuorma versio 1)}
     :laskutettava-yritys-id          nil
     :laskutusosoite-id               -1
     :korvattu-energiatodistus-id     nil
     :draft-visible-to-paakayttaja    false
     :bypass-validation-limits        false
     :bypass-validation-limits-reason nil}
    (schema-by-version-and-ready-for-signing versio
                                             ready-for-signing?)
    generators))

(defn generate-adds [n versio ready-for-signing?]
  (repeatedly n #(generate-add versio ready-for-signing?)))

(def generate-updates generate-adds)

(defn generate-adds-with-zeros [n versio]
  (map #(assoc-in %
                  [:lahtotiedot :rakennusvaippa]
                  {:alapohja          {:ala 0 :U 0.03}
                   :ikkunat           {:ala 0 :U 0.4}
                   :ylapohja          {:ala 0 :U 0.03}
                   :ilmatilavuus      1M
                   :lampokapasiteetti 1M
                   :ilmanvuotoluku    1M
                   :ulkoseinat        {:ala 1M :U 0.05}
                   :kylmasillat-UA    0.1
                   :ulkoovet          {:ala 0 :U 0.2}})
       (generate-adds n versio true)))

(defn insert! [energiatodistus-adds laatija-id]
  (mapv #(:id (energiatodistus-service/add-energiatodistus!
                (ts/db-user laatija-id)
                {:id laatija-id}
                (if (-> % :perustiedot (contains? :uudisrakennus))
                  2013
                  2018)
                %))
        energiatodistus-adds))

(defn generate-and-insert!
  ([versio ready-for-signing? laatija-id]
   (first (generate-and-insert! 1 versio ready-for-signing? laatija-id)))
  ([n versio ready-for-signing? laatija-id]
   (let [energiatodistus-adds (generate-adds n versio ready-for-signing?)]
     (zipmap (insert! energiatodistus-adds laatija-id) energiatodistus-adds))))

(defn generate-pdf-as-file-mock [_ _ _ _]
  (let [in "src/test/resources/energiatodistukset/signing-process/generate-pdf-as-file.pdf"
        out (->> (java.util.UUID/randomUUID)
                 .toString
                 (format "%s/energiatodistus-in-system-signing-test.pdf")
                 (str "tmp-energiatodistukset/"))]
    (io/make-parents out)
    (io/copy (io/file in) (io/file out))
    out))

(defn get-parameters-in-test [_]
  (with-open [in (FileInputStream. "src/test/resources/energiatodistukset/signing-process/stateful-parameters")
              object-input-stream (ObjectInputStream. in)]
    ^PAdESSignatureParameters (.readObject object-input-stream)))

(defn sign-pdf-at-time! [energiatodistus-id laatija-id now]
  (with-bindings {#'solita.etp.service.signing.pdf-sign/get-signature-parameters get-parameters-in-test
                  #'solita.etp.service.energiatodistus-pdf/generate-pdf-as-file  generate-pdf-as-file-mock
                  #'solita.etp.service.signing.pdf-sign/get-tsp-source           test-timeserver/get-tsp-source-in-test}
    (let [language (-> (energiatodistus-service/find-energiatodistus
                         ts/*db*
                         energiatodistus-id)
                       :perustiedot
                       :kieli)
          laatija-allekirjoitus-id (-> (laatija-service/find-laatija-by-id ts/*db* laatija-id) :allekirjoitus-id)]
      (doseq [language-code (energiatodistus-service/language-id->codes language)]
        (energiatodistus-signing-service/find-energiatodistus-digest
          ts/*db*
          ts/*aws-s3-client*
          energiatodistus-id
          language-code
          laatija-allekirjoitus-id)
        (energiatodistus-signing-service/sign-energiatodistus-pdf
          ts/*db*
          ts/*aws-s3-client*
          {:id laatija-id :sukunimi "Specimen-Potex"}
          now
          energiatodistus-id
          language-code
          {:signature "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",
           :chain     ["MIIDfTCCAmWgAwIBAgIQKpypOZKgm9xXGXh1OgGGizANBgkqhkiG9w0BAQsFADAaMRgwFgYDVQQDDA9FYXN5LVJTQSBTdWItQ0EwHhcNMjQxMTEzMTgxMDA3WhcNMjcwMjE2MTgxMDA3WjAbMRkwFwYDVQQDDBBzb21lX2NlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkMFYjl5Sodr6oxjL27joA7BSt56k7PYJW9lIQhj7mviio3yYdDF7UHPX+m4SJ/kvlhHpRhhVEHQ2xzfILLq9AijRXuytd8PuTKJXm1r8Ljlv68PZ3iBSYhK+pAvzuBPu9uHl7isnFmEtlcpSG/MFBlrnn6sCf3b+fF2/6F+ckWPr7DPTqz1RQ9HjV0PBnSf9U2jKcwa7Q4bODwBjvKgpqXN5KvF6rMio9Qr8aaNyv4DRd8j2MMo7SuUFEw5L1KC9nTnXq455nO8ZVvk/1q4R5NEJq+A/QVZbXHF1heFLeCDFkewYvMrJYkWO9G1YoRaUfc8VPwiTrOV5tRpax8eLlwIDAQABo4G9MIG6MDIGCCsGAQUFBwEBBCYwJDAiBggrBgEFBQcwAYYWaHR0cDovL2xvY2FsaG9zdDoyMDYxLzAJBgNVHRMEAjAAMB0GA1UdDgQWBBRSDYXLhVWPUOxZANDgghPTsqsfaDBNBgNVHSMERjBEgBQ0mVClsFXUeKKN2LmdcbgnCNw4naEapBgwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0GCEDpuK63HG/7CI8tqqS6dPm4wCwYDVR0PBAQDAgbAMA0GCSqGSIb3DQEBCwUAA4IBAQBvKs02M/Dg09Xfi3Th01H3wfH6jVV3yvqfYH5Eyc0rASNx33Gfu9qC+BASNkhTjvlvNyKKHpCRuZ0Mzf8Ynba395yXL5CPE/41wYlVwz30hpHCJ1gPRtZMZAiGp4eqBsFEOFpgkQSjZBLjcmAkOShj1EbUzM9SgXtVQKR5llwuCqAYQIuNK+xnzX01NUc9lhg8kojfdw6x92PUYDu87xRF6JRVhUGlM1M8Qg30zbkBirE4qiPECNbq1RMtOCYlVanKSyY1gZH+KbHGjLD3Vt8xEo7sA5hGjW4z2YIt9a4FNZ815+qtiPXwzFnnKLYS6KoRhms+MKrL1GlkGSFAjjDx"
                       "MIIDfzCCAmegAwIBAgIQOm4rrccb/sIjy2qpLp0+bjANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTAeFw0yNDExMTMxODEwMDFaFw0yNzAyMTYxODEwMDFaMBoxGDAWBgNVBAMMD0Vhc3ktUlNBIFN1Yi1DQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANCoczgc4Ksyt1O01TkcKIeA1NPK2UXQaaq/a6dsDJbqtQoFRBzcfGta1gUaTele3a/X9cxfCeiHWE7HymBYYgvlKmWqK4F5j5Yn5hkOcCAvdNEqmTGUKck4+4V86pXi7S8uQ352NzVqgTFVd9pnxtA53v9PZzC/d/6QkhDWpJCcZHFMHS2zZsdMLQ0tIo+oqhL3oIg5PtiPYhTu1pIFO8FhlfIEWnvXUNaMYwRqmqcyX1nwkBVbMfD21s8EFRaEqncDnVj4SpT07LWuNAlBKMQynT6xxXf1S1OW/QUv+FWS3VuBCFJOJNFVy/ln2X0OlTB3Q/rwUQixwIPS/ya3sjUCAwEAAaOBxDCBwTAyBggrBgEFBQcBAQQmMCQwIgYIKwYBBQUHMAGGFmh0dHA6Ly9sb2NhbGhvc3Q6MjA2MC8wDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUNJlQpbBV1Hiijdi5nXG4JwjcOJ0wUQYDVR0jBEowSIAU8CkC2Bdsv+xy+QAO03J0FS4Zni+hGqQYMBYxFDASBgNVBAMMC0Vhc3ktUlNBIENBghRNJ70P771kisZ6YcaopFEjOOoq/DALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQELBQADggEBADHoCi8Xa9cnALtcR2GQ/5msMdjxkGN24hvUbPbNz4sr+kJ5Xb3LBIUb4aPU9o+RO9H4/3+ZKhtFxLWisKa6Mf/kA/reOcxzpZIMDbv4nn1GjhE9xhHkmtQ+nbH2K0y71qmTsLa5fPYTeei2i+RKAx6L/6XGX6UtZ6XWUnPhU22epo+E1q6m3qw19wFf0koWcZu3vj1h2B+bp+3a4saF7+49pk2MSyItYH7HuESd+vqnnxZRY4QIPSifaq9cI0kQjnnrGipsHQxSyDBKfEivOMtx8i54U4oHOJotjfuBn6UmLODg7UpE3mCAff4FhTEBJEnD2Ibdw1Vx5u8OoG/iWhA="
                       "MIIDSzCCAjOgAwIBAgIUTSe9D++9ZIrGemHGqKRRIzjqKvwwDQYJKoZIhvcNAQELBQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjQxMTEzMTgwOTU5WhcNMzQxMTExMTgwOTU5WjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANCpdQ8BmI5u7uSIEy3nFfXwg9r3mVJQwID8KtePu/H+1NvCURfi12lrdis7/zfX7qGgl5H4apjuRSb5s44XkZ0qs9j4qOr3J+myaSQ0MjgQ5GBxqEDN6JxPm0l/Q0H5IjJ+KixkbY/+0UjQFWCRKkmFYCqf5yVwhXn/ITMcmwvGAdvkIsy/sfk0+LJ3hjoVnBd335YZE5I2gMs7auE3xEvfoZKUDvQ3ddQwG/Rg5KLwUsuGnPglPEWUPUgWlOK4GU7pCZUP9nLh/ZWA6DehPqpubLtTShkc4v8PG2FIPe0uSnm0SSRV1k2YisFU/6BiwQPnN+ogPX5ksT52YrDjpcMCAwEAAaOBkDCBjTAMBgNVHRMEBTADAQH/MB0GA1UdDgQWBBTwKQLYF2y/7HL5AA7TcnQVLhmeLzBRBgNVHSMESjBIgBTwKQLYF2y/7HL5AA7TcnQVLhmeL6EapBgwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0GCFE0nvQ/vvWSKxnphxqikUSM46ir8MAsGA1UdDwQEAwIBBjANBgkqhkiG9w0BAQsFAAOCAQEAdHZfTl9TcoYiV53l1JZZTLWAdfGulJ1H9uFudjVhWIEsKygtP+NINBwtzSMZB2Jr3Tla6PXinhre91rPP47tc5yNbWizmR7l7CqWPbXHqi7DzUIdRgGGDchb1KcmaEzh4rNupP3/mtmuZYTDkHqTNzoKdIXvBPyk9uweyrEhHCx/ManQ7rKZg+tP7YVF5ptvITXjMv0E/cGpCUyiMpJaL7qj2WtRDEwqdl//pbWHfiM0D4VoEZXhFqhYRGXUhmBEmB3paHuh9gKGWOX2xCBSBYsxFOgU6Gl21AwMBaaATkoEtxB1XWnj/4NMJg5MCZKuNOix6r7T436Ss4I9RSoW+Q=="]}
          :system)))))


(defn sign-at-time! [energiatodistus-id laatija-id skip-pdf? now]
  (let [db (ts/db-user laatija-id)
        whoami {:id laatija-id}]
    (energiatodistus-service/start-energiatodistus-signing! db
                                                            whoami
                                                            energiatodistus-id)
    (when-not skip-pdf?
      (sign-pdf-at-time! energiatodistus-id laatija-id now))
    (energiatodistus-service/end-energiatodistus-signing! db
                                                          ts/*aws-s3-client*
                                                          whoami
                                                          energiatodistus-id
                                                          {:skip-pdf-signed-assert? true
                                                           :allekirjoitusaika       now})))

(def time-when-test-cert-not-expired (Instant/parse "2022-05-20T12:00:00Z"))
(def time-when-test-cert-expired (Instant/parse "2022-05-25T12:00:00Z"))

(defn sign-pdf! [energiatodistus-id laatija-id]
  (sign-pdf-at-time! energiatodistus-id laatija-id time-when-test-cert-not-expired))

(defn sign! [energiatodistus-id laatija-id skip-pdf?]
  (sign-at-time! energiatodistus-id laatija-id skip-pdf? time-when-test-cert-not-expired))
