(ns solita.etp.service.file-type
  "Detects a file's actual format from the content bytes of the file,
  independent of any content-type or file extension supplied by the
  uploader.

  The detection is intentionally simple: it only looks at a handful of
  bytes at the start of the file (\"magic numbers\") and, for zip-based
  container formats, does a plain substring search for a few
  well-known entry names within the bytes that were read. This is not
  a full, general purpose file-format parser (such as Apache Tika) -
  the goal is only to recognize the file formats that ETP needs to
  react to (common office/image formats and executables), while
  keeping the implementation small and easy to reason about from a
  security perspective."
  (:require [clojure.string :as string])
  (:import (java.io InputStream)
           (java.nio.charset StandardCharsets)))

;; Number of bytes read from the beginning of the file for the
;; detection. Large enough to reach the first few zip entries of a
;; typical office document (where the entries identifying the
;; document's subtype usually reside), but small enough to avoid
;; reading whole large files into memory.
(def buffer-size (* 64 1024))

(def unknown-file-type
  "Returned when the content does not match any of the recognized
  file formats, or the file is empty."
  {:content-type "application/octet-stream" :extensions [] :executable false})

(defn- unsigned-byte [^bytes bs i]
  (bit-and 0xff (aget bs i)))

(defn- prefix-match?
  "True when the bytes in `bs` starting from `offset` match the given
  `pattern` (a sequence of unsigned byte values, 0-255)."
  [^bytes bs offset pattern]
  (and (>= (- (alength bs) offset) (count pattern))
       (every? (fn [[i expected]] (= expected (unsigned-byte bs (+ offset i))))
               (map-indexed vector pattern))))

;; *** Simple, offset-0 (unless noted) magic number signatures ***
;; Ordered so that more specific signatures are tried before more
;; generic ones with overlapping prefixes.
;;
;; :executable marks formats that are (or can directly contain) a
;; runnable program, so that callers can weed those out without
;; having to know every individual content-type involved.
(def ^:private magic-number-signatures
  [;; Documents / archives
   {:content-type "application/pdf" :extensions ["pdf"] :executable false
    :magic        [0x25 0x50 0x44 0x46]}                    ; %PDF
   {:content-type "application/gzip" :extensions ["gz"] :executable false
    :magic        [0x1F 0x8B]}
   {:content-type "application/x-7z-compressed" :extensions ["7z"] :executable false
    :magic        [0x37 0x7A 0xBC 0xAF 0x27 0x1C]}
   {:content-type "application/x-rar-compressed" :extensions ["rar"] :executable false
    :magic        [0x52 0x61 0x72 0x21 0x1A 0x07]}
   {:content-type "application/x-cfb" :extensions ["doc" "xls" "ppt" "msi"] :executable false
    :magic        [0xD0 0xCF 0x11 0xE0 0xA1 0xB1 0x1A 0xE1]} ; old MS Office / MSI (OLE2)

   ;; Images
   {:content-type "image/png" :extensions ["png"] :executable false
    :magic        [0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A]}
   {:content-type "image/jpeg" :extensions ["jpg" "jpeg"] :executable false
    :magic        [0xFF 0xD8 0xFF]}
   {:content-type "image/gif" :extensions ["gif"] :executable false
    :magic        [0x47 0x49 0x46 0x38]}                    ; GIF87a / GIF89a
   {:content-type "image/bmp" :extensions ["bmp"] :executable false
    :magic        [0x42 0x4D]}
   {:content-type "image/tiff" :extensions ["tif" "tiff"] :executable false
    :magic        [0x49 0x49 0x2A 0x00]}                    ; little-endian
   {:content-type "image/tiff" :extensions ["tif" "tiff"] :executable false
    :magic        [0x4D 0x4D 0x00 0x2A]}                    ; big-endian

   ;; Executables and other formats that should never be accepted as
   ;; attachments.
   {:content-type "application/x-msdownload" :extensions ["exe" "dll"] :executable true
    :magic        [0x4D 0x5A]}                              ; MZ (Windows PE)
   {:content-type "application/x-elf" :extensions [] :executable true
    :magic        [0x7F 0x45 0x4C 0x46]}
   {:content-type "application/x-mach-binary" :extensions [] :executable true
    :magic        [0xFE 0xED 0xFA 0xCE]}
   {:content-type "application/x-mach-binary" :extensions [] :executable true
    :magic        [0xFE 0xED 0xFA 0xCF]}
   {:content-type "application/x-mach-binary" :extensions [] :executable true
    :magic        [0xCE 0xFA 0xED 0xFE]}
   {:content-type "application/x-mach-binary" :extensions [] :executable true
    :magic        [0xCF 0xFA 0xED 0xFE]}
   {:content-type "application/java-vm" :extensions ["class"] :executable true
    :magic        [0xCA 0xFE 0xBA 0xBE]}
   {:content-type "application/x-sh" :extensions ["sh"] :executable true
    :magic        [0x23 0x21]}])                            ; #! (shebang)

(defn- match-magic-number-signature [^bytes bs]
  (some (fn [{:keys [magic] :as signature}]
          (when (prefix-match? bs 0 magic)
            (select-keys signature [:content-type :extensions :executable])))
        magic-number-signatures))

(defn- webp? [^bytes bs]
  (and (prefix-match? bs 0 [0x52 0x49 0x46 0x46])           ; RIFF
       (prefix-match? bs 8 [0x57 0x45 0x42 0x50])))         ; WEBP

(def ^:private zip-signatures
  [[0x50 0x4B 0x03 0x04]                                    ; local file header
   [0x50 0x4B 0x05 0x06]                                    ; empty archive
   [0x50 0x4B 0x07 0x08]])                                  ; spanned archive

(defn- zip? [^bytes bs]
  (some #(prefix-match? bs 0 %) zip-signatures))

;; Zip entry names are stored as plain (uncompressed) bytes in each
;; entry's local file header even when the entry's content is
;; compressed, so a simple substring search over the raw bytes is
;; enough to recognize the well-known entries used by these zip-based
;; container formats without having to actually parse the zip
;; structure.
(def ^:private zip-subtype-markers
  [{:contains     "word/document.xml"
    :content-type "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    :extensions   ["docx"] :executable false}
   {:contains     "xl/workbook.xml"
    :content-type "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    :extensions   ["xlsx"] :executable false}
   {:contains     "ppt/presentation.xml"
    :content-type "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    :extensions   ["pptx"] :executable false}])

(def ^:private opendocument-mimetype->result
  {"application/vnd.oasis.opendocument.text"         {:content-type "application/vnd.oasis.opendocument.text"
                                                      :extensions ["odt"]
                                                      :executable false}
   "application/vnd.oasis.opendocument.spreadsheet"  {:content-type "application/vnd.oasis.opendocument.spreadsheet"
                                                      :extensions ["ods"]
                                                      :executable false}
   "application/vnd.oasis.opendocument.presentation" {:content-type "application/vnd.oasis.opendocument.presentation"
                                                      :extensions ["odp"]
                                                      :executable false}})

(defn- bytes->latin1-string [^bytes bs]
  ;; ISO-8859-1 maps every byte value to exactly one char, so this is
  ;; a lossless, allocation-cheap way to search the raw bytes for
  ;; ASCII substrings such as zip entry names.
  (String. bs StandardCharsets/ISO_8859_1))

(defn- opendocument-subtype [^String s]
  (some (fn [[mimetype result]] (when (string/includes? s mimetype) result))
        opendocument-mimetype->result))

(defn- openxml-subtype [^String s]
  (some (fn [{:keys [contains content-type extensions executable]}]
          (when (string/includes? s contains)
            {:content-type content-type :extensions extensions :executable executable}))
        zip-subtype-markers))

(defn- zip-subtype [^bytes bs]
  (let [s (bytes->latin1-string bs)]
    (or (opendocument-subtype s)
        (openxml-subtype s))))

(defn detect
  "Reads (at most) `buffer-size` bytes from the beginning of
  `input-stream` and detects the file format based on its content.

  Returns a map {:content-type \"...\" :extensions [...] :executable
  bool}. `extensions` lists the (lowercase, without a leading dot)
  file extensions that are conventionally used for the detected
  format, in preference order. `executable` is true when the detected
  format is (or can directly contain) a runnable program, so callers
  can reject such attachments without needing to enumerate every
  individual content-type. Returns `unknown-file-type`
  (application/octet-stream, no extensions, not executable) for empty
  content or content that does not match any recognized format.

  Does not close `input-stream`."
  [^InputStream input-stream]
  (let [bs (.readNBytes input-stream buffer-size)]
    (cond
      (zero? (alength bs)) unknown-file-type
      (webp? bs) {:content-type "image/webp"
                  :extensions ["webp"]
                  :executable false}
      (zip? bs) (or (zip-subtype bs) {:content-type "application/zip"
                                      :extensions ["zip"]
                                      :executable false})
      :else (or (match-magic-number-signature bs) unknown-file-type))))
