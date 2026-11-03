(ns solita.etp.service.pdf-heading-hierarchy-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [solita.etp.pdf-headings :as pdf-headings]
            [solita.etp.service.complete-energiatodistus :as complete-energiatodistus]
            [solita.etp.service.energiatodistus-pdf :as energiatodistus-pdf]
            [solita.etp.service.perusparannuspassi :as perusparannuspassi]
            [solita.etp.test-data.energiatodistus :as energiatodistus-test-data]
            [solita.etp.test-data.laatija :as laatija]
            [solita.etp.test-system :as ts])
  (:import (java.time LocalDate)))

(t/use-fixtures :each ts/fixture)

(def ppp-add
  {:valid true
   :energiatodistus-id nil
   :passin-perustiedot {:havainnointikaynti (LocalDate/of 2026 11 1)
                         :passin-esittely (LocalDate/of 2026 11 2)
                         :tayttaa-a0-vaatimukset false
                         :tayttaa-aplus-vaatimukset false
                         :lisatietoja-fi nil :lisatietoja-sv nil}
   :vaiheet [{:vaihe-nro 1 :valid true
              :toimenpiteet {:toimenpideseloste-fi nil
                             :toimenpideseloste-sv nil
                             :toimenpide-ehdotukset []}
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2030 1 1)
                         :vaiheen-loppu-pvm nil
                         :ostoenergian-tarve-kaukolampo 35000M
                         :ostoenergian-tarve-sahko 8000M
                         :ostoenergian-tarve-uusiutuvat-pat 0M
                         :ostoenergian-tarve-fossiiliset-pat 0M
                         :ostoenergian-tarve-kaukojaahdytys 0M
                         :uusiutuvan-energian-kokonaistuotto 2000M
                         :uusiutuvan-energian-hyodynnetty-osuus 5M
                         :toteutunut-ostoenergia-kaukolampo 33000M
                         :toteutunut-ostoenergia-sahko 7000M
                         :toteutunut-ostoenergia-uusiutuvat-pat 0M
                         :toteutunut-ostoenergia-fossiiliset-pat 0M
                         :toteutunut-ostoenergia-kaukojaahdytys 0M}}]
   :rakennuksen-perustiedot {:e-luvun-vahimmaistavoitetaso 80
                             :ulkoovet-ehdotettu-taso 1M
                             :ikkunat-ehdotettu-taso 0.8M
                             :paalammitysjarjestelma-ehdotettu-taso 0
                             :jaahdytys-ehdotettu-taso nil
                             :alapohja-ehdotettu-taso 0.15M
                             :uusiutuva-energia-lahtotilanne 0
                             :uusiutuva-energia-ehdotettu-taso 1
                             :ulkoseinat-ehdotettu-taso 0.17M
                             :ylapohja-ehdotettu-taso 0.09M
                             :mahdollisuus-liittya-energiatehokkaaseen 0
                             :ilmanvaihto-ehdotettu-taso 8
                             :lisatietoja-fi nil
                             :lisatietoja-sv nil}
   :tulokset {:kaukolampo-hinta 10M
              :kaukojaahdytys-hinta 18M
              :sahko-hinta 15M
              :uusiutuvat-pat-hinta 8M
              :fossiiliset-pat-hinta 14M
              :lisatietoja-fi nil
              :lisatietoja-sv nil}})

(defn- flatten-headings [headings]
  (mapcat (fn [{:keys [type children]}]
            (cons type (flatten-headings children)))
          headings))

(t/deftest energiatodistus-2026-with-perusparannuspassi-heading-hierarchy-test
  (let [laatija-id (laatija/insert-suomifi-laatija!
                     {:etunimi "Liisa"
                      :sukunimi "Specimen-Potex"
                     :puhelin "0501234567"
                     :patevyystaso 4
                     :jakeluosoite "Testikatu 1"
                     :postinumero "00100"
                     :postitoimipaikka "Helsinki"
                     :maa "FI"
                     :toteamispaivamaara (LocalDate/of 2025 1 1)
                     :toteaja "ARA"})
        whoami {:id laatija-id :rooli 0 :patevyystaso 4}
        et-id (first (energiatodistus-test-data/generate-and-insert!
                       2026 true laatija-id))
        _ (perusparannuspassi/insert-perusparannuspassi!
            (ts/db-user laatija-id)
            whoami
            (assoc ppp-add :energiatodistus-id et-id))
        complete-et (complete-energiatodistus/find-complete-energiatodistus
                      ts/*db* whoami et-id)
        pdf-path (energiatodistus-pdf/generate-et-pdf-as-file
                   ts/*db* whoami complete-et "fi" true "heading-hierarchy")]
    (try
      (let [heading-tree (:headings (pdf-headings/extract-heading-tree pdf-path))]
        (t/is (= ["H1" ; ET: Energiatodistus
                  "H2" ; ET: Lämmitysjärjestelmän kuvaus
                  "H2" ; ET: Ilmanvaihtojärjestelmän kuvaus
                  "H2" ; ET: Toteutunut ostoenergiankulutus ja tuotettu uusiutuva energia
                  "H2" ; ET: Keskeiset toimenpiteet rakennuksen E-luvun parantamiseksi
                  "H2" ; ET: E-luvun laskennan lähtötiedot
                  "H3" ; ET: Rakennusvaippa
                  "H3" ; ET: Ilmanvaihtojärjestelmä
                  "H3" ; ET: Lämmitysjärjestelmä
                  "H3" ; ET: Jäähdytysjärjestelmä
                  "H3" ; ET: Sisäiset lämpökuormat eri käyttöasteilla
                  "H2" ; ET: E-luvun laskennan tulokset
                  "H3" ; ET: E-luku
                  "H3" ; ET: Rakennuksen hyödyntämä ja tuottama uusiutuva energia
                  "H3" ; ET: Rakennuksen teknisten järjestelmien energiankulutus
                  "H3" ; ET: Energian nettotarve
                  "H3" ; ET: Lämpökuormat
                  "H2" ; ET: Lisämerkintöjä
                  "H3" ; ET: Lisätietoja energiatehokkuudesta
                  "H2" ; ET: Hiilijalanjälki ja hiilikädenjälki
                  "H3" ; ET: Hiilijalanjälki
                  "H3" ; ET: Hiilikädenjälki
                  "H1" ; PPP: Perusparannuspassi
                  "H2" ; PPP: Perusparannuspassissa ehdotettujen toimenpiteiden vaikutukset
                  "H3" ; PPP: Energiatehokkuusluokan ja E-luvun muutos
                  "H3" ; PPP: Toimenpiteiden kohdistuminen rakennuksen osa-alueisiin
                  "H3" ; PPP: Rakennus ehdotettujen toimenpiteiden jälkeen
                  "H2" ; PPP: Vaiheessa 1 toteutettavat toimenpiteet
                  "H3" ; PPP: Toimenpide-ehdotukset 2030–2050
                  "H3" ; PPP: Toimenpideseloste
                  "H3" ; PPP: Energiankulutus vaiheen jälkeen
                  "H3" ; PPP: Energiankulutus, kustannukset ja kasvihuonepäästöt vaiheen jälkeen
                  "H2" ; PPP: Vaiheistuksen yhteenveto
                  "H2" ; PPP: Laskennan taustatiedot
                  "H2"] ; PPP: Lisätietoja
                 (vec (flatten-headings heading-tree)))))
      (finally
        (io/delete-file pdf-path true)))))
