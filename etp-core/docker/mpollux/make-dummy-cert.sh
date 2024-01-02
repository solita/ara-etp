#!/bin/sh

tmpdir=$(mktemp -d)

cat > $tmpdir/openssl.cnf <<'EOF'
[ req ]
default_bits            = 2048                  # RSA key size
encrypt_key             = no                    # Protect private key
default_md              = sha256                # MD to use
utf8                    = yes                   # Input is UTF-8
string_mask             = utf8only              # Emit UTF-8 strings
prompt                  = no                    # Don't prompt for DN
distinguished_name      = dummy_dn              # DN section

[ dummy_dn ]
# C=FI/serialNumber=999004228, GN=TIMO, SN=SPECIMEN-POTEX, CN=SPECIMEN-POTEX TIMO 999004228
countryName             = FI
serialNumber            = 999004228
givenName               = TIMO
surname                 = SPECIMEN-POTEX
commonName              = SPECIMEN-POTEX TIMO 999004228
EOF


openssl req \
    -config $tmpdir/openssl.cnf \
    -new \
    -out $tmpdir/csr.pem \
    -keyout $tmpdir/key.pem

openssl x509 \
    -signkey $tmpdir/key.pem \
    -in $tmpdir/csr.pem \
    -req \
    -days 3650 \
    -out $tmpdir/cert.pem

echo "Copy the following three times into the chain item in api/sign:"
echo "(this does not produce a working signature in any sense, but gets us a PDF for development use)"
echo
sed '1d;$d' < $tmpdir/cert.pem | tr -d '\n'
echo
echo
