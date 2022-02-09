#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
DB_SCRIPTS="${TE_REPO}/thirdeye-persistence/src/main/resources/db"
DB_INIT_SQL_FILE="${DB_SCRIPTS}/db-init.sql"
CREATE_SCHEMA_SQL_FILE="${DB_SCRIPTS}/create-schema.sql"

MYSQL_PORT=${MYSQL_PORT:="3306"}
MYSQL_HOST=${MYSQL_HOST:="127.0.0.1"}

# Create a database called thirdeye_test for use with thirdeye
mysql -u root --password= -P ${MYSQL_PORT} --host ${MYSQL_HOST} < "$DB_INIT_SQL_FILE"

# Create the schema
mysql -u root thirdeye_test --password= -P ${MYSQL_PORT} --host ${MYSQL_HOST} < "$CREATE_SCHEMA_SQL_FILE"
