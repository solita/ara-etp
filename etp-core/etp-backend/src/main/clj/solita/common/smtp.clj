(ns solita.common.smtp
  (:require [clojure.tools.logging :as log])
  (:import (javax.mail Message$RecipientType Session Transport)
           (javax.mail.internet InternetAddress
                                MimeMessage
                                MimeBodyPart
                                MimeMultipart
                                InternetHeaders)
           (java.util Base64 Properties)
           (java.io File InputStream)
           (org.apache.commons.io IOUtils)))

(def timeout "5000")
(def charset "UTF-8")

(defn- mail-properties [port]
  (doto (System/getProperties)
    (.put "mail.transport.protocol" "smtp")
    (.put "mail.smtp.port" port)
    (.put "mail.smtp.starttls.enable" "true")
    (.put "mail.smtp.auth" "true")
    (.put "mail.smtp.connectiontimeout" "5000")
    (.put "mail.smtp.timeout" "5000")))

(defn- session [^Properties properties]
  (Session/getDefaultInstance properties))

(defn- mime-message [^Session session
                     ^String from-email ^String from-name
                     to ^String subject]
  (doto (MimeMessage. session)
    (.setFrom (InternetAddress. from-email from-name))
    (.setSubject subject)
    (.setRecipients Message$RecipientType/TO
                    ^"[Ljavax.mail.internet.InternetAddress;"
                    (->> to
                         (map #(InternetAddress. %))
                         (into-array InternetAddress)))))

(defn- send-email! [host port username password
                    from-email from-name to subject
                    add-content reply-to-email reply-to-name]
  (let [^Properties properties (mail-properties port)
        ^Session session (session properties)
        ^MimeMessage mime-message (mime-message session
                                                from-email
                                                from-name
                                                to
                                                subject)]
    (with-open [^Transport transport (.getTransport session)]
      (.connect transport host username password)
      (add-content mime-message)
      (when (and reply-to-email reply-to-name)
        (.setReplyTo mime-message (into-array [(InternetAddress. reply-to-email reply-to-name)])))
      (.sendMessage transport mime-message
                    (.getAllRecipients mime-message))
      (log/info "Email sent " {:to to :subject subject})
      nil)))

(defn- body-mime-body-part [^String body ^String subtype]
  (doto (MimeBodyPart.)
    (.setText body charset subtype)))

(defn- multipart [& mime-body-parts]
  (let [mime-multi-part (MimeMultipart.)]
    (doseq [^MimeBodyPart mime-body-part mime-body-parts]
      (.addBodyPart mime-multi-part mime-body-part))
    mime-multi-part))

(defn- add-multipart [^MimeMultipart content ^MimeMessage message]
  (.setContent message content))

(defn file->attachment [^File file]
  (doto (MimeBodyPart.)
    (.attachFile file)))

(defn input-stream->attachment [^InputStream input-stream ^String name ^String content-type]
  (doto (MimeBodyPart. (InternetHeaders.) (.encode (Base64/getEncoder) (IOUtils/toByteArray input-stream)))
    (.setFileName name)
    (.setHeader "Content-Type" content-type)
    (.setHeader "Content-Transfer-Encoding" "base64")
    (.setDisposition MimeBodyPart/ATTACHMENT)))

(defn send-multipart-email! [{:keys [host port username password
                                     from-email from-name to subject
                                     ^String body ^String subtype attachments
                                     reply-to-email reply-to-name]}]
  (let [body-mime-body-part (body-mime-body-part body subtype)
        multipart (apply multipart body-mime-body-part attachments)]
    (send-email! host port username password
                 from-email from-name to subject
                 (partial add-multipart multipart)
                 reply-to-email reply-to-name)))

(defn- set-text [^String body ^String subtype ^MimeMessage message]
  (.setText message body charset subtype))

(defn send-text-email! [{:keys [host port username password
                                from-email from-name to subject
                                ^String body ^String subtype
                                reply-to-email reply-to-name]}]
  (send-email! host port username password
               from-email from-name to subject
               (partial set-text body subtype)
               reply-to-email reply-to-name))
