#!/bin/bash
#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

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
  curl -X POST http://${COORDINATOR_HOST}:${COORDINATOR_PORT}/api/data-sources -H "Content-Type: application/json" \
    --data-binary "@/home/thirdeye/resources/pinot-datasource.json"
}

onboard_dataset() {
  attempt_counter=0
  max_attempts=20
  until [ $(curl -o /dev/null -s -w "%{http_code}\n" -X POST "http://${COORDINATOR_HOST}:${COORDINATOR_PORT}/api/data-sources/onboard-dataset" -d "dataSourceName=pinot&datasetName=${DATASET}") == "200" ]; do
      if [ ${attempt_counter} -eq ${max_attempts} ];then
        echo "Max attempts reached"
        return 1
      fi

      printf '.'
      attempt_counter=$(($attempt_counter+1))
      sleep 5
  done
}

load_default_alert_templates() {
  curl -X POST  http://${COORDINATOR_HOST}:${COORDINATOR_PORT}/api/alert-templates/load-defaults -d "updateExisting=true"
}

create_metric() {
  curl -X POST http://${COORDINATOR_HOST}:${COORDINATOR_PORT}/api/metrics -H "Content-Type: application/json" \
      --data-binary "@/home/thirdeye/resources/metric.json"
}

create_alert() {
  curl -X POST http://${COORDINATOR_HOST}:${COORDINATOR_PORT}/api/alerts -H "Content-Type: application/json" \
    --data-binary "@/home/thirdeye/resources/alert.json"
}

# Create Pinot datasource in ThirdEye
create_datasource

# Onboard dataset from Pinot datasource in ThirdEye
onboard_dataset

# Load the default alert templates into ThirdEye
load_default_alert_templates

# create metric in ThirdEye
create_metric

# create alert in ThirdEye
create_alert
