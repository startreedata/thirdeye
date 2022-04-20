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

## Documentation

Detailed documentation can be found at [ThirdEye documentation](https://dev.startree.ai/docs/thirdeye/) for a complete description of ThirdEye's features.

- [Getting started](https://dev.startree.ai/docs/thirdeye/getting-started/)
- [Data Sources Setup](https://dev.startree.ai/docs/thirdeye/how-tos/database/)
- [Alert Setup](https://dev.startree.ai/docs/thirdeye/concepts/alert-configuration)

## Build

### Prerequisite
ThirdEye requires Java 8.
We recommend using MySQL 8.0 with ThirdEye.
ThirdEye UI requires internal npm packages. Make sure you can access them. See [thirdeye-ui prerequisites](./thirdeye-ui/README.md#configure-node-package-manager-npm-for-use-with-artifactory)

### Database setup
If you have MySQL 8.0 installed, run `scripts/db-setup.sh`. This script uses the `root` user to 
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

# If you are working on backend, You may skip the ui module
mvn install -pl '!thirdeye-ui'

# To Skip Integration tests
mvn install -pl '!thirdeye-integration-tests'
```

### Running ThirdEye from Distribution

ThirdEye builds a tarball and creates an installed dir post build.  
ThirdEye has 3 main components all of which start from a single launcher  
- **Coordinator**: This is the API server which exposes a swagger endpoint that will be used in this guide
- **Scheduler**: This is the component that runs the cron jobs and automated pipelines
- **Worker**: This is the component that does all the hard work: running detection tasks and generating anomalies.
```
# cd to the distribution dir
cd thirdeye-distribution/target/thirdeye-distribution-1.0.0-SNAPSHOT-dist/thirdeye-distribution-1.0.0-SNAPSHOT

# Run the coordinator
# To run a scheduler, enable scheduler.enabled: true inside the configuration
# To run a worker, enable taskDriver.enabled: true inside the configuration

# To load plugins, export the plugin directory
export THIRDEYE_PLUGINS_DIR="${PWD}/plugins"

# Run the server
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
After setting up IntelliJ, navigate to `ai.startree.thirdeye.ThirdEyeServer` class. Press the `play ▶️` icon
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

### Adding Copyright message

In IntelliJ, navigate to `Editor > Copyright > Copyright Profiles` and create a new profile.
Update settings to apply this by default to all source files.

```java
/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
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

# License FAQ

## Context
ThirdEye's open-source solution has been proven to be very useful for anomaly detection and root cause analysis across different companies (eg: LinkedIn, Confluent). As part of the community, we have seen more and more users have shown interest in using ThirdEye and looking for dedicated support. Currently, this is a sub-project** under Apache Pinot with no strong leadership. Hence, StarTree decided to take responsibility for  ThirdEye and make its source available under the StarTree Community License, with the goal to build a thriving community around ThirdEye within the StarTree and Apache Pinot communities.

**Note: The old open-source ThirdEye (Github: https://github.com/project-thirdeye/thirdeye) re-uses the license used for Pinot and is tightly coupled with Pinot. Developers need to fork both PInot and ThirdEye to make it work. Hence for these reasons, it is called a sub-project even though in GitHub it is an independent project.

## Problem: What problem is this solving?
As we consider taking over leadership of this project we will need to:
Rebuild the community
Increase community awareness
Increase community adoption

## Scope
ThirdEye will be “source available” under the StarTree Community License. This means external partners will be able to view, change, and share the code. However, the license will have a limitation designed to sustain our stewardship of the project, which will prevent anyone from offering this as a cloud service or SaaS solution or as software).

## How will the StarTree IP be protected?
Leverage a plugin mechanism to ensure our critical algorithms are still proprietary and confidential.


## StarTree ThirdEye Community License FAQ

### What is the new StarTree ThirdEye Community License?
StarTree is taking leadership of ThirdEye which is currently a sub-project under Apache Pinot and converting it to a source-available license.

### Tell me what this means
Under the StarTree Community License, you can access the source code and modify or redistribute it; there is only one thing you cannot do, and that is using for the “Excluded Purpose”  to make a competing SaaS offering. Here is the exact language:

> “Excluded Purpose” is making available any software-as-a-service, platform-as-a-service, infrastructure-as-a-service, or other similar online services that compete with StarTree products or services that provide the Software.

For example, it does not allow the hosting of ThirdEye under the StarTree Community License as online service offerings that compete with StarTree SaaS products or services that provide the same software.  If you are not doing what is excluded, this license change will not affect you.

### Does this apply to Apache Pinot and existing open-source ThirdEye (sub-project under Apache Pinot)?
No. The license change only applies to our StarTree ThirdEye that was previously under Apache Pinot as a sub-project. Any versions of ThirdEye that were previously released under the Apache 2.0 license are still available under that license.

### Will StarTree continue to contribute to old ThirdEye under Apache Pinot?
Absolutely. StarTree will take leadership of ThirdEye and will continue to make contributions to the development, maintenance, and improvement of ThirdEye.

### Why did we do this?
We think it is a necessary step. This lets us invest heavily in code that we distribute for free, while sustaining a healthy business that funds this investment.

We aren’t alone in looking at alternative licensing mechanisms. Other companies have recently gone through licensing adjustments, attempting to solve for the problem of public clouds reaping rewards for serving software while not supporting the software developers.

### Can I download, modify, or redistribute the code?
Yes. The new code with enhanced architecture will be published under a new GitHub location.

### Can I embed StarTree ThirdEye Community software in the software I distribute?
Yes, you can.

### Can I embed StarTree ThirdEye Community software in a SaaS offering I create?
Yes, provided the SaaS offering does not fall within the “Excluded Purpose” discussed above.

### Is StarTree ThirdEye Community License open source?
Strictly speaking, it is “source-available.” Many people use the phrase “open source” in a loose sense to mean that you can freely download, modify, and redistribute the code, and those things are all true of the code under the StarTree ThirdEye Community License. However, in the strictest sense “open source” means a license that meets the Open Source Definition, or that is approved by the Open Source Initiative (“OSI”). The StarTree ThirdEye Community License is not approved by the OSI and likely would not be as it excludes the use case of creating a SaaS offering of the code. Because of this, we will not refer to the StarTree ThirdEye Community License or any code released under it as open source.

### What if I create a competing service but give it away for free. Is that “competitive?”
Yes. That’s just a competitive product with a price of zero. “Competitive” means that a product or service is an economic substitute.

### I’m confused about what use cases are competitive. What if future StarTree ThirdEye products compete with mine?
Let’s go through a specific example. Say that you are building a SaaS eCommerce Order Management Application and you want to include ThirdEye in the implementation of that offering. Of course, you can do that, this service does not compete with any StarTree ThirdEye product that “provides the software”. Note that this would be true, even if in the future StarTree ThirdEye had its own eCommerce Order Management Application (not likely!). The excluded purpose for ThirdEye is limited to competition with StarTree ThirdEye’s SaaS offering of ThirdEye. And on a practical level, if you are marketing your software as an alternative for our software, that means it is competitive.

### When will the StarTree ThirdEye Community License be effective?
It will apply to StarTree ThirdEye Platform initial and later commits. Previous commits to github ([link](https://github.com/project-thirdeye/thirdeye/commit/1b9dfb57d4113bce37b8f3708394b0a9f0594329))  and bug fixes to such commits (if any) will remain under the sub-project of Apache Pinot and its respective licenses.

### Can I still use older versions of the StarTree ThirdEye Open Source Software?
Yes. Our new license release does not affect your rights to keep using the software you received in the past under the old ThirdEye sub-project of Apache Pinot under Apache 2.0. So, if you are still using the previous version, you can continue to use it in the same way you have been using it. And of course, our new release doesn’t affect the licensing for Apache Pinot or old ThirdEye at all.

That said, we may not be releasing updates to the old ThirdEye Open Source code under Apache Pinot, and it may become outdated or insecure. We don’t recommend being left behind, and we encourage you to migrate to the latest version of StarTree ThirdEye Platform (link to StarTree Developer Hub).

### My company has a policy against using code with a non-commercial restriction. What should I do?
The StarTree ThirdEye Community License is not a “non-commercial” license restriction. It only prevents one narrow kind of excluded purpose, which is using our software in a competing SaaS offering. If your company lawyers have concerns about our license, we would be happy to discuss it. Just [contact us here](https://www.startree.ai/contact-us).

### I’d like to customize some of the StarTree ThirdEye Community software. Can I?
Yes. The Excluded Purpose does not restrict the creation of modifications.

### Why didn’t StarTree ThirdEye use AGPL?
AGPL doesn’t solve the problem we are trying to fix. AGPL allows cloud service providers to sell services using the exact software being licensed, and charge for it, without any limitation. This means the software developer has become the unpaid developer and maintainer for the cloud service provider—which is not a scenario we want to enable.

Also, AGPL is not suitable for our customers who need to redistribute commercial products that include our software.  If you put AGPL code in a distributed program, you have to open-source the whole program. We want you to be able to embed our code in proprietary applications, change it and not worry about open sourcing any of your changes. We don’t think that proprietary applications are bad, and we think it’s great if you use StarTree ThirdEye Community software to create them.

### Why didn’t you use the Commons Clause?
Commons Clause is an approach that adds an anti-SaaS provision to existing open-source licenses, which can make interpreting the license confusing. We felt it was better to create a license for the specific purpose we intended. If Commons Clause or another such mechanism emerges as a common standard for solving the problem we’re targeting, we will gladly consider it.

### We have an Enterprise subscription. How does this change affect us?
If you have entered into a separate enterprise agreement with us, the new release will not restrict your rights under such agreement. For any specific questions, please reach out to your account representative.

### Does the StarTree ThirdEye Community License impose a general prohibition against competing with StarTree ThirdEye?
It does not. The excluded purpose is the following:

“Making available any software-as-a-service, platform-as-a-service, infrastructure-as-a-service or other similar online services that compete with StarTree ThirdEye products or services that provide the software”

This may be easier to understand with a specific example. Say “the software” that you are licensing is ThirdEye. Then the excluded purpose for your use of ThirdEye could be read as

“Making available any software-as-a-service, platform-as-a-service, infrastructure-as-a-service or other similar online services that compete with StarTree ThirdEye products or services that provide [ThirdEye capabilities]”.

In other words, it’s a limitation on your use of ThirdEye to compete with our ThirdEye offering. You are not forbidden from competing with other products StarTree ThirdEye has or may have in the future that you have not licensed under the StarTree ThirdEye Community License.

### Is the StarTree ThirdEye Community License a EULA?
No, a EULA only gives you the right to use; the StarTree ThirdEye Community License grants other rights as well.




