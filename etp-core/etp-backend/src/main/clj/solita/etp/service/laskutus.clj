(ns solita.etp.service.laskutus
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.core.match :as match]
            [clojure.tools.logging :as log]
            [solita.common.xml :as xml]
            [solita.common.xlsx :as xlsx]
            [solita.common.libreoffice :as libreoffice]
            [solita.common.sftp :as sftp]
            [solita.common.smtp :as smtp]
            [solita.etp.email :as email]
            [solita.etp.config :as config]
            [solita.etp.db :as db]
            [solita.etp.service.file :as file-service])
  (:import (java.time Instant LocalDate Month ZoneId)
           (java.time.temporal ChronoUnit)
           (java.time.format DateTimeFormatter)))

;; *** Require sql functions ***
(db/require-queries 'laskutus)

(def tmp-dir "tmp-laskutus")

(def asiakastieto-destination-dir "etp/from_etp/asiakastieto/ara/")
(def asiakastieto-filename-prefix "asiakastieto_etp_ara_")
(def laskutustieto-destination-dir "etp/from_etp/laskutustieto/ara/")
(def laskutustieto-filename-prefix "laskutustieto_etp_ara_")

(def asiakastieto-ns "http://kiekuhanke.fi/kieku/asiakasin")
(def laskutustieto-ns "http://www.kiekuhanke.fi/kieku/myyntitilaus")

(def timezone (ZoneId/of "Europe/Helsinki"))
(def date-formatter-fi (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy")
                                  timezone))
(def date-formatter-xml (.withZone (DateTimeFormatter/ofPattern "yyyyMMdd")
                                   timezone))
(def time-formatter-fi (.withZone (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")
                                  timezone))

(def time-formatter-file (.withZone (DateTimeFormatter/ofPattern "yyyyMMddHHmmss")
                                    timezone))

(def sleep-between-asiakastiedot-and-laskutustiedot (* 15 60 1000))

(defn safe-subs [s start end]
  (when s
    (let [start (max start 0)]
      (subs s start (max start (min end (count s)))))))

(defn find-kuukauden-laskutus [db]
  (laskutus-db/select-kuukauden-laskutus db))

(defn mark-as-laskutettu! [db laskutus]
  (laskutus-db/mark-as-laskutettu! db {:ids (mapv :energiatodistus-id laskutus)}))


(defn asiakasluokituskoodi [type-id]
  (case type-id
    1 "03"
    2 "01"))

(def fields-for-asiakastieto
  #{:laskutus-asiakastunnus :laatija-id :nimi :henkilotunnus :laskutuskieli
    :yritys-id :ytunnus :valittajatunnus :verkkolaskuosoite :jakeluosoite
    :vastaanottajan-tarkenne :postinumero :postitoimipaikka :maa :type-id})

(defn asiakastiedot [laskutus]
  (->> laskutus
       (reduce (fn [acc {:keys [laskutus-asiakastunnus] :as laskutus-item}]
                 (if (contains? acc laskutus-asiakastunnus)
                   acc
                   (assoc acc
                          laskutus-asiakastunnus
                          (select-keys laskutus-item fields-for-asiakastieto))))
               {})
       vals))

(defn asiakastieto-xml
  [{:keys [laskutus-asiakastunnus nimi henkilotunnus laskutuskieli ytunnus
           valittajatunnus verkkolaskuosoite jakeluosoite
           vastaanottajan-tarkenne postinumero postitoimipaikka maa yritys-id type-id]}]
  (let [verkkolaskutus? (and valittajatunnus verkkolaskuosoite)
        long-jakeluosoite? (> (count jakeluosoite) 35)
        luokituskoodi (if yritys-id
                        (asiakasluokituskoodi type-id)
                        "02")]
    (->> [["AsiakasTunnus" laskutus-asiakastunnus]
          ["TiliryhmaAsiakasKoodi" "Z700"]
          ["AsiakasluokitusKoodi" luokituskoodi]
          ["MaaKoodi" maa]
          ["Nimi1Nimi" (safe-subs nimi 0 35)]
          ["Nimi2Nimi" (safe-subs vastaanottajan-tarkenne 0 35)]
          ["LajittelutietoTeksti" (safe-subs nimi 0 10)]
          ["KieliavainKoodi" (case laskutuskieli
                               1 "V"
                               2 "E"
                               "U")]
          (when ytunnus
            ["YritysTunnus" ytunnus])
          (when ytunnus
            ["AlvNro" (str "FI" (str/replace ytunnus #"-" ""))])
          (when verkkolaskutus?
            ["VastaanottajaOVTTunnus" verkkolaskuosoite])
          (when verkkolaskutus?
            ["VastaanottajaOperaattoriTunnus" valittajatunnus])
          ["LuonnollinenHloKytkin" (if yritys-id "false" "true")]
          ["LaskuKasittelyohjeTeksti" (if verkkolaskutus?
                                        "20000000"
                                        "01000000")]
          ;; TODO YritysmuotoKoodi if yritys is toiminimi.
          ["AsiakasYritystasoinenTietoTyyppi"
           ["YritysTunnus" "7010"]
           ["LajittelutAvainKoodi" "001"]
           ["MaksuehtoavainKoodi" "ZM21"]
           ["KoronlaskentaKoodi" "Z1"]
           ["KassaSuunnitteluryhmaKoodi" "E1"]
           ["MaksukayttaytyminenTallennusKytkin" "true"]]
          ["AsiakasYhteysTietoTyyppi"
           ["LahiOsoite2" (if long-jakeluosoite?
                            (subs jakeluosoite 0 36)
                            "")]
           ["LahiOsoite" (if long-jakeluosoite?
                           (subs jakeluosoite 36)
                           jakeluosoite)]
           ["PaikkakuntaKoodi" postitoimipaikka]
           ["PostiNro" postinumero]
           ["MaaKoodi" maa]
           (when-not yritys-id
             ["AsiakasHenkiloTunnus" henkilotunnus])]
          ["AsiakasMyyntiJakelutietoTyyppi"
           ["MyyntiOrganisaatioKoodi" "7010"]
           ["JakelutieKoodi" "01"]
           ["SektoriKoodi" "01"]
           ["ValuuttaKoodi" "EUR"]
           ["AsiakasTiliointiryhmaKoodi" luokituskoodi]
           ["MaksuehtoavainKoodi" "ZM21"]]
          ["AsiakasSanomaPerustietoTyyppi"
           ["YleinenLahettajaInformaatioTyyppi"
            ["PorttiNro" (if (= config/environment-alias "prod")
                           "SAPVMP"
                           "SAPVMA")]
            ["KumppaniNro" "ETP"]
            ["KumppanilajiKoodi" "LS"]]]]
         xml/simple-elements
         (apply (fn [& elements]
                  (xml/element (xml/qname asiakastieto-ns "Asiakas")
                               {:xmlns/ns2 asiakastieto-ns}
                               elements))))))

(defn laskutustiedot [laskutus]
  (->> laskutus
       (reduce (fn [acc {:keys [laskutus-asiakastunnus laatija-id laatija-nimi
                               energiatodistus-id allekirjoitusaika
                               laskutuskieli laskuriviviite]}]
                 (-> acc
                     (assoc-in [laskutus-asiakastunnus :laskutus-asiakastunnus]
                               laskutus-asiakastunnus)
                     (assoc-in [laskutus-asiakastunnus :laskutuskieli]
                               laskutuskieli)
                     (assoc-in [laskutus-asiakastunnus
                                :laatijat
                                laatija-id
                                :nimi]
                               laatija-nimi)
                     (update-in [laskutus-asiakastunnus
                                 :laatijat
                                 laatija-id
                                 :energiatodistukset]
                                conj
                                {:id energiatodistus-id
                                 :allekirjoitusaika allekirjoitusaika
                                 :laskuriviviite laskuriviviite})))
               {})
       vals))

(defn tilausrivi [teksti kieli]
  ["TilausriviTekstiTyyppi"
   ["Teksti" teksti]
   ["TekstiSijaintiKoodi" "0002"]
   ["KieliKoodi" (case kieli
                   1 "SV"
                   2 "EN"
                   "FI")]])

(defn energiatodistus-tilausrivi-text [id allekirjoitusaika laskuriviviite laskutuskieli]
  (apply format
         (match/match [laskutuskieli laskuriviviite]
                      [1 (_ :guard nil?)] "Energicertifikat %s, datum: %s"
                      [1 _] "Energicertifikat %s, datum: %s, referens: %s"
                      [2 (_ :guard nil?)] "EPC %s, date: %s"
                      [2 _] "EPC %s, date: %s, reference: %s"
                      [_ (_ :guard nil?)] "Energiatodistus %s, pvm: %s"
                      [_ _] "Energiatodistus %s, pvm: %s, viite: %s")
         [(str id) (.format date-formatter-fi allekirjoitusaika) laskuriviviite]))

(defn tilausrivit-for-laatija [{:keys [nimi energiatodistukset]} laskutuskieli]
  (cons (tilausrivi nimi laskutuskieli)
        (->> energiatodistukset
             (sort-by :id)
             (map (fn [{:keys [id allekirjoitusaika laskuriviviite]}]
                    (tilausrivi (energiatodistus-tilausrivi-text id
                                                                 allekirjoitusaika
                                                                 laskuriviviite
                                                                 laskutuskieli)
                                laskutuskieli))))))

(defn laskutustieto-xml [now {:keys [laskutus-asiakastunnus laskutuskieli
                                     laatijat]}]
  (let [today (LocalDate/ofInstant now timezone)
        last-month (.minusMonths today 1)
        december-laskutus? (-> last-month .getMonth (= Month/DECEMBER))
        formatted-last-day-of-last-month (->> (.lengthOfMonth last-month)
                                              (.withDayOfMonth last-month)
                                              (.format date-formatter-xml))]
    (->> [["MyyntiOrganisaatioKoodi" "7010"]
          ["JakelutieKoodi" "13"]
          ["SektoriKoodi" "01"]
          ["TilausLajiKoodi" "Z001"]
          ["PalveluLuontiPvm" formatted-last-day-of-last-month]
          (if december-laskutus? ["LaskuPvm" formatted-last-day-of-last-month] nil)
          ["HinnoitteluPvm" formatted-last-day-of-last-month]
          ["TilausAsiakasTyyppi"
           ["AsiakasNro" laskutus-asiakastunnus]]
          ["LaskuttajaAsiakasTyyppi"
           ["AsiakasNro" "701013A000"]]
          ["MyyntitilausSanomaPerustietoTyyppi"
           ["YleinenLahettajaInformaatioTyyppi"
            ["PorttiNro" (if (= config/environment-alias "prod")
                           "SAPVMP"
                           "SAPVMA")]
            ["KumppaniNro" "ETP"]
            ["KumppanilajiKoodi" "LS"]]]
          (vec (concat ["MyyntiTilausriviTyyppi"
                        ["RiviNro" "10"]
                        ["TilausMaaraArvo" (->> laatijat
                                                vals
                                                (mapcat :energiatodistukset)
                                                count)]
                        ["NimikeNro" "RA0001"]]
                       (mapcat #(tilausrivit-for-laatija % laskutuskieli)
                               (->> laatijat vals (sort-by :nimi)))))]
         (filterv #(not (nil? %)))
         xml/simple-elements
         (apply (fn [& elements]
                  (xml/element (xml/qname laskutustieto-ns "Myyntitilaus")
                               {:xmlns/ns2 laskutustieto-ns}
                               elements))))))

(defn tasmaytysraportti [laskutus now]
  (let [row-data (reduce (fn [acc {:keys [laskutus-asiakastunnus nimi]}]
                           (if (get acc laskutus-asiakastunnus)
                             (update-in acc [laskutus-asiakastunnus :count] inc)
                             (assoc acc laskutus-asiakastunnus {:count 1
                                                                :nimi nimi})))
                         {}
                         laskutus)
        row-data-count (count row-data)
        laskutus-count (count laskutus)]
    (concat [["ETP" nil nil nil nil (.format time-formatter-fi now)]
             ["ARA" nil (str "Myyntilaskut " (.format date-formatter-fi
                                                      (.minus now 1 ChronoUnit/DAYS)))]
             []
             ["Asiakkaiden lukumäärä yhteensä" nil {:v row-data-count :align :left}]
             ["Myyntitilausten lukumäärä yhteensä" nil {:v row-data-count :align :left}]
             ["Velotusmyyntitilausten lukumäärä yhteensä" nil {:v row-data-count :align :left}]
             ["Energiatodistusten lukumäärä yhteensä" nil {:v laskutus-count :align :left}]
             ["Hyvitystilausten lukumäärä yhteensä" nil {:v 0 :align :left}]
             ["Siirrettyjen liitetiedostojen lukumäärä" nil {:v 0 :align :left}]
             []
             []
             (mapv #(hash-map :v % :align :center)
                   ["Tilauslaji" "Asiakkaan numero" "Asiakkaan nimi" "Laskutettava nimike" "KPL"])]
            (->> row-data
                 keys
                 sort
                 (map (fn [laskutus-asiakastunnus]
                        [{:v "Z001" :align :center}
                         {:v laskutus-asiakastunnus :align :center}
                         {:v (get-in row-data [laskutus-asiakastunnus :nimi])
                          :align :center}
                         {:v "RA0001" :align :center}
                         {:v (get-in row-data [laskutus-asiakastunnus :count])
                          :align :center}]))))))

(defn write-tasmaytysraportti-file! [tasmaytysraportti now]
  (io/make-parents (str tmp-dir "/example.txt"))
  (let [xlsx-path (str tmp-dir
                       "/tasmaytysraportti-"
                       (.format time-formatter-file now)
                       ".xlsx")
        xlsx-file (io/file xlsx-path)
        pdf-path (str/replace xlsx-path #".xlsx$" ".pdf")
        pdf-file (io/file pdf-path)]
    (with-open [xlsx (xlsx/create-xlsx)]
      (let [sheet (xlsx/create-sheet xlsx "Sheet 0")]
        (xlsx/set-sheet-landscape sheet true)
        (xlsx/fill-sheet! xlsx sheet tasmaytysraportti [5000 5000 7000 5000 5000])
        (xlsx/save-xlsx xlsx xlsx-path)))
    (let [{:keys [exit] :as sh-result} (libreoffice/run-with-args
                                            "--convert-to"
                                            "pdf"
                                            (.getName xlsx-file)
                                            :dir
                                            (.getParent xlsx-file))
          pdf-exists? (.exists pdf-file)]
      (io/delete-file xlsx-path)
      (if (and (zero? exit) pdf-exists?)
        pdf-file
        (throw (ex-info "Creating tasmaytysraportti PDF failed."
                        (assoc sh-result
                               :type :xlsx-pdf-conversion-failure
                               :xlsx (.getName xlsx-file)
                               :pdf-result? pdf-exists?)))))))

(defn send-tasmaytysraportti-email! [tasmaytysraportti-file]
  (email/send-multipart-email! {:to          config/laskutus-tasmaytysraportti-email-to
                                :subject     "ARA ETP täsmätysraportti"
                                :body        "Liitteenä täsmäytysraportti."
                                :subtype     "plain"
                                :attachments [(smtp/file->attachment tasmaytysraportti-file)]}))

(defn xml-filename [now filename-prefix idx]
  (str filename-prefix
       (.format time-formatter-file now)
       (format "%03d" idx)
       ".xml"))

(defn write-xmls-files! [xmls filename-prefix]
  (doall (for [[idx xml] (map-indexed vector xmls)
               :let [now (Instant/now)
                     filename (xml-filename now filename-prefix idx)
                     path (str tmp-dir "/" filename)]]
           (with-open [file (io/writer path)]
             (xml/emit xml file)
             (io/file path)))))

(defn file-key-prefix [now dry-run?]
  (let [today (LocalDate/ofInstant now timezone)]
    (str "laskutus/"
         (.getYear today)
         "/"
         (.format time-formatter-file now)
         (when dry-run? "-dry-run")
         "/")))

(defn store-files! [aws-s3-client file-key-prefix files]
  (doseq [file files]
    (file-service/upsert-file-from-file aws-s3-client
                                        (str file-key-prefix (.getName file))
                                        file)))

(defn upload-files-with-sftp! [sftp-connection files destination-dir]
  (sftp/make-directory! sftp-connection destination-dir)
  (doseq [file files]
    (sftp/upload! sftp-connection
                  (.getPath file)
                  (str destination-dir (.getName file)))))

(defn delete-files! [files]
  (doseq [file files]
    (io/delete-file (.getPath file))))

(defn connect-sftp! []
  (sftp/connect! config/laskutus-sftp-host
                 config/laskutus-sftp-port
                 config/laskutus-sftp-username
                 config/laskutus-sftp-password
                 config/known-hosts-path))

(defn do-kuukauden-laskutus [db aws-s3-client dry-run?]
  (log/info "Starting kuukauden laskutusajo." {:dry-run? dry-run?})
  (io/make-parents (str tmp-dir "/example.txt"))
  (let [now (Instant/now)
        laskutus (find-kuukauden-laskutus db)
        asiakastieto-xmls (->> laskutus
                               asiakastiedot
                               (map asiakastieto-xml))
        laskutustieto-xmls (->> laskutus
                                laskutustiedot
                                (map #(laskutustieto-xml now %)))
        asiakastieto-xml-files (write-xmls-files!
                                asiakastieto-xmls
                                asiakastieto-filename-prefix)
        laskutustieto-xml-files (write-xmls-files!
                                 laskutustieto-xmls
                                 laskutustieto-filename-prefix)
        tasmaytysraportti-file (write-tasmaytysraportti-file!
                                (tasmaytysraportti laskutus now)
                                now)
        all-files (concat asiakastieto-xml-files
                          laskutustieto-xml-files
                          [tasmaytysraportti-file])
        file-key-prefix (file-key-prefix now dry-run?)]
    (log/info "Laskutus related files created.")
    (store-files! aws-s3-client file-key-prefix all-files)
    (log/info "Laskutus related files stored.")
    (if (every? #(-> % str/blank? not) [config/laskutus-sftp-host
                                        config/laskutus-sftp-username])
      (do (with-open [sftp-connection (connect-sftp!)]
            (log/info (str "SFTP connection (for uploading asiakastiedot) to "
                           config/laskutus-sftp-host
                           " established."))

            (if dry-run?
              (log/info "Skipping uploading asiakastieto xmls because this is a dry run.")
              (do
                (upload-files-with-sftp! sftp-connection
                                         asiakastieto-xml-files
                                         asiakastieto-destination-dir)
                (log/info "Asiakastieto xmls uploaded with SFTP."))))
          (log/info (format "Waiting %.2f minutes before continuing."
                            (double (/ sleep-between-asiakastiedot-and-laskutustiedot 1000 60))))
          (Thread/sleep sleep-between-asiakastiedot-and-laskutustiedot)
          (with-open [sftp-connection (connect-sftp!)]
            (log/info (str "SFTP connection (for uploading laskutustiedot) to "
                           config/laskutus-sftp-host
                           " established."))
            (if dry-run?
              (log/info "Skipping uploading laskutustieto xmls because this is a dry run.")
              (do
                (upload-files-with-sftp! sftp-connection
                                         laskutustieto-xml-files
                                         laskutustieto-destination-dir)
                (log/info "Laskutustieto xmls uploaded with SFTP."))))
          (if dry-run?
            (log/info "Skipping sending täsmätysraportti because this is a dry run.")
            (try
              (send-tasmaytysraportti-email! tasmaytysraportti-file)
              (log/info "Täsmätysraportti sent as an email")
              (catch Exception e
                (log/error "Sending täsmätysraportti as email failed:" e))))
          (if dry-run?
            (log/info "Skipping marking energiatodistukset as laskutettu because this is a dry run.")
            (do
              (mark-as-laskutettu! db laskutus)
              (log/info "Energiatodistukset marked as laskutettu"))))
      (log/warn "SFTP configuration missing. Skipping actual integration."))
    (delete-files! all-files)
    (log/info "Laskutus related temporary files deleted.")
    (log/info "Kuukauden laskutusajo finished.")
    {:started-at now
     :stopped-at (Instant/now)}))
