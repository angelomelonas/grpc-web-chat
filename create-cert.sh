#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# Change the specific details here. Pay close attention to the CA_CN_NAME and the CN_NAME.          #
# Also not that the backslashes (\\) after -subj should probably be replaced for Mac/Unix systems.  #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

PASS=123456

CERT_OUT_DIR=keystore
CERT_OUT_ENVOY=envoy/certificates
CERT_OUT_CLIENT=client/certificates

# Certificate Authority
echo Generating CA key...
openssl genrsa -des3 -passout pass:${PASS} \
        -out ${CERT_OUT_DIR}/ca.key 2048

echo Generating CA cetificate...
openssl req -new -x509 -passin pass:${PASS} -days 365 -key ${CERT_OUT_DIR}/ca.key \
        -out ${CERT_OUT_DIR}/ca.crt -config ${CERT_OUT_DIR}/config/certificate.conf

# Server side key
echo Generate server key...
openssl genrsa -des3 -passout pass:${PASS} \
        -out ${CERT_OUT_DIR}/server.key 2048

echo Generating server signing request...
openssl req -new -passin pass:${PASS} -key ${CERT_OUT_DIR}/server.key \
        -out ${CERT_OUT_DIR}/server.csr -config ${CERT_OUT_DIR}/config/certificate.conf

echo Generating server certificate...
openssl x509 -req -passin pass:${PASS} -days 365 -in ${CERT_OUT_DIR}/server.csr \
        -CA ${CERT_OUT_DIR}/ca.crt -CAkey ${CERT_OUT_DIR}/ca.key -set_serial 01 \
        -out ${CERT_OUT_DIR}/server.crt -sha256 -extfile ${CERT_OUT_DIR}/config/certificate.conf -extensions req_ext

# Remove servery key passphrase
echo Removing passphrase from server key...
openssl rsa -passin pass:${PASS} -in ${CERT_OUT_DIR}/server.key \
        -out ${CERT_OUT_DIR}/server.key

# Copy certificates to client file server and Envoy
echo Copying server key and server certificate to client file server...
cp -rf ${CERT_OUT_DIR}/server.key ${CERT_OUT_CLIENT}/server.key
cp -rf ${CERT_OUT_DIR}/server.crt ${CERT_OUT_CLIENT}/server.crt

echo Copying server key and server certificate to Envoy...
cp -rf ${CERT_OUT_DIR}/server.key ${CERT_OUT_ENVOY}/server.key
cp -rf ${CERT_OUT_DIR}/server.crt ${CERT_OUT_ENVOY}/server.crt

echo Done! Closing in 3 seconds...
sleep 3
