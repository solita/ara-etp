FROM --platform=amd64 eclipse-temurin:11.0.18_10-jdk-jammy

COPY target /target
COPY db.sh /

CMD ["./db.sh", "migrate"]
