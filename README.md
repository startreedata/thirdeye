# Introduction to ThirdEye
[![Build Status](https://api.travis-ci.org/apache/incubator-pinot.svg?branch=master)](https://travis-ci.org/apache/incubator-pinot) [![license](https://img.shields.io/github/license/linkedin/pinot.svg)](LICENSE)

ThirdEye is an integrated tool for realtime monitoring of time series and interactive root-cause analysis. It enables anyone inside an organization to collaborate on effective identification and analysis of deviations in business and system metrics. ThirdEye supports the entire workflow from anomaly detection, over root-cause analysis, to issue resolution and post-mortem reporting.

## What is it for? (key features)

Online monitoring and analysis of business and system metrics from multiple data sources. ThirdEye comes batteries included for both detection and analysis use cases. It aims to minimize the Mean-Time-To-Detection (MTTD) and Mean-Time-To-Recovery (MTTR) of production issues. ThirdEye improves its detection and analysis performance over time from incremental user feedback.

**Detection**
* Detection toolkit based on business rules and exponential smoothing
* Realtime monitoring of high-dimensional time series
* Native support for seasonality and permanent change points in time series
* Email alerts with 1-click feedback for automated tuning of detection algorithms

**Root-Cause Analysis**
* Collaborative root-cause analysis dashboards
* Interactive slice-and-dice of data, correlation analysis, and event identification
* Reporting and archiving tools for anomalies and analyses
* Knowledge graph construction over time from user feedback

**Integration**
* Connectors for continuous time series data from Pinot, Presto, MySQL and CSV
* Connectors for discrete event data sources, such as holidays from Google calendar
* Plugin support for detection and analysis components

## What it isn't? (limitations)

ThirdEye maintains a dedicated meta-data store to capture data sources, anomalies, and relationships between entities but does not store raw time series data. It relies on systems such as Pinot, Presto, MySQL, RocksDB, and Kafka to obtain both realtime and historic time series data.

ThirdEye does not replace your issue tracker - it integrates with it. ThirdEye supports collaboration but focuses on the data-integration aspect of anomaly detection and root-cause analysis. After all, your organization probably already has a well-oiled issue resolution process that we don't want to disrupt.

ThirdEye is not a generic dashboard builder toolkit. ThirdEye attempts to bring overview data from different sources into one single place on-demand. In-depth data about events, such as A/B experiments and deployments, should be kept in their respective systems. ThirdEye can link to these directly.

## Getting Involved
 
 - Ask questions on [Apache ThirdEye Slack](https://communityinviter.com/apps/apache-thirdeye/apache-thirdeye)

## Documentation

Detailed documentation can be found at [ThirdEye documentation](https://thirdeye.readthedocs.io) for a complete description of ThirdEye's features.

- [Quick Start](https://thirdeye.readthedocs.io/en/latest/quick_start.html)
- [Data Sources Setup](https://thirdeye.readthedocs.io/en/latest/datasources.html)
- [Production Settings](https://thirdeye.readthedocs.io/en/latest/production.html)
- [Alert Setup](https://thirdeye.readthedocs.io/en/latest/alert_setup.html)

## Build

### Prerequisite
ThirdEye requires Java 8.
We recommend using MySQL 5.7 with ThirdEye.
ThirdEye UI requires internal npm packages. Make sure you can access them. See [thirdeye-ui prerequisites](./thirdeye-ui/README.md#configure-node-package-manager-npm-for-use-with-artifactory)

### Database setup
If you have MySQL 5.7 installed, run `scripts/db-setup.sh`. This script uses the `root` user to 
create the database and tables.

Alternatively, you can use docker to launch mysql. Simply execute `scripts/mysql-docker-start.sh`

### Build with Maven

ThirdEye is a maven project and uses standard maven commands.  
You can use the maven wrapper `./mnwv` instead of maven `mvn`.  
```
# Build ThirdEye from source
mvn install

# To skip tests during build
mvn install -DskipTests

# If you are working on backend, You may skip the ui and docs modules
mvn install -pl '!thirdeye-ui' -pl '!thirdeye-docs'

# To Skip Integration tests
mvn install -pl '!thirdeye-integration-tests'
```

### Running ThirdEye from Distribution

ThirdEye builds a tarball and creates an installed dir post build.
```
# cd to the distribution dir
cd thirdeye-distribution/target/thirdeye-distribution-1.0.0-SNAPSHOT-dist/thirdeye-distribution-1.0.0-SNAPSHOT
```

ThirdEye has 3 main components all of which start from a single launcher
- **Coordinator**: This is the API server which exposes a swagger endpoint that will be used in this guide
- **Scheduler**: This is the component that runs the cron jobs and automated pipelines
- **Worker**: This is the component that does all the hard work: running detection tasks and generating anomalies.

```
# WIP: This section needs to be refactored.
#
# Run the coordinator
# To run a scheduler, enable scheduler.enabled: true inside the configuration
# To run a worker, enable taskDriver.enabled: true inside the configuration
bin/thirdeye.sh server
```

### Docker

Once a distribution is ready, you can simply package it into a docker container using the command below.

```SHELL
./mvnw package -D skipTests && docker build -t thirdeye:latest .
```

#### Start ThirdEye Coordinator
```SHELL
docker run \
    --name  thirdeye-coordinator \
    -p 8081:8080 \
    -d thirdeye:latest coordinator
```

#### Start ThirdEye worker
```SHELL
docker run \
    --name  thirdeye-worker \
    -p 8081:8080 \
    -d thirdeye:latest worker
```

## Developer Guide

Please use Intellij and import ThirdEye as a maven project. Please import the code style from the file `intellij-code-style.xml`.

### Running ThirdEye Coordinator in debug mode
After setting up IntelliJ, navigate to `org.apache.pinot.thirdeye.ThirdEyeServer` class. Press the `play ▶️` icon
and choose debug. This should run the application. However, it would need the right args to start the server.

In the debug configuration settings, set the following values:
- Set program args to `server config/server.yaml`

You are all set. Run the debug now (you can just press the debug button) to run the server. By default,
the server should be accessible at http://localhost:8080

### Swagger

ThirdEye Coordinator exposes documentation for most of apis under `/swagger`. By default, the server
should be accessible at http://localhost:8080/swagger

### ThirdEye Release

ThirdEye uses `maven-release-plugin` to do it's releases.

#### Starting with a clean slate
You can start with a fresh slate using the command below. This cleans up all temporary files 
generated by the release plugin.
```
mvn release:clean
```

#### Perform the Release

> **Note! Please Note that this will create 2 commits AND PUSH to origin triggering the pipelines!**

Switches:
- `initialize` goal sets up the next version of ThirdEye by incrementing the
minor version (default). Major releases are done differently.
- `-B` switch turns on batch mode and skips all prompts. ThirdEye is already configured so you can 
simply use the batch mode.
- `-e` spits out more context if errors occur.
```
mvn -B -DskipTests -Darguments=-DskipTests release:clean initialize release:prepare release:perform -DdryRun=true
```
Skip the `-DdryRun=true` to cut a release.
