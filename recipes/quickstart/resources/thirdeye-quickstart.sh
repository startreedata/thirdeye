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

# create alert in ThirdEye
create_alert
