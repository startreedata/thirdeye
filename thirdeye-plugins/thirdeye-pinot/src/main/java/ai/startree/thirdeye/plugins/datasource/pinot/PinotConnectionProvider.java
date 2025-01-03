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

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotConnectionUtils.isClosed;
import static ai.startree.thirdeye.spi.Constants.TWO_DIGITS_FORMATTER;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHeaders;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotClientException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotConnectionProvider {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionProvider.class);

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final PinotThirdEyeDataSourceConfig config;
  private Connection connection;
  private String currentToken;
  
  public PinotConnectionProvider(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public Connection get() {
    if (config.isOAuthEnabled()) {
      // fixme cyril every time this method is called and oAuth is enabled, getting the connection results in reading a file
      final String newToken = requireNonNull(PinotOauthUtils.getOauthToken(config.getOauth()), "token supplied is null");
      if (connection == null || !Objects.equals(currentToken, newToken) || isClosed(connection)) {
        // need to update the authorization token
        /* Closing old connection is a lower priority. do it async */
        closeConnectionAsync(connection);
        
        currentToken = newToken;
        final Map<String, String> additionalHeaders = Map.of(HttpHeaders.AUTHORIZATION, currentToken);
        connection = PinotConnectionUtils.createConnection(config, additionalHeaders);
      }
    } else {
      if (connection == null || isClosed(connection)) {
        closeConnectionAsync(connection);
        connection = PinotConnectionUtils.createConnection(config, Map.of());
      }
    }
    return connection;
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
        LOG.info("Successfully closed pinot connection. took {}ms",
            TWO_DIGITS_FORMATTER.format((System.nanoTime() - start) / 1e6));
      }
    } catch (final PinotClientException e) {
      LOG.warn("Exception closing connection: {}", e.getMessage(), e);
    } catch (final Exception e) {
      LOG.error("Exception closing connection: {}", e.getMessage(), e);
    }
  }
}
