FROM --platform=amd64 eclipse-temurin:17.0.12_7-jre-alpine

RUN apk add --upgrade curl \
    && curl -L -O https://github.com/Nilhcem/FakeSMTP/raw/gh-pages/downloads/fakeSMTP-latest.zip \
    && unzip fakeSMTP-latest.zip

EXPOSE 25
CMD ["java", "-jar", "fakeSMTP-2.0.jar", "-s", "-b"]
