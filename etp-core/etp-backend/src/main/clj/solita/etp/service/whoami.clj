(ns solita.etp.service.whoami
  (:require [schema.core :as schema]
            [schema-tools.core :as st]
            [buddy.hashers :as hashers]
            [flathead.flatten :as flat]
            [solita.etp.db :as db]
            [solita.etp.exception :as exception]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.schema.kayttaja :as kayttaja-schema]))

;; *** Require sql functions ***
(db/require-queries 'whoami)

(defn verified-api-key? [api-key api-key-hash]
  (if (and api-key api-key-hash)
    (:valid (hashers/verify api-key api-key-hash))
    false))

(def db-row->whoami
  (kayttaja-service/db-row->kayttaja
    (assoc kayttaja-schema/Whoami
      :api-key-hash (schema/maybe schema/Str))))

(defn- only-first! [query users]
  (when-not (empty? (rest users))
    (exception/throw-ex-info! {:type :whoami-duplicate
                               :message "Resolving whoami failed. More than one user matched the whoami query."
                               :users users
                               :query query}))
  (first users))

(defn- find-whoami-with-api-key-hash [db query]
  (some->> (merge {:email nil
                   :henkilotunnus nil
                   :virtu {:localid nil
                           :organisaatio nil}}
                  query)
           (flat/tree->flat "_")
           (whoami-db/select-whoami db)
           (only-first! query)
           db-row->whoami))

(defn find-whoami [db query]
  (some-> (find-whoami-with-api-key-hash db query)
          (st/select-schema kayttaja-schema/Whoami)))

(defn find-whoami-by-email-and-api-key [db email api-key]
  (let [whoami (find-whoami-with-api-key-hash db {:email email})]
    (when (verified-api-key? api-key (:api-key-hash whoami))
      (st/select-schema whoami kayttaja-schema/Whoami))))

(defn update-kayttaja-with-whoami! [db whoami]
  (whoami-db/update-kayttaja-with-whoami! db whoami))
