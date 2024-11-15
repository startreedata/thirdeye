#
# Copyright 2024 StarTree Inc
#
# Licensed under the StarTree Community License (the "License"); you may not use
# this file except in compliance with the License. You may obtain a copy of the
# License at http://www.startree.ai/legal/startree-community-license
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
# either express or implied.
# See the License for the specific language governing permissions and limitations under
# the License.
#

FROM eclipse-temurin:21-jdk-alpine as builder
# build jcmd tools to make them available at runtime
RUN ${JAVA_HOME}/bin/jlink --module-path jmods --add-modules jdk.jcmd --output /jcmd
WORKDIR /build
RUN apk add --no-cache git
COPY ./ ./
# if the disitrbution is provided, do nothing - else build it
RUN if [[ ! -d thirdeye-distribution/target/thirdeye-distribution-*-dist/thirdeye-distribution-* ]]; then ./mvnw package -U -DskipTests; fi

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1000 thirdeye && \
  adduser -u 1000 thirdeye -G thirdeye -D

USER thirdeye
WORKDIR /home/thirdeye/thirdeye

EXPOSE 8080
EXPOSE 8081
EXPOSE 8443

COPY --from=builder --chown=1000:1000 /build/thirdeye-distribution/target/thirdeye-distribution-*-dist/thirdeye-distribution-*/ ./
# add jcmd tools
COPY --from=builder --chown=1000:1000 /jcmd /jcmd
ENV PATH="${PATH}:/jcmd/bin"

ENTRYPOINT ["sh", "bin/thirdeye.sh"]
