SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
DB_SCRIPTS="${TE_REPO}/thirdeye-persistence/src/main/resources/db"
DB_INIT_SQL_FILE="${DB_SCRIPTS}/db-init.sql"
CREATE_SCHEMA_SQL_FILE="${DB_SCRIPTS}/create-schema.sql"

CONTAINER_NAME="ipca-mysql"
function start_docker_mysql57 {
  PORT=3306
  CONTAINER_IMAGE="mysql/mysql-server:5.7"

	docker run -p $PORT:3306 --restart always --env MYSQL_ROOT_HOST=% --name=ipca-mysql -e MYSQL_ROOT_PASSWORD=admin -d $CONTAINER_IMAGE &&
	sleep 10 && \
	docker run --rm -it --net=host $CONTAINER_IMAGE mysql -h 127.0.0.1 -P $PORT -u root -padmin -e "$(cat "$DB_INIT_SQL_FILE")" && \
	sleep 3 && \
	docker run --rm -it --net=host $CONTAINER_IMAGE mysql -h 127.0.0.1 -P $PORT -u root -padmin -e "Use thirdeye_test; $(cat "$CREATE_SCHEMA_SQL_FILE")"
}

function stop_docker_mysql57 {
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
}

stop_docker_mysql57
sleep 2
start_docker_mysql57
