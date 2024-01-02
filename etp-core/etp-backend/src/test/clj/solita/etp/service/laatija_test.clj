(ns solita.etp.service.laatija-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [solita.etp.test-system :as ts]
            [solita.etp.test :as etp-test]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.service.whoami :as whoami-service]
            [solita.etp.service.laatija :as service]
            [solita.etp.service.viesti :as viesti-service]
            [solita.etp.whoami :as test-whoami]
            [solita.etp.service.kayttaja :as kayttaja-service])
  (:import (java.time LocalDate ZoneId Instant)))

(t/use-fixtures :each ts/fixture)

(defn test-data-set [logged-in-once? public-email-and-wwwosoite?]
  (let [laatijat (laatija-test-data/generate-and-insert! 200)]
    (doseq [id (keys laatijat)]
      (when logged-in-once?
        (whoami-service/update-kayttaja-with-whoami!
         ts/*db*
         {:id id :cognitoid nil}))
      (when public-email-and-wwwosoite?
        (service/update-laatija-by-id! ts/*db* id {:julkinenemail true
                                                   :julkinenpuhelin false
                                                   :julkinenwwwosoite true
                                                   :julkinenosoite false
                                                   :julkinenpostinumero false})))
    {:laatijat laatijat}))

(t/deftest public-laatija-test
  (let [laatija (-> (laatija-test-data/generate-adds 1)
                    first
                    (merge {:login               (Instant/now)
                            :voimassa            true
                            :laatimiskielto      false
                            :julkinenpuhelin     false
                            :julkinenemail       true
                            :julkinenosoite      false
                            :julkinenpostinumero false}))]
    (t/is (every? #(contains? (service/public-laatija laatija) %)
                  [:email :etunimi]) (str "Failure processing " (service/public-laatija laatija)))
    (t/is (not-every? #(contains? (service/public-laatija laatija) %)
                      [:puhelin :jakeluosoite :postinumero
                       :postitoimipaikka :maa]))
    (t/is (nil? (-> laatija
                    (assoc :laatimiskielto true)
                    service/public-laatija)))
    (t/is (nil? (-> laatija
                    (dissoc :voimassa)
                    service/public-laatija)))))

(defn common-find-all-assertions [laatija-adds found]
  (t/is (every? #(not (nil? %)) found))
  (t/is (= (set (map :sukunimi laatija-adds))
           (set (map :sukunimi found))))
  (t/is (every? #(-> % :aktiivinen nil? not) found)))

(defn paakayttaja-and-laskuttaja-find-all-assertions [laatija-adds found]
  (t/is (every? #(-> % :henkilotunnus count (= 11)) found))
  (t/is (every? #(contains? % :postitoimipaikka) found))
  (t/is (= (set (map :henkilotunnus laatija-adds))
           (set (map :henkilotunnus found)))))

(t/deftest find-all-laatijat-as-paakayttaja-test
  (let [{:keys [laatijat]} (test-data-set true true)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/paakayttaja)]
    (common-find-all-assertions (vals laatijat) found)
    (paakayttaja-and-laskuttaja-find-all-assertions (vals laatijat) found)))

(t/deftest find-all-laatijat-as-laskuttaja-test
  (let [{:keys [laatijat]} (test-data-set true true)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/laskuttaja)]
    (common-find-all-assertions (vals laatijat) found)
    (paakayttaja-and-laskuttaja-find-all-assertions (vals laatijat) found)))

(t/deftest find-all-laatijat-as-patevyyden-toteaja-test
  (let [{:keys [laatijat]} (test-data-set true true)
        found (service/find-all-laatijat
               ts/*db*
               kayttaja-test-data/patevyyden-toteaja)]
    (common-find-all-assertions (vals laatijat) found)
    (t/is (every? #(-> % :henkilotunnus count (= 6)) found))
    (t/is (every? #(-> % :henkilotunnus (str/includes? "-") not) found))
    (t/is (every? #(contains? % :postitoimipaikka) found))))

(t/deftest find-all-laatijat-as-public-test
  (let [{:keys [laatijat]} (test-data-set true true)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/public)]
    (common-find-all-assertions (vals laatijat) found)
    (t/is (every? #(-> % (contains? :henkilotunnus) not) found))
    (t/is (every? #(-> % (contains? :puhelin) not) found))
    (t/is (every? #(-> % (contains? :jakeluosoite) not) found))
    (t/is (every? #(-> % (contains? :postinumero) not) found))
    (t/is (every? #(-> % (contains? :postitoimipaikka) not) found))
    (t/is (every? #(-> % (contains? :maa) not) found))
    (t/is (= (set (map :email (vals laatijat)))
             (set (map :email found))))
    (t/is (= (set (map :wwwosoite laatijat))
             (set (map :wwwosoite found))))))

(defn find-all-laatijat-not-public-assertions [laatija-adds found]
  (t/is (= (set (map #(select-keys
                       %
                       [:etunimi :sukunimi :email :puhelin :jakeluosoite])
                     laatija-adds))
           (set (map #(select-keys
                       %
                       [:etunimi :sukunimi :email :puhelin :jakeluosoite])
                     found))))
  (t/is (every? #(-> % :aktiivinen false?) found))
  (t/is (every? #(-> % :login nil?) found)))

(t/deftest find-all-laatijat-as-paakayttaja-not-public-test
  (let [{:keys [laatijat]} (test-data-set false false)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/paakayttaja)]
    (find-all-laatijat-not-public-assertions (vals laatijat) found)))

(t/deftest find-all-laatijat-as-laskuttaja-not-public-test
  (let [{:keys [laatijat]} (test-data-set false false)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/laskuttaja)]
    (find-all-laatijat-not-public-assertions (vals laatijat) found)))

(t/deftest find-all-laatijat-as-patevyyden-toteaja-not-public-test
  (let [{:keys [laatijat]} (test-data-set false false)
        found (service/find-all-laatijat
               ts/*db*
               kayttaja-test-data/patevyyden-toteaja)]
    (find-all-laatijat-not-public-assertions (vals laatijat) found)))

(t/deftest find-all-laatijat-as-public-not-public-test
  (let [_ (test-data-set false false)
        found (service/find-all-laatijat ts/*db* kayttaja-test-data/public)]
    (t/is (empty? found))))

;; TODO test for finding, attaching and detaching yritys from laatija

(t/deftest find-patevyydet-test
  (let [patevyydet (service/find-patevyystasot ts/*db*)
        fi-labels  (map :label-fi patevyydet)
        se-labels  (map :label-sv patevyydet)]
    (t/is (= ["Perustaso" "Ylempi taso"] fi-labels))
    (t/is (= ["Basnivå" "Högre nivå"] se-labels))))

(t/deftest validate-laatija-patevyys!-test
  (let [{:keys [laatijat]} (test-data-set false false)]
    (doseq [id (keys laatijat)]
      (service/update-laatija-by-id! ts/*db*
                                     id
                                     {:laatimiskielto false
                                      :toteamispaivamaara (LocalDate/now)})
      (t/is (nil? (service/validate-laatija-patevyys! ts/*db* id)))

      (service/update-laatija-by-id! ts/*db*
                                     id {:laatimiskielto true
                                         :toteamispaivamaara (LocalDate/now)})
      (t/is (= (etp-test/catch-ex-data-no-msg
                #(service/validate-laatija-patevyys! ts/*db* id))
               {:type :laatimiskielto})))))

(defn patevyys-paattymisaika [toteamispaivamaara]
  (-> toteamispaivamaara
      (.plusYears 7)
      (.plusDays 1)
      (.atStartOfDay (ZoneId/of "Europe/Helsinki"))
      (.toInstant)))

(t/deftest validate-laatija-patevyys!-expired-test
  (let [{:keys [laatijat]} (test-data-set false false)
        expiring-today (.minusYears (LocalDate/now) 7)
        expired-3-years-ago (.minusYears (LocalDate/now) 10)
        expired-yesterday (-> (LocalDate/now) (.minusYears 7) (.minusDays 1))]
    (doseq [id (keys laatijat)]
      (service/update-laatija-by-id! ts/*db*
                                     id
                                     {:laatimiskielto false
                                      :toteamispaivamaara expiring-today})
      (t/is (nil? (service/validate-laatija-patevyys! ts/*db* id)))

      (service/update-laatija-by-id! ts/*db*
                                     id
                                     {:laatimiskielto false
                                      :toteamispaivamaara expired-3-years-ago})
      (t/is (= (etp-test/catch-ex-data-no-msg
                #(service/validate-laatija-patevyys! ts/*db* id))
               {:type          :patevyys-expired
                :paattymisaika (patevyys-paattymisaika expired-3-years-ago)}))

      (service/update-laatija-by-id!
       ts/*db* id {:laatimiskielto false
                   :toteamispaivamaara expired-yesterday})
      (t/is (= (etp-test/catch-ex-data-no-msg
                #(service/validate-laatija-patevyys! ts/*db* id))
               {:type          :patevyys-expired
                :paattymisaika (patevyys-paattymisaika expired-yesterday)})))))

(t/deftest send-patevyys-expiration-message!
  (let [paakayttaja-id (kayttaja-test-data/insert-paakayttaja!)
        [id _] (laatija-test-data/generate-and-insert!)
        ^LocalDate now (LocalDate/now)
        system-id (kayttaja-service/system-kayttaja :communication)
        options {:months-before-expiration 6 :fallback-window 5}]
    (service/update-laatija-by-id!
      ts/*db* id
      {:toteamispaivamaara (-> now (.minusYears 7) (.plusMonths 6))})

    (service/send-patevyys-expiration-messages! (ts/db-user system-id) options)
    ;; second time should not send anything
    (service/send-patevyys-expiration-messages! (ts/db-user system-id) options)

    ;; assert viesti
    (let [viestiketjut (viesti-service/find-ketjut
                         ts/*db* (test-whoami/paakayttaja paakayttaja-id)
                         {:include-kasitelty true})
          viestiketju (first viestiketjut)
          vastaanottaja (-> viestiketju :vastaanottajat first)
          viesti (-> viestiketju :viestit first)]
      (t/is (= (count viestiketjut) 1))
      (t/is (= (-> viestiketju :vastaanottajat count) 1))
      (t/is (= (-> viestiketju :viestit count) 1))
      (t/is (= (:id vastaanottaja) id))
      (t/is (= (-> viesti :from :id) system-id))
      (t/is (some? (:body viesti))))))
