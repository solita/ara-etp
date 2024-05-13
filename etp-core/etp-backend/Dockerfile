# Build image
FROM --platform=amd64 clojure:temurin-17-tools-deps-alpine@sha256:f8ffd1b139116d0cd4b4764215a28619522e213be5b054335071c97e4bb00b1b as builder

# Preload dependencies for better caching
COPY deps.edn /usr/src/etp-backend/
WORKDIR /usr/src/etp-backend
RUN clojure -P -M:uberjar

COPY . /usr/src/etp-backend
RUN clojure -M:uberjar


# Production image
FROM --platform=amd64 eclipse-temurin:17.0.8_7-jre-jammy

# LibreOffice installation
RUN apt-get update && \
apt-get install -y --no-install-recommends libxinerama1 libdbus-1-3 libx11-xcb1 \
libdbus-glib-1-2 libcairo2 libcups2 libsm6 awscli jq && \
curl -L -O https://etp-build-resources.s3.eu-central-1.amazonaws.com/LibreOffice/LibreOffice_7.1.0_Linux_x86-64_deb.tar.gz && \
tar -xzvf LibreOffice_7.1.0_Linux_x86-64_deb.tar.gz && \
dpkg -i LibreOffice_7.1.0.3_Linux_x86-64_deb/DEBS/*.deb && \
ln -s /usr/local/bin/libreoffice7.1 /usr/local/bin/libreoffice && \
rm -rf LibreOffice_7.1.0_Linux_x86-64_deb.tar.gz LibreOffice_7.1.0.3_Linux_x86-64_deb

# The application
COPY libreoffice/ /opt/etp/libreoffice/
COPY --from=builder /usr/src/etp-backend/target/etp-backend.jar /opt/etp/target/
COPY start.sh /opt/etp/

WORKDIR /opt/etp
ENTRYPOINT ["bash", "./start.sh"]
