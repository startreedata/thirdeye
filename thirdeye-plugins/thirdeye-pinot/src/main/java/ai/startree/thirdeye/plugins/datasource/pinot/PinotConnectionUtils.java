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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransport;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransportFactory;
import org.apache.pinot.client.PinotClientTransport;
import org.asynchttpclient.AsyncHttpClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotConnectionUtils {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionUtils.class);
  private static final String THIRDEYE_CLIENT_USER_AGENT;
  
  // reflection hacks - see usage - will be removed once Connection provides a isClosed method
  private static final Field transportField;
  private static final Field asyncHttpClientField;
  
  static {
    String thirdeyeVersion;
    try {
      thirdeyeVersion = PinotConnectionUtils.class.getPackage().getImplementationVersion();
    } catch (Exception e) {
      thirdeyeVersion = "unknown";
    }
    String pinotClientVersion;
    try {
      pinotClientVersion = JsonAsyncHttpPinotClientTransportFactory.class.getPackage().getImplementationVersion();
    } catch (Exception e) {
      pinotClientVersion = "unknown";
    }
    THIRDEYE_CLIENT_USER_AGENT = "thirdeye/" + thirdeyeVersion + " pinot-java-client/" + pinotClientVersion;
  }
  
  static {
    try {
      transportField = Connection.class.getDeclaredField("_transport");
      transportField.setAccessible(true);
      asyncHttpClientField = JsonAsyncHttpPinotClientTransport.class.getDeclaredField("_httpClient");
      asyncHttpClientField.setAccessible(true); 
    } catch (Exception e) {
      LOG.error("Fatal error. Failed to prepare Pinot Connection.isClosed method by reflection hack. Will not be able to connect to Pinot.");
      throw new RuntimeException(e);
    }
  }

  public static Connection createConnection(final PinotThirdEyeDataSourceConfig config,
      final @NonNull Map<String, String> additionalHeaders) {
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
      final PinotThirdEyeDataSourceConfig config,
      final @NonNull Map<String, String> additionalHeaders) {
    final JsonAsyncHttpPinotClientTransportFactory factory = 
        new JsonAsyncHttpPinotClientTransportFactory();

    final Properties properties = new Properties();
    if (config.getControllerConnectionScheme() != null) {
      final String scheme = config.getControllerConnectionScheme();
      // not using setScheme on purpose - see https://github.com/apache/pinot/issues/14500
      properties.setProperty("scheme", scheme);
      if ("https".equals(scheme)) {
        try {
          factory.setSslContext(SSLContext.getDefault());
        } catch (final NoSuchAlgorithmException e) {
          // FIXME CYRIL - follow up PR --> throw an exception instead of not setting https
          LOG.warn("SSL context not set for transport!");
        }
      }
    }

    final Map<String, String> mergedHeaders = new HashMap<>();
    optional(config.getHeaders()).ifPresent(mergedHeaders::putAll);
    mergedHeaders.putAll(additionalHeaders);
    factory.setHeaders(mergedHeaders);

    properties.setProperty("appId", THIRDEYE_CLIENT_USER_AGENT);
    optional(config.getReadTimeoutMs())
        .ifPresent(v -> properties.setProperty("brokerReadTimeoutMs", v.toString()));
    optional(config.getConnectTimeoutMs())
        .ifPresent(v -> properties.setProperty("brokerConnectTimeoutMs", v.toString()));
    
    if (config.getBrokerResponseTimeoutMs() != null) {
      // using error logs to quickly find who is using this
      LOG.error("brokerResponseTimeoutMs is set in Pinot configuration. This value is ignored and will be removed in a next version.");
    }
    if (config.getRequestTimeoutMs() != null) {
      // using error logs to quickly find who is using this
      LOG.error("requestTimeoutMs is set in Pinot configuration. This value is ignored and will be removed in a next version.");
    }

    return factory.withConnectionProperties(properties).buildTransport();
  }
  
  // hack function to simulate a isClosed method that is not available in the interface provided by Connection
  // will be removed once the Connection provides a isClosed method
  public static boolean isClosed(@NonNull Connection connection) {
    try {
      final JsonAsyncHttpPinotClientTransport transport = (JsonAsyncHttpPinotClientTransport) transportField.get(connection);
      final AsyncHttpClient asyncHttpClient = (AsyncHttpClient) asyncHttpClientField.get(transport);
      return asyncHttpClient.isClosed();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
