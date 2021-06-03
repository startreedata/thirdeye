SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
DB_INIT_SQL_FILE="${TE_REPO}/thirdeye-core/src/main/resources/schema/db-init.sql"
CREATE_SCHEMA_SQL_FILE="${TE_REPO}/thirdeye-core/src/main/resources/schema/create-schema.sql"

# Create a database called thirdeye_test for use with thirdeye
mysql -u root -p < "$DB_INIT_SQL_FILE"

# Create the schema
mysql -u root thirdeye_test -p < "$CREATE_SCHEMA_SQL_FILE"
