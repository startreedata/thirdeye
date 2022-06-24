#!/bin/sh
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

# Script Usage
# ---------------------------------------------
# ./thirdeye-ce.sh ${MODE}
#
# - MODE: Choices: {server, ui, * }
#       server: Start the server
#       ui: Start the ui server
#       For any other value, the script fails.
#

# Placeholder until license validation is implemented
if [[ -z "${STARTREE_LICENSE}" ]]; then
  echo "The environment var for STARTREE_LICENSE is unset. Please visit https://www.startree.ai/ for a free community-edition license"
  exit 1
fi
source thirdeye.sh
