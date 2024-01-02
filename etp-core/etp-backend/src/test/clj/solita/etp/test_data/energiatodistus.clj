(ns solita.etp.test-data.energiatodistus
  (:require [clojure.test.check.generators :as test-generators]
            [clojure.string :as str]
            [schema.core :as schema]
            [schema-generators.generators :as g]
            [flathead.deep :as deep]
            [solita.common.logic :as logic]
            [solita.common.schema :as xschema]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.schema.common :as common-schema]
            [solita.etp.schema.energiatodistus :as energiatodistus-schema]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service])
  (:import (java.time Instant)))

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
   {:perustiedot (merge
                  {:kieli (rand-int 2)
                   :kayttotarkoitus "YAT"}
                  (when (= versio 2018)
                    {:laatimisvaihe (rand-int 2)}))
    :lahtotiedot {:ilmanvaihto {:tyyppi-id (rand-int 7)}
                  :lammitys {:lammitysmuoto-1 {:id (rand-int 10)}
                             :lammitysmuoto-2 {:id (rand-int 10)}
                             :lammonjako {:id (rand-int 13)}}
                  :sis-kuorma (sisainen-kuorma versio 1)}
    :laskutettava-yritys-id nil
    :laskutusosoite-id -1
    :korvattu-energiatodistus-id nil
    :draft-visible-to-paakayttaja false
    :bypass-validation-limits false
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
                  {:alapohja {:ala 0 :U 0.03}
                   :ikkunat {:ala 0 :U 0.4}
                   :ylapohja {:ala 0 :U 0.03}
                   :ilmatilavuus 1M
                   :lampokapasiteetti 1M
                   :ilmanvuotoluku 1M
                   :ulkoseinat {:ala 1M :U 0.05}
                   :kylmasillat-UA 0.1
                   :ulkoovet {:ala 0 :U 0.2}})
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

(defn sign-pdf-at-time! [energiatodistus-id laatija-id now]
  (let [language (-> (energiatodistus-service/find-energiatodistus
                      ts/*db*
                      energiatodistus-id)
                     :perustiedot
                     :kieli)]
    (doseq [language-code (energiatodistus-service/language-id->codes language)]
      (energiatodistus-pdf-service/find-energiatodistus-digest
       ts/*db*
       ts/*aws-s3-client*
       energiatodistus-id
       language-code)
      (energiatodistus-pdf-service/sign-energiatodistus-pdf
       ts/*db*
       ts/*aws-s3-client*
       {:id laatija-id :sukunimi "Specimen-Potex"}
       now
       energiatodistus-id
       language-code
       {:signature "IAMcrYCDqC0nprcI2aKTZGAqHurktQYjw6IBh4gDrvl5FKrKczRlE07x8iwWd66O11J/LXuWj3xdNz3UTcPzvUBurT0VH4KDy9oGxeMbMLrJoWmD3gvzUrrRox/oA8/wKuTnqo/PIkJzkZFxty3zeh5ahNQAZEqXnUP+oBi524WlPNcSXA4EnTNlTm7FfJlWIUw8Ljo1ZqaFgOw7omTEeYJgBLiYAZgTSxeNkDMTogAqQA9jXnukUMDu7s/0APsZpFEU/kbYZM3Sz5XfgGHbq4p/zUzskTKqeMDfiJg8hGkMXfViLwS4cyriW/VyCm87WCBqkOeHD7whOv4KyVA/cw==",
        :chain     ["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"
                    "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"
                    "MIIEHjCCAwagAwIBAgIDAdTAMA0GCSqGSIb3DQEBBQUAMIGlMQswCQYDVQQGEwJGSTEQMA4GA1UECBMHRmlubGFuZDEjMCEGA1UEChMaVmFlc3RvcmVraXN0ZXJpa2Vza3VzIFRFU1QxKTAnBgNVBAsTIENlcnRpZmljYXRpb24gQXV0aG9yaXR5IFNlcnZpY2VzMRkwFwYDVQQLExBWYXJtZW5uZXBhbHZlbHV0MRkwFwYDVQQDExBWUksgVEVTVCBSb290IENBMB4XDTAyMTIxNzE5MDA0NFoXDTIzMTIxNzE4NTg1MFowgaUxCzAJBgNVBAYTAkZJMRAwDgYDVQQIEwdGaW5sYW5kMSMwIQYDVQQKExpWYWVzdG9yZWtpc3RlcmlrZXNrdXMgVEVTVDEpMCcGA1UECxMgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkgU2VydmljZXMxGTAXBgNVBAsTEFZhcm1lbm5lcGFsdmVsdXQxGTAXBgNVBAMTEFZSSyBURVNUIFJvb3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDUxaN5pNBQqhtAm/XeVX+qbF2ILwiAumkt68A8JcrKOxOuUeXc3f6SWK/n+y0wZO/xr/c4ibElJTfvr+2Q33C5Z4gHS2NwJtK+/fbiwrZ667HUkCK11qehYC4dFeR7K01xKMGosDujp/ns6esldgBi0/30GNiFVKs7Kf19QjA+vGwMVtDdNVBdiJLtYxxV60gymHvD82Jwg1UFdvl1B2EkR5yrNdzBNbeeRGF6cBEbLJUvrHkNB5z8rng2qqz6rmuSBWl7SqChQRv/qP88Uz+nZaal3TCjg+IaGboURh1odW+u/JwQGVLzCutjVqyiqMMPwu65NUCDvoEnxeIVqPJLAgMBAAGjVTBTMA8GA1UdEwEB/wQFMAMBAf8wEQYJYIZIAYb4QgEBBAQDAgAHMA4GA1UdDwEB/wQEAwIBxjAdBgNVHQ4EFgQU3t5QCxdnwzepyZiINEjJMXnrbaEwDQYJKoZIhvcNAQEFBQADggEBAFSoft0eoN/19XnLGhWKLgveR/eTkaJX7ap63ffftnleky9exYSAFun5s8Rw7/Bf8WLftMa/YvGpfV64azcqzas2yo3HKKvTPHOegJbm5tMS3qVi8PGf6jxYcPeAFXMDg9SAex6GLxI5uoXflZ3TiDj64CvCSxHPCTwe2ybprVrRti6LwCO1iEOTGjvxuUSxVhRKcywZUgn1oVmcim63AvvfDcD8Ytx8xTlPPnibTkQzlwaWMCF+kDitksFbkOUdWYVoWFkb4vis/BYc6ZyjF1Wb1yEYNvVfL4/17kiZnZGxLNbM4Ygm2vUEZ8ualEtXfFCfv9DA8MeVIUfa2pTH6Tk="]}))))

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
                                                          {:skip-pdf-signed-assert? true})))

(def time-when-test-cert-not-expired (Instant/parse "2022-05-20T12:00:00Z"))
(def time-when-test-cert-expired (Instant/parse "2022-05-25T12:00:00Z"))

(defn sign-pdf! [energiatodistus-id laatija-id]
  (sign-pdf-at-time! energiatodistus-id laatija-id time-when-test-cert-not-expired))

(defn sign! [energiatodistus-id laatija-id skip-pdf?]
  (sign-at-time! energiatodistus-id laatija-id skip-pdf? time-when-test-cert-not-expired))
