#!/usr/bin/env bash
SCRIPT_PATH=$(dirname "$0")/
POSTGRES_DIR=${SCRIPT_PATH}../server-app/target/postgres
pg_ctl start --pgdata=${POSTGRES_DIR} --log=serverlog