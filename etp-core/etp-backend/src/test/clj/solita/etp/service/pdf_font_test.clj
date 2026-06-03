(ns solita.etp.service.pdf-font-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.pdf :as pdf-service]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
            [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
            [solita.etp.test-system :as ts])
  (:import (java.io ByteArrayInputStream InputStream)
           (java.time LocalDate)
           (org.apache.pdfbox.pdmodel PDDocument PDResources)
           (org.apache.pdfbox.pdmodel.font PDFont)
           (org.apache.pdfbox.pdmodel.graphics.form PDFormXObject)))

(t/use-fixtures :each ts/fixture)

(defn- create-laatija-and-whoami!
  []
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1)
                         first
                         (merge {:patevyystaso 4})))
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}]
    [laatija-id whoami]))

(defn- create-et-and-ppp!
  [laatija-id whoami]
  (let [[et-id _] (energiatodistus-test-data/generate-and-insert! 2026 true laatija-id)
        ppp-add (-> (ppp-test-data/generate-add et-id)
                    (assoc-in [:vaiheet 0 :tulokset :vaiheen-alku-pvm] (LocalDate/of 2025 1 1)))
        ppp-id (:id (perusparannuspassi-service/insert-perusparannuspassi!
                      (ts/db-user laatija-id)
                      whoami
                      ppp-add))]
    [et-id ppp-id]))

(defn- create-energiatodistus-2026!
  []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        db (ts/db-user laatija-id)
        whoami {:id laatija-id :rooli 0}
        [energiatodistus-id _] (energiatodistus-test-data/generate-and-insert! 2026 true laatija-id)]
    {:db db
     :whoami whoami
     :energiatodistus-id energiatodistus-id}))

