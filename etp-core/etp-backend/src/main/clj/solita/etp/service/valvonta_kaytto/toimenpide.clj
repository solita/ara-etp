(ns solita.etp.service.valvonta-kaytto.toimenpide
  (:require [solita.etp.db :as db]))

(db/require-queries 'valvonta-kaytto)

(def ^:private type-id->type-key
  {;; käytönvalvonnan asia avataan ashaan (case open)
   0  :case
   ;; tietopyynnön toimenpidetyypit
   1  :rfi-request
   2  :rfi-order
   3  :rfi-warning
   ;; päätös
   4  :decision-order
   ;; valvonnan sulkeminen (case closed)
   5  :closed
   ;; Uhkasakkoprosessi
   6  :court-hearing
   7  :decision-order-hearing-letter
   8  :decision-order-actual-decision
   9  :decision-order-notice-first-mailing
   10 :decision-order-notice-second-mailing
   11 :decision-order-notice-bailiff
   12 :decision-order-waiting-for-deadline
   14 :penalty-decision-hearing-letter
   15 :penalty-decision-actual-decision
   16 :penalty-decision-notice-first-mailing
   17 :penalty-decision-notice-second-mailing
   18 :penalty-decision-notice-bailiff
   19 :penalty-decision-waiting-for-deadline
   21 :penalty-list-delivery-in-progress})

(defn type-key [type-id]
  (if-let [type-key (type-id->type-key type-id)]
    type-key
    (throw (Exception.))))

(defn type? [type toimenpide]
  (= (-> toimenpide :type-id type-key) type))

(defn some-type? [type-key-set toimenpide]
  (contains? type-key-set (-> toimenpide :type-id type-key)))

(def case-open? (partial type? :case))
(def case-close? (partial type? :closed))
(def send-tiedoksi? (partial type? :rfi-request))

(def kaskypaatos-kuulemiskirje? (partial type? :decision-order-hearing-letter))

(def kaskypaatos-varsinainen-paatos? (partial type? :decision-order-actual-decision))

(def kaskypaatos-haastemies-tiedoksianto? (partial type? :decision-order-notice-bailiff))

(def sakkopaatos-haastemies-tiedoksianto? (partial type? :penalty-decision-notice-bailiff))

(def kaskypaatos-toimenpide?
  (partial some-type? #{:decision-order-hearing-letter
                        :decision-order-actual-decision
                        :decision-order-notice-first-mailing
                        :decision-order-notice-second-mailing
                        :decision-order-notice-bailiff
                        :decision-order-waiting-for-deadline}))

(def sakkopaatos-kuulemiskirje? (partial type? :penalty-decision-hearing-letter))

(def sakkopaatos-varsinainen-paatos? (partial type? :penalty-decision-actual-decision))

(def sakkopaatos-toimenpide?
  (partial some-type? #{:penalty-decision-hearing-letter
                        :penalty-decision-actual-decision
                        :penalty-decision-notice-first-mailing
                        :penalty-decision-notice-second-mailing
                        :penalty-decision-notice-bailiff
                        :penalty-decision-waiting-for-deadline}))

(def has-hallinto-oikeus-liite?
  (partial some-type? #{:decision-order-actual-decision
                        :penalty-decision-actual-decision
                        :penalty-decision-notice-bailiff}))

(def asha-toimenpide?
  (partial some-type? #{:rfi-request
                        :rfi-order
                        :rfi-warning
                        :decision-order-hearing-letter
                        :decision-order-actual-decision
                        :decision-order-notice-bailiff
                        :penalty-decision-hearing-letter
                        :penalty-decision-actual-decision
                        :penalty-decision-notice-bailiff}))


(def with-diaarinumero? (comp not (partial type? :case)))

(defn manually-deliverable? [db type-id]
  (let [manually-deliverable-toimenpidetypes
        (->> db
             valvonta-kaytto-db/select-manually-deliverable-toimenpidetypes
             (map :id)
             set)]
    (contains? manually-deliverable-toimenpidetypes type-id)))


(defn osapuoli-has-document? [osapuoli-specific-data]
  (boolean (:document osapuoli-specific-data)))

(defn recipient-answered? [osapuoli-specific-data]
  (boolean (:recipient-answered osapuoli-specific-data)))