#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

set -x

pushd src && \
  rm -rf ~/.m2 && \
  ln -fs $(pwd)/m2 ~/.m2

cat > ~/.m2/settings.xml <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>startree-snapshots</id>
      <username>${MVN_REPOSITORY_USERNAME}</username>
      <password>${MVN_REPOSITORY_PASSWORD}</password>
    </server>
    <server>
      <id>startree-releases</id>
      <username>${MVN_REPOSITORY_USERNAME}</username>
      <password>${MVN_REPOSITORY_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF

./mvnw -U package -DskipTests  -pl '!thirdeye-ui' && \
  ./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec > ../version/version
