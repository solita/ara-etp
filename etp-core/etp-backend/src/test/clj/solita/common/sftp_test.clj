(ns solita.common.sftp-test
  (:require [clojure.test :as t]
            [solita.common.sftp :as sftp]))

(def host "localhost")
(def port 2222)
(def username "etp")
(def password "etp")
(def known-hosts-path "known_hosts")
(def src-path "deps.edn")

(t/deftest sftp-test
  (let [tmp-dir-part-1 (.toString (java.util.UUID/randomUUID))
        tmp-dir-part-2 "tmp"
        tmp-dir (str tmp-dir-part-1 "/" tmp-dir-part-2)
        dst-path (str tmp-dir "/" src-path)]
    (with-open [connection (sftp/connect! host port username password known-hosts-path)]
      (t/is (sftp/connected? connection))

      ;; Creating a directory
      (t/is (not (sftp/file-exists? connection tmp-dir)))
      (sftp/make-directory! connection tmp-dir)
      (sftp/make-directory! connection tmp-dir)
      (t/is (sftp/file-exists? connection tmp-dir))

      ;; Uploading the file
      (t/is (not (sftp/file-exists? connection dst-path)))
      (sftp/upload! connection src-path dst-path)
      (t/is (sftp/file-exists? connection dst-path))

      ;; Deleting the file
      (sftp/delete! connection dst-path)
      (sftp/delete! connection dst-path)
      (t/is (not (sftp/file-exists? connection dst-path)))

      ;; Deleting the directory
      (sftp/delete! connection tmp-dir true)
      (t/is (not (sftp/file-exists? connection tmp-dir))))))
