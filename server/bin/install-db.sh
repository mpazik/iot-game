#!/usr/bin/env bash

DB_NAME=$1
DB_USER=$2
SCRIPT_PATH=$(dirname "$0")/
SQL_DIR=${SCRIPT_PATH}../resources/sql/
echo ${DB_NAME}
echo ${DB_USER}

POSTGRES_DIR=${SCRIPT_PATH}../server-app/target/postgres

if [ -d "$POSTGRES_DIR" ]; then
    echo "Database already exists."
    exit 0;
fi
# Installing Database
mkdir -p ${POSTGRES_DIR}
pg_ctl init --pgdata=${POSTGRES_DIR}

# Installing schema and initial data
pg_ctl start --pgdata=${POSTGRES_DIR} --log=serverlog
echo "Waiting for server to startup."
sleep 3

echo "Setup database."
psql --dbname=postgres --username=$(whoami) --file=${SQL_DIR}test/instal.sql
psql --dbname=${DB_NAME} --username=$(whoami) --file=${SQL_DIR}root-commands.sql

echo "Setup tables."
psql --dbname=${DB_NAME} --username=${DB_USER} --file=${SQL_DIR}create-tables.sql

echo "Setup data."
psql --dbname=${DB_NAME} --username=${DB_USER} --file=${SQL_DIR}test/test-data.sql