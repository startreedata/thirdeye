/*
 * Copyright 2023 StarTree Inc
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
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotConnectionBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionBuilder.class);

  public Connection createConnection(final PinotThirdEyeDataSourceConfig config) {
    final String brokerUrl = config.getBrokerUrl();
    final PinotClientTransport transport = buildTransport(config);

    final Connection connection;
    if (brokerUrl != null && brokerUrl.trim().length() > 0) {
      connection = ConnectionFactory.fromHostList(singletonList(brokerUrl), transport);
      LOG.info("Created pinot transport with brokers [{}]", brokerUrl);
    } else {
      connection = ConnectionFactory.fromZookeeper(String.format("%s/%s",
          config.getZookeeperUrl(),
          config.getClusterName()), transport);
      LOG.info("Created pinot transport with controller {}:{}",
          config.getControllerHost(),
          config.getControllerPort());
    }
    return connection;
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

}
