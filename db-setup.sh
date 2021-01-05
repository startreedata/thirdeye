SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}"

# Create a database called thirdeye_test for use with thirdeye
mysql -u root -p < "${TE_REPO}/thirdeye-core/src/main/resources/schema/db-init.sql"

# Create the schema
mysql -u root thirdeye_test -p < "${TE_REPO}/thirdeye-core/src/main/resources/schema/create-schema.sql"
