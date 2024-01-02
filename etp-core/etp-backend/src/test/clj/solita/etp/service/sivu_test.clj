(ns solita.etp.service.sivu-test
  (:require [clojure.test :as t]
            [solita.common.map :as xmap]
            [solita.etp.service.rooli :as rooli-service]
            [solita.etp.service.sivu :as service]
            [solita.etp.test-data.kayttaja :as kayttaja-test-data]
            [solita.etp.test-data.laatija :as laatija-test-data]
            [solita.etp.test-data.sivu :as sivu-test-data]
            [solita.etp.test-system :as ts]))

(t/use-fixtures :each ts/fixture)

(defn no-ordinal [sivu] (dissoc sivu :ordinal))

(defn test-data-set []
  (let [paakayttaja-adds (->> (kayttaja-test-data/generate-adds 2)
                              (map #(assoc % :rooli 2)))
        paakayttaja-ids (kayttaja-test-data/insert! paakayttaja-adds)
        laskuttaja-adds (->> (kayttaja-test-data/generate-adds 2)
                             (map #(assoc % :rooli 3)))
        laskuttaja-ids (kayttaja-test-data/insert! laskuttaja-adds)
        laatijat (laatija-test-data/generate-and-insert! 10)
        published-sivut (sivu-test-data/generate-and-insert!
                         2 3 nil
                         #(assoc % :published true))
        unpublished-sivut (sivu-test-data/generate-and-insert!
                           1 3 nil
                           #(assoc % :published false))
        sivut (concat published-sivut unpublished-sivut)]
    {:paakayttajat (zipmap paakayttaja-ids paakayttaja-adds)
     :laskuttajat (zipmap laskuttaja-ids laskuttaja-adds)
     :laatijat laatijat
     :sivut sivut
     :published-sivut published-sivut
     :unpublished-sivut unpublished-sivut}))

(defn ordinal-test-data-set []
  (let [whoami kayttaja-test-data/paakayttaja
        defaults {:published true
                  :ordinal nil
                  :parent-id nil
                  :body ""}
        root-0-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                         :title "Pääsivu 0")))
        root-1-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                         :title "Pääsivu 1")))
        sub-0-0-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                          :title "Alisivu 0 0"
                                                          :parent-id root-0-id)))
        sub-0-1-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                          :title "Alisivu 0 1"
                                                          :parent-id root-0-id)))
        sub-1-0-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                          :title "Alisivu 1 0"
                                                          :parent-id root-1-id)))
        sub-1-1-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                          :title "Alisivu 1 1"
                                                          :parent-id root-1-id)))]
    {:defaults defaults
     :root-0-id root-0-id
     :root-1-id root-1-id
     :sub-0-0-id sub-0-0-id
     :sub-0-1-id sub-0-1-id
     :sub-1-0-id sub-1-0-id
     :sub-1-1-id sub-1-1-id
     :whoami whoami}))

