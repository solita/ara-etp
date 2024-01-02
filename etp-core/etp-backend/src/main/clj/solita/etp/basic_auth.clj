(ns solita.etp.basic-auth
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as base64]))

(def safe-split (fnil str/split ""))

(defn req->id-and-password [{:keys [headers]}]
  (let [{:strs [authorization]} headers
        [method credentials] (safe-split authorization #" ")
        [id password] (-> credentials
                          base64/decode
                          codecs/bytes->str
                          (safe-split #":"))]
    (if (and (= (str/lower-case method) "basic") id password)
      {:id id :password password}
      (log/warn "Basic auth failed: "
                {:method method
                 :id id
                 :password (apply str (-> password count (repeat \*)))}))))
