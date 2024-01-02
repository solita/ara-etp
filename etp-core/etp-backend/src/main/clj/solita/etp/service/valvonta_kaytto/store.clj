(ns solita.etp.service.valvonta-kaytto.store
  (:require [solita.etp.service.file :as file-service]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [clojure.java.io :as io]))

(def document-file-key-prefix "valvonta/kaytto")
(def hallinto-oikeus-attachment-file-key-prefix "valvonta/kaytto/hallinto-oikeus-attachment")

(defn- file-path [file-key-prefix valvonta-id toimenpide-id osapuoli]
  (cond
    (osapuoli/henkilo? osapuoli) (str file-key-prefix "/" valvonta-id "/" toimenpide-id "/henkilo/" (:id osapuoli))
    (osapuoli/yritys? osapuoli) (str file-key-prefix "/" valvonta-id "/" toimenpide-id "/yritys/" (:id osapuoli))))

(defn store-document!
  "Store the main document of the käytönvalvonta toimenpide to the object storage"
  [aws-s3-client valvonta-id toimenpide-id osapuoli document]
  (file-service/upsert-file-from-bytes
    aws-s3-client
    (file-path document-file-key-prefix valvonta-id toimenpide-id osapuoli)
    document))

(defn find-document
  "Retrieve the main document of the käytönvalvonta toimenpide from the object storage"
  [aws-s3-client valvonta-id toimenpide-id osapuoli]
  (file-service/find-file aws-s3-client (file-path document-file-key-prefix valvonta-id toimenpide-id osapuoli)))

(defn info-letter []
  (-> "pdf/Saate_rakennuksen_omistaja_su_ru.pdf" io/resource io/input-stream))

(defn ^:dynamic store-hallinto-oikeus-attachment!
  "Store the hallinto-oikeus attachment of the käytönvalvonta toimenpide to the object storage"
  [aws-s3-client valvonta-id toimenpide-id osapuoli document]
  (file-service/upsert-file-from-bytes
    aws-s3-client
    (file-path hallinto-oikeus-attachment-file-key-prefix valvonta-id toimenpide-id osapuoli)
    document))

(defn find-hallinto-oikeus-attachment
  "Retrieve the hallinto-oikeus attachment of the käytönvalvonta toimenpide from the object storage"
  [aws-s3-client valvonta-id toimenpide-id osapuoli]
  (file-service/find-file aws-s3-client (file-path hallinto-oikeus-attachment-file-key-prefix valvonta-id toimenpide-id osapuoli)))
