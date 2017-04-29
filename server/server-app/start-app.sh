#!/usr/bin/env bash

java -DassetsAddress=${ASSETS_ADDRESS} \
-DcontainerHost=0.0.0.0 \
-DcontainerHost=${CONTAINER_HOST} \
-Ddatabase.host=${DB_HOST} \
-Ddatabase.name=${DB_NAME} \
-Ddatabase.user=${DB_USER} \
-Ddatabase.password=${DB_PASSWORD} \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.rmi.port=9998 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=${CONTAINER_HOST} \
-DdevMode=false -D -classpath server.jar:lib/* dzida.server.app.GameServer