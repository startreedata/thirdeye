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
REPO_DIR="${SCRIPT_DIR}/.."

if [ -z "${PINOT_VERSION}" ]; then
    PINOT_VERSION=0.11.0
fi

export PINOT_INSTALL_TMP_DIR="${REPO_DIR}/tmp/pinot-bin"
export PINOT_DIR="${PINOT_INSTALL_TMP_DIR}/apache-pinot-${PINOT_VERSION}-bin"
export PINOT_LAUNCH_SH="${PINOT_DIR}/bin/quick-start-batch.sh"

# If pinot quick-start script cannot be found, then reinstall
if [[ ! -f "${PINOT_LAUNCH_SH}" ]]; then

  # remove pinot tmp dir
  rm -rf "${PINOT_INSTALL_TMP_DIR}"

  # Download pinot
  curl -o /tmp/pinot-bin.tar.gz "https://downloads.apache.org/pinot/apache-pinot-${PINOT_VERSION}/apache-pinot-${PINOT_VERSION}-bin.tar.gz"
  mkdir -p "${PINOT_INSTALL_TMP_DIR}"

  # extract pinot
  tar -vxf /tmp/pinot-bin.tar.gz -C "${PINOT_INSTALL_TMP_DIR}/"
fi

# Launch pinot
cd "${PINOT_DIR}" && ${PINOT_LAUNCH_SH}
