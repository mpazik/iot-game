#!/usr/bin/env bash

SCRIPT_DIR="$(dirname "$0")/"
PROJECT_DIR="${SCRIPT_DIR}../"
SERVER_APP_DIR="${PROJECT_DIR}server-app/"
TARGET_DIR="${SERVER_APP_DIR}target/"

echo "Building project"
mvn -f ${SERVER_APP_DIR} package

echo
echo "Synchronise instance scripts"
rsync --archive --progress --delete ${PROJECT_DIR}instance-scripts/ iot-inst1:~/instance-scripts

echo
echo "Closing running server"
ssh iot-inst1 './instance-scripts/close.sh'

echo
echo "Synchronise dependencies"
rsync --archive --progress --delete ${TARGET_DIR}dependency/ iot-inst1:~/lib

APP_JAR_PATH=$(ls ${TARGET_DIR}server-app-*.jar)
echo
echo "Synchronise application jar"
rsync --archive --progress --delete ${APP_JAR_PATH} iot-inst1:~/server.jar

echo
echo "Starting server"
ssh iot-inst1 './instance-scripts/start.sh &>/dev/null &'