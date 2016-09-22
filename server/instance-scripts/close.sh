#!/usr/bin/env bash

SCRIPT_PATH="$(dirname "$0")/"
SERVER_DIR="${SCRIPT_PATH}../"
PID_PATH="${SERVER_DIR}server.pid"

die() {
    echo "$*"
    exit 1
}

[ ! -f ${PID_PATH} ] && die "Server is not running, expected PID file in: ${PID_PATH}"
PID=$(<"$PID_PATH")

echo "Closing server with pid: ${PID}"
kill -15 ${PID}

echo "Waiting for server to close"

while [ -e /proc/${PID} ]
do
    echo "Server still running"
    sleep 1
done
echo "Server closed"
rm ${PID_PATH}
