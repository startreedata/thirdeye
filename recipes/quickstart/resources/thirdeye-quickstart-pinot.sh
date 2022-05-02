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
if [ -z "${BROKER_HOST}" ]; then
    BROKER_HOST=pinot
fi
if [ -z "${BROKER_PORT}" ]; then
    BROKER_PORT=8000
fi
if [ -z "${COORDINATOR_HOST}" ]; then
    COORDINATOR_HOST=coordinator
fi
if [ -z "${COORDINATOR_PORT}" ]; then
    COORDINATOR_PORT=8080
fi
if [ -z "${DATASET}" ]; then
    DATASET=pageviews
fi

create_datasource() {
  wget http://coordinator:8080/api/data-sources --header "Content-Type: application/json" \
    --post-file /home/thirdeye/resources/pinot-datasource.json
}

onboard_dataset() {
  wget http://coordinator:8080/api/data-sources/onboard-all --post-data "name=pinot"
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

load_default_alert_templates() {
  wget http://coordinator:8080/api/alert-templates/load-defaults --post-data "updateExisting=true"
}

create_alert() {
  wget http://coordinator:8080/api/alerts --header "Content-Type: application/json" \
    --post-file /home/thirdeye/resources/alert.json
}

# Add dataset to Pinot
add_dataset "/home/thirdeye/examples/${DATASET}"

# Create Pinot datasource in ThirdEye
create_datasource

# Onboard dataset from Pinot datasource in ThirdEye
onboard_dataset

# Load the default alert templates into ThirdEye
load_default_alert_templates

# create alert in ThirdEye
create_alert
