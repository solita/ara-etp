(ns solita.etp.api.energiatodistus-liite-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-system :as ts]))

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

(defn- post-liite-file! [energiatodistus-id filename]
  (ts/handler (-> (mock/request :post (str "/api/private/energiatodistukset/2013/"
                                            energiatodistus-id "/liitteet/files"))
                  (kayttaja-test-data/with-virtu-user)
                  (mock/header "Accept" "application/json")
                  (mock/multipart-body
                    {:files {:value        (io/file "deps.edn")
                             :filename     filename
                             :content-type "application/octet-stream"}})
                  (fix-multipart-charset))))

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
    (first (energiatodistus-test-data/insert! [add] laatija-id))))

(t/deftest add-liite-with-empty-filename-test
  (let [energiatodistus-id (setup-energiatodistus-visible-to-paakayttaja!)
        post-response (post-liite-file! energiatodistus-id "")]
    (t/is (= 201 (:status post-response))
          "Attachment upload is accepted even with an empty filename")
    (let [liitteet (-> (get-liitteet energiatodistus-id)
                       :body
                       (j/read-value j/keyword-keys-object-mapper))]
      (t/is (some #(= "" (:nimi %)) liitteet)
            "Empty filename is stored as-is"))))

(t/deftest add-liite-with-quote-in-filename-test
  (let [energiatodistus-id (setup-energiatodistus-visible-to-paakayttaja!)
        filename "quote\"file.txt"
        ;; The multipart encoder backslash-escapes the embedded quote in the
        ;; Content-Disposition header (`filename="quote\"file.txt"`), and
        ;; ring's multipart-params parsing does not undo that escaping, so
        ;; the backslash ends up baked into the stored nimi. This is not
        ;; app-level sanitization - it's just how the raw header value comes
        ;; through - and it demonstrates that a quote character isn't
        ;; rejected or stripped anywhere along the way.
        expected-stored-filename "quote\\\"file.txt"
        post-response (post-liite-file! energiatodistus-id filename)]
    (t/is (= 201 (:status post-response))
          "Attachment upload is accepted even with a quote in the filename")
    (let [liitteet (-> (get-liitteet energiatodistus-id)
                       :body
                       (j/read-value j/keyword-keys-object-mapper))]
      (t/is (some #(= expected-stored-filename (:nimi %)) liitteet)
            "Filename containing a quote passes through without being rejected"))))

