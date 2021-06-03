if [ -z "${PINOT_VERSION}" ]; then 
    PINOT_VERSION=0.7.1
fi
PINOT_VERSION=0.7.1
wget -O /tmp/pinot-bin.tar.gz https://downloads.apache.org/incubator/pinot/apache-pinot-incubating-${PINOT_VERSION}/apache-pinot-incubating-${PINOT_VERSION}-bin.tar.gz
mkdir /tmp/pinot-bin/
tar -vxf /tmp/pinot-bin.tar.gz -C /tmp/pinot-bin/
/tmp/pinot-bin/apache-pinot-incubating-${PINOT_VERSION}-bin/bin/quick-start-batch.sh

