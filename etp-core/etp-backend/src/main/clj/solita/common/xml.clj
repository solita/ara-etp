(ns solita.common.xml
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.core.match :as match]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [camel-snake-kebab.core :as csk])
  (:import (java.net URL)
           (java.io StringReader)
           (javax.xml XMLConstants)
           (javax.xml.stream XMLStreamException)
           (javax.xml.transform.stream StreamSource)
           (javax.xml.validation SchemaFactory Schema)
           (org.xml.sax SAXException)))

(def envelope-url "http://schemas.xmlsoap.org/soap/envelope/")

(def default-schema-language XMLConstants/W3C_XML_SCHEMA_NS_URI)
(def schema-factory (SchemaFactory/newInstance default-schema-language))

(def element xml/element)
(def qname xml/qname)
(def emit xml/emit)
(def emit-str xml/emit-str)

(defn- xml-not-well-formed [e]
  (ex-info (.getMessage e) {:type :xml-not-well-formed} e))

(defn input-stream->xml [is]
  (try
    (xml/parse is)
    (catch Exception e
      (throw (xml-not-well-formed e)))))

(defn string->xml [string]
  (xml/parse-str string))

(defn xml? [x]
  (and (associative? x)
       (every? #(contains? x %) [:tag :content])))

(defn without-soap-envelope [{:keys [content] :as xml}]
  (if (-> (-> xml :tag name str/lower-case) (= "envelope"))
    (->> xml
         :content
         (filter #(= (some-> % :tag name str/lower-case) "body"))
         first
         :content
         (filter xml?))
    xml))

(defn with-soap-envelope [xml]
  (element (qname envelope-url "Envelope")
           {}
           (element (qname envelope-url "Body") {} xml)))

(defn with-kebab-case-tags [xml]
  (walk/postwalk (fn [x]
                   (if (xml? x)
                     (update x :tag (comp keyword csk/->kebab-case name))
                     x))
                 xml))

(defn load-schema [s local?]
  (.newSchema schema-factory (if local?
                               (-> s io/resource)
                               (URL. s))))

;; Use of emit-str is safe and works, but it's not memory-efficient.
;; Using emit with PipedOutputStream and PipedInputStream would
;; be efficient, but would required additional threads.
(defn xml->stream-source [xml]
  (-> xml xml/emit-str StringReader. StreamSource.))

(defn validate-xsd! [xml ^Schema schema]
  (try
    (-> schema .newValidator (.validate (xml->stream-source xml)))
    (catch XMLStreamException e
      ;; Could happen if the lazy document loader did not perform full
      ;; parsing in the parse function.
      (throw (xml-not-well-formed e)))
    (catch SAXException e
      (throw (ex-info (.getMessage e) {:type :xml-xsd-validation-failed} e)))))

(defn get-in-xml
  "Similar to get-in in core but works with xml. Keywords filter elements by
  tag type while integers choose elements using index. Multiple keywords in
  succession will default to first tag of that type, meaning that [:a :b :c]
  is essentially same as [:a 0 :b 0 :c]"
  [xml path]
  (reduce (fn [acc k]
            (match/match [(int? k) (xml? acc)]
                         [true true] (get (->> acc :content (into [])) k)
                         [true false] (get (into [] acc) k)
                         [false true] (filter #(= k (:tag %)) (:content acc))
                         [false false] (filter #(= k (:tag %)) (-> acc first :content))))
          xml
          path))

(defn get-content
  "Similar to get-in-xml, but returns the first content at the end of the path.
   Meant to be used when it's expected that there's a leaf element at the end
   of the path."
  [xml path]
  (let [path (if (-> path last keyword?) (conj path 0) path)]
    (-> xml (get-in-xml path) :content first)))

(defn simple-elements
  "Walks coll and converts vectors where first element is string into xml
  elements: [\"foo\" \"bar\"] => <foo>bar</foo>."
  [coll]
  (walk/postwalk (fn [x]
                   (if (and (vector? x) (-> x first string?))
                     (apply (partial element (first x) {}) (rest x))
                     x))
                 coll))
