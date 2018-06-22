#!/bin/bash

set -e
ROOT_CA=ca
INTERMEDIATE=int
NODE=pkey
CHAIN=chain
CLUSTER=${1:-172.23.123.176}
USERNAME=${2:-sdkqecertuser}
ADMINCRED=Administrator:password
SSH=ssh
SCP=scp

if [ -f /etc/debian_version ]; then
        SSH="sshpass -p 'couchbase' ssh"
        SCP="sshpass -p 'couchbase' scp"
fi

echo Generate ROOT CA >&2
# Generate ROOT CA
openssl genrsa -out ${ROOT_CA}.key 2048
openssl req -new -x509  -days 3650 -sha256 -key ${ROOT_CA}.key -out ${ROOT_CA}.pem \
-subj '/C=UA/O=My Company/CN=My Company Root CA'

echo Generate intermediate >&2
# Generate intemediate key and sign with ROOT CA
openssl genrsa -out ${INTERMEDIATE}.key 2048
openssl req -new -key ${INTERMEDIATE}.key -out ${INTERMEDIATE}.csr -subj '/C=UA/O=My Company/CN=My Company Intermediate CA'

openssl x509 -req -in ${INTERMEDIATE}.csr -CA ${ROOT_CA}.pem -CAkey ${ROOT_CA}.key -CAcreateserial \
-CAserial rootCA.srl -extfile v3_ca.ext -out ${INTERMEDIATE}.pem -days 365

# Generate client key and sign with ROOT CA and INTERMEDIATE KEY
openssl genrsa -out ${NODE}.key 2048
openssl req -new -key ${NODE}.key -out ${NODE}.csr -subj "/C=UA/O=My Company/CN=${USERNAME}"
openssl x509 -req -in ${NODE}.csr -CA ${INTERMEDIATE}.pem -CAkey ${INTERMEDIATE}.key -CAcreateserial \
-CAserial intermediateCA.srl -out ${NODE}.pem -days 365

# Generate certificate chain file
cat ${NODE}.pem ${INTERMEDIATE}.pem > ${CHAIN}.pem

INBOX=/opt/couchbase/var/lib/couchbase/inbox/
CHAIN=chain.pem

# loop through nodes
hosts=`curl -su Administrator:password http://${CLUSTER}:8091/pools/nodes|/usr/local/bin/jq '.nodes[].hostname'`
arr_hosts=( $hosts )
for host in "${arr_hosts[@]}"
do
    ip=`echo $host|sed 's/\"\([^:]*\):.*/\1/'`
	# Copy private key and chain file to a node:/opt/couchbase/var/lib/couchbase/inbox
	${SSH} root@${ip} "mkdir ${INBOX}" || true
	${SCP} chain.pem root@${ip}:${INBOX}
	${SCP} pkey.key root@${ip}:${INBOX}
	${SSH} root@${ip} "chmod a+x ${INBOX}${CHAIN}"
	${SSH} root@${ip} "chmod a+x ${INBOX}${NODE}.key"

	# Upload ROOT CA and activate it
	curl -X POST --data-binary "@./${ROOT_CA}.pem" \
    	http://${ADMINCRED}@${ip}:8091/controller/uploadClusterCA
	curl -X POST http://${ADMINCRED}@${ip}:8091/node/controller/reloadCertificate

	# Enaable client cert
	curl -X POST  --data-binary "state=enable" http://${ADMINCRED}@${ip}:8091/settings/clientCertAuth
	curl -X POST  --data-binary "delimiter=" http://${ADMINCRED}@${ip}:8091/settings/clientCertAuth
	curl -X POST  --data-binary "path=subject.cn" http://${ADMINCRED}@${ip}:8091/settings/clientCertAuth
	curl -X POST  --data-binary "prefix=" http://${ADMINCRED}@${ip}:8091/settings/clientCertAuth
done

# Create keystore file and import ROOT/INTERMEDIATE/CLIENT cert
KEYSTORE_FILE=keystore.jks
STOREPASS=123456

rm -f ${KEYSTORE_FILE}
keytool -genkey -keyalg RSA -alias selfsigned -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -validity 360 -keysize 2048 -noprompt \
-dname "CN=${USERNAME}, OU=None, O=None, L=None, S=None, C=US" \
-keypass ${STOREPASS}

keytool -certreq -alias selfsigned -keyalg RSA -file my.csr -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt
openssl x509 -req -in my.csr -CA ${INTERMEDIATE}.pem -CAkey ${INTERMEDIATE}.key -CAcreateserial -out clientcert.pem -days 365
keytool -import -trustcacerts -file ${ROOT_CA}.pem -alias root -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt
keytool -import -trustcacerts -file ${INTERMEDIATE}.pem -alias int -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt
keytool -import -keystore ${KEYSTORE_FILE} -file clientcert.pem -alias selfsigned -storepass ${STOREPASS} -noprompt