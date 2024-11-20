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

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSource.HTTPS_SCHEME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransportFactory;
import org.apache.pinot.client.PinotClientTransport;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotConnectionUtils {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionUtils.class);
  private static final String THIRDEYE_CLIENT_USER_AGENT;
  
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

  public static CloseableHttpClient createHttpClient(
      final PinotThirdEyeDataSourceConfig config, final @NonNull Map<String, String> additionalHeaders) {
    final HttpClientBuilder builder = HttpClients.custom();
    
    // set headers
    final Map<String, String> mergedHeaders = new HashMap<>();
    optional(config.getHeaders()).ifPresent(mergedHeaders::putAll);
    mergedHeaders.putAll(additionalHeaders);
    builder.setDefaultHeaders(mergedHeaders.entrySet()
        .stream()
        .map(e -> new BasicHeader(e.getKey(), e.getValue()))
        .collect(Collectors.toList()));

    // set ssl context if necessary
    if (HTTPS_SCHEME.equals(config.getControllerConnectionScheme())) {
      builder.setSSLContext(httpsSslContext()).setSSLHostnameVerifier(new NoopHostnameVerifier());
    }

    return builder.build();
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

  // SSL context that accepts all SSL certificate. 
  // Assume that the Pinot brokers are setup in the same internal network
  private static SSLContext httpsSslContext() {
    try {
      return new SSLContextBuilder()
          .loadTrustMaterial(null, new AcceptAllTrustStrategy())
          .build();
    } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      // This section shouldn't happen because we use Accept All Strategy
      LOG.error("Failed to generate SSL context for Pinot in https.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * This class accepts (i.e., ignores) all SSL certificate.
   */
  private static class AcceptAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(final X509Certificate[] x509Certificates, final String s)
        throws CertificateException {
      return true;
    }
  }
}
