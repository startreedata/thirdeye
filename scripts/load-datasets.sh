#!/bin/bash
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

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
TE_REPO="${SCRIPT_DIR}/.."
if [ -z "${PINOT_VERSION}" ]; then
  PINOT_VERSION=0.11.0
fi
if [ -z "${CONTROLLER_PROTOCOL}" ]; then
  CONTROLLER_PROTOCOL=http
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
  "${PINOT_ADMIN_SH}" AddTable -tableConfigFile "$tableConfigFile" -schemaFile "$schemaFile" -controllerProtocol ${CONTROLLER_PROTOCOL} -controllerHost ${CONTROLLER_HOST} -controllerPort ${CONTROLLER_PORT} -exec
}

# #unused. Please do not remove.
# This utility can be used to upload all segments in a certain directory (compressed or uncompressed)
# Usage: add_segments /path/to/segments
function add_segments() {
  segmentDirectoryPath=$1
  "${PINOT_ADMIN_SH}" UploadSegment -controllerProtocol ${CONTROLLER_PROTOCOL} -controllerHost "${CONTROLLER_HOST}" -controllerPort "${CONTROLLER_PORT}" -segmentDir "$segmentDirectoryPath"
}

function add_dataset() {
  table_config="$1/table_config.json"
  schema="$1/schema.json"
  job_spec="$1/ingestion_job_spec.yaml"

  data_dir="$1/rawdata"
  segment_dir="tmp/data/segments/${1##*/}"

  echo "Adding table: $1"
  create_table "$table_config" "$schema"
  "${PINOT_ADMIN_SH}" LaunchDataIngestionJob -jobSpecFile "${job_spec}" -values controllerProtocol="${CONTROLLER_PROTOCOL}" controllerHost="${CONTROLLER_HOST}" controllerPort="${CONTROLLER_PORT}" dataDir="${data_dir}" segmentDir="${segment_dir}"
}

# Add datasets
add_dataset "examples/pageviews"
add_dataset "examples/us_monthly_air_passengers_simplified"
add_dataset "examples/pageviews_with_missing_data"
add_dataset "examples/pageviews_with_nulls"
add_dataset "examples/pageviews_with_floats"
add_dataset "examples/order_events"
add_dataset "examples/new_customers_holiday"
add_dataset "examples/rideshare"

#
# Add a dataset in pinot using REST APIs
# Beta: Use at your own risk!
#
# Creates a dataset in pinot:
# - Step 1: Add schema
# - Step 2: Add table
# - Step 3: Ingest Data
#
function add_table_beta() {
  CONTROLLER_URI="${CONTROLLER_PROTOCOL}://${CONTROLLER_HOST}:${CONTROLLER_PORT}"
  TABLE_NAME_WITH_TYPE="pageviews_OFFLINE"

  curl -F schemaName=@schema.json "${CONTROLLER_URI}/schemas"
  curl -i -X POST -H 'Content-Type: application/json' -d @table_config.json "${CONTROLLER_URI}/tables"
  curl -X POST -F file=@rawdata/data.csv \
    -H "Content-Type: multipart/form-data" \
    --data-urlencode "tableNameWithType=${TABLE_NAME_WITH_TYPE}" \
    --data-urlencode "batchConfigMapStr={\"inputFormat\":\"csv\"}" \
    "${CONTROLLER_URI}/ingestFromFile"
}
