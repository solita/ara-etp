(ns solita.etp.service.liite
  (:require [solita.etp.db :as db]
            [solita.etp.schema.liite :as liite-schema]
            [solita.etp.service.json :as json]
            [solita.etp.service.file :as file-service]
            [solita.etp.service.file-type :as file-type]
            [solita.etp.service.energiatodistus :as energiatodistus-service]
            [solita.etp.exception :as exception]
            [schema.coerce :as coerce]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [clojure.string :as string]))

; *** Require sql functions ***
(db/require-queries 'liite)

; *** Conversions from database data types ***
(def coerce-liite (coerce/coercer liite-schema/Liite json/json-coercions))

(defn- insert-liite! [liite db]
  (-> (db/with-db-exception-translation jdbc/insert! db :liite liite db/default-opts)
      first
      :id))

(defn file-key [liite-id]
  (str "liitteet/" liite-id))

(defn- insert-file! [key aws-s3-client file]
  (file-service/upsert-file-from-file aws-s3-client key file))

(defn assert-permission! [db whoami energiatodistus-id]
  (when-not (energiatodistus-service/find-energiatodistus
              db whoami energiatodistus-id)
    (exception/throw-forbidden!)))

(defn- unescape-quoted-string
  "Multipart filenames are transmitted inside a quoted-string
  (RFC 2616 section 2.2), where a backslash escapes the character that
  follows it - for example, a literal quote character is sent as a
  backslash followed by a quote. Ring's multipart parsing does not undo
  this escaping, so any backslash followed by a character in the filename
  needs to be unescaped back to that character before it is stored."
  [s]
  (when s
    (string/replace s #"\\(.)" "$1")))

(defn temp-file->liite [temp-file]
  (-> temp-file
      (dissoc :tempfile :size)
      (set/rename-keys {:content-type :contenttype
                        :filename :nimi})
      (update :nimi unescape-quoted-string)))

(defn- file-extension
  "Returns the lowercase file extension (without the leading dot) of
  `filename`, or nil if the filename has no extension."
  [filename]
  (when filename
    (let [dot (string/last-index-of filename ".")]
      (when (and dot (pos? dot) (< (inc dot) (count filename)))
        (string/lower-case (subs filename (inc dot)))))))

;; Allows ASCII letters/digits, the Finnish/Swedish vowels, and a
;; handful of punctuation characters that commonly occur in file
;; names. Notably excludes path separators, quotes and control
;; characters, so that a filename can never be used to break out of
;; the content-disposition header it is later served in, or to smuggle
;; a path when (mis)interpreted by a downstream program.
(def ^:private filename-pattern
  #"[a-zA-Z0-9åäöÅÄÖ._() -]{2,100}")

(defn- filename-boundary-character? [ch]
  (contains? #{\. \space} ch))

(defn- valid-filename? [filename]
  (boolean
   (and filename
        (re-matches filename-pattern filename)
        (not (filename-boundary-character? (first filename)))
        (not (filename-boundary-character? (last filename))))))

(defn assert-valid-filename!
  "Rejects filenames that aren't 2-100 characters long, contain
  characters outside a small, safe allowlist, or start/end with a dot
  or space. This prevents attachment filenames from being used to
  inject unexpected content into the content-disposition header the
  filename is later served in.

  Shared by every attachment upload entry point (energiatodistus
  liitteet, valvonta liitteet as well as viestiketju liitteet)."
  [filename]
  (when-not (valid-filename? filename)
    (exception/throw-ex-info!
     :liite-invalid-filename
     "Liitetiedoston nimi ei ole sallitussa muodossa.")))

;; File extensions that are conventionally used for directly runnable
;; programs or scripts on common operating systems. Attachments with
;; one of these extensions are rejected regardless of what their
;; actual content looks like, as a defence-in-depth measure on top of
;; the content-based executable detection in resolve-content-type!
;; (which only catches formats it recognizes from their content, such
;; as a shebang line - a plain-text script without one would not be
;; flagged as executable by content alone).
(def ^:private forbidden-executable-extensions
  #{"exe" "com" "bat" "cmd" "sh" "bash" "zsh" "command" "msi" "msp"
    "ps1" "ps1xml" "psc1" "psm1" "vbs" "vbe" "js" "jse" "wsf" "wsh"
    "scr" "pif" "gadget" "application" "hta" "cpl" "msc" "jar" "apk"
    "app" "workflow" "action" "run" "bin" "out" "elf" "dylib" "so"})

(defn assert-extension-not-forbidden!
  "Rejects filenames whose extension is conventionally used for
  directly runnable programs or scripts.

  Shared by every attachment upload entry point (energiatodistus
  liitteet, valvonta liitteet as well as viestiketju liitteet)."
  [filename]
  (when (contains? forbidden-executable-extensions (file-extension filename))
    (exception/throw-ex-info!
     :liite-forbidden-extension
     "Liitetiedoston tiedostopääte ei ole sallittu.")))

(defn resolve-content-type!
  "Detects the file's actual format from its content and validates it
  against the content-type and filename declared by the uploader,
  rejecting the attachment when they don't agree. ETP does not care
  much about the content of attachments in general, but:

  - files that are (or can directly contain) a runnable program are
    always rejected, to mitigate the risk of users sending malicious
    executables to each other.
  - files whose content is recognized as a specific format are
    rejected unless the declared content-type matches, so that the
    uploader can't make ETP serve the file back later with an
    arbitrary, possibly misleading, content-type.
  - files whose content is recognized as a specific format are also
    rejected unless the filename's extension is one of the extensions
    conventionally used for that format.
  - files whose content isn't recognized as any specific format are
    always accepted regardless of their declared content-type or
    filename extension, and are stored with content-type
    application/octet-stream.

  Returns the content-type that should be stored for the attachment.

  Shared by every attachment upload entry point (energiatodistus
  liitteet, valvonta liitteet as well as viestiketju liitteet)."
  [tempfile declared-content-type filename]
  (let [{:keys [content-type extensions executable]}
        (with-open [stream (io/input-stream tempfile)]
          (file-type/detect stream))]
    (when executable
      (exception/throw-ex-info!
       :liite-executable
       "Liitetiedosto on suoritettava ohjelma eikä sitä voi lisätä liitteeksi."))
    (when (not= content-type "application/octet-stream")
      (when (not= content-type declared-content-type)
        (exception/throw-ex-info!
         :liite-content-type-mismatch
         "Liitetiedoston sisältö ei vastaa ilmoitettua tiedostotyyppiä."))
      (when-not (contains? (set extensions) (file-extension filename))
        (exception/throw-ex-info!
         :liite-extension-mismatch
         "Liitetiedoston tiedostopääte ei vastaa tiedoston sisältöä.")))
    content-type))

(defn add-liite-from-file! [db aws-s3-client energiatodistus-id file]
  (let [liite (temp-file->liite file)]
    (assert-valid-filename! (:nimi liite))
    (assert-extension-not-forbidden! (:nimi liite))
    (let [content-type (resolve-content-type!
                         (:tempfile file) (:contenttype liite) (:nimi liite))]
      (jdbc/with-db-transaction [db db]
        (let [id (-> liite
                     (assoc :contenttype content-type)
                     (assoc :energiatodistus-id energiatodistus-id)
                     (insert-liite! db))]
          (-> id file-key (insert-file! aws-s3-client (:tempfile file)))
          id)))))

(defn add-liitteet-from-files! [db aws-s3-client whoami energiatodistus-id files]
  (jdbc/with-db-transaction [db db]
    (assert-permission! db whoami energiatodistus-id)
    (mapv #(add-liite-from-file!
            db aws-s3-client energiatodistus-id %)
          files)))

(defn add-liite-from-link!
  ([db energiatodistus-id liite]
    (-> liite
        (assoc :energiatodistus-id energiatodistus-id)
        (assoc :contenttype "text/uri-list")
        (insert-liite! db)))
  ([db whoami energiatodistus-id liite]
   (assert-permission! db whoami energiatodistus-id)
   (add-liite-from-link! db energiatodistus-id liite)))

(defn find-energiatodistus-liitteet [db whoami energiatodistus-id]
  (jdbc/with-db-transaction [db db]
    (assert-permission! db whoami energiatodistus-id)
    (map coerce-liite
         (liite-db/select-liite-by-energiatodistus-id
          db {:energiatodistus-id energiatodistus-id}))))

(defn find-energiatodistus-liite-content [db whoami aws-s3-client liite-id]
  (when-let [liite (first (liite-db/select-liite db {:id liite-id}))]
    (assert-permission! db whoami (:energiatodistus-id liite))
    (assoc liite :content (file-service/find-file aws-s3-client (file-key liite-id)))))

(defn delete-liite! [db whoami liite-id]
  (let [energiatodistus-id (some->> {:id liite-id}
                                    (liite-db/select-liite db)
                                    first
                                    :energiatodistus-id)]
    (assert-permission! db whoami energiatodistus-id)
    (liite-db/delete-liite! db {:id liite-id})))
