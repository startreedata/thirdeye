/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.datasource.pinot;

import static org.apache.pinot.client.PinotConnectionBuilder.MAX_CONNECTIONS;

import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import com.google.common.cache.CacheLoader;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.PinotConnectionBuilder;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSetGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotResponseCacheLoader extends CacheLoader<PinotQuery, ThirdEyeResultSetGroup> {

  private static final Logger LOG = LoggerFactory.getLogger(PinotResponseCacheLoader.class);

  private static final String SQL_QUERY_FORMAT = "sql";
  private static final String PQL_QUERY_FORMAT = "pql";

  private final AtomicInteger activeConnections = new AtomicInteger();
  private final PinotThirdEyeDataSourceConfig config;
  private Connection[] connections;

  public PinotResponseCacheLoader(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public void initConnections() throws Exception {
    connections = new PinotConnectionBuilder().createConnections(config);
  }

  @Override
  public ThirdEyeResultSetGroup load(final PinotQuery pinotQuery) {
    try {
      final Connection connection = getConnection();
      try {
        synchronized (connection) {
          final int activeConnections = this.activeConnections.incrementAndGet();
          final long start = System.currentTimeMillis();
          final String queryFormat = pinotQuery.isUseSql() ? SQL_QUERY_FORMAT : PQL_QUERY_FORMAT;
          final ResultSetGroup resultSetGroup = connection.execute(
              pinotQuery.getTableName(),
              new Request(queryFormat, pinotQuery.getQuery()));
          final long end = System.currentTimeMillis();
          LOG.info("Query:{}  took:{} ms  connections:{}",
              pinotQuery.getQuery().replace('\n', ' '),
              (end - start),
              activeConnections);

          return PinotThirdEyeDataSourceUtils.toThirdEyeResultSetGroup(resultSetGroup);
        }
      } finally {
        activeConnections.decrementAndGet();
      }
    } catch (final PinotClientException cause) {
      LOG.error("Error when running pql:" + pinotQuery.getQuery(), cause);
      throw new PinotClientException("Error when running pql:" + pinotQuery.getQuery(), cause);
    }
  }

  public Connection getConnection() {
    return connections[(int) (Thread.currentThread().getId() % MAX_CONNECTIONS)];
  }

  public void close() {
    if (connections != null) {
      Arrays.stream(connections)
          .filter(Objects::nonNull)
          .forEach(this::close);
    }
  }

  private void close(final Connection connection) {
    try {
      connection.close();
    } catch (final PinotClientException e) {
      // skip
    }
  }
}
