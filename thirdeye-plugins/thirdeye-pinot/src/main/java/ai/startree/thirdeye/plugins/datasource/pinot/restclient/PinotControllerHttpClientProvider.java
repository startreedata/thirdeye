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

import static ai.startree.thirdeye.spi.Constants.TWO_DIGITS_FORMATTER;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotOauthUtils;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotControllerHttpClientProvider {

  private static final Logger LOG = LoggerFactory.getLogger(PinotControllerHttpClientProvider.class);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  
  private final PinotThirdEyeDataSourceConfig config;
  private CloseableHttpClient pinotControllerClient = null;
  private String currentToken = null;

  public PinotControllerHttpClientProvider(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public CloseableHttpClient get() {
    if (config.isOAuthEnabled()) {
      // fixme cyril - at every call this reads a file 
      final String newToken = requireNonNull(PinotOauthUtils.getOauthToken(config.getOauth()), "token supplied is null");
      if (pinotControllerClient == null || !Objects.equals(currentToken, newToken)) {
        // need to update the authorization token 
        // closing old connection is a lower priority - do it async
        closeClientAsync(pinotControllerClient);
        
        currentToken = newToken;
        final Map<String, String> additionalHeaders = Map.of(HttpHeaders.AUTHORIZATION, currentToken);
        pinotControllerClient = PinotConnectionUtils.createHttpClient(config, additionalHeaders);
      }
    } else {
      if (pinotControllerClient == null) {
        pinotControllerClient = PinotConnectionUtils.createHttpClient(config, Map.of());
      }
    }
    
    return pinotControllerClient;
  }

  private void closeClientAsync(final CloseableHttpClient pinotControllerClient) {
    if (pinotControllerClient != null) {
      executorService.submit(() -> this.closeClient(pinotControllerClient)); 
    }
  }

  private void closeClient(final CloseableHttpClient client) {
    try {
      final long startTime = System.nanoTime();
      if (client != null) {
        client.close();
        LOG.info("Successfully closed pinot controller client. took {}ms",
            TWO_DIGITS_FORMATTER.format((System.nanoTime() - startTime) / 1e6));
      }
    } catch (final IOException e) {
      LOG.warn("Failed to close pinot controller client", e);
    } catch (final Exception e) {
      LOG.error("Failed to close pinot controller client", e);
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
}
