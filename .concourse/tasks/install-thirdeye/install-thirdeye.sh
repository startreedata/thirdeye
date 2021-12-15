#!/bin/bash

wait_service() {
  attempt_counter=0
  max_attempts=600

  until $(curl --output /dev/null --silent --head --fail $1); do
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
  ./kubectl delete ns te-helm-test && \
  echo "pausing for 10s after cleanup" && \
  sleep 10s && \
  ./kubectl create ns te-helm-test && \
  echo HELM_VERSION && \
  ./helm install thirdeye internal/startree-thirdeye --version $HELM_VERSION -n te-helm-test --devel && \
  echo "Waiting for Services availability" && \
  sleep 10s && \
  wait_service http://$(./kubectl get service thirdeye-startree-thirdeye-coordinator -n te-helm-test --output jsonpath='{.status.loadBalancer.ingress[0].hostname}'):8080 && \
  ./helm test thirdeye -n te-helm-test --filter "!name=thirdeye-mysql-test"