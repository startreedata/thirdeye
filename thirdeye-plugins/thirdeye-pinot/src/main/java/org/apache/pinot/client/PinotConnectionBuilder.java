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
package org.apache.pinot.client;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.singletonList;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotConnectionBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionBuilder.class);
  private static final long CONNECTION_TIMEOUT = 60000;
  public static int MAX_CONNECTIONS;

  static {
    try {
      MAX_CONNECTIONS = Integer.parseInt(System.getProperty("max_pinot_connections", "2"));
    } catch (final Exception e) {
      MAX_CONNECTIONS = 2;
    }
  }

  private Connection[] fromHostList(final List<String> thirdeyeBrokers,
      final PinotClientTransport transport) {
    final Connection[] connections = new Connection[MAX_CONNECTIONS];
    for (int i = 0; i < MAX_CONNECTIONS; ++i) {
      connections[i] = ConnectionFactory.fromHostList(thirdeyeBrokers, transport);
    }
    return connections;
  }

  private Connection[] fromZookeeper(final String zkUrl,
      final PinotClientTransport transport) throws Exception {
    final Callable<Connection> callable = () -> ConnectionFactory.fromZookeeper(zkUrl, transport);
    return fromFutures(executeReplicated(callable, MAX_CONNECTIONS));
  }

  private <T> Collection<Future<T>> executeReplicated(final Callable<T> callable,
      final int n) {
    final ExecutorService executor = Executors.newCachedThreadPool();
    final Collection<Future<T>> futures = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      futures.add(executor.submit(callable));
    }
    executor.shutdown();
    return futures;
  }

  private Connection[] fromFutures(final Collection<Future<Connection>> futures)
      throws Exception {
    final Connection[] connections = new Connection[futures.size()];
    int i = 0;
    for (final Future<Connection> f : futures) {
      connections[i++] = f.get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    return connections;
  }

  private PinotClientTransport buildTransport(
      final PinotThirdEyeDataSourceConfig config) {
    final ThirdEyeJsonAsyncHttpPinotClientTransportFactory factory =
        new ThirdEyeJsonAsyncHttpPinotClientTransportFactory();

    optional(config.getControllerConnectionScheme()).ifPresent(
        schema -> {
          factory.setScheme(schema);
          if ("https".equals(schema)) {
            try {
              factory.setSslContext(SSLContext.getDefault());
            } catch (final NoSuchAlgorithmException e) {
              LOG.warn("SSL context not set for transport!");
            }
          }
        }
    );

    optional(config.getHeaders())
        .filter(headers -> !headers.isEmpty())
        .ifPresent(factory::setHeaders);

    optional(config.getReadTimeoutMs())
        .ifPresent(factory::setReadTimeoutMs);

    optional(config.getRequestTimeoutMs())
        .ifPresent(factory::setRequestTimeoutMs);

    optional(config.getConnectTimeoutMs())
        .ifPresent(factory::setConnectTimeoutMs);

    optional(config.getBrokerResponseTimeoutMs())
        .ifPresent(factory::setBrokerResponseTimeoutMs);

    return factory.buildTransport();
  }

  public Connection[] createConnections(final PinotThirdEyeDataSourceConfig config)
      throws Exception {
    final String brokerUrl = config.getBrokerUrl();
    final PinotClientTransport transport = buildTransport(config);

    final Connection[] connections;
    if (brokerUrl != null && brokerUrl.trim().length() > 0) {
      connections = fromHostList(singletonList(brokerUrl), transport);
      LOG.info("Created pinot transport with brokers [{}]", brokerUrl);
    } else {
      connections = fromZookeeper(
          String.format("%s/%s", config.getZookeeperUrl(),
              config.getClusterName()),
          transport);
      LOG.info("Created pinot transport with controller {}:{}",
          config.getControllerHost(),
          config.getControllerPort());
    }
    return connections;
  }
}
