if [ -z "${PINOT_VERSION}" ]; then 
    PINOT_VERSION=0.7.1
fi
if [[ ! -d "/tmp/pinot-bin/" ]]; then
  wget -O /tmp/pinot-bin.tar.gz https://downloads.apache.org/pinot/apache-pinot-incubating-${PINOT_VERSION}/apache-pinot-incubating-${PINOT_VERSION}-bin.tar.gz
  mkdir /tmp/pinot-bin/
  tar -vxf /tmp/pinot-bin.tar.gz -C /tmp/pinot-bin/
fi
/tmp/pinot-bin/apache-pinot-incubating-${PINOT_VERSION}-bin/bin/quick-start-batch.sh
