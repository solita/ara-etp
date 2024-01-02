(ns solita.common.sftp
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (com.jcraft.jsch JSch ChannelSftp SftpException)))

(def default-port 22)
(def timeout (* 1000 60 10))

(defn disconnect! [{:keys [session channel]}]
  (.disconnect channel)
  (.disconnect session)
  nil)

(defrecord SftpConnection
    [jsch session channel]
  java.io.Closeable
  (close [this]
    (disconnect! this)))

(defn connect!
  ([host username password known-hosts-path]
   (connect! host default-port username password known-hosts-path))
  ([host port username password known-hosts-path]
   (let [known-hosts-resource (io/resource known-hosts-path)
         _ (log/info "Connecting with SFTP: " {:host host
                                               :port port
                                               :username username
                                               :known-hosts known-hosts-resource})
         jsch (doto (JSch.)
                (.setKnownHosts (io/input-stream known-hosts-resource)))
         session (doto (.getSession jsch username host port)
                   (.setPassword password)
                   (.connect timeout))]
     (->SftpConnection jsch session (doto (.openChannel session "sftp")
                                      (.connect))))))


(defn connected? [{:keys [session]}]
  (.isConnected session))

(defn files-in-dir
  ([{:keys [channel] :as connection}]
   (files-in-dir connection (.pwd channel)))
  ([{:keys [channel]} path]
   (disj
    (->> path (.ls channel) (map #(.getFilename %)) (set))
    "."
    "..")))

(defn file-exists-in-current-dir? [connection filename]
  (contains? (files-in-dir connection) filename))

(defn file-exists? [{:keys [channel] :as connection} path]
  (let [current-dir (.pwd channel)
        split-path (str/split path #"/")
        dirs-exist? (every? (fn [dir]
                              (when (file-exists-in-current-dir? connection dir)
                                (.cd channel dir)
                                true))
                            (butlast split-path))
        file-exists? (and dirs-exist?
                          (file-exists-in-current-dir? connection
                                                       (last split-path)))]
    (.cd channel current-dir)
    file-exists?))

(defn make-directory! [{:keys [channel] :as connection} path]
  (let [current-dir (.pwd channel)]
    (doseq [dir (str/split path #"/")]
      (when-not (file-exists-in-current-dir? connection dir)
        (.mkdir channel dir))
      (.cd channel dir))
    (.cd channel current-dir)))

(defn upload! [{:keys [channel]} src-path dst-path]
  (.put channel src-path dst-path))

(defn delete!
  ([connection path]
   (delete! connection path false))
  ([{:keys [channel] :as connection} path directory?]
   (when (file-exists? connection (str/replace path #"\*$" ""))
     (if directory?
       (.rmdir channel path)
       (.rm channel path)))))
