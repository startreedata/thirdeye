#!/bin/bash

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

set -x
# github ssh setup
apt-get update && apt-get -y install ssh-client
mkdir ~/.ssh
ssh-keyscan github.com > ~/.ssh/known_hosts
set +x
echo "${GITHUB_PRIVATE_KEY}" > ~/.ssh/github_key
set -x
chmod 400 ~/.ssh/github_key

cat > ~/.ssh/config <<EOF
Host github.com
 HostName github.com
 IdentityFile ~/.ssh/github_key
EOF

if (ssh -T git@github.com 2>&1 | grep -q 'successfully'); then
  echo "SSH connection to github successful."
else
  echo "Could not connect to github with ssh" || exit 1
fi

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

./mvnw -B -DskipTests -Darguments=-DskipTests release:clean initialize release:prepare -DdryRun=true
