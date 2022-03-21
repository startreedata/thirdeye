#!/bin/bash

set -x

pushd src && \
  rm -rf ~/.m2 && \
  ln -fs $(pwd)/m2 ~/.m2 && \
  mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec > ../version/version