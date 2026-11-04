(ns solita.etp.test-data.energiatodistus
  (:require [clojure.java.io :as io]
            [flathead.deep :as deep]
            [schema-generators.generators :as g]
            [schema.core :as schema]
            [solita.common.logic :as logic]
            [solita.common.schema :as xschema]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.service.signing.pdf-sign]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf]
            [solita.etp.service.energiatodistus-signing :as energiatodistus-signing-service]
            [solita.etp.service.laatija :as laatija-service]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]
            [solita.etp.test-timeserver :as test-timeserver])
  (:import (eu.europa.esig.dss.pades PAdESSignatureParameters)
           (java.io FileInputStream ObjectInputStream)
           (java.time Instant)))

(def generators {schema/Str                   generators/postgresql-safe-string-generator
                 schema/Num                   (g/always 1.0M)
                 common-schema/Num1           (g/always 1.0M)
                 common-schema/NonNegative    (g/always 1.0M)
                 common-schema/IntNonNegative (g/always 1)})

(defn schema-by-version-and-ready-for-signing [versio ready-for-signing?]
  (cond->> (cond
             (= versio 2013) energiatodistus-schema/EnergiatodistusSave2013
             (= versio 2018) energiatodistus-schema/EnergiatodistusSave2018
             (= versio 2026) energiatodistus-schema/EnergiatodistusSave2026)
           ready-for-signing? (deep/map-values record?
                                               (logic/when* xschema/maybe? :schema))))

