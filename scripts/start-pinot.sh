if [ -z "${PINOT_VERSION}" ]; then 
    PINOT_VERSION=0.7.1
fi

export PINOT_INSTALL_TMP_DIR="/tmp/pinot-bin"
export PINOT_LAUNCH_SH="${PINOT_INSTALL_TMP_DIR}/apache-pinot-incubating-${PINOT_VERSION}-bin/bin/quick-start-batch.sh"

# If pinot quick-start script cannot be found, then reinstall
if [[ ! -f "${PINOT_LAUNCH_SH}" ]]; then

  # remove pinot tmp dir
  rm -rf "${PINOT_INSTALL_TMP_DIR}"

  # Download pinot
  curl -o /tmp/pinot-bin.tar.gz "https://downloads.apache.org/pinot/apache-pinot-incubating-${PINOT_VERSION}/apache-pinot-incubating-${PINOT_VERSION}-bin.tar.gz"
  mkdir "${PINOT_INSTALL_TMP_DIR}"

  # extract pinot
  tar -vxf /tmp/pinot-bin.tar.gz -C "${PINOT_INSTALL_TMP_DIR}/"
fi

# Launch pinot
${PINOT_LAUNCH_SH}
