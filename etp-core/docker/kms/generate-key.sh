#!/usr/bin/env bash
# Used to generate a new key for local KMS. No need to run this, but kept in place to have the process documented.
set -euo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "\
Keys:
  Asymmetric:
    Rsa:
      - Metadata:
          KeyId: baf442ae-4a56-4e7e-bb48-6e0a8625fec0
          KeyUsage: SIGN_VERIFY
        PrivateKeyPem: |
$(openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 | sed 's/^/          /')"\
> "$SCRIPT_DIR/seed.yaml"
