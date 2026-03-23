(ns solita.etp.test-runner
  (:require [clojure.string :as str]))

(def ^:private windows? (str/includes? (System/getProperty "os.name") "Windows"))

(defn- is-terminal?
  "Check if stdout is connected to a terminal (TTY)."
  []
  (some? (System/console)))

(defn- non-brokens [vars]
  (filter #(and (-> % meta :broken-test not)
                (or (not windows?) (-> % meta :broken-on-windows-test not)))
          vars))

(defn- matches-filter? [var-ref filters]
  (let [fqn (str (symbol var-ref))]
    (some #(str/includes? fqn %) filters)))

(defn find-tests [& filters]
  (require 'eftest.runner)
  (cond-> ((resolve 'eftest.runner/find-tests) "src/test")
    true non-brokens
    (seq filters) (->> (filter #(matches-filter? % filters)))))

(defn run-tests
  ([] (run-tests {}))
  ([config & filters]
   (require 'eftest.runner)
   (require 'eftest.report.progress)
   (require 'eftest.report.pretty)
   (let [tests (apply find-tests filters)
         base-config (assoc config
                           :report (if (is-terminal?)
                                     (resolve 'eftest.report.progress/report)
                                     (resolve 'eftest.report.pretty/report)))]
     ((resolve 'eftest.runner/run-tests) tests base-config))))

(defn run-tests-and-exit! [& filters]
  (let [{:keys [fail error]} (apply run-tests {} filters)]
    (System/exit (if (and (zero? fail) (zero? error)) 0 1))))

(defn run-tests-with-junit-reporter-and-exit! []
  (require 'eftest.report)
  (require 'eftest.report.junit)
  (let [{:keys [fail error]} (run-tests {:report ((resolve 'eftest.report/report-to-file)
                                                  (resolve 'eftest.report.junit/report) "target/test.xml")})]
    (System/exit (if (and (zero? fail) (zero? error)) 0 1))))

(defn -main [& args]
  (apply run-tests-and-exit! args))
