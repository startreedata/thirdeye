#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TE_REPO="${SCRIPT_DIR}/.."
if [ -z "${PINOT_VERSION}" ]; then
    PINOT_VERSION=0.9.3
fi
if [ -z "${CONTROLLER_HOST}" ]; then
    CONTROLLER_HOST=localhost
fi
if [ -z "${CONTROLLER_PORT}" ]; then
    CONTROLLER_PORT=9000
fi
if [ -z "${PINOT_DIST_ROOT}" ]; then
    PINOT_DIST_ROOT="${TE_REPO}/tmp/pinot-bin/apache-pinot-${PINOT_VERSION}-bin"
fi
cd "${TE_REPO}" || (echo "failed to load" && exit 1)

PINOT_ADMIN_SH="${PINOT_DIST_ROOT}/bin/pinot-admin.sh"

# Create a pinot table using a given schema and table config
function create_table() {
  tableConfigFile=$1
  schemaFile=$2
  "${PINOT_ADMIN_SH}" AddTable -tableConfigFile "$tableConfigFile" -schemaFile "$schemaFile"  -controllerHost ${CONTROLLER_HOST} -controllerPort ${CONTROLLER_PORT} -exec
}

# #unused. Please do not remove.
# This utility can be used to upload all segments in a certain directory (compressed or uncompressed)
# Usage: add_segments /path/to/segments
function add_segments() {
  segmentDirectoryPath=$1
  "${PINOT_ADMIN_SH}" UploadSegment -controllerHost "${CONTROLLER_HOST}" -controllerPort "${CONTROLLER_PORT}" -segmentDir "$segmentDirectoryPath"
}

function add_dataset() {
  table_config="$1/table_config.json"
  schema="$1/schema.json"
  job_spec="$1/ingestion_job_spec.yaml"

  echo "Adding table: $1"
  create_table "$table_config" "$schema"
  "${PINOT_ADMIN_SH}" LaunchDataIngestionJob -jobSpecFile "${job_spec}" -values controllerHost="${CONTROLLER_HOST}" controllerPort="${CONTROLLER_PORT}"
}


# Add datasets
add_dataset "examples/pageviews"
add_dataset "examples/us_monthly_air_passengers_simplified"
add_dataset "examples/pageviews_with_missing_data"
add_dataset "examples/order_events"
