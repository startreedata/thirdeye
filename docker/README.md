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

## Building the image

There is a docker build script which will build a given Git repo/branch and tag the image.

Usage:

```SHELL
docker build -t ${DOCKER_TAG} --build-arg BRANCH=${BRANCH} --build-arg GIT_URL=${GIT_URL} -f Dockerfile .
```

This tags the image with `${DOCKER_TAG}`.

- `DOCKER_TAG`: Name and tag your docker image. Default is `thirdeye:latest`.
- `BRANCH`: The branch to build. Default is `master`.
- `GIT_URL`: The HTTPS Git repo url. Default: `https://github.com/cortexdata/thirdeye`
- `SSH_PRIVATE_KEY`: Insert your ssh key here if this is a private GitHub repo.

Example:
```
# Using defaults
docker build -t thirdeye:latest
```

```SHELL
# If you want to customize the build
docker build \
    --tag thirdeye:latest \
    --build-arg BRANCH=master \
    --build-arg GIT_URL="https://github.com/cortexdata/thirdeye" \
    --build-arg SSH_PRIVATE_KEY="$(cat ~/.ssh/id_rsa)" \
    -f Dockerfile .
```

## How to publish a docker image

Script `docker-push.sh` publishes a given docker image to your docker registry.

In order to push to your own repo, the image needs to be explicitly tagged with the repo name.

* Example of publishing a image to [apachepinot/thirdeye](https://cloud.docker.com/u/apachepinot/repository/docker/apachepinot/thirdeye) dockerHub repo.

```SHELL
docker tag thirdeye:latest apachepinot/thirdeye:latest
./docker-push.sh apachepinot/thirdeye:latest
```

Script `docker-build-and-push.sh` builds and publishes this docker image to your docker registry after build.

* Example of building and publishing a image to [apachepinot/thirdeye](https://cloud.docker.com/u/apachepinot/repository/docker/apachepinot/thirdeye) dockerHub repo.

```SHELL
./docker-build-and-push.sh apachepinot/thirdeye:latest master https://github.com/apache/incubator-pinot.git
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

