(ns solita.etp.db.flywaydb
  (:require [clojure.string :as str])
  (:import (org.flywaydb.core Flyway)
           (org.flywaydb.core.api.configuration FluentConfiguration)
           (org.flywaydb.core.internal.configuration ConfigUtils)))

(def guide
  (str/join
    \newline
    ["Usage: java -jar etp-db.jar [command]"
     "Parameters: DB_URL, DB_USER and DB_PASSWORD can be defined as environment variables. Otherwise common default values are used. "
     "Supported commands are: "
     "- clean    - initialize a fresh database"
     "- migrate  - migrate to the latest database layout"
     "- repair   - attempt to fix migration checksum mismatches"]))

(def flyway-configuration {
                           ConfigUtils/SCHEMAS                         "etp,audit"
                           ConfigUtils/SQL_MIGRATION_PREFIX            "v"
                           ConfigUtils/SQL_MIGRATION_SEPARATOR         "-"
                           ConfigUtils/REPEATABLE_SQL_MIGRATION_PREFIX "r"
                           ConfigUtils/LOCATIONS                       "classpath:migration"
                           "flyway.postgresql.transactional.lock"      "false"
                           ConfigUtils/CLEAN_DISABLED                  "false"}) ; Reverted to old default. There's a safety valve already in deployment scripts

(defn map-keys [f m] (into {} (map (fn [[k, v]] [(f k) v]) m)))

(defn configure-flyway [db]
  (-> ^FluentConfiguration (Flyway/configure)
      (.dataSource (:url db) (:user db) (:password db))
      (.configuration flyway-configuration)
      .load))

(defn env [name default]
  (or (System/getenv name) default))

(defn- add-application-name
  "Add ApplicationName to query parameters of the given url.

  Example:
  http://localhost:5763/db => http://localhost:5763/db?ApplicationName=0@database.etp
  http://localhost:5763/db?other_param=value => http://localhost:5763/db?ApplicationName=0@database.etp&other_param=value"
  [url]
  (let [[base-url existing-query-string] (str/split url #"\?" 2)]
    (->> ["ApplicationName=0@database.etp" existing-query-string]
         (remove nil?)
         (str/join "&")
         (str base-url "?"))))

(defn read-configuration []
  {:user     (env "DB_USER" "etp")
   :password (env "DB_PASSWORD" "etp")
   :url      (-> (env "DB_URL" "jdbc:postgresql://localhost:5432/postgres") add-application-name)})

(defn run [args]
  (let [command (str/trim (or (first args) "<empty string>"))
        db (read-configuration)
        flyway (configure-flyway db)]
    (case command
      "clean" (.clean flyway)
      "migrate" (.migrate flyway)
      "repair" (.repair flyway)
      (do
        (println "Unsupported command:" command)
        (println guide)))))

(defn -main [& args] (run args))
