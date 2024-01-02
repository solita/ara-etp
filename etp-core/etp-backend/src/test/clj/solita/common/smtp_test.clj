(ns solita.common.smtp-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [solita.etp.config :as config]
            [solita.common.smtp :as smtp]))

(def sender-email "sender@example.com")
(def sender-name "Sender")
(def to ["recipient1@example.com" "recipient2@example.com"])
(def subject "Subject")
(def body "This is body!")
(def subtype "plain")
(def attachments (map smtp/file->attachment [(io/file "deps.edn") (io/file "start.sh")]))

(def result-email-from-and-to
  (format "From: %s <%s>
To: %s"
          sender-name
          sender-email
          (str/join ", " to)))

(def result-email-subject-and-mime
  (format "Subject: %s
MIME-Version: 1.0
Content-Type: multipart/mixed;"
          subject))

(def result-email-body-part
  (format "Content-Type: text/plain; charset=UTF-8
Content-Transfer-Encoding: 7bit

%s" body))

(def result-email-attachment-1-part
  "Content-Type: application/octet-stream; name=deps.edn
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename=deps.edn

{:paths ")

(def result-email-attachment-2-part
  "Content-Type: application/octet-stream; name=start.sh
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename=start.sh

#!/bin/bash")

(def email-directory (io/file "../docker/smtp/received-emails"))

(defn email-directory-files []
  (sort (.listFiles email-directory)))

(defn empty-email-directory! []
  (doseq [file (email-directory-files)]
    (io/delete-file file)))

(defn send-email! [attachments]
  (smtp/send-multipart-email!
    {:host        config/smtp-host
     :port        config/smtp-port
     :username    config/smtp-username
     :password    config/smtp-password
     :from-email  sender-email
     :from-name   sender-name
     :to          to
     :subject     subject
     :body        body
     :subtype     subtype
     :attachments attachments}))

;; 2 recipients x 2 emails => 4 files.
(t/deftest ^:eftest/synchronized send-email!-test
  (empty-email-directory!)
  (t/is (= 0 (count (email-directory-files))))
  (send-email! nil)
  (send-email! attachments)
  (let [[first-email _ second-email :as emails] (email-directory-files)
        first-email-content (slurp first-email)
        second-email-content (slurp second-email)]
    (t/is (= 4 (count emails)))
    (doseq [email-content [first-email-content second-email-content]]
      (t/is (str/includes? first-email-content result-email-from-and-to))
      (t/is (str/includes? first-email-content result-email-subject-and-mime))
      (t/is (str/includes? first-email-content result-email-body-part)))
    (t/is (not (str/includes? first-email-content result-email-attachment-1-part)))
    (t/is (not (str/includes? first-email-content result-email-attachment-2-part)))
    (t/is (str/includes? second-email-content result-email-attachment-1-part))
    (t/is (str/includes? second-email-content result-email-attachment-2-part))))
