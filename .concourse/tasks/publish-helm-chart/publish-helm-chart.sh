#!/bin/bash

if [ -z "$CHART_PATH" ]; then
  echo "CHART_PATH is undefined." 1>&2
  exit 1
fi

if [ -z "$CHART_PREFIX" ]; then
  echo "CHART_PREFIX is undefined." 1>&2
  exit 1
fi

set -x

pushd build && \
  CHART_PATH=${CHART_PREFIX}-$(cat ../chart-version/version).tgz && \
  curl -H "X-JFrog-Art-Api:${HELM_REPOSITORY_TOKEN}" -T ${CHART_PATH} "${HELM_REPOSITORY_URL}/${CHART_PATH}"