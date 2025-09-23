# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-alpine@sha256:7c64ea0be8d926cfa7257ee574207d4dc51e0393991302be135c54c966e5f0ed as builder

# Preload dependencies using the prepare (-P) flag for better caching
COPY deps.edn /usr/src/etp-db/
WORKDIR /usr/src/etp-db
RUN clojure -P -M:uberjar

COPY . /usr/src/etp-db
RUN clojure -M:uberjar

# Production image
FROM --platform=amd64 eclipse-temurin:17.0.12_7-jre-jammy

COPY --from=builder /usr/src/etp-db/target /target
COPY --from=builder /usr/src/etp-db/src/test /target/test
COPY db.sh /

CMD ["./db.sh", "migrate"]