(defn sisainen-kuorma [versio id]
  (-> (energiatodistus-service/find-sisaiset-kuormat ts/*db* versio)
      (->> (filter (comp (partial = id) :kayttotarkoitusluokka-id)))
      first
      (dissoc :kayttotarkoitusluokka-id)))

(defn generate-add [versio ready-for-signing?]
  (generators/complete
    (merge
      {:perustiedot                     (merge
                                          {:kieli           (rand-int 2)
                                           :kayttotarkoitus "YAT"}
                                          (when (contains? #{2018 2026} versio)
                                            {:laatimisvaihe (rand-int 2)})
                                          (when (contains? #{2026} versio)
                                            {:havainnointikayntityyppi-id (rand-int 2)}))
       :lahtotiedot                     {:ilmanvaihto {:tyyppi-id (rand-int 7)}
                                         :lammitys    {:lammitysmuoto-1 {:id (rand-int 10)}
                                                       :lammitysmuoto-2 {:id (rand-int 10)}
                                                       :lammonjako      {:id (rand-int 13)}}
                                         :sis-kuorma  (sisainen-kuorma versio 1)}
       :laskutettava-yritys-id          nil
       :laskutusosoite-id               -1
       :korvattu-energiatodistus-id              nil
       :yksinkertaistettu-paivitysmenettely     false
       :draft-visible-to-paakayttaja            false
       :bypass-validation-limits        false
       :bypass-validation-limits-reason nil}
      (when (contains? #{2026} versio)
        {:ilmastoselvitys {:laadintaperuste (rand-int 2)}}))
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

(defn- add->versio
  "Here we don't know what version the add is, but we deduce it from its structure."
  [add]
  (cond (-> add :perustiedot (contains? :uudisrakennus)) 2013
        (-> add :perustiedot (contains? :havainnointikayntityyppi-id)) 2026
        :else 2018))

(defn insert!
  [energiatodistus-adds laatija-id]
  (mapv #(:id (energiatodistus-service/add-energiatodistus!
                (ts/db-user laatija-id)
                {:id laatija-id}
                (add->versio %)
                %)) energiatodistus-adds))

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

(defn generate-2026-pdf-as-file-mock [_ _ _ _ _ _]
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

(defn sign-pdf-at-time! [energiatodistus-id laatija-id now whoami]
  (with-bindings {#'solita.etp.service.signing.pdf-sign/get-signature-parameters             get-parameters-in-test
                  #'solita.etp.service.energiatodistus-pdf/generate-et-2013-2018-pdf-as-file generate-pdf-as-file-mock
                  #'solita.etp.service.energiatodistus-pdf/generate-et-2026-pdf-as-file      generate-2026-pdf-as-file-mock
                  #'solita.etp.service.signing.pdf-sign/get-tsp-source                       test-timeserver/get-tsp-source-in-test}
    (let [language (-> (energiatodistus-service/find-energiatodistus
                         ts/*db*
                         energiatodistus-id)
                       :perustiedot
                       :kieli)
          laatija-allekirjoitus-id (-> (laatija-service/find-laatija-by-id ts/*db* laatija-id) :allekirjoitus-id)]
      (doseq [language-code (energiatodistus-service/language-id->codes language)]
        (energiatodistus-signing-service/find-energiatodistus-digest
          ts/*db*
          whoami
          ts/*aws-s3-client*
          energiatodistus-id
          language-code
          laatija-allekirjoitus-id
          now)
        (energiatodistus-signing-service/sign-energiatodistus-pdf
          ts/*db*
          ts/*aws-s3-client*
          now
          energiatodistus-id
          language-code
          {:signature "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",
           :chain     ["MIIDfTCCAmWgAwIBAgIQKpypOZKgm9xXGXh1OgGGizANBgkqhkiG9w0BAQsFADAaMRgwFgYDVQQDDA9FYXN5LVJTQSBTdWItQ0EwHhcNMjQxMTEzMTgxMDA3WhcNMjcwMjE2MTgxMDA3WjAbMRkwFwYDVQQDDBBzb21lX2NlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkMFYjl5Sodr6oxjL27joA7BSt56k7PYJW9lIQhj7mviio3yYdDF7UHPX+m4SJ/kvlhHpRhhVEHQ2xzfILLq9AijRXuytd8PuTKJXm1r8Ljlv68PZ3iBSYhK+pAvzuBPu9uHl7isnFmEtlcpSG/MFBlrnn6sCf3b+fF2/6F+ckWPr7DPTqz1RQ9HjV0PBnSf9U2jKcwa7Q4bODwBjvKgpqXN5KvF6rMio9Qr8aaNyv4DRd8j2MMo7SuUFEw5L1KC9nTnXq455nO8ZVvk/1q4R5NEJq+A/QVZbXHF1heFLeCDFkewYvMrJYkWO9G1YoRaUfc8VPwiTrOV5tRpax8eLlwIDAQABo4G9MIG6MDIGCCsGAQUFBwEBBCYwJDAiBggrBgEFBQcwAYYWaHR0cDovL2xvY2FsaG9zdDoyMDYxLzAJBgNVHRMEAjAAMB0GA1UdDgQWBBRSDYXLhVWPUOxZANDgghPTsqsfaDBNBgNVHSMERjBEgBQ0mVClsFXUeKKN2LmdcbgnCNw4naEapBgwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0GCEDpuK63HG/7CI8tqqS6dPm4wCwYDVR0PBAQDAgbAMA0GCSqGSIb3DQEBCwUAA4IBAQBvKs02M/Dg09Xfi3Th01H3wfH6jVV3yvqfYH5Eyc0rASNx33Gfu9qC+BASNkhTjvlvNyKKHpCRuZ0Mzf8Ynba395yXL5CPE/41wYlVwz30hpHCJ1gPRtZMZAiGp4eqBsFEOFpgkQSjZBLjcmAkOShj1EbUzM9SgXtVQKR5llwuCqAYQIuNK+xnzX01NUc9lhg8kojfdw6x92PUYDu87xRF6JRVhUGlM1M8Qg30zbkBirE4qiPECNbq1RMtOCYlVanKSyY1gZH+KbHGjLD3Vt8xEo7sA5hGjW4z2YIt9a4FNZ815+qtiPXwzFnnKLYS6KoRhms+MKrL1GlkGSFAjjDx"
                       "MIIDfzCCAmegAwIBAgIQOm4rrccb/sIjy2qpLp0+bjANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTAeFw0yNDExMTMxODEwMDFaFw0yNzAyMTYxODEwMDFaMBoxGDAWBgNVBAMMD0Vhc3ktUlNBIFN1Yi1DQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANCoczgc4Ksyt1O01TkcKIeA1NPK2UXQaaq/a6dsDJbqtQoFRBzcfGta1gUaTele3a/X9cxfCeiHWE7HymBYYgvlKmWqK4F5j5Yn5hkOcCAvdNEqmTGUKck4+4V86pXi7S8uQ352NzVqgTFVd9pnxtA53v9PZzC/d/6QkhDWpJCcZHFMHS2zZsdMLQ0tIo+oqhL3oIg5PtiPYhTu1pIFO8FhlfIEWnvXUNaMYwRqmqcyX1nwkBVbMfD21s8EFRaEqncDnVj4SpT07LWuNAlBKMQynT6xxXf1S1OW/QUv+FWS3VuBCFJOJNFVy/ln2X0OlTB3Q/rwUQixwIPS/ya3sjUCAwEAAaOBxDCBwTAyBggrBgEFBQcBAQQmMCQwIgYIKwYBBQUHMAGGFmh0dHA6Ly9sb2NhbGhvc3Q6MjA2MC8wDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUNJlQpbBV1Hiijdi5nXG4JwjcOJ0wUQYDVR0jBEowSIAU8CkC2Bdsv+xy+QAO03J0FS4Zni+hGqQYMBYxFDASBgNVBAMMC0Vhc3ktUlNBIENBghRNJ70P771kisZ6YcaopFEjOOoq/DALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQELBQADggEBADHoCi8Xa9cnALtcR2GQ/5msMdjxkGN24hvUbPbNz4sr+kJ5Xb3LBIUb4aPU9o+RO9H4/3+ZKhtFxLWisKa6Mf/kA/reOcxzpZIMDbv4nn1GjhE9xhHkmtQ+nbH2K0y71qmTsLa5fPYTeei2i+RKAx6L/6XGX6UtZ6XWUnPhU22epo+E1q6m3qw19wFf0koWcZu3vj1h2B+bp+3a4saF7+49pk2MSyItYH7HuESd+vqnnxZRY4QIPSifaq9cI0kQjnnrGipsHQxSyDBKfEivOMtx8i54U4oHOJotjfuBn6UmLODg7UpE3mCAff4FhTEBJEnD2Ibdw1Vx5u8OoG/iWhA="
                       "MIIDSzCCAjOgAwIBAgIUTSe9D++9ZIrGemHGqKRRIzjqKvwwDQYJKoZIhvcNAQELBQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjQxMTEzMTgwOTU5WhcNMzQxMTExMTgwOTU5WjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANCpdQ8BmI5u7uSIEy3nFfXwg9r3mVJQwID8KtePu/H+1NvCURfi12lrdis7/zfX7qGgl5H4apjuRSb5s44XkZ0qs9j4qOr3J+myaSQ0MjgQ5GBxqEDN6JxPm0l/Q0H5IjJ+KixkbY/+0UjQFWCRKkmFYCqf5yVwhXn/ITMcmwvGAdvkIsy/sfk0+LJ3hjoVnBd335YZE5I2gMs7auE3xEvfoZKUDvQ3ddQwG/Rg5KLwUsuGnPglPEWUPUgWlOK4GU7pCZUP9nLh/ZWA6DehPqpubLtTShkc4v8PG2FIPe0uSnm0SSRV1k2YisFU/6BiwQPnN+ogPX5ksT52YrDjpcMCAwEAAaOBkDCBjTAMBgNVHRMEBTADAQH/MB0GA1UdDgQWBBTwKQLYF2y/7HL5AA7TcnQVLhmeLzBRBgNVHSMESjBIgBTwKQLYF2y/7HL5AA7TcnQVLhmeL6EapBgwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0GCFE0nvQ/vvWSKxnphxqikUSM46ir8MAsGA1UdDwQEAwIBBjANBgkqhkiG9w0BAQsFAAOCAQEAdHZfTl9TcoYiV53l1JZZTLWAdfGulJ1H9uFudjVhWIEsKygtP+NINBwtzSMZB2Jr3Tla6PXinhre91rPP47tc5yNbWizmR7l7CqWPbXHqi7DzUIdRgGGDchb1KcmaEzh4rNupP3/mtmuZYTDkHqTNzoKdIXvBPyk9uweyrEhHCx/ManQ7rKZg+tP7YVF5ptvITXjMv0E/cGpCUyiMpJaL7qj2WtRDEwqdl//pbWHfiM0D4VoEZXhFqhYRGXUhmBEmB3paHuh9gKGWOX2xCBSBYsxFOgU6Gl21AwMBaaATkoEtxB1XWnj/4NMJg5MCZKuNOix6r7T436Ss4I9RSoW+Q=="]})))))


(defn sign-at-time! [energiatodistus-id laatija-id skip-pdf? now]
  (let [db (ts/db-user laatija-id)
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}]
    (energiatodistus-service/start-energiatodistus-signing! db
                                                            whoami
                                                            energiatodistus-id)
    (when-not skip-pdf?
      (sign-pdf-at-time! energiatodistus-id laatija-id now whoami))
    (energiatodistus-service/end-energiatodistus-signing! db
                                                          ts/*aws-s3-client*
                                                          whoami
                                                          energiatodistus-id
                                                          {:skip-pdf-signed-assert? true
                                                           :allekirjoitusaika       now})))

(def time-when-test-cert-not-expired (Instant/parse "2022-05-20T12:00:00Z"))
(def time-when-test-cert-expired (Instant/parse "2022-05-25T12:00:00Z"))

(defn sign-pdf! [energiatodistus-id laatija-id whoami]
  (sign-pdf-at-time! energiatodistus-id laatija-id time-when-test-cert-not-expired whoami))

(defn sign! [energiatodistus-id laatija-id skip-pdf?]
  (sign-at-time! energiatodistus-id laatija-id skip-pdf? time-when-test-cert-not-expired))
