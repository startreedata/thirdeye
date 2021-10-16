#!/bin/bash

cat > ~/.npmrc <<EOF
; this will be our temp .npmrc credentials for Artifactory
//repo.startreedata.io/artifactory/api/npm/startree-ui/:_auth=$(echo -n ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD}|base64 -w 0 -)
//repo.startreedata.io/artifactory/api/npm/startree-ui/:email=${ARTIFACTORY_EMAIL}
//repo.startreedata.io/artifactory/api/npm/startree-ui/:always-auth=true
EOF

if [ $? -ne 0 ]; then
    exit 1
fi

pushd thirdeye-ui && \
  npm ci