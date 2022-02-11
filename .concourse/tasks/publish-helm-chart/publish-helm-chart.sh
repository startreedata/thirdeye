#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

if [ -z "$CHART_PREFIX" ]; then
  echo "CHART_PREFIX is undefined." 1>&2
  exit 1
fi

if [ -z "$ARTIFACTORY_URL" ]; then
  echo "ARTIFACTORY_URL is undefined." 1>&2
  exit 1
fi


if [ -z "$ARTIFACTORY_TOKEN" ]; then
  echo "ARTIFACTORY_TOKEN is undefined." 1>&2
  exit 1
fi

curl \
  -H "X-JFrog-Art-Api:${ARTIFACTORY_TOKEN}" \
  -T chart-distrib/*.tgz \
  "${ARTIFACTORY_URL}/$CHART_PREFIX-$(cat chart-version/version).tgz"
