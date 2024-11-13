#! /usr/bin/env nix-shell
#! nix-shell -I channel:nixos-24.05-small --pure -i bash -p openssl

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PKI_ROOT_DIR="${SCRIPT_DIR}/pki-root"
PKI_INT_DIR="${SCRIPT_DIR}/pki-int"

SOME_REQ_NAME="some_certificate"

OCSP_REQ_NAME="ocsp_responder"
OCSP_LOGS_DIR="${SCRIPT_DIR}/logs/"
OCSP_ROOT_LOG="${OCSP_LOGS_DIR}/ocsp-root.log"
OCSP_INT_LOG="${OCSP_LOGS_DIR}/ocsp-int.log"

clean() {
  echo "Cleaning"
  rm -r "${PKI_ROOT_DIR}"
  rm -r "${PKI_INT_DIR}"
  rm -r "${OCSP_LOGS_DIR}"
}

easyrsa_root() {
  ./easyrsa --pki-dir="${PKI_ROOT_DIR}" "$@"
}

easyrsa_int() {
  ./easyrsa --pki-dir="${PKI_INT_DIR}" "$@"
}

create_root_ca() {
  easyrsa_root init-pki

  cp -r "${SCRIPT_DIR}/x509-types" "${PKI_ROOT_DIR}/x509-types"

  easyrsa_root build-ca nopass

  echo "authorityInfoAccess = OCSP;URI:http://localhost:2060/" >> "${PKI_ROOT_DIR}"/x509-types/COMMON
}

create_int_ca() {
  easyrsa_int init-pki

  cp -r "${SCRIPT_DIR}/x509-types" "${PKI_INT_DIR}/x509-types"

  easyrsa_int build-ca subca nopass

  echo "authorityInfoAccess = OCSP;URI:http://localhost:2061/" >> "${PKI_INT_DIR}"/x509-types/COMMON
}

create_int_ca_cert() {
  easyrsa_root import-req "${PKI_INT_DIR}/reqs/ca.req" "int_ca"
  easyrsa_root sign-req ca "int_ca"
  cp "${PKI_ROOT_DIR}/issued/int_ca.crt" "${PKI_INT_DIR}/ca.crt"
}

create_root_ocsp_cert() {
  easyrsa_root gen-req "${OCSP_REQ_NAME}" nopass
  easyrsa_root sign-req ocsp "${OCSP_REQ_NAME}"
}

create_int_ocsp_cert() {
  easyrsa_int gen-req "${OCSP_REQ_NAME}" nopass
  easyrsa_int sign-req ocsp "${OCSP_REQ_NAME}"
}

create_leaf_cert() {
  easyrsa_int gen-req "${SOME_REQ_NAME}" nopass
  easyrsa_int sign-req dsign "${SOME_REQ_NAME}"
}

start_root_ocsp_responder() {
  openssl ocsp \
      -index "${PKI_ROOT_DIR}/index.txt" \
      -port 2560 \
      -rsigner "${PKI_ROOT_DIR}/issued/${OCSP_REQ_NAME}.crt" \
      -rkey "${PKI_ROOT_DIR}/private/${OCSP_REQ_NAME}.key" \
      -CA "${PKI_ROOT_DIR}/ca.crt" \
      -text \
      -out "${OCSP_ROOT_LOG}"
}

start_int_ocsp_responder() {
  openssl ocsp \
      -index "${PKI_INT_DIR}/index.txt" \
      -port 2561 \
      -rsigner "${PKI_INT_DIR}/issued/${OCSP_REQ_NAME}.crt" \
      -rkey "${PKI_INT_DIR}/private/${OCSP_REQ_NAME}.key" \
      -CA "${PKI_INT_DIR}/ca.crt" \
      -text \
      -out "${OCSP_INT_LOG}"
}

case "$1" in
    clean)
        clean
        ;;
    start_root_ocsp)
        mkdir -p "${OCSP_LOGS_DIR}"
        start_root_ocsp_responder
        ;;
    start_int_ocsp)
        mkdir -p "${OCSP_LOGS_DIR}"
        start_int_ocsp_responder
        ;;
    build)
        create_root_ca
        create_int_ca
        create_int_ca_cert
        create_root_ocsp_cert
        create_int_ocsp_cert
        create_leaf_cert
        ;;
    *)
        echo "Error: Invalid option. Valid options: clean, start_root_ocsp, start_int_ocsp, build"
        exit 1
        ;;
esac
