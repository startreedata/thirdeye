#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

FROM openjdk:8 AS thirdeye_build_env

LABEL MAINTAINER=dev@pinot.apache.org

ARG BRANCH=master
ARG GIT_URL="git@github.com:cortexdata/thirdeye.git"
RUN echo "Trying to build Thirdeye from [ ${GIT_URL} ] on branch [ ${BRANCH} ]"

# Install maven
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && wget https://www.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -P /tmp \
  && tar -xzf /tmp/apache-maven-*.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven-*.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG /opt/.m2

# Add SSH key to download private repo from GitHub
ARG SSH_PRIVATE_KEY
RUN mkdir /root/.ssh/ && \
    echo "${SSH_PRIVATE_KEY}" > /root/.ssh/id_rsa && \
    chmod 600 /root/.ssh/id_rsa && \
    touch /root/.ssh/known_hosts && \
    ssh-keyscan github.com >> /root/.ssh/known_hosts

# Clone the repo, build and extract the tarball into TE_HOME and remove the repo
ENV TE_REPO=/opt/terepo
ENV TE_HOME=/opt/thirdeye
RUN git clone ${GIT_URL} ${TE_REPO} \
    && cd ${TE_REPO} \
    && git checkout ${BRANCH} \
    && mvn clean install -X -DskipTests || exit 1 \
    && mkdir -p ${TE_HOME} \
    && tar xf ${TE_REPO}/thirdeye-distribution/target/thirdeye-distribution-*-dist.tar.gz -C ${TE_HOME} --strip-components=1 \
    && rm -rf ${TE_REPO}

EXPOSE 8080
WORKDIR ${TE_HOME}
ENTRYPOINT ["./bin/thirdeye.sh"]
