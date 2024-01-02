(ns solita.etp.api.energiatodistus-xml-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [solita.common.xml-test :as xml-test]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.api.energiatodistus-xml :as xml-api]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  (let [laatijat (laatija-test-data/generate-and-insert! 1)
        laatija-id (-> laatijat keys sort first)]
    {:laatija-id laatija-id}))

(t/deftest xml->energiatodistus-test
  (let [energiatodistus (xml-api/xml->energiatodistus xml-test/xml-without-soap-envelope)]
    (t/is (= "Kilpikonna" (get-in energiatodistus [:perustiedot :nimi])))
    (t/is (= 0.5 (get-in energiatodistus [:lahtotiedot :rakennusvaippa :ikkunat :U])))
    (t/is (= 14.0 (get-in energiatodistus [:lahtotiedot :sis-kuorma :valaistus :lampokuorma])))
    (t/is (= 0.5 (get-in energiatodistus [:tulokset :tekniset-jarjestelmat :jaahdytys :kaukojaahdytys])))
    (t/is (empty? (get-in energiatodistus [:toteutunut-ostoenergiankulutus :muu])))
    (t/is (= "p:Nimi" (get-in energiatodistus [:huomiot :lammitys :toimenpide 0 :nimi-fi])))))

(t/deftest xml-post-test
  (let [{:keys [laatija-id]} (test-data-set)
        response
        (with-open [body (-> "legacy-api/esimerkki-2018.xml" io/resource io/input-stream)]
          (let [handler (xml-api/handle-post 2018)
                request {:db ts/*db*
                         :whoami {:id laatija-id :rooli 0}
                         :body body}]
            (handler request)))]
    (t/is (= 200 (:status response)))
    (t/is (= 1 (-> (re-matches #".*<b:TodistusTunnus>(\d+)</b:TodistusTunnus>.*" (:body response)) second Integer/parseInt)))))

(t/deftest xml-post-sansbody-test
  (let [{:keys [laatija-id]} (test-data-set)
        response
        (let [handler (xml-api/handle-post 2018)
              request {:db ts/*db*
                       :whoami {:id laatija-id :rooli 0}
                       :body nil}]
          (handler request))]
    (t/is (= 400 (:status response)))))
