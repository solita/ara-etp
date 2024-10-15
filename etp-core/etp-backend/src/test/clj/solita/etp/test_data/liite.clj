(ns solita.etp.test-data.liite
  (:require [clojure.java.io :as io]
            [solita.etp.test-system :as ts]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.service.liite :as liite-service]))

(def files ["deps.edn" "Dockerfile"])

(defn generate-file-adds [n]
  (->> files
       cycle
       (map (fn [filename]
              {:size        (rand-int 3000)
               :tempfile    (io/file filename)
               :contenttype "application/octet-stream"
               :nimi        filename}))
       (take n)))

(defn generate-link-adds [n]
  (map (fn [i]
         {:nimi (str "Link " i)
          :url  (str "https://example.com/" i)})
       (range n)))

(defn insert-files! [file-adds laatija-id energiatodistus-id]
  (liite-service/add-liitteet-from-files! (ts/db-user laatija-id)
                                          ts/*aws-s3-client*
                                          {:id laatija-id :rooli 0}
                                          energiatodistus-id
                                          file-adds))

(defn insert-links! [link-adds laatija-id energiatodistus-id]
  (mapv #(liite-service/add-liite-from-link! (ts/db-user laatija-id)
                                             {:id laatija-id :rooli 0}
                                             energiatodistus-id
                                             %)
        link-adds))

(defn generate-and-insert-files! [n laatija-id energiatodistus-id]
  (let [file-adds (generate-file-adds n)]
    (zipmap (insert-files! file-adds laatija-id energiatodistus-id) file-adds)))

(defn generate-and-insert-links! [n laatija-id energiatodistus-id]
  (let [link-adds (generate-link-adds n)]
    (zipmap (insert-links! link-adds laatija-id energiatodistus-id) link-adds)))

(defn insert-files-to-viestiketju! [file-adds laatija-id viestiketju-id]
  (viesti-service/add-liitteet-from-files! ts/*db*
                                           ts/*aws-s3-client*
                                           viestiketju-id
                                           file-adds))

(defn insert-links-to-viestiketju! [link-adds laatija-id viestiketju-id]
  (mapv #(viesti-service/add-liite-from-link! ts/*db*
                                              viestiketju-id
                                              %)
        link-adds))

(defn generate-and-insert-files-to-viestiketju! [n laatija-id viestiketju-id]
  (let [file-adds (generate-file-adds n)]
    (zipmap (insert-files-to-viestiketju! file-adds laatija-id viestiketju-id) file-adds)))

(defn generate-and-insert-links-to-viestiketju! [n laatija-id viestiketju-id]
  (let [link-adds (generate-link-adds n)]
    (zipmap (insert-links-to-viestiketju! link-adds laatija-id viestiketju-id) link-adds)))
