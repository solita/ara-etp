(ns solita.etp.service.energiatodistus-tila)

(def ^:private tilat [:draft :in-signing :signed :discarded :replaced :deleted])

(defn tila-key [tila-id] (nth tilat tila-id))

(defn- in-tila? [tila-id energiatodistus]
  (= (:tila-id energiatodistus) tila-id))

(def draft? (partial in-tila? 0))
(def in-signing? (partial in-tila? 1))
(def signed? (partial in-tila? 2))
(def discarded? (partial in-tila? 3))
(def replaced? (partial in-tila? 4))
(def deleted? (partial in-tila? 5))
