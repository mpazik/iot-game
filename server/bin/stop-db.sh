#!/usr/bin/env bash
SCRIPT_PATH=$(dirname "$0")/
POSTGRES_DIR=${SCRIPT_PATH}../server-app/target/postgres
if [ -d "$POSTGRES_DIR" ]; then
    pg_ctl stop --pgdata=${POSTGRES_DIR} --mode=fast
    exit 0 # if directory exist but postgress is not running, stopping postgres returns error code
else
    echo "Postgres directory does not exists. No database to stop."
fi