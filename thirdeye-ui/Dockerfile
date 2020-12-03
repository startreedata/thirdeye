FROM nginx:1.19.5-alpine

COPY dist/ /etc/nginx/html/

COPY docker/nginx.conf /etc/nginx/nginx.conf

COPY docker/templates/ /etc/nginx/templates/