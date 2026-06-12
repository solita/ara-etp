(ns solita.etp.service.openhtmltopdf-warning-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf-service]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]
            [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf-service]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.perusparannuspassi :as ppp-test-data]
            [solita.etp.test-system :as ts])
  (:import (ch.qos.logback.classic Logger)
           (ch.qos.logback.classic.spi ILoggingEvent)
           (ch.qos.logback.core.read ListAppender)
           (com.openhtmltopdf.slf4j Slf4jLogger)
           (com.openhtmltopdf.util XRLog)
           (java.io InputStream)
           (java.time LocalDate)
           (java.util.regex Pattern)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.slf4j LoggerFactory)))

(t/use-fixtures :each ts/fixture)

(defn- capture-openhtmltopdf-warnings [f]
  (XRLog/setLoggerImpl (Slf4jLogger.))
  (let [^Logger logger (LoggerFactory/getLogger "com.openhtmltopdf")
        original-additive? (.isAdditive logger)
        appender (ListAppender.)]
    (.start appender)
    (.setAdditive logger false)
    (.addAppender logger appender)
    (try
      {:result   (f)
       :warnings (->> (seq (.-list appender))
                      (map #(.getFormattedMessage ^ILoggingEvent %))
                      vec)}
      (finally
        (.detachAppender logger appender)
        (.setAdditive logger original-additive?)
        (.stop appender)))))

(defn- warning-matches? [warning matcher]
  (cond
    (string? matcher) (= warning matcher)
    (instance? Pattern matcher) (boolean (re-find matcher warning))
    (ifn? matcher) (boolean (matcher warning))
    :else (throw (ex-info "Unsupported OpenHTMLToPDF warning matcher"
                          {:matcher matcher
                           :type    (type matcher)}))))

(defn- analyze-openhtmltopdf-warnings
  [warnings expected-warnings]
  {:unexpected (->> warnings
                    (remove (fn [warning]
                              (some #(warning-matches? warning %) expected-warnings)))
                    vec)
   :missing-expected (->> expected-warnings
                          (remove (fn [expected-warning]
                                    (some #(warning-matches? % expected-warning) warnings)))
                          vec)})

(defn- assert-only-expected-openhtmltopdf-warnings!
  [warnings expected-warnings]
  (let [{:keys [unexpected missing-expected]}
        (analyze-openhtmltopdf-warnings warnings expected-warnings)]
    (t/is (empty? unexpected)
          (str "Unexpected OpenHTMLToPDF warnings: " (pr-str unexpected)
               "\nAll warnings: " (pr-str warnings)))
    (t/is (empty? missing-expected)
          (str "Expected OpenHTMLToPDF warnings were not emitted: "
               (pr-str missing-expected)
               "\nAll warnings: " (pr-str warnings)))))

(t/deftest openhtmltopdf-warning-analysis-test
  (t/testing "fails when an expected warning is no longer emitted"
    (let [{:keys [unexpected missing-expected]}
          (analyze-openhtmltopdf-warnings
            ["No document description provided. Document will not be PDF/UA compliant."]
            [#"No document description provided\. Document will not be PDF/UA compliant\."
             #"No alt attribute provided for image/replaced in PDF/UA document\."])]
      (t/is (empty? unexpected))
      (t/is (= 1 (count missing-expected)))
      (t/is (warning-matches? "No alt attribute provided for image/replaced in PDF/UA document."
                              (first missing-expected))))))

(defn- create-laatija-and-whoami!
  []
  (let [laatija-id (laatija-test-data/insert-suomifi-laatija!
                     (-> (laatija-test-data/generate-adds 1)
                         first
                         (merge {:patevyystaso 4
                                 ;; The name might cause problems and testing that is not in the scope of this test.
                                 :etunimi "Goodfirstname"
                                 :sukunim "Goodlastname"
                                 })))
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}]
    [laatija-id whoami]))

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

(defn- create-perusparannuspassi!
  [laatija-id whoami]
  (let [[et-id _] (energiatodistus-test-data/generate-and-insert! 2026 true laatija-id)
        ppp-add (-> (ppp-test-data/generate-add et-id)
                    (assoc-in [:vaiheet 0 :tulokset :vaiheen-alku-pvm] (LocalDate/of 2025 1 1)))
        ppp-id (:id (perusparannuspassi-service/insert-perusparannuspassi!
                      (ts/db-user laatija-id)
                      whoami
                      ppp-add))]
    ppp-id))

(t/deftest ^{:broken-test "inexclicably fails in GHA"} openhtmltopdf-warnings-test
  (t/testing "energiatodistus 2026 emits all and only the expected OpenHTMLToPDF warnings"
    (let [{:keys [db whoami energiatodistus-id]} (create-energiatodistus-2026!)
          complete-energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus db whoami energiatodistus-id)
          language-code (or (some-> complete-energiatodistus
                                    :perustiedot
                                    :kieli
                                    energiatodistus-service/language-id->codes
                                    first)
                            "fi")
          expected-warnings []
          {:keys [result warnings]}
          (capture-openhtmltopdf-warnings
            #(let [pdf-path (energiatodistus-pdf-service/generate-et-pdf-as-file
                              db
                              whoami
                              complete-energiatodistus
                              language-code
                              true
                              "warning-test-signature-id")]
               (try
                 (with-open [^InputStream pdf-stream (io/input-stream pdf-path)]
                   (let [pdf-bytes (.readAllBytes pdf-stream)
                         pdf-document (PDDocument/load pdf-bytes)]
                     (try
                       {:byte-count (alength pdf-bytes)
                        :page-count (.getNumberOfPages pdf-document)}
                       (finally
                         (.close pdf-document)))))
                 (finally
                   (io/delete-file pdf-path true))))) ]
      (t/is (some? complete-energiatodistus) "Complete energiatodistus should be returned for PDF generation")
      (t/is (pos? (:byte-count result)) "Generated energiatodistus PDF should contain bytes")
      (t/is (pos? (:page-count result)) "Generated energiatodistus PDF should have at least one page")
      (assert-only-expected-openhtmltopdf-warnings! warnings expected-warnings)))

  (t/testing "perusparannuspassi emits all and only the expected OpenHTMLToPDF warnings"
    (let [[laatija-id whoami] (create-laatija-and-whoami!)
          ppp-id (create-perusparannuspassi! laatija-id whoami)
          expected-warnings []
          {:keys [result warnings]}
          (capture-openhtmltopdf-warnings
            #(with-open [^InputStream pdf-stream
                         (ppp-pdf-service/find-perusparannuspassi-pdf ts/*db* whoami ppp-id "fi")]
               (t/is (some? pdf-stream) "Perusparannuspassi PDF stream should be returned")
               (let [pdf-bytes (.readAllBytes pdf-stream)
                     pdf-document (PDDocument/load pdf-bytes)]
                 (try
                   {:byte-count (alength pdf-bytes)
                    :page-count (.getNumberOfPages pdf-document)}
                   (finally
                     (.close pdf-document))))))]
      (t/is (pos? (:byte-count result)) "Generated perusparannuspassi PDF should contain bytes")
      (t/is (pos? (:page-count result)) "Generated perusparannuspassi PDF should have at least one page")
      (assert-only-expected-openhtmltopdf-warnings! warnings expected-warnings))))

