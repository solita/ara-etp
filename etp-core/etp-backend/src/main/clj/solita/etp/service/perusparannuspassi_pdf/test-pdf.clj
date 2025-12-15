(ns user
  (:require [solita.etp.service.perusparannuspassi-pdf :as ppp-pdf]
            [solita.etp.service.e-luokka :as e-luokka-service]
            [clojure.java.io :as io])
  (:import [java.time LocalDate]))

(def out-file "ppp-preview.pdf")

;; Mock Energiatodistus data
(def mock-et
  {:id "ET-123456"
   :laatija-fullname "Matti Meikäläinen"
   :allekirjoitusaika (LocalDate/of 2023 1 1)
   :perustiedot {:nimi-fi "Esimerkkitalo Oy"
                 :nimi-sv "Exempelhus Ab"
                 :katuosoite-fi "Esimerkkikatu 1"
                 :katuosoite-sv "Exempelgatan 1"
                 :rakennustunnus "1234567890"
                 :havainnointikaynti (LocalDate/of 2023 1 1)
                 :kayttotarkoitus 1}
   :tulokset {:e-luku 100 :e-luokka "C"}
   :lahtotiedot {:lammitetty-nettoala 100}})

;; Mock Perusparannuspassi data
(def mock-ppp
  {:id "PPP-98765"
   :passin-perustiedot {:passin-esittely (LocalDate/of 2023 2 1)
                        :tayttaa-a0-vaatimukset false
                        :tayttaa-aplus-vaatimukset false}
   :vaiheet [{:vaihe-nro 1
              :valid true
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2024 1 1)}
              :toimenpiteet {:toimenpide-ehdotukset [{:id 1 :label-fi "Yläpohjan lisäeristys" :label-sv "Tilläggsisolering av vindsbjälklag"}
                                                     {:id 2 :label-fi "Tiiveyden parantaminen" :label-sv "Förbättring av täthet"}
                                                     {:id 3 :label-fi "Aurinkosähkö" :label-sv "Solel"}]}}
             {:vaihe-nro 2
              :valid true
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2025 1 1)}
              :toimenpiteet {:toimenpide-ehdotukset [{:id 4 :label-fi "Ikkunoiden uusiminen" :label-sv "Förnyande av fönster"}]}}
             {:vaihe-nro 3
              :valid true
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2026 1 1)}
              :toimenpiteet {:toimenpide-ehdotukset []}}
             {:vaihe-nro 4
              :valid true
              :tulokset {:vaiheen-alku-pvm (LocalDate/of 2027 1 1)}
              :toimenpiteet {:toimenpide-ehdotukset []}}]})

;; Mock classification data
(def mock-alakayttotarkoitukset
  [{:id 1 :label-fi "Asuinkerrostalot" :label-sv "Flerbostadshus"}])

(def params
  {:energiatodistus mock-et
   :perusparannuspassi mock-ppp
   :kieli :fi
   :alakayttotarkoitukset mock-alakayttotarkoitukset
   :kayttotarkoitukset []
   :output-stream (io/output-stream out-file)})

;; Generate PDF with mocked e-luokka calculations to avoid complex dependencies
(with-redefs [e-luokka-service/e-luku-from-ppp-vaihe
              (fn [_ _ vaihe]
                (case (:vaihe-nro vaihe)
                  1 70
                  2 90
                  3 120
                  4 150
                  100))

              e-luokka-service/e-luokka
              (fn [_ _ _ _ _ e-luku]
                {:e-luokka (cond
                             (<= e-luku 75) "A"
                             (<= e-luku 100) "B"
                             (<= e-luku 130) "C"
                             (<= e-luku 160) "D"
                             :else "E")})]
  (ppp-pdf/generate-perusparannuspassi-pdf params))

(println "PDF generated to:" (.getAbsolutePath (io/file out-file)))