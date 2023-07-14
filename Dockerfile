#
# Copyright 2023 StarTree Inc
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

FROM adoptopenjdk/openjdk11:alpine as builder
WORKDIR /build
RUN apk add --no-cache git
COPY ./ ./
RUN ./mvnw -T 1C install -DskipTests


FROM adoptopenjdk/openjdk11:alpine
RUN addgroup -g 1000 thirdeye && \
  adduser -u 1000 thirdeye -G thirdeye -D -H

USER thirdeye
WORKDIR /app

EXPOSE 8080
EXPOSE 8081
EXPOSE 8443

ENV THIRDEYE_PLUGINS_DIR "/app/plugins"

COPY --from=builder --chown=1000:1000 /build/thirdeye-distribution/target/thirdeye-distribution-1.168.0-SNAPSHOT-dist/thirdeye-distribution-1.168.0-SNAPSHOT/ /app/

ENTRYPOINT ["sh", "bin/thirdeye.sh"]
