#!/usr/bin/env bash

DB_NAME=$1
DB_USER=$2
SCRIPT_PATH=$(dirname "$0")/
SQL_DIR=${SCRIPT_PATH}../resources/sql/

POSTGRES_DIR=${SCRIPT_PATH}../server-app/target/postgres

if [ ! -d "$POSTGRES_DIR" ]; then
    echo "Database does not exists."
    exit 0;
fi

echo "Clean database."
psql --dbname=${DB_NAME} --username=${DB_USER} --file=${SQL_DIR}drop-tables.sql

echo "Setup tables."
psql --dbname=${DB_NAME} --username=${DB_USER} --file=${SQL_DIR}create-tables.sql

echo "Setup data."
psql --dbname=${DB_NAME} --username=${DB_USER} --file=${SQL_DIR}test/test-data.sql
