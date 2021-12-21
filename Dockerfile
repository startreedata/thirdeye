FROM adoptopenjdk/openjdk11:alpine

EXPOSE 8080

RUN addgroup -S thirdeye && \
  adduser -S thirdeye -G thirdeye && \
  chown thirdeye:thirdeye /home/thirdeye

USER thirdeye
WORKDIR /home/thirdeye

COPY thirdeye-distribution/target/thirdeye-distribution-*-dist.tar.gz thirdeye-distribution-dist.tar.gz

RUN mkdir thirdeye && tar -xvf thirdeye-distribution-dist.tar.gz -C thirdeye --strip-components=1 && \
  rm thirdeye-distribution-dist.tar.gz

WORKDIR /home/thirdeye/thirdeye

ENTRYPOINT ["sh", "bin/thirdeye.sh"]
