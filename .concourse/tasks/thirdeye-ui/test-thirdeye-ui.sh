#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

if [ $? -ne 0 ]; then
    exit 1
fi

pushd thirdeye-ui && \
  npm test
