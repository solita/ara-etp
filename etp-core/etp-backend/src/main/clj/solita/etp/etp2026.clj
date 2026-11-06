(ns solita.etp.etp2026)

(defn implement-2026-via-2018 [versio _why]
  "Treat version 2018 as 2027 as an interim solution during
  development of ETP 2026"
  (if (= versio 2027)
    2018
    versio))
