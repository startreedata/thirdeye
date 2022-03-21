SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
DB_SCRIPTS="${TE_REPO}/thirdeye-persistence/src/main/resources/db"
DB_INIT_SQL_FILE="${DB_SCRIPTS}/db-init.sql"
CREATE_SCHEMA_SQL_FILE="${DB_SCRIPTS}/create-schema.sql"

# Create a database called thirdeye_test for use with thirdeye
mysql -u root -p < "$DB_INIT_SQL_FILE"

# Create the schema
mysql -u root thirdeye_test -p < "$CREATE_SCHEMA_SQL_FILE"
