#!/usr/bin/env bash
# Used to generate a new key for local KMS. No need to run this, but kept in place to have the process documented.
set -euo pipefail
set -x
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
KEY_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.key"
CSR_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.csr"
CRT_FILE_ROOT="${SCRIPT_DIR}/signing-key-root.crt"

KEY_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.key"
CSR_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.csr"
CRT_FILE_INT="${SCRIPT_DIR}/signing-key-intermediate.crt"

KEY_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.key"
CSR_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.csr"
CRT_FILE_LEAF="${SCRIPT_DIR}/signing-key-leaf.crt"

# Create directories
# mkdir -p ca/root ca/intermediate

# Create Root CA
openssl genrsa -out "${KEY_FILE_ROOT}" 4096
openssl req -x509 -new -nodes -key "${KEY_FILE_ROOT}" -sha256 -days 1024 -out "${CRT_FILE_ROOT}" -subj "/CN=ROOT CA"

# Create Intermediate CA
openssl genrsa -out "${KEY_FILE_INT}" 4096
openssl req -new -key "${KEY_FILE_INT}" -out "${CSR_FILE_INT}" -subj "/CN=INT CA"

# Sign Intermediate CA with Root CA
openssl x509 -req -in "${CSR_FILE_INT}" -CA "${CRT_FILE_ROOT}" -CAkey "${KEY_FILE_ROOT}" -CAcreateserial -out "${CRT_FILE_INT}" -days 500 -sha256

# Create a Certificate
openssl genrsa -out "${KEY_FILE_LEAF}" 2048
openssl req -new -key "${KEY_FILE_LEAF}" -out "${CSR_FILE_LEAF}" -subj "/CN=LEAF CA"

# TODO: Add some attributes?
# Sign the Certificate with Intermediate CA
openssl x509 -req -in "${CSR_FILE_LEAF}" -CA "${CRT_FILE_INT}" -CAkey "${KEY_FILE_INT}" -CAcreateserial -out "${CRT_FILE_LEAF}" -days 375 -sha256

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

# Move the certificate to resources
mkdir -p "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature"
mv "${CRT_FILE_LEAF}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-leaf.pem.crt"
mv "${CRT_FILE_INT}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-int.pem.crt"
mv "${CRT_FILE_ROOT}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing-root.pem.crt"

# TODO: Move also to somewhere where the test system can use it?