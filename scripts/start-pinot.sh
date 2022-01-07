#!/bin/bash
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
REPO_DIR="${SCRIPT_DIR}/.."

if [ -z "${PINOT_VERSION}" ]; then
    PINOT_VERSION=0.9.3
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
