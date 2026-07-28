(ns solita.etp.service.file-type-test
  (:require [clojure.test :as t]
            [solita.etp.service.file-type :as service])
  (:import (java.io ByteArrayInputStream)
           (java.nio.charset StandardCharsets)))

(defn- ->stream ^ByteArrayInputStream [byte-values]
  (ByteArrayInputStream. (byte-array (map unchecked-byte byte-values))))

(defn- concat-bytes [& parts]
  (mapcat (fn [part]
            (cond
              (string? part) (map int (.getBytes ^String part StandardCharsets/ISO_8859_1))
              :else part))
          parts))

(t/deftest empty-content-is-unknown-test
  (t/is (= service/unknown-file-type
           (service/detect (->stream []))))
  (t/is (= "application/octet-stream" (:content-type (service/detect (->stream [])))))
  (t/is (false? (:executable (service/detect (->stream []))))))

(t/deftest executable-key-present-on-every-result-test
  (t/is (every? #(contains? % :executable)
                [(service/detect (->stream []))
                 (service/detect (->stream (concat-bytes "%PDF-1.7" (repeat 20 0))))
                 (service/detect (->stream (concat-bytes [0x4D 0x5A 0x90 0x00] (repeat 20 0))))
                 (service/detect (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "some-file.txt" (repeat 20 0))))])))

(t/deftest unrecognized-content-is-unknown-test
  (t/is (= service/unknown-file-type
           (service/detect (->stream (repeat 32 0x00))))))

(t/deftest pdf-detection-test
  (t/is (= {:content-type "application/pdf" :extensions ["pdf"] :executable false}
           (service/detect (->stream (concat-bytes "%PDF-1.7\n%..." (repeat 20 0)))))))

(t/deftest png-detection-test
  (t/is (= {:content-type "image/png" :extensions ["png"] :executable false}
           (service/detect (->stream (concat-bytes [0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A]
                                                     (repeat 20 0)))))))

(t/deftest jpeg-detection-test
  (t/is (= {:content-type "image/jpeg" :extensions ["jpg" "jpeg"] :executable false}
           (service/detect (->stream (concat-bytes [0xFF 0xD8 0xFF 0xE0] (repeat 20 0)))))))

(t/deftest gif-detection-test
  (t/is (= {:content-type "image/gif" :extensions ["gif"] :executable false}
           (service/detect (->stream (concat-bytes "GIF89a" (repeat 20 0))))))
  (t/is (= {:content-type "image/gif" :extensions ["gif"] :executable false}
           (service/detect (->stream (concat-bytes "GIF87a" (repeat 20 0)))))))

(t/deftest bmp-detection-test
  (t/is (= {:content-type "image/bmp" :extensions ["bmp"] :executable false}
           (service/detect (->stream (concat-bytes "BM" (repeat 20 0)))))))

(t/deftest tiff-detection-test
  (t/is (= {:content-type "image/tiff" :extensions ["tif" "tiff"] :executable false}
           (service/detect (->stream (concat-bytes [0x49 0x49 0x2A 0x00] (repeat 20 0))))))
  (t/is (= {:content-type "image/tiff" :extensions ["tif" "tiff"] :executable false}
           (service/detect (->stream (concat-bytes [0x4D 0x4D 0x00 0x2A] (repeat 20 0)))))))

(t/deftest webp-detection-test
  (t/is (= {:content-type "image/webp" :extensions ["webp"] :executable false}
           (service/detect (->stream (concat-bytes "RIFF" [0 0 0 0] "WEBPVP8 " (repeat 20 0)))))))

(t/deftest gzip-detection-test
  (t/is (= {:content-type "application/gzip" :extensions ["gz"] :executable false}
           (service/detect (->stream (concat-bytes [0x1F 0x8B 0x08 0x00] (repeat 20 0)))))))

(t/deftest generic-zip-detection-test
  (t/is (= {:content-type "application/zip" :extensions ["zip"] :executable false}
           (service/detect (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "some-file.txt" (repeat 20 0)))))))

(t/deftest docx-detection-test
  (t/is (= {:content-type "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            :extensions ["docx"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "[Content_Types].xml"
                                     [0x50 0x4B 0x03 0x04] "word/document.xml"
                                     (repeat 20 0)))))))

(t/deftest xlsx-detection-test
  (t/is (= {:content-type "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            :extensions ["xlsx"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "[Content_Types].xml"
                                     [0x50 0x4B 0x03 0x04] "xl/workbook.xml"
                                     (repeat 20 0)))))))

(t/deftest pptx-detection-test
  (t/is (= {:content-type "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            :extensions ["pptx"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "[Content_Types].xml"
                                     [0x50 0x4B 0x03 0x04] "ppt/presentation.xml"
                                     (repeat 20 0)))))))

(t/deftest odt-detection-test
  (t/is (= {:content-type "application/vnd.oasis.opendocument.text" :extensions ["odt"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "mimetype"
                                     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                     "application/vnd.oasis.opendocument.text"
                                     (repeat 20 0)))))))

(t/deftest ods-detection-test
  (t/is (= {:content-type "application/vnd.oasis.opendocument.spreadsheet" :extensions ["ods"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "mimetype"
                                     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                     "application/vnd.oasis.opendocument.spreadsheet"
                                     (repeat 20 0)))))))

(t/deftest odp-detection-test
  (t/is (= {:content-type "application/vnd.oasis.opendocument.presentation" :extensions ["odp"] :executable false}
           (service/detect
            (->stream (concat-bytes [0x50 0x4B 0x03 0x04] "mimetype"
                                     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                     "application/vnd.oasis.opendocument.presentation"
                                     (repeat 20 0)))))))

(t/deftest old-office-ole2-detection-test
  (t/is (= {:content-type "application/x-cfb" :extensions ["doc" "xls" "ppt" "msi"] :executable false}
           (service/detect
            (->stream (concat-bytes [0xD0 0xCF 0x11 0xE0 0xA1 0xB1 0x1A 0xE1] (repeat 20 0)))))))

(t/deftest windows-executable-detection-test
  (t/is (= {:content-type "application/x-msdownload" :extensions ["exe" "dll"] :executable true}
           (service/detect (->stream (concat-bytes [0x4D 0x5A 0x90 0x00] (repeat 20 0)))))))

(t/deftest elf-executable-detection-test
  (t/is (= {:content-type "application/x-elf" :extensions [] :executable true}
           (service/detect (->stream (concat-bytes [0x7F 0x45 0x4C 0x46] (repeat 20 0)))))))

(t/deftest mach-o-executable-detection-test
  (doseq [magic [[0xFE 0xED 0xFA 0xCE] [0xFE 0xED 0xFA 0xCF]
                 [0xCE 0xFA 0xED 0xFE] [0xCF 0xFA 0xED 0xFE]]]
    (t/is (= {:content-type "application/x-mach-binary" :extensions [] :executable true}
             (service/detect (->stream (concat-bytes magic (repeat 20 0))))))))

(t/deftest java-class-detection-test
  (t/is (= {:content-type "application/java-vm" :extensions ["class"] :executable true}
           (service/detect (->stream (concat-bytes [0xCA 0xFE 0xBA 0xBE 0x00 0x00 0x00 0x34] (repeat 20 0)))))))

(t/deftest shell-script-detection-test
  (t/is (= {:content-type "application/x-sh" :extensions ["sh"] :executable true}
           (service/detect (->stream (concat-bytes "#!/bin/sh\necho hi\n"))))))
