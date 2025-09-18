(ns solita.etp.service.perusparannuspassi
  (:require
    [clojure.java.jdbc :as jdbc]
    [solita.etp.db :as db]))

(db/require-queries 'perusparannuspassi)

(defn find-perusparannuspassi [db id]
  (jdbc/with-db-transaction
    [tx db]
    (if-let [ppp (-> (perusparannuspassi-db/select-perusparannuspassi tx {:id id})
                     first)]
      (assoc ppp :vaiheet (->> (perusparannuspassi-db/select-perusparannuspassi-vaiheet tx {:perusparannuspassi-id (:id ppp)})
                               (map #(dissoc % :perusparannuspassi-id))
                               (into []))))))
