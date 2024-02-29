# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-alpine@sha256:a030abc4283e3c7ba4f02a5e4ac3b9a6b9393e3f8a2d9233bf4339ed045d4f3c as builder

# Preload dependencies using the prepare (-P) flag for better caching
COPY deps.edn /usr/src/etp-db/
WORKDIR /usr/src/etp-db
RUN clojure -P -M:uberjar

COPY . /usr/src/etp-db
RUN clojure -M:uberjar

# Production image
FROM --platform=amd64 eclipse-temurin:17.0.8_7-jre-jammy

COPY --from=builder /usr/src/etp-db/target /target
COPY --from=builder /usr/src/etp-db/src/test /target/test
COPY db.sh /

CMD ["./db.sh", "migrate"]
