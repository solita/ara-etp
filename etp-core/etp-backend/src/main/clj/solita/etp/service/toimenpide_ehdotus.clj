(ns solita.etp.service.toimenpide-ehdotus
  (:require [clojure.java.jdbc :as jdbc]
            [solita.etp.db :as db]))

(defn find-all
  "Find all toimenpide-ehdotus entries with their ordered by ordinal"
  [db]
  (jdbc/query db
              ["SELECT id, label_fi, label_sv, valid FROM toimenpide_ehdotus ORDER BY ordinal ASC"]
              db/default-opts))

(defn find-toimenpide-ehdotus-groups
  "Find all toimenpide-ehdotus groups"
  [db]
  (jdbc/query db
              ["SELECT id, label_fi, label_sv, valid FROM toimenpide_ehdotus_group ORDER BY ordinal ASC"]
              db/default-opts))

(defn group-by-group-id
  "Group toimenpide-ehdotus entries by their group_id.
   Returns a map of group-id -> [toimenpide-ehdotus entries]"
  [toimenpide-ehdotukset]
  (group-by :group-id toimenpide-ehdotukset))

(defn find-by-group-id
  "Find all toimenpide-ehdotus entries for a specific group"
  [db group-id]
  (jdbc/query db
              ["SELECT id, label_fi, label_sv, valid, group_id FROM toimenpide_ehdotus WHERE group_id = ? ORDER BY ordinal ASC"
               group-id]
              db/default-opts))

