(ns solita.etp.test-data.laatija
  (:require [schema-tools.core :as st]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.service.kayttaja-laatija :as kayttaja-laatija-service]))

(defn generate-adds [n]
  (map #(generators/complete {:henkilotunnus %1
                              :email %2
                              :patevyystaso (rand-nth [1 2])}
                             laatija-schema/KayttajaLaatijaAdd)
       (generators/unique-henkilotunnukset n)
       (generators/unique-emails n)))

(defn dissoc-admin-update [update]
  (apply dissoc update (concat (keys laatija-schema/KayttajaAdminUpdate)
                               (keys laatija-schema/LaatijaAdminUpdate))))

(defn generate-updates [n include-admin-fields?]
  (map #(cond-> (generators/complete {:email %2
                                      :henkilotunnus %1
                                      :patevyystaso (rand-nth [1 2])
                                      :toimintaalue (rand-nth (range 0 18))}
                                     laatija-schema/KayttajaLaatijaUpdate)
          (not include-admin-fields?) dissoc-admin-update)
       (generators/unique-henkilotunnukset n)
       (generators/unique-emails n)))

(defn insert! [kayttaja-laatija-adds]
  (kayttaja-laatija-service/upsert-kayttaja-laatijat! ts/*db* kayttaja-laatija-adds))

(defn generate-and-insert!
  ([] (first (generate-and-insert! 1)))
  ([n]
   (let [kayttaja-laatija-adds (generate-adds n)]
     (zipmap (insert! kayttaja-laatija-adds) kayttaja-laatija-adds))))
