#!/bin/bash

if [ -z "$CONCOURSE_TEAM" ]; then
  echo "CONCOURSE_TEAM is undefined. Using \`thirdeye\`"
  export CONCOURSE_TEAM="thirdeye"
fi

export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

set -x

fly \
  -t $CONCOURSE_TEAM \
  set-pipeline \
  -p thirdeye-helm-integration-testing \
  -c $DIR/thirdeye.yml