(t/deftest find-sivu
  (let [ds (test-data-set)
        sivut-by-id (->> (:sivut ds)
                         (group-by :id)
                         (xmap/map-values first))
        unpub-id (-> ds :unpublished-sivut first :id)]
    (t/is (nil? (service/find-sivu ts/*db*
                                   kayttaja-test-data/laatija
                                   unpub-id)))

    (t/is (= (no-ordinal (get sivut-by-id unpub-id))
             (no-ordinal (service/find-sivu ts/*db*
                                            kayttaja-test-data/paakayttaja
                                            unpub-id))))))

(t/deftest find-all
  (let [ds (test-data-set)]
    ;; laatija sees only published sivut
    (t/is (= (->> (service/find-all-sivut ts/*db*
                                          kayttaja-test-data/laatija)
                  (map :id) set)
             (->> (:published-sivut ds)
                  (map :id) set)))

    ;; paakayttaja sees every page
    (t/is (= (->> (service/find-all-sivut ts/*db*
                                          kayttaja-test-data/paakayttaja)
                  (map :id) set)
             (->> (:sivut ds)
                  (map :id) set)))))

(t/deftest update-body
  (let [ds (test-data-set)
        sivu (-> ds :sivut first)
        sivu-id (:id sivu)
        new-body "Lorem ipsum"]
    (t/is (= (no-ordinal sivu)
             (no-ordinal (service/find-sivu ts/*db*
                                            kayttaja-test-data/paakayttaja
                                            sivu-id))))
    (service/update-sivu! ts/*db* sivu-id {:body new-body})
    (t/is (= (no-ordinal (assoc sivu :body new-body))
             (no-ordinal (service/find-sivu ts/*db*
                                            kayttaja-test-data/paakayttaja
                                            sivu-id))))))

(t/deftest delete-leaf-sivu
  (let [ds (test-data-set)
        parent-ids (->> ds :sivut (map :parent-id) set)
        sivu (->> ds :sivut (filter #(not (contains? parent-ids (:id %))))  first)
        sivu-id (:id sivu)]
    (t/is (not (nil? (service/find-sivu ts/*db*
                                     kayttaja-test-data/paakayttaja
                                     sivu-id))))
    (t/is (= 1 (service/delete-sivu! ts/*db* sivu-id)))
    (t/is (nil? (service/find-sivu ts/*db*
                                   kayttaja-test-data/paakayttaja
                                   sivu-id)))))

(t/deftest delete-missing
  (let [{:keys [sivut]} (test-data-set)
        missing-id (->> (map :id sivut)
                        (reduce max)
                        inc)]
    (t/is (nil? (service/find-sivu ts/*db*
                                   kayttaja-test-data/paakayttaja
                                   missing-id)))
    (t/is (= 0 (service/delete-sivu! ts/*db* missing-id)))
    (t/is (nil? (service/find-sivu ts/*db*
                                   kayttaja-test-data/paakayttaja
                                   missing-id)))))

(t/deftest update-title
  (let [ds (test-data-set)
        sivu (-> ds :sivut first)
        sivu-id (:id sivu)
        new-title "Hello"]
    (t/is (= (no-ordinal sivu)
             (no-ordinal (service/find-sivu ts/*db*
                                            kayttaja-test-data/paakayttaja
                                            sivu-id))))
    (t/is (= 1 (service/update-sivu! ts/*db* sivu-id {:title new-title})))
    (t/is (= (no-ordinal (assoc sivu :title new-title))
             (no-ordinal (service/find-sivu ts/*db*
                                            kayttaja-test-data/paakayttaja
                                            sivu-id))))))

(t/deftest update-missing
  (let [{:keys [sivut]} (test-data-set)
        missing-id (->> (map :id sivut)
                        (reduce max)
                        inc)
        new-title "Hello"]
    (t/is (= 0 (service/update-sivu! ts/*db* missing-id {:title new-title})))))

(t/deftest find-empty
  (t/is (empty? (service/find-all-sivut ts/*db*
                                        kayttaja-test-data/paakayttaja))))

(t/deftest find-one-after-add
  (let [sivu-in {:title "Pääsääntö"
                 :body "Äläpä tee virheitä"
                 :published true
                 :ordinal 4
                 :parent-id nil}
        added-sivu-id (:id (service/add-sivu! ts/*db* sivu-in))
        sivu-out (service/find-sivu ts/*db*
                                    kayttaja-test-data/paakayttaja
                                    added-sivu-id)]
    (t/is (not (nil? added-sivu-id)))
    (t/is (not (nil? sivu-out)))
    (t/is (= (:id    sivu-out) added-sivu-id))
    (t/is (= (:title sivu-out) (:title sivu-in)))
    (t/is (= (:body  sivu-out) (:body  sivu-in)))
    (t/is (:published sivu-out))))

(t/deftest ordinal-new-root-prepend
  (let [{:keys [defaults
                root-0-id
                root-1-id
                whoami]} (ordinal-test-data-set)
        sivu-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                       :title "Päätason sivu"
                                                       :parent-id nil
                                                       :ordinal 0)))]
    (t/is (= 0 (-> (service/find-sivu ts/*db* whoami sivu-id)
                   :ordinal)))
    (t/is (= 1 (-> (service/find-sivu ts/*db* whoami root-0-id)
                   :ordinal)))
    (t/is (= 2 (-> (service/find-sivu ts/*db* whoami root-1-id)
                   :ordinal)))))

(t/deftest ordinal-delete
  (let [{:keys [defaults
                root-1-id
                sub-1-0-id
                sub-1-1-id
                whoami]} (ordinal-test-data-set)]
    (service/delete-sivu! ts/*db* sub-1-0-id)
    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-1-1-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 0 :parent-id root-1-id}))))

(t/deftest ordinal-new-root-append
  (let [{:keys [defaults
                root-0-id
                root-1-id
                whoami]} (ordinal-test-data-set)
        sivu-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                       :title "Päätason sivu"
                                                       :parent-id nil
                                                       :ordinal nil)))]
    (t/is (= 0 (-> (service/find-sivu ts/*db* whoami root-0-id)
                   :ordinal)))
    (t/is (= 1 (-> (service/find-sivu ts/*db* whoami root-1-id)
                   :ordinal)))
    (t/is (= 2 (-> (service/find-sivu ts/*db* whoami sivu-id)
                   :ordinal)))))

(t/deftest ordinal-new-root-append
  (let [{:keys [defaults
                root-0-id
                root-1-id
                whoami]} (ordinal-test-data-set)
        sivu-id (:id (service/add-sivu! ts/*db* (assoc defaults
                                                       :title "Päätason sivu"
                                                       :parent-id nil
                                                       :ordinal nil)))]
    (t/is (= 0 (-> (service/find-sivu ts/*db* whoami root-0-id)
                   :ordinal)))
    (t/is (= 1 (-> (service/find-sivu ts/*db* whoami root-1-id)
                   :ordinal)))
    (t/is (= 2 (-> (service/find-sivu ts/*db* whoami sivu-id)
                   :ordinal)))))


(t/deftest ordinal-reroot
  (let [{:keys [defaults
                root-0-id
                root-1-id
                sub-0-0-id
                sub-0-1-id
                sub-1-0-id
                sub-1-1-id
                whoami]} (ordinal-test-data-set)]
    (service/update-sivu! ts/*db* sub-1-1-id {:parent-id root-0-id
                                              :ordinal 1})
    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-0-0-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 0 :parent-id root-0-id}))

    ;; The moved sivu should be here as the second sub-page of the
    ;; first root.
    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-1-1-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 1 :parent-id root-0-id}))

    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-0-1-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 2 :parent-id root-0-id}))

    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-1-0-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 0 :parent-id root-1-id}))))


(t/deftest ordinal-reroot-last
  (let [{:keys [defaults
                root-0-id
                root-1-id
                sub-0-0-id
                sub-0-1-id
                sub-1-0-id
                sub-1-1-id
                whoami]} (ordinal-test-data-set)]
    (service/update-sivu! ts/*db* sub-1-0-id {:parent-id root-0-id})

    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-0-0-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 0 :parent-id root-0-id}))

    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-0-1-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 1 :parent-id root-0-id}))

    ;; The ordinal was not specified, so it is expected that the first
    ;; available ordinal is assigned.
    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-1-0-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 2 :parent-id root-0-id}))

    (t/is (= (-> (service/find-sivu ts/*db* whoami sub-1-1-id)
                 (select-keys [:ordinal :parent-id]))
             {:ordinal 0 :parent-id root-1-id}))))


(t/deftest simple-hierarchy
  (let [whoami kayttaja-test-data/paakayttaja
        defaults {:published false
                  :ordinal 1
                  :parent-id nil}
        root-1-in (assoc defaults
                         :title "Lämmitysjärjestelmät"
                         :body "Miten saadaan lämpöenergiaa sisään")
        root-1-id (:id (service/add-sivu! ts/*db* root-1-in))
        child-1-1-in (assoc defaults
                            :title "Takat"
                            :body "Poltetaan asioita"
                            :parent-id root-1-id
                            :ordinal 1)
        child-1-1-id (service/add-sivu! ts/*db* child-1-1-in)
        child-1-2-in (assoc defaults
                            :title "Koirat"
                            :body "Nämäkin luovuttavat lämpöenergiaa"
                            :parent-id root-1-id
                            :ordinal 2)
        child-1-2-id (service/add-sivu! ts/*db* child-1-2-in)
        root-2-in (assoc defaults
                         :title "Eristäminen"
                         :body "Miten saadaan energia pysymään sisällä?")
        root-2-id (:id (service/add-sivu! ts/*db* root-2-in))
        sivu-list (service/find-all-sivut ts/*db* whoami)]
    (t/is (not (empty? sivu-list)))
    (t/is (= (:body (service/find-sivu ts/*db* whoami (:id child-1-2-id)))
             (:body child-1-2-in)))))
