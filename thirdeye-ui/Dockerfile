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
