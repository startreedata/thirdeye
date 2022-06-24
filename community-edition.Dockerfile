#
# Copyright 2022 StarTree Inc
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

FROM adoptopenjdk/openjdk11:alpine

EXPOSE 8080
EXPOSE 8081
EXPOSE 8443

RUN addgroup -S thirdeye && \
  adduser -S thirdeye -G thirdeye && \
  chown thirdeye:thirdeye /home/thirdeye

USER thirdeye
WORKDIR /home/thirdeye

COPY thirdeye-distribution/target/thirdeye-distribution-*-dist.tar.gz thirdeye-distribution-dist.tar.gz

RUN mkdir thirdeye && tar -xvf thirdeye-distribution-dist.tar.gz -C thirdeye --strip-components=1 && \
  rm thirdeye-distribution-dist.tar.gz

WORKDIR /home/thirdeye/thirdeye

ENTRYPOINT ["sh", "bin/thirdeye-ce.sh"]
