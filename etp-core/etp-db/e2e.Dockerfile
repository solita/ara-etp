# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-1.11.1.1347-alpine as builder

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN clojure -M:uberjar
RUN unzip target/etp-db.jar -d target/etp-db && \
    cp -rf src/test target/test && \
    rm -f target/etp-db.jar

# Production image
FROM --platform=amd64 eclipse-temurin:17.0.8_7-jre-jammy

# TODO: Fix docker. Use builder
COPY --from=builder /usr/src/app/target /target
COPY ./src /src
COPY db.sh /

#CMD ["DB_URL=jdbc:postgresql://db:5432/postgres" "clojure" "-M" "-m" "solita.etp.db.flywaydb" "migrate"]
CMD [ "clojure" "-M:test" "-m" "solita.etp.db.flywaydb" "migrate"]
