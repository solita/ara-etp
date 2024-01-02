(ns solita.common.cf-signed-url
  (:require [clojure.string :as str]
            [solita.etp.service.json :as json])
  (:import
   (clojure.lang ExceptionInfo)
   (java.io FileInputStream InputStreamReader StringReader)
   (java.lang IllegalArgumentException NumberFormatException)
   (java.util Base64)
   (org.bouncycastle.asn1.pkcs PrivateKeyInfo)
   (org.bouncycastle.asn1.x509 SubjectPublicKeyInfo)
   (org.bouncycastle.crypto.digests SHA1Digest)
   (org.bouncycastle.crypto.signers RSADigestSigner)
   (org.bouncycastle.crypto.util PrivateKeyFactory PublicKeyFactory)
   (org.bouncycastle.openssl PEMKeyPair PEMParser)))

;; Namespace for producing a signed URL for CloudFront canned policy, as defined at
;; https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-creating-signed-url-canned-policy.html

(defn str->parsed-pem [s]
  (-> s
      StringReader.
      PEMParser.
      .readObject))

(defn- pem->private-key [parsed-pem]
  ;; Not sure why it is defined that way, but the type of object
  ;; returned by PEMParser.readObject seems to vary depending on quite
  ;; inconsequential things. For example, seems that PKCS1 formatted
  ;; private key is returned as PEMKeyPair, while PKCS8 formatted
  ;; private key produces just a PrivateKeyInfo object
  (cond
    (instance? PEMKeyPair parsed-pem)
    (-> parsed-pem
        .getPrivateKeyInfo
        PrivateKeyFactory/createKey)
    (instance? PrivateKeyInfo parsed-pem)
    (-> parsed-pem
        PrivateKeyFactory/createKey)
    :else
    (throw (ex-info "Unexpected private key object from decoded PEM" {}))))

(defn pem-string->private-key [pem-str]
  (-> pem-str
      str->parsed-pem
      pem->private-key))

(defn- pem->public-key [parsed-pem]
  (cond
    (instance? SubjectPublicKeyInfo parsed-pem)
    (-> parsed-pem
        PublicKeyFactory/createKey)
    :else
    (throw (ex-info "Unexpected public key object from decoded PEM" {}))))

(defn pem-string->public-key [pem-text]
  (-> pem-text
      str->parsed-pem
      pem->public-key))

(defn- bytes->querystring-safe-base64 [s]
  (let [encoder (Base64/getEncoder)]
    (-> (.encode encoder s)
        String.
        (.replace \+ \-)
        (.replace \= \_)
        (.replace \/ \~))))

(defn querystring-safe-base64->bytes [s]
  (let [decoder (Base64/getDecoder)
        base64 (-> s
                   (.replace \- \+)
                   (.replace \_ \=)
                   (.replace \~ \/))]
    (.decode decoder base64)))

(defn sign-document [document-bytes private-key]
  (let [signer (RSADigestSigner. (SHA1Digest.))]
    (.init signer true private-key)
    (.update signer document-bytes 0 (count document-bytes))
    (-> signer
        .generateSignature
        bytes->querystring-safe-base64)))

(defn verify-document-signature [document-bytes public-key signature]
  (let [signer (RSADigestSigner. (SHA1Digest.))
        signature-bytes (querystring-safe-base64->bytes signature)]
    (.init signer false public-key)
    (.update signer document-bytes 0 (count document-bytes))
    (.verifySignature signer signature-bytes)))

(defn unix-time []
  (quot (System/currentTimeMillis) 1000))

(defn policy-document [url ip-address expires]
  {:Statement [{:Resource url
                :Condition {:DateLessThan {:AWS:EpochTime expires}
                            :IpAddress {:AWS:SourceIp ip-address}}}]})

(defn url->signed-url [url expires ip-address {:keys [key-pair-id private-key]}]
  (let [policy-bytes (-> (policy-document url ip-address expires)
                         json/write-value-as-string
                         .getBytes)]
    (str url
         (if (.contains url "?")
           "&" "?")
         "Policy=" (bytes->querystring-safe-base64 policy-bytes)
         "&Signature=" (sign-document policy-bytes private-key)
         "&Key-Pair-Id=" key-pair-id)))

(defn trim-trailing-? [s]
  (if (.endsWith s "?")
    (.substring s 0 (dec (.length s)))
    s))

(defn url->base-url+sig-params [url]
  (let [index-of-policy (or (str/index-of url "?Policy=")
                            (str/index-of url "&Policy="))]
    [(-> (.substring url 0 index-of-policy)
         trim-trailing-?)
     (-> (.substring url (inc index-of-policy)))]))

(defn- query->map [query]
  (try
    (into {}
          (-> query
              (str/split #"&")
              (->> (filter (comp not empty?)))
              (->> (map #(str/split % #"=")))))
    (catch IllegalArgumentException e
      (throw (ex-info "bad argument list format" {:type :bad-argument-list-format})))))

(defn- signed-url->components [signed-url]
  (let [[base-url params-text] (url->base-url+sig-params signed-url)
        params (query->map params-text)]
    (when (-> params keys set (= #{"Policy" "Signature" "Key-Pair-Id"}) not)
      (throw (ex-info "Unexpected query parameters in signed URL"
                      {:type :extra-query-params})))
    {:base-url base-url
     :policy (get params "Policy")
     :signature (get params "Signature")
     :key-pair-id (-> params (get "Key-Pair-Id"))}))

(defn signed-url-problem [signed-url source-addr time {:keys [key-pair-id public-key]}]
  (try
    (let [{:keys [base-url policy signature] :as components} (signed-url->components signed-url)
          ;; Assuming no query parameters
          policy-bytes (querystring-safe-base64->bytes policy)
          policy-doc (json/read-value policy-bytes)
          policy-url (-> policy-doc (get-in [:Statement 0 :Resource]))
          policy-expires (-> policy-doc (get-in [:Statement 0 :Condition :DateLessThan :AWS:EpochTime]))
          policy-ip-address (-> policy-doc (get-in [:Statement 0 :Condition :IpAddress :AWS:SourceIp]))]
      (cond
        (-> policy-bytes
            (verify-document-signature public-key signature)
            not)
        :invalid-signature
        (-> components :key-pair-id (= key-pair-id) not)
        :invalid-key-pair-id
        (not (= source-addr policy-ip-address))
        :invalid-ip-address
        (not (= policy-url base-url))
        :invalid-url
        (< policy-expires time)
        :expired-url
        (not (= policy-doc (policy-document base-url policy-ip-address policy-expires)))
        :unsupported-policy-features))
    (catch ExceptionInfo e
      (case (-> e ex-data :type)
        :extra-query-params :format
        :bad-expire-stamp :format
        :bad-argument-list-format :format
        (throw e)))))

(defn signed-url-problem-now [signed-url source-addr {:keys [key-pair-id public-key] :as k}]
  (signed-url-problem signed-url source-addr (unix-time) k))
