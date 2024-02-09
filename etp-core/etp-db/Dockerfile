# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-1.11.1.1347-alpine as builder

# TODO: Copy less stuff to invalidate the cached layer less often.
COPY . /usr/src/etp-db
WORKDIR /usr/src/etp-db
RUN clojure -M:uberjar
RUN unzip target/etp-db.jar -d target/etp-db && \
    cp -rf src/test target/test && \
    rm -f target/etp-db.jar

# Production image
FROM --platform=amd64 eclipse-temurin:17.0.8_7-jre-jammy

# TODO: Fix docker. Use builder
COPY --from=builder /usr/src/etp-db/target /target
# COPY ./src /src
COPY db.sh /

CMD ["./db.sh", "migrate"]
