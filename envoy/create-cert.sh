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
PASS=1234

# Certificate Authority
echo ------------ Generate CA key: ------------
 openssl genrsa -des3 -passout pass:${PASS} -out pem/ca.key 2048

echo ------------ Generate CA certificate: ------------
openssl req -new -x509 -passin pass:${PASS} -days 365 -key pem/ca.key -out pem/ca.crt -subj "//C=${C_CODE}\ST=${STATE}\L=${LOCALITY}\O=${COMPANY}\OU=${UNIT}\CN=${CA_CN_NAME}"

# Server side key
echo ------------ Generate server key: ------------
openssl genrsa -des3 -passout pass:${PASS} -out pem/server.key 2048

echo ------------ Generate server signing request: ------------
openssl req -new -passin pass:${PASS} -key pem/server.key -out pem/server.csr -subj "//C=${C_CODE}\ST=${STATE}\L=${LOCALITY}\O=${COMPANY}\OU=${UNIT}\CN=${CN_NAME}"

echo ------------ Server certificate: ------------
openssl x509 -req -passin pass:${PASS} -days 365 -in pem/server.csr -CA pem/ca.crt -CAkey pem/ca.key -set_serial 01 -out pem/server.crt -sha256 -extfile v3.ext

echo ------------ Remove passphrase from server key: ------------
openssl rsa -passin pass:${PASS} -in pem/server.key -out pem/server.key

echo ------------ Copy key and cert to client for dev server: ------------
cp -rf pem/server.key ../client/pem/server.key
cp -rf pem/server.crt ../client/pem/server.crt
