(ns solita.etp.service.valvonta-kaytto
  (:require [solita.etp.service.valvonta-kaytto.asha :as asha]
            [solita.etp.service.valvonta-kaytto.osapuoli :as osapuoli]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]
            [solita.etp.service.valvonta-kaytto.suomifi-viestit :as suomifi-viestit]
            [clojure.java.io :as io]
            [solita.etp.service.file :as file-service]
            [solita.etp.db :as db]
            [clojure.java.jdbc :as jdbc]
            [solita.etp.service.luokittelu :as luokittelu]
            [flathead.flatten :as flat]
            [solita.common.maybe :as maybe]
            [solita.common.map :as map]
            [solita.common.logic :as logic]
            [solita.etp.service.valvonta-kaytto.store :as store]
            [solita.etp.service.valvonta-kaytto.email :as email]
            [solita.etp.exception :as exception]
            [solita.etp.service.concurrent :as concurrent]
            [solita.etp.service.liite :as liite-service]
            [solita.etp.service.geo :as geo-service]
            [solita.etp.service.csv :as csv-service])
  (:import (java.time Instant)))

(db/require-queries 'valvonta-kaytto)

(defn find-henkilot [db valvonta-id]
  (valvonta-kaytto-db/select-henkilot db {:valvonta-id valvonta-id}))

(defn find-yritykset [db valvonta-id]
  (valvonta-kaytto-db/select-yritykset db {:valvonta-id valvonta-id}))

(defn- db-row->valvonta [valvonta-db-row]
  (update valvonta-db-row :postinumero (maybe/lift1 #(format "%05d" %))))

(def ^:private default-valvonta-query
  {:valvoja-id             nil
   :has-valvoja            nil
   :include-closed         false
   :only-uhkasakkoprosessi false
   :keyword                nil
   :toimenpidetype-id      nil
   :asiakirjapohja-id      nil
   :limit                  10
   :offset                 0})

(defn department-head-data
  "Finds the previously used department head title
   and name from the newest käskypäätös / varsinainen päätös toimenpide specific data"
  [db]
  (if-let [latest-department-head (first (valvonta-kaytto-db/find-department-head-data db))]
    latest-department-head
    {:department-head-title-fi nil
     :department-head-title-sv nil
     :department-head-name     nil}))

(defn- nil-if-not-exists [key object]
  (update object key (logic/when* (comp nil? :id) (constantly nil))))

(defn find-valvonnat [db query]
  (->> (valvonta-kaytto-db/select-valvonnat db (merge default-valvonta-query query))
       (map (comp (partial nil-if-not-exists :last-toimenpide)
                  (partial nil-if-not-exists :energiatodistus)
                  db-row->valvonta
                  #(flat/flat->tree #"\$" %)))
       (pmap #(assoc % :henkilot (find-henkilot db (:id %))
                       :yritykset (find-yritykset db (:id %))))))

(defn count-valvonnat
  "Returns the number of valvonnat that matches the query.
   Takes the same query parameters as find-valvonnat
   but does not respect limit and offset as the count is used to
   determine the need for pagination for find-valvonnat."
  [db query]
  (first
    (valvonta-kaytto-db/select-valvonnat-count
      db (merge default-valvonta-query query))))

(defn find-valvonta [db valvonta-id]
  (->> (valvonta-kaytto-db/select-valvonta db {:id valvonta-id})
       (map db-row->valvonta)
       first))

(defn- valvonta->db-row [valvonta]
  (update valvonta :postinumero (maybe/lift1 #(Integer/parseInt %))))

(defn add-valvonta! [db valvonta]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :vk-valvonta
        (valvonta->db-row valvonta)
        db/default-opts)
      first
      :id))

(defn update-valvonta! [db valvonta-id valvonta]
  (first (db/with-db-exception-translation
           jdbc/update! db :vk_valvonta
           (valvonta->db-row valvonta)
           ["id = ?" valvonta-id]
           db/default-opts)))

(defn delete-valvonta! [db valvonta-id]
  (valvonta-kaytto-db/delete-valvonta! db {:id valvonta-id}))

(defn find-ilmoituspaikat [db]
  (luokittelu/find-vk-ilmoituspaikat db))

(defn find-henkilo [db henkilo-id]
  (first (valvonta-kaytto-db/select-henkilo db {:id henkilo-id})))

(defn add-henkilo! [db valvonta-id henkilo]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :vk_henkilo
        (assoc henkilo :valvonta-id valvonta-id)
        db/default-opts)
      first
      :id))

(defn update-henkilo! [db valvonta-id henkilo-id henkilo]
  (first (db/with-db-exception-translation
           jdbc/update! db :vk_henkilo
           (assoc henkilo :valvonta-id valvonta-id)
           ["id = ?" henkilo-id]
           db/default-opts)))

(defn delete-henkilo! [db henkilo-id]
  (valvonta-kaytto-db/delete-henkilo! db {:id henkilo-id}))

(defn find-roolit [db]
  (luokittelu/find-vk-roolit db))

(defn find-toimitustavat [db]
  (luokittelu/find-vk-toimitustavat db))

(defn find-hallinto-oikeudet [db]
  (luokittelu/find-hallinto-oikeudet db))

(defn find-karajaoikeudet [db]
  (luokittelu/find-karajaoikeudet db))

(defn find-yritys [db yritys-id]
  (first (valvonta-kaytto-db/select-yritys db {:id yritys-id})))

(defn add-yritys! [db valvonta-id yritys]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :vk_yritys
        (assoc yritys :valvonta-id valvonta-id)
        db/default-opts)
      first
      :id))

(defn update-yritys! [db valvonta-id yritys-id yritys]
  (first (db/with-db-exception-translation
           jdbc/update! db :vk_yritys
           (assoc yritys :valvonta-id valvonta-id)
           ["id = ?" yritys-id]
           db/default-opts)))

(defn delete-yritys! [db yritys-id]
  (valvonta-kaytto-db/delete-yritys! db {:id yritys-id}))

(defn find-liitteet [db valvonta-id]
  (valvonta-kaytto-db/select-liite-by-valvonta-id db {:valvonta-id valvonta-id}))

(defn file-path [valvonta-id liite-id]
  (str "/valvonta/kaytto/" valvonta-id "/liitteet/" liite-id))

(defn- insert-liite! [db liite]
  (-> (db/with-db-exception-translation jdbc/insert! db :vk_valvonta_liite liite db/default-opts)
      first
      :id))

(defn add-liitteet-from-files! [db aws-s3-client valvonta-id liitteet]
  (doseq [liite liitteet]
    (let [liite-id (insert-liite! db (-> liite
                                         liite-service/temp-file->liite
                                         (assoc :valvonta-id valvonta-id)))]
      (file-service/upsert-file-from-file
        aws-s3-client
        (file-path valvonta-id liite-id)
        (:tempfile liite)))))

(defn add-liite-from-link! [db valvonta-id liite]
  (insert-liite! db (assoc liite :contenttype "text/uri-list"
                                 :valvonta-id valvonta-id)))

(defn delete-liite! [db liite-id]
  (valvonta-kaytto-db/delete-liite! db {:id liite-id}))

(defn find-liite [db aws-s3-client valvonta-id liite-id]
  (when-let [liite (first (valvonta-kaytto-db/select-liite db {:id liite-id}))]
    (assoc liite :tempfile
                 (file-service/find-file aws-s3-client (file-path valvonta-id liite-id)))))

(defn find-toimenpidetyypit [db]
  (valvonta-kaytto-db/find-vk-toimenpidetypes db))

(defn find-templates [db]
  (valvonta-kaytto-db/select-templates db))

(defn toimenpide-filename [toimenpide]
  (:filename (asha/toimenpide-type->document (:type-id toimenpide))))

(defn- db-row->toimenpide [db-row]
  (as-> db-row %
        (flat/flat->tree #"\$" %)
        (assoc % :filename (toimenpide-filename %))))

(defn- find-toimenpide-henkilot [db toimenpide-id]
  (valvonta-kaytto-db/select-toimenpide-henkilo db {:toimenpide-id toimenpide-id}))

(defn- find-toimenpide-yritykset [db toimenpide-id]
  (valvonta-kaytto-db/select-toimenpide-yritys db {:toimenpide-id toimenpide-id}))

(defn find-toimenpiteet [db valvonta-id]
  (->> (valvonta-kaytto-db/select-toimenpiteet db {:valvonta-id valvonta-id})
       (map db-row->toimenpide)
       (pmap #(assoc % :henkilot (find-toimenpide-henkilot db (:id %))
                       :yritykset (find-toimenpide-yritykset db (:id %))))))

(defn- insert-toimenpide-osapuolet! [db valvonta-id toimenpide-id]
  (let [params (map/bindings->map valvonta-id toimenpide-id)]
    (valvonta-kaytto-db/insert-toimenpide-henkilot! db params)
    (valvonta-kaytto-db/insert-toimenpide-yritykset! db params)))

(defn- insert-toimenpide! [db valvonta-id diaarinumero toimenpide-add]
  (first (db/with-db-exception-translation
           jdbc/insert! db :vk-toimenpide
           (-> toimenpide-add
               (dissoc :bypass-asha)
               (assoc
                 :diaarinumero diaarinumero
                 :valvonta-id valvonta-id
                 :publish-time (Instant/now)))
           db/default-opts)))

(defn find-diaarinumero [db id toimenpide]
  (when (toimenpide/with-diaarinumero? toimenpide)
    (-> (valvonta-kaytto-db/select-last-diaarinumero db {:id id})
        first
        :diaarinumero)))

(defn- find-osapuolet [db valvonta-id]
  (concat
    (find-henkilot db valvonta-id)
    (find-yritykset db valvonta-id)))

(defn- send-toimenpide-email! [db aws-s3-client valvonta toimenpide osapuolet]
  (when (-> db :connection some?)
    (exception/illegal-argument!
      (str "Connections are not thread safe see "
           "https://jdbc.postgresql.org/documentation/head/thread.html. "
           "Existing connection is not allowed when sending emails in background.")))
  (concurrent/run-background
    #(email/send-toimenpide-email! db aws-s3-client valvonta toimenpide osapuolet)
    (str "Sending email failed for toimenpide: " (:id valvonta) "/" (:id toimenpide))))

(defn- send-suomifi-viestit! [aws-s3-client
                              valvonta
                              toimenpide
                              osapuolet]
  (concurrent/run-background
    #(suomifi-viestit/send-suomifi-viestit! aws-s3-client valvonta toimenpide osapuolet)
    (str "Sending suomifi-viestit failed for toimenpide: " (:id valvonta) "/" (:id toimenpide))))

(defn add-toimenpide! [db aws-s3-client whoami valvonta-id toimenpide-add]
  (jdbc/with-db-transaction
    [tx db]
    (let [add-postitoimipaikka (fn [valvonta]
                                 (let [postinumero (maybe/map* #(luokittelu/find-luokka
                                                                  (Integer/parseInt %)
                                                                  (geo-service/find-all-postinumerot db))
                                                               (:postinumero valvonta))]
                                   (assoc valvonta
                                     :postitoimipaikka-fi (:label-fi postinumero)
                                     :postitoimipaikka-sv (:label-sv postinumero))))
          valvonta (-> (find-valvonta tx valvonta-id)
                       (add-postitoimipaikka))
          ilmoituspaikat (find-ilmoituspaikat tx)
          roolit (find-roolit tx)
          diaarinumero (if (toimenpide/case-open? toimenpide-add)
                         (asha/open-case!
                           tx whoami valvonta
                           (find-osapuolet tx valvonta-id)
                           ilmoituspaikat)
                         (find-diaarinumero tx valvonta-id toimenpide-add))
          toimenpide (insert-toimenpide! tx valvonta-id diaarinumero toimenpide-add)
          toimenpide-id (:id toimenpide)]
      (insert-toimenpide-osapuolet! tx valvonta-id toimenpide-id)
      (let [case-close (toimenpide/case-close? toimenpide)
            bypass-asha (:bypass-asha toimenpide-add)
            asha-toimenpide (toimenpide/asha-toimenpide? toimenpide-add)]
        (cond
          ;; Close in asha unless bypassed
          case-close
          (when (not bypass-asha) (asha/close-case! whoami valvonta-id toimenpide))

          ;; Log asha-toimenpide and send messages
          ;; kehotus-toimenpide has all osapuolet, everything else in the process targets only the owners
          asha-toimenpide
          (let [find-toimenpide-osapuolet (comp flatten (juxt find-toimenpide-henkilot find-toimenpide-yritykset))
                osapuolet (cond->> (find-toimenpide-osapuolet tx (:id toimenpide))
                                   (not (toimenpide/kehotus? toimenpide)) (filter osapuoli/omistaja?))]
            (asha/log-toimenpide!
              tx aws-s3-client whoami valvonta toimenpide
              osapuolet ilmoituspaikat roolit)
            (when-not (toimenpide/manually-deliverable? db (:type-id toimenpide))
              (send-suomifi-viestit! aws-s3-client valvonta toimenpide osapuolet)
              (send-toimenpide-email! db aws-s3-client valvonta toimenpide osapuolet)))))

      {:id toimenpide-id})))

(defn update-toimenpide! [db toimenpide-id toimenpide]
  (first (db/with-db-exception-translation
           jdbc/update! db :vk_toimenpide
           toimenpide ["id = ?" toimenpide-id]
           db/default-opts)))

(defn preview-toimenpide [db whoami id toimenpide osapuoli]
  (maybe/map*
    io/input-stream
    (asha/generate-pdf-document
      db
      whoami
      (find-valvonta db id)
      (assoc toimenpide :diaarinumero (find-diaarinumero db id toimenpide))
      (find-ilmoituspaikat db)
      osapuoli
      (find-osapuolet db id)
      (find-roolit db))))


(defn preview-henkilo-toimenpide [db whoami id toimenpide henkilo-id]
  (preview-toimenpide
    db
    whoami
    id
    toimenpide
    (find-henkilo db henkilo-id)))

(defn preview-yritys-toimenpide [db whoami id toimenpide yritys-id]
  (preview-toimenpide
    db
    whoami
    id
    toimenpide
    (find-yritys db yritys-id)))

(defn find-toimenpide-henkilo-document [db aws-s3-client valvonta-id toimenpide-id henkilo-id]
  (store/find-document aws-s3-client valvonta-id toimenpide-id (find-henkilo db henkilo-id)))

(defn find-toimenpide-yritys-document [db aws-s3-client valvonta-id toimenpide-id yritys-id]
  (store/find-document aws-s3-client valvonta-id toimenpide-id (find-yritys db yritys-id)))

(defn find-henkilo-hallinto-oikeus-attachment [db aws-s3-client valvonta-id toimenpide-id henkilo-id]
  (store/find-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id (find-henkilo db henkilo-id)))

(defn find-yritys-hallinto-oikeus-attachment [db aws-s3-client valvonta-id toimenpide-id yritys-id]
  (store/find-hallinto-oikeus-attachment aws-s3-client valvonta-id toimenpide-id (find-yritys db yritys-id)))

(defn find-notes [db id]
  (valvonta-kaytto-db/select-valvonta-notes
    db {:valvonta-id id}))

(defn add-note! [db valvonta-id description]
  (-> (db/with-db-exception-translation
        jdbc/insert! db :vk-note
        {:valvonta-id valvonta-id
         :description description}
        db/default-opts)
      first
      (select-keys [:id])))

(defn update-note! [db id description]
  (first (db/with-db-exception-translation
           jdbc/update! db :vk-note
           {:description description} ["id = ?" id]
           db/default-opts)))

(def ^:private csv-header
  (csv-service/csv-line
    ["id" "rakennustunnus", "diaarinumero"
     "katuosoite" "postinumero" "postitoimipaikka"
     "toimenpide-id" "toimenpidetyyppi" "aika" "valvoja" "energiatodistus hankittu"]))

(defn- boolean->checked
  "Represent boolean as x when it's true in käytönvalvonta-csv and as empty when false"
  [csv-row-coll]
  (map
    (fn [value]
      (if (boolean? value)
        (when value
          "x")
        value))
    csv-row-coll))

(defn csv [db]
  (fn [write!]
    (write! csv-header)
    (jdbc/query
      db
      "select valvonta.id,
              valvonta.rakennustunnus,
              toimenpide.diaarinumero,
              valvonta.katuosoite,
              lpad(valvonta.postinumero::text, 5, '0'),
              postinumero.label_fi,
              toimenpide.id,
              toimenpidetype.label_fi,
              toimenpide.publish_time,
              fullname(kayttaja),
              energiatodistus is not null as energiatodistus_exists

       from vk_valvonta valvonta
           left join postinumero on postinumero.id = valvonta.postinumero
           left join vk_toimenpide toimenpide on toimenpide.valvonta_id = valvonta.id
           left join vk_toimenpidetype toimenpidetype on toimenpidetype.id = toimenpide.type_id
           left join kayttaja on kayttaja.id = valvonta.valvoja_id
           left join lateral (select next_toimenpide.create_time
                                     from vk_toimenpide as next_toimenpide
                                     where next_toimenpide.valvonta_id = valvonta.id
                                     and next_toimenpide.create_time > toimenpide.create_time
                                     order by create_time asc
                                     limit 1) as next_toimenpide on true
           left join lateral (select energiatodistus.id
                                     from energiatodistus
                                     where energiatodistus.pt$rakennustunnus = valvonta.rakennustunnus
                                     -- Energiatodistus cannot be acquired during valvonnan aloitus or lopetus, so the end
                                     -- of tszrange in those cases is the toimenpide create_time - range from
                                     -- create_time to create_time includes nothing.
                                     -- For other toimenpidetypes, coalesce selects either the create_time of the next toimenpide
                                     -- as the end of the range. As tstzrange is exclusive in the end, if the energiatodistus
                                     -- is acquired before that it gets attributed to the current toimenpide.
                                     -- If there is no next toimenpide yet, the end of the range is infinity to catch
                                     -- that the energiatodistus was acquired during the current toimenpide.
                                     -- With this implementation we don't need to check the toimenpide deadline,
                                     -- as the next toimenpide create_time is the cut-off point for the current toimenpide
                                     -- instead of the deadline.
                                     and tstzrange(toimenpide.create_time,
                                                   CASE
                                                       WHEN toimenpide.type_id in (0, 5) THEN toimenpide.create_time
                                                       ELSE coalesce(next_toimenpide.create_time, 'infinity')
                                                       END
                                         ) @>
                                         energiatodistus.allekirjoitusaika
                                     limit 1) energiatodistus on true
           where not valvonta.deleted"

      {:row-fn        (comp write! csv-service/csv-line boolean->checked)
       :as-arrays?    :cols-as-is
       :result-set-fn dorun
       :result-type   :forward-only
       :concurrency   :read-only
       :fetch-size    100})))

(defn find-valvonnat-by-rakennustunnus [db rakennustunnus]
  (valvonta-kaytto-db/select-valvonta-by-rakennustunnus
    db
    {:rakennustunnus rakennustunnus}))

