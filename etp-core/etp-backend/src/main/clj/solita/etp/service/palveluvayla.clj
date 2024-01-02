(ns solita.etp.service.palveluvayla
  (:require [solita.etp.service.energiatodistus :as service.energiatodistus]
            [solita.etp.service.energiatodistus-pdf :as service.energiatodistus-pdf]
            [solita.etp.service.energiatodistus-search :as service.energiatodistus-search]))

(def i-am-paakayttaja {:rooli 2})

(defn find-first-existing-pdf
  "Return the first language version that exists if any. If language-preference-order is not given, [fi sv] is used."
  [id language-preference-order db aws-s3-client]
  (some identity (->> (or language-preference-order ["fi" "sv"])
                      (map #(service.energiatodistus-pdf/find-energiatodistus-pdf db
                                                                                  aws-s3-client
                                                                                  i-am-paakayttaja
                                                                                  id
                                                                                  %)))))

(defn search-by-rakennustunnus
  "Search for energiatodistus by rakennustunnus as pääkäyttäjä and coerce into schema"
  [rakennustunnus schema db & [versio]]
  (service.energiatodistus-search/search
    db
    i-am-paakayttaja
    {:where [(-> [["=" "energiatodistus.perustiedot.rakennustunnus" rakennustunnus]]
                 (into (when versio [["=" "energiatodistus.versio" versio]])))]}
    schema))

(defn- drop-wrong-version
  "Return nil if the energiatodistus is not the given version"
  [et version]
  (if (= (:versio et) version)
    et
    nil))

(defn get-by-id
  "Get energiatodistus by id as pääkäyttäjä. If version is not given, the result is mapped EnertiatoistusForAnyLaatija schema."
  ([id db]
   (service.energiatodistus/find-energiatodistus-any-laatija db id))
  ([id db version]
   (-> (service.energiatodistus/find-energiatodistus db id)
       (drop-wrong-version version))))
