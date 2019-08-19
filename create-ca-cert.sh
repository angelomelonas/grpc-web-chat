#!/bin/bash

PASS=123456

CERT_OUT_DIR=keystore

# Certificate Authority
echo Generating CA key...
openssl genrsa -des3 -passout pass:${PASS} \
        -out ${CERT_OUT_DIR}/ca.key 2048

echo Generating CA certificate...
openssl req -new -x509 -passin pass:${PASS} -days 365 -key ${CERT_OUT_DIR}/ca.key \
        -out ${CERT_OUT_DIR}/ca.crt -config ${CERT_OUT_DIR}/config/ca-certificate.conf
