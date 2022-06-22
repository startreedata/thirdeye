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

if [ -z "${CONTROLLER_HOST}" ]; then
    CONTROLLER_HOST=pinot
fi
if [ -z "${CONTROLLER_PORT}" ]; then
    CONTROLLER_PORT=9000
fi
if [ -z "${BROKER_HOST}" ]; then
    BROKER_HOST=pinot
fi
if [ -z "${BROKER_PORT}" ]; then
    BROKER_PORT=8000
fi
if [ -z "${DATASET}" ]; then
    DATASET=pageviews
fi

wait_service() {
  attempt_counter=0
  max_attempts=20
  healthy="DOWN"
  while [ "$healthy" != "OK200" ]
  do
      if [ ${attempt_counter} -eq ${max_attempts} ];then
          echo "Max attempts reached"
          return 1
      fi

      healthy="$(curl -sw "%{http_code}\n" "http://${BROKER_HOST}:${BROKER_PORT}/health")"
      printf '.'
      attempt_counter=$(($attempt_counter+1))
      sleep 10
  done
}

# Create a pinot table using a given schema and table config
create_table() {
  tableConfigFile=$1
  schemaFile=$2
  /opt/pinot/bin/pinot-admin.sh AddTable -tableConfigFile "$tableConfigFile" -schemaFile "$schemaFile"  -controllerHost ${CONTROLLER_HOST} -controllerPort ${CONTROLLER_PORT} -exec
}

add_dataset() {
  table_config="$1/table_config.json"
  schema="$1/schema.json"
  job_spec="$1/ingestion_job_spec.yaml"

  echo "Adding table: $1"
  create_table "$table_config" "$schema"
  /opt/pinot/bin/pinot-admin.sh LaunchDataIngestionJob -jobSpecFile "${job_spec}" -values controllerHost="${CONTROLLER_HOST}" controllerPort="${CONTROLLER_PORT}"
}

wait_service

# Add dataset to Pinot
add_dataset "/home/thirdeye/examples/${DATASET}"
