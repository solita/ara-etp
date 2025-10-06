(ns solita.etp.test-data.perusparannuspassi
  (:require [schema-generators.generators :as g]
            [clojure.test.check.generators :as test-generators]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.test-system :as ts]
            [solita.etp.schema.perusparannuspassi :as perusparannuspassi-schema]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi-service]))

(def vaihe-nro-gen
  (test-generators/choose 1 4))

(def vaihe-gen
  (test-generators/let [n vaihe-nro-gen
                        v test-generators/boolean]
                       {:vaihe-nro n
                        :valid     v}))

;; Generate 0..4 unique phases (distinct vaihe-nro, optional random order)
(def vaiheet-gen
  (test-generators/let [ns (g/always [1 2 3 4])
                        bs (test-generators/vector test-generators/boolean (count ns))]
                       (->> (map vector ns bs)
                            (mapv (fn [[n b]] {:vaihe-nro n :valid b})))))

(def perusparannuspassi-save-gen
  "Generates a PerusparannupassiSave without energiatodistus-id."
  (test-generators/let [valid (g/always true)
                        vaiheet vaiheet-gen]
    {:valid   valid
     :vaiheet vaiheet}))

(def generators {perusparannuspassi-schema/PerusparannuspassiSave perusparannuspassi-save-gen
                 [perusparannuspassi-schema/PerusparannuspassiVaihe] vaiheet-gen})

(defn generate-add
  "Generates a perusparannuspassi that can be added.

  Parameters:
  - `energiatodistus-id` - ppp can only be created for an existing et"
  [energiatodistus-id]
  (generators/complete
    {:energiatodistus-id energiatodistus-id
     :valid true
     :passin-perustiedot {:havainnointikaynti nil
                          :passin-esittely nil
                          :tayttaa-a0-vaatimukset false
                          :tayttaa-aplus-vaatimukset false}
     :vaiheet            [{:vaihe-nro 1
                          :valid true
                          :toimenpiteet {:toimenpideseloste-fi nil
                                         :toimenpideseloste-sv nil
                                         :toimenpide-ehdotukset [ ]}
                          :tulokset {:vaiheen-alku-pvm nil
                                     :vaiheen-loppu-pvm nil
                                     :ostoenergian-tarve-kaukolampo 1
                                     :ostoenergian-tarve-sahko 1
                                     :ostoenergian-tarve-uusiutuvat-pat 1
                                     :ostoenergian-tarve-fossiiliset-pat 1
                                     :ostoenergian-tarve-kaukojaahdytys 1
                                     :uusiutuvan-energian-kokonaistuotto 1
                                     :uusiutuvan-energian-hyodynnetty-osuus 1
                                     :toteutunut-ostoenergia-kaukolampo 1
                                     :toteutunut-ostoenergia-sahko 1
                                     :toteutunut-ostoenergia-uusiutuvat-pat 1
                                     :toteutunut-ostoenergia-fossiiliset-pat 1
                                     :toteutunut-ostoenergia-kaukojaahdytys 1}}]
     :rakennuksen-perustiedot {:ulkoseinat-ehdotettu-taso 1
                               :ylapohja-ehdotettu-taso 1
                               :alapohja-ehdotettu-taso 1
                               :ikkunat-ehdotettu-taso 1
                               :paalammitysjarjestelma-ehdotettu-taso 1
                               :ilmanvaihto-ehdotettu-taso 1
                               :uusiutuva-energia-ehdotettu-taso 1
                               :jaahdytys-ehdotettu-taso 1
                               :mahdollisuus-liittya-energiatehokkaaseen 1}
     :tulokset {:kaukolampo-hinta 1
                :kaukojaahdytys-hinta 1
                :sahko-hinta 1
                :uusiutuvat-pat-hinta 1
                :fossiiliset-pat-hinta 1
                :lisatiedot-fi nil
                :lisatiedot-sv nil}}
    perusparannuspassi-schema/PerusparannuspassiSave
    generators))

(defn generate-adds [energiatodistus-ids]
  (map generate-add energiatodistus-ids))

(defn insert! [perusparannuspassi-adds whoami]
  (mapv #(:id (perusparannuspassi-service/insert-perusparannuspassi!
               (ts/db-user (:id whoami))
               whoami
               %)) perusparannuspassi-adds))

(defn generate-and-insert!
  [energiatodistus-ids laatija-id]
   (let [perusparannuspassi-adds (generate-adds energiatodistus-ids)]
     (insert! perusparannuspassi-adds laatija-id)))

