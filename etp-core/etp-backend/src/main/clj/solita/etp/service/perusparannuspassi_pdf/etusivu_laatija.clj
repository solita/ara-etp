(ns solita.etp.service.perusparannuspassi-pdf.etusivu-laatija
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [hiccup.core :refer [h]]
    [solita.common.time :as time]
    [solita.etp.service.localization :as loc]))

(defn- fullname->firstname-last
  "Converts 'Sukunimi, Etunimi' to 'Etunimi Sukunimi'.
  Logs a warning (with the id) and returns the original if the format is unexpected."
  [fullname id]
  (let [parts (str/split fullname #", " 2)]
    (if (and (= (count parts) 2)
             (not (str/includes? (second parts) ",")))
      (str (second parts) " " (first parts))
      (do (log/warn "Unexpected format of laatija name for id:" id)
          fullname))))

(defn etusivu-laatija [{:keys [energiatodistus kieli]}]
  (let [l (kieli loc/ppp-pdf-localization)]
    [:dl {:class "etusivu-laatija-allekirjoitus"}
     [:div {:class "laatija-block"}
      [:dt (str (l :perusparannuspassin-laatija) ":")]
      [:dd {:class "laatija-nimi"} (-> energiatodistus :laatija-fullname
                   (fullname->firstname-last (-> energiatodistus :laatija-id))
                   h)]]
     [:div {:class "allekirjoituspaiva-block"}
      [:dt {:class "hidden-dt"} (l :allekirjoituspaiva)]
      [:dd (time/format-date (:allekirjoitusaika energiatodistus))]]]))
