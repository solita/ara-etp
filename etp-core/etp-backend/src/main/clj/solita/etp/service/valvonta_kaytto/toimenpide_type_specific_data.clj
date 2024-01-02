(ns solita.etp.service.valvonta-kaytto.toimenpide-type-specific-data
  (:require [clojure.string :as string]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]))

(db/require-queries 'hallinto-oikeus)
(db/require-queries 'karajaoikeus)
(db/require-queries 'valvonta-kaytto)

(defmulti format-type-specific-data
          (fn [_db toimenpide _osapuoli] (-> toimenpide :type-id toimenpide/type-key)))

(defn- find-value-from-osapuoli-specific-data [key osapuoli-specific-data osapuoli]
  (->> osapuoli-specific-data
       (filter #(= (:osapuoli %)
                   osapuoli))
       first
       key))

(def find-administrative-court-id-from-osapuoli-specific-data
  (partial find-value-from-osapuoli-specific-data :hallinto-oikeus-id))

(def find-recipient-answered-from-osapuoli-specific-data
  (partial find-value-from-osapuoli-specific-data :recipient-answered))

(defn hallinto-oikeus-id->formatted-strings [db hallinto-oikeus-id]
  (if-let [formatted-strings (first (hallinto-oikeus-db/find-document-template-wording-by-hallinto-oikeus-id db {:hallinto-oikeus-id hallinto-oikeus-id}))]
    formatted-strings
    (exception/throw-ex-info!
      {:message (str "Unknown hallinto-oikeus-id: " hallinto-oikeus-id)})))

(defn hallinto-oikeus-id-exists-for-osapuoli? [toimenpide osapuoli]
  (boolean
    (find-administrative-court-id-from-osapuoli-specific-data
      (-> toimenpide
          :type-specific-data
          :osapuoli-specific-data)
      {:id   (:id osapuoli)
       :type (osapuoli/osapuoli->osapuoli-type osapuoli)})))

(defn format-actual-decision-data [db toimenpide osapuoli]
  (let [recipient-answered? (-> toimenpide
                                :type-specific-data
                                :osapuoli-specific-data
                                (find-recipient-answered-from-osapuoli-specific-data osapuoli))
        hallinto-oikeus-strings (hallinto-oikeus-id->formatted-strings
                                  db
                                  (-> toimenpide
                                      :type-specific-data
                                      :osapuoli-specific-data
                                      (find-administrative-court-id-from-osapuoli-specific-data osapuoli)))]
    {:recipient-answered       recipient-answered?
     :vastaus-fi               (let [answer-commentary (-> toimenpide
                                                           :type-specific-data
                                                           :osapuoli-specific-data
                                                           ((partial find-value-from-osapuoli-specific-data :answer-commentary-fi) osapuoli))
                                     recipient-answered-string (if recipient-answered?
                                                                 "Asianosainen antoi vastineen kuulemiskirjeeseen. "
                                                                 "Asianosainen ei vastannut kuulemiskirjeeseen. ")]
                                 (str recipient-answered-string answer-commentary))
     :vastaus-sv               (let [answer-commentary (-> toimenpide
                                                           :type-specific-data
                                                           :osapuoli-specific-data
                                                           ((partial find-value-from-osapuoli-specific-data :answer-commentary-sv) osapuoli))
                                     recipient-answered-string (if recipient-answered?
                                                                 "gav ett bemötande till brevet om hörande. "
                                                                 "svarade inte på brevet om hörande. ")]
                                 (str recipient-answered-string answer-commentary))
     :oikeus-fi                (:fi hallinto-oikeus-strings)
     :oikeus-sv                (:sv hallinto-oikeus-strings)
     :fine                     (-> toimenpide :type-specific-data :fine)
     :statement-fi             (-> toimenpide
                                   :type-specific-data
                                   :osapuoli-specific-data
                                   ((partial find-value-from-osapuoli-specific-data :statement-fi) osapuoli))
     :statement-sv             (-> toimenpide
                                   :type-specific-data
                                   :osapuoli-specific-data
                                   ((partial find-value-from-osapuoli-specific-data :statement-sv) osapuoli))
     :department-head-name     (-> toimenpide :type-specific-data :department-head-name)
     :department-head-title-fi (-> toimenpide :type-specific-data :department-head-title-fi)
     :department-head-title-sv (-> toimenpide :type-specific-data :department-head-title-sv)}))

(defmethod format-type-specific-data :decision-order-actual-decision [db toimenpide osapuoli]
  (format-actual-decision-data db toimenpide osapuoli))

(defmethod format-type-specific-data :penalty-decision-actual-decision [db toimenpide osapuoli]
  (format-actual-decision-data db toimenpide osapuoli))

(defn- karajaoikeus-id->name [db id]
  (first (karajaoikeus-db/find-karajaoikeus-name-by-id db {:karajaoikeus-id id})))

(defn- format-notice-bailiff [db toimenpide osapuoli]
  (let [karajaoikeus-id (-> toimenpide
                            :type-specific-data
                            :osapuoli-specific-data
                            ((partial find-value-from-osapuoli-specific-data :karajaoikeus-id) osapuoli))
        haastemies-email (-> toimenpide
                             :type-specific-data
                             :osapuoli-specific-data
                             ((partial find-value-from-osapuoli-specific-data :haastemies-email) osapuoli))]
    {:karajaoikeus     (karajaoikeus-id->name db karajaoikeus-id)
     :haastemies-email haastemies-email}))

(defmethod format-type-specific-data :decision-order-notice-bailiff [db toimenpide osapuoli]
  (format-notice-bailiff db toimenpide osapuoli))

(defmethod format-type-specific-data :penalty-decision-notice-bailiff [db toimenpide osapuoli]
  (format-notice-bailiff db toimenpide osapuoli))

(defmethod format-type-specific-data :decision-order-hearing-letter [db toimenpide _osapuoli]
  (let [henkilo-osapuolet (valvonta-kaytto-db/select-henkilot db {:valvonta-id (:valvonta-id toimenpide)})
        yritys-osapuolet (valvonta-kaytto-db/select-yritykset db {:valvonta-id (:valvonta-id toimenpide)})
        omistajat (filter osapuoli/omistaja? (concat henkilo-osapuolet yritys-osapuolet))
        omistaja-strings (map (fn [omistaja]
                                (if (osapuoli/henkilo? omistaja)
                                  (str (:sukunimi omistaja) " " (:etunimi omistaja))
                                  (:nimi omistaja)))
                              omistajat)]
    {:fine      (-> toimenpide :type-specific-data :fine)
     :omistajat (string/join ", " omistaja-strings)}))

(defmethod format-type-specific-data :default [_ toimenpide _]
  (:type-specific-data toimenpide))
