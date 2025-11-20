#!/bin/bash

echo Start backend core
exec java -Djava.awt.headless=true \
     ${JAVA_OPTS} \
     -cp target/etp-backend.jar \
     clojure.main -m solita.etp.core
