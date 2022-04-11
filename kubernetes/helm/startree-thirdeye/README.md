# ThirdEye (Alpha)

This helm chart installs ThirdEye with a Pinot Quickstart Cluster. It sets up the following
components.

- MySQL 8.0 server
- ThirdEye UI
- ThirdEye Coordinator
- ThirdEye Worker
- ThirdEye Scheduler

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
```

To install in namespace `te`, you can simply pass on the helm arguments directly.

```bash
helm install thirdeye . --namespace te
```

To see the notifications in action ThirdEye can be configured to fire emails once it set up with an SMTP server. The following example
uses the Google SMTP server.  
Make sure you pass the `ui.publicUrl` as it will be used to form the anomaly page links shared in the email.

```bash
export SMTP_PASSWORD="password"
export SMTP_USERNAME="from.email@gmail.com"
export THIRDEYE_UI_PUBLIC_URL="http://localhost:8081"

helm install thirdeye . \
  --set ui.publicUrl="${THIRDEYE_UI_PUBLIC_URL}" \
  --set smtp.host="smtp.gmail.com" \
  --set smtp.port="465" \
  --set secrets.smtpUsername.value=${SMTP_USERNAME} \
  --set secrets.smtpPassword.value=${SMTP_PASSWORD}

```

## Upgrading ThirdEye
```bash
# For example, This upgrades ThirdEye to the desired development image.
helm upgrade --install thirdeye -n "${namespace}" . \
  --set image.tag=${VERSION} \
  --set ui.image.tag=${VERSION}
```

## Uninstalling ThirdEye

Simply run one of the commands below depending on whether you are using a namespace or not.

```bash
helm uninstall thirdeye
helm uninstall thirdeye --namespace te
```

## Configurations

Please see [values.yaml](values.yaml) for configurable parameters. Specify parameters
using `--set key=value[,key=value]` argument to `helm install`

Alternatively a YAML file that specifies the values for the parameters can be provided like this:

```bash
helm install thirdeye . -f values.yaml
```

### Dynamic Secrets

ThirdEye has plugin infrastructure which allow users to go ahead and create their own plugins. To avoid creating `Secret` 
resources for the sensitive data for each new plugin, it provides a way of creating dynamic secrets as required. Below 
is a snippet from the `values.yaml` file
```yaml
secrets:
  smtpUsername:
    env: SMTP_USER
    value: tobefedexternally
  smtpPassword:
    env: SMTP_PASSWORD
    value: tobefedexternally
  holidayLoaderKey:
    encoded: true
    value: <base 64 encoded json key>
```
1. A single Secret resource will be created which will have data fields corresponding to each entry in `secrets`. 
2. A secret data field will be injected as an environment variable, with name as `env`, in the server pods if we pass the `env`.  
   For example, smtpUsername will be injected as environment variable with variable name as `SMTP_USER` and value as `tobefedexternally`,  
   while `holidayLoaderKey` won't be injected as environment variable.
3. The values will be considered as plain text by default and will be encoded internally unless we provide `encoded: true`, then the value won't be encoded by the charts. 
4. It is recommended to pass values other than simple string (e.g. JSON payload) as `base64` encoded value to avoid any parsing issues.

### Holiday Events

ThirdEye allows you to display events from external Google Calendars. To enable this feature, simply
provide a **base64 encoded** JSON key. Check https://docs.simplecalendar.io/google-api-key/

```bash
helm install thirdeye . \
  --set secrets.holidayLoaderKey.value="<base64 encoded key>" \
  --set secrets.holidayLoaderKey.encoded=true
