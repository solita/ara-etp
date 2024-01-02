(ns solita.etp.service.kayttotarkoitus
  (:require [solita.etp.db :as db]))

; *** Require sql functions ***
(db/require-queries 'kayttotarkoitus)

(defn find-kayttotarkoitukset [db versio]
  (kayttotarkoitus-db/select-kayttotarkoitusluokat-by-versio
   db
   {:versio versio}))

(defn find-alakayttotarkoitukset [db versio]
  (kayttotarkoitus-db/select-alakayttotarkoitusluokat-by-versio
   db
   {:versio versio}))

