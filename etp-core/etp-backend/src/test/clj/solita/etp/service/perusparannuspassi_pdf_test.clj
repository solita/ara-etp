(ns solita.etp.service.perusparannuspassi-pdf-test
  (:require [clojure.test :as t]
            [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf-service]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
            [solita.etp.test-system :as ts])
  (:import (java.io InputStream)
           (java.time LocalDate)
           (org.apache.pdfbox Loader)))

(t/use-fixtures :each ts/fixture)

(defn- create-laatija-and-whoami!
  "Creates a laatija with patevyystaso 4 and returns [laatija-id whoami]."
  []
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1)
                         first
                         (merge {:patevyystaso 4})))
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}]
    [laatija-id whoami]))

(defn- create-et-and-ppp!
  "Creates a 2027 ET and a PPP with given phase A+ and pass-level A0 overrides.
   The PPP vaihe includes :vaiheen-alku-pvm so that PDF generation
   actually processes the vaihe (the PDF filters out vaiheet without dates).
   Returns [et-id ppp-id]."
  [laatija-id whoami aplus-tiedot]
  (let [[et-id _] (energiatodistus-test-data/generate-and-insert! 2027 true laatija-id)
        ppp-add (-> (ppp-test-data/generate-add et-id)
                    (update :passin-perustiedot merge
                            (select-keys aplus-tiedot [:a0-merkinta-id]))
                    (assoc-in [:vaiheet 0 :tayttaa-aplus-vaatimukset]
                              (boolean (:tayttaa-aplus-vaatimukset aplus-tiedot)))
                    (assoc-in [:vaiheet 0 :tulokset :vaiheen-alku-pvm] (LocalDate/of 2025 1 1)))
        ppp-id (:id (perusparannuspassi-service/insert-perusparannuspassi!
                      (ts/db-user laatija-id)
                      whoami
                      ppp-add))]
    [et-id ppp-id]))

