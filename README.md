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

## Developer Guide

Please use Intellij and import ThirdEye as a maven project. Please import the code style from the file `intellij-code-style.xml`.

### Running ThirdEye Coordinator in debug mode
After setting up IntelliJ, navigate to `org.apache.pinot.thirdeye.ThirdEyeServer` class. Press the `play ▶️` icon
and choose debug. This should run the application. However, it would need the right args to start the server.

In the debug configuration settings, set the following values:
- Set program args to `server config/coordinator.yaml`
- Set working dir to `repo/thirdeye/thirdeye-coordinator`

You are all set. Run the debug now (you can just press the debug button) to run the server. By default,
the server should be accessible at http://localhost:8080

### Swagger

ThirdEye Coordinator exposes documentation for most of apis under `/swagger`. By default, the server
should be accessible at http://localhost:8080/swagger

### Obtaining an auth token.

You can get the `accessToken` by hitting the `/auth/login` API with any principal/password combination.
The resulting token obtained in the body of the response can be used to authorize request.

In the `Authorization` header. Something like this
```
Bearer Bearer ${base64encodedjwttoken}
```  

This bash snippet creates a command `localtoken` to output the header value.
```shell script
function localtoken {
  token "http://localhost:8080" "admin" "password"
}

function token {
  host=$1
  principal=$2
  password=$3

  curl -X POST \
    "${host}/api/auth/login" \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d "principal=${principal}&password=${password}&grant_type=password"  2>/dev/null | python -c \
    'import json,sys;print ("Bearer " + json.load(sys.stdin)["accessToken"])'
}
```
Usage:
```shell script
$ localtoken 
Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJwcmluY2lwYWwiOiJhZG1pbiIsImlzcyI6InRoaXJkZXllIiwiZXhwIjoxNjAyNjI4NTA1fQ.hKp-z-BJBrEPc-k_qVgqnitkPeCBvW2bTXGYIFJ5FNunLcQqDhvAj8NRG0Dvgb97YfvC1bxRsg9-S1VEsP6QZg
```
