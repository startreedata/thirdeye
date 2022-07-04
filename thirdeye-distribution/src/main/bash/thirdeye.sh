#!/bin/sh
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

# Script Usage
# ---------------------------------------------
# ./thirdeye.sh ${MODE}
#
# - MODE: Choices: {server, ui, * }
#       server: Start the server
#       ui: Start the ui server
#       For any other value, the script fails.
#

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
UI_DIR="${APP_HOME}/ui"

CLASSPATH=""
for filepath in "${LIB_DIR}"/*; do
  CLASSPATH="${CLASSPATH}:${filepath}"
done

if [ -z "$JAVA_OPTS" ] ; then
  ALL_JAVA_OPTS=""
else
  ALL_JAVA_OPTS="${JAVA_OPTS}"
fi

function start_server {
  class_ref="ai.startree.thirdeye.ThirdEyeServer"

  echo "Starting Thirdeye server.. config_dir: ${CONFIG_DIR}"
  java ${ALL_JAVA_OPTS:+$ALL_JAVA_OPTS}  -cp "${CLASSPATH}" ${class_ref} server "${CONFIG_DIR}"/server.yaml
}

function start_ui {
  class_ref="ai.startree.thirdeye.ThirdEyeUiServer"

  java ${ALL_JAVA_OPTS:+$ALL_JAVA_OPTS} -cp "${CLASSPATH}" ${class_ref} --port 8081 --proxyHostPort localhost:8080 --resourceBase "${UI_DIR}"
}

MODE=$1
case ${MODE} in
    "server" )  start_server ;;
    "ui"  )          start_ui ;;
    * )              echo "Invalid argument: ${MODE}! Exiting."; exit 1;;
esac
