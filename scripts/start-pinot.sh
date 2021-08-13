if [ -z "${PINOT_VERSION}" ]; then 
    PINOT_VERSION=0.7.1
fi

export PINOT_INSTALL_TMP_DIR="/tmp/pinot-bin/"

if [[ ! -d "${PINOT_INSTALL_TMP_DIR}" ]]; then
  curl -o /tmp/pinot-bin.tar.gz "https://downloads.apache.org/pinot/apache-pinot-incubating-${PINOT_VERSION}/apache-pinot-incubating-${PINOT_VERSION}-bin.tar.gz"
  mkdir "${PINOT_INSTALL_TMP_DIR}"
  tar -vxf /tmp/pinot-bin.tar.gz -C "${PINOT_INSTALL_TMP_DIR}"
fi
${PINOT_INSTALL_TMP_DIR}/apache-pinot-incubating-${PINOT_VERSION}-bin/bin/quick-start-batch.sh
