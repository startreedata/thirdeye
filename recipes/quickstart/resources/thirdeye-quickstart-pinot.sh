#!/bin/bash
#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

if [ -z "${CONTROLLER_HOST}" ]; then
    CONTROLLER_HOST=pinot
fi
if [ -z "${CONTROLLER_PORT}" ]; then
    CONTROLLER_PORT=9000
fi
if [ -z "${DATASET}" ]; then
    DATASET=pageviews
fi

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

# Add dataset to Pinot
add_dataset "/home/thirdeye/examples/${DATASET}"
