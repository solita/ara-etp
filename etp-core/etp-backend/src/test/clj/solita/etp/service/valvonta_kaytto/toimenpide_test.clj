(ns solita.etp.service.valvonta-kaytto.toimenpide-test
  (:require [clojure.test :as t]
            [solita.etp.service.valvonta-kaytto.toimenpide :as toimenpide]
            [solita.etp.service.valvonta-kaytto :as valvonta-kaytto]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(t/deftest type-key-test
  (t/testing "type-id 0 returns the type-key :case"
    (t/is (= (toimenpide/type-key 0)
             :case)))
  (t/testing "type-id 1 return the type-key :rfi-request"
    (t/is (= (toimenpide/type-key 1)
             :rfi-request)))

  (t/testing "type-id 2 return the type-key :rfi-order"
    (t/is (= (toimenpide/type-key 2)
             :rfi-order)))

  (t/testing "type-id 3 return the type-key :rfi-warning"
    (t/is (= (toimenpide/type-key 3)
             :rfi-warning)))

  (t/testing "type-id 4 return the type-key :decision-order"
    (t/is (= (toimenpide/type-key 4)
             :decision-order)))

  (t/testing "type-id 5 return the type-key :closed"
    (t/is (= (toimenpide/type-key 5)
             :closed)))

  (t/testing "type-id 7 return the type-key :decision-order-hearing-letter"
    (t/is (= (toimenpide/type-key 7)
             :decision-order-hearing-letter)))

  (t/testing "unknown type-id results in an exception"
    (t/is (thrown? Exception (toimenpide/type-key 666)))))

(t/deftest manually-deliverable?-test
  (t/testing "Toimenpide-type 7 document is sent manually"
    (t/is (true? (toimenpide/manually-deliverable? ts/*db* 7))))

  (t/testing "Toimenpide-types 0 - 6 are not sent manually"
    (doseq [toimenpide-type (range 7)]
      (t/is (false? (toimenpide/manually-deliverable? ts/*db* toimenpide-type))))))

(t/deftest kaskypaatos-toimenpide?-test
  (t/testing "Käskypäätös / kuulemiskirje is recognized as kaskypäätös-toimenpide?"
    (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 7}))))

  (t/testing "Käskypäätös / varsinainen päätös is recognized as kaskypäätös-toimenpide?"
    (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 8}))))

  (t/testing "Toimepidetypes with id 0-6 are not recognized as kaskypäätös-toimenpide?"
    (doseq [toimenpide-type (range 7)]
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id toimenpide-type}))))))

(t/deftest sakkopaatos-toimenpide?-test
  (t/testing "Sakkopäätös / kuulemiskirje is recognized as sakkopäätös-toimenpide?"
    (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 14}))))
  (t/testing "Sakkopäätös / varsinainen päätös is recognized as sakkopäätös-toimenpide?"
    (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 15}))))
  (t/testing "Käskypäätös / kuulemiskirje is not recognized as sakkopäätös-toimenpide?"
    (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 7})))))

(t/deftest all-toimenpiteet-test
  (t/testing "All toimenpiteet"
    (let [type-ids-from-database (map :id (valvonta-kaytto/find-toimenpidetyypit ts/*db*))
          type-key-exists-and-is-keyword? #(keyword? (toimenpide/type-key %1))
          type-keys-that-exist (->> type-ids-from-database
                                    (filter type-key-exists-and-is-keyword?)
                                    (map toimenpide/type-key))]
      (t/testing "type-key keywords exist for all ids in the database"
        (doseq [id type-ids-from-database]
          (t/testing (str "Toimenpide with id " id " in database has a type-key keyword")
            (t/is (true? (type-key-exists-and-is-keyword? id))))))
      (t/testing "type-keys are distinct" (t/is (true? (apply distinct? type-keys-that-exist))))))

  (t/testing "Valvonnan aloitus"
    (t/testing "has type-id 0 and type-key :case"
      (t/is (= (toimenpide/type-key 0) :case)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 0}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 0}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 0})))))

  (t/testing "Tietopyyntö 2021"
    (t/testing "has type-id 1 and type-key :rfi-request"
      (t/is (= (toimenpide/type-key 1) :rfi-request)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 1}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 1}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 1})))))

  (t/testing "Kehotus"
    (t/testing "has type-id 2 and type-key :rfi-order"
      (t/is (= (toimenpide/type-key 2) :rfi-order)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 2}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 2}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 2})))))

  (t/testing "Varoitus"
    (t/testing "has type-id 3 and type-key :rfi-warning"
      (t/is (= (toimenpide/type-key 3) :rfi-warning)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 3}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 3}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 3})))))

  (t/testing "Käskypäätös"
    (t/testing "has type-id 4 and type-key :decision-order"
      (t/is (= (toimenpide/type-key 4) :decision-order)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 4}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 4}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 4})))))

  (t/testing "Valvonannan lopetus"
    (t/testing "has type-id 5 and type-key :court-hearing"
      (t/is (= (toimenpide/type-key 5) :closed)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 5}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 5}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 5})))))

  (t/testing "HaO-käsittely"
    (t/testing "has type-id 6 and type-key :court-hearing"
      (t/is (= (toimenpide/type-key 6) :court-hearing)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 6}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 6}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 6})))))

  (t/testing "Käskypäätös / kuulemiskirje"
    (t/testing "has type-id 7 and type-key :decision-order-hearing-letter"
      (t/is (= (toimenpide/type-key 7) :decision-order-hearing-letter)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 7}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 7}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 7})))))

  (t/testing "Käskypäätös / varsinainen päätös"
    (t/testing "has type-id 8 and type-key :decision-order-actual-decision"
      (t/is (= (toimenpide/type-key 8) :decision-order-actual-decision)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 8}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 8}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 8})))))

  (t/testing "Käskypäätös / tiedoksianto (ensimmäinen postitus)"
    (t/testing "has type-id 9 and type-key :decision-order-notice-first-mailing"
      (t/is (= (toimenpide/type-key 9) :decision-order-notice-first-mailing)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 9}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 9}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 9})))))

  (t/testing "Käskypäätös / tiedoksianto (toinen postitus)"
    (t/testing "has type-id 10 and type-key :decision-order-notice-second-mailing"
      (t/is (= (toimenpide/type-key 10) :decision-order-notice-second-mailing)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 10}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 10}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 10})))))

  (t/testing "Käskypäätös / tiedoksianto (Haastemies)"
    (t/testing "has type-id 11 and type-key :decision-order-notice-bailiff"
      (t/is (= (toimenpide/type-key 11) :decision-order-notice-bailiff)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 11}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 11}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 11})))))

  (t/testing "Käskypäätös / valitusajan odotus ja umpeutuminen"
    (t/testing "has type-id 12 and type-key :decision-order-waiting-for-deadline"
      (t/is (= (toimenpide/type-key 12) :decision-order-waiting-for-deadline)))
    (t/testing "is kaskypaatos-toimenpide?"
      (t/is (true? (toimenpide/kaskypaatos-toimenpide? {:type-id 12}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 12}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 12})))))

  (t/testing "Käskypäätös / kuulemiskirje"
    (t/testing "has type-id 14 and type-key :penalty-decision-hering-letter"
      (t/is (= (toimenpide/type-key 14) :penalty-decision-hearing-letter)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 14}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 14}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 14})))))

  (t/testing "Sakkopäätös / varsinainen päätös"
    (t/testing "has type-id 15 and type-key :penalty-decision-actual-decision"
      (t/is (= (toimenpide/type-key 15) :penalty-decision-actual-decision)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 15}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 15}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 15})))))

  (t/testing "Sakkopäätös / tiedoksianto (ensimmäinen postitus)"
    (t/testing "has type-id 16 and type-key :penalty-decision-notice-first-mailing"
      (t/is (= (toimenpide/type-key 16) :penalty-decision-notice-first-mailing)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 16}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 16}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 16})))))

  (t/testing "Sakkopäätös / tiedoksianto (toinen postitus)"
    (t/testing "has type-id 17 and type-key :penalty-decision-notice-second-mailing"
      (t/is (= (toimenpide/type-key 17) :penalty-decision-notice-second-mailing)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 17}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 17}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 17})))))

  (t/testing "Sakkopäätös / tiedoksianto (Haastemies)"
    (t/testing "has type-id 18 and type-key :penalty-decision-notice-bailiff"
      (t/is (= (toimenpide/type-key 18) :penalty-decision-notice-bailiff)))
    (t/testing "is sakkopaatos-haastemies-tiedoksianto?"
      (t/is (true? (toimenpide/sakkopaatos-haastemies-tiedoksianto? {:type-id 18}))))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 18}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 18}))))
    (t/testing "is asha-toimenpide?"
      (t/is (true? (toimenpide/asha-toimenpide? {:type-id 18})))))

  (t/testing "Sakkopäätös / valitusajan odotus ja umpeutuminen"
    (t/testing "has type-id 19 and type-key :penalty-decision-waiting-for-deadline"
      (t/is (= (toimenpide/type-key 19) :penalty-decision-waiting-for-deadline)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 19}))))
    (t/testing "is sakkopaatos-toimenpide?"
      (t/is (true? (toimenpide/sakkopaatos-toimenpide? {:type-id 19}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 19})))))

  (t/testing "Sakkoluettelon lähetys menossa"
    (t/testing "has type-id 21 and type-key :penalty-list-delivery-in-progress"
      (t/is (= (toimenpide/type-key 21) :penalty-list-delivery-in-progress)))
    (t/testing "is not kaskypaatos-toimenpide?"
      (t/is (false? (toimenpide/kaskypaatos-toimenpide? {:type-id 21}))))
    (t/testing "is not sakkopaatos-toimenpide?"
      (t/is (false? (toimenpide/sakkopaatos-toimenpide? {:type-id 21}))))
    (t/testing "is not asha-toimenpide?"
      (t/is (false? (toimenpide/asha-toimenpide? {:type-id 21}))))))