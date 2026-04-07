(ns solita.etp.service.laatimisvaihe
  (:require [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.etp.exception :as exception]
            [solita.common.logic :as logic]))

(def find-laatimisvaiheet luokittelu-service/find-laatimisvaiheet)

(def ^:private vaihe-keys
  [:rakennuslupa,
   :kayttoonotto,
   :olemassaolevarakennus,
   :rakennuslupa-perusparannus,
   :kayttoonotto-perusparannus])

(defn vaihe-key [vaihe-id] (nth vaihe-keys vaihe-id))

(defn- in-vaihe? [vaihe-id energiatodistus]
  (= (-> energiatodistus :perustiedot :laatimisvaihe)
     vaihe-id))

;; applicable only for 2018 version
(def rakennuslupa? (partial in-vaihe? 0))
(def kayttoonotto? (partial in-vaihe? 1))

;; applicable 2013 and 2018 versions
(def olemassaoleva-rakennus?
  (logic/if* (logic/pred = :versio 2013)
             (complement (logic/pipe :perustiedot :uudisrakennus))
             (partial in-vaihe? 2)))

(def ^:private allowed-laatimisvaihe-ids
  {2018 #{0 1 2}
   2026 #{0 1 2 3 4}})

(defn validate-laatimisvaihe-for-versio!
  "Validates that the laatimisvaihe of the energiatodistus is allowed for its version.
   Throws :invalid-laatimisvaihe if the value is not in the allowed set."
  [energiatodistus]
  (let [versio (:versio energiatodistus)
        laatimisvaihe (-> energiatodistus :perustiedot :laatimisvaihe)
        allowed (get allowed-laatimisvaihe-ids versio)]
    (when (and allowed
               (some? laatimisvaihe)
               (not (contains? allowed laatimisvaihe)))
      (exception/throw-ex-info!
        {:type    :invalid-laatimisvaihe
         :message (str "Laatimisvaihe " laatimisvaihe
                       " is not allowed for energiatodistus version " versio)}))))
