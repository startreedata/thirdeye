#!/bin/bash

if [ $? -ne 0 ]; then
    exit 1
fi

pushd projects/my-apps-ui \
    && npx cypress install \
    && npm run test-e2e -- --config baseUrl=$CYPRESS_BASE_URL --env username=$USERNAME,password=$PASSWORD,clientSecret=$CLIENT_SECRET
