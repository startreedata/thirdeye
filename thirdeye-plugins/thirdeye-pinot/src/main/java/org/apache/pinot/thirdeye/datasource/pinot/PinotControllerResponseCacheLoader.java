/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource.pinot;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLContext;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransport;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransportFactory;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSetGroup;
import org.apache.pinot.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotControllerResponseCacheLoader extends PinotResponseCacheLoader {

  private static final Logger LOG = LoggerFactory
      .getLogger(PinotControllerResponseCacheLoader.class);

  private static final long CONNECTION_TIMEOUT = 60000;
  private static final String BROKER_PREFIX = "Broker_";
  private static final String SQL_QUERY_FORMAT = "sql";
  private static final String PQL_QUERY_FORMAT = "pql";
  private static int MAX_CONNECTIONS;

  static {
    try {
      MAX_CONNECTIONS = Integer.parseInt(System.getProperty("max_pinot_connections", "2"));
    } catch (Exception e) {
      MAX_CONNECTIONS = 2;
    }
  }

  private final AtomicInteger activeConnections = new AtomicInteger();
  private Connection[] connections;

  private static Connection[] fromHostList(final List<String> thirdeyeBrokers, JsonAsyncHttpPinotClientTransport transport) throws Exception {
    Callable<Connection> callable = () -> ConnectionFactory.fromHostList(thirdeyeBrokers, transport);
    return fromFutures(executeReplicated(callable, MAX_CONNECTIONS));
  }

  private static Connection[] fromZookeeper(final String zkUrl, JsonAsyncHttpPinotClientTransport transport) throws Exception {
    Callable<Connection> callable = () -> ConnectionFactory.fromZookeeper(zkUrl, transport);
    return fromFutures(executeReplicated(callable, MAX_CONNECTIONS));
  }

  private static <T> Collection<Future<T>> executeReplicated(Callable<T> callable, int n) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Collection<Future<T>> futures = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      futures.add(executor.submit(callable));
    }
    executor.shutdown();
    return futures;
  }

  private static Connection[] fromFutures(Collection<Future<Connection>> futures) throws Exception {
    Connection[] connections = new Connection[futures.size()];
    int i = 0;
    for (Future<Connection> f : futures) {
      connections[i++] = f.get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    return connections;
  }

  private static JsonAsyncHttpPinotClientTransport buildTransport(PinotThirdEyeDataSourceConfig config){
    JsonAsyncHttpPinotClientTransportFactory factory = new JsonAsyncHttpPinotClientTransportFactory();
    Optional.ofNullable(config.getControllerConnectionScheme()).ifPresent(
      schema -> {
        factory.setScheme(schema);
        if(schema.equals("https")) {
          try {
            factory.setSslContext(SSLContext.getDefault());
          } catch (NoSuchAlgorithmException e) {
            LOG.warn("SSL context not set for transport!");
          }
        }
      }
    );
    Optional.ofNullable(config.getHeaders()).ifPresent(
      headers -> {
        if (!headers.isEmpty()) {
          factory.setHeaders(headers);
        }
      });
    return (JsonAsyncHttpPinotClientTransport) factory.buildTransport();
  }

  /**
   * Initializes the cache loader using the given property map.
   *
   * @param properties the property map that provides controller's information.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  public void init(Map<String, Object> properties) throws Exception {
    PinotThirdEyeDataSourceConfig dataSourceConfig = PinotThirdEyeDataSourceConfigFactory
        .createFromProperties(properties);
    this.init(dataSourceConfig);
  }

  /**
   * Initializes the cache loader using the given data source config.
   *
   * @param pinotThirdEyeDataSourceConfig the data source config that provides controller's
   *     information.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  private void init(PinotThirdEyeDataSourceConfig pinotThirdEyeDataSourceConfig) throws Exception {
    final String brokerUrl = pinotThirdEyeDataSourceConfig.getBrokerUrl();
    final JsonAsyncHttpPinotClientTransport transport = buildTransport(pinotThirdEyeDataSourceConfig) ;

    if (brokerUrl != null && brokerUrl.trim().length() > 0) {
      this.connections = fromHostList(Collections.singletonList(brokerUrl), transport);
      LOG.info("Created PinotControllerResponseCacheLoader with brokers [{}]", brokerUrl);
    } else {
      this.connections = fromZookeeper(
        String.format("%s/%s", pinotThirdEyeDataSourceConfig.getZookeeperUrl(),
          pinotThirdEyeDataSourceConfig.getClusterName()),
        transport);
      LOG.info("Created PinotControllerResponseCacheLoader with controller {}:{}",
          pinotThirdEyeDataSourceConfig.getControllerHost(),
          pinotThirdEyeDataSourceConfig.getControllerPort());
    }
  }

  @Override
  public ThirdEyeResultSetGroup load(PinotQuery pinotQuery) throws Exception {
    try {
      Connection connection = getConnection();
      try {
        synchronized (connection) {
          int activeConnections = this.activeConnections.incrementAndGet();
          long start = System.currentTimeMillis();
          final String queryFormat = pinotQuery.isUseSql() ? SQL_QUERY_FORMAT : PQL_QUERY_FORMAT;
          ResultSetGroup resultSetGroup = connection
              .execute(pinotQuery.getTableName(), new Request(queryFormat, pinotQuery.getQuery()));
          long end = System.currentTimeMillis();
          LOG.info("Query:{}  took:{} ms  connections:{}", pinotQuery.getQuery(), (end - start),
              activeConnections);

          return ResultSetUtils.toThirdEyeResultSetGroup(resultSetGroup);
        }
      } finally {
        this.activeConnections.decrementAndGet();
      }
    } catch (PinotClientException cause) {
      LOG.error("Error when running pql:" + pinotQuery.getQuery(), cause);
      throw new PinotClientException("Error when running pql:" + pinotQuery.getQuery(), cause);
    }
  }

  @Override
  public Connection getConnection() {
    return connections[(int) (Thread.currentThread().getId() % MAX_CONNECTIONS)];
  }

  @Override
  public void close() {
    if (connections != null) {
      for (Connection connection : connections) {
        if (connection != null) {
          try {
            connection.close();
          } catch (PinotClientException e) {
            // skip
          }
        }
      }
    }
  }

}
