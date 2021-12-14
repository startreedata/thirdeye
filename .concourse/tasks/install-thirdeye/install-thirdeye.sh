#!/bin/bash

wait_service() {
  attempt_counter=0
  max_attempts=600
  sleep 10
}

set -x && \
  ENVIRONMENT_NAME=`cat environment/metadata | jq '.name' -r` && \
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
#  ./kubectl delete ns te-helm-test && \
  echo "pausing for 10s after cleanup" && \
  sleep 10s && \
  ./helm repo add internal ${STARTREE_HELM_REPOSITORY_URL} \
    --username ${STARTREE_HELM_USERNAME} \
    --password ${STARTREE_HELM_PASSWORD} && \
  ./helm repo update && \
  ./kubectl create ns te-helm-test && \
  ./helm install thirdeye internal/startree-thirdeye --version 0.0.0-build.6 -n te-helm-test --devel && \
  echo "Waiting for Services availability" && \
  wait_service
