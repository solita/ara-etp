#!/usr/bin/env bash
set -e

if [ -z "$NVD_API_TOKEN" ]; then
  echo "Error: NVD_API_TOKEN environment variable is not set."
  echo "Get your free API key from: https://nvd.nist.gov/developers/request-an-api-key"
  echo "Then run: export NVD_API_TOKEN=your-api-key"
  exit 1
fi

clojure -Ttools install nvd-clojure/nvd-clojure '{:mvn/version "5.0.0"}' :as nvd
clojure -J-Dclojure.main.report=stderr -J-Danalyzer.ossindex.enabled=false -Tnvd nvd.task/check :classpath \""$(clojure -Spath)\"" :config-filename \""nvd-config.edn\"" :nvd-api '{:key "'"$NVD_API_TOKEN"'"}'
