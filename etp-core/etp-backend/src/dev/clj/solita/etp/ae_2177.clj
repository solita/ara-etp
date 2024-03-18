(ns solita.etp.ae-2177
  "Try out changing PDF metadata using pdfbox"
  (:import
    (java.io ByteArrayOutputStream File)
    (java.util Calendar)
    (org.apache.pdfbox.pdmodel PDDocument)
    (org.apache.pdfbox.pdmodel.common PDMetadata)
    (org.apache.xmpbox XMPMetadata)
    (org.apache.xmpbox.xml XmpSerializer)))

(defn make-xmp-metadata [author-name title create-date]
  (let [xmp-metadata (XMPMetadata/createXMPMetadata)]
    (doto (.createAndAddPFAIdentificationSchema xmp-metadata)
      (.setPart (Integer/valueOf 2))
      (.setConformance "B"))

    (doto (.createAndAddDublinCoreSchema xmp-metadata)
      (.setTitle title)
      (.addCreator author-name))

    (doto (.createAndAddXMPBasicSchema xmp-metadata)
      (.setCreateDate create-date))

    xmp-metadata))

(defn xmp-metadata->bytearray [metadata]
  (let [xmp-serializer (XmpSerializer.)
        buf (ByteArrayOutputStream.)]
    (.serialize xmp-serializer metadata buf true)
    (.toByteArray buf)))

(defn metadata->text [metadata]
  (let [xmp-serializer (XmpSerializer.)
        buf (ByteArrayOutputStream.)]
    (.serialize xmp-serializer metadata buf true)
    (.toString buf "UTF-8")))

(defn set-metadata [^String path-in ^String path-out ^String author-name title]
  (println "Reading in the document")
  (let [creation-date (Calendar/getInstance)
        document (PDDocument/load (File. path-in))]
    (try
      (let [metadata (PDMetadata. document)
            catalog (.getDocumentCatalog document)
            xmp-metadata-bytes (-> (make-xmp-metadata author-name title creation-date) xmp-metadata->bytearray)]
        (.importXMPMetadata metadata xmp-metadata-bytes)
        (.setMetadata catalog metadata))

      (doto (.getDocumentInformation document)
        (.setTitle title)
        (.setAuthor author-name)
        (.setSubject nil)
        (.setKeywords nil)
        (.setCreator nil)
        (.setProducer nil)
        (.setCreationDate creation-date)
        (.setModificationDate creation-date))

      (println "Saving the document")
      (.save document path-out)
      (finally
        (println "Closing the document")
        (.close document)))))