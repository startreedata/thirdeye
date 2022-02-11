#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

if [ -z "$CONCOURSE_TEAM" ]; then
  echo "CONCOURSE_TEAM is undefined. Using \`startree-ui\`"
  export CONCOURSE_TEAM="startree-ui"
fi

export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

set -x

fly \
  -t $CONCOURSE_TEAM \
  set-pipeline \
  -p thirdeye-ui \
  -c $DIR/thirdeye-ui.yml \
  -l $DIR/tasks/thirdeye-ui/thirdeye-vars.yaml
