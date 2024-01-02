(ns solita.etp.service.laatimisvaihe
  (:require [solita.etp.service.luokittelu :as luokittelu-service]
            [solita.common.logic :as logic]))

(def find-laatimisvaiheet luokittelu-service/find-laatimisvaiheet)

(def ^:private vaihe-keys
  [:rakennuslupa,
   :kayttoonotto,
   :olemassaolevarakennus])

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
