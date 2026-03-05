(ns solita.etp.api.external-response
  "Transforms internal Clojure error data into user-friendly JSON responses
   for external API consumers."
  (:require [clojure.tools.logging :as log])
  (:import (clojure.lang ExceptionInfo)))

(defn- format-invalid-value
  "Formats an :invalid-value error into the external response shape.
   The :property field is already an API path string from to-property-name."
  [error]
  {:error   "invalid-value"
   :message "Kentän arvo on sallitun alueen ulkopuolella."
   :details [{:field   (str (:property error))
              :error   "out-of-range"
              :message (str "Arvo " (:value error)
                            " ei ole sallitulla välillä [" (:min error) ", " (:max error) "].")}]})

(defn- format-foreign-key-violation
  "Formats a :foreign-key-violation error."
  [error]
  {:error   "foreign-key-violation"
   :message "Viitattu entiteetti ei ole olemassa."
   :details [{:field   (or (:constraint error) "tuntematon")
              :error   "invalid-reference"
              :message (str "Viiteavainrajoite '" (:constraint error)
                            "' rikkoutui. Tarkista, että kaikki viitatut tunnisteet ovat olemassa.")}]})

(defn- format-invalid-sisainen-kuorma
  "Formats an :invalid-sisainen-kuorma error."
  [error]
  {:error   "invalid-sisainen-kuorma"
   :message "Sisäisen kuorman arvot ovat kiinteitä valitulle käyttötarkoitukselle."
   :details [{:field   "lahtotiedot.sis-kuorma"
              :error   "fixed-values"
              :message (str "Arvojen tulee vastata: " (:valid-kuorma error))}]})

(defn- format-invalid-replace
  "Formats an :invalid-replace error."
  [error]
  {:error   "invalid-replace"
   :message (or (:message error) "Energiatodistusta ei voida korvata.")
   :details []})

(defn- format-invalid-laskutusosoite
  "Formats an :invalid-laskutusosoite error."
  [error]
  {:error   "invalid-laskutusosoite"
   :message (or (:message error) "Virheellinen laskutusosoite.")
   :details []})

(defn- format-missing-value
  "Formats a :missing-value error."
  [error]
  {:error   "missing-value"
   :message "Pakolliset kentät puuttuvat."
   :details (mapv (fn [prop]
                    {:field   (str prop)
                     :error   "required"
                     :message (str "Kenttä '" prop "' on pakollinen.")})
                  (:missing error))})

(defn- format-forbidden
  "Formats a :forbidden error."
  [error]
  {:error   "forbidden"
   :message (or (:reason error) "Pääsy kielletty.")
   :details []})

(defn- format-not-found
  "Formats entity-not-found errors."
  [error]
  {:error   (name (:type error))
   :message (or (:message error) "Entiteettiä ei löytynyt.")
   :details []})

(defn- format-generic-error
  "Formats any other error type into the external response shape."
  [error]
  {:error   (if (keyword? (:type error))
              (name (:type error))
              (str (:type error)))
   :message (or (:message error) "Tapahtui virhe.")
   :details []})

(defn- format-error
  "Dispatches formatting based on error type."
  [error]
  (case (:type error)
    :invalid-value         (format-invalid-value error)
    :foreign-key-violation (format-foreign-key-violation error)
    :invalid-sisainen-kuorma (format-invalid-sisainen-kuorma error)
    :invalid-replace       (format-invalid-replace error)
    :invalid-laskutusosoite (format-invalid-laskutusosoite error)
    :missing-value         (format-missing-value error)
    :forbidden             (format-forbidden error)
    :invalid-energiatodistus-versio (format-not-found error)
    :invalid-energiatodistus-id     (format-not-found error)
    :energiatodistus-not-found      (format-not-found error)
    (format-generic-error error)))

(defn- matches-description?
  "Checks if error ex-data matches an error description (ignoring :response key)."
  [error error-description]
  (let [matcher (dissoc error-description :response)
        matched-error (select-keys error (keys matcher))]
    (= matcher matched-error)))

(defn with-external-exceptions
  "Like api-response/with-exceptions but transforms the error body into
   a user-friendly JSON structure for external API consumers."
  [response-fn error-descriptions]
  (log/info "with-external-exceptions called")
  (try
    (response-fn)
    (catch ExceptionInfo e
      (let [error (ex-data e)
            _ (log/info "Caught ExceptionInfo:" (:type error) error)
            description (first (filter (partial matches-description? error)
                                       error-descriptions))]
        (log/info "Matched description:" description)
        (if (nil? description)
          (do
            (log/info "No matching description, re-throwing")
            (throw e))
          (let [formatted (format-error error)]
            (log/info "Formatted response:" formatted)
            {:status (:response description)
             :body   formatted}))))))
