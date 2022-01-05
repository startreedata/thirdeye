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

package org.apache.pinot.thirdeye.datasource.pinotsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlControllerResponseCacheLoader extends PinotSqlResponseCacheLoader {

  private static final Logger LOG = LoggerFactory
      .getLogger(PinotSqlControllerResponseCacheLoader.class);

  private static final long CONNECTION_TIMEOUT = 60000;
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

  private static Connection[] fromController(
      final PinotSqlThirdEyeDataSourceConfig pinotSqlThirdEyeDataSourceConfig) throws Exception {
    Properties info = new Properties();
    Map<String, String> headers = pinotSqlThirdEyeDataSourceConfig.getHeaders();
    for(String header : headers.keySet()){
      info.setProperty(String.format("headers.%s", header), headers.get(header));
    }
    info.setProperty("scheme", pinotSqlThirdEyeDataSourceConfig.getControllerConnectionScheme());
    Callable<Connection> callable = () -> DriverManager.getConnection(pinotSqlThirdEyeDataSourceConfig.connectionUrl(),
      info);
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

  /**
   * Initializes the cache loader using the given property map.
   *
   * @param properties the property map that provides controller's information.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  public void init(Map<String, Object> properties) throws Exception {
    PinotSqlThirdEyeDataSourceConfig dataSourceConfig = PinotSqlDataSourceConfigFactory
        .createFromProperties(properties);
    this.init(dataSourceConfig);
  }

  /**
   * Initializes the cache loader using the given data source config.
   *
   * @param pinotSqlThirdEyeDataSourceConfig the data source config that provides controller's
   *     information.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  private void init(PinotSqlThirdEyeDataSourceConfig pinotSqlThirdEyeDataSourceConfig) throws Exception {
    if (pinotSqlThirdEyeDataSourceConfig.getControllerHost() != null
        && pinotSqlThirdEyeDataSourceConfig.getControllerPort() > 0
        && pinotSqlThirdEyeDataSourceConfig.getControllerHost().trim().length() > 0) {
      this.connections = fromController(pinotSqlThirdEyeDataSourceConfig);
      LOG.info("Created PinotControllerResponseCacheLoader with controller {}:{}",
          pinotSqlThirdEyeDataSourceConfig.getControllerHost(),
          pinotSqlThirdEyeDataSourceConfig.getControllerPort());
    }
  }

  @Override
  public ResultSet load(PinotSqlQuery query) throws Exception {
    try {
      Connection connection = getConnection();
      try {
        synchronized (connection) {
          int activeConnections = this.activeConnections.incrementAndGet();
          long start = System.currentTimeMillis();
          Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(query.getQuery());
          long end = System.currentTimeMillis();
          LOG.info("Query:{}  took:{} ms  connections:{}", query.getQuery(), (end - start),
              activeConnections);
          return resultSet;
        }
      } finally {
        this.activeConnections.decrementAndGet();
      }
    } catch (SQLException cause) {
      LOG.error("Error when running sql:" + query.getQuery(), cause);
      throw new SQLException("Error when running sql:" + query.getQuery(), cause);
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
          } catch (SQLException e) {
            // skip
          }
        }
      }
    }
  }
}
