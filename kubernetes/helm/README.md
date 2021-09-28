
# ThirdEye (Alpha)


This helm chart installs ThirdEye with a Pinot Quickstart Cluster. 
It sets up the following components.
- MySQL 5.7 server
- ThirdEye Frontend server
- ThirdEye Backend server


### Prerequisites

- kubectl (<https://kubernetes.io/docs/tasks/tools/install-kubectl>)
- Helm (<https://helm.sh/docs/using_helm/#installing-helm>)
- Configure kubectl to connect to the Kubernetes cluster.
- An already Setup Pinot quickstart cluster.
- Docker Registry Credentials

#### Docker Registry Credentials for Kubernetes

To deploy thirdeye on Kubernetes, you may need access credentials to fetch the docker image from 
the startree artifactory. To do that, simply create a kubernetes secret using the vault credentials.

```bash
kubectl create secret docker-registry startree \ 
  --docker-server="repo.startreedata.io/external-docker-registry" \
  --docker-username=<your-name> \
  --docker-password=<your-pword> \
  --docker-email=<your-email>
```


## Installing ThirdEye

This installs thirdeye in the default namespace. The `helmw` is a light helm wrapper
that adds additional configuration to get ThirdEye up and running.
```bash
# Installs the latest development image of thirdeye
./helmw install thirdeye .

# Install a released version.
./helmw install thirdeye . --set image.tag=1.3.0 --set ui.image.tag=1.3.0

# Upgrade thirdeye
# For example, This upgrades ThirdEye to the latest development image.
./helmw upgrade --install thirdeye . --set image.tag=latest --set ui.image.tag=latest
```

All arguments passed to this script are forwarded to helm. So to install in namespace `te`, 
you can simply pass on the helm arguments directly.  

```bash
./helmw install thirdeye . --namespace te
```

## Uninstalling ThirdEye

Simply run one of the commands below depending on whether you are using a namespace or not.

```bash
helm uninstall thirdeye
helm uninstall thirdeye --namespace te
```


## Configuration

> Warning: The initdb.sql used for setting up the db may be out of date. Please note that this chart is currently a work in progress.  
 
Please see `values.yaml` for configurable parameters. Specify parameters using `--set key=value[,key=value]` argument to `helm install`

Alternatively a YAML file that specifies the values for the parameters can be provided like this:

```bash
./helmw install thirdeye . --name thirdeye -f values.yaml
```

## Holiday Events

ThirdEye allows you to display events from external Google Calendars. To enable this feature, 
simply provide a JSON key. Check https://docs.simplecalendar.io/google-api-key/

```bash
./helmw install thirdeye . --set-file config.holidayLoaderKey="/path/to/holiday-loader-key.json"
```


## Email Configuration via SMTP

ThirdEye can be configured to fire emails once it set up with an SMTP server. The following example 
uses the Google SMTP server.
```bash
  # The ! is escaped with \!
  export SMTP_PASSWORD="password"
  export SMTP_USERNAME="from.email@gmail.com"

  ./helmw install thirdeye . --set smtp.host="smtp.gmail.com" --set smtp.port="465" --set smtp.username=${SMTP_USERNAME} --set smtp.password=${SMTP_PASSWORD}

```
