#!/usr/bin/env bash

SCRIPT_PATH="$(dirname "$0")/"
SERVER_DIR="${SCRIPT_PATH}../"
DB_CREDENTIAL_PATH="${SERVER_DIR}db-credential"

die() {
    echo "$*"
    exit 1
}

[ ! -f ${DB_CREDENTIAL_PATH} ] && die "Expected data-base credential file in path: ${DB_CREDENTIAL_PATH}"
source ${DB_CREDENTIAL_PATH}

[ -z ${DB_NAME} ] && die "Data-base credential file should export DB_NAME variable"
[ -z ${DB_USER} ] && die "Data-base credential file should export DB_USER variable"
[ -z ${DB_PASSWORD} ] && die "Data-base credential file should export DB_PASSWORD variable"

nohup java -DassetsAddress=http://assets.islesoftales.com \
-DcontainerHost=ec2-54-93-37-232.eu-central-1.compute.amazonaws.com \
-Ddatabase.host=iot-inst1.c16tsjrk308r.eu-central-1.rds.amazonaws.com:5432 \
-Ddatabase.name=${DB_NAME} \
-Ddatabase.user=${DB_USER} \
-Ddatabase.password=${DB_PASSWORD} \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9998 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=ec2-52-59-242-56.eu-central-1.compute.amazonaws.com \
-DdevMode=false -D -classpath ${SERVER_DIR}server.jar:${SERVER_DIR}lib/* dzida.server.app.GameServer \
2>&1 >> logfile.log & echo $! > ${SERVER_DIR}server.pid