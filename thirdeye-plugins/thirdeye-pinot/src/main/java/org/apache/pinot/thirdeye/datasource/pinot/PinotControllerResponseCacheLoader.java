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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.InstanceConfig;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
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

  private static Connection[] fromHostList(final String[] thirdeyeBrokers) throws Exception {
    Callable<Connection> callable = () -> ConnectionFactory.fromHostList(thirdeyeBrokers);
    return fromFutures(executeReplicated(callable, MAX_CONNECTIONS));
  }

  private static Connection[] fromZookeeper(
      final PinotThirdEyeDataSourceConfig pinotThirdEyeDataSourceConfig) throws Exception {
    Callable<Connection> callable = () -> ConnectionFactory.fromZookeeper(
        pinotThirdEyeDataSourceConfig.getZookeeperUrl()
            + "/" + pinotThirdEyeDataSourceConfig.getClusterName());
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
    List<String> brokerList = pinotThirdEyeDataSourceConfig.getBrokerList();
    if(brokerList != null && !brokerList.isEmpty()){
      this.connections = fromHostList(brokerList.toArray(new String[brokerList.size()]));
      LOG.info("Created PinotControllerResponseCacheLoader with brokers {}", pinotThirdEyeDataSourceConfig.getBrokerList());
    } else if (pinotThirdEyeDataSourceConfig.getBrokerUrl() != null
        && pinotThirdEyeDataSourceConfig.getBrokerUrl().trim().length() > 0) {
      ZkClient zkClient = new ZkClient(pinotThirdEyeDataSourceConfig.getZookeeperUrl());
      zkClient.setZkSerializer(new ZNRecordSerializer());
      zkClient.waitUntilConnected(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
      ZKHelixAdmin helixAdmin = new ZKHelixAdmin(zkClient);
      List<String> thirdeyeBrokerList = helixAdmin.getInstancesInClusterWithTag(
          pinotThirdEyeDataSourceConfig.getClusterName(), pinotThirdEyeDataSourceConfig.getTag());

      String[] thirdeyeBrokers = new String[thirdeyeBrokerList.size()];
      for (int i = 0; i < thirdeyeBrokerList.size(); i++) {
        String instanceName = thirdeyeBrokerList.get(i);
        InstanceConfig instanceConfig =
            helixAdmin
                .getInstanceConfig(pinotThirdEyeDataSourceConfig.getClusterName(), instanceName);
        thirdeyeBrokers[i] = instanceConfig.getHostName().replaceAll(BROKER_PREFIX, "") + ":"
            + instanceConfig.getPort();
      }
      this.connections = fromHostList(thirdeyeBrokers);
      LOG.info("Created PinotControllerResponseCacheLoader with brokers {}", thirdeyeBrokers);
    } else {
      this.connections = fromZookeeper(pinotThirdEyeDataSourceConfig);
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
