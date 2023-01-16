#!/bin/bash
#
# Copyright 2023 StarTree Inc
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

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

cd "${SCRIPT_DIR}" || exit 1

bash ./start-pinot.sh &

PINOT_CONTROLLER_URI="localhost:9000"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' ${PINOT_CONTROLLER_URI})" != "200" ]]; do sleep 2; echo "Waiting for pinot controller.." ; done
echo "Pinot Controller is up. Checking for bootstrap completion.."

BOOTSTRAP_COMPLETE_URI="localhost:9000/tables/baseballStats/size?detailed=true"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' ${BOOTSTRAP_COMPLETE_URI})" != "200" ]]; do sleep 2; echo "Waiting for bootstrap completion.." ; done

echo "Waiting for additional 5 sec.."
sleep 5

source ./load-datasets.sh
