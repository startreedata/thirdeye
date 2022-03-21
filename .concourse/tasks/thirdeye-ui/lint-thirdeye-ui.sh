#!/bin/bash

if [ $? -ne 0 ]; then
    exit 1
fi

pushd thirdeye-ui && \
  npm run lint-check
