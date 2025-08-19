# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-alpine@sha256:29527b07a28802cbfb93dee6b25c621baef06941bd285afc7500106287f724ba as builder

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
