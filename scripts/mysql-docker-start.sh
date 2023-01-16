#!/bin/bash
#
# Copyright 2023 StarTree Inc
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

CONTAINER_NAME="ipca-mysql"
function start_docker_mysql80 {
  PORT=3306
  CONTAINER_IMAGE="mysql/mysql-server:8.0"

	docker run -p $PORT:3306 --restart always --env MYSQL_ROOT_HOST=% --name=ipca-mysql -e MYSQL_ROOT_PASSWORD=admin -d $CONTAINER_IMAGE &&
	sleep 10 && \
	docker run --rm -it --net=host $CONTAINER_IMAGE mysql -h 127.0.0.1 -P $PORT -u root -padmin -e "$(cat "$DB_INIT_SQL_FILE")"
}

function stop_docker_mysql80 {
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
}

stop_docker_mysql80
sleep 2
start_docker_mysql80
