#!/usr/bin/env bash
set -e

clojure -Ttools install nvd-clojure/nvd-clojure '{:mvn/version "5.0.0"}' :as nvd
clojure -J-Dclojure.main.report=stderr -Tnvd nvd.task/check :classpath \""$(clojure -Spath)\"" :config-filename \""nvd-config.edn\""
