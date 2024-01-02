(ns solita.common.xml-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [solita.common.xml :as xml]
            [solita.etp.test :as etp-test]))

(def xml-path "legacy-api/esimerkki-2018.xml")
(def raw-xml (-> xml-path io/resource io/input-stream xml/input-stream->xml))
(def xml-without-soap-envelope (-> raw-xml xml/without-soap-envelope first))
(def sanitized-xml (xml/with-kebab-case-tags xml-without-soap-envelope))

(def test-xsd-path "legacy-api/energiatodistus-2018.xsd")
(def test-schema (xml/load-schema test-xsd-path true))

(t/deftest input-stream->xml-test
  (t/is (= (:tag raw-xml)
           :xmlns.http%3A%2F%2Fschemas.xmlsoap.org%2Fsoap%2Fenvelope%2F/Envelope)))

(t/deftest xml?-test
  (t/is (xml/xml? raw-xml))
  (t/is (xml/xml? xml-without-soap-envelope))
  (t/is (xml/xml? sanitized-xml))
  (t/is (-> :something xml/xml? false?))
  (t/is (-> {:tag :hello} xml/xml? false?)))

(t/deftest without-soap-envelope-test
  (t/is (= "EnergiatodistusIlmoitus" (-> xml-without-soap-envelope :tag name)))
  (t/is (= (xml/without-soap-envelope xml-without-soap-envelope)
           xml-without-soap-envelope)))

(t/deftest with-soap-envelope-test
  (let [soap-enveloped-xml (xml/with-soap-envelope xml-without-soap-envelope)]
    (t/is (= (-> soap-enveloped-xml :tag name) "Envelope"))
    (t/is (= (-> soap-enveloped-xml :content first :tag name) "Body"))
    (t/is (= (-> soap-enveloped-xml :content first :content first)
             xml-without-soap-envelope))))

(t/deftest with-kebab-case-tags-test
  (t/is (= (:tag sanitized-xml) :energiatodistus-ilmoitus)))

(t/deftest load-schema-test
  (t/is (instance? javax.xml.validation.Schema test-schema)))

(t/deftest xml->stream-source-test
  (t/is (instance? javax.xml.transform.stream.StreamSource
                   (xml/xml->stream-source raw-xml))))

(t/deftest schema-validation-test
  (xml/validate-xsd! xml-without-soap-envelope test-schema)
  (t/is (= (etp-test/catch-ex-data #(xml/validate-xsd! sanitized-xml test-schema))
           {:type :xml-xsd-validation-failed})))

(t/deftest get-in-xml-test
  (t/is (= "Kilpikonna"
           (-> sanitized-xml
               (xml/get-in-xml [:energiatodistus 0 :perustiedot 0 :nimi 0])
               :content
               first)))
  (t/is (= "Kilpikonna"
           (-> sanitized-xml
               (xml/get-in-xml [:energiatodistus :perustiedot :nimi])
               first
               :content
               first)))
  (t/is (= "14.0"
           (-> sanitized-xml
               (xml/get-in-xml [:energiatodistus
                                :lahtotiedot
                                :lahtotiedot-sis-kuormat
                                :sis-kuorma
                                1
                                :valaistus
                                0])
               :content
               first))))

(t/deftest get-content-test
  (t/is (= "Kilpikonna"
           (-> sanitized-xml
               (xml/get-content [:energiatodistus :perustiedot :nimi]))))
  (t/is (= "14.0"
           (-> sanitized-xml
               (xml/get-content [:energiatodistus
                                 :lahtotiedot
                                 :lahtotiedot-sis-kuormat
                                 :sis-kuorma
                                 1
                                 :valaistus])))))

(t/deftest simple-elements-test
  (t/is (nil? (xml/simple-elements nil)))
  (t/is (= [] (xml/simple-elements [])))
  (t/is (= (xml/element "foo" {} "bar") (xml/simple-elements ["foo" "bar"])))
  (t/is (= (xml/element "foo"
                        {}
                        (xml/element "bar" {} "baz")
                        (xml/element "x" {} "y"))
           (xml/simple-elements ["foo"
                                 ["bar" "baz"]
                                 ["x" "y"]]))))
