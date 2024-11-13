#! /usr/bin/env nix-shell
#! nix-shell -I channel:nixos-24.05-small --pure -i bash -p openssl

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# EASYRSA="${SCRIPT_DIR}"
# EASYRSA_SSL_CONF="${SCRIPT_DIR}/openssl-easyrsa.cnf"
# EASYRSA_EXT_DIR="${SCRIPT_DIR}/x509-types/"

OCSP_KEY="${SCRIPT_DIR}/ocsp/ocsp.key"
OCSP_CSR="${SCRIPT_DIR}/ocsp/ocsp.csr" 
OCSP_REQ_NAME="ocsp_responder"

SOME_KEY="${SCRIPT_DIR}/requester/requester.key"
SOME_CSR="${SCRIPT_DIR}/requester/requester.csr" 

PKI_ROOT_DIR="${SCRIPT_DIR}/pki-root"
PKI_INT_DIR="${SCRIPT_DIR}/pki-int"

SOME_REQ_NAME="some_certificate"

OCSP_LOG="${SCRIPT_DIR}/ocsp.log"

clean() {
  echo "Cleaning"
  rm -r "${PKI_ROOT_DIR}"
  rm -r "${PKI_INT_DIR}/pki-root"
  rm -r "${SCRIPT_DIR}/pki-int"
  rm -r "${SCRIPT_DIR}/ocsp"
  rm -r "${SCRIPT_DIR}/requester"
}
clean

exit 0

easyrsa_root() {
  ./easyrsa --pki-dir="${PKI_ROOT_DIR}" "$@"
}

easyrsa_int() {
  ./easyrsa --pki-dir="${PKI_INT_DIR}" "$@"
}

create_root_ca() {
  easyrsa_root init-pki
  easyrsa_root build-ca nopass
}
create_root_ca

create_int_ca() {
  easyrsa_int init-pki
  easyrsa_int build-ca subca nopass
}
create_int_ca

create_int_ca_cert() {
  easyrsa_root import-req "${PKI_INT_DIR}/reqs/ca.req" "int_ca"
  easyrsa_root sign-req ca "int_ca"
  cp "${PKI_ROOT_DIR}/issued/int_ca.crt" "${PKI_INT_DIR}/ca.crt"
}
create_int_ca_cert

create_root_ocsp_cert() {
  easyrsa_root gen-req "${OCSP_REQ_NAME}" nopass
  easyrsa_root sign-req ocsp "${OCSP_REQ_NAME}"
}
create_root_ocsp_cert

create_requester_cert() {
  easyrsa_root gen-req "${RES}" nopass
  openssl genpkey -algorithm RSA -out "${SOME_KEY}"
  openssl req -new -key "${OCSP_KEY}" -out "${SOME_CSR}" -subj "/CN=INT CA"
  ./easyrsa import-req "${SOME_CSR}" "${SOME_REQ_NAME}"
  ./easyrsa sign-req client "${SOME_REQ_NAME}"
}
#create_requester_cert

start_ocsp_responder() {
  openssl ocsp \
      -index "${PKI_ROOT_DIR}/index.txt" \
      -port 2560 \
      -rsigner "${PKI_ROOT_DIR}/issued/${OCSP_REQ_NAME}.crt" \
      -rkey "${PKI_ROOT_DIR}/private/${OCSP_REQ_NAME}.key" \
      -CA "${PKI_ROOT_DIR}/ca.crt" \
      -text \
      -out "${OCSP_LOG}"
}
start_ocsp_responder

