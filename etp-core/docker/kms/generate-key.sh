#!/usr/bin/env bash
# Used to generate a new key for local KMS. No need to run this, but kept in place to have the process documented.
set -euxo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
KEY_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.key"
CSR_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.csr"
CRT_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.crt"
SER_FILE_ROOT="${SCRIPT_DIR}/root-serial"
echo "01" > "${SER_FILE_ROOT}"

KEY_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.key"
CSR_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.csr"
CRT_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.crt"
SER_FILE_INT="${SCRIPT_DIR}/root-int"
echo "01" > "${SER_FILE_INT}"
EXT_FILE_INT="${SCRIPT_DIR}/v3-int.ext"
echo "basicConstraints = CA:TRUE, pathlen:0
keyUsage = keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer" > "${EXT_FILE_INT}"

KEY_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.key"
CSR_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.csr"
CRT_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.crt"
EXT_FILE_LEAF="${SCRIPT_DIR}/v3-leaf.ext"
echo "basicConstraints = CA:FALSE
keyUsage = digitalSignature, nonRepudiation
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer" > "${EXT_FILE_LEAF}"

# Create Root CA
openssl genrsa -out "${KEY_FILE_ROOT}" 4096
openssl req -x509 -new -nodes -key "${KEY_FILE_ROOT}" -sha256 -days 1024 -out "${CRT_FILE_ROOT}" \
    -subj "/C=US/ST=California/L=San Francisco/O=My Company/CN=ETP LOCAL DEV ROOT" \
    -addext "basicConstraints = CA:TRUE" \
    -addext "keyUsage = keyCertSign, cRLSign"

# Create Intermediate CA
openssl genrsa -out "${KEY_FILE_INT}" 4096
openssl req -new -key "${KEY_FILE_INT}" -out "${CSR_FILE_INT}" -subj "/C=US/ST=California/L=San Francisco/O=My Company/CN=ETP LOCAL DEV INT"

# Sign Intermediate CA with Root CA
openssl x509 -req -in "${CSR_FILE_INT}" -CA "${CRT_FILE_ROOT}" -CAkey "${KEY_FILE_ROOT}" -CAserial "${SCRIPT_DIR}/root-serial" -out "${CRT_FILE_INT}" -days 500 -sha256 -extfile "${EXT_FILE_INT}"

# Create a Certificate Signing Request
openssl genrsa -out "${KEY_FILE_LEAF}" 2048
openssl req -new -key "${KEY_FILE_LEAF}" -out "${CSR_FILE_LEAF}" -subj "/C=US/ST=California/L=San Francisco/O=My Company/CN=ETP LOCAL DEV LEAF"

# Sign the Certificate with Intermediate CA
openssl x509 -req -in "${CSR_FILE_LEAF}" -CA "${CRT_FILE_INT}" -CAkey "${KEY_FILE_INT}" -CAserial "${SCRIPT_DIR}/int-serial" -out "${CRT_FILE_LEAF}" -days 375 -sha256 -extfile "${EXT_FILE_LEAF}"

echo "\
Keys:
  Asymmetric:
    Rsa:
      - Metadata:
          KeyId: baf442ae-4a56-4e7e-bb48-6e0a8625fec0
          KeyUsage: SIGN_VERIFY
        PrivateKeyPem: |
$(cat "${KEY_FILE_LEAF}" | sed 's/^/          /')"\
> "$SCRIPT_DIR/seed.yaml"

# Clean unnecessary files
rm "${KEY_FILE_LEAF}"
rm "${CSR_FILE_LEAF}"
rm "${KEY_FILE_INT}"
rm "${CSR_FILE_INT}"
rm "${KEY_FILE_ROOT}"

rm "${EXT_FILE_LEAF}"
rm "${EXT_FILE_INT}"

rm "${SER_FILE_INT}"
rm "${SER_FILE_ROOT}"

# Move the certificate to resources
mkdir -p "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature"
mv "${CRT_FILE_LEAF}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-leaf.pem.crt"
mv "${CRT_FILE_INT}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-int.pem.crt"
mv "${CRT_FILE_ROOT}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-root.pem.crt"
