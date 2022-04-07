#!/bin/bash

DEPLOYMENT_NAME="thirdeye"
TEST_NAMESPACE="te-helm-test"
TE_CHART_NAME="startree-thirdeye"

ping_server() {
  echo "http://$(./kubectl get service ${DEPLOYMENT_NAME}-startree-thirdeye-coordinator -n ${TEST_NAMESPACE} --output jsonpath='{.status.loadBalancer.ingress[0].ip}'):8080"
}

wait_service() {
  attempt_counter=0
  max_attempts=600
  until [ $(curl -o /dev/null -s -w "%{http_code}\n" "$(ping_server)") == "200" ]; do
      if [ ${attempt_counter} -eq ${max_attempts} ];then
        echo "Max attempts reached"
        return 1
      fi

      printf '.'
      attempt_counter=$(($attempt_counter+1))
      sleep 5
  done
}

set -x && \
  ENVIRONMENT_NAME=`cat environment/metadata | jq '.name' -r` && \
  HELM_VERSION=`cat helm-version/version` && \
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
  ./kubectl create ns ${TEST_NAMESPACE} && \
  echo HELM_VERSION && \
  ./helm install ${DEPLOYMENT_NAME} internal/${TE_CHART_NAME} --version $HELM_VERSION -n ${TEST_NAMESPACE} --devel && \
  echo "Waiting for Services availability" && \
  wait_service