```
### Custom Calendar List

ThirdEye helm chart has a default list of calendars mentioned below
- en.australian#holiday@group.v.calendar.google.com
- en.austrian#holiday@group.v.calendar.google.com
- en.brazilian#holiday@group.v.calendar.google.com
- en.canadian#holiday@group.v.calendar.google.com
- en.china#holiday@group.v.calendar.google.com
- en.christian#holiday@group.v.calendar.google.com
- en.danish#holiday@group.v.calendar.google.com
- en.dutch#holiday@group.v.calendar.google.com
- en.finnish#holiday@group.v.calendar.google.com
- en.french#holiday@group.v.calendar.google.com
- en.german#holiday@group.v.calendar.google.com
- en.greek#holiday@group.v.calendar.google.com
- en.hong_kong#holiday@group.v.calendar.google.com
- en.indian#holiday@group.v.calendar.google.com
- en.indonesian#holiday@group.v.calendar.google.com
- en.irish#holiday@group.v.calendar.google.com
- en.islamic#holiday@group.v.calendar.google.com
- en.italian#holiday@group.v.calendar.google.com
- en.japanese#holiday@group.v.calendar.google.com
- en.jewish#holiday@group.v.calendar.google.com
- en.malaysia#holiday@group.v.calendar.google.com
- en.mexican#holiday@group.v.calendar.google.com
- en.new_zealand#holiday@group.v.calendar.google.com
- en.norwegian#holiday@group.v.calendar.google.com
- en.philippines#holiday@group.v.calendar.google.com
- en.polish#holiday@group.v.calendar.google.com
- en.portuguese#holiday@group.v.calendar.google.com
- en.russian#holiday@group.v.calendar.google.com
- en.singapore#holiday@group.v.calendar.google.com
- en.sa#holiday@group.v.calendar.google.com
- en.south_korea#holiday@group.v.calendar.google.com
- en.spain#holiday@group.v.calendar.google.com
- en.swedish#holiday@group.v.calendar.google.com
- en.taiwan#holiday@group.v.calendar.google.com
- en.uk#holiday@group.v.calendar.google.com
- en.usa#holiday@group.v.calendar.google.com
- en.vietnamese#holiday@group.v.calendar.google.com

But if you want pass a selected list of calendars it can be done as
```bash
helm install thirdeye . \
  --set config.calendars="{en.australian#holiday@group.v.calendar.google.com,en.austrian#holiday@group.v.calendar.google.com}"
```

### SSL/TLS Support

To enable SSL/TLS on ThirdEye components the prerequisite is to have the certificates injected into the namespace as Secret
- The secrets for server components should have the name as `{component pod name}-internal-tls`
- Each server component secret must have these data fields: `ca.crt`, `tls.crt`, `tls.key`
- The secret for UI component should have name as `{UI pod name}-tls`
- UI secret must have these data fields: `tls.crt`, `tls.key`

SSL/TLS can be configured using
```bash
  --set tls.enabled=true
  --set tls.password=<default is changeit>
```

### OAuth2 Support

Configurations for OAuth2
```yaml
auth:
  enabled: true
  oauth:
    serverUrl: <auth server url>
    keysUrl: <keys url>
    required:
    - sub
    - exp
    exactMatch:
      iss: <issuer url>
    cache:
      size: 64
      ttl: 60000
```
Details

| Property           | Description                                                                                                                                     |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`          | Flag to enable/disable auth                                                                                                                     |
| `oauth.serverUrl`  | OIDC server url. Usually the oidc server has a standard endpoint to expose its metadata which contains info required for keysUrl and issuer url |
| `oauth.keysUrl`    | Endpoint where jwk keys are present                                                                                                             |
| `oauth.required`   | List of claims which are required in the auth token. If any claim from the list is absent, it will considered unauthorised request              |
| `oauth.exactMatch` | List of claims and their expected value. Any inconsistent claim-value pair will end up being an unauthorized request                            |
| `cache.size`       | Cache size in KBs                                                                                                                               |
| `cache.ttl`        | Lifetime of the cache in millis                                                                                                                 |

### Other useful configurations

| Property                 | Description                                                                                                          |
|--------------------------|----------------------------------------------------------------------------------------------------------------------|
| `image.repository`       | Docker repository where ThirdEye server image is present                                                             |
| `image.tag`              | Docker image tag of ThirdEye server image                                                                            |
| `ui.image.repository`    | Docker repository where ThirdEye UI image is present                                                                 |
| `ui.image.tag`           | Docker image tag of ThirdEye UI image                                                                                |
| `ui.port`                | UI service port                                                                                                      |
| `ui.publicUrl`           | Url on which ThirdEye UI is exposed publicly. All the notifications will use this url to share the anomaly page link |
| `scheduler.enabled`      | Flag to run a separate scheduler. If not enabled then coordinator itself will take care of scheduling tasks          |
| `worker.enabled`         | Flag to run a separate worker. If not enabled then coordinator itself will take care of running tasks                |
| `prometheus.enabled`     | Flag to expose prometheus metrics and adding annotations for prometheus to scrape the metrics                        |
| `mysql.mysqlUser`        | Database username                                                                                                    |
| `mysql.mysqlPassword`    | Database password                                                                                                    |
| `mysql.persistence.size` | Size of persistent volume created for database storage                                                               |
| `config.jdbcParameters`  | Config to pass additional parameters to the jdbc connection string                                                   |

Please refer [values.yaml](values.yaml) for default values.
