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

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.cloneConfig;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.HttpHeaders;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.PinotConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotConnectionManager {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionManager.class);

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final PinotThirdEyeDataSourceConfig config;
  private final PinotConnectionBuilder pinotConnectionBuilder;
  private Connection connection;
  private String prevToken;

  @Inject
  public PinotConnectionManager(final PinotConnectionBuilder pinotConnectionBuilder,
      final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
    this.pinotConnectionBuilder = pinotConnectionBuilder;
  }

  private boolean isNewConnectionReqd() {
    if (connection == null) {
      return true;
    }
    if (config.getOauth() == null || !config.getOauth().isEnabled()) {
      /* no oauth. no connection update required */
      return false;
    }

    /* oauth case */
    if (prevToken == null) {
      /* no existing token to compare*/
      return true;
    }
    final String newToken = requireNonNull(PinotOauthTokenSupplier.getOauthToken(config.getOauth()), "token supplied is null");
    return !prevToken.equals(newToken);
  }

  public Connection get() {
    if (isNewConnectionReqd()) {
      /* Closing old connection is a lower priority. do it async */
      closeConnectionAsync(connection);

      final PinotThirdEyeDataSourceConfig config = newConfigWithOauthHeader();
      connection = pinotConnectionBuilder.createConnection(config);
      prevToken = optional(config.getHeaders())
          .map(headers -> headers.get(HttpHeaders.AUTHORIZATION))
          .orElse(null);
    }
    return connection;
  }

  private PinotThirdEyeDataSourceConfig newConfigWithOauthHeader() {
    final PinotThirdEyeDataSourceConfig newConfig = cloneConfig(config);

    /* Inject the oauth header into headers */
    final String newToken = PinotOauthTokenSupplier.getOauthToken(config.getOauth());
    if (newConfig.getHeaders() == null) {
      newConfig.setHeaders(new HashMap<>());
    }
    newConfig
        .getHeaders()
        .put(HttpHeaders.AUTHORIZATION, newToken);
    return newConfig;
  }

  public void close() {
    closeConnection(connection);
    connection = null;
  }

  private void closeConnectionAsync(@Nullable final Connection connection) {
    if (connection != null) {
      executorService.submit(() -> closeConnection(connection));
    }
  }

  private void closeConnection(@Nullable final Connection connection) {
    try {
      final long start = System.nanoTime();
      if (connection != null) {
        connection.close();
        LOG.info(String.format("Successfully closed pinot connection. took %.2fms",
            ((System.nanoTime() - start) / 1e6)));
      }
    } catch (final PinotClientException e) {
      LOG.warn("Exception closing connection: {}", e.getMessage(), e);
    } catch (final Exception e) {
      LOG.error("Exception closing connection: {}", e.getMessage(), e);
    }
  }
}
