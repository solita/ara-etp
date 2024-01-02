(ns solita.etp.service.valvonta-kaytto.hallinto-oikeus-attachment
  (:require [solita.etp.db :as db]
            [clojure.java.io :as io]))

(db/require-queries 'hallinto-oikeus)

(def attachment-directory-path "pdf/hallinto-oikeudet/")

(defn attachment-for-hallinto-oikeus-id [db hallinto-oikeus-id]
  (if-let [attachment-name (->> {:hallinto-oikeus-id hallinto-oikeus-id}
                                (hallinto-oikeus-db/find-attachment-name-by-hallinto-oikeus-id db)
                                first
                                :attachment-name)]
    (-> (str attachment-directory-path attachment-name)
        io/resource
        io/input-stream
        .readAllBytes)
    (throw (Exception. (str "Attachment not found for hallinto-oikeus-id: " hallinto-oikeus-id)))))
