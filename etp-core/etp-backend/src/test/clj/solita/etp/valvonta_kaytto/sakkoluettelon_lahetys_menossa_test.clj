(ns solita.etp.valvonta-kaytto.sakkoluettelon-lahetys-menossa-test
  (:require
    [clojure.test :as t]
    [jsonista.core :as j]
    [ring.mock.request :as mock]
    [solita.etp.service.valvonta-kaytto :as valvonta-service]
    [solita.etp.test-data.kayttaja :as test-kayttajat]
    [solita.etp.test-system :as ts])
  (:import (java.time LocalDate)))

(t/use-fixtures :each ts/fixture)

(t/deftest sakkopaatos-ensimmainen-postitus-test
  (test-kayttajat/insert-virtu-paakayttaja!
    {:etunimi  "Asian"
     :sukunimi "Tuntija"
     :email    "testi@ara.fi"
     :puhelin  "0504363675457"})
  (t/testing "Sakkoluettelon lähetys menossa -toimenpide is created successfully"
    (let [valvonta-id (valvonta-service/add-valvonta! ts/*db*
                                                      {:katuosoite        "Testitie 5"
                                                       :postinumero       "90100"
                                                       :ilmoituspaikka-id 0})
          new-toimenpide {:type-id       21
                          :deadline-date (str (LocalDate/of 2023 11 4))
                          :template-id   nil
                          :description   "Sakkoluettelo täytetään ja allekirjoitus tapahtuu tämän järjestelmän ulkopuolella. Sakkoluettelo lähetetään sähköpostitse seuraamusmaksut.ork (at) om.fi.sec"}
          response (ts/handler (-> (mock/request :post (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                   (mock/json-body new-toimenpide)
                                   (test-kayttajat/with-virtu-user)
                                   (mock/header "Accept" "application/json")))]
      (t/is (= (:status response) 201))

      (t/testing "Toimenpide is returned through the api"
        (let [response (ts/handler (-> (mock/request :get (format "/api/private/valvonta/kaytto/%s/toimenpiteet" valvonta-id))
                                       (test-kayttajat/with-virtu-user)
                                       (mock/header "Accept" "application/json")))
              response-body (j/read-value (:body response) j/keyword-keys-object-mapper)]
          (t/is (= (:status response) 200))
          (t/is (= (count response-body) 1))
          (t/is (= (-> response-body
                       first
                       (dissoc :publish-time :create-time))
                   {:author             {:etunimi  "Asian"
                                         :id       1
                                         :rooli-id 2
                                         :sukunimi "Tuntija"}
                    :deadline-date      "2023-11-04"
                    :description        "Sakkoluettelo täytetään ja allekirjoitus tapahtuu tämän järjestelmän ulkopuolella. Sakkoluettelo lähetetään sähköpostitse seuraamusmaksut.ork (at) om.fi.sec"
                    :diaarinumero       nil
                    :filename           nil
                    :henkilot           []
                    :id                 1
                    :template-id        nil
                    :type-id            21
                    :type-specific-data nil
                    :valvonta-id        1
                    :yritykset          []})))))))
