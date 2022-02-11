#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

if [ -z "$CHART_DIR" ]; then
  echo "CHART_DIR is undefined." 1>&2
  exit 1
fi

set -x

export CHART_DIR=$(realpath "chart-src/${CHART_DIR}")
export CHART_PATH="${CHART_DIR}/Chart.yaml"

if [ ! -f "${CHART_PATH}" ]; then
  echo "Could not find a chart at $CHART_PATH." 1>&2
  exit 1
fi

if [ ! -f "version/version" ]; then
  echo "Could not find a version at version/version." 1>&2
  exit 1
fi

VERSION=$(cat version/version)

pushd $CHART_DIR && \
  yq --version && \
  NEW_CHART_CONTENT=$(yq w Chart.yaml version ${VERSION}) && \
  echo "$NEW_CHART_CONTENT" > Chart.yaml && \
  INITDBSQL=$(cat ./config/initdb.sql) yq e -i '.mysql.initializationFiles."initdb.sql" = strenv(INITDBSQL)' values.yaml && \
  helm dependency update && \
  helm package . && \
popd && \
mv $CHART_DIR/*.tgz build/ 
