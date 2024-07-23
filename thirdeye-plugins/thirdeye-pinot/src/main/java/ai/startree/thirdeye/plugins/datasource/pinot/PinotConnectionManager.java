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

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.HttpHeaders;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.PinotConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotConnectionManager {

  private static final Logger LOG = LoggerFactory.getLogger(PinotConnectionManager.class);

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final PinotThirdEyeDataSourceConfig config;
  private Connection connection;
  private String currentToken;

  @Inject
  public PinotConnectionManager(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public Connection get() {
    if (config.isOAuthEnabled()) {
      // fixme cyril every time this method is called and oAuth is enabled, getting the connection results in reading a file
      final String newToken = requireNonNull(PinotOauthTokenSupplier.getOauthToken(config.getOauth()), "token supplied is null");
      if (connection == null || !Objects.equals(currentToken, newToken)) {
        // need to update the authorization token
        /* Closing old connection is a lower priority. do it async */
        closeConnectionAsync(connection);
        
        currentToken = newToken;
        final Map<String, String> additionalHeaders = Map.of(HttpHeaders.AUTHORIZATION, currentToken);
        connection = PinotConnectionUtils.createConnection(config, additionalHeaders);
      }
    } else {
      if (connection == null) {
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
