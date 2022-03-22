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

# setup git itself
git config --global user.email "thirdeye-ci@startree.ai"
git config --global user.name "ThirdEye CI"

pushd src && \
  rm -rf ~/.m2 && \
  ln -fs $(pwd)/m2 ~/.m2

# concourse checkouts a particular commit in detached mode - maven requires to be on a precise branch - get back on branch
git_ref=$(git rev-parse HEAD)
git checkout "$GIT_BRANCH"
master_last_commit=$(git rev-parse HEAD)
if [ $git_ref != $master_last_commit ]; then
  echo "Concourse commit ref $git_ref and runtime master ref $master_last_commit are not the same. This should not happen. Aborting release."
  exit 1
fi

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

./mvnw -B -DskipTests -Darguments=-DskipTests release:clean initialize release:prepare
