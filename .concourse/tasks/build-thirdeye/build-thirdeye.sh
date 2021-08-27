#!/bin/bash

set -x

pushd src && \
  rm -rf ~/.m2 && \
  ./mvnw -version && \
  ./mvnw -U package -DskipTests  -pl '!thirdeye-ui' && \
  ./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec > ../version/version