(t/deftest generate-ppp-pdf-smoke-test
  ;; Given: a laatija, a 2027 ET, and a PPP with one valid vaihe
  (let [[laatija-id whoami] (create-laatija-and-whoami!)]

    ;; Scenario 1: Basic smoke test — PPP-PDF generates without exception
    (t/testing "PPP-PDF generation succeeds and returns a valid PDF InputStream"
      (let [[_ ppp-id] (create-et-and-ppp! laatija-id whoami
                                           {:tayttaa-aplus-vaatimukset false
                                            :a0-merkinta-id    0})
            ;; When: generate PPP-PDF via find-perusparannuspassi-pdf
            result (ppp-pdf-service/find-perusparannuspassi-pdf
                     ts/*db* whoami ppp-id "fi")]
        ;; Then: result is a non-nil InputStream
        (t/is (some? result)
              "PDF generation should return a non-nil result")
        (t/is (instance? InputStream result)
              "PDF generation should return an InputStream")
        ;; Then: InputStream contains valid PDF data that PDDocument can load
        (let [pdf-bytes (.readAllBytes result)
              document (Loader/loadPDF pdf-bytes)]
          (try
            (t/is (pos? (.getNumberOfPages document))
                  "Generated PDF should have at least one page")
            (finally
              (.close document))))))

    ;; Scenario 2: PPP with A+ flag and A0 marking, vaihe in A+ range
    (t/testing "PPP-PDF with A+ flag and A0 marking generates successfully"
      (let [[_ ppp-id] (create-et-and-ppp! laatija-id whoami
                                           {:tayttaa-aplus-vaatimukset true
                                            :a0-merkinta-id    1})
            result (ppp-pdf-service/find-perusparannuspassi-pdf
                     ts/*db* whoami ppp-id "fi")]
        (t/is (some? result)
              "PDF generation should succeed with both tayttaa-*-vaatimukset true")
        (let [pdf-bytes (.readAllBytes result)
              document (Loader/loadPDF pdf-bytes)]
          (try
            (t/is (pos? (.getNumberOfPages document))
                  "Generated PDF should have pages")
            (finally
              (.close document))))))

    ;; Scenario 3: PPP without A+ flag and A0 marking — downgrade scenario
    (t/testing "PPP-PDF without A+ flag and A0 marking generates successfully (downgrade)"
      (let [[_ ppp-id] (create-et-and-ppp! laatija-id whoami
                                           {:tayttaa-aplus-vaatimukset false
                                            :a0-merkinta-id    0})
            result (ppp-pdf-service/find-perusparannuspassi-pdf
                     ts/*db* whoami ppp-id "fi")]
        (t/is (some? result)
              "PDF generation should succeed even with downgrade")
        (let [pdf-bytes (.readAllBytes result)
              document (Loader/loadPDF pdf-bytes)]
          (try
            (t/is (pos? (.getNumberOfPages document))
                  "Downgraded PDF should still have pages")
            (finally
              (.close document))))))

    ;; Scenario 4: PPP with A0, A0_korjattu and A0_MPR values
    (doseq [a0-merkinta-id [1 2 3]]
      (t/testing (str "PPP-PDF with A0 marking " a0-merkinta-id " generates successfully")
        (let [[_ ppp-id] (create-et-and-ppp! laatija-id whoami
                                             {:tayttaa-aplus-vaatimukset false
                                              :a0-merkinta-id a0-merkinta-id})
              result (ppp-pdf-service/find-perusparannuspassi-pdf
                       ts/*db* whoami ppp-id "fi")
              document (Loader/loadPDF (.readAllBytes result))]
          (try
            (t/is (pos? (.getNumberOfPages document))
                  "Generated PDF should have at least one page")
            (finally
              (.close document))))))

    ;; Scenario 5: PPP with multiple vaihe (3 valid phases)
    (t/testing "PPP-PDF with multiple vaiheet generates successfully"
      (let [[et-id _] (energiatodistus-test-data/generate-and-insert! 2027 true laatija-id)
            base-ppp (ppp-test-data/generate-add et-id)
            base-vaihe (first (:vaiheet base-ppp))
            ;; Each vaihe needs :vaiheen-alku-pvm so PDF actually processes them
            make-vaihe (fn [nro date tayttaa-aplus-vaatimukset]
                         (-> base-vaihe
                             (assoc :vaihe-nro nro)
                             (assoc :tayttaa-aplus-vaatimukset tayttaa-aplus-vaatimukset)
                             (assoc-in [:tulokset :vaiheen-alku-pvm] date)))
            multi-vaihe-ppp (-> base-ppp
                                (update :passin-perustiedot merge
                                        {:a0-merkinta-id 1})
                                (assoc :vaiheet [(make-vaihe 1 (LocalDate/of 2025 1 1) true)
                                                 (make-vaihe 2 (LocalDate/of 2025 7 1) true)
                                                 (make-vaihe 3 (LocalDate/of 2026 1 1) false)]))
            ppp-id (:id (perusparannuspassi-service/insert-perusparannuspassi!
                          (ts/db-user laatija-id)
                          whoami
                          multi-vaihe-ppp))
            result (ppp-pdf-service/find-perusparannuspassi-pdf
                     ts/*db* whoami ppp-id "fi")
            html (ppp-pdf-service/find-perusparannuspassi-html
                   ts/*db* whoami ppp-id "fi")]
        (t/is (some? result)
              "Multi-vaihe PPP-PDF generation should succeed")
        (t/is (re-find #"(?s)Täyttää A\+ -luokan vaatimukset</dt>\s*<dd>Ei</dd>" html)
              "The final A+ answer should come from the last active vaihe")
        (let [pdf-bytes (.readAllBytes result)
              document (Loader/loadPDF pdf-bytes)]
          (try
            (t/is (>= (.getNumberOfPages document) 4)
                  "Multi-vaihe PDF should have at least etusivu + 3 vaihesivut")
            (finally
              (.close document))))))))
