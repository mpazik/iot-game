#!/usr/bin/env bash
SCRIPT_PATH=$(dirname "$0")/
POSTGRES_DIR=${SCRIPT_PATH}../server-app/target/postgres
if [ -d "$POSTGRES_DIR" ]; then
    pg_ctl stop --pgdata=${POSTGRES_DIR} --mode=fast
else
    echo "Postgres directory does not exists. No database to stop."
fi