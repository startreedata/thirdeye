/*
 * Copyright 2024 StarTree Inc
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
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotConnectionBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionBuilder.class);

  public Connection createConnection(final PinotThirdEyeDataSourceConfig config, final Map<String, String> additionalHeaders) {
    final String brokerUrl = config.getBrokerUrl();
    final PinotClientTransport transport = buildTransport(config, additionalHeaders);

    final Connection connection;
    if (brokerUrl != null && brokerUrl.trim().length() > 0) {
      connection = ConnectionFactory.fromHostList(singletonList(brokerUrl), transport);
      LOG.info("Created pinot transport with brokers [{}]", brokerUrl);
    } else {
      final String zookeeperUrl = requireNonNull(config.getZookeeperUrl(),
          "zookeeperUrl is required if brokerUrl is not provided").trim();
      checkArgument(zookeeperUrl.length() > 0, "if provided, zookeeperUrl cannot be empty");
      connection = ConnectionFactory.fromZookeeper(String.format("%s/%s",
          zookeeperUrl,
          config.getClusterName()), transport);
      LOG.info("Created pinot transport with controller {}:{}",
          config.getControllerHost(),
          config.getControllerPort());
    }
    return connection;
  }

  private static PinotClientTransport buildTransport(
      final PinotThirdEyeDataSourceConfig config, final @NonNull Map<String, String> additionalHeaders) {
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

    final Map<String, String> mergedHeaders = new HashMap<>();
    optional(config.getHeaders()).ifPresent(mergedHeaders::putAll);
    mergedHeaders.putAll(additionalHeaders);
    factory.setHeaders(mergedHeaders);

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
