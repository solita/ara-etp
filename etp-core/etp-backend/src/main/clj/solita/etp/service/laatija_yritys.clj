(ns solita.etp.service.laatija-yritys)

(def proposal? (comp #(= 0 %) :tila-id))
(def accepted? (comp #(= 1 %) :tila-id))
(def deleted? (comp #(= 2 %) :tila-id))