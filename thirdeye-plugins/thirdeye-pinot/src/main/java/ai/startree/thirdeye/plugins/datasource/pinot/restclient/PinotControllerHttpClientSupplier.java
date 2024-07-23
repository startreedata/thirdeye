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
package ai.startree.thirdeye.plugins.datasource.pinot.restclient;

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSource.HTTPS_SCHEME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotOauthUtils;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHeaders;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotControllerHttpClientSupplier {

  private static final Logger LOG = LoggerFactory.getLogger(PinotControllerHttpClientSupplier.class);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final PinotThirdEyeDataSourceConfig config;

  private CloseableHttpClient pinotControllerClient = null;
  private String prevToken = null;

  @Inject
  public PinotControllerHttpClientSupplier(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public CloseableHttpClient get() {
    if (config.isOAuthEnabled()) {
      // fixme cyril - at every call this reads a file 
      final String newToken = requireNonNull(PinotOauthUtils.getOauthToken(config.getOauth()), "token supplied is null");
      if (pinotControllerClient == null || !Objects.equals(prevToken, newToken)) {
        // close former client asynchronously
        executorService.submit(() -> this.closeClient(pinotControllerClient));
        pinotControllerClient = buildPinotControllerClient();
      }
    } else {
      if (pinotControllerClient == null) {
        pinotControllerClient = buildPinotControllerClient();
      }
    }
    
    return pinotControllerClient;
  }

  private void closeClient(final CloseableHttpClient client) {
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) {
        LOG.error("Failed to close pinot controller client", e);
      }  
    }
  }

  private CloseableHttpClient buildPinotControllerClient() {
    final HttpClientBuilder builder = HttpClients.custom();
    configureHeaders(builder);
    configureHttps(builder);

    return builder.build();
  }

  private void configureHeaders(final HttpClientBuilder builder) {
    final Map<String, String> headers = new HashMap<>(
        optional(config.getHeaders()).orElse(Collections.emptyMap()));
    if (config.isOAuthEnabled()) {
      final String value = PinotOauthUtils.getOauthToken(config.getOauth());
      headers.put(HttpHeaders.AUTHORIZATION, value);
      prevToken = value;
    }
    if (!headers.isEmpty()) {
      builder.setDefaultHeaders(headers.entrySet()
          .stream()
          .map(e -> new BasicHeader(e.getKey(), e.getValue()))
          .collect(Collectors.toList()));
    }
  }

  private void configureHttps(final HttpClientBuilder builder) {
    if (HTTPS_SCHEME.equals(config.getControllerConnectionScheme())) {
      try {
        // Accept all SSL certificate because we assume that the Pinot broker are setup in the
        // same internal network
        final SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, new AcceptAllTrustStrategy())
            .build();
        builder.setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier());
      } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
        // This section shouldn't happen because we use Accept All Strategy
        LOG.error("Failed to start auto onboard for Pinot data source.");
        throw new RuntimeException(e);
      }
    }
  }

  public void close() {
    if (pinotControllerClient != null) {
      try {
        pinotControllerClient.close();
      } catch (IOException e) {
        LOG.error("Exception closing pinotControllerClient", e);
      }
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
