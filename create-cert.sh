#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# Change the specific details here. Pay close attention to the CA_CN_NAME and the CN_NAME.          #
# Also not that the backslashes (\\) after -subj should probably be replaced for Mac/Unix systems.  #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

C_CODE=ZA
STATE=WesternCape
LOCALITY=Stellenbosch
COMPANY=ExampleCo
UNIT=ExampleCoSecurityDepartment
CA_CN_NAME=example-ca.com
CN_NAME=ExampleCoCN
EMAIL=security@example.com
PASS=123456

# Clear certificates if they exist
rm -Rf envoy/certificates
rm -Rf client/certificates
rm -Rf server/src/main/resources/keystore

# Create directories for certificates
echo Attempting to create directories for certificates...
mkdir envoy/certificates
mkdir client/certificates
mkdir server/src/main/resources/keystore

# Certificate Authority
echo Generating CA key...
openssl genrsa -des3 -passout pass:${PASS} \
        -out envoy/certificates/ca.key 2048

echo Generating CA cetificate...
openssl req -new -x509 -passin pass:${PASS} -days 365 -key envoy/certificates/ca.key \
        -out envoy/certificates/ca.crt -subj "//C=${C_CODE}\ST=${STATE}\L=${LOCALITY}\O=${COMPANY}\OU=${UNIT}\CN=${CA_CN_NAME}"

# Server side key
echo Generate server key...
openssl genrsa -des3 -passout pass:${PASS} \
        -out envoy/certificates/server.key 2048

echo Generating server signing request...
openssl req -new -passin pass:${PASS} -key envoy/certificates/server.key \
        -out envoy/certificates/server.csr -subj "//C=${C_CODE}\ST=${STATE}\L=${LOCALITY}\O=${COMPANY}\OU=${UNIT}\CN=${CN_NAME}"

echo Generating server certificate...
openssl x509 -req -passin pass:${PASS} -days 365 -in envoy/certificates/server.csr \
        -CA envoy/certificates/ca.crt -CAkey envoy/certificates/ca.key -set_serial 01 \
        -out envoy/certificates/server.crt -sha256 -extfile envoy/v3.ext

# Remove servery key passphrase
echo Removing passphrase from server key...
openssl rsa -passin pass:${PASS} -in envoy/certificates/server.key \
        -out envoy/certificates/server.key

echo Creating a keystore for the Spring Boot static file server...

# Create PKCS12 keystore from private key and public certificate
winpty openssl pkcs12 -export -in envoy/certificates/server.crt -inkey envoy/certificates/server.key \
               -out envoy/certificates/server.p12 -name myservercert \
               -passout pass:${PASS} \
               -CAfile envoy/certificates/ca.crt -caname root
# Create a proprietary JKS keystore
keytool -importkeystore \
        -deststorepass ${PASS} -destkeypass ${PASS} -destkeystore envoy/certificates/server.jks \
        -srckeystore envoy/certificates/server.p12 -srcstoretype PKCS12 -srcstorepass ${PASS} \
        -alias myservercert

# Convert from the Java JKS to PKCS12 keystore format
keytool -importkeystore -srckeystore envoy/certificates/server.jks -srcstorepass ${PASS} -srcalias myservercert \
        -destkeystore server/src/main/resources/keystore/server.p12 -destalias grpcwebservercert -deststorepass ${PASS} -deststoretype pkcs12 \
        -noprompt

# Copy certificates to client file server
echo Copying server key and server certificate to client file server...
cp -rf envoy/certificates/server.key client/certificates/server.key
cp -rf envoy/certificates/server.crt client/certificates/server.crt

echo Done! Closing in 3 seconds...
sleep 3
