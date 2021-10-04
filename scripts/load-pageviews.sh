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
    PINOT_DIST_ROOT="${TE_REPO}/tmp/pinot-bin/apache-pinot-incubating-${PINOT_VERSION}-bin"
fi
cd ${TE_REPO}

# Create a pinot table using a given schema and table config
function add_table() {
  tableConfigFile=$1
  schemaFile=$2
  ${PINOT_DIST_ROOT}/bin/pinot-admin.sh AddTable -tableConfigFile "$tableConfigFile" -schemaFile "$schemaFile"  -controllerHost ${CONTROLLER_HOST} -controllerPort ${CONTROLLER_PORT} -exec
}

# #unused. Please do not remove.
# This utility can be used to upload all segments in a certain directory (compressed or uncompressed)
# Usage: add_segments /path/to/segments
function add_segments() {
  segmentDirectoryPath=$1
  ${PINOT_DIST_ROOT}/bin/pinot-admin.sh UploadSegment -controllerHost "${CONTROLLER_HOST}" -controllerPort "${CONTROLLER_PORT}" -segmentDir "$segmentDirectoryPath"
}

add_table "examples/pageviews/pageviews_offline_table_config.json" "examples/pageviews/pageviews_schema.json"

if [ -z "${JOB_SPEC}" ]; then
    JOB_SPEC=examples/pageviews/ingestionJobSpec.yaml
fi
${PINOT_DIST_ROOT}/bin/pinot-admin.sh LaunchDataIngestionJob -jobSpecFile ${JOB_SPEC} -values controllerHost="${CONTROLLER_HOST}" controllerPort="${CONTROLLER_PORT}"

