SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
if [ -z "${PINOT_VERSION}" ]; then
    PINOT_VERSION=0.7.1
fi
if [ -z "${CONTROLLER_HOST}" ]; then
    CONTROLLER_HOST=localhost
fi
if [ -z "${CONTROLLER_PORT}" ]; then
    CONTROLLER_PORT=9000
fi
if [ -z "${PINOT_DIST_ROOT}" ]; then
    PINOT_DIST_ROOT=/tmp/pinot-bin/apache-pinot-incubating-${PINOT_VERSION}-bin
fi
cd ${TE_REPO}
${PINOT_DIST_ROOT}/bin/pinot-admin.sh AddTable -tableConfigFile examples/pageviews/pageviews_offline_table_config.json -schemaFile examples/pageviews/pageviews_schema.json  -controllerHost ${CONTROLLER_HOST} -controllerPort ${CONTROLLER_PORT} -exec

if [ -z "${JOB_SPEC}" ]; then
    JOB_SPEC=examples/pageviews/ingestionJobSpec.yaml
fi
${PINOT_DIST_ROOT}/bin/pinot-admin.sh LaunchDataIngestionJob -jobSpecFile ${JOB_SPEC} -values controllerHost="${CONTROLLER_HOST}" controllerPort="${CONTROLLER_PORT}"

