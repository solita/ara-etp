#!/bin/bash

export SUOMIFI_VIESTIT_KEYSTORE_PASSWORD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
export SUOMIFI_VIESTIT_KEYSTORE_ALIAS=viestit

echo Retrieve /secret/etp/suomifi/viestit/client.crt
aws secretsmanager get-secret-value --secret-id "/secret/etp/suomifi/viestit/client.crt" | jq .SecretString -r > public.pem
echo Retrieve /secret/etp/suomifi/viestit/private.key
aws secretsmanager get-secret-value --secret-id "/secret/etp/suomifi/viestit/private.key" | jq .SecretString -r > private.pem

echo Pack certificate as p12 file format
openssl pkcs12 -export -in public.pem -inkey private.pem -out viestit.p12 -name ${SUOMIFI_VIESTIT_KEYSTORE_ALIAS} -passout pass:${SUOMIFI_VIESTIT_KEYSTORE_PASSWORD}

echo Import certificate from p12 format
keytool -importkeystore -destkeystore viestit.jks -deststorepass "${SUOMIFI_VIESTIT_KEYSTORE_PASSWORD}" -srckeystore viestit.p12 -srcstoretype PKCS12 -srcstorepass "${SUOMIFI_VIESTIT_KEYSTORE_PASSWORD}"

echo Clean up
rm -f viestit.p12 private.pem public.pem

echo Start backend core
exec java -Djava.awt.headless=true \
     ${JAVA_OPTS} \
     -cp target/etp-backend.jar \
     clojure.main -m solita.etp.core
