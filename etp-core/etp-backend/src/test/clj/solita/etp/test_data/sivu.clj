(ns solita.etp.test-data.sivu
  (:require [solita.etp.schema.sivu :as sivu-schema]
            [solita.etp.service.sivu :as sivu-service]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]))

(defn generate-adds [n parent-id]
  (map #(generators/complete {:ordinal %
                              :parent-id parent-id}
                             sivu-schema/SivuSave) (range n)))

(defn insert! [sivu-adds]
  (mapv #(sivu-service/add-sivu! ts/*db* %) sivu-adds))

(defn generate-and-insert! [num-pages subs parent-id f]
  (let [sivut (->> (generate-adds num-pages parent-id)
                  (map f))
        sivu-ids (insert! sivut)
        this-level (mapv merge sivu-ids sivut)]
    (if (>= 0 subs)
      this-level
      (vec (reduce concat
                   this-level
                   (mapv #(generate-and-insert! num-pages
                                                (dec subs)
                                                (:id %)
                                                f)
                         sivu-ids))))))
