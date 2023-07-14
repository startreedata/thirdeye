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

FROM node:14.21.3-bullseye as builder

WORKDIR /build

COPY ./package.json ./package-lock.json ./
RUN npm ci

COPY ./ ./
RUN npm run build

FROM nginx:1.24-alpine

RUN addgroup -g 1000 thirdeye && \
    adduser -u 1000 thirdeye -G thirdeye -H -D

USER thirdeye

WORKDIR /app

RUN mkdir -p nginx/conf.d nginx/log

ENV NGINX_ENVSUBST_TEMPLATE_DIR /app/nginx/templates
ENV NGINX_ENVSUBST_OUTPUT_DIR /app/nginx/conf.d
ENV THIRDEYE_API_BASE_URL "http://thirdeye-server:8080"
ENV DNS_RESOLVER "127.0.0.1:53"
ENV NGINX_PORT "8085"

COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/templates/ nginx/templates/
COPY --from=builder --chown=1000:1000 /build/dist /app/dist