(defn- normalize-font-name [font-name]
  (some-> font-name
          (str/replace #"^[A-Z]{6}\+" "")
          (str/replace #",.*$" "")
          (str/lower-case)))

(defn- font-names-in-font [^PDFont font]
  (->> [(some-> font .getName)
        (some-> font .getFontDescriptor .getFontName)]
       (keep normalize-font-name)
       set))

(defn- font-names-in-resources [^PDResources resources]
  (if (nil? resources)
    #{}
    (let [direct-fonts (->> (iterator-seq (.iterator (.getFontNames resources)))
                            (mapcat (fn [font-name]
                                      (when-let [font (.getFont resources font-name)]
                                        (font-names-in-font font))))
                            set)
          nested-fonts (->> (iterator-seq (.iterator (.getXObjectNames resources)))
                            (mapcat (fn [xobject-name]
                                      (let [xobject (.getXObject resources xobject-name)]
                                        (if (instance? PDFormXObject xobject)
                                          (font-names-in-resources (.getResources ^PDFormXObject xobject))
                                          #{}))))
                            set)]
      (into direct-fonts nested-fonts))))

(defn- pdf-font-names [pdf-bytes]
  (with-open [pdf-stream (ByteArrayInputStream. pdf-bytes)
              document (PDDocument/load pdf-stream)]
    (->> (iterator-seq (.iterator (.getPages document)))
         (mapcat (fn [page]
                   (font-names-in-resources (.getResources page))))
         set)))

(defn- uses-font-family? [font-names family]
  (let [family (str/lower-case family)]
    (boolean (some #(str/includes? % family) font-names))))

(defn- assert-font-family-present! [document-label font-names family]
  (t/is (uses-font-family? font-names family)
        (str document-label " should include font family '" family
             "'. Fonts: " (pr-str (sort font-names)))))

(defn- assert-font-family-absent! [document-label font-names family]
  (t/is (not (uses-font-family? font-names family))
        (str document-label " should not include font family '" family
             "'. Fonts: " (pr-str (sort font-names)))))

(defn- generate-current-ppp-pdf-bytes [whoami ppp-id]
  (with-open [^InputStream pdf-stream (or (ppp-pdf-service/find-perusparannuspassi-pdf ts/*db* whoami ppp-id "fi")
                                          (throw (ex-info "Perusparannuspassi PDF was not found"
                                                          {:ppp-id ppp-id})))]
    (.readAllBytes pdf-stream)))

(defn- generate-energiatodistus-2026-pdf-bytes [db whoami energiatodistus-id]
  (let [complete-energiatodistus (or (complete-energiatodistus-service/find-complete-energiatodistus db whoami energiatodistus-id)
                                     (throw (ex-info "Energiatodistus was not found"
                                                     {:energiatodistus-id energiatodistus-id})))
        pdf-path (energiatodistus-pdf-service/generate-et-pdf-as-file db
                                                                      whoami
                                                                      complete-energiatodistus
                                                                      "fi"
                                                                      true
                                                                      "font-test-signature-id")]
    (try
      (with-open [pdf-stream (io/input-stream pdf-path)]
        (.readAllBytes pdf-stream))
      (finally
        (io/delete-file pdf-path true)))))

(defn- generate-template-letter-pdf-bytes []
  (pdf-service/generate-pdf->bytes {:template "<h1>Font regression letter</h1><p>Tämä on testidokumentti.</p>"}))

(defn- generate-ipost-address-page-pdf-bytes []
  (pdf-service/generate-pdf->bytes
    {:layout "pdf/ipost-address-page.html"
     :data   {:lahettaja    {:nimi "Lähettäjä Oy"
                             :jakeluosoite "Katu 1"
                             :postinumero "00100"
                             :postitoimipaikka "Helsinki"}
              :vastaanottaja {:nimi "Vastaanottaja Oy"
                              :jakeluosoite "Testikatu 2"
                              :postinumero "00200"
                              :postitoimipaikka "Espoo"}}}))

(t/deftest pdf-font-family-regression-test
  (let [[laatija-id ppp-whoami] (create-laatija-and-whoami!)
        [et-with-ppp-id ppp-id] (create-et-and-ppp! laatija-id ppp-whoami)
        {:keys [db whoami energiatodistus-id]} (create-energiatodistus-2026!)
        document-fonts {"Perusparannuspassi 2026" (-> (generate-current-ppp-pdf-bytes ppp-whoami ppp-id) pdf-font-names)
                        ;; Only ET 2026-based PDFs are covered here because older energiatodistus PDFs are generated via a different path.
                        "Energiatodistus 2026"                    (-> (generate-energiatodistus-2026-pdf-bytes db whoami energiatodistus-id) pdf-font-names)
                        "Energiatodistus 2026 + Perusparannuspassi" (-> (generate-energiatodistus-2026-pdf-bytes ts/*db* ppp-whoami et-with-ppp-id) pdf-font-names)
                        "template-letter"                        (-> (generate-template-letter-pdf-bytes) pdf-font-names)
                        "iPost"                                  (-> (generate-ipost-address-page-pdf-bytes) pdf-font-names)}]
    (t/testing "Perusparannuspassi 2026 uses Roboto and does not use Carlito"
      (assert-font-family-present! "Perusparannuspassi 2026" (get document-fonts "Perusparannuspassi 2026") "roboto")
      (assert-font-family-absent! "Perusparannuspassi 2026" (get document-fonts "Perusparannuspassi 2026") "carlito"))

    (t/testing "Energiatodistus 2026 uses Carlito and does not use Roboto"
      (assert-font-family-present! "Energiatodistus 2026" (get document-fonts "Energiatodistus 2026") "carlito")
      (assert-font-family-absent! "Energiatodistus 2026" (get document-fonts "Energiatodistus 2026") "roboto"))

    (t/testing "Energiatodistus 2026 + Perusparannuspassi uses both Roboto and Carlito"
      (assert-font-family-present! "Energiatodistus 2026 + Perusparannuspassi"
                                   (get document-fonts "Energiatodistus 2026 + Perusparannuspassi")
                                   "roboto")
      (assert-font-family-present! "Energiatodistus 2026 + Perusparannuspassi"
                                  (get document-fonts "Energiatodistus 2026 + Perusparannuspassi")
                                  "carlito"))

    (t/testing "iPost uses Roboto and does not use Carlito"
      (assert-font-family-present! "iPost" (get document-fonts "iPost") "roboto")
      (assert-font-family-absent! "iPost" (get document-fonts "iPost") "carlito"))

    (t/testing "template-letter uses Roboto and does not use Carlito"
      (assert-font-family-present! "template-letter" (get document-fonts "template-letter") "roboto")
      (assert-font-family-absent! "template-letter" (get document-fonts "template-letter") "carlito"))))



