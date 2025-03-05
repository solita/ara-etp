#!/usr/bin/env bash
set -e

# FIXME: dependency-check-core is currently updated manually. It also required updating org.slf4j/slf4j-api. Remove when new version of nvd-clojure is released.
clojure -Ttools install nvd-clojure/nvd-clojure '{:mvn/version "4.0.0" :exclusions [org.owasp/dependency-check-core]}' :as nvd
clojure -J-Dclojure.main.report=stderr -Sdeps '{:deps {org.slf4j/slf4j-api {:mvn/version "2.0.17"} org.owasp/dependency-check-core {:mvn/version "12.1.0"}}}' -Tnvd nvd.task/check :classpath \""$(clojure -Spath)\"" :config-filename \""nvd-config.edn\""
