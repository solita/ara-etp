#!/usr/bin/env bash
# Used to generate a new key for local KMS. No need to run this, but kept in place to have the process documented.
set -euo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
KEY_FILE="${SCRIPT_DIR}/signing-key.key"
CSR_FILE="${SCRIPT_DIR}/signing-key.csr"
CRT_FILE="${SCRIPT_DIR}/signing-key.crt"

# Generate key
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${KEY_FILE}"
# Create csr
openssl req -key "${KEY_FILE}" -new -out "${CSR_FILE}"
# Create a self-signed certificate
# TODO: Add some attributes?
openssl x509 -signkey "${KEY_FILE}" -in "${CSR_FILE}" -req -days 730 -out "${CRT_FILE}"

echo "\
Keys:
  Asymmetric:
    Rsa:
      - Metadata:
          KeyId: baf442ae-4a56-4e7e-bb48-6e0a8625fec0
          KeyUsage: SIGN_VERIFY
        PrivateKeyPem: |
$(cat "${KEY_FILE}" | sed 's/^/          /')"\
> "$SCRIPT_DIR/seed.yaml"

# Clean unnecessary files
rm "${KEY_FILE}"
rm "${CSR_FILE}"

# Move the certificate to resources
mkdir -p "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature"
mv "${CRT_FILE}" "${SCRIPT_DIR}/../../etp-backend/src/test/resources/dvv-system-signature/local-signing.pem.crt"

# TODO: Move also to somewhere where the test system can use it?