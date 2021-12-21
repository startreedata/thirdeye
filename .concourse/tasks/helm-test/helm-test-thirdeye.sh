#!/bin/bash

DEPLOYMENT_NAME="thirdeye"
TEST_NAMESPACE="te-helm-test"

set -x && \
  ENVIRONMENT_NAME=`cat environment/metadata | jq '.name' -r` && \
  curl https://get.helm.sh/helm-v3.7.1-linux-amd64.tar.gz -O && tar -zxvf helm-*.tar.gz --strip-components=1 && \
  chmod +x helm && \
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
  ./helm test ${DEPLOYMENT_NAME} -n ${TEST_NAMESPACE} --filter "!name=thirdeye-mysql-test"
