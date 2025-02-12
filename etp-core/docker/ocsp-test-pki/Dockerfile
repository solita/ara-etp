FROM --platform=amd64 alpine:3.20

RUN apk add --upgrade \
    bash \
    openssl

RUN mkdir -p /var/log/ocsp-responder/

WORKDIR "/etc/pki"

EXPOSE 2060

CMD  ["openssl","ocsp", \
"-index", "index.txt", \
"-port", "2060", \
"-rsigner", "issued/ocsp_responder.crt", \
"-rkey", "private/ocsp_responder.key", \
"-CA", "ca.crt", \
"-text", \
"-out", "/var/log/ocsp-responder/ocsp.log", \
"-nmin", "1"]
