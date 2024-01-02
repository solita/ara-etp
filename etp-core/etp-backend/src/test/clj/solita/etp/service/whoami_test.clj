(ns solita.etp.service.whoami-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [schema.core :as schema]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.service.whoami :as service]
            [solita.etp.service.kayttaja :as kayttaja-service]
            [solita.etp.schema.kayttaja :as kayttaja-schema]
            [solita.etp.schema.laatija :as laatija-schema]))

(t/use-fixtures :each ts/fixture)

(defn test-data-set []
  {:kayttajat (kayttaja-test-data/generate-and-insert! 100)})

(t/deftest verified-api-key?-test
  (let [api-key "api-key-123"
        api-key-hash "bcrypt+sha512$0a550ff24167fd8c9262e8f9084c91f3$12$828a5cf28a7b0238a671c90c7bb8a14a0a3f70a3afdef207"]
    (t/is (false? (service/verified-api-key? nil nil)))
    (t/is (false? (service/verified-api-key? nil api-key-hash)))
    (t/is (false? (service/verified-api-key? api-key nil)))
    (t/is (true? (service/verified-api-key? api-key api-key-hash)))
    (t/is (false? (service/verified-api-key?
                   api-key
                   (str/replace api-key-hash #"7" "0"))))))

(t/deftest update-kayttaja-with-whoami!-test
  (let [{:keys [kayttajat]} (test-data-set)]
    (doseq [id (-> kayttajat keys sort)
            :let [found-before (kayttaja-service/find-kayttaja
                                ts/*db*
                                kayttaja-test-data/paakayttaja
                                id)
                  cognitoid (str "cognitoid-" (rand-int 1000000))
                  virtulocalid (:email found-before)
                  virtuorganisaatio "organisaatio"
                  _ (service/update-kayttaja-with-whoami! ts/*db*
                                                          {:id id
                                                           :cognitoid cognitoid})
                  found-after (kayttaja-service/find-kayttaja
                               ts/*db*
                               kayttaja-test-data/paakayttaja
                               id)]]
      (schema/validate kayttaja-schema/Kayttaja found-after)
      (t/is (-> found-before :login nil?))
      (t/is (-> found-after :login nil? not))
      (t/is (-> found-after :email (:email found-before)))
      (t/is (-> found-before :cognitoid nil?))
      (t/is (= cognitoid (:cognitoid found-after)))
      (t/is (-> found-before :virtu :localid nil?))
      (t/is (-> found-before :virtu :organisaatio nil?)))))
