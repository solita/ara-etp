(ns solita.etp.perusparannuspassi-hiccup
  (:require [solita.etp.service.complete-energiatodistus :as complete-energiatodistus-service]
            [solita.etp.service.complete-perusparannuspassi :as complete-ppp-service]
            [solita.etp.service.kayttotarkoitus :as kayttotarkoitus-service]
            [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf]
            [solita.etp.service.toimenpide-ehdotus :as toimenpide-ehdotus-service]))

(defn generate-perusparannuspassi-hiccup-from-db
  "Load a perusparannuspassi and its related data, then generate its Hiccup.
   The three-argument arity generates Finnish content."
  ([db whoami perusparannuspassi-id]
   (generate-perusparannuspassi-hiccup-from-db db whoami perusparannuspassi-id :fi))
  ([db whoami perusparannuspassi-id kieli]
   (let [perusparannuspassi (complete-ppp-service/find-complete-perusparannuspassi
                              db whoami perusparannuspassi-id)
         energiatodistus (complete-energiatodistus-service/find-complete-energiatodistus
                           db whoami (:energiatodistus-id perusparannuspassi))
         versio (:versio energiatodistus)
         luokittelut {:kayttotarkoitukset    (kayttotarkoitus-service/find-kayttotarkoitukset db versio)
                      :alakayttotarkoitukset (kayttotarkoitus-service/find-alakayttotarkoitukset db versio)
                      :mahdollisuus-liittya  (luokittelu-service/find-mahdollisuus-liittya db)
                      :uusiutuva-energia     (luokittelu-service/find-uusiutuva-energia db)
                      :lammitysmuodot        (luokittelu-service/find-lammitysmuodot db)
                      :ilmanvaihtotyypit     (luokittelu-service/find-ilmanvaihtotyypit db)
                      :toimenpide-ehdotukset (toimenpide-ehdotus-service/find-all db)}]
     (ppp-pdf/generate-perusparannuspassi-hiccup
       (merge luokittelut
              {:energiatodistus    energiatodistus
               :perusparannuspassi perusparannuspassi
               :kieli              (keyword kieli)})))))
