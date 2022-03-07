# ThirdEye (Alpha)

This helm chart installs ThirdEye with a Pinot Quickstart Cluster. It sets up the following
components.

- MySQL 8.0 server
- ThirdEye Frontend server
- ThirdEye Backend server

### Prerequisites

- kubectl (<https://kubernetes.io/docs/tasks/tools/install-kubectl>)
- Helm (<https://helm.sh/docs/using_helm/#installing-helm>)
- Configure kubectl to connect to the Kubernetes cluster.
- An already Setup Pinot quickstart cluster.
- Docker Registry Credentials

#### Docker Registry Credentials for Kubernetes

To deploy thirdeye on Kubernetes, you may need access credentials to fetch the docker image from the
startree artifactory. To do that, simply create a kubernetes secret using the vault credentials.

```bash
kubectl create secret docker-registry startree \ 
  --docker-server="repo.startreedata.io/external-docker-registry" \
  --docker-username=<your-name> \
  --docker-password=<your-pword> \
  --docker-email=<your-email>
```

## Installing ThirdEye

This installs thirdeye in the default namespace.

```bash
# fetch dependencies. example: mysql. See Chart.yaml
helm dependency update

# Installs the latest development image of thirdeye
helm install thirdeye .

# Install a released version.
helm install thirdeye -n "${namespace}" . \
  --set image.tag=${VERSION} \
  --set ui.image.tag=${VERSION} \
  --set ui.publicUrl="${THIRDEYE_UI_PUBLIC_URL}" \
  --set smtp.host=${SMTP_HOST} \
  --set smtp.port=${SMTP_PORT} \
  --set smtp.username=${SMTP_USER} \
  --set smtp.password=${SMTP_PASSWORD}

# Upgrade thirdeye
# For example, This upgrades ThirdEye to the latest development image.
helm upgrade --install thirdeye -n "${namespace}" . \
  --set image.tag=${VERSION} \
  --set ui.image.tag=${VERSION} \
  --set ui.publicUrl="${THIRDEYE_UI_PUBLIC_URL}" \
  --set smtp.host=${SMTP_HOST} \
  --set smtp.port=${SMTP_PORT} \
  --set smtp.username=${SMTP_USER} \
  --set smtp.password=${SMTP_PASSWORD}
```

To install in namespace `te`, you can simply pass on the helm arguments directly.

```bash
helm install thirdeye . --namespace te
```

## Uninstalling ThirdEye

Simply run one of the commands below depending on whether you are using a namespace or not.

```bash
helm uninstall thirdeye
helm uninstall thirdeye --namespace te
```

## Configuration

> Warning: The initdb.sql used for setting up the db is currently passed as a helm value to the charts.

Please see `values.yaml` for configurable parameters. Specify parameters
using `--set key=value[,key=value]` argument to `helm install`

Alternatively a YAML file that specifies the values for the parameters can be provided like this:

```bash
helm install thirdeye . --name thirdeye -f values.yaml
```

## Holiday Events

ThirdEye allows you to display events from external Google Calendars. To enable this feature, simply
provide a **base64 encoded** JSON key. Check https://docs.simplecalendar.io/google-api-key/

```bash
helm install thirdeye . --set config.holidayLoaderKey="<base64 encoded key>"
```

## Email Configuration via SMTP

ThirdEye can be configured to fire emails once it set up with an SMTP server. The following example
uses the Google SMTP server.

```bash
  # The ! is escaped with \!
  export SMTP_PASSWORD="password"
  export SMTP_USERNAME="from.email@gmail.com"

  helm install thirdeye . --set smtp.host="smtp.gmail.com" --set smtp.port="465" --set smtp.username=${SMTP_USERNAME} --set smtp.password=${SMTP_PASSWORD}

```
