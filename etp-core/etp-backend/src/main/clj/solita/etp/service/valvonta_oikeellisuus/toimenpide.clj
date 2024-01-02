(ns solita.etp.service.valvonta-oikeellisuus.toimenpide)

(def ^:private type-keys
  [;; kevyt valvontamenettely
   :verified :anomaly

   ;; raskas valvontamenettely
   ;; asia avataan ashaan (case open)
   :case
   ;; tietopyynnön toimenpidetyypit
   :rfi-request :rfi-reply :rfi-order :rfi-warning
   ;; valvonnan toimenpidetyypit
   :audit-report :audit-reply :audit-order :audit-warning
   ;; lisäselvityspyynnön toimenpidetyypit
   :rfc-request :rfc-reply
   ;; päätös
   :decision-prohibition
   ;; raskaan valvonnan sulkeminen (case closed)
   :closed])

(defn type-key [type-id] (nth type-keys type-id))

(defn type? [type toimenpide]
  (= (-> toimenpide :type-id type-key) type))

(defn some-type? [type-key-set toimenpide]
  (contains? type-key-set (-> toimenpide :type-id type-key)))

(def draft-support?
  (partial some-type? #{:rfi-reply :audit-report :audit-reply :rfc-reply}))

(def case-open? (partial type? :case))
(def case-closed? (partial type? :closed))
(def anomaly? (partial type? :anomaly))

(def asha-toimenpide?
  (partial some-type?
           #{:rfi-request :rfi-order :rfi-warning
             :audit-report :audit-order :audit-warning
             :rfc-request}))

(def published? #(-> % :publish-time some?))
(def draft? (complement published?))

(def audit-report? (partial type? :audit-report))

(def audit-reply? (partial type? :audit-reply))

(def reply?
  (partial some-type? #{:rfi-reply :audit-reply :rfc-reply}))

(def close-valvonta?
  (partial some-type? #{:verified :anomaly :closed}))
