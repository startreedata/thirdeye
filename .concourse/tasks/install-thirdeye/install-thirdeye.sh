#!/bin/bash

wait_service() {
  attempt_counter=0
  max_attempts=600
  sleep 10
}

set -x && \
  ENVIRONMENT_NAME=`cat environment/metadata | jq '.name' -r` && \
  ls helm-version && \
  HELM_VERSION=`cat helm-version` && \
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
  chmod +x ./kubectl && \
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
  ./helm repo add internal ${STARTREE_HELM_REPOSITORY_URL} \
    --username ${STARTREE_HELM_USERNAME} \
    --password ${STARTREE_HELM_PASSWORD} && \
  ./helm repo update && \
  ./kubectl delete ns te-helm-test && \
  echo "pausing for 10s after cleanup" && \
  sleep 10s && \
  ./kubectl create ns te-helm-test && \
  echo HELM_VERSION && \
  ./helm install thirdeye internal/startree-thirdeye --version HELM_VERSION -n te-helm-test --devel && \
  echo "Waiting for Services availability" && \
  wait_service && \
  ./helm test thirdeye -n te-helm-test --filter "!name=thirdeye-mysql-test"