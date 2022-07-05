#
# Copyright 2022 StarTree Inc
#
# Licensed under the StarTree Community License (the "License"); you may not use
# this file except in compliance with the License. You may obtain a copy of the
# License at http://www.startree.ai/legal/startree-community-license
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
# either express or implied.
# See the License for the specific language governing permissions and limitations under
# the License.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
DB_SCRIPTS="${TE_REPO}/thirdeye-persistence/src/main/resources/db"
DB_INIT_SQL_FILE="${DB_SCRIPTS}/db-init.sql"

MYSQL_PORT=${MYSQL_PORT:="3306"}
MYSQL_HOST=${MYSQL_HOST:="127.0.0.1"}

# Create a database called thirdeye and a user uthirdeye
mysql -u root --password= -P ${MYSQL_PORT} --host ${MYSQL_HOST} < "$DB_INIT_SQL_FILE"
