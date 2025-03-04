# OCSP Request
openssl ocsp -CAfile pki/ca.crt \
    -issuer pki/ca.crt \
    -cert pki/issued/some_certificate.crt \
    -url http://localhost:2560 -resp_text
