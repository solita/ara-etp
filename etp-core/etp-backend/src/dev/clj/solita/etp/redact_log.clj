(ns solita.etp.redact-log
  (:require
    [clojure.string :as str]))

(def ^:private redactions
  [["(?i)(:henkilötunnus|:henkilotunnus)(\\s+)[^,}\\s]+"
    "$1$2[REDACTED_HENKILOTUNNUS]"]
   ["(?i)(:username\\s+)[^,}\\s]+"
    "$1[REDACTED_USERNAME]"]
   ["(?i)(:password\\s*)[^,}\\s]*"
    "$1[REDACTED_PASSWORD]"]
   ["(?i)(:id\\s*)[^,}\\s]+(?=,\\s*:password)"
    "$1[REDACTED_AUTH_ID]"]
   ["(?i)(laatija-allekirjoitus-id:\\s*)[0-9a-f-]{36}"
    "$1[REDACTED_SIGNATURE_ID]"]
   ["(?i)(https?://[^\\s?]+\\?)[^\\s]+"
    "$1[REDACTED_URL_QUERY]"]
   ["(?i)\\bC-[0-9a-f]{32,}\\b"
    "[REDACTED_CERTIFICATE_ID]"]
   ["\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b"
    "[REDACTED_UUID]"]])

(defn redact-line [line]
  (reduce (fn [redacted [pattern replacement]]
            (str/replace redacted (re-pattern pattern) replacement))
          line
          redactions))

(defn -main [& args]
  (when (seq args)
    (binding [*err* *out*]
      (println "Usage: cat production.log | clj -M:dev -m solita.etp.redact-log"))
    (System/exit 1))
  (->> (line-seq (java.io.BufferedReader. *in*))
       (map redact-line)
       (run! println)))
