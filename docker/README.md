<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# ThirdEye on Docker
This directory builds a docker image of [CortexData ThirdEye](https://github.com/cortexdata/thirdeye)  

## Build

This section guides you to customize and build the docker image. It is required to build the project with maven before running the Docker build.

Usage:

```SHELL
./mvnw package -D skipTests && docker build -t thirdeye:latest .
```

## How to Run it

The entry point of docker image is `thirdeye.sh` script.

### Create an isolated bridge network in docker. [Optional] 

```SHELL
docker network create -d bridge te-demonet
```

### Start Pinot Batch Quickstart

```SHELL
docker run \
    --network=te-demonet \
    --name pinot-quickstart \
    -p 9000:9000 \
    -d apachepinot/pinot:latest QuickStart \
    -type batch
```

### Start ThirdEye Coordinator
```SHELL
docker run \
    --name  thirdeye-coordinator \
    -p 8081:8080 \
    -d thirdeye:b1 coordinator
```

### Start ThirdEye worker
```SHELL
docker run \
    --name  thirdeye-worker \
    -p 8081:8080 \
    -d thirdeye:b1 worker
```

