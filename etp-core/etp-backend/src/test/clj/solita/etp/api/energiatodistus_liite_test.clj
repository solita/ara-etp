(ns solita.etp.api.energiatodistus-liite-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-system :as ts])
  (:import (java.nio.charset StandardCharsets)))

(t/use-fixtures :each ts/fixture)

(defn- fix-multipart-charset
  "ring.mock.request/multipart-body sets Content-Type to
  \"multipart/form-data; charset=...; boundary=...\". muuntaja's naive
  content-type parser only looks at the first `;`-separated parameter and
  assumes it's the charset with nothing following it, so it ends up trying
  to negotiate the bogus charset \"iso-8859-1; boundary=...\" and fails.
  Dropping the charset parameter (boundary is all reitit's multipart
  middleware needs) avoids that and lets muuntaja fall back to its default."
  [request]
  (let [strip-charset #(string/replace % #";\s*charset=[^;]*" "")]
    (-> request
        (update :content-type strip-charset)
        (update-in [:headers "content-type"] strip-charset))))

(defn- post-liite-file [energiatodistus-id filename]
  (-> (mock/request :post (str "/api/private/energiatodistukset/2013/"
                             energiatodistus-id "/liitteet/files"))
    (kayttaja-test-data/with-virtu-user)
    (mock/header "Accept" "application/json")
    (mock/multipart-body
                  {:files {:value        (io/file "deps.edn")
                           :filename     filename
                           :content-type "application/octet-stream"}})
    (fix-multipart-charset)))

(defn- get-liitteet [energiatodistus-id]
  (ts/handler (-> (mock/request :get (str "/api/private/energiatodistukset/2013/"
                                          energiatodistus-id "/liitteet"))
                  (kayttaja-test-data/with-virtu-user)
                  (mock/header "Accept" "application/json"))))

(defn- setup-energiatodistus-visible-to-paakayttaja! []
  (kayttaja-test-data/insert-virtu-paakayttaja!)
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)
        ;; POST /liitteet/files requires paakayttaja access, and paakayttaja
        ;; can only see draft energiatodistukset marked visible to them.
        add (-> (energiatodistus-test-data/generate-add 2013 true)
                (assoc :draft-visible-to-paakayttaja true))]
    {:energiatodistus-id (first (energiatodistus-test-data/insert! [add] laatija-id))
     :laatija-id laatija-id}))

(t/deftest add-liite-with-empty-filename-test
  (let [{:keys [energiatodistus-id]} (setup-energiatodistus-visible-to-paakayttaja!)
        post-response (ts/handler (post-liite-file energiatodistus-id ""))]
    (t/is (= 201 (:status post-response))
          "Attachment upload is accepted even with an empty filename")
    (let [liitteet (-> (get-liitteet energiatodistus-id)
                       :body
                       (j/read-value j/keyword-keys-object-mapper))]
      (t/is (some #(= "" (:nimi %)) liitteet)
            "Empty filename is stored as-is"))))

(t/deftest add-liite-rejects-executable-test
  (let [{:keys [energiatodistus-id]} (setup-energiatodistus-visible-to-paakayttaja!)
        executable-file (doto (java.io.File/createTempFile "liite-api-test" ".exe")
                          .deleteOnExit)
        _ (io/copy (byte-array (map unchecked-byte [0x4D 0x5A 0x00 0x00]))
                    executable-file)
        post-request (-> (mock/request :post (str "/api/private/energiatodistukset/2013/"
                                                energiatodistus-id "/liitteet/files"))
                        (kayttaja-test-data/with-virtu-user)
                        (mock/header "Accept" "application/json")
                        (mock/multipart-body
                         {:files {:value        executable-file
                                  :filename     "virus.exe"
                                  :content-type "application/octet-stream"}})
                        (fix-multipart-charset))
        post-response (ts/handler post-request)]
    (t/is (= 400 (:status post-response))
          "Executable attachment upload is rejected with 400 Bad Request")
    (let [liitteet (-> (get-liitteet energiatodistus-id)
                       :body
                       (j/read-value j/keyword-keys-object-mapper))]
      (t/is (empty? liitteet)
            "No liite was persisted for the rejected executable"))))

(defn bais->str
  "Reads all bytes from a ByteArrayInputStream, decodes as UTF-8,
   resets the stream, and returns the string."
  [^java.io.ByteArrayInputStream bais]
  (.reset bais)
  (let [bytes (.readAllBytes bais)]
    (.reset bais)
    (String. bytes StandardCharsets/UTF_8)))

(t/deftest add-liite-with-quote-in-filename-test
  (let [{:keys [energiatodistus-id laatija-id]} (setup-energiatodistus-visible-to-paakayttaja!)
        filename "quote\"file.txt"
        escaped-filename "quote\\\"file.txt"
        post-request (post-liite-file energiatodistus-id filename)
        post-response (ts/handler post-request)]
    (t/is (= 201 (:status post-response))
          "Attachment upload is accepted even with a quote in the filename")

    (t/is (string/includes?
            (-> post-request :body bais->str)
            escaped-filename)
          "Expecting the quote to be escaped in the multipart request")

    (let [liitteet-in-db (liite-service/find-energiatodistus-liitteet ts/*db*
                                                                      {:id laatija-id :rooli 0}
                                                                      energiatodistus-id)]
      (t/is (= filename (-> liitteet-in-db first :nimi))
            "Expecting the database to have filename in original form"))

    (let [liitteet (-> (get-liitteet energiatodistus-id)
                       :body
                       (j/read-value j/keyword-keys-object-mapper))]
      (t/is (some #(= filename (:nimi %)) liitteet)
            "Expecting deserialized filename to not be escaped"))))

