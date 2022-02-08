#!/bin/bash

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
  # no java opts: empty string
  ALL_JAVA_OPTS=""
else
  # some java opts: add quotes to avoid globbing/word splitting
  ALL_JAVA_OPTS=$(printf '"%s"' "${JAVA_OPTS}")
fi

function start_server {
  class_ref="org.apache.pinot.thirdeye.ThirdEyeServer"

  echo "Starting Thirdeye server.. config_dir: ${CONFIG_DIR}"
  java ${ALL_JAVA_OPTS} -cp "${CLASSPATH}" ${class_ref} server "${CONFIG_DIR}"/server.yaml
}

function start_ui {
  class_ref="org.apache.pinot.thirdeye.ThirdEyeUiServer"

  java ${ALL_JAVA_OPTS} -cp "${CLASSPATH}" ${class_ref} --port 8081 --proxyHostPort localhost:8080 --resourceBase "${UI_DIR}"
}

MODE=$1
case ${MODE} in
    "server" )  start_server ;;
    "ui"  )          start_ui ;;
    * )              echo "Invalid argument: ${MODE}! Exiting."; exit 1;;
esac
