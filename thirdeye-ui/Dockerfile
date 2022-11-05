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
#
# See the License for the specific language governing permissions and limitations under
# the License.
#

FROM nginx:1.19.5-alpine

RUN addgroup -S thirdeye && \
    adduser -S thirdeye -G thirdeye && \
    chown thirdeye:thirdeye /home/thirdeye

USER thirdeye

WORKDIR /home/thirdeye

RUN mkdir -p nginx/conf.d && \
    mkdir nginx/log

COPY dist/ /etc/nginx/html/
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/templates/ nginx/templates/

ENV NGINX_ENVSUBST_TEMPLATE_DIR /home/thirdeye/nginx/templates
ENV NGINX_ENVSUBST_OUTPUT_DIR /home/thirdeye/nginx/conf.d
