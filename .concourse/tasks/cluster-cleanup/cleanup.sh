#!/bin/bash

TEST_NAMESPACE="te-helm-test"

set -x && \
  ENVIRONMENT_NAME=`cat environment/metadata | jq '.name' -r` && \
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
  chmod +x ./kubectl && \
  set +x && \
  az login \
    --service-principal \
    --username ${AZURE_CLIENT_ID} \
    --password ${AZURE_CLIENT_SECRET} \
    --tenant ${AZURE_TENANT_ID} && \
  az aks get-credentials \
    --name $ENVIRONMENT_NAME \
    --resource-group $ENVIRONMENT_NAME && \
  set -x && \
  ./kubectl delete ns ${TEST_NAMESPACE} && \
  echo "pausing for 10s after cleanup" && \
  sleep 10s
