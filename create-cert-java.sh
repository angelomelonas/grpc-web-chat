#!/usr/bin/env bash

CERT_OUT_DIR=keystore
JKS_CERT_ALIAS=servercertjks
PKCS12_CERT_ALIAS=servercertpkcs12

echo Creating a keystore for the Spring Boot static file server...

# Create PKCS12 keystore from private key and public certificate
openssl pkcs12 -export -in ${CERT_OUT_DIR}/server.crt -inkey ${CERT_OUT_DIR}/server.key \
               -out ${CERT_OUT_DIR}/server.p12 -name ${JKS_CERT_ALIAS} \
               -passout pass:${PASS} \
               -CAfile ${CERT_OUT_DIR}/ca.crt -caname root

# Create a proprietary JKS keystore (requires Java to be installed)
keytool -importkeystore \
        -deststorepass ${PASS} -destkeypass ${PASS} -destkeystore ${CERT_OUT_DIR}/server.jks \
        -srckeystore ${CERT_OUT_DIR}/server.p12 -srcstoretype PKCS12 -srcstorepass ${PASS} \
        -alias ${JKS_CERT_ALIAS}

# Convert from the Java JKS to PKCS12 keystore format (requires Java to be installed)
keytool -importkeystore -srckeystore ${CERT_OUT_DIR}/server.jks -srcstorepass ${PASS} -srcalias ${JKS_CERT_ALIAS} \
        -destkeystore ${CERT_OUT_DIR}/server.p12 -destalias ${PKCS12_CERT_ALIAS} -deststorepass ${PASS} -deststoretype pkcs12 \
        -noprompt

echo Done! Closing in 3 seconds...
sleep 3
