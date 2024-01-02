(ns solita.etp.service.rooli
  (:require [solita.etp.service.luokittelu :as luokittelu-service]))

(def ^:private rooli-keys
  [:laatija :patevyyden-toteaja :paakayttaja :laskuttaja :aineistokayttaja])

(defn find-roolit [db] (luokittelu-service/find-roolit db))

(defn public? [whoami] (nil? whoami))

(defn- partner? [{:keys [partner]}] partner)

(defn laatija? [{:keys [rooli]}]
  (= rooli 0))

(def accredited-laatija?
  "Accredited laatijan pätevyyden on todentanut virallinen pätevyyden toteaja.
   Partner-laatijalla ei ole oikeasti laatijan pätevyyttä ja hän voi lähinnä kokeilla tai testata järjestelmää."
  (every-pred laatija? (complement partner?)))

(defn patevyydentoteaja? [{:keys [rooli]}]
  (= rooli 1))

(defn paakayttaja? [{:keys [rooli]}]
  (= rooli 2))

(defn laskuttaja? [{:keys [rooli]}]
  (= rooli 3))

(defn aineistokayttaja? [{:keys [rooli]}]
  (= rooli 4))

(defn system? [{:keys [rooli]}]
  (= rooli -1))

(defn rooli-key [rooli-id] (nth rooli-keys rooli-id))

(def energiatodistus-reader? (some-fn laatija? paakayttaja? laskuttaja?))
