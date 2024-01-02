(ns solita.etp.service.valvonta
  (:require [solita.etp.db :as db]))

(db/require-queries 'valvonta)

(defn find-valvojat [db] (valvonta-db/select-valvojat db))
