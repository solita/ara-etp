(ns solita.common.libreoffice
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import (java.io IOException File)
           (java.nio.file Files Path)
           (java.nio.file.attribute FileAttribute)))

;; Recursive delete from https://gist.github.com/olieidel/c551a911a4798312e4ef42a584677397
(defn rm [^File file]
  (when (.isDirectory file)
    (doseq [file-in-dir (.listFiles file)]
      (rm file-in-dir)))
  (io/delete-file file))

(defn rm-path [^Path path]
  (let [file (.toFile path)]
    (when (.exists file) (rm file))))

(def config-path
  ;; The libreoffice configuration is defined in this directory.
  ;; Directory must exists but can be empty if no specific configuration is required.
  "./libreoffice/config")

(defn make-tmpdir []
  (let [attrs (make-array FileAttribute 0)]
    (Files/createTempDirectory "libreoffice-user" attrs)))

(defn populate-tmpdir [path]
  (let [target (str path "/config")
        cmd ["cp" "-r" config-path target]
        result (apply shell/sh cmd)]
    (when (not (= 0 (:exit result)))
      (throw (IOException.
               (str "Libreoffice configuration copy from: " config-path
                    " to: " target " failed for: "
                    (:err result)))))))

(defn exec-libreoffice [tmpdir args]
  (let [dirparam (str "-env:UserInstallation=file://" tmpdir "/config")]
    (apply shell/sh
           "libreoffice"
           dirparam
           "--headless"
           args)))

(defn exec-libreoffice-on-windows [args]
  ;; On windows, it seems to be difficult to get the UserInstallation parameter
  ;; to work at all. Because this is strictcly a development situation, we
  ;; ignore the usual need of having a per-process configuration. Instead, we
  ;; expect the developer to not run multiple PDF generaion operations
  ;; concurrently
  (apply shell/sh "C:\\Program Files\\LibreOffice\\program\\soffice.bin" "--headless" args))

(defn run-with-args [& args]
  (if  (-> "os.name" System/getProperty (.startsWith "Windows "))
    (exec-libreoffice-on-windows args)
    (let [tmpdir (make-tmpdir)]
      (try
        (populate-tmpdir tmpdir)
        (exec-libreoffice tmpdir args)
        (finally (println (str 'rm-path tmpdir)))))))
