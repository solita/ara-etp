(ns solita.etp.service.valvonta-kaytto.asha
  (:require [clojure.string :as string]
            [solita.common.time :as time]
            [solita.etp.service.asha :as asha]
            [solita.etp.schema.valvonta-kaytto :as vk-schema]
            [solita.etp.service.valvonta-kaytto.hallinto-oikeus-attachment :as hao-attachment]
            [solita.etp.service.valvonta-kaytto.previous-toimenpide-data :as previous-toimenpide]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]
            [solita.etp.service.valvonta-kaytto.toimenpide-type-specific-data :as type-specific-data]
            [solita.etp.service.valvonta-kaytto.template :as template]
            [solita.etp.service.valvonta-kaytto.store :as store]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [solita.etp.service.pdf :as pdf]
            [solita.etp.db :as db]
            [solita.common.formats :as formats])
  (:import (java.time Instant)))

(db/require-queries 'valvonta-kaytto)
(db/require-queries 'geo)

(defn toimenpide-type->document [type-id]
  (let [type-key (toimenpide/type-key type-id)
        documents {:rfi-request                      {:type "Pyyntö" :filename "tietopyynto.pdf"}
                   :rfi-order                        {:type "Kirje" :filename "kehotus.pdf"}
                   :rfi-warning                      {:type "Kirje" :filename "varoitus.pdf"}
                   :decision-order-hearing-letter    {:type     "Kirje"
                                                      :filename "kuulemiskirje.pdf"}
                   :decision-order-actual-decision   {:type     "Kirje"
                                                      :filename "kaskypaatos.pdf"}
                   :decision-order-notice-bailiff    {:type     "Kirje"
                                                      :filename "haastemies-tiedoksianto.pdf"}
                   :penalty-decision-hearing-letter  {:type     "Kirje"
                                                      :filename "sakkopaatos-kuulemiskirje.pdf"}
                   :penalty-decision-actual-decision {:type     "Kirje"
                                                      :filename "sakkopaatos.pdf"}
                   :penalty-decision-notice-bailiff  {:type     "Kirje"
                                                      :filename "haastemies-tiedoksianto.pdf"}}]
    (get documents type-key)))

(defn toimenpide-type->attachment [type-id]
  (let [type-key (toimenpide/type-key type-id)
        attachments {:decision-order-actual-decision   {:type     "Kirje"
                                                        :filename "hallinto-oikeus.pdf"}
                     :penalty-decision-actual-decision {:type     "Kirje"
                                                        :filename "hallinto-oikeus.pdf"}
                     :penalty-decision-notice-bailiff  {:type     "Kirje"
                                                        :filename "hallinto-oikeus.pdf"}}]
    (get attachments type-key)))

(defn find-kaytto-valvonta-documents [db valvonta-id]
  (->> (valvonta-kaytto-db/select-valvonta-documents db {:valvonta-id valvonta-id})
       (map (fn [toimenpide]
              (let [type (toimenpide/type-key (:type-id toimenpide))]
                {type (:publish-time toimenpide)})))
       (into {})))

(defn- find-ilmoituspaikka [ilmoituspaikat valvonta]
  (if (osapuoli/ilmoituspaikka-other? valvonta)
    (:ilmoituspaikka-description valvonta)
    (->> ilmoituspaikat (filter #(= (:id %) (:ilmoituspaikka-id valvonta))) first :label-fi)))

(defn- find-postitoimipaikka [db postinumero]
  (-> (geo-db/select-postinumero-by-id db {:id (formats/string->int postinumero)}) first :label-fi))

(defn- template-optional [value] (if (some? value) [value] []))

(defn- tiedoksi-saaja [roolit osapuoli]
  (let [rooli (first (filter #(= (:id %) (:rooli-id osapuoli)) roolit))]
    {:nimi  (or (:nimi osapuoli) (str (:etunimi osapuoli) " " (:sukunimi osapuoli)))
     :rooli (template-optional
              (if (osapuoli/other-rooli? osapuoli)
                (:rooli-description osapuoli)
                (when (some? rooli) (str (:label-fi rooli) "/" (:label-sv rooli)))))
     :email (template-optional (:email osapuoli))}))

(defn- template-data [db whoami valvonta toimenpide osapuoli dokumentit ilmoituspaikat tiedoksi roolit]
  {:päivä                  (time/today)
   :määräpäivä             (time/format-date (:deadline-date toimenpide))
   :diaarinumero           (:diaarinumero toimenpide)
   :valvoja                (select-keys whoami [:etunimi :sukunimi :email :puhelin])
   :omistaja-henkilo       (when (osapuoli/henkilo? osapuoli)
                             (-> (select-keys osapuoli [:etunimi :sukunimi :jakeluosoite :postinumero :postitoimipaikka :henkilotunnus])
                                 (update-in [:henkilotunnus] (fn [henkilotunnus] (when henkilotunnus (string/replace henkilotunnus "-" "‑")))))) ; Replace hyphen with non-breaking variant
   :omistaja-yritys        (when (osapuoli/yritys? osapuoli)
                             (select-keys osapuoli [:nimi :jakeluosoite :postinumero :postitoimipaikka :vastaanottajan-tarkenne]))
   :kohde                  {:katuosoite       (:katuosoite valvonta)
                            :postinumero      (:postinumero valvonta)
                            :postitoimipaikka (find-postitoimipaikka db (:postinumero valvonta))
                            :ilmoituspaikka   (find-ilmoituspaikka ilmoituspaikat valvonta)
                            :ilmoitustunnus   (:ilmoitustunnus valvonta)
                            :havaintopäivä    (-> valvonta :havaintopaiva time/format-date)}
   :tietopyynto            {:tietopyynto-pvm         (time/format-date (:rfi-request dokumentit))
                            :tietopyynto-kehotus-pvm (time/format-date (:rfi-order dokumentit))}
   :tiedoksi               (map (partial tiedoksi-saaja roolit) tiedoksi)
   :tyyppikohtaiset-tiedot (type-specific-data/format-type-specific-data
                             db
                             toimenpide
                             {:id   (:id osapuoli)
                              :type (osapuoli/osapuoli->osapuoli-type osapuoli)})
   :aiemmat-toimenpiteet   (previous-toimenpide/formatted-previous-toimenpide-data db toimenpide (:id valvonta))})

(defn- request-id [valvonta-id toimenpide-id]
  (str valvonta-id "/" toimenpide-id))

(defn- osapuoli->contact [osapuoli]
  (cond
    (osapuoli/henkilo? osapuoli)
    {:type          "PERSON"
     :first-name    (:etunimi osapuoli)
     :last-name     (:sukunimi osapuoli)
     :phone-number  (:puhelin osapuoli)
     :email-address (:email osapuoli)}
    (osapuoli/yritys? osapuoli)
    {:type                "ORGANIZATION"
     :organizational-name (:nimi osapuoli)
     :phone-number        (:puhelin osapuoli)
     :email-address       (:email osapuoli)}))

(defn- available-processing-actions [toimenpide osapuolet]
  {:rfi-request                      {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Vireillepano"}}
                                      :processing-action {:name                 "Tietopyyntö"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}
                                      :document          (toimenpide-type->document (:type-id toimenpide))}
   :rfi-order                        {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Vireillepano"}}
                                      :processing-action {:name                 "Kehotuksen antaminen"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}
                                      :document          (toimenpide-type->document (:type-id toimenpide))}
   :rfi-warning                      {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Käsittely"}}
                                      :processing-action {:name                 "Varoituksen antaminen"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}
                                      :document          (toimenpide-type->document (:type-id toimenpide))}
   :decision-order-hearing-letter    {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Käsittely"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :processing-action {:name                 "Kuuleminen uhkasakon asettamisesta"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}
   :decision-order-actual-decision   {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Päätöksenteko"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :attachment        (toimenpide-type->attachment (:type-id toimenpide))
                                      :processing-action {:name                 "Uhkasakon asettaminen"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}
   :decision-order-notice-bailiff    {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Tiedoksianto ja toimeenpano"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :processing-action {:name                 "Asiakirjan toimituspyyntö haastemiehelle"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}
   :penalty-decision-hearing-letter  {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Käsittely"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :processing-action {:name                 "Kuuleminen uhkasakon tuomitsemisesta"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}
   :penalty-decision-actual-decision {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Päätöksenteko"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :attachment        (toimenpide-type->attachment (:type-id toimenpide))
                                      :processing-action {:name                 "Uhkasakon tuomitseminen maksettavaksi"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}
   :penalty-decision-notice-bailiff  {:identity          {:case              {:number (:diaarinumero toimenpide)}
                                                          :processing-action {:name-identity "Tiedoksianto ja toimeenpano"}}
                                      :document          (toimenpide-type->document (:type-id toimenpide))
                                      :attachment        (toimenpide-type->attachment (:type-id toimenpide))
                                      :processing-action {:name                 "Asiakirjan toimituspyyntö haastemiehelle"
                                                          :reception-date       (Instant/now)
                                                          :contacting-direction "SENT"
                                                          :contact              (map osapuoli->contact osapuolet)}}})

(defn- resolve-processing-action [toimenpide osapuolet]
  (let [processing-actions (available-processing-actions toimenpide osapuolet)
        type-key (toimenpide/type-key (:type-id toimenpide))]
    (get processing-actions type-key)))

(defn open-case! [db whoami valvonta osapuolet ilmoituspaikat]
  (asha/open-case! {:request-id     (request-id (:id valvonta) 1)
                    :sender-id      (:email whoami)
                    :classification "05.03.01"
                    :service        "general"               ; Yleinen menettely
                    :name           (asha/string-join ", " [(:rakennustunnus valvonta)
                                                            (:katuosoite valvonta)
                                                            (asha/string-join " " [(:postinumero valvonta)
                                                                                   (find-postitoimipaikka db (:postinumero valvonta))])])

                    :description    (asha/string-join "\r" [(find-ilmoituspaikka ilmoituspaikat valvonta)
                                                            (:ilmoitustunnus valvonta)])
                    :attach         {:contact (map osapuoli->contact osapuolet)}}))

(defn generate-pdf-document
  [db whoami valvonta toimenpide ilmoituspaikat osapuoli osapuolet roolit]
  (let [template-id (:template-id toimenpide)
        template (-> (valvonta-kaytto-db/select-template db {:id template-id}) first)
        documents (find-kaytto-valvonta-documents db (:id valvonta))
        tiedoksi (if (template/send-tiedoksi? template) (filter osapuoli/tiedoksi? osapuolet) [])
        template-data (template-data db whoami valvonta toimenpide
                                     osapuoli documents ilmoituspaikat
                                     tiedoksi roolit)]
    (pdf/generate-pdf->bytes {:template (:content template)
                              :data     template-data})))

(defn remove-osapuolet-with-no-document
  "If toimenpidetype of the toimenpide is such that the document might not be created for some,
  osapuolet will be filtered so that only those are returned that have a :document as true
  specified in type-specific-data of the toimenpide.
  For all other toimenpidetypes all osapuolet are returned."
  [toimenpide osapuolet]
  (if ((some-fn toimenpide/kaskypaatos-varsinainen-paatos?
                toimenpide/kaskypaatos-haastemies-tiedoksianto?
                toimenpide/sakkopaatos-kuulemiskirje?
                toimenpide/sakkopaatos-varsinainen-paatos?
                toimenpide/sakkopaatos-haastemies-tiedoksianto?) toimenpide)
    (let [osapuolet-with-document (->> toimenpide
                                       :type-specific-data
                                       :osapuoli-specific-data
                                       (filter toimenpide/osapuoli-has-document?)
                                       (map :osapuoli))
          henkilo-osapuolet-with-documents (map :id (filter #(= (:type %) vk-schema/henkilo) osapuolet-with-document))
          yritys-osapuolet-with-documents (map :id (filter #(= (:type %) vk-schema/yritys) osapuolet-with-document))]
      (concat
        (filter #(contains? (set henkilo-osapuolet-with-documents) (:id %)) (filter osapuoli/henkilo? osapuolet))
        (filter #(contains? (set yritys-osapuolet-with-documents) (:id %)) (filter osapuoli/yritys? osapuolet))))
    osapuolet))

(defn store-hallinto-oikeus-attachment! [db aws-s3-client valvonta-id toimenpide osapuoli]
  (let [osapuoli-type (osapuoli/osapuoli->osapuoli-type osapuoli)
        hallinto-oikeus-id (-> toimenpide
                               :type-specific-data
                               :osapuoli-specific-data
                               (type-specific-data/find-administrative-court-id-from-osapuoli-specific-data {:id   (:id osapuoli)
                                                                                                             :type osapuoli-type}))
        attachment (hao-attachment/attachment-for-hallinto-oikeus-id db hallinto-oikeus-id)]
    (store/store-hallinto-oikeus-attachment! aws-s3-client valvonta-id (:id toimenpide) osapuoli attachment)
    attachment))

(defn log-toimenpide! [db aws-s3-client whoami valvonta toimenpide osapuolet ilmoituspaikat roolit]
  (let [request-id (request-id (:id valvonta) (:id toimenpide))
        sender-id (:email whoami)
        case-number (:diaarinumero toimenpide)
        processing-action (resolve-processing-action toimenpide osapuolet)
        documents (when (:document processing-action)
                    (->> osapuolet
                         (filter osapuoli/omistaja?)
                         (remove-osapuolet-with-no-document toimenpide)
                         (map (fn [osapuoli]
                                (let [document (generate-pdf-document db whoami valvonta toimenpide ilmoituspaikat
                                                                      osapuoli osapuolet roolit)]
                                  (store/store-document! aws-s3-client (:id valvonta) (:id toimenpide) osapuoli document)
                                  document)))))
        attachments (when (toimenpide/has-hallinto-oikeus-liite? toimenpide)
                      (->> osapuolet
                           (filter osapuoli/omistaja?)
                           (remove-osapuolet-with-no-document toimenpide)
                           (mapv (fn [osapuoli]
                                   (when (type-specific-data/hallinto-oikeus-id-exists-for-osapuoli? toimenpide osapuoli)
                                     (store-hallinto-oikeus-attachment! db aws-s3-client (:id valvonta) toimenpide osapuoli))))
                           (remove nil?)))]
    (asha/log-toimenpide!
      sender-id
      request-id
      case-number
      processing-action
      documents
      attachments)))

(defn close-case! [whoami valvonta-id toimenpide]
  (asha/close-case!
    (:email whoami)
    (request-id valvonta-id (:id toimenpide))
    (:diaarinumero toimenpide)
    (:description toimenpide)))
