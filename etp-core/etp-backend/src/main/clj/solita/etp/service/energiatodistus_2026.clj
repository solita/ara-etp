(ns solita.etp.service.energiatodistus-2026)

(def toimenpide-ehdotukset-hidden-e-luokat #{"A" "A0" "A+"})

(defn show-toimenpide-pages? [energiatodistus]
  (let [e-luokka (-> energiatodistus :tulokset :e-luokka)
        has-valid-ppp? (-> energiatodistus :perusparannuspassi-valid)
        laatimisvaihe (-> energiatodistus :perustiedot :laatimisvaihe)]
    (and (= 2 laatimisvaihe)
         (not (contains? toimenpide-ehdotukset-hidden-e-luokat e-luokka))
         (not has-valid-ppp?))))
